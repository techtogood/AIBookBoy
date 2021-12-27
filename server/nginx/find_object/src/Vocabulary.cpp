/*
Copyright (c) 2011-2014, Mathieu Labbe - IntRoLab - Universite de Sherbrooke
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Universite de Sherbrooke nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

#include "../include/Setting.h"
#include "../include/Vocabulary.h"


#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/mman.h>
#if CV_MAJOR_VERSION < 3
#include <opencv2/gpu/gpu.hpp>
#define CVCUDA cv::gpu
#else
#include <opencv2/core/cuda.hpp>
#define CVCUDA cv::cuda
#ifdef HAVE_OPENCV_CUDAFEATURES2D
#include <opencv2/cudafeatures2d.hpp>
#endif
#endif

namespace my_find_object {

Vocabulary::Vocabulary()
{
}

Vocabulary::~Vocabulary()
{
}

void Vocabulary::clear()
{
	wordToObjects_.clear();
	notIndexedDescriptors_ = cv::Mat();
	notIndexedWordIds_.clear();

	if( mp_descriptorData_ != NULL )
	{
		free(mp_descriptorData_ );
		mp_descriptorData_ = NULL;
	}

  //getGeneral_invertedSearch
  //我们从对象的描述符中创建了一个词汇表，并将场景的描述符匹配到这个词汇表中，而不是将描述符与从场景中提取的描述符创建的词汇表匹配。它是反向搜索模式。
  //vocabularyFixed:词汇固定
  if(Setting::getValue_bool("vocabularyFixed") && Setting::getValue_bool("invertedSearch"))
	{
		this->update(); // if vocabulary structure has changed

		// If the dictionary is fixed, don't clear indexed descriptors
    //如果字典是固定的，不要清除索引描述符。
    return;
	}

	indexedDescriptors_ = cv::Mat();
}
#if 0
bool Vocabulary::save(const std::string &filename) const
{
    FILE *fileDescriptor = fopen("/home/xiaojuan/res/data/Descriptor.bin", "w");
    if(NULL == fileDescriptor)
    {
      UDEBUG("open file : /data/Descriptor is error.\n");
      exit(0);
    }
    int dataSize = indexedDescriptors_.elemSize()*indexedDescriptors_.cols*indexedDescriptors_.rows;
    printf("dataSize=%d \n",dataSize);
    fwrite(indexedDescriptors_.data, dataSize, 1, fileDescriptor);
    fclose(fileDescriptor);

    FILE *word2objectFile = fopen("/home/xiaojuan/res/data/word2object.bin", "w");
    if(NULL == word2objectFile)
    {
      UDEBUG("open file : /home/xiaojuan/res/data/word2object is error.\n");
      exit(0);
    }
    int wordsize = wordToObjects_.size();
    fwrite(&wordsize,sizeof(wordsize),1,word2objectFile);
	int word[2];
    for(std::multimap<int, int>::const_iterator it = wordToObjects_.begin();
        it != wordToObjects_.end(); it++)
    {
      word[0] = it->first;
      word[1] = it->second;
      fwrite(word,sizeof(wordsize),2,word2objectFile);
    }
    fclose(word2objectFile);

#ifndef HANMING
  UDEBUG("open file:%s .\n",filename.c_str());

  FILE *fp = fopen(filename.c_str(), "wb");
  if(NULL == fp)
  {
    UDEBUG("open file:%s error.\n",filename.c_str());
    exit(1);
  }
  flannIndex_->saveIndex(fp);
  fclose(fp);
  return true;
#endif
}
#endif
bool Vocabulary::save(FILE *filePtr ) 
{
	  int dataSize = indexedDescriptors_.elemSize()*indexedDescriptors_.cols*indexedDescriptors_.rows;
		printf("dataSize=%d \n",dataSize);
		int type = indexedDescriptors_.type();
		fwrite(&(indexedDescriptors_.rows), sizeof(indexedDescriptors_.rows), 1, filePtr);
    fwrite(&(indexedDescriptors_.cols), sizeof(indexedDescriptors_.cols), 1, filePtr);
    fwrite(&type, sizeof(type), 1, filePtr);
		fwrite(&dataSize, sizeof(int), 1, filePtr);
		printf("rows=%d cols=%d type=%d dataSize=%d \n",indexedDescriptors_.rows,indexedDescriptors_.cols,type,dataSize);
		
		fwrite(indexedDescriptors_.data, dataSize, 1, filePtr);

    int wordsize = wordToObjects_.size();
    printf("wordsize=%d \n",wordsize);
    fwrite(&wordsize,sizeof(wordsize),1,filePtr);
	  int word[2];
    for(std::multimap<int, int>::const_iterator it = wordToObjects_.begin();
        it != wordToObjects_.end(); it++)
    {
      word[0] = it->first;
      word[1] = it->second;
      fwrite(word,sizeof(wordsize),2,filePtr);
    }

    

#ifndef HANMING
  //flannIndex_->saveIndex(filePtr);
#endif
	return true;

}

#if 0
bool Vocabulary::load(const std::string & filename)
{
  int m_fileId_ = open("/home/xiaojuan/res/data/Descriptor.bin", O_RDONLY);
  if(m_fileId_ < 0)
  {
    UDEBUG("can not open file /home/xiaojuan/res/data/Descriptor.bin .\n");
    return false;
  }
  struct stat status;
  fstat(m_fileId_, &status);
  m_fileSize_ = status.st_size;

  mp_descriptorData_ = mmap(0,m_fileSize_, PROT_READ, MAP_SHARED, m_fileId_,0);
  if(NULL == mp_descriptorData_)
  {
    UDEBUG("mmap failed ... \n");
    return false;
  }

  FILE *word2objectFile = fopen("/home/xiaojuan/res/data/word2object.bin", "r");
  if(NULL == word2objectFile)
  {
    UDEBUG("open file : /home/xiaojuan/res/data/word2object is error.\n");
    exit(0);
  }
  int wordsize;
  fread(&wordsize,sizeof(wordsize),1,word2objectFile);
  for(int i=0; i<wordsize; i++)
  {
    int word[2];
    fread(word,sizeof(wordsize),2,word2objectFile);
    wordToObjects_.insert(std::make_pair(word[0],word[1]));
  }
  fclose(word2objectFile);

#ifndef HANMING
  if(!notIndexedDescriptors_.empty())
  {
    if(!indexedDescriptors_.empty())
    {
      return false;
    }

    //concatenate descriptors
    //连接描述符
    indexedDescriptors_.push_back(notIndexedDescriptors_);

    notIndexedDescriptors_ = cv::Mat();
    notIndexedWordIds_.clear();
  }
  UDEBUG("m_fileSize_ = %d \n", m_fileSize_);
  UDEBUG("build flannIndex_  indexedDescriptors_ size = %d \n", m_fileSize_/512);

  flannIndex_ = new my_cvflann::my_KMeansIndex<cvflann::L2<float>>(mp_descriptorData_,m_fileSize_/512);
  FILE *fp = fopen(filename.c_str(), "rb");
  if(NULL == fp)
  {
    UDEBUG("open file:%s error.\n",filename.c_str());
    exit(1);
  }
  flannIndex_->loadIndex(fp);
#endif
  return true;
}
#endif
bool Vocabulary::load(FILE* filePtr)
{
	int rows,cols,type;
			int dataSize;
			fread(&rows,sizeof(rows),1,filePtr);
			fread(&cols,sizeof(cols),1,filePtr);
			fread(&type,sizeof(type),1,filePtr);
			//fread(&dataSize,sizeof(dataSize),1,filePtr);
			//unsigned char data[dataSize];
			//fread(data, dataSize, 1, filePtr);
			//UDEBUG("rows = %d  cols = %d datasize = %d	\n", rows, cols, dataSize);
			//descriptors_ = cv::Mat(rows, cols, type, data).clone();

		

  fread(&m_fileSize_,sizeof(m_fileSize_),1,filePtr);
  printf("rows=%d cols=%d type=%d m_fileSize_=%d \n",rows,cols,type,m_fileSize_);
  mp_descriptorData_ = (void*)malloc(m_fileSize_);
  fread(mp_descriptorData_,m_fileSize_,1,filePtr);

  indexedDescriptors_ = cv::Mat(rows, cols, type, mp_descriptorData_).clone();

 /* mp_descriptorData_ = mmap(0,m_fileSize_, PROT_READ, MAP_SHARED, m_fileId_,0);
  if(NULL == mp_descriptorData_)
  {
    UDEBUG("mmap failed ... \n");
    return false;
  }*/

  /*FILE *word2objectFile = fopen("/home/xiaojuan/res/data/word2object.bin", "r");
  if(NULL == word2objectFile)
  {
    UDEBUG("open file : /home/xiaojuan/res/data/word2object is error.\n");
    exit(0);
  }*/
  int wordsize;
  fread(&wordsize,sizeof(wordsize),1,filePtr);
  for(int i=0; i<wordsize; i++)
  {
    int word[2];
    fread(word,sizeof(wordsize),2,filePtr);
    wordToObjects_.insert(std::make_pair(word[0],word[1]));
  }

