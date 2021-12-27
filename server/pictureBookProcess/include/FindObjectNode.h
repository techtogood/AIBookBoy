#ifndef FINDOBJECTNODE_H
#define FINDOBJECTNODE_H

#include "../include/FeatureExtractor.h"
#include "../include/Vocabulary.h"
#include "../include/DetectionInfo.h"
#include "../include/ObjSignature.h"
#include "../include/Setting.h"
#include "../include/log.h"
#include <map>
#include <opencv2/opencv.hpp>
#include <vector>

#include <unistd.h>
#include <dirent.h>
#include <thread>
// #include "MyCameraNode.h"

namespace my_find_object {

class MyCameraNode{};

#define SHOW_IMAGE 1

class FindObjectNode
{
public:
  explicit FindObjectNode(MyCameraNode *cameraNode, FeatureExtractor *featureNode);
  FindObjectNode(FeatureExtractor *featureNode);
  ~FindObjectNode();

  double getEntropy(const cv::Mat &srcImage);

  //加载目录下面的所有图片作为一个词典
  int loadObjects(const string &dirPath, bool recursive = false);
  const ObjSignature * addObject(const string & fileName, const string &filePath);
  const ObjSignature * addObject(const cv::Mat & image, int id=0, const string & filePath = "");
  bool addObject(ObjSignature * obj); // take ownership when true is returned

  //
  void updateObjects(const vector<int> & ids = vector<int>());
  void updateVocabulary(const vector<int> & ids = vector<int>());

  //对图片进行检索  info为检索的结果
  void detect(const cv::Mat &image, DetectionInfo &info);
  int  detect(const cv::Mat &image, int retArr[]);
  void imageFeatureExtractor(const cv::Mat &image);

  void saveToOneFile(const string &FileName);
  void loadFromOneFile(const string &FileName);
private:
  MyCameraNode *m_cameraNode_;
  FeatureExtractor *m_featureNode_;
  std::map<int, ObjSignature*> m_objects_;
  Vocabulary *m_vocabulary_;
  std::map<int, cv::Mat> m_objectsDescriptors_;
  std::map<int, int> m_dataRange_;

#if SHOW_IMAGE
  std::vector<cv::KeyPoint> m_keyPoints_;
  cv::Mat m_descriptors_;
#endif

};


class HomographyThread
{
public:
  HomographyThread(
      const std::multimap<int, int> * matches, // <object, scene>
      int objectId,
      const std::vector<cv::KeyPoint> * kptsA,
      const std::vector<cv::KeyPoint> * kptsB) : // image only required if opticalFlow is on
        matches_(matches),
        objectId_(objectId),
        kptsA_(kptsA),
        kptsB_(kptsB),
        code_(DetectionInfo::kRejectedUndef),m_th(NULL)
  {
//    UASSERT(matches && kptsA && kptsB);
    //qDebug() << "HomographyThread:matches.size " << matches->size();
  }
  virtual ~HomographyThread()
  {
    if(NULL != m_th)
    {
      delete m_th;
    }
  }
  thread *m_th;
  int getObjectId() const {return objectId_;}
  const std::vector<int> & getIndexesA() const {return indexesA_;}
  const std::vector<int> & getIndexesB() const {return indexesB_;}
  const std::vector<uchar> & getOutlierMask() const {return outlierMask_;}
  std::multimap<int, int> getInliers() const {return inliers_;}
  std::multimap<int, int> getOutliers() const {return outliers_;}
  const cv::Mat & getHomography() const {return h_;}
  DetectionInfo::RejectedCode rejectedCode() const {return code_;}
  void start()
  {
#ifdef THREAD
//    m_th = new thread(&HomographyThread::run, this);

    this->run();
#else
    this->run();
#endif
  }
  void wait()
  {
    if(NULL != m_th)
    {
      m_th->join();
    }


  }
protected:
  virtual void run()
  {

    std::vector<cv::Point2f> mpts_1(matches_->size());
    std::vector<cv::Point2f> mpts_2(matches_->size());
    indexesA_.resize(matches_->size());
    indexesB_.resize(matches_->size());

    int j=0;
    for(std::multimap<int, int>::const_iterator iter = matches_->begin(); iter!=matches_->end(); ++iter)
    {
      mpts_1[j] = kptsA_->at(iter->first).pt;
      indexesA_[j] = iter->first;
      mpts_2[j] = kptsB_->at(iter->second).pt;
      indexesB_[j] = iter->second;
      ++j;
    }
    Setting::getTime();
    if((int)mpts_1.size() >=10)
    {

      h_ = findHomography(mpts_1,
          mpts_2,
          cv::RANSAC,
          3.0,
          outlierMask_);

      for(unsigned int k=0; k<mpts_1.size();++k)
      {
        if(outlierMask_.size() && outlierMask_.at(k))
        {
          inliers_.insert(std::make_pair(indexesA_[k], indexesB_[k]));
        }
        else
        {
          outliers_.insert(std::make_pair(indexesA_[k], indexesB_[k]));
        }
      }

      if(h_.empty())
      {
        h_ = cv::Mat();
        code_ = DetectionInfo::kRejectedAllInliers;
      }
      else if(inliers_.size() == (int)outlierMask_.size() && !h_.empty())
      {
        if(/*Settings::getHomography_ignoreWhenAllInliers() ||*/ cv::countNonZero(h_) < 1)
        {
          // ignore homography when all features are inliers
          h_ = cv::Mat();
          code_ = DetectionInfo::kRejectedAllInliers;
        }
      }
    }
    else
    {
      code_ = DetectionInfo::kRejectedLowMatches;
    }
    UDEBUG("%d findHomography ... using time(ms): %f \n",objectId_, Setting::getTime());

    //UINFO("Homography Object %d time=%d ms", objectIndex_, time.elapsed());
  }
private:
  const std::multimap<int, int> * matches_;
  int objectId_;
  const std::vector<cv::KeyPoint> * kptsA_;
  const std::vector<cv::KeyPoint> * kptsB_;
  DetectionInfo::RejectedCode code_;

  std::vector<int> indexesA_;
  std::vector<int> indexesB_;
  std::vector<uchar> outlierMask_;
  std::multimap<int, int> inliers_;
  std::multimap<int, int> outliers_;
  cv::Mat h_;
};


vector<string> getFiles(string cate_dir);
//QImage CvMat2QImage(const cv::Mat & image, bool isBgr = true);

}
#endif // FINDOBJECTNODE_H
