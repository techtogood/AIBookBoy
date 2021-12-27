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

#ifndef OBJSIGNATURE_H_
#define OBJSIGNATURE_H_

#include <opencv2/opencv.hpp>
#include <stdlib.h>
#include <iostream>
#include "Setting.h"

namespace my_find_object {

class ObjSignature {
public:
	ObjSignature() :
		id_(-1)
	{}
  ObjSignature(int id, const cv::Mat & image, const std::string & filePath) :
		id_(id),
		image_(image),
		filePath_(filePath)
  {
    rect_.push_back(cv::Point2d(0,0));
    rect_.push_back(cv::Point2d(0,image.rows));
    rect_.push_back(cv::Point2d(image.cols,image.rows));
    rect_.push_back(cv::Point2d(image.cols,0));
  }
	virtual ~ObjSignature() {}

	void setData(const std::vector<cv::KeyPoint> & keypoints, const cv::Mat & descriptors)
	{
		if(keypoints.size() < 50 || descriptors.rows < 50)
		{
			keypoints_.clear();
			descriptors_.release();
		}
		else
		{
			keypoints_ = keypoints;
			descriptors_ = descriptors;
			words_size = descriptors_.rows;//words_.size();
		}

	}
  void setWords(const std::multimap<int, int> & words) {words_ = words;}
	void setId(int id) {id_ = id;}
	void removeImage() {image_ = cv::Mat();}

  const std::vector<cv::Point2d> & rect() const {return rect_;}
  void setrect(int rows,int cols)
  {
    rect_.clear();
    rect_.push_back(cv::Point2d(0,0));
    rect_.push_back(cv::Point2d(0,rows));
    rect_.push_back(cv::Point2d(cols,rows));
    rect_.push_back(cv::Point2d(cols,0));
  }
	int id() const {return id_;}
  const std::string & filePath() const {return filePath_;}
	const cv::Mat & image() const {return image_;}
	const std::vector<cv::KeyPoint> & keypoints() const {return keypoints_;}
	const cv::Mat & descriptors() const {return descriptors_;}
  const std::multimap<int, int> & words() const {return words_;}

  const int width() { return image_.cols;}
  const int height() { return image_.rows;}
  const int getDataSize()
  {
	  if(!keypoints().size() || descriptors().empty())
	  {
		  return 0;
	  }
	  else
	  {
#if 0  
return  (sizeof(id_)+sizeof(keypoints_.size())+sizeof(cv::KeyPoint)*keypoints_.size()+sizeof(descriptors_.rows)
                            +sizeof(descriptors_.cols)+sizeof(int)+sizeof(int)*2*words_.size()+sizeof(image_.rows)+sizeof(image_.cols));
#endif  
    return (sizeof(id_)+sizeof(int)+sizeof(cv::KeyPoint)*keypoints_.size()+sizeof(words_size)+sizeof(int)*2*words_.size()/*+sizeof(image_rows)+sizeof(image_cols)*/);

	  }
	  
  }
  void save(FILE *filePtr) const
  {
	  if(!keypoints().size() || descriptors().empty())
	  {
		  return;
	  }
    int size=(int)keypoints_.size();
    fwrite(&id_, sizeof(id_), 1, filePtr);
    fwrite(&size, sizeof(size), 1, filePtr);
    for(unsigned int j=0; j<keypoints_.size(); ++j)
    {
      fwrite(&(keypoints_.at(j)), sizeof(cv::KeyPoint), 1, filePtr);
    }
		
    int wordSize = words_size;
    fwrite(&wordSize, sizeof(wordSize), 1, filePtr);
    for(std::multimap<int, int>::const_iterator it = words_.begin(); it != words_.end(); it ++)
    {
      fwrite(&(it->first), sizeof(int), 1, filePtr);
      fwrite(&(it->second), sizeof(int), 1, filePtr);
    }
	  //fwrite(&(image_rows), sizeof(image_rows), 1, filePtr);
	  //fwrite(&(image_cols), sizeof(image_cols), 1, filePtr);
    //UDEBUG("obj save rows =%d cols=%d \n",image_rows,image_cols);
  }

  int load(FILE *filePtr)
  {
    if(feof(filePtr))
    {
      UDEBUG("read data error 2 \n");
      return 1;
    }

    int nKpts;
    fread(&id_,sizeof(id_),1,filePtr);
    fread(&nKpts,sizeof(nKpts),1,filePtr);
    UDEBUG("obj load id_ =%d nKpts=%d \n",id_,nKpts);
    if(feof(filePtr))
    {
      UDEBUG("read data error 1 \n", id_, nKpts);
      return 1;
    }
    keypoints_.resize(nKpts);
    for(int i=0;i<nKpts;++i)
    {
        fread(&(keypoints_.at(i)),sizeof(cv::KeyPoint),1,filePtr);
    }

    fread(&words_size,sizeof(words_size),1,filePtr);
    for(int i=0; i<words_size; i++)
    {
      int first,second;
      fread(&first,sizeof(first),1,filePtr);
      fread(&second,sizeof(second),1,filePtr);
      words_.insert(std::make_pair(first,second));
    }


    //fread(&image_rows,sizeof(image_rows),1,filePtr);
    //fread(&image_cols,sizeof(image_cols),1,filePtr);
    //UDEBUG("image rows = %d  image cols = %d   \n", image_rows, image_cols);
    //setrect(image_rows,image_cols);
    return 0;
  }
  
  
  int load_ex(FILE *filePtr)
  {
    if(feof(filePtr))
    {
      UDEBUG("read data error 2 \n");
      return 1;
    }

    int nKpts;
    fread(&id_,sizeof(id_),1,filePtr);
    fread(&nKpts,sizeof(nKpts),1,filePtr);
    if(feof(filePtr))
    {
      UDEBUG("read data error 1 \n", id_, nKpts);
      return 1;
    }
    keypoints_.resize(nKpts);
    for(int i=0;i<nKpts;++i)
    {
        fread(&(keypoints_.at(i)),sizeof(cv::KeyPoint),1,filePtr);
    }
    int rows,cols,type;
    int dataSize;
    fread(&rows,sizeof(rows),1,filePtr);
    fread(&cols,sizeof(cols),1,filePtr);
    fread(&type,sizeof(type),1,filePtr);
    fread(&dataSize,sizeof(dataSize),1,filePtr);
    for(int i=0; i<rows; i++)
    {
      int first,second;
      fread(&first,sizeof(first),1,filePtr);
      fread(&second,sizeof(second),1,filePtr);
	    words_.insert(std::make_pair(first,second));
    }

    fread(&rows,sizeof(rows),1,filePtr);
    fread(&cols,sizeof(cols),1,filePtr);
    setrect(rows,cols);
    return 0;
  }
private:
	int id_;
	//int image_cols;
	//int image_rows;
	int words_size;
	cv::Mat image_;
  //QRect rect_;
  std::vector<cv::Point2d> rect_;
  std::string filePath_;
	std::vector<cv::KeyPoint> keypoints_;
	cv::Mat descriptors_;
  std::multimap<int, int> words_; // <word id, keypoint indexes>

};

} // namespace find_object

#endif /* OBJSIGNATURE_H_ */
