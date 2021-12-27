#include "../include/FindObjectNode.h"
#include <stdio.h>
#include <stdlib.h>
#include "../include/Vocabulary.h"

namespace my_find_object {

FindObjectNode::FindObjectNode(MyCameraNode *cameraNode, FeatureExtractor *featureNode)
  :m_cameraNode_(cameraNode), m_featureNode_(featureNode),
    m_vocabulary_(new Vocabulary())
{
#if 0
  //save
  loadObjects(Setting::getValue_string("filePath"));
  saveToOneFile(Setting::getValue_string("filePath")+"CoverData.bin");
//  string FilePath = "/data/PictureReadingData/ContentData/";
//  vector<string> fileNameList = getFiles(FilePath);
//  for(int i=0; i<fileNameList.size(); i++)
//  {
//    loadObjects((FilePath + fileNameList[i]+"/"));
//    saveToOneFile(FilePath + fileNameList[i]+".bin");
//  }
  exit(1);
#else
  //load
  loadFromOneFile("/data/PictureBookRead/CoverData.bin");
  cout << "FindObjectNode loadFromOneFile OK!" << endl;
#endif
//  m_cameraNode_->setMyFindObjectNode(this);
}

FindObjectNode::FindObjectNode(FeatureExtractor *featureNode)
      :m_cameraNode_(NULL), m_featureNode_(featureNode),
       m_vocabulary_(new Vocabulary())
{

}


double FindObjectNode::getEntropy(const cv::Mat &srcImage)
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

//    double histPro[256] = {};
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

int FindObjectNode::loadObjects(const std::string &dirPath, bool recursive)
{
  UDEBUG("date path:%s \n",dirPath.data());

  string formats = "*.jpeg *.png *.jpg *.bmp *.tiff *.ppm *.pgm";

  vector<string> fileNameList = getFiles(dirPath);
  vector<int> idsLoaded;
  for(int i=0; i<fileNameList.size(); i++)
  {
    const ObjSignature * s = this->addObject(fileNameList.at(i),dirPath);
    if(s)
    {
      idsLoaded.push_back(s->id());
    }
  }

  if(idsLoaded.size())
  {
    this->updateObjects(idsLoaded);
    this->updateVocabulary(idsLoaded);
  }

  return idsLoaded.size();
}

const ObjSignature *FindObjectNode::addObject(const string &fileName,const string &filePath)
{
  if(fileName.size())
  {
    cv::Mat img = cv::imread(filePath + fileName, cv::IMREAD_GRAYSCALE);
    //cv::resize(img,img,cv::Size(img.cols/4,img.rows/4));
    //printf("cols =%d rows=%d \n",img.cols,img.rows);
    //cv::resize(img,img,cv::Size(600,img.rows*600/img.cols));
    if(!img.empty())
    {
      if(img.channels() != 1 || img.depth() != CV_8U)
      {
        cv::cvtColor(img, img, cv::COLOR_BGR2BGRA);
      }
      int id = 0;
      string name = fileName;
      vector<string> fileInfo = str_spalitby(name,".");
      if(fileInfo.size())
      {
        id = atoi(fileInfo[0].data());
        if(id>0)
        {
          if(m_objects_.end() != m_objects_.find(id))
          {
            UDEBUG("Object already added %d . \n" , id);
            id = 0;
          }
        }
        else
        {
          id = 0;
        }
      }
      else
      {
        //...
      }
      const ObjSignature * s = this->addObject(img, id, filePath);
      if(s)
      {
        return s;
      }
    }
    else
    {
      UDEBUG("Could not read image \"%s\" \n",filePath.data());
    }
  }
  else
  {
   UDEBUG("File path is null!?\n ");
  }
  return 0;
}


const ObjSignature *FindObjectNode::addObject(const cv::Mat &image, int id, const string &filePath)
{
  ObjSignature * s = new ObjSignature(id, image, filePath);
  if(!this->addObject(s))
  {
    delete s;
    return 0;
  }
  return s;
}

bool FindObjectNode::addObject(ObjSignature *obj)
{
  //if(obj->id() && m_objects_.contains(obj->id()))
  if(obj->id() && (m_objects_.end() != m_objects_.find(obj->id())))
  {
    UDEBUG("object with id %d already added! \n", obj->id());
    return false;
  }
  else if(obj->id() == 0)
  {
    obj->setId(Setting::getObjectID());
  }

  m_objects_.insert(std::make_pair(obj->id(), obj));

  return true;
}

void FindObjectNode::updateObjects(const vector<int> &ids)
{
  std::vector<ObjSignature*> objectsList;

  if(ids.size())
  {
    for(int i=0; i<ids.size(); ++i)
    {
      if(m_objects_.end() != m_objects_.find(ids[i]))
      {
        objectsList.push_back(m_objects_[ids[i]]);
      }
      else
      {
        UDEBUG("Not found object %d! \n" ,ids[i]);
      }
    }
  }
  else
  {
    objectsList = getMapValues(m_objects_);
  }
  UDEBUG("共有 %d 张图片 \n ", m_objects_.size());
  if(objectsList.size())
  {
    int threadCounts = 1;
    if(threadCounts == 0)
    {
      threadCounts = objectsList.size();
    }

    if(objectsList.size())
    {
      UDEBUG("Features extraction objects nember is %d \n", (int)objectsList.size());
      /*使用两个ｆｏｒ循环错位将所有的图片分散到threadCounts个线程中*/
      for(int i=0; i<objectsList.size(); i+=threadCounts)
      {
        UDEBUG("ExtractorThread object id= %d \n ", i );
        vector<ExtractorThread*> threads;
        for(int k=i; k<i+threadCounts && k<objectsList.size(); ++k)
        {
          if(!objectsList.at(k)->image().empty())
          {
            threads.push_back(new ExtractorThread(m_featureNode_, objectsList.at(k)->id(), objectsList.at(k)->image()));
            threads.back()->start();
          }
          else
          {

            ObjSignature *obj =  getMapValue(m_objects_,objectsList.at(k)->id());
            obj->setData(std::vector<cv::KeyPoint>(), cv::Mat());
            UDEBUG("%d is iempty.  \n", obj->id());
          }
        }

        for(int j=0; j<threads.size(); ++j)
        {
          int id = threads[j]->objectId();
          UDEBUG("object %d descriptor has %d \n",id ,threads[j]->descriptors().rows);
          ObjSignature *obj =  getMapValue(m_objects_,id );
          obj->setData(threads[j]->keypoints(), threads[j]->descriptors());
        }
      }
      //qDebug("Features extraction from %d objects... done! (%d ms)", objectsList.size(), time.elapsed());
    }
  }
  else
  {
    UDEBUG("No objects to update... \n");
  }
}

void FindObjectNode::updateVocabulary(const vector<int> &ids)
{
  int count = 0;
  int dim = -1;
  int type = -1;
  vector<ObjSignature*> objectsList;
  if(ids.size())
  {
    for(int i=0; i<ids.size(); ++i)
    {
      //if(m_objects_.contains(ids[i]))
      if(m_objects_.end() != m_objects_.find(ids[i]))
      {
        objectsList.push_back(m_objects_[ids[i]]);
      }
    }
    if(m_vocabulary_->size())
    {
      dim = m_vocabulary_->dim();
      type = m_vocabulary_->type();
    }
  }
  else
  {
    m_objectsDescriptors_.clear();
    m_dataRange_.clear();
    m_vocabulary_->clear();
  }

  // Get the total size and verify descriptors
  //验证每个的ｄｅｓｃｒｉｐｔｏｒ是否是同一长度的
  //并统计所有的descriptor的个数(每个descriptor占一行)
  for(int i=0; i<objectsList.size(); ++i)
  {
    if(!objectsList.at(i)->descriptors().empty())
    {
      if(dim >= 0 && objectsList.at(i)->descriptors().cols != dim)
      {
        UDEBUG("Descriptors of the objects are not all the same size! Objects "
            "opened must have all the same size (and from the same descriptor extractor).\n");
        return;
      }
      dim = objectsList.at(i)->descriptors().cols;
      if(type >= 0 && objectsList.at(i)->descriptors().type() != type)
      {
        UDEBUG("Descriptors of the objects are not all the same type! Objects opened "
            "must have been processed by the same descriptor extractor.\n");
        return;
      }
      type = objectsList.at(i)->descriptors().type();
      count += objectsList.at(i)->descriptors().rows;
    }
  }
  UDEBUG("All descriptors rows = %d \n", count);
  // Copy data
  if(count)
  {
    UDEBUG("Updating global descriptors matrix: Objects=%d, total descriptors=%d, dim=%d, type=%d \n",
        (int)m_objects_.size(), count, dim, type);
//    if(!Setting::getValue_bool("invertedSearch")) // invertedSearch = true
    if(false)
    {
#if 1
      //Number of threads used for objects matching and homography computation.
      //0 means as many threads as objects. On InvertedSearch mode, multi-threading has only effect on homography computation.
      if(true)
      {
        // If only one thread, put all descriptors in the same cv::Mat
        int row = 0;
        bool vocabularyEmpty = m_objectsDescriptors_.size() == 0;
        if(vocabularyEmpty)
        {
          m_objectsDescriptors_.insert(std::make_pair(0, cv::Mat(count, dim, type)));
        }
        else
        {
          row = m_objectsDescriptors_.begin()->second.rows;
        }
        for(int i=0; i<objectsList.size(); ++i)
        {
          objectsList[i]->setWords(std::multimap<int,int>());
          if(objectsList.at(i)->descriptors().rows)
          {
            if(vocabularyEmpty)
            {
              cv::Mat dest(m_objectsDescriptors_.begin()->second, cv::Range(row, row+objectsList.at(i)->descriptors().rows));
              objectsList.at(i)->descriptors().copyTo(dest);
            }
            else
            {
              m_objectsDescriptors_.begin()->second.push_back(objectsList.at(i)->descriptors());
            }

            row += objectsList.at(i)->descriptors().rows;
            // dataRange contains the upper_bound for each
            // object (the last descriptors position in the
            // global object descriptors matrix)
            if(objectsList.at(i)->descriptors().rows)
            {
              m_dataRange_.insert(std::make_pair(row-1, objectsList.at(i)->id()));
            }
          }
        }
      }
      else
      {
        for(int i=0; i<objectsList.size(); ++i)
        {
          objectsList[i]->setWords(std::multimap<int,int>());
          m_objectsDescriptors_.insert(std::make_pair(objectsList.at(i)->id(), objectsList.at(i)->descriptors()));
        }
      }
#endif
    }
    else
    {
      bool incremental = false/*Settings::getGeneral_vocabularyIncremental() && !Settings::getGeneral_vocabularyFixed()*/;
      int updateVocabularyMinWords = 2000;
      int addedWords = 0;
      for(int i=0; i<objectsList.size(); ++i)
      {
        std::multimap<int, int> words = m_vocabulary_->addWords(objectsList[i]->descriptors(), objectsList.at(i)->id());
        objectsList[i]->setWords(words);
        addedWords += words.size();
        bool updated = false;
        if(incremental && addedWords && addedWords >= updateVocabularyMinWords)
        {
          m_vocabulary_->update();
          addedWords = 0;
          updated = true;
        }
      }
      if(addedWords)
      {
        m_vocabulary_->update();
      }
    }
  }
}


void FindObjectNode::detect(const cv::Mat &image, DetectionInfo &info)
{
  info = DetectionInfo();

  bool success = false;
  if(image.empty())
  {
    return ;
  }

  cv::Mat grayImage;
  if(image.channels() != 1 || image.depth() != CV_8U)
  {
    cv::cvtColor(image, grayImage, cv::COLOR_BGR2BGRA);
  }
  else
  {
    grayImage = image;
  }
  //UDEBUG("ExtractorThread begin...\n");
  Setting::getTime();
  ExtractorThread extractorThread(m_featureNode_, -1, grayImage);
  extractorThread.start();
  UDEBUG("ExtractorThread using time (s) %f .\n",Setting::getTime());
  info.sceneKeypoints_ = extractorThread.keypoints();
  info.sceneDescriptors_ = extractorThread.descriptors();

  bool consistentNNData = (m_vocabulary_->wordToObjects().begin()->second!=-1 && Setting::getValue_bool("invertedSearch"));
  if(/*(descriptorsValid || vocabularyValid) && */info.sceneKeypoints_.size() && consistentNNData)
  {
    success = true;
    std::multimap<int, int> words;
    for(std::map<int, ObjSignature*>::const_iterator iter = m_objects_.begin(); iter != m_objects_.end(); iter++)
    {
      info.matches_.insert(std::make_pair(iter->first, std::multimap<int, int>()));
    }

    if(Setting::getValue_bool("invertedSearch") || true)
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
      if(!Setting::getValue_bool("invertedSearch"))///默认为false
      {

        //match objects to scene
        results = cv::Mat(m_objectsDescriptors_.begin()->second.rows, k, CV_32SC1); // results index
        dists = cv::Mat(m_objectsDescriptors_.begin()->second.rows, k, CV_32FC1); // Distance results are CV_32FC1
        m_vocabulary_->search(m_objectsDescriptors_.begin()->second, results, dists, k);
      }
      else
      {
        results = cv::Mat(info.sceneDescriptors_.rows, k, CV_32SC1); // results index
        dists = cv::Mat(info.sceneDescriptors_.rows, k, CV_32FC1); // Distance results are CV_32FC1
        m_vocabulary_->search(info.sceneDescriptors_, results, dists, k);
      }
      UDEBUG("m_vocabulary search end... using time(ms): %f \n", Setting::getTime());
      for(int i=0; i<dists.rows; i++)
      {
        bool matched = false;
        if(Setting::getValue_bool("nndrRatioUsed")
           && dists.at<float>(i,0) <= (dists.at<float>(i,1) * 0.8))
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
            std::map<int, int>::const_iterator iter = m_dataRange_.lower_bound(i);
            int objectId = iter->second;
            int fisrtObjectDescriptorIndex = (iter == m_dataRange_.begin())?0:(--iter)->first+1;
            int objectDescriptorIndex = i - fisrtObjectDescriptorIndex;

            if(words.count(wordID) == 1)
            {
              getMapValue(info.matches_,objectId).insert(std::make_pair(objectDescriptorIndex, words.find(wordID)->second));
            }
          }
        }
      }
    }
    else
    {
      //...
    }

