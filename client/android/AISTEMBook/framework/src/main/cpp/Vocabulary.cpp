/*
Copyright (c) 2011-2014, Mathieu Ltdde - IntRoLab - Universite de Sherbrooke
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

extern int my_feature_type;

namespace my_find_object {

Vocabulary::Vocabulary():mp_descriptorData_(NULL)
{
}

Vocabulary::~Vocabulary()
{
	UDEBUG("delete Vocabulary");

	if( flannIndex_ != NULL )
	{
		delete flannIndex_;
		flannIndex_ = NULL;
	}

	if( mp_descriptorData_ != NULL )
	{
		free(mp_descriptorData_ );
		mp_descriptorData_ = NULL;
	}

	if( wordToObjects_.size() > 0 )
	{
		wordToObjects_.clear();
	}
	if( notIndexedWordIds_.size() > 0 )
	{
      notIndexedWordIds_.clear();
	}
	UDEBUG("delete Vocabulary end..");
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

bool Vocabulary::load(FILE* filePtr)
{
  int rows,cols,type;
  int dataSize;
  fread(&rows,sizeof(rows),1,filePtr);
  fread(&cols,sizeof(cols),1,filePtr);
  fread(&type,sizeof(type),1,filePtr);

  fread(&m_fileSize_,sizeof(m_fileSize_),1,filePtr);
  mp_descriptorData_ = (void*)malloc(m_fileSize_);
  if( mp_descriptorData_ == NULL ){
    return false;
  }else{
    fread(mp_descriptorData_,m_fileSize_,1,filePtr);
  }
  indexedDescriptors_ = cv::Mat(rows, cols, type, mp_descriptorData_).clone();

  int wordsize;
  fread(&wordsize,sizeof(wordsize),1,filePtr);
  for(int i=0; i<wordsize; i++)
  {
    int word[2];
    fread(word,sizeof(wordsize),2,filePtr);
    wordToObjects_.insert(std::make_pair(word[0],word[1]));
  }

  cv::flann::IndexParams * params = new cv::flann::KDTreeIndexParams(4);
#if CV_MAJOR_VERSION == 2 and CV_MINOR_VERSION == 4 and CV_SUBMINOR_VERSION >= 12
  my_flannIndex_.build(indexedDescriptors_, cv::Mat(), *params, /*Settings::getFlannDistanceType()*/ (cvflann::flann_distance_t)(0+1));
#else
  my_flannIndex_.build(indexedDescriptors_, *params, /*Settings::getFlannDistanceType()*/(cvflann::flann_distance_t)(0+1));
#endif
  delete params;

  return true;
}


bool Vocabulary::load_ex(FILE* filePtr)
{
  fread(&m_fileSize_,sizeof(m_fileSize_),1,filePtr);
  mp_descriptorData_ = (void*)malloc(m_fileSize_);
  if( mp_descriptorData_ == NULL ){
    return false;
  }else{
    fread(mp_descriptorData_,m_fileSize_,1,filePtr);
  }

  int wordsize;
  fread(&wordsize,sizeof(wordsize),1,filePtr);
  for(int i=0; i<wordsize; i++)
  {
    int word[2];
    fread(word,sizeof(wordsize),2,filePtr);
    wordToObjects_.insert(std::make_pair(word[0],word[1]));
  }

#ifndef HANMING
	UDEBUG("my_feature_type my_feature_type =%d ..... ",my_feature_type);

	if(my_feature_type == 1)
	{
		flannIndex_ = new my_cvflann::my_KMeansIndex< cvflann::L2<float> >(mp_descriptorData_, m_fileSize_/256);
	}
	else if (my_feature_type == 2)
	{
		flannIndex_ = new my_cvflann::my_KMeansIndex< cvflann::L2<float> >(mp_descriptorData_, m_fileSize_/128);
	}
	else if (my_feature_type == 3)
	{
		flannIndex_ = new my_cvflann::my_KMeansIndex< cvflann::L2<float> >(mp_descriptorData_, m_fileSize_/512);
	}
  
  flannIndex_->loadIndex(filePtr);
#endif
  return true;
}

