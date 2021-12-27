/**
 * @file /src/main.cpp
 *
 * @brief Qt based gui.
 *
 * @date November 2010
 **/
/*****************************************************************************
** Includes
*****************************************************************************/

#include "../include/FindObjectNode.h"
/*****************************************************************************
** Main
*****************************************************************************/
using namespace my_find_object;


int main(int argc, char **argv)
{

  //初始化ROS
  ros::init(argc,argv, "my_find_object");

  //初始化Setting
  my_find_object::Setting::init();

  //一个ros节点  用来接收相机发布出来的话题数据，该类中包含一个findObjectNode指针，用于接收到图片后进行处理
  MyCameraNode *cameraNode = new MyCameraNode();

  //特征提取类
  FeatureExtractor *featureNode = new FeatureExtractor();

  //主处理类  处理图片和词典
  FindObjectNode *findObject = new FindObjectNode(cameraNode, featureNode);
  ros::spin();
  return 0;
}
