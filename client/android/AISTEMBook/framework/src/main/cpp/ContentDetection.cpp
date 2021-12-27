#include "../include/ContentDetection.h"
#include "../include/FindObjectNode.h"
namespace my_find_object {
ContentDetection::ContentDetection():
  m_vocabulary_(new Vocabulary()),m_oldCoverID(-1),m_datatype(0)
{

}

void ContentDetection::clear()
{
  std::map<int, ObjSignature*>::iterator it;

  /*if(NULL != m_vocabulary_)
  {
    delete m_vocabulary_;
    m_vocabulary_ = NULL;
  }*/
  m_vocabulary_->clear();

  it = m_objects_.begin();
  while(it != m_objects_.end())
  {
    if( it->second != NULL )   
    delete it->second;
    it++;
  }
  m_objects_.clear();
  m_oldCoverID = -1;
}

void ContentDetection::ReloadDataFromID(const int &ID)
{
  //如果传入的ID和当前的ID一样就不进行处理
  if(ID == m_oldCoverID)
  {
    return ;
  }
  if( ID >= 1000 )
  {
    m_datatype = 0; //KDTREE
  }
  else{
    m_datatype = 1;//kmeans
  }
   //清除原有数据
	clear();

  m_oldCoverID = ID;

  //将ID转化为文件路径名
  char name[100] = {};
  sprintf(name,"%d",ID);
  string FileName = "/sdcard/data/book/cover/data/";
  FileName += name;
  FileName += ".data";

  //重新加载数据
  LoadFromFile(FileName);
}

void ContentDetection::LoadFromFile(const string &FileName)
{
  UDEBUG("filename:%s",FileName.data());
  int featureType = 0;
  int objDataSize = 0 ;
  char* begin_ptr = NULL;
  char* cur_ptr=NULL;
  Setting::getTime();
  FILE *filePtr = fopen(FileName.data(), "rb");
  if(NULL == filePtr)
  {
    UDEBUG("open file fail:%s.\n",FileName.data());
    exit(0);
  }

    UDEBUG("LoadFromFile:%d.\n",m_datatype);

  fread(&featureType,sizeof(featureType),1,filePtr); //no use now

  fread(&objDataSize,sizeof(objDataSize),1,filePtr);
  begin_ptr = (char*)filePtr;
//  vector<int> idsLoaded;
  while(1)
  {
    ObjSignature *obj = new ObjSignature();
    if( m_datatype == 0 )
    {
      obj->load(filePtr);
    }
    else
    {
      obj->load_ex(filePtr);
    }
//    idsLoaded.push_back(obj->id());
	  UDEBUG("load object Id is :%d",obj->id());
    m_objects_.insert(std::make_pair(obj->id(),obj));
    cur_ptr = (char*)filePtr;

    if(ftell(filePtr) >= objDataSize)
    {
       break;
    }
  }
  //m_vocabulary_ = new Vocabulary();
  if(m_datatype == 0 )
  {
    m_vocabulary_->load(filePtr);
  }
  else
  {
    m_vocabulary_->load_ex(filePtr);
  }
  fclose(filePtr);
}



int ContentDetection::detect(const cv::Mat &descriptors, const std::vector<cv::KeyPoint> keypoints)
{
  if(NULL == m_vocabulary_ || descriptors.empty() || (keypoints.size() != descriptors.rows) )
  {
	  UDEBUG("NULL == m_vocabulary_ || descriptors.empty() || (keypoints.size() != descriptors.rows) \n");
    return -1;
  }
  DetectionInfo info;

  bool success = false;

  info.sceneKeypoints_ = keypoints;
  info.sceneDescriptors_ = descriptors;
    //UDEBUG("info.sceneKeypoints_.size() = %d \n",info.sceneKeypoints_.size());
  bool consistentNNData = m_vocabulary_->wordToObjects().begin()->second !=-1 ;
  if(info.sceneKeypoints_.size() && consistentNNData)
  {
	  
    success = true;
    std::multimap<int, int> words;
    for(std::map<int, ObjSignature*>::const_iterator iter = m_objects_.begin(); iter != m_objects_.end(); iter++)
    {
      info.matches_.insert(std::make_pair(iter->first, std::multimap<int, int>()));
    }

    if(true)
    {

      cv::Mat results;
      cv::Mat dists;
      int k=2;
      //UDEBUG("m_vocabulary search begin... scan image descriptor is %d  type = %d \n", info.sceneDescriptors_.rows, info.sceneDescriptors_.type());
      Setting::getTime();


      results = cv::Mat(info.sceneDescriptors_.rows, k, CV_32SC1); // results index
      dists = cv::Mat(info.sceneDescriptors_.rows, k, CV_32FC1); // Distance results are CV_32FC1
      if( m_datatype == 0 )
      {
        m_vocabulary_->search(info.sceneDescriptors_, results, dists, k);
      }
      else
      {
        m_vocabulary_->search_ex(info.sceneDescriptors_, results, dists, k);
      }


      //UDEBUG("m_vocabulary search end... using time(ms): %f \n", Setting::getTime());
	    
      for(int i=0; i<dists.rows; i++)
      {
        bool matched = false;
        if(dists.at<float>(i,0) <= (dists.at<float>(i,1) * 0.8))
        {
          matched = true;
        }
        if((matched || !Setting::getValue_bool("nndrRatioUsed"))
           && Setting::getValue_bool("minDistanceUsed"))
        {
          if(dists.at<float>(i,0) <= 35)
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
          if(true)
          {
            info.sceneWords_.insert(std::make_pair(wordID,i));
            std::vector<int> objIds = getMapValues(m_vocabulary_->wordToObjects(),wordID);
//	          UDEBUG("objIds = %d \n",objIds.size());
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
	              int idNum = *iter;
	              std::map<int, ObjSignature*>::const_iterator its1 = m_objects_.find(idNum);
	              if(its1 != m_objects_.end())
	              {
		              if(NULL != its1->second)
		              {
			              std::map<int,int>::const_iterator it2 = its1->second->words().find(wordID);
			              if(it2 != its1->second->words().end())
			              {
				              std::map<int, std::multimap<int, int> >::iterator its3 = info.matches_.find(idNum);
				              if (its3 != info.matches_.end())
				              {
					              (its3->second).insert(std::make_pair(it2->second, i));
				              }
			              }
		              }
	              }
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
	  //UDEBUG("m_vocabulary matchs end... using time(ms): %f \n", Setting::getTime());
#if 1
    int threadCounts = 8;
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

      //info.objDetected2_.clear();
      //info.objDetected3_.clear();

      for(int j=0; j<threads.size(); ++j)
      {
	      threads[j]->wait();
        int id = threads[j]->getObjectId();
        //UDEBUG("id=: %d getInliers=%d  counts=%d \n",id,threads[j]->getInliers().size(),threads[j]->getMatchCounts());
        DetectionInfo::RejectedCode code = DetectionInfo::kRejectedUndef;
        if(threads[j]->getHomography().empty())
        {
          code = threads[j]->rejectedCode();
        }
        /*if(code == DetectionInfo::kRejectedUndef &&
           threads[j]->getInliers().size() < 10 )
        {
          code = DetectionInfo::kRejectedLowInliers;
        }*/

        if(code == DetectionInfo::kRejectedUndef)
        {
          info.objDetected2_.insert(std::make_pair(id, threads[j]->getMatchCounts()));
          info.objDetected3_.insert(std::make_pair(id, threads[j]->getInliers().size()));
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
	//UDEBUG("m_vocabulary all end... using time(ms): %f \n", Setting::getTime());
	//UDEBUG("info.objDetected2_ size: %d \n", info.objDetected3_.size());
	
  std::multimap<int,int>::iterator it_1 = info.objDetected3_.begin();
  int max_1 = 0 , max_2 = 0 ,max_id = 0;
	if(it_1 != info.objDetected3_.end())
  {
	  for(;it_1 != info.objDetected3_.end(); it_1++ )
	  {
	  //UDEBUG(" it_1->second: %d \n",  it_1->second);
		  if( max_1 < it_1->second)
		  {
		    max_1 =  it_1->second;
		    max_id = it_1->first;
		  }
	  }
  }

  //UDEBUG("max_1=%d  \n", max_1);

  it_1 = info.objDetected3_.begin();

  if(it_1 != info.objDetected3_.end())
  {
	  for(;it_1 != info.objDetected3_.end(); it_1++ )
	  {
		  if( max_2 < it_1->second && it_1->second < max_1  )
		  {
		    max_2 =  it_1->second;
		  }
	  }
  }
  //UDEBUG("max_1=%d  max_2=%d \n", max_1,max_2);

  if( max_1 >= 10 || max_1 -max_2 >=5 )
  {
    std::multimap<int,int>::iterator it = info.objDetected2_.begin();
    if(it != info.objDetected2_.end())
    {
      int count = 0;
  	  int result = -1;
  	  for(;it != info.objDetected2_.end(); it++ )
  	  {
  		  if(count < it->second)
  		  {
  			  count = it->second;
  			  result = it->first;
  		  }
  	  }

  	  //UDEBUG("result=%d  max_id=%d \n", result,max_id);
  	  if( result == max_id )
  	  {
  	  return result;
  	  }
  	  else
  	  {
  	  return -1;
  	  }
  	  
    }
    else
    {
      return -1;
    }
  }
  else
  {
     return -1;
  }
}
	
	double ContentDetection::getEntropy(const cv::Mat &srcImage)
	{
		int hist[256] = {};
		
		for(int row=0; row<srcImage.rows; row++)
		{
			for(int col=0;col<srcImage.cols; col++)
			{
				int index = srcImage.at<uchar>(row,col);
				hist[index] ++;
			}
		}
		
		int imageSize = srcImage.rows*srcImage.cols;
		double result = 0.0;
		for(int k=0; k<256; k++)
		{
			
			if(0 == hist[k])
			{
			
			}
			else
			{
				double temp = (double)hist[k]/(double)imageSize;
				result  = result-temp*(log(temp)/log(2.0));
			}
		}
		cout << result << endl;
		return result;
	}


}
