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
//#include <sqlite3.h>
#include <stdlib.h>
#include <iostream>
#include<mysql/mysql.h>  
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
    //rect_(0,0,image.cols, image.rows),
		filePath_(filePath)
  {
    rect_.push_back(cv::Point2d(0,0));
    rect_.push_back(cv::Point2d(0,image.rows));
    rect_.push_back(cv::Point2d(image.cols,image.rows));
    rect_.push_back(cv::Point2d(image.cols,0));
  }
	virtual ~ObjSignature() 
	{
		UDEBUG("delete ObjSignature \n");
		rect_.clear();
		keypoints_.clear();
		words_.clear();
		UDEBUG("delete ObjSignature end\n");
	}

	void setData(const std::vector<cv::KeyPoint> & keypoints, const cv::Mat & descriptors)
	{
		keypoints_ = keypoints;
		descriptors_ = descriptors;
		image_rows = image_.rows;
		image_cols = image_.cols;
		words_size = descriptors_.rows;//words_.size();
		printf("setData image_rows=%d image_cols =%d words_size=%d des rows=%d\n",image_rows,image_cols,words_size,descriptors_.rows);
		
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
  #if 0
  const int getDataSize() { return  (sizeof(id_)+sizeof(keypoints_.size())+sizeof(cv::KeyPoint)*keypoints_.size()+sizeof(descriptors_.rows)
                            +sizeof(descriptors_.cols)+sizeof(int)+sizeof(int)*2*words_.size()+sizeof(image_.rows)+sizeof(image_.cols));}
  #else
  const int getDataSize() { return  (sizeof(id_)+sizeof(int)+sizeof(cv::KeyPoint)*keypoints_.size()+sizeof(words_size)+sizeof(int)*2*words_.size()+sizeof(image_rows)+sizeof(image_cols));}
  #endif
#if 0
  void save(FILE *filePtr) const
	{
    fprintf(filePtr,"%d %d ",id_,(int)keypoints_.size());
		for(unsigned int j=0; j<keypoints_.size(); ++j)
		{
      fprintf(filePtr,"%f %d %d %f %f %f %f ",keypoints_.at(j).angle,keypoints_.at(j).class_id,keypoints_[j].octave,
              keypoints_.at(j).pt.x, keypoints_.at(j).pt.y, keypoints_.at(j).response, keypoints_.at(j).size);
		}

    int dataSize = descriptors_.elemSize()*descriptors_.cols*descriptors_.rows;
    fprintf(filePtr," %d %d %d %d ",descriptors_.rows, descriptors_.cols, descriptors_.type(), dataSize);
    UDEBUG("rows = %d  cols = %d datasize = %d \n", descriptors_.rows, descriptors_.cols, dataSize);
//    fwrite(descriptors_.data, dataSize, 1, descriptorFile);
    for(std::multimap<int, int>::const_iterator it = words_.begin(); it != words_.end(); it ++)
    {
    //printf( "word=%d des=%d \n", it->first, it->second);
      fprintf(filePtr, "%d %d ", it->first, it->second);
    }
    
	printf("rows=%d cols=%d \n",image_.rows ,image_.cols);

    fprintf(filePtr, "%d %d ",image_.rows ,image_.cols);
	}
	#else
    void save(FILE *filePtr) const
	{
    //fprintf(filePtr,"%d %d ",id_,(int)keypoints_.size());
    printf("save id_=%d keypoint size=%d \n",id_,keypoints_.size());
    int size=(int)keypoints_.size();
    fwrite(&id_, sizeof(id_), 1, filePtr);
    fwrite(&size, sizeof(size), 1, filePtr);
		for(unsigned int j=0; j<keypoints_.size(); ++j)
		{
      /*fprintf(filePtr,"%f %d %d %f %f %f %f ",keypoints_.at(j).angle,keypoints_.at(j).class_id,keypoints_[j].octave,
              keypoints_.at(j).pt.x, keypoints_.at(j).pt.y, keypoints_.at(j).response, keypoints_.at(j).size);*/
              fwrite(&(keypoints_.at(j)), sizeof(cv::KeyPoint), 1, filePtr);
		}
		printf("save ptr=%d\n",ftell(filePtr));
		
    //int dataSize = descriptors_.elemSize()*descriptors_.cols*descriptors_.rows;
    //int type = descriptors_.type();
    //fwrite(&(descriptors_.rows), sizeof(descriptors_.rows), 1, filePtr);
    //fwrite(&(descriptors_.cols), sizeof(descriptors_.cols), 1, filePtr);
    //fwrite(&type, sizeof(type), 1, filePtr);
    //fwrite(&dataSize, sizeof(dataSize), 1, filePtr);
    //fprintf(filePtr," %d %d %d %d ",descriptors_.rows, descriptors_.cols, descriptors_.type(), dataSize);
    //UDEBUG("rows = %d  cols = %d type=%d ze = %d \n", descriptors_.rows, descriptors_.cols,type,dataSize);
    //fwrite(descriptors_.data, dataSize, 1, filePtr);
    int wordSize = words_size;
    fwrite(&wordSize, sizeof(wordSize), 1, filePtr);
    for(std::multimap<int, int>::const_iterator it = words_.begin(); it != words_.end(); it ++)
    {
    //printf( "word=%d des=%d \n", it->first, it->second);
      //fprintf(filePtr, "%d %d ", it->first, it->second);
      fwrite(&(it->first), sizeof(int), 1, filePtr);
      fwrite(&(it->second), sizeof(int), 1, filePtr);
    }
    
  	printf("rows=%d cols=%d \n",image_rows ,image_cols);

  	fwrite(&(image_rows), sizeof(image_rows), 1, filePtr);
  	fwrite(&(image_cols), sizeof(image_cols), 1, filePtr);

    //fprintf(filePtr, "%d %d ",image_.rows ,image_.cols);
	}
	#endif
#if 0
  void save(sqlite3 * db)
  {
    saveObject(db);
    saveWordToObject(db);
  }
  /*
   * object_table
   * [objectId,keypoints[],imageRow,imageCol]
   * CREATE TABLE object_table(objectID integer PRIMARY KEY not null,keypoints blob ,imageRow integer,imageCol integer);
  */
  void saveObject(sqlite3 * db)
  {
    UDEBUG("saveObject begin .\n");
    int result = 0;
    sqlite3_stmt * stat = NULL;
    std::string sql = "INSERT INTO object_table VALUES(?,?,?,?)";
    result = sqlite3_prepare(db,sql.c_str(),-1, &stat, 0);
    if(result != SQLITE_OK)
    {
      return ;
    }

    result = sqlite3_bind_int(stat, 1, id_);
    if(result != SQLITE_OK)
    {
      return ;
    }

    result = sqlite3_bind_blob(stat, 2, (void*)&keypoints_[0], sizeof(cv::KeyPoint)*keypoints_.size(), NULL);
    if(result != SQLITE_OK)
    {
      return ;
    }

    result = sqlite3_bind_int(stat, 3, image_.rows);
    if(result != SQLITE_OK)
    {
      return ;
    }

    result = sqlite3_bind_int(stat, 4, image_.cols);
    if(result != SQLITE_OK)
    {
      return ;
    }

    result = sqlite3_step(stat);

    if(result != SQLITE_DONE)
    {
      UDEBUG("insert %d object into object_table error \n", id_);
    }
    sqlite3_finalize(stat);
    UDEBUG("saveObject end .\n");
  }

  /*
   * word_2_object_table
   * [objectID, wordID, keypointID]
   * CREATE TABLE word_2_object_table(objectID integer,wordID integer,keypointID integer);
  */
  void saveWordToObject(sqlite3 * db)
  {
    UDEBUG("saveWordToObject begin .\n");
    int result = 0;
    std::string insert = "INSERT INTO word_2_object_table VALUES(";
    for(std::multimap<int, int>::const_iterator it = words_.begin(); it != words_.end(); it ++)
    {
      char date[50];
      memset(date,0,50);
      sprintf(date,"%d,%d,%d)",id_, it->first, it->second);
      std::string sql = insert + date;
//      UDEBUG ("%s \n", sql.c_str());
      char *err = NULL;
      result = sqlite3_exec(db, sql.c_str(), NULL, 0, &err);
      if(result != SQLITE_OK)
      {
        UDEBUG("insert %d object into word_2_object_table error: %s \n", id_, err);
        return ;
      }
    }
    UDEBUG("saveObject end .\n");
  }
#endif

#if 0
	void save(MYSQL *connection)
	{
	  saveObject(connection);
	  saveWordToObject(connection);
	}
	/*
	 * object_table
	 * [objectId,keypoints[],imageRow,imageCol]
	 * CREATE TABLE object_table(objectID integer PRIMARY KEY not null,keypoints blob ,imageRow integer,imageCol integer);
	*/
	void saveObject(MYSQL *connection)
	{
	  UDEBUG("saveObject begin lenth=%d.\n",sizeof(cv::KeyPoint)*keypoints_.size());
	  int result = 0;
	  char sql[sizeof(cv::KeyPoint)*keypoints_.size()*2], *end;

	  //void* keypoints = (void*)malloc(sizeof(cv::KeyPoint)*keypoints_.size());
	  sprintf(sql, "INSERT INTO object_table(id,rows,cols,keypoints) VALUE (%d, %d, %d, ", id_ ,image_.rows, image_.cols);
      end = sql + strlen(sql);
      *end++ = '\'';
      end += mysql_real_escape_string(connection, end,(char*)&keypoints_[0],sizeof(cv::KeyPoint)*keypoints_.size());
      *end++ = '\'';
      *end++ = ')';

      UDEBUG("end - sql: %d\n", (unsigned int)(end - sql));

      if(mysql_real_query(connection, sql, (unsigned int)(end - sql)))
      {
        UDEBUG("Query failed (%s)\n", mysql_error(connection));
      }
      /*memset(sql,0,256);
      sprintf(sql, "INSERT INTO object_table(rows, cols) VALUE (%d, %d)", image_.rows, image_.cols );
      if(mysql_query(connection, sql))  
      {  
        UDEBUG("Query Error:\n",mysql_error(connection));
      }  */
	  UDEBUG("saveObject end .\n");
	  return;
	}
  
	/*
	 * word_2_object_table
	 * [objectID, wordID, keypointID]
	 * CREATE TABLE word_2_object_table(objectID integer,wordID integer,keypointID integer);
	*/
	void saveWordToObject(MYSQL *connection)
	{
	  UDEBUG("saveWordToObject begin .\n");
	  int result = 0;
	  std::string insert = "INSERT INTO word_2_object_table(id, word_id, keypoint_id) VALUES(";
	  for(std::multimap<int, int>::const_iterator it = words_.begin(); it != words_.end(); it ++)
	  {
	    printf( "word=%d des=%d \n", it->first, it->second);
		char data[50];
		memset(data,0,50);
		sprintf(data,"%d,%d,%d)",id_, it->first, it->second);
		std::string sql = insert + data;
    	UDEBUG ("%s \n", sql.c_str());
		if(mysql_query(connection, sql.c_str()))  
	    {  
	        cout << "Query Error:" << mysql_error(connection);  
	        return ;
	    }  
	    else  
	    {  
	        cout << "Query ok!" << sql<<endl;  
	    }  
	  }
	  UDEBUG("saveObject end .\n");
	}
#endif
#if 0
  int load(FILE *filePtr)
  {
    if(feof(filePtr))
    {
      UDEBUG("read data error 2 \n");
      return 1;
    }

		int nKpts;
    fscanf(filePtr,"%d %d ",&id_, &nKpts);
    if(feof(filePtr))
    {
      UDEBUG("read data error 1 \n", id_, nKpts);
      return 1;
    }
    //UDEBUG("id = %d   keypoints size = %d \n", id_, nKpts);
		keypoints_.resize(nKpts);
		for(int i=0;i<nKpts;++i)
		{
      fscanf(filePtr,"%f %d %d %f %f %f %f ", &keypoints_.at(i).angle, &keypoints_.at(i).class_id, &keypoints_[i].octave,
              &keypoints_.at(i).pt.x, &keypoints_.at(i).pt.y, &keypoints_.at(i).response, &keypoints_.at(i).size);
		}


		int rows,cols,type;
    int dataSize;
    fscanf(filePtr,"%d %d %d %d ",&rows, &cols, &type, &dataSize);
//    unsigned char data[dataSize];
//    fread(data, dataSize, 1, descriptorFile);
    UDEBUG("rows = %d  cols = %d datasize = %d  \n", rows, cols, dataSize);
//    descriptors_ = cv::Mat(rows, cols, type, data).clone();
    for(int i=0; i<rows; i++)
    {
      int first,second;
      fscanf(filePtr,"%d %d ",&first, &second);
      words_.insert(std::make_pair(first,second));
    }

    fscanf(filePtr," %d %d ",&rows, &cols);
    UDEBUG("image rows = %d  image cols = %d   \n", rows, cols);
    setrect(rows,cols);
    return 0;
	}
#else
  int load(FILE *filePtr)
  {
    if(feof(filePtr))
    {
      UDEBUG("read data error 2 \n");
      return 1;
    }

		int nKpts;
    //fscanf(filePtr,"%d %d ",&id_, &nKpts);
    fread(&id_,sizeof(id_),1,filePtr);
    fread(&nKpts,sizeof(nKpts),1,filePtr);
    printf("load id_=%d keypoint size=%d \n",id_,nKpts);
    if(feof(filePtr))
    {
      UDEBUG("read data error 1 \n", id_, nKpts);
      return 1;
    }
    //UDEBUG("id = %d   keypoints size = %d \n", id_, nKpts);
		keypoints_.resize(nKpts);
		for(int i=0;i<nKpts;++i)
		{
      /*fscanf(filePtr,"%f %d %d %f %f %f %f ", &keypoints_.at(i).angle, &keypoints_.at(i).class_id, &keypoints_[i].octave,
              &keypoints_.at(i).pt.x, &keypoints_.at(i).pt.y, &keypoints_.at(i).response, &keypoints_.at(i).size);*/
       fread(&(keypoints_.at(i)),sizeof(cv::KeyPoint),1,filePtr);
		}
		printf("load ptr=%d\n",ftell(filePtr));


		//int rows,cols,type;
    //int dataSize;
    //fscanf(filePtr,"%d %d %d %d ",&rows, &cols, &type, &dataSize);
    //fread(&rows,sizeof(rows),1,filePtr);
    //fread(&cols,sizeof(cols),1,filePtr);
    //fread(&type,sizeof(type),1,filePtr);
    //fread(&dataSize,sizeof(dataSize),1,filePtr);
    //unsigned char data[dataSize];
    //fread(data, dataSize, 1, filePtr);
    //UDEBUG("rows = %d  cols = %d datasize = %d  \n", rows, cols, dataSize);
    //descriptors_ = cv::Mat(rows, cols, type, data).clone();
    //int wordSize = 0;
    fread(&words_size,sizeof(words_size),1,filePtr);
    for(int i=0; i<words_size; i++)
    {
      int first,second;
      //fscanf(filePtr,"%d %d ",&first, &second);
      fread(&first,sizeof(first),1,filePtr);
      fread(&second,sizeof(second),1,filePtr);
      words_.insert(std::make_pair(first,second));
    }

    //fscanf(filePtr," %d %d ",&rows, &cols);
    //int rows=0,cols=0;
    fread(&image_rows,sizeof(image_rows),1,filePtr);
    fread(&image_cols,sizeof(image_cols),1,filePtr);
    UDEBUG("image rows = %d  image cols = %d   \n", image_rows, image_cols);
    setrect(image_rows,image_cols);
    return 0;
	}
#endif
  /*
   * object_table
   * [objectID,keypoint,imageRow,imageCol]
   * word_2_object_table
   * [objectID, wordID, keypointID]
   */
   #if 0
  int load(sqlite3 *db, int objectID)
  {
    int result = 0;
    sqlite3_stmt * stat = NULL;
    char arr[20];
    sprintf(arr, "%d ;", objectID);
    std::string select = "SELECT keypoints,imageRow,imageCol FROM object_table WHERE objectID = ";
    std::string sql =  select + arr;
    result = sqlite3_prepare(db,sql.c_str(),-1, &stat, 0);
    if(result != SQLITE_OK)
    {
      return 1;
    }
    while(1)
    {
      result = sqlite3_step(stat);
      if(SQLITE_ROW == result)
      {
        id_ = objectID;
        void *keypointData =  (void *)sqlite3_column_blob(stat,0);
        int keypointDataLen = sqlite3_column_bytes(stat,0);
        keypoints_.resize(keypointDataLen/sizeof(cv::KeyPoint));
        memcpy(&keypoints_[0],keypointData,keypointDataLen);
        int imageRow,imageCol;
        imageRow = sqlite3_column_int(stat,1);
        imageCol = sqlite3_column_int(stat,2);
        setrect(imageRow,imageCol);
      }
      else if(SQLITE_DONE == result)
      {
        break;
      }
    }

    result = sqlite3_reset(stat);
    if(result != SQLITE_OK)
    {
      return 1;
    }

    //select word_2_object
    std::string select1 = "SELECT wordID,keypointID FROM word_2_object_table WHERE objectID = ";
    sql = select1 + arr;
    result = sqlite3_prepare(db,sql.c_str(),-1, &stat, 0);
    if(result != SQLITE_OK)
    {
      return 1;
    }
    while(1)
    {
      result = sqlite3_step(stat);
      if(SQLITE_ROW == result)
      {
        int wordID,keypointsID;
        wordID = sqlite3_column_int(stat,0);
        keypointsID = sqlite3_column_int(stat,1);
        words_.insert(std::make_pair(wordID,keypointsID));
      }
      else if(SQLITE_DONE == result)
      {
        break;
      }
    }
    sqlite3_finalize(stat);

    return 0;
  }


  int load(MYSQL *connection, int objectID)
  {
    char arr[20];
    MYSQL_RES *result;
    MYSQL_ROW row;
    unsigned long *lengths;
    sprintf(arr, "%d ;", objectID);
    std::string select = "SELECT keypoints,rows,cols FROM object_table WHERE id = ";
    std::string sql =  select + arr;

    if(mysql_query(Setting::getMySQLDB(), sql.c_str()))  
    {  
      UDEBUG("mysql Query Error33:%d \n",Setting::getMySQLDB());
      return 1;
    }  

	result = mysql_store_result(Setting::getMySQLDB()); // 获取结果集  
	// mysql_field_count()返回connection查询的列数  
	for(int i=0; i < mysql_field_count(Setting::getMySQLDB()); ++i)  
	{  
	    // 获取下一行  
		lengths = mysql_fetch_lengths(result);
	    row = mysql_fetch_row(result);  
	    if(row <= 0)  
	    {  
	        break;  
	    }  
	    id_ = objectID;
        void *keypointData = (void *)row[0];
        int keypointDataLen =lengths[0];
        keypoints_.resize(keypointDataLen/sizeof(cv::KeyPoint));
        memcpy(&keypoints_[0],keypointData,keypointDataLen);
        int imageRow,imageCol;
        imageRow = atoi(row[1]);
        imageCol =atoi(row[2]);
        setrect(imageRow,imageCol);
	}  
	// 释放结果集的内存  
	mysql_free_result(result);  
	

    //select word_2_object
    std::string select1 = "SELECT word_id,keypoint_id FROM word_2_object_table WHERE id = ";
    sql = select1 + arr;
	if(mysql_query(Setting::getMySQLDB(), sql.c_str()))	
	{  
	  UDEBUG("mysql Query Error44:%d \n",Setting::getMySQLDB());
	  return 1;
	}
    result = mysql_store_result(Setting::getMySQLDB()); // 获取结果集  
    for(int i=0; i < mysql_field_count(Setting::getMySQLDB()); ++i)  
	{  
	    // 获取下一行  
		lengths = mysql_fetch_lengths(result);
	    row = mysql_fetch_row(result);  
	    if(row <= 0)  
	    {  
	        break;  
	    }  

        int wordID,keypointsID;
        wordID = atoi(row[0]);
        keypointsID = atoi(row[1]);
        words_.insert(std::make_pair(wordID,keypointsID));
	}  
	// 释放结果集的内存  
	mysql_free_result(result);  

    return 0;
  }
  #endif
private:
	int id_;
	int image_cols;
	int image_rows;
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
