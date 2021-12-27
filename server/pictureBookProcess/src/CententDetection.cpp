#include "../include/CententDetection.h"
#include "../include/FindObjectNode.h"
namespace my_find_object {
ContentDetection::ContentDetection():
  m_vocabulary_(NULL),m_oldCoverID(-1)
{

}

void ContentDetection::ReloadDataFromID(const int &ID)
{
  //如果传入的ID和当前的ID一样就不进行处理
  if(ID == m_oldCoverID)
  {
    return ;
  }

  m_oldCoverID = ID;

  //将ID转化为文件路径名
  string FileName = "/data/PictureReadingData/ContentData/"+std::to_string(ID)+".bin";

  //清除原有数据
  if(NULL != m_vocabulary_)
  {
    delete m_vocabulary_;
    m_vocabulary_ = NULL;
  }
  m_objects_.clear();

  //重新加载数据
  LoadFromFile(FileName);
}

void ContentDetection::LoadFromFile(const string &FileName)
{
  cout << "fileName:" << FileName << endl;
  int objDataSize = 0 ;
  char* begin_ptr = NULL;
  char* cur_ptr=NULL;
  Setting::getTime();
  FILE *filePtr = fopen(FileName.data(), "rb");
  if(NULL == filePtr)
  {
    UDEBUG("open file :%s.\n",FileName.data());
    exit(0);
  }

  fread(&objDataSize,sizeof(objDataSize),1,filePtr);
  begin_ptr = (char*)filePtr;
  vector<int> idsLoaded;
  while(1)
  {
    ObjSignature *obj = new ObjSignature();
    obj->load(filePtr);
    idsLoaded.push_back(obj->id());
    m_objects_.insert(std::make_pair(obj->id(),obj));
    cur_ptr = (char*)filePtr;

    if(ftell(filePtr) >= objDataSize)
    {
       break;
    }
  }
  m_vocabulary_ = new Vocabulary();
  m_vocabulary_->load(filePtr);
  fclose(filePtr);
}

int ContentDetection::detect(const cv::Mat &descriptors, const std::vector<cv::KeyPoint> keypoints)
{
  if(NULL == m_vocabulary_)
  {
    return -1;
  }
  DetectionInfo info;

  bool success = false;

  info.sceneKeypoints_ = keypoints;
  info.sceneDescriptors_ = descriptors;

  bool consistentNNData = (m_vocabulary_->wordToObjects().begin()->second!=-1 && Setting::getValue_bool("invertedSearch"));
  if(info.sceneKeypoints_.size() && consistentNNData)
  {
    success = true;
    std::multimap<int, int> words;
    for(std::map<int, ObjSignature*>::const_iterator iter = m_objects_.begin(); iter != m_objects_.end(); iter++)
    {
      info.matches_.insert(std::make_pair(iter->first, std::multimap<int, int>()));
    }

    if(Setting::getValue_bool("invertedSearch") || (1 == Setting::getValue_int("threadCount")))
    {

      cv::Mat results;
      cv::Mat dists;
#ifdef HANMING
      int k = Setting::getValue_bool("minDistanceUsed")?1:2;
#else
      int k=2;
#endif
      UDEBUG("m_vocabulary search begin... scan image descriptor is %d  type = %d \n", info.sceneDescriptors_.rows, info.sceneDescriptors_.type());
      Setting::getTime();


      results = cv::Mat(info.sceneDescriptors_.rows, k, CV_32SC1); // results index
      dists = cv::Mat(info.sceneDescriptors_.rows, k, CV_32FC1); // Distance results are CV_32FC1
      m_vocabulary_->search(info.sceneDescriptors_, results, dists, k);


      UDEBUG("m_vocabulary search end... using time(ms): %f \n", Setting::getTime());
      for(int i=0; i<dists.rows; i++)
      {
        bool matched = false;
        if(Setting::getValue_bool("nndrRatioUsed")
           && dists.at<float>(i,0) <= (dists.at<float>(i,1) * Setting::getValue_float("nndrRatio")))
        {
          matched = true;
        }
        if((matched || !Setting::getValue_bool("nndrRatioUsed"))
           && Setting::getValue_bool("minDistanceUsed"))
        {
          if(dists.at<float>(i,0) <= Setting::getValue_float("minDistance"))
          {
            matched = true;
          }
          else
          {
            matched = false;
          }
        }

        if(!matched && !Setting::getValue_bool("nndrRatioUsed")
           && !Setting::getValue_bool("minDistanceUsed")
           && dists.at<float>(i,0) >= 0)
        {
          matched = true;
        }
        if(info.minMatchedDistance_ == -1 || info.minMatchedDistance_ > dists.at<float>(i,0))
        {
          info.minMatchedDistance_ = dists.at<float>(i,0);
        }
        if(info.maxMatchedDistance_ == -1 || info.maxMatchedDistance_ < dists.at<float>(i,0))
        {
          info.maxMatchedDistance_ = dists.at<float>(i,0);
        }

        if(matched)
        {
          int wordID = results.at<int>(i,0);
          if(Setting::getValue_bool("invertedSearch"))
          {
            info.sceneWords_.insert(std::make_pair(wordID,i));
            std::vector<int> objIds = getMapValues(m_vocabulary_->wordToObjects(),wordID);
            for(int j=0; j<objIds.size(); j++)
            {
              int count = 0;
              std::vector<int>::iterator iter=objIds.begin();
              advance(iter,j);
              std::multimap<int, int>::const_iterator m =m_vocabulary_->wordToObjects().find(wordID);
              int wordIDLen = m_vocabulary_->wordToObjects().count(wordID);
              for(k = 0; k < wordIDLen; k++,m++)
              {
                if( m->second == *iter )
                {
                    count++;
                }
              }
              if( count == 1 )
              {
                (info.matches_.find(*iter)->second).insert(pair<int, int>((((m_objects_.find(*iter)->second)->words()).find(wordID))->second, i));
              }
            }
          }
          else
          {
            //....
          }
        }
      }
    }
    else
    {

    }

#if 1
    int threadCounts = Setting::getValue_int("threadCount");
    if(threadCounts == 0)
    {
      threadCounts = info.matches_.size();
    }
    std::vector<int> matchesId = getMapKeys(info.matches_);
    std::vector< std::multimap<int,int> > matchesList = getMapValues(info.matches_);
    for(int i=0; i<matchesList.size(); i+=threadCounts)
    {
      vector<HomographyThread*> threads;

      for(int k=i; k<i+threadCounts && k<matchesList.size(); ++k)
      {
        int objectId = matchesId[k];
        ObjSignature *obj = getMapValue(m_objects_,objectId);
        threads.push_back(new HomographyThread(
            &matchesList[k],
            objectId,
            &obj->keypoints(),
            &info.sceneKeypoints_));
        threads.back()->start();
      }

      for(int j=0; j<threads.size(); ++j)
      {

        int id = threads[j]->getObjectId();
        DetectionInfo::RejectedCode code = DetectionInfo::kRejectedUndef;
        if(threads[j]->getHomography().empty())
        {
          code = threads[j]->rejectedCode();
        }
        if(code == DetectionInfo::kRejectedUndef &&
           threads[j]->getInliers().size() < 10	)
        {
          code = DetectionInfo::kRejectedLowInliers;
        }

        if(code == DetectionInfo::kRejectedUndef)
        {
          info.objDetected_.insert(std::make_pair(id, threads[j]->getHomography()));
        }
        else
        {

        }
      }

      for(int j=0; j<threads.size(); ++j)
      {
        delete threads.at(j);
      }
    }
#endif
  }

  std::multimap<int, cv::Mat>::iterator it = info.objDetected_.begin();
  if(it != info.objDetected_.end())
  {
    return it->first;
  }
  else
  {
    return -1;
  }
}


}