#ifndef HANMING
  if(!notIndexedDescriptors_.empty())
  {
    if(!indexedDescriptors_.empty())
    {
      return false;
    }

    //concatenate descriptors
    //连接描述符
    indexedDescriptors_.push_back(notIndexedDescriptors_);

    notIndexedDescriptors_ = cv::Mat();
    notIndexedWordIds_.clear();
  }
  #if 0
  UDEBUG("m_fileSize_ = %d \n", m_fileSize_);
  UDEBUG("build flannIndex_  indexedDescriptors_ size = %d \n", m_fileSize_/512);

  flannIndex_ = new my_cvflann::my_KMeansIndex<cvflann::L2<float>>(mp_descriptorData_,m_fileSize_/512);

  flannIndex_->loadIndex(filePtr);
  #else
    cv::flann::IndexParams * params = new cv::flann::KDTreeIndexParams(4);
#if CV_MAJOR_VERSION == 2 and CV_MINOR_VERSION == 4 and CV_SUBMINOR_VERSION >= 12
  flannIndex_.build(indexedDescriptors_, cv::Mat(), *params, /*Settings::getFlannDistanceType()*/ (cvflann::flann_distance_t)(0+1));
#else
  flannIndex_.build(indexedDescriptors_, *params, /*Settings::getFlannDistanceType()*/(cvflann::flann_distance_t)(0+1));
