#ifndef FEATUREEXTRACTOR_H
#define FEATUREEXTRACTOR_H

#include <opencv2/features2d.hpp>

#include <thread>
#include "../include/Setting.h"

namespace my_find_object {

/*
 * 本类提供三种特征提取方法：ORB SIFT SURF， 根据Setting中的FeatureType确定
*/

class FeatureExtractor
{
public:
  explicit FeatureExtractor();
  FeatureExtractor(int featureType);
  void createORBFeature2D();
  void createSIFTFeature2D();
  void createSURFFeature2D();


  void detect(const cv::Mat & image, std::vector<cv::KeyPoint> & keypoints, const cv::Mat & mask = cv::Mat());
  void compute(const cv::Mat & image, std::vector<cv::KeyPoint> & keypoints, cv::Mat & descriptors);
  void detectAndCompute(const cv::Mat &image, const cv::Mat &mask, std::vector<cv::KeyPoint> &keyPoints, cv::Mat & descriptors);

  const int getFeatureType() const;
  ~FeatureExtractor();
private:
  cv::Ptr<cv::Feature2D> m_feature2D_;
  int m_featureType;
};

/*
 * 之前是一个特征提取线程  利用的QThread  但是改为C++ 之后 不能以相同的方式进行
 * so目前没有单独开一个线程
*/

class ExtractorThread
{
public:
  ExtractorThread(FeatureExtractor *feature, int objectID, const cv::Mat &image);
  ExtractorThread(FeatureExtractor *feature = new FeatureExtractor());
  ~ExtractorThread();
  int objectId() const {return m_objectId_;}
  const cv::Mat & image() const {return m_image_;}
  const std::vector<cv::KeyPoint> & keypoints() const {return m_keypoints_;}
  const cv::Mat & descriptors() const {return m_descriptors_;}

  void computerDescriptors(const cv::Mat &srcIMage);
  void start()
  {
    this->run();
  }
protected:
  virtual void run();
private:
  FeatureExtractor *m_featureExtractor_;
  int m_objectId_;
  cv::Mat m_image_;
  std::vector<cv::KeyPoint> m_keypoints_;
  cv::Mat m_descriptors_;
};
}
#endif // FEATUREEXTRACTOR_H
