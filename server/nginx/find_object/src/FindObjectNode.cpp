#include "../include/FindObjectNode.h"
#include <stdio.h>
#include <stdlib.h>
#include "../include/Vocabulary.h"
//#include <sqlite3.h>

extern unsigned int findObject_func_configure;

namespace my_find_object {

FindObjectNode::FindObjectNode(/*MyCameraNode *cameraNode,*/ FeatureExtractor *featureNode)
  :/*m_cameraNode_(cameraNode), */m_featureNode_(featureNode),
    m_vocabulary_(new Vocabulary())
{
#if 0
  //save
  loadObjects(Setting::getValue_string("filePath"));
  //saveToFile();
  //saveToMySQL();
#else
  //load
  if (findObject_func_configure ==1 )
  {
    loadObjects(Setting::getValue_string("filePath"));
    saveToOneFile();
  }
  else
  {
    loadFromOneFile();
  }
  //addObjectAndUpdate("10.jpg","/home/xiaojuan/res/data/book/cover/");
  //saveToOneFile();
  //addObjectAndUpdate();
  //loadFromFile();
#endif
  //m_cameraNode_->setMyFindObjectNode(this);
}

int FindObjectNode::loadObjects(const std::string &dirPath, bool recursive)
{
  UDEBUG("date path:%s \n",dirPath.data());

  string formats = "*.jpeg *.png *.jpg *.bmp *.tiff *.ppm *.pgm";

  vector<string> fileNameList = getFiles(dirPath);
  vector<int> idsLoaded;
  for(int i=0; i<fileNameList.size(); i++)
  {
    UDEBUG("file name = %s \n",fileNameList.at(i).data());
#if 1
#if 1
    const ObjSignature * s = this->addObject(fileNameList.at(i));
    if(s)
    {
      idsLoaded.push_back(s->id());
    }
#else
    const ObjSignature * s = this->addObject_2(fileNameList.at(i));
    if(s)
    {
      idsLoaded.push_back(s->id());
    }
#endif
#else
    vector<const ObjSignature*> ret = this->addObject_1(fileNameList.at(i));
    for(int i=0; i<ret.size(); i++)
    {
      if(NULL != ret[i])
      {
        idsLoaded.push_back(ret[i]->id());
      }
    }
#endif
  }

  if(idsLoaded.size())
  {
    this->updateObjects(idsLoaded);
    this->updateVocabulary(idsLoaded);
  }

  return idsLoaded.size();
}

const ObjSignature *FindObjectNode::addObject(const string &fileName)
{
  string filePath = Setting::getValue_string("filePath");
  if(fileName.size())
  {
    cv::Mat img = cv::imread(filePath+fileName, cv::IMREAD_GRAYSCALE);
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
        //front:前面
        id = atoi(fileInfo[0].data());
        if(id>0)
        {
          //if(m_objects_.contains(id))
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
        UDEBUG("File name doesn't contain \".\" (\"%s\") \n",filePath.data());
      }
      const ObjSignature * s = this->addObject(img, id, filePath);
      if(s)
      {
        //UDEBUG("Added object: %d  %s \n", s->id(), fileName.data());
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

const vector<const ObjSignature *> FindObjectNode::addObject_1(const string &fileName)
{
  vector<const ObjSignature *> v_objRet;

  string filePath = Setting::getValue_string("filePath");
  if(fileName.size())
  {
    cv::Mat img = cv::imread(filePath+fileName, cv::IMREAD_GRAYSCALE);
    if(!img.empty())
    {
      if(img.channels() != 1 || img.depth() != CV_8U)
      {
        cv::cvtColor(img, img, cv::COLOR_BGR2BGRA);
      }

      int row = img.rows;
      int col = img.cols;

      cv::Mat img1 = img.rowRange(0,row/2).colRange(0,col/2);
      const ObjSignature * s1 = this->addObject(img1, Setting::getObjectID(), filePath);
      if(NULL == s1)
      {
        //UDEBUG("Added object: %d  %s \n", s->id(), fileName.data());
        return v_objRet;
      }
      else
      {
        v_objRet.push_back(s1);
      }

      cv::Mat img2 = img.rowRange(row/2,row).colRange(0,col/2);
      const ObjSignature * s2 = this->addObject(img2, Setting::getObjectID(), filePath);
      if(NULL == s2)
      {
        //UDEBUG("Added object: %d  %s \n", s->id(), fileName.data());
        return v_objRet;
      }
      else
      {
        v_objRet.push_back(s2);
      }

      cv::Mat img3 = img.rowRange(0,row/2).colRange(col/2,col);
      const ObjSignature * s3 = this->addObject(img3, Setting::getObjectID(), filePath);
      if(NULL == s3)
      {
        //UDEBUG("Added object: %d  %s \n", s->id(), fileName.data());
        return v_objRet;
      }
      else
      {
        v_objRet.push_back(s3);
      }

      cv::Mat img4 = img.rowRange(row/2,row).colRange(col/2,col);
      const ObjSignature * s4 = this->addObject(img4, Setting::getObjectID(), filePath);
      if(NULL == s4)
      {
        //UDEBUG("Added object: %d  %s \n", s->id(), fileName.data());
        return v_objRet;
      }
      else
      {
        v_objRet.push_back(s4);
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
  return v_objRet;
}

const ObjSignature *FindObjectNode::addObject_2(const string &fileName)
{
  string filePath = Setting::getValue_string("filePath");
  char cbuf[480*480+1];
  char buf[256] = {0};
  if(fileName.size())
  {
    //cv::Mat img = cv::imread(filePath+fileName, cv::IMREAD_GRAYSCALE);
    //Mat imgData(h, w, CV_8UC4, (unsigned char*)cbuf);
    FILE*fp = fopen((filePath+fileName).c_str(), "r");
    if( fp != NULL )
    {
	   fread(cbuf,480*480,1,fp);
	   cbuf[480*480] = 0;
	   printf("cbuf[100]=%d\n",cbuf[100]);
	   
    }
    else
    {
       cout<<"open"<<filePath+fileName<<"error"<<endl;
       return NULL;
    }
    cv::Mat img = cv::Mat(320,480, CV_8UC1,cbuf);
    
    //cv::cvtColor(img, img, cv::COLOR_BGR2BGRA);
    
    if(!img.empty())
    {
      if(img.channels() != 1 || img.depth() != CV_8U)
      {
        printf("cvtColor to COLOR_BGR2BGRA \n");
        cv::cvtColor(img, img, cv::COLOR_BGR2BGRA);
      }
      int id = 0;
      string name = fileName;
      vector<string> fileInfo = str_spalitby(name,".");
      if(fileInfo.size())
      {
        //front:前面
        id = atoi(fileInfo[0].data());
        sprintf(buf,"/home/xiaojuan/res/%d.jpg",id);
        printf("buf = %s \n",buf);
        cv::imwrite(buf,img);
        if(id>0)
        {
          //if(m_objects_.contains(id))
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
        UDEBUG("File name doesn't contain \".\" (\"%s\") \n",filePath.data());
      }
      const ObjSignature * s = this->addObject(img, id, filePath);
      if(s)
      {
        //UDEBUG("Added object: %d  %s \n", s->id(), fileName.data());
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
  printf("addObject id = %d \n",obj->id());

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
        UDEBUG("push object %d! \n" ,ids[i]);
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
  UDEBUG("ids = %d m_objects_ = %d objectsList =%d\n ", ids.size(), m_objects_.size(),objectsList.size());
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
          UDEBUG("object %d descriptor %d \n",id ,threads[j]->descriptors().rows);
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
  UDEBUG("updateVocabulary111...%d \n",ids.size());
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
    //m_dataRange_.clear();
    m_vocabulary_->clear();
  }

  // Get the total size and verify descriptors
  //验证每个的ｄｅｓｃｒｉｐｔｏｒ是否是同一长度的
  //并统计所有的descriptor的个数(每个descriptor占一行)
  UDEBUG("updateVocabulary222.. \n");
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
    if(!Setting::getValue_bool("invertedSearch")) // invertedSearch = true
    {
#if 0
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
        UDEBUG("updateVocabulary333.. \n");
        std::multimap<int, int> words = m_vocabulary_->addWords(objectsList[i]->descriptors(), objectsList.at(i)->id());
        objectsList[i]->setWords(words);
        addedWords += words.size();
        bool updated = false;
        if(incremental && addedWords && addedWords >= updateVocabularyMinWords)
        {
          UDEBUG("updateVocabulary444.. \n");
          m_vocabulary_->update();
          addedWords = 0;
          updated = true;
        }
      }
      if(addedWords)
      {
        UDEBUG("updateVocabular555.. \n");
        m_vocabulary_->update();
      }
    }
  }
}

#if 0
void FindObjectNode::updateVocabularyFromFile(const vector<int> &ids)
{
  int count = 0;
  int dim = -1;
  int type = -1;
  vector<ObjSignature*> objectsList;
  if(ids.size())
  {
    for(int i=0; i<ids.size(); ++i)
    {
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
  m_vocabulary_->resizeIndexDescriptors(count,dim,type);
  // Copy data
  if(count)
  {
    UDEBUG("Updating global descriptors matrix: Objects=%d, total descriptors=%d, dim=%d, type=%d \n",
        (int)m_objects_.size(), count, dim, type);
    int addedWords = 0;
    for(int i=0; i<objectsList.size(); ++i)
    {
      m_vocabulary_->addWords(objectsList.at(i)->descriptors(), objectsList.at(i)->words(), objectsList.at(i)->id());
    }
    m_vocabulary_->load();
  }
}
#endif


void FindObjectNode::addObjectAndUpdate(const string &fileName,const string &filePath)
{
  //const ObjSignature * s = this->addObject(image, id, filePath);
  if(fileName.size())
  {
    cv::Mat img = cv::imread(filePath + fileName, cv::IMREAD_GRAYSCALE);
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
        vector<int> ids;
        printf("add id = %d \n",s->id());
		    ids.push_back(s->id());
		    updateObjects(ids);
		    updateVocabulary(ids);
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
  
}


#if 0
void FindObjectNode::detect(const cv::Mat &image, DetectionInfo &info)
#else
int FindObjectNode::detect(const cv::Mat &image)
#endif
{
  DetectionInfo info;

  bool success = false;
  if(image.empty())
  {
    return -1;
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

//  bool consistentNNData = ( m_vocabulary_->size()!=0 && m_vocabulary_->wordToObjects().begin()->second!=-1 && Setting::getValue_bool("invertedSearch"))
//                        ||((m_vocabulary_->size()==0 || m_vocabulary_->wordToObjects().begin()->second==-1) && !Setting::getValue_bool("invertedSearch"));

//  bool descriptorsValid = !Setting::getValue_bool("invertedSearch") &&
//              !m_objectsDescriptors_.empty() &&
//              m_objectsDescriptors_.begin()->second.cols == info.sceneDescriptors_.cols &&
//              m_objectsDescriptors_.begin()->second.type() == info.sceneDescriptors_.type();

//  bool vocabularyValid = Setting::getValue_bool("invertedSearch") &&
//              m_vocabulary_->size() &&
//              !m_vocabulary_->indexedDescriptors().empty() &&
//              m_vocabulary_->indexedDescriptors().cols == info.sceneDescriptors_.cols &&
//              (m_vocabulary_->indexedDescriptors().type() == info.sceneDescriptors_.type() ||
//              ((Setting::getValue_string("convertType")== "float") && m_vocabulary_->indexedDescriptors().type() == CV_32FC1));

  bool consistentNNData = (m_vocabulary_->wordToObjects().begin()->second!=-1 && Setting::getValue_bool("invertedSearch"));
  if(/*(descriptorsValid || vocabularyValid) && */info.sceneKeypoints_.size() && consistentNNData)
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
#if 0  //多线程搜索
      int threadCounts = Setting.getValue("threadCount").toInt();
      if(threadCounts == 0)
      {
        threadCounts = (int)m_objectsDescriptors_.size();
      }

      QList<int> objectsDescriptorsId = m_objectsDescriptors_.keys();
      QList<cv::Mat> objectsDescriptorsMat = m_objectsDescriptors_.values();
      for(int j=0; j<objectsDescriptorsMat.size(); j+=threadCounts)
      {
        QVector<SearchThread*> threads;

        for(int k=j; k<j+threadCounts && k<objectsDescriptorsMat.size(); ++k)
        {
          threads.push_back(new SearchThread(vocabulary_, objectsDescriptorsId[k], &objectsDescriptorsMat[k], &words));
          threads.back()->start();
        }

        for(int k=0; k<threads.size(); ++k)
        {
          threads[k]->wait();
          info.matches_[threads[k]->getObjectId()] = threads[k]->getMatches();

          if(info.minMatchedDistance_ == -1 || info.minMatchedDistance_ > threads[k]->getMinMatchedDistance())
          {
            info.minMatchedDistance_ = threads[k]->getMinMatchedDistance();
          }
          if(info.maxMatchedDistance_ == -1 || info.maxMatchedDistance_ < threads[k]->getMaxMatchedDistance())
          {
            info.maxMatchedDistance_ = threads[k]->getMaxMatchedDistance();
          }
          delete threads[k];
        }
      }
#endif
    }

#if 1
    int threadCounts = Setting::getValue_int("threadCount");
    if(threadCounts == 0)
    {
      threadCounts = info.matches_.size();
    }
    
    UDEBUG("threadCounts=%d  \n",threadCounts);
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
            &info.sceneKeypoints_,
            obj->image(),
            grayImage));
        threads.back()->start();
      }

      for(int j=0; j<threads.size(); ++j)
      {

        //UDEBUG("getInliers=%d  \n",threads[j]->getInliers().size());
        

        int id = threads[j]->getObjectId();
        
        //UDEBUG("insert id=%d  MatchCounts=%d  getInliers=%d \n", id,threads[j]->getMatchCounts(),threads[j]->getInliers().size());
        DetectionInfo::RejectedCode code = DetectionInfo::kRejectedUndef;
        if(threads[j]->getHomography().empty())
        {
          code = threads[j]->rejectedCode();
        }
        if(code == DetectionInfo::kRejectedUndef &&
           threads[j]->getInliers().size() < 10 )
        {
          code = DetectionInfo::kRejectedLowInliers;
        }
//        if(code == DetectionInfo::kRejectedUndef)
//        {
//          const cv::Mat & H = threads[j]->getHomography();
//          ObjSignature *obj = getMapValue(m_objects_,id);
//          std::vector<cv::Point2d> src = obj->rect();
//          std::vector<cv::Point2d> dest;
//          cv::perspectiveTransform(src,dest, H);
//          //cv::perspectiveTransform();
//          // If a point is outside of 2x times the surface of the scene, homography is invalid.
//          for(int i=0; i<dest.size(); i++)
//          {
//            cv::Point2d point = dest[i];
//            if((point.x < -image.cols && point.x < -obj->width()) ||
//               (point.x > image.cols*2  && point.x > obj->width()*2) ||
//               (point.y < -image.rows  && point.x < -obj->height()) ||
//               (point.y > image.rows*2  && point.x > obj->height()*2))
//            {
//              code= DetectionInfo::kRejectedNotValid;
//              break;
//            }
//          }
//        }

        if(code == DetectionInfo::kRejectedUndef)
        {
          //info.objDetected_.insert(std::make_pair(id, threads[j]->getHomography()));
          UDEBUG("insert id=%d  MatchCounts=%d  getInliers=%d \n", id,threads[j]->getMatchCounts(),threads[j]->getInliers().size());
          info.objDetectedMatchCount_.insert(std::make_pair(id, threads[j]->getMatchCounts()));
          info.objDetectedInliersCount_.insert(std::make_pair(id, threads[j]->getInliers().size()));
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

  std::multimap<int,int>::iterator it_1 = info.objDetectedInliersCount_.begin();
  int max_1 = 0 , max_2 = 0 ,max_id = 0;
	if(it_1 != info.objDetectedInliersCount_.end())
  {
	  for(;it_1 != info.objDetectedInliersCount_.end(); it_1++ )
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

  it_1 = info.objDetectedInliersCount_.begin();

  if(it_1 != info.objDetectedInliersCount_.end())
  {
	  for(;it_1 != info.objDetectedInliersCount_.end(); it_1++ )
	  {
		  if( max_2 < it_1->second && it_1->second < max_1  )
		  {
		    max_2 =  it_1->second;
		  }
	  }
  }
  UDEBUG("max_1=%d  max_2=%d \n", max_1,max_2);

  if( max_1 >= 15 || max_1 -max_2 >=10 )
  {
    std::multimap<int,int>::iterator it = info.objDetectedMatchCount_.begin();
    if(it != info.objDetectedMatchCount_.end())
    {
      int count = 0;
  	  int result = -1;
  	  for(;it != info.objDetectedMatchCount_.end(); it++ )
  	  {
  		  if(count < it->second)
  		  {
  			  count = it->second;
  			  result = it->first;
  		  }
  	  }

  	  UDEBUG("result=%d  max_id=%d  maxMatchCount=%d \n", result,max_id,count);
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

int FindObjectNode::detect(const cv::Mat &image, int retArr[])
{
#if 0
  DetectionInfo info;
  this->detect(image, info);
  m_keyPoints_ = info.sceneKeypoints_;

  cv::Mat destImage;
  cv::drawKeypoints(image,m_keyPoints_, destImage);
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
#endif
}


void FindObjectNode::imageFeatureExtractor(const cv::Mat &image)
{
#if 0
#if SHOW_IMAGE
//  cv::imshow("src", image);
#endif

  DetectionInfo info;
//  QTime time;
//  time.start();

  this->detect(image, info);
  m_keyPoints_ = info.sceneKeypoints_;

#if SHOW_IMAGE
  cv::Mat destImage;
  cv::drawKeypoints(image,m_keyPoints_, destImage);

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
//    cout << "src = " << src << "\ndest = " << dest << endl;
  }
  cv::imshow("dest", destImage);
  cv::waitKey(1);
#endif
#endif
}
#if 0
void FindObjectNode::saveToFile()
{
  m_vocabulary_->save();

  Setting::getTime();
  FILE *filePtr = fopen("/home/xiaojuan/res/data/objsignature.txt", "w");
  if(NULL == filePtr)
  {
    UDEBUG("open file : /home/xiaojuan/res/data/objsignature is error.\n");
    exit(0);
  }
  UDEBUG("write date number is %d \n", m_objects_.size());
  for(std::map<int, ObjSignature*>::const_iterator it = m_objects_.begin(); it != m_objects_.end(); it ++)
  {
    //此处不再保存特征
    it->second->save(filePtr);
    //cout << it->second->descriptors() << endl;
  }
  fclose(filePtr);
  UDEBUG("save object file using time %f \n", Setting::getTime());

//  exit(1);
}
#endif
#if 1
void FindObjectNode::saveToOneFile()
{
  Setting::getTime();
  int objDataSzie = 0;
  FILE *filePtr = fopen("/usr/local/nginx/conf/cover.bin", "wb");
  if(NULL == filePtr)
  {
    UDEBUG("/usr/local/nginx/conf/cover.bin");
    exit(0);
  }
  UDEBUG("write date number is %d \n", m_objects_.size());
  for(std::map<int, ObjSignature*>::const_iterator it = m_objects_.begin(); it != m_objects_.end(); it ++)
  {
    //此处不再保存特征
    
    objDataSzie += it->second->getDataSize();
    //cout << it->second->descriptors() << endl;
  }
  UDEBUG("objDataSzie = %d \n", objDataSzie);
  fwrite(&objDataSzie, sizeof(objDataSzie), 1, filePtr);
  for(std::map<int, ObjSignature*>::const_iterator it = m_objects_.begin(); it != m_objects_.end(); it ++)
  {
    //此处不再保存特征
    it->second->save(filePtr);
    //cout << it->second->descriptors() << endl;
  }
  UDEBUG("after obj save file ptr=%d \n",ftell(filePtr));
  m_vocabulary_->save(filePtr);
  
  fclose(filePtr);
  UDEBUG("save object file using time %f \n", Setting::getTime());

//  exit(1);
}
#endif
void FindObjectNode::loadFromOneFile()
{
    UDEBUG("load object begin \n");
    int objDataSize = 0 ;
    char* begin_ptr = NULL;
    char* cur_ptr=NULL;
	Setting::getTime();

	std::map<int, ObjSignature*>::iterator it;
  /*if(NULL != m_vocabulary_)
  {
    delete m_vocabulary_;
    m_vocabulary_ = NULL;
  }*/
  if(NULL != m_vocabulary_){
  //m_vocabulary_->clear();
  }
	UDEBUG("load object begin111 \n");
if(m_objects_.size() > 0 ){
  it = m_objects_.begin();
  while(it != m_objects_.end())
  {
    if( it->second != NULL )   
    delete it->second;
    it++;
  }
  m_objects_.clear();
  }
  
	FILE *filePtr = fopen("/usr/local/nginx/conf/cover.bin", "rb");
	//FILE *filePtr = fopen("/home/xiaojuan/res/data/cover.bin", "rb");
	UDEBUG("open file using time %f \n", Setting::getTime());
	if(NULL == filePtr)
	{
	  UDEBUG("open file :/usr/local/nginx/conf/cover.bin is error.\n");
	  exit(0);
	}
  //  FILE *fileDescriptor2 = fopen("/data/Descriptor.bin", "r");
  //  if(NULL == fileDescriptor2)
  //  {
  //	UDEBUG("open file : /data/Descriptor is error.\n");
  //	exit(0);
  //  }
  
    fread(&objDataSize,sizeof(objDataSize),1,filePtr);
    begin_ptr = (char*)filePtr;
    UDEBUG("load objDataSize =%d \n",objDataSize);
	vector<int> idsLoaded;
	while(1)
	{
	  UDEBUG("00filePtr-ptr=%d \n",ftell(filePtr));
	  ObjSignature *obj = new ObjSignature();
	  UDEBUG("22filePtr-ptr=%d \n",ftell(filePtr));
	  obj->load(filePtr);
	  UDEBUG("33filePtr-ptr=%d \n",ftell(filePtr));
	  idsLoaded.push_back(obj->id());
	  m_objects_.insert(std::make_pair(obj->id(),obj));
	  cur_ptr = (char*)filePtr;

	  UDEBUG("111filePtr-ptr=%d \n",ftell(filePtr));

	  if(ftell(filePtr) >= objDataSize)
	  {
	     break;
	  }
	}
	UDEBUG("read date number is %d \n", idsLoaded.size());
	UDEBUG("load object file using time %f \n", Setting::getTime());
	//updateVocabularyFromFile(idsLoaded);
	m_vocabulary_->load(filePtr);
	fclose(filePtr);
	UDEBUG("make Vocabulary using time %f \n", Setting::getTime());

//  exit(1);
}


#if 0
void FindObjectNode::loadFromFile()
{
#if 0
  //测试存储到文件中的vocabulary 是否一致
  enum flann_datatype_t
  {
    FLANN_INT8 = 0,
    FLANN_INT16 = 1,
    FLANN_INT32 = 2,
    FLANN_INT64 = 3,
    FLANN_UINT8 = 4,
    FLANN_UINT16 = 5,
    FLANN_UINT32 = 6,
    FLANN_UINT64 = 7,
    FLANN_FLOAT32 = 8,
    FLANN_FLOAT64 = 9
  };
  enum flann_algorithm_t
  {
    FLANN_INDEX_LINEAR = 0,
    FLANN_INDEX_KDTREE = 1,
    FLANN_INDEX_KMEANS = 2,
    FLANN_INDEX_COMPOSITE = 3,
    FLANN_INDEX_KDTREE_SINGLE = 4,
    FLANN_INDEX_HIERARCHICAL = 5,
    FLANN_INDEX_LSH = 6,
    FLANN_INDEX_SAVED = 254,
    FLANN_INDEX_AUTOTUNED = 255,

    // deprecated constants, should use the FLANN_INDEX_* ones instead
        LINEAR = 0,
    KDTREE = 1,
    KMEANS = 2,
    COMPOSITE = 3,
    KDTREE_SINGLE = 4,
    SAVED = 254,
    AUTOTUNED = 255
  };
  struct IndexHeader
  {
    char signature[16];
    char version[16];
    flann_datatype_t data_type;
    flann_algorithm_t index_type;
    size_t rows;
    size_t cols;
  };
  FILE *fin = fopen("/data/vocabulary.bin", "rb");
  IndexHeader header;
  fread(&header,sizeof(header),1,fin);
  UDEBUG("head.size = %d signature = %s version = %s cols = %d rows = %d\n", sizeof(header), header.signature, header.version,header.cols, header.rows);
  fclose(fin);
  exit(1);
#endif
  UDEBUG("load object begin \n");
  Setting::getTime();
  FILE *filePtr2 = fopen("/home/xiaojuan/res/data/objsignature.txt", "r");
  UDEBUG("open file using time %f \n", Setting::getTime());
  if(NULL == filePtr2)
  {
    UDEBUG("open file : /home/xiaojuan/res/data/objsignature.txt is error.\n");
    exit(0);
  }
//  FILE *fileDescriptor2 = fopen("/data/Descriptor.bin", "r");
//  if(NULL == fileDescriptor2)
//  {
//    UDEBUG("open file : /data/Descriptor is error.\n");
//    exit(0);
//  }
  vector<int> idsLoaded;
  while(1)
  {
    ObjSignature *obj = new ObjSignature();
    if(0 == obj->load(filePtr2))
    {
      idsLoaded.push_back(obj->id());
      m_objects_.insert(std::make_pair(obj->id(),obj));
    }
    else
    {
      delete obj;
      break;
    }
  }
  fclose(filePtr2);
//  fclose(fileDescriptor2);
  UDEBUG("read date number is %d \n", idsLoaded.size());
  UDEBUG("load object file using time %f \n", Setting::getTime());
  //updateVocabularyFromFile(idsLoaded);
  m_vocabulary_->load();
  UDEBUG("make Vocabulary using time %f \n", Setting::getTime());
}
#endif
#if 0
void FindObjectNode::saveToDB()
{
  Setting::getTime();
  Setting::setDB("/data/find_object.db");
  std::string sql = "DELETE FROM descriptor_table;DELETE FROM object_table;DELETE FROM word_2_object_table;";
  sqlite3_exec(Setting::getDB(),sql.c_str(),NULL,NULL,NULL);
  std::map<int, ObjSignature*>::iterator it = m_objects_.begin();
  for(; it != m_objects_.end(); it ++)
  {
    it->second->save(Setting::getDB());
  }

  m_vocabulary_->save(Setting::getDB());
  UDEBUG("save object file using time %f \n", Setting::getTime());
  exit(1);
}

void FindObjectNode::saveToMySQL()
{
  Setting::getTime();
  std::string host = "localhost";
  std::string user = "root";
  std::string pwd = "123";
  std::string db_name = "picture_book";
  Setting::initDB(host,user,pwd,db_name);
  std::string sql = "DELETE FROM descriptor_table;DELETE FROM object_table;DELETE FROM word_2_object_table;";

  printf("m_mySQLConn=0x%x\n",Setting::getMySQLDB());

  if(mysql_query(Setting::getMySQLDB(),"DELETE FROM descriptor_table;"))  
  {  
    UDEBUG("mysql Query Error111=%s:\n",mysql_error(Setting::getMySQLDB()));
    return;
  }  
  if(mysql_query(Setting::getMySQLDB(),"DELETE FROM object_table;"))  
  {  
    UDEBUG("mysql Query Error object_table =%s:\n",mysql_error(Setting::getMySQLDB()));
    return;
  } 

  if(mysql_query(Setting::getMySQLDB(),"DELETE FROM word_2_object_table;"))  
  {  
    UDEBUG("mysql Query Error word_2_object_table =%s:\n",mysql_error(Setting::getMySQLDB()));
    return;
  } 
  std::map<int, ObjSignature*>::iterator it = m_objects_.begin();
  for(; it != m_objects_.end(); it ++)
  {
    it->second->save(Setting::getMySQLDB());
  }

  m_vocabulary_->save(Setting::getMySQLDB());
  Setting::closeDB();
  UDEBUG("save object file using time %f \n", Setting::getTime());
  //exit(1);
}


void FindObjectNode::loadFromDB()
{
  Setting::getTime();
  Setting::setDB("/data/find_object.db");
  sqlite3_stmt * stat = NULL;

  std::string sql = "SELECT objectID FROM object_table;";
  int result = sqlite3_prepare(Setting::getDB(),sql.c_str(),-1, &stat, 0);
  vector<int> rowCount;
  if(result != SQLITE_OK)
  {
    return ;
  }
  while(1)
  {
    result = sqlite3_step(stat);
    if(SQLITE_ROW == result)
    {
      int objectID =  sqlite3_column_int(stat,0);
      rowCount.push_back(objectID);
    }
    else if(SQLITE_DONE == result)
    {
      break;
    }
  }
  sqlite3_finalize(stat);

  for(int i=0; i<rowCount.size(); i++)
  {
    ObjSignature* obj = new ObjSignature();
    if(0 == obj->load(Setting::getDB(),rowCount[i]))
    {
      UDEBUG("objectID = %d \n",obj->id());
      m_objects_.insert(std::make_pair(obj->id(),obj));
    }
    else
    {
      delete obj;
      break;
    }
  }

  m_vocabulary_->load(Setting::getDB());
  UDEBUG("load object file using time %f \n", Setting::getTime());
}


void FindObjectNode::loadFromMySOLDB()
{
  Setting::getTime();
  std::string host = "127.0.0.1";
  std::string user = "root";
  std::string pwd = "123";
  std::string db_name = "picture_book";
  MYSQL_RES *result;
  MYSQL_ROW row;
  int objectID = 0;
  Setting::initDB(host,user,pwd,db_name);

  std::string sql = "SELECT id FROM object_table;";
  if(mysql_query(Setting::getMySQLDB(), sql.c_str()))  
  {  
    UDEBUG("mysql Query Error22:%d \n",Setting::getMySQLDB());
    return;
  }  
  vector<int> rowCount;

	result = mysql_use_result(Setting::getMySQLDB()); // 获取结果集  
	// mysql_field_count()返回connection查询的列数  
	for(int i=0; i < mysql_field_count(Setting::getMySQLDB()); ++i)  
	{  
	    // 获取下一行  
	    row = mysql_fetch_row(result);  
	    if(row <= 0)  
	    {  
	        break;  
	    }  
	    objectID = atoi(row[0]);
        rowCount.push_back(objectID);
	}  
	// 释放结果集的内存  
	mysql_free_result(result);  

  for(int i=0; i<rowCount.size(); i++)
  {
    ObjSignature* obj = new ObjSignature();
    if(0 == obj->load(Setting::getMySQLDB(),rowCount[i]))
    {
      UDEBUG("objectID = %d \n",obj->id());
      m_objects_.insert(std::make_pair(obj->id(),obj));
    }
    else
    {
      delete obj;
      break;
    }
  }

  m_vocabulary_->load(Setting::getMySQLDB());
  UDEBUG("load object file using time %f \n", Setting::getTime());
}

#endif
#if 0
QImage CvMat2QImage(const cv::Mat &image, bool isBgr)
{
  QImage qtemp;
  if(!image.empty() && image.depth() == CV_8U)
  {
    if(image.channels()==3)
    {
      const unsigned char * data = image.data;
      if(image.channels() == 3)
      {
        qtemp = QImage(image.cols, image.rows, QImage::Format_RGB32);
        for(int y = 0; y < image.rows; ++y, data += image.cols*image.elemSize())
        {
          for(int x = 0; x < image.cols; ++x)
          {
            QRgb * p = ((QRgb*)qtemp.scanLine (y)) + x;
            if(isBgr)
            {
              *p = qRgb(data[x * image.channels()+2], data[x * image.channels()+1], data[x * image.channels()]);
            }
            else
            {
              *p = qRgb(data[x * image.channels()], data[x * image.channels()+1], data[x * image.channels()+2]);
            }
          }
        }
      }
    }
    else if(image.channels() == 1)
    {
      // mono grayscale
      qtemp = QImage(image.data, image.cols, image.rows, image.cols, QImage::Format_Indexed8).copy();
      QVector<QRgb> my_table;
      for(int i = 0; i < 256; i++) my_table.push_back(qRgb(i,i,i));
      qtemp.setColorTable(my_table);
    }
    else
    {
      printf("Wrong image format, must have 1 or 3 channels\n");
    }
  }
  return qtemp;
}
#endif

vector<string> getFiles(string cate_dir)
{
    vector<string> files;//存放文件名

//#ifdef WIN32
//    _finddata_t file;
//    long lf;
//    //输入文件夹路径
//    if ((lf=_findfirst(cate_dir.c_str(), &file)) == -1) {
//        cout<<cate_dir<<" not found!!!"<<endl;
//    } else {
//        while(_findnext(lf, &file) == 0) {
//            //输出文件名
//            //cout<<file.name<<endl;
//            if (strcmp(file.name, ".") == 0 || strcmp(file.name, "..") == 0)
//                continue;
//            files.push_back(file.name);
//        }
//    }
//    _findclose(lf);
//#endif

//#ifdef linux
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
            //printf("d_name:%s/%s\n",basePath,ptr->d_name);
            files.push_back(ptr->d_name);
        else if(ptr->d_type == 10)    ///link file
            //printf("d_name:%s/%s\n",basePath,ptr->d_name);
            continue;
        else if(ptr->d_type == 4)    ///dir
        {
            files.push_back(ptr->d_name);
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