#endif
   delete params;
  #endif
  
#endif
  return true;
}


/*
 * descriptor_table
 * [ID,descriptor,objectID]
 * CREATE TABLE descriptor_table(ID integer PRIMARY KEY not null,descriptor blob ,objectID integer not null);
*/
#if 0
bool Vocabulary::save(sqlite3 *db)
{
  UDEBUG("save vocabulary begin .\n");
#ifndef HANMING
  int result = 0;
  sqlite3_stmt * stat = NULL;
  std::string sql = "INSERT INTO descriptor_table VALUES(?,?,?)";
  result = sqlite3_prepare(db,sql.c_str(),-1, &stat, 0);
  if(result != SQLITE_OK)
  {
    return false;
  }
  int dataSize = indexedDescriptors_.elemSize()*indexedDescriptors_.cols;
  for(int i=0; i<indexedDescriptors_.rows; i++)
  {
    result = sqlite3_bind_int(stat, 1, i);
    if(result != SQLITE_OK)
    {
      return false;
    }

    result = sqlite3_bind_blob(stat,2,indexedDescriptors_.rowRange(i,i+1).data,dataSize,NULL);
    if(result != SQLITE_OK)
    {
      return false;
    }

    std::multimap<int,int>::iterator it = wordToObjects_.find(i);
    result = sqlite3_bind_int(stat, 3, it->second);
    if(result != SQLITE_OK)
    {
      return false;
    }
    result = sqlite3_step(stat);
    if(result != SQLITE_DONE)
    {
      UDEBUG("insert indexedDescriptors_ %d row into descriptor_table error \n", i);
      return false;
    }
    result = sqlite3_reset(stat);
    if(result != SQLITE_OK)
    {
      return false;
    }
  }
  sqlite3_finalize(stat);
  UDEBUG("save vocabulary end .\n");

  save();//save index to file
#endif
  return true;
}

