#ifndef TRANINGBASE_H
#define TRANINGBASE_H
#include <vector>
#include <stack>
#include <opencv2/flann.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/ml/ml.hpp>
#include <opencv2/core/core.hpp>
#include <string>
#include <unistd.h>
#include <dirent.h>
#include <opencv2/text.hpp>
#include "ULogger.h"


namespace char_recognition {
#define DEBUG 1
//const std::string DATADIR = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	const std::string DATADIR = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefgjklpqrtuvy";
	const int lineCount[] = {2,3,1,2,1,1,1,1,1,1,1,1,1,1,2,2,2,2,1,1,1,1,1,1,1,1,2,3,1,2,1,1,1,1,1,1,2,2,2,1,1,1,1};
//const std::string DATADIR = "0123456789";
const int destImageWidth = 30;
const int destImageHeight = 40;

struct ImageData
{
	ImageData()
	:ID(0),lineCount(0){}
	int ID;
	cv::Mat srcImage;
	cv::Mat elaborationImage;
	int lineCount;
	int descriptor[9];
};

class Vocabulary
{
public:
  Vocabulary();
  void Training();//new change
	char search(cv::Mat &srcImage); //new add
	double getEntropy(const cv::Mat &srcImage);//熵值
	std::vector<int>  search(std::multimap<int,cv::Mat> vSrcimages);
	void search(cv::Mat &srcImage,const int &index, std::multimap<int,int> &resultMap); //KMeans
	std::vector<int> charRecognitionDetect(const cv::Mat &srcImage);//new change
	cv::Mat toBinary(const cv::Mat &srcImage,int threshold=110);
	//new add
	std::multimap<int,cv::Mat> getTargetArea(const cv::Mat &srcImage);
	cv::Mat elaborationImage_1(const cv::Mat &srcImage);//new change
	cv::Mat getConnectedDomain(const cv::Mat &srcImage);//获取图像中面积最大的连通域
	cv::Mat deleteNoise(const cv::Mat &srcImage);//去除零星的噪声点
	cv::Mat deleteEdge(const cv::Mat &srcImage);//去除边缘
	cv::Mat getDifferenceImage(const cv::Mat &image1, const cv::Mat &image2);
	int getLineDescriptor(const cv::Mat &srcImage);
	void getBlockDescriptoy(const cv::Mat &srcImage,int descriptor[]);//分为3X3
	void lineTransformImage(const cv::Mat &srcImage, cv::Mat &destImage);//线性变换
	
	int getConnectedDomainCount(const cv::Mat &srcImage);//获取图像背景的连通区域个数  即这个字母有几个闭环
	
	std::multimap<int,cv::Mat> getAllConnectedDomain(const cv::Mat &srcImage);//获取图像中all连通域
	void homoFilters(const cv::Mat &srcImage, cv::Mat &dst, double sigma = 3.0);//同态滤波
	
	void setCurrentWordLength(int Length) {m_CurrentWordLength = Length;}
private:
//		std::vector<ImageData> m_VocabularyData;//new add
	std::vector< std::vector<ImageData> > m_VocabularyDatas;
	int m_CurrentWordLength;
};
int floatToInt(double f);
} //end namespace
#endif // TRANINGBASE_H