#if 1
    int threadCounts = 1;
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
        threads.push_back(new HomographyThread(&matchesList[k],objectId,&obj->keypoints(),&info.sceneKeypoints_));
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
          //...
        }
      }

      for(int j=0; j<threads.size(); ++j)
      {
        delete threads.at(j);
      }
    }
#endif
  }

}

int FindObjectNode::detect(const cv::Mat &image, int retArr[])
{
  DetectionInfo info;
  this->detect(image, info);
  m_keyPoints_ = info.sceneKeypoints_;

  cv::Mat destImage;
  //cv::drawKeypoints(image,m_keyPoints_, destImage);
  int count = 0;
  std::multimap<int, cv::Mat>::iterator it = info.objDetected_.begin();
  for(; it != info.objDetected_.end(); it++)
  {
    retArr[count] = it->first;
    count ++;
    if(count > 10)
    {
      break;
    }
  }
  return count;

}


void FindObjectNode::imageFeatureExtractor(const cv::Mat &image)
{
#if SHOW_IMAGE
//  cv::imshow("src", image);
#endif

  DetectionInfo info;

  this->detect(image, info);
  m_keyPoints_ = info.sceneKeypoints_;
  cout << info.objDetected_.begin()->first << endl;

#if SHOW_IMAGE
  cv::Mat destImage;
  //cv::drawKeypoints(image,m_keyPoints_, destImage);

  std::multimap<int, cv::Mat>::iterator it = info.objDetected_.begin();
  for(; it != info.objDetected_.end(); it++)
  {
    const cv::Mat & H = it->second;
    ObjSignature *obj = getMapValue(m_objects_,it->first);
    std::vector<cv::Point2d> src = obj->rect();
    std::vector<cv::Point2d> dest;
    cv::perspectiveTransform(src,dest, H);
    cv::line(destImage, dest[0],dest[1],CV_RGB(255,0,0),5);
    cv::line(destImage, dest[1],dest[2],CV_RGB(255,0,0),5);
    cv::line(destImage, dest[2],dest[3],CV_RGB(255,0,0),5);
    cv::line(destImage, dest[3],dest[0],CV_RGB(255,0,0),5);
  }
//  cv::imshow("dest", destImage);
//  cv::waitKey(1);
#endif
}