bool Vocabulary::save(FILE *filePtr)
{
  int dataSize = indexedDescriptors_.elemSize()*indexedDescriptors_.cols*indexedDescriptors_.rows;
  int type = indexedDescriptors_.type();
  fwrite(&(indexedDescriptors_.rows), sizeof(indexedDescriptors_.rows), 1, filePtr);
  fwrite(&(indexedDescriptors_.cols), sizeof(indexedDescriptors_.cols), 1, filePtr);
  fwrite(&type, sizeof(type), 1, filePtr);
  fwrite(&dataSize, sizeof(int), 1, filePtr);
  fwrite(indexedDescriptors_.data, dataSize, 1, filePtr);

  int wordsize = wordToObjects_.size();
  fwrite(&wordsize,sizeof(wordsize),1,filePtr);
  int word[2];
  for(std::multimap<int, int>::const_iterator it = wordToObjects_.begin();
      it != wordToObjects_.end(); it++)
  {
    word[0] = it->first;
    word[1] = it->second;
    fwrite(word,sizeof(wordsize),2,filePtr);
  }
#if 0
  #ifndef HANMING
    flannIndex_->saveIndex(filePtr);
  #endif
#endif
    return true;
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
					return words;
				}
				else
				{
					//
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
				// Check if this descriptor matches with a word not already added to the vocabulary
				// Do linear search only
				cv::Mat tmpResults;
				cv::Mat	tmpDists;
				if(descriptors.type()==CV_8U)
				{
					int normType = cv::NORM_HAMMING;
          if(/*(Setting::getValue_string("FeatureType")=="ORB")*/my_feature_type == 2)
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
         fullResults.begin()->first <= 0.8 * (++fullResults.begin())->first)
			{
				matched = true;
			}

      if((matched || !Setting::getValue_bool("nndrRatioUsed")) &&
         Setting::getValue_bool("minDistanceUsed"))
      {
        if(fullResults.begin()->first <= 35)
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
		if(!indexedDescriptors_.empty())
		{
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
    //UDEBUG("build flannIndex_  indexedDescriptors_ size = %d \n", indexedDescriptors_.rows);
    UDEBUG("build flannIndex_ enter time(ms): %f \n", Setting::getCurTime());

    cv::flann::IndexParams * params = new cv::flann::KDTreeIndexParams(4);
#if CV_MAJOR_VERSION == 2 and CV_MINOR_VERSION == 4 and CV_SUBMINOR_VERSION >= 12
    my_flannIndex_.build(indexedDescriptors_, cv::Mat(), *params, /*Settings::getFlannDistanceType()*/ (cvflann::flann_distance_t)(0+1));
#else
    my_flannIndex_.build(indexedDescriptors_, *params, /*Settings::getFlannDistanceType()*/(cvflann::flann_distance_t)(0+1));
#endif
    delete params;

    UDEBUG("build flannIndex_ exit time(ms): %f \n", Setting::getCurTime());
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

   //   descriptorsIn.convertTo(descriptors, CV_32F);
  //  }
  //  else
#endif
		{
			descriptors = descriptorsIn;
		}
    if(false)
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
      #if 0
      flannIndex_->knnSearch(descriptors, results, dists, k);
      #else
      my_flannIndex_.knnSearch(descriptors, results, dists, k,
                                      cv::flann::SearchParams(
                                              /*Settings::getNearestNeighbor_search_checks()*/32,
                                              /*Settings::getNearestNeighbor_search_eps()*/0,
                                              /*Settings::getNearestNeighbor_search_sorted()*/1));
      #endif
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


void Vocabulary::search_ex(const cv::Mat & descriptorsIn, cv::Mat & results, cv::Mat & dists, int k)
  {
    if(!indexedDescriptors_.empty() || NULL != mp_descriptorData_)
    {
  
      cv::Mat descriptors;
      //Convert Bin To Float
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
      if(false)
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
        flannIndex_->knnSearch(descriptors, results, dists, k);
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
