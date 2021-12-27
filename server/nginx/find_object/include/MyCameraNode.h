#ifndef MYCAMERANODE_H
#define MYCAMERANODE_H

#include <ros/ros.h>
#include <image_transport/image_transport.h>
#include <sensor_msgs/Image.h>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <cv_bridge/cv_bridge.h>
#include <iostream>
#include <vector>
#include <math.h>

#include "../include/Setting.h"



namespace my_find_object {

class FindObjectNode;

class MyCameraNode
{
  //Q_OBJECT;
public:
  explicit MyCameraNode();

  void imageRawTopicCallback(const sensor_msgs::ImageConstPtr &msg);

//Q_SIGNALS:
  void imageReceived(const cv::Mat &image);

  void setMyFindObjectNode(FindObjectNode *);
//private Q_SLOTS:
  void timeout();
private:
  cv::Mat getTransFrama(cv::Mat &image);


  //QTimer m_timer_;
  ros::Subscriber m_subHead_;
  bool m_correctionFlg_;

  cv::Mat m_tranframe;
  bool    m_tranframeFlg;

  double  m_outputImageWidth;
  double  m_outputImageHeight;
  FindObjectNode *m_findObjectNode;
};


}
#endif // MYCAMERANODE_H