void FindObjectNode::saveToOneFile(const string &FileName)
{
  Setting::getTime();
  int objDataSzie = 0;
  FILE *filePtr = fopen(FileName.data(), "wb");
  if(NULL == filePtr)
  {
    UDEBUG("open file : %s.\n",FileName.data());
    exit(0);
  }

  //保存特征类型  1:SURF  2:ORB  3:SIFT
  int featureType = m_featureNode_->getFeatureType();
  fwrite(&featureType, sizeof(featureType), 1, filePtr);

  UDEBUG("write featureType success...\n");
  for(std::map<int, ObjSignature*>::const_iterator it = m_objects_.begin(); it != m_objects_.end(); it ++)
  {
    //此处不再保存特征

    objDataSzie += it->second->getDataSize();
  }
  fwrite(&objDataSzie, sizeof(objDataSzie), 1, filePtr);
  UDEBUG("write objDataSzie<%d> success...\n",objDataSzie);
  for(std::map<int, ObjSignature*>::const_iterator it = m_objects_.begin(); it != m_objects_.end(); it ++)
  {
    //此处不再保存特征
    it->second->save(filePtr);

  }
  UDEBUG("write m_objects_ success...\n");
  m_vocabulary_->save(filePtr);
  UDEBUG("write m_vocabulary_ success...\n");
  fclose(filePtr);
}