bool Vocabulary::load(sqlite3 *db)
{
#ifndef HANMING
  //load wordToObject
  int result = 0;
  sqlite3_stmt * stat = NULL;

  //indexedDescriptors_ = cv::Mat(getDescriptorRows(db),64, CV_32F);
  std::string select = "SELECT ID,objectID,descriptor FROM descriptor_table; ";
  result = sqlite3_prepare(db,select.c_str(),-1, &stat, 0);
  if(result != SQLITE_OK)
  {
    return false;
  }
  while(1)
  {
    result = sqlite3_step(stat);
    if(SQLITE_ROW == result)
    {
      int wordID,objectID;
      wordID = sqlite3_column_int(stat,0);
      objectID = sqlite3_column_int(stat,1);
      //UDEBUG("wordID = %d   objectID = %d \n",wordID, objectID);
      wordToObjects_.insert(std::make_pair(wordID,objectID));

//      float *descriptor = (float*)sqlite3_column_blob(stat,2);
//      cv::Mat des(1,64,CV_32F,descriptor);
//      des.row(0).copyTo(indexedDescriptors_.row(wordID));
    }
    else if(SQLITE_DONE == result)
    {
      break;
    }
  }
  sqlite3_finalize(stat);

  //load vocabulary index
  flannIndex_ = new my_cvflann::my_KMeansIndex<cvflann::L2<float>>(db,getDescriptorRows(db));
  FILE *fp = fopen(vocabularyFileName.c_str(), "rb");
  if(NULL == fp)
  {
    UDEBUG("open file:%s error.\n",vocabularyFileName.c_str());
    exit(1);
  }
  flannIndex_->loadIndex(fp);
#endif
  return true;
}


bool Vocabulary::save( MYSQL *connection )
{
  UDEBUG("save vocabulary begin .\n");
#ifndef HANMING

	int dataSize = indexedDescriptors_.elemSize()*indexedDescriptors_.cols;
	char sql[dataSize*2], *end;

	for(int i=0; i<indexedDescriptors_.rows; i++)
	{

	std::multimap<int,int>::iterator it = wordToObjects_.find(i);

	sprintf(sql, "INSERT INTO descriptor_table(id,ObjectId,descriptors) VALUE (%d, %d, ",i,it->second );
	end = sql + strlen(sql);
	*end++ = '\'';
	end += mysql_real_escape_string(connection, end,(char *)(indexedDescriptors_.rowRange(i,i+1).data),dataSize);
	*end++ = '\'';
	*end++ = ')';

	//UDEBUG("end - sql: %d\n", (unsigned int)(end - sql));

	if(mysql_real_query(connection, sql, (unsigned int)(end - sql)))
	{
	  UDEBUG("Query failed (%s)\n", mysql_error(connection));
	}
	/*std::multimap<int,int>::iterator it = wordToObjects_.find(i);
	sprintf(sql, "INSERT INTO descriptor_table(ObjectId) VALUE (%d)",it->second );
	if(mysql_query(connection, sql))  
	{  
	  UDEBUG("Query Error:\n",mysql_error(connection));
	}  */
	}

  
  UDEBUG("save vocabulary end .\n");

  save();//save index to file
#endif
  return true;
}

