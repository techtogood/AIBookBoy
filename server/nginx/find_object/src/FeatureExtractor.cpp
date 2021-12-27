#include "../include/FeatureExtractor.h"
#include <opencv2/xfeatures2d.hpp>
#include "opencv2/core/ocl.hpp"

namespace my_find_object {

/*
 * 本类提供三种特征提取方法：ORB SIFT SURF， 根据Setting中的FeatureType确定
*/


FeatureExtractor::FeatureExtractor()
  : m_feature2D_(new cv::Feature2D)
{

  std::string featureType = Setting::getValue_string("FeatureType");

  
  cv::ocl::setUseOpenCL(true);
  printf("OpenCL Enabled: %u\n", true && cv::ocl::haveOpenCL());
  UDEBUG("featureType = %s \n",featureType.data());
  if("ORB" == featureType)
  {
    createORBFeature2D();
  }
  else if("SIFT" == featureType)
  {
    createSIFTFeature2D();
  }
  else if("SURF" == featureType)
  {
    createSURFFeature2D();
  }
}

void FeatureExtractor::createORBFeature2D()
{
  UDEBUG("createORBFeature2D. \n");
#if CV_MAJOR_VERSION < 3
  m_feature2D_ = cv::Ptr<cv::Feature2D> (new cv::ORB(Setting::getValue_int("ORB/nFeatures"),
                             Setting::getValue_float("ORB/scaleFactor"),
                             Setting::getValue_int("ORB/nLevel"),
                             Setting::getValue_int("ORB/edgeThreshold"),
                             Setting::getValue_int("ORB/firstLevel"),
                             Setting::getValue_int("ORB/WTA_K"),
                             Setting::getValue_int("ORB/scoreType"),
                             Setting::getValue_int("ORB/pathSize") ) );
#else
	m_feature2D_ = cv::Ptr<cv::Feature2D> (cv::ORB::create(Setting::getValue_int("ORB/nFeatures"),
	                               Setting::getValue_float("ORB/scaleFactor"),
	                               Setting::getValue_int("ORB/nLevel"),
	                               Setting::getValue_int("ORB/edgeThreshold"),
	                               Setting::getValue_int("ORB/firstLevel"),
	                               Setting::getValue_int("ORB/WTA_K"),
	                               Setting::getValue_int("ORB/scoreType"),
	                               Setting::getValue_int("ORB/pathSize")) );
#endif
}

void FeatureExtractor::createSIFTFeature2D()
{
  UDEBUG("createSIFTFeature2D. \n");
#if CV_MAJOR_VERSION < 3
  m_feature2D_ = cv::Ptr<cv::Feature2D> (new cv::SIFT(Setting::getValue_int("SIFT/nFeatures"),
                                                      Setting::getValue_int("SIFT/nOctaveLayers"),
                                                      Setting::getValue_double("SIFT/contrastThreshold"),
                                                      Setting::getValue_double("SIFT/edgeThreshold"),
                                                      Setting::getValue_double("SIFT/sigma")));
#else
  m_feature2D_ = cv::Ptr<cv::Feature2D> (cv::xfeatures2d::SIFT::create(Setting::getValue_int("SIFT/nFeatures"),
	                                             Setting::getValue_int("SIFT/nOctaveLayers"),
	                                             Setting::getValue_double("SIFT/contrastThreshold"),
	                                             Setting::getValue_double("SIFT/edgeThreshold"),
	                                             Setting::getValue_double("SIFT/sigma")));
#endif
}

void FeatureExtractor::createSURFFeature2D()
{
  UDEBUG("createSURFFeature2D. \n");
#if CV_MAJOR_VERSION < 3
  m_feature2D_ = cv::Ptr<cv::Feature2D> ( new cv::SURF(Setting::getValue_double("SURF/hessianThreshold"),
                              Setting::getValue_int("SURF/nOctaves"),
                              Setting::getValue_int("SURF/nOctaveLayers"),
                              false,
                              false) );
#else
	m_feature2D_ = cv::Ptr<cv::Feature2D> ( cv::xfeatures2d::SURF::create(Setting::getValue_double("SURF/hessianThreshold"),
	                                                     Setting::getValue_int("SURF/nOctaves"),
	                                                     Setting::getValue_int("SURF/nOctaveLayers"),
                                                      	 false,
	                                                     false) );
#endif
}

void FeatureExtractor::detect(const cv::Mat &image, std::vector<cv::KeyPoint> &keypoints, const cv::Mat &mask)
{
  if(NULL != m_feature2D_)
  {
    m_feature2D_->detect(image, keypoints, mask);
  }
}

void FeatureExtractor::compute(const cv::Mat &image, std::vector<cv::KeyPoint> &keypoints, cv::Mat &descriptors)
{
  if(NULL != m_feature2D_)
  {
    m_feature2D_->compute(image, keypoints, descriptors);
  }
}

void FeatureExtractor::detectAndCompute(const cv::Mat &image, const cv::Mat &mask, std::vector<cv::KeyPoint> &keyPoints, cv::Mat &descriptors)
{
  if(!m_feature2D_.empty())
  {
#if CV_MAJOR_VERSION < 3
    (*m_feature2D_)(image, mask, keyPoints, descriptors);
#else
	  m_feature2D_->detectAndCompute(image, mask, keyPoints, descriptors);
#endif
  }
}


/*
 * 之前是一个特征提取线程  利用的QThread  但是改为C++ 之后 不能以相同的方式进行
 * so目前没有单独开一个线程
*/




ExtractorThread::ExtractorThread(FeatureExtractor *feature, int objectID, const cv::Mat &image)
  :m_featureExtractor_(feature), m_objectId_(objectID),m_image_(image)
{

}

ExtractorThread::~ExtractorThread()
{

}

void ExtractorThread::run()
{
#if 1
  m_featureExtractor_->detectAndCompute(m_image_,cv::Mat(),m_keypoints_, m_descriptors_);
#else
  cv::Mat Mat;
  thread t1(&FeatureExtractor::detectAndCompute,
            m_featureExtractor_, std::ref(m_image_),std::ref(Mat),std::ref(m_keypoints_), std::ref(m_descriptors_));
  t1.join();
#endif
}


}
