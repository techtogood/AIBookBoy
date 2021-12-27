#include "../include/CoverDetection.h"
#include "../include/FindObjectNode.h"
extern my_find_object::Crc32 *crc32object;

namespace my_find_object {


CoverDetection::CoverDetection():m_vocabulary_(new Vocabulary()),m_feature_type_(FEATURE_SURF)
{
    //UDEBUG("CoverDatection init...");
  int feature_type_bak = my_feature_type;
  
	my_feature_type = FEATURE_SIFT;
	m_featureExtractor_sift = new FeatureExtractor();

	my_feature_type = FEATURE_ORB;
	m_featureExtractor_orb = new FeatureExtractor();

	my_feature_type = FEATURE_SURF;
	m_featureExtractor_surf = new FeatureExtractor();

	my_feature_type = feature_type_bak;
	UDEBUG("CoverDatection init... exit");
}

void CoverDetection::clear()
{
	std::map<int, ObjSignature*>::iterator it;
  /*if(NULL != m_vocabulary_)
  {
    delete m_vocabulary_;
    m_vocabulary_ = NULL;
  }*/
  if(NULL != m_vocabulary_){
    m_vocabulary_->clear();
  }

  it = m_objects_.begin();
  while(it != m_objects_.end())
  {
    if( it->second != NULL )   
    delete it->second;
    it++;
  }
  m_objects_.clear();
}

void CoverDetection::setFeatureType( int type)
{
  m_feature_type_ = type;
} 

int CoverDetection::getFeatureType()
{
  return m_feature_type_;
} 


int CoverDetection::loadFromFile(const string &FileName)
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
    return 1;
  }

  /*if(!crc32object->isVaildDataFileByCrc(filePtr)){
    return 1;
  }*/
  fseek(filePtr,0L,SEEK_SET);

  fread(&featureType,sizeof(featureType),1,filePtr); //no use now

  fread(&objDataSize,sizeof(objDataSize),1,filePtr);

  UDEBUG("loadFromFile:%d.\n",objDataSize);
  begin_ptr = (char*)filePtr;
//  vector<int> idsLoaded;
  while(1)
  {
    ObjSignature *obj = new ObjSignature();
    obj->load(filePtr);
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
  m_vocabulary_->load(filePtr);
  fclose(filePtr);
  return 0;
}

void CoverDetection::saveToFile(const string &FileName)
{
  Setting::getTime();
  int objDataSzie = 0;
  FILE *filePtr = fopen(FileName.data(), "w+b");
  if(NULL == filePtr)
  {
    UDEBUG("open file : %s.\n",FileName.data());
    exit(0);
  }
  UDEBUG("saveToFile m_feature_type_ : %d.\n",m_feature_type_);
  fwrite(&m_feature_type_, sizeof(m_feature_type_), 1, filePtr);
  for(std::map<int, ObjSignature*>::const_iterator it = m_objects_.begin(); it != m_objects_.end(); it ++)
  {
    //此处不再保存特征
		UDEBUG("obj size : %d.\n",it->second->getDataSize());
    objDataSzie += it->second->getDataSize();
  }
  fwrite(&objDataSzie, sizeof(objDataSzie), 1, filePtr);
  for(std::map<int, ObjSignature*>::const_iterator it = m_objects_.begin(); it != m_objects_.end(); it ++)
  {
    //此处不再保存特征
    it->second->save(filePtr);
  }
  m_vocabulary_->save(filePtr);

  //crc32object->addCrcToDataFile(filePtr);

  fclose(filePtr);
}


void CoverDetection::updateObjects(const vector<int> &ids)
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
  UDEBUG("ids = %d m_objects_ = %d \n ", ids.size(), m_objects_.size());
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
						if( m_feature_type_ == FEATURE_ORB )
						{
	            threads.push_back(new ExtractorThread(m_featureExtractor_orb, objectsList.at(k)->id(), objectsList.at(k)->image()));
            }
            else if( m_feature_type_ == FEATURE_SIFT )
						{
	            threads.push_back(new ExtractorThread(m_featureExtractor_sift, objectsList.at(k)->id(), objectsList.at(k)->image()));
            }
            else
						{
	            threads.push_back(new ExtractorThread(m_featureExtractor_surf, objectsList.at(k)->id(), objectsList.at(k)->image()));
            }
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

void CoverDetection::updateVocabulary(const vector<int> &ids)
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

int CoverDetection::loadObjects(const std::string &dirPath)
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
      cout << "ID :" << s->id() << endl;
    }
  }

  if(idsLoaded.size())
  {
    this->updateObjects(idsLoaded);
    this->updateVocabulary(idsLoaded);
  }

  return idsLoaded.size();
}


bool CoverDetection::addObject(ObjSignature *obj)
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


const ObjSignature *CoverDetection::addObject(const cv::Mat &image, int id, const string &filePath)
{
  ObjSignature * s = new ObjSignature(id, image, filePath);
  if(!this->addObject(s))
  {
    delete s;
    return 0;
  }
  return s;
}

const ObjSignature *CoverDetection::addObject(const string &fileName,const string &filePath)
{
  cout << filePath + fileName << endl;
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


void CoverDetection::addObjectAndUpdate(const string &fileName,const string &filePath)
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


int CoverDetection::detect(const cv::Mat &descriptors, const std::vector<cv::KeyPoint> keypoints)
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
      m_vocabulary_->search(info.sceneDescriptors_, results, dists, k);


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

      for(int j=0; j<threads.size(); ++j)
      {
	      threads[j]->wait();
        int id = threads[j]->getObjectId();
        DetectionInfo::RejectedCode code = DetectionInfo::kRejectedUndef;
        if(threads[j]->getHomography().empty())
        {
          code = threads[j]->rejectedCode();
        }
        if(code == DetectionInfo::kRejectedUndef &&
           threads[j]->getInliers().size() < 20	)
        {
          code = DetectionInfo::kRejectedLowInliers;
        }

        if(code == DetectionInfo::kRejectedUndef)
        {
          info.objDetected2_.insert(std::make_pair(id, threads[j]->getMatchCounts()));
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
	//UDEBUG("info.objDetected2_ size: %d \n", info.objDetected2_.size());
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
	  return result;
  }
  else
  {
    return -1;
  }
}
	


}