bool Vocabulary::load(MYSQL *connection)
{
#ifndef HANMING
  //load wordToObject
   MYSQL_RES *result;
   MYSQL_ROW row;

  //indexedDescriptors_ = cv::Mat(getDescriptorRows(db),64, CV_32F);
  std::string select = "SELECT id,objectId,descriptor FROM descriptor_table; ";

  if(mysql_query(connection, select.c_str()))  
    {  
      UDEBUG("mysql Query Error555:%d \n",connection);
      return 1;
    }  

	result = mysql_store_result(connection); // 获取结果集  
	// mysql_field_count()返回connection查询的列数  
	for(int i=0; i < mysql_field_count(connection); ++i)  
	{  
	    // 获取下一行  
	    row = mysql_fetch_row(result);  
	    if(row <= 0)  
	    {  
	        break;  
	    }  
	    int wordID,objectID;
        wordID = atoi(row[0]);
        objectID = atoi(row[2]);
        UDEBUG("wordID = %d   objectID = %d \n",wordID, objectID);
        wordToObjects_.insert(std::make_pair(wordID,objectID));
	}  
	// 释放结果集的内存  
	mysql_free_result(result);  
	
  std::string select_1 = "SELECT count(*) FROM descriptor_table; ";
  
  if(mysql_query(connection, select_1.c_str()))	
  {  
	UDEBUG("mysql Query Error666:%d \n",connection);
	return 1;
  }  
  result = mysql_store_result(connection); // 获取结果集  

  int descriptorRows = mysql_num_rows(result) ;
	
  //load vocabulary index
  flannIndex_ = new my_cvflann::my_KMeansIndex<cvflann::L2<float>>(connection,descriptorRows);
  FILE *fp = fopen(vocabularyFileName.c_str(), "rb");
  if(NULL == fp)
  {
    UDEBUG("open file:%s error.\n",vocabularyFileName.c_str());
    exit(1);
  }
  flannIndex_->loadIndex(fp);
#endif
  return true;
}



int Vocabulary::getDescriptorRows(sqlite3 *db)
{
  int result = 0;
  sqlite3_stmt * stat = NULL;
  std::string select = "SELECT count(*) FROM descriptor_table; ";
  int descriptorRows = 0;
  result = sqlite3_prepare(db,select.c_str(),-1, &stat, 0);
  if(result != SQLITE_OK)
  {
    return descriptorRows;
  }
  while(1)
  {
    result = sqlite3_step(stat);
    if(SQLITE_ROW == result)
    {
      descriptorRows = sqlite3_column_int(stat,0);
    }
    else if(SQLITE_DONE == result)
    {
      break;
    }
  }
  sqlite3_finalize(stat);
  return descriptorRows;
}
#endif

void Vocabulary::addWords(const cv::Mat &descriptors, const std::multimap<int, int> &words,const int &ids)
{
  std::multimap<int, int>::const_iterator it = words.begin();
  for(; it != words.end(); it++)
  {
    if(indexedDescriptors_.rows > it->first/* && descriptors.rows > it->second*/)
    {
//      descriptors.row(it->second).copyTo(indexedDescriptors_.row(it->first));
      //cout << indexedDescriptors_.row(it->first) <<"===" << descriptors.row(it->second);
      wordToObjects_.insert(std::make_pair(it->first, ids));
    }
  }
}

