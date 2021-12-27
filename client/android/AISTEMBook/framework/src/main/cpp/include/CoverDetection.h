#ifndef COVERDETECTION_H
#define COVERDETECTION_H

#include <dirent.h>
#include "Vocabulary.h"
#include "DetectionInfo.h"
#include "ObjSignature.h"
#include "FindObjectNode.h"

extern int my_feature_type;

namespace my_find_object {


class CoverDetection
{

public:
  explicit CoverDetection();//初始化一个空的内容检测类
  void clear();//清空内存
  void setFeatureType( int type);
  int getFeatureType();
  int loadFromFile(const string &FileName);//根据文件名加载数据
  void saveToFile(const string &FileName);
	void updateObjects(const vector<int> &ids);
	void updateVocabulary(const vector<int> &ids);

	int loadObjects(const std::string &dirPath);
	bool addObject(ObjSignature *obj);
	const ObjSignature *addObject(const cv::Mat &image, int id, const string &filePath);
	const ObjSignature *addObject(const string &fileName,const string &filePath);
	void addObjectAndUpdate(const string &fileName,const string &filePath);
  int  detect(const cv::Mat &descriptors,const std::vector<cv::KeyPoint> keypoints);
private:
  std::map<int, ObjSignature*> m_objects_;
  Vocabulary *m_vocabulary_;
  FeatureExtractor *m_featureExtractor_surf;
  FeatureExtractor *m_featureExtractor_orb;
  FeatureExtractor *m_featureExtractor_sift;
  int m_feature_type_;
};

  class Crc32
  {
    public:
    Crc32() {
      unsigned int c;     
      unsigned int i, j;    
      for (i = 0; i < 256; i++) 
      {        
        c = (unsigned int)i;   
        for (j = 0; j < 8; j++) {  
         if (c & 1)                
           c = 0xedb88320L ^ (c >> 1);         
         else                
           c = c >> 1;         
        }          
        crc_table[i] = c;     
      }  
    }

    unsigned int getCrc32(unsigned int crc,unsigned char *buffer, unsigned int size)  {    
      unsigned int i;     
      for (i = 0; i < size; i++) {  
        crc = crc_table[(crc ^ buffer[i]) & 0xff] ^ (crc >> 8);    
      }   
      return crc ; 
    } 

    int addCrcToDataFile(FILE* fp) {    
    
      unsigned char crcbuf[1024];
      unsigned int rdlen;
      unsigned int crc = 0xffffffff;
      
      if( fp == NULL ){
        return 1;
      }

      fseek(fp,0L,SEEK_SET);

	    while((rdlen = fread(crcbuf, sizeof(unsigned char), 1024, fp)) > 0){
		    crc = getCrc32(crc, crcbuf, rdlen);
		  }
		  UDEBUG("addCrcToDataFile crc: 0x%x.\n",crc);
		  fwrite(&crc, sizeof(crc), 1, fp);
		  return 0;

    } 

    bool isVaildDataFileByCrc(FILE* fp) {    
      unsigned int save_crc =0, calc_crc=0;
  
      fseek(fp,0L,SEEK_END); /* 定位到文件末尾 */

      int flen=ftell(fp)-4; /* 得到文件大小 ,后4字节是CRC校验码 */
      unsigned char* buf = (unsigned char *)malloc(flen+1); /* 根据文件大小动态分配内存空间 */
      if(buf == NULL){
        return false;
      }
      else{
        fseek(fp,0L,SEEK_SET);
        fread(buf,flen,1,fp);
        buf[flen] = '\0';
        calc_crc = getCrc32(0xFFFFFFFF,buf,flen);
        fseek(fp,0L,flen-1); /* 定位到文件到时 */
        fread(&save_crc,sizeof(save_crc),1,fp);
        free(buf);

        UDEBUG("save_crc: 0x%x. calc_crc:0x%x\n",save_crc,calc_crc);
        if( save_crc == calc_crc ){
          return true;
        }else{
          return false;
        }
      }
    } 

    unsigned int crc_table[256];
  };


}
#endif // CENTENTDETECTION_H
