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
 // Q_OBJECT;
public:
  explicit FeatureExtractor();

  void createORBFeature2D();
  void createSIFTFeature2D();
  void createSURFFeature2D();


  void detect(const cv::Mat & image, std::vector<cv::KeyPoint> & keypoints, const cv::Mat & mask = cv::Mat());
  void compute(const cv::Mat & image, std::vector<cv::KeyPoint> & keypoints, cv::Mat & descriptors);
  void detectAndCompute(const cv::Mat &image, const cv::Mat &mask, std::vector<cv::KeyPoint> &keyPoints, cv::Mat & descriptors);

private:
  cv::Ptr<cv::Feature2D> m_feature2D_;
};

/*
 * 之前是一个特征提取线程  利用的QThread  但是改为C++ 之后 不能以相同的方式进行
 * so目前没有单独开一个线程
*/

class ExtractorThread
{
 // Q_OBJECT;
public:
  ExtractorThread(FeatureExtractor *feature, int objectID, const cv::Mat &image);
  ~ExtractorThread();
  int objectId() const {return m_objectId_;}
  const cv::Mat & image() const {return m_image_;}
  const std::vector<cv::KeyPoint> & keypoints() const {return m_keypoints_;}
  const cv::Mat & descriptors() const {return m_descriptors_;}
  void start()
  {
#ifdef THREAD
    thread t1(&ExtractorThread::run, this);
    t1.join();
#else
    this->run();
#endif
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
