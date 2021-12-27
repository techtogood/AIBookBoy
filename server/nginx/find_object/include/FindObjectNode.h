#ifndef FINDOBJECTNODE_H
#define FINDOBJECTNODE_H

//#include "../include/MyCameraNode.h"
#include "../include/FeatureExtractor.h"
#include "../include/Vocabulary.h"
#include "../include/DetectionInfo.h"
#include "../include/ObjSignature.h"
#include <map>
#include <opencv2/opencv.hpp>
#include <vector>

#include <unistd.h>
#include <dirent.h>
#include <thread>


namespace my_find_object {

#define SHOW_IMAGE 1

class FindObjectNode
{
  //Q_OBJECT
public:
  explicit FindObjectNode(/*MyCameraNode *cameraNode,*/ FeatureExtractor *featureNode);


  //加载目录下面的所有图片作为一个词典


  int loadObjects(const string &dirPath, bool recursive = false);
  const ObjSignature * addObject(const string & fileName);
  const vector<const ObjSignature *> addObject_1(const string & fileName);//将图片分为4份
  const ObjSignature *addObject_2(const string &fileName);
  const ObjSignature * addObject(const cv::Mat & image, int id=0, const string & filePath = "");
  bool addObject(ObjSignature * obj); // take ownership when true is returned

  //
  void updateObjects(const vector<int> & ids = vector<int>());
  void updateVocabulary(const vector<int> & ids = vector<int>());
  void updateVocabularyFromFile(const vector<int> & ids = vector<int>());
  void addObjectAndUpdate(const string &fileName,const string &filePath);

  //对图片进行检索  info为检索的结果
  #if 0
  void detect(const cv::Mat &image, DetectionInfo &info);
  #else
  int detect(const cv::Mat &image);
  #endif
  int  detect(const cv::Mat &image, int retArr[]);
  void imageFeatureExtractor(const cv::Mat &image);

  //void saveToFile();
  void saveToOneFile();
  //void loadFromFile();
  //void saveToDB();
  //void loadFromDB();
  //void saveToMySQL();
  //void loadFromMySOLDB();
  void loadFromOneFile();
private:
  //MyCameraNode *m_cameraNode_;
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
      const std::vector<cv::KeyPoint> * kptsB,
      const cv::Mat & imageA,   // image only required if opticalFlow is on
      const cv::Mat & imageB) : // image only required if opticalFlow is on
        matches_(matches),
        objectId_(objectId),
        kptsA_(kptsA),
        kptsB_(kptsB),
        imageA_(imageA),
        imageB_(imageB),
        code_(DetectionInfo::kRejectedUndef)
  {
//    UASSERT(matches && kptsA && kptsB);
    //qDebug() << "HomographyThread:matches.size " << matches->size();
  }
  virtual ~HomographyThread() {}

  int getObjectId() const {return objectId_;}
  const std::vector<int> & getIndexesA() const {return indexesA_;}
  const std::vector<int> & getIndexesB() const {return indexesB_;}
  const std::vector<uchar> & getOutlierMask() const {return outlierMask_;}
  std::multimap<int, int> getInliers() const {return inliers_;}
  std::multimap<int, int> getOutliers() const {return outliers_;}
  const cv::Mat & getHomography() const {return h_;}
  DetectionInfo::RejectedCode rejectedCode() const {return code_;}
  const int getMatchCounts(){ return matches_->size();}
  void start()
  {
#ifdef THREAD
    thread t1(&HomographyThread::run, this);
    t1.join();
#else
    this->run();
#endif
  }
protected:
  virtual void run()
  {
    //QTime time;
    //time.start();

    std::vector<cv::Point2f> mpts_1(matches_->size());
    std::vector<cv::Point2f> mpts_2(matches_->size());
    indexesA_.resize(matches_->size());
    indexesB_.resize(matches_->size());

    //qDebug("Fill matches...");
    int j=0;
    for(std::multimap<int, int>::const_iterator iter = matches_->begin(); iter!=matches_->end(); ++iter)
    {
//      UASSERT_MSG(iter.key() < (int)kptsA_->size(), uFormat("key=%d size=%d", iter.key(),(int)kptsA_->size()).c_str());
//      UASSERT_MSG(iter.value() < (int)kptsB_->size(), uFormat("key=%d size=%d", iter.value(),(int)kptsB_->size()).c_str());
      mpts_1[j] = kptsA_->at(iter->first).pt;
      indexesA_[j] = iter->first;
      mpts_2[j] = kptsB_->at(iter->second).pt;
      indexesB_[j] = iter->second;
      ++j;
    }
	  //printf("size======%d\n",(int)mpts_1.size() );

    if((int)mpts_1.size() >=20 /*Settings::getHomography_minimumInliers()*/)
    {
      if(false/*Settings::getHomography_opticalFlow()*/)
      {
        cv::Mat imageA = imageA_;
        cv::Mat imageB = imageB_;
        if(imageA_.cols < imageB_.cols && imageA_.rows < imageB_.rows)
        {
          // padding, optical flow wants images of the same size
          imageA = cv::Mat::zeros(imageB_.size(), imageA_.type());
          imageA_.copyTo(imageA(cv::Rect(0,0,imageA_.cols, imageA_.rows)));
        }
        if(imageA.size() == imageB.size())
        {
          //qDebug("Optical flow...");
          //refine matches
          std::vector<unsigned char> status;
          std::vector<float> err;
          cv::calcOpticalFlowPyrLK(
              imageA,
              imageB_,
              mpts_1,
              mpts_2,
              status,
              err,
             /* cv::Size(Settings::getHomography_opticalFlowWinSize(), Settings::getHomography_opticalFlowWinSize())*/cv::Size(16,16),
              /*Settings::getHomography_opticalFlowMaxLevel()*/3,
              cv::TermCriteria(cv::TermCriteria::COUNT+cv::TermCriteria::EPS,
              /*Settings::getHomography_opticalFlowIterations()*/30,
              /*Settings::getHomography_opticalFlowEps()*/0.01),
              cv::OPTFLOW_LK_GET_MIN_EIGENVALS | cv::OPTFLOW_USE_INITIAL_FLOW, 1e-4);
        }
        else
        {
          UDEBUG("Object's image should be less/equal size of the scene image to use Optical Flow.\n");
        }
      }

    // qDebug() << "Find homography... begin" << objectId_;

      h_ = findHomography(mpts_1,
          mpts_2,
          /*Settings::getHomographyMethod()*/cv::RANSAC,
          /*Settings::getHomography_ransacReprojThr()*/3.0,
          outlierMask_);

    //  qDebug() << "Find homography... end" << objectId_;

//      qDebug(outlierMask_.size() == 0 || outlierMask_.size() == mpts_1.size());
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

    //UINFO("Homography Object %d time=%d ms", objectIndex_, time.elapsed());
  }
private:
  const std::multimap<int, int> * matches_;
  int objectId_;
  const std::vector<cv::KeyPoint> * kptsA_;
  const std::vector<cv::KeyPoint> * kptsB_;
  cv::Mat imageA_;
  cv::Mat imageB_;
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