std::multimap<int, int> Vocabulary::addWords(const cv::Mat & descriptorsIn, int objectId)
{
  std::multimap<int, int> words;
	if (descriptorsIn.empty())
	{
		return words;
	}

	cv::Mat descriptors;
  //getNearestNeighbor_7ConvertBinToFloat
  //在量子化之前将二进制描述符转换为浮点数，因此可以使用FLANN策略。
#ifndef HANMING
  if(descriptorsIn.type() == CV_8U && (Setting::getValue_string("convertType")== "float"))
  {
    descriptorsIn.convertTo(descriptors, CV_32F);
  }
  else
#endif
	{
		descriptors = descriptorsIn;
	}
  //getGeneral_vocabularyIncremental
  //词汇量是递增的。当添加新对象时，它们的描述符与已经在词汇表中的描述符相比较，以查找是否已经存在
  //Fixed:固定
  //default false || false
  //Settings::getGeneral_vocabularyIncremental() ||
  if(Setting::getValue_bool("vocabularyFixed"))
	{
		int k = 2;
		cv::Mat results;
		cv::Mat	dists;

		bool globalSearch = false;

		if(!indexedDescriptors_.empty() && indexedDescriptors_.rows >= (int)k)
		{
			if(indexedDescriptors_.type() != descriptors.type() || indexedDescriptors_.cols != descriptors.cols)
			{
        if(Setting::getValue_bool("vocabularyFixed"))
				{
//					UERROR("Descriptors (type=%d size=%d) to search in vocabulary are not the same type/size as those in the vocabulary (type=%d size=%d)! Empty words returned.",
//							descriptors.type(), descriptors.cols, indexedDescriptors_.type(), indexedDescriptors_.cols);
					return words;
				}
				else
				{
//					UFATAL("Descriptors (type=%d size=%d) to search in vocabulary are not the same type/size as those in the vocabulary (type=%d size=%d)!",
//							descriptors.type(), descriptors.cols, indexedDescriptors_.type(), indexedDescriptors_.cols);
				}
			}

			this->search(descriptors, results, dists, k);

			if( dists.type() == CV_32S )
			{
				cv::Mat temp;
				dists.convertTo(temp, CV_32F);
				dists = temp;
			}

			globalSearch = true;
		}

    if(!Setting::getValue_bool("vocabularyFixed"))
		{
			notIndexedWordIds_.reserve(notIndexedWordIds_.size() + descriptors.rows);
			notIndexedDescriptors_.reserve(notIndexedDescriptors_.rows + descriptors.rows);
		}
		int matches = 0;
		for(int i = 0; i < descriptors.rows; ++i)
		{
      std::multimap<float, int> fullResults; // nearest descriptors sorted by distance
			if(notIndexedDescriptors_.rows)
			{
      //	UASSERT(notIndexedDescriptors_.type() == descriptors.type() && notIndexedDescriptors_.cols == descriptors.cols);

				// Check if this descriptor matches with a word not already added to the vocabulary
				// Do linear search only
				cv::Mat tmpResults;
				cv::Mat	tmpDists;
				if(descriptors.type()==CV_8U)
				{
					//normType – One of NORM_L1, NORM_L2, NORM_HAMMING, NORM_HAMMING2. L1 and L2 norms are
					//			 preferable choices for SIFT and SURF descriptors, NORM_HAMMING should be
					// 			 used with ORB, BRISK and BRIEF, NORM_HAMMING2 should be used with ORB
					// 			 when WTA_K==3 or 4 (see ORB::ORB constructor description).
					int normType = cv::NORM_HAMMING;
          if((Setting::getValue_string("FeatureType")=="ORB") &&
            (Setting::getValue_int("ORB/WTA_K")==3 || Setting::getValue_int("ORB/WTA_K")==4))
					{
						normType = cv::NORM_HAMMING2;
					}

          //最近邻搜索
					cv::batchDistance( descriptors.row(i),
									notIndexedDescriptors_,
									tmpDists,
									CV_32S,
									tmpResults,
									normType,
									notIndexedDescriptors_.rows>=k?k:1,
									cv::Mat(),
									0,
									false);
				}
				else
				{
					cv::flann::Index tmpIndex;
#if CV_MAJOR_VERSION == 2 and CV_MINOR_VERSION == 4 and CV_SUBMINOR_VERSION >= 12
					tmpIndex.build(notIndexedDescriptors_, cv::Mat(), cv::flann::LinearIndexParams(), cvflann::FLANN_DIST_L2);
#else
					tmpIndex.build(notIndexedDescriptors_, cv::flann::LinearIndexParams(), cvflann::FLANN_DIST_L2);
#endif
					tmpIndex.knnSearch(descriptors.row(i), tmpResults, tmpDists, notIndexedDescriptors_.rows>1?k:1, cvflann::FLANN_DIST_L2);
				}

				if( tmpDists.type() == CV_32S )
				{
					cv::Mat temp;
					tmpDists.convertTo(temp, CV_32F);
					tmpDists = temp;
				}

				for(int j = 0; j < tmpResults.cols; ++j)
				{
					if(tmpResults.at<int>(0,j) >= 0)
					{
						//printf("local i=%d, j=%d, tmpDist=%f tmpResult=%d\n", i ,j, tmpDists.at<float>(0,j), tmpResults.at<int>(0,j));
            fullResults.insert(std::make_pair(tmpDists.at<float>(0,j), notIndexedWordIds_.at(tmpResults.at<int>(0,j))));
					}
				}
			}

			if(globalSearch)
			{
				for(int j=0; j<k; ++j)
				{
					if(results.at<int>(i,j) >= 0)
					{
						//printf("global i=%d, j=%d, dist=%f\n", i ,j, dists.at<float>(i,j));
            fullResults.insert(std::make_pair(dists.at<float>(i,j), results.at<int>(i,j)));
					}
				}
			}

			bool matched = false;
      if(Setting::getValue_bool("nndrRatioUsed") &&
			   fullResults.size() >= 2 &&
         fullResults.begin()->first <= Setting::getValue_float("nndrRatio") * (++fullResults.begin())->first)
			{
				matched = true;
			}

      if((matched || !Setting::getValue_bool("nndrRatioUsed")) &&
         Setting::getValue_bool("minDistanceUsed"))
      {
        if(fullResults.begin()->first <= Setting::getValue_float("minDistance"))
        {
          matched = true;
        }
        else
        {
          matched = false;
        }
      }
      if(!matched && !Setting::getValue_bool("nndrRatioUsed") && !Setting::getValue_bool("minDistanceUsed"))
      {
        matched = true; // no criterion, match to the nearest descriptor
      }
			if(matched)
			{
        words.insert(std::make_pair(fullResults.begin()->second, i));
        wordToObjects_.insert(std::make_pair(fullResults.begin()->second, objectId));
				++matches;
			}
      else if(!Setting::getValue_bool("invertedSearch") || !Setting::getValue_bool("vocabularyFixed"))
			{
				//concatenate new words
				notIndexedWordIds_.push_back(indexedDescriptors_.rows + notIndexedDescriptors_.rows);
				notIndexedDescriptors_.push_back(descriptors.row(i));
        words.insert(std::make_pair(notIndexedWordIds_.back(), i));
        wordToObjects_.insert(std::make_pair(notIndexedWordIds_.back(), objectId));
			}
			else
			{
        words.insert(std::make_pair(-1, i)); // invalid word
			}
		}
	}
	else
	{
		for(int i = 0; i < descriptors.rows; ++i)
		{
      wordToObjects_.insert(std::make_pair(indexedDescriptors_.rows + notIndexedDescriptors_.rows+i, objectId));
      words.insert(std::make_pair(indexedDescriptors_.rows + notIndexedDescriptors_.rows+i, i));
			notIndexedWordIds_.push_back(indexedDescriptors_.rows + notIndexedDescriptors_.rows+i);
		}

		//just concatenate descriptors
		notIndexedDescriptors_.push_back(descriptors);
	}
	return words;
}

