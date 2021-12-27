#ifndef CENTENTDETECTION_H
#define CENTENTDETECTION_H

#include "Vocabulary.h"
#include "DetectionInfo.h"
#include "ObjSignature.h"
namespace my_find_object {


class ContentDetection
{
  /*先初始化一个空的内容检测类
   *在封面检测成功时，才会根据封面的ID调用ReloadDataFromID进行数据加载
   *并且在每次封面检测成功之后都会进行一次加载
  */
public:
  explicit ContentDetection();//初始化一个空的内容检测类
  void ReloadDataFromID(const int &ID);//根据传入的封面ID进行数据加载   对应的文件为：ID.bin
  void LoadFromFile(const string &FileName);//根据文件名加载数据

  int  detect(const cv::Mat &descriptors,const std::vector<cv::KeyPoint> keypoints);
private:
  std::map<int, ObjSignature*> m_objects_;
  Vocabulary *m_vocabulary_;
  int m_oldCoverID;
};
}
#endif // CENTENTDETECTION_H