void FindObjectNode::loadFromOneFile(const string &FileName)
{
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

  int featureType = 0;
  fread(&featureType,sizeof(featureType),1,filePtr);
  if(m_featureNode_->getFeatureType() != featureType)
  {
    UDEBUG("feature type is different with File:%s and FeatureExtractor.\n",FileName.data());
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
  m_vocabulary_->load(filePtr);
  fclose(filePtr);
}

  FindObjectNode::~FindObjectNode()
  {
    UDEBUG("~FindObjectNode.......");
    std::map<int, ObjSignature*>::iterator it = m_objects_.begin();
    while(it != m_objects_.end())
    {
      UDEBUG("delete m_objects_....\n");
      delete it->second;
      m_objects_.erase(it++);
    }
    if(NULL != m_vocabulary_)
    {
      delete m_vocabulary_;
      m_vocabulary_ = NULL;
    }
  }

bool isImageFlie( char* fileName )//以全数字命名，并且.jpg,jpeg,png后缀的文件
{
  char * p = fileName;
  
  if ( p == NULL )
    return false;
    
  while(*p != 0)
  {
    if(*p == '.')
      break;
    p++;
  }
  if(!strcmp((p+1),"jpg")
  || !strcmp((p+1),"jpeg")
  || !strcmp((p+1),"png")
  || !strcmp((p+1),"JPG")
  || !strcmp((p+1),"JPEG")
  || !strcmp((p+1),"PNG"))
  {
    char*q = fileName;
    while(q != p)//检测文件名是否为全数字
    {
      if((!isdigit(*(q++))))
      {
         return false;
      }
    }
    return true;
  }
  return false;

}
  vector<string> getFiles(string cate_dir)
{
    vector<string> files;//存放文件名
    DIR *dir;
    struct dirent *ptr;
    char base[1000];

    if ((dir=opendir(cate_dir.c_str())) == NULL)
        {
        perror("Open dir error...");
                exit(1);
        }

    while ((ptr=readdir(dir)) != NULL)
    {
        if(strcmp(ptr->d_name,".")==0 || strcmp(ptr->d_name,"..")==0)    ///current dir OR parrent dir
                continue;
        else if(ptr->d_type == 8)    ///file
            //printf("d_name %s\n",ptr->d_name);
            if(isImageFlie(ptr->d_name))
            {
                printf("insert file %s\n",ptr->d_name);
                files.push_back(ptr->d_name);
            }
        else if(ptr->d_type == 10)    ///link file
            //printf("d_name:%s/%s\n",basePath,ptr->d_name);
            continue;
        else if(ptr->d_type == 4)    ///dir
        {
            //files.push_back(ptr->d_name);
            /*
                memset(base,'\0',sizeof(base));
                strcpy(base,basePath);
                strcat(base,"/");
                strcat(base,ptr->d_nSame);
                readFileList(base);
            */
        }
    }
    closedir(dir);
//#endif

    //排序，按从小到大排序
    sort(files.begin(), files.end());
    return files;
}

}