/****************************************
 * 将没有索引的descriptors添加到索引descriptors中
 * 如果有需要  重新构建一个flann
 ************************************/
void Vocabulary::update()
{
	if(!notIndexedDescriptors_.empty())
	{
	UDEBUG("update 111.. \n");
		if(!indexedDescriptors_.empty())
		{
		UDEBUG("update return.. \n");
      //return;
		}
		
		//concatenate descriptors
    //连接描述符
		indexedDescriptors_.push_back(notIndexedDescriptors_);

		notIndexedDescriptors_ = cv::Mat();
		notIndexedWordIds_.clear();
	}

  ////if ! 暴力搜索最近的
  if(!indexedDescriptors_.empty() && !Setting::getValue_bool("bruteSearch"))
  {
 #if 0
    UDEBUG("build flannIndex_  indexedDescriptors_ size = %d \n", indexedDescriptors_.rows);
#ifndef HANMING
    flannIndex_ = new my_cvflann::my_KMeansIndex<cvflann::L2<float>>(indexedDescriptors_);
    flannIndex_->buildIndex();
#else
//    flannIndex_ = new cv::flann::Index(indexedDescriptors_,cv::flann::LshIndexParams(20,10,2));
    flannIndex_ = new cv::flann::Index(indexedDescriptors_,cv::flann::HierarchicalClusteringIndexParams()/*,cvflann::FLANN_DIST_HAMMING*/);
#endif
    UDEBUG("build flannIndex_  end \n");
#endif
  cv::flann::IndexParams * params = new cv::flann::KDTreeIndexParams(4);
#if CV_MAJOR_VERSION == 2 and CV_MINOR_VERSION == 4 and CV_SUBMINOR_VERSION >= 12
  flannIndex_.build(indexedDescriptors_, cv::Mat(), *params, /*Settings::getFlannDistanceType()*/ (cvflann::flann_distance_t)(0+1));
#else
  flannIndex_.build(indexedDescriptors_, *params, /*Settings::getFlannDistanceType()*/(cvflann::flann_distance_t)(0+1));
#endif
   delete params;
  

  }
}

