#include "../include/MyCameraNode.h"
#include "../include/FindObjectNode.h"

namespace my_find_object {

MyCameraNode::MyCameraNode()
{
  ros::NodeHandle nh;
  m_correctionFlg_ = my_find_object::Setting::getValue_bool("ImageCorrection");
  //std::string topicName = my_find_object::Setting::getValue_string("ImageRecvTopic");
  //m_subHead_ = nh.subscribe(topicName,1, &MyCameraNode::imageRawTopicCallback, this) ;
  int rate = my_find_object::Setting::getValue_int("Rate");
  //m_timer_.setInterval(1000/rate);
  //connect(&m_timer_, SIGNAL(timeout()), this, SLOT(timeout()));

  //m_timer_.start();

  m_tranframe = cv::Mat::zeros(1,1,CV_64F);
  m_tranframeFlg = false;
  m_outputImageWidth = 640;
  m_outputImageHeight = 480;
  //publish topic
  //m_sub = nh.advertise<sensor_msgs::Image>("image_correction", 1);
}

cv::Mat MyCameraNode::getTransFrama(cv::Mat &image)
{
  cv::Point2f  arr_srcPoints[4], arr_destPoints[4];
  #if AUTO
    vector <cv::Point> v_srcPoints;
    v_srcPoints = my_findContours(image);
    if(4 != v_srcPoints.size())
    {
      return cv::Mat(1,1,CV_8U);
    }
    arr_srcPoints[0] = v_srcPoints[0];
    arr_srcPoints[1] = v_srcPoints[1];
    arr_srcPoints[2] = v_srcPoints[2];
    arr_srcPoints[3] = v_srcPoints[3];
  #else
    arr_srcPoints[0].x = 100; //175
    arr_srcPoints[0].y = 0;  //56
    arr_srcPoints[1].x = 0;  //22
    arr_srcPoints[1].y = 250; //281
    arr_srcPoints[2].x = 480; //606
    arr_srcPoints[2].y = 250; //272
    arr_srcPoints[3].x = 430; //470
    arr_srcPoints[4].y = 0;  //46
  #endif
    arr_destPoints[0].x = (0+0); //200
    arr_destPoints[0].y = (0+0); //200

    arr_destPoints[1].x = (0+0); //200
    arr_destPoints[1].y = (0+400); //600

    arr_destPoints[2].x = (0+400); //482
    arr_destPoints[2].y = (0+400); //600

    arr_destPoints[3].x = (0+400); //482
    arr_destPoints[4].y = (0+0); //200
    return cv::getPerspectiveTransform(arr_srcPoints, arr_destPoints);
}

void MyCameraNode::imageRawTopicCallback(const sensor_msgs::ImageConstPtr &msg)
{
  cv::Mat image;
  if(msg->data.size())
  {
    cv_bridge::CvImageConstPtr ptr = cv_bridge::toCvShare(msg);
    if(msg->encoding.compare(sensor_msgs::image_encodings::BGR8) == 0)
    {
      image = ptr->image.clone();
    }
    else if(msg->encoding.compare(sensor_msgs::image_encodings::RGB8) == 0)
    {
      cv::cvtColor(ptr->image, image, cv::COLOR_RGB2BGR);
    }
  }
  else
  {
    ROS_INFO("chatterCallback error");
    return ;
  }

  if(image.size)
  {
    cv::imshow("src", image);
    if(m_correctionFlg_)
    {
      if(!m_tranframeFlg)
      {
        m_tranframe = getTransFrama(image);
        if(m_tranframe.rows < 3 || m_tranframe.cols < 3)
        {
          //...
        }
        else
        {
          std::vector<cv::Point2f> srcPoints, destPoints;
          srcPoints.resize(3);
          destPoints.resize(3);

          srcPoints[0].x = image.cols;
          srcPoints[0].y = image.rows;

          srcPoints[1].x = 0;
          srcPoints[1].y = image.rows;
          srcPoints[2].x = image.cols;
          srcPoints[2].y = 0;
          cv::perspectiveTransform(srcPoints, destPoints, m_tranframe);

          m_outputImageWidth = std::max(destPoints[0].x, std::max(destPoints[1].x, destPoints[2].x));
          m_outputImageHeight = std::max(destPoints[0].y, std::max(destPoints[1].y, destPoints[2].y));

          m_tranframeFlg = true;
        }
      }
      cv::warpPerspective(image,image,m_tranframe,cv::Size(m_outputImageWidth,m_outputImageHeight));
    }
    m_findObjectNode->imageFeatureExtractor(image);
  }
}

void MyCameraNode::setMyFindObjectNode(FindObjectNode *node)
{
  m_findObjectNode = node;
}

void MyCameraNode::timeout()
{
  ros::spinOnce();
}

}