void Vocabulary::search(const cv::Mat & descriptorsIn, cv::Mat & results, cv::Mat & dists, int k)
{
  if(!indexedDescriptors_.empty() || NULL != mp_descriptorData_)
	{

		cv::Mat descriptors;
    //Convert Bin To Float
#ifndef HANMING
    //if(descriptorsIn.type() == CV_8U && (Setting::getValue_string("convertType")== "float"))
    //{
    //  descriptorsIn.convertTo(descriptors, CV_32F);
    //}
    //else
#endif
		{
			descriptors = descriptorsIn;
		}
    if(Setting::getValue_bool("bruteSearch"))
    {
      std::vector<std::vector<cv::DMatch> > matches;
      cv::BFMatcher matcher(indexedDescriptors_.type()==CV_8U?cv::NORM_HAMMING:cv::NORM_L2);
      matcher.knnMatch(descriptors, indexedDescriptors_, matches, k);
      results = cv::Mat((int)matches.size(), k, CV_32SC1);
      dists = cv::Mat((int)matches.size(), k, CV_32FC1);
      for(unsigned int i=0; i<matches.size(); ++i)
      {
        for(int j=0; j<k; ++j)
        {
          results.at<int>(i, j) = matches[i].at(j).trainIdx;
          dists.at<float>(i, j) = matches[i].at(j).distance;
        }
      }
    }
    else
    {
      /*
       * descriptors:查询点
       * results:将包含最近邻居的索引的向量 min k
       * dests:将会包含到最近的邻居的距离的向量 min k
       * k:要搜索的最近邻居的数目
      */
      //flannIndex_->knnSearch(descriptors, results, dists, k);
      flannIndex_.knnSearch(descriptors, results, dists, k,
                                      cv::flann::SearchParams(
                                              /*Settings::getNearestNeighbor_search_checks()*/32,
                                              /*Settings::getNearestNeighbor_search_eps()*/0,
                                              /*Settings::getNearestNeighbor_search_sorted()*/1));
    }


    if( dists.type() == CV_32S )
    {
      cv::Mat temp;
      dists.convertTo(temp, CV_32F);
      dists = temp;
    }
  }
  else
  {
    UDEBUG("indexedDescriptors_ is empty.\n");
  }

}

} // namespace find_object
