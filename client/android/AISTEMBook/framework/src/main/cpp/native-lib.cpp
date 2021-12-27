#include <jni.h>
#include <map>  
#include <list>
#include <algorithm> 
#include <android/bitmap.h>
#include <opencv2/core/core.hpp>
#include "include/FindObjectNode.h"
#include "include/ContentDetection.h"
#include "include/CoverDetection.h"
#include "include/ObjSignature.h"
#include "include/Vocabulary.h"
#include "include/Setting.h"
#include "include/ULogger.h"
#include "include/charRecognition.h"
#include "ImagePreprocessor.h"

extern int CharRecog();
using namespace std;
using namespace cv;
using namespace my_find_object;
using namespace char_recognition;

//字母识别算法部分
char_recognition::Vocabulary *charRecognitionVocabulary = NULL;

my_find_object::ContentDetection *contentDetectionNode = NULL; //内容查找
my_find_object::CoverDetection *coverDetectionNode = NULL;//封面查找
my_find_object::ExtractorThread *featureExtractor_surf = NULL;//特征提取
my_find_object::ExtractorThread *featureExtractor_surf_2 = NULL;//特征提取
my_find_object::ExtractorThread *featureExtractor_orb = NULL;//特征提取
my_find_object::ExtractorThread *featureExtractor_sift = NULL;//特征提取
my_find_object::FindObjectNode *findObjectNode = NULL;//封面更新
my_find_object::Crc32 *crc32object = NULL;//crc校验类对象；
int my_feature_type = FEATURE_SURF; 

cv::Mat transfromMat_picture;
cv::Mat transfromMat;
Mat mapx;
Mat mapy;
ImagePreprocessor imagePreprocessor;
extern "C"
{
JNIEXPORT jint JNICALL Java_ai_aistem_xbot_framework_application_LetterRecognitionAPP_CharRecognitionInit(JNIEnv *env, jobject instance) {
	

	cv::Point2f srcPoints[4];
	srcPoints[0].x = 720*0.000;
	srcPoints[0].y = 480*0.335;
	srcPoints[1].x = 720*1.000;
	srcPoints[1].y = 480*0.385;
	srcPoints[2].x = 720*0.880;
	srcPoints[2].y = 480*0.640;
	srcPoints[3].x = 720*0.105;
	srcPoints[3].y = 480*0.590;

//  src_mat.setTo({0,0,800*0.060,600*0.384,800*0.990,600*0.384,800*0.920,600*0.553,800*0.130,600*0.553});
	cv::Point2f destPoints[4];
	destPoints[0].x = 720*0.060;
	destPoints[0].y = 480*0.335;
	destPoints[1].x = 720*0.960;
	destPoints[1].y = 480*0.335;
	destPoints[2].x = 720*0.960;
	destPoints[2].y = 480*0.690;
	destPoints[3].x = 720*0.060;
	destPoints[3].y = 480*0.690;
	transfromMat = cv::getPerspectiveTransform(srcPoints,destPoints);
	
	return 0;
}

JNIEXPORT jbyteArray JNICALL Java_ai_aistem_xbot_framework_application_LetterRecognitionAPP_getTargetArea(JNIEnv *env, jobject instance,long mat) {
	
	int colorType = CV_32S;
	Mat imgData = (*((Mat*)mat));
	Mat out;
	/*
	cv::Mat Guass_image;
	imgData.copyTo(Guass_image);

#if 0
	//分成5段进行二值化
//	int offset1 = 80;
//	int offset2 = 220;
//	int offset3 = 430;
//	int offset4 = 560;
	int offset1 = 130;
	int offset2 = 260;
	int offset3 = 390;
	int offset4 = 520;
	
	UDEBUG("Guass_image.rows = %d\n",Guass_image.rows);
	cv::Mat image1 = Guass_image.colRange(0,offset1);
	cv::Mat image2 = Guass_image.colRange(offset1,offset2);
	cv::Mat image3 = Guass_image.colRange(offset2,offset3);
	cv::Mat image4 = Guass_image.colRange(offset3,offset4);
	cv::Mat image5 = Guass_image.colRange(offset4,Guass_image.cols);
	

	
	cv::GaussianBlur(image1,image1,cv::Size(3, 3), 0, 0);
	cv::GaussianBlur(image2,image2,cv::Size(3, 3), 0, 0);
	cv::GaussianBlur(image3,image3,cv::Size(3, 3), 0, 0);
	cv::GaussianBlur(image4,image4,cv::Size(3, 3), 0, 0);
	cv::GaussianBlur(image5,image5,cv::Size(3, 3), 0, 0);
	
	cv::threshold(image1,image1,252,255,cv::THRESH_OTSU);
	cv::threshold(image2,image2,252,255,cv::THRESH_OTSU);
	cv::threshold(image3,image3,254,255,cv::THRESH_OTSU);
	cv::threshold(image4,image4,252,255,cv::THRESH_OTSU);
	cv::threshold(image5,image5,252,255,cv::THRESH_OTSU);
	
	
//	cv::threshold(image1,image1,252,255,cv::THRESH_BINARY_INV);
//	cv::threshold(image2,image2,252,255,cv::THRESH_BINARY_INV);
//	cv::threshold(image3,image3,254,255,cv::THRESH_BINARY_INV);
//	cv::threshold(image4,image4,252,255,cv::THRESH_BINARY_INV);
//	cv::threshold(image5,image5,252,255,cv::THRESH_BINARY_INV);
	
	for(int row=0;row<Guass_image.rows; row++)
	{
		for(int col=0; col<Guass_image.cols; col++)
		{
			if(col < offset1)
			{
				Guass_image.at<uchar>(row,col) = image1.at<uchar>(row,col);
			}
			else if(col < offset2)
			{
				Guass_image.at<uchar>(row,col) = image2.at<uchar>(row,col-offset1);
			}
			else if(col < offset3)
			{
				Guass_image.at<uchar>(row,col) = image3.at<uchar>(row,col-offset2);
			}
			else if(col < offset4)
			{
				Guass_image.at<uchar>(row,col) = image4.at<uchar>(row,col-offset3);
			}
			else
			{
				Guass_image.at<uchar>(row,col) = image5.at<uchar>(row,col-offset4);
			}
		}
	}
#else
	int offset1 = 80;
	int offset2 = 220;
	int offset3 = 430;
	int offset4 = 560;
	
	UDEBUG("Guass_image.rows = %d\n",Guass_image.rows);
	cv::Mat image1 = Guass_image.colRange(0,offset1);
	cv::Mat image2 = Guass_image.colRange(offset1,offset2);
	cv::Mat image3 = Guass_image.colRange(offset2,offset3);
	cv::Mat image4 = Guass_image.colRange(offset3,offset4);
	cv::Mat image5 = Guass_image.colRange(offset4,Guass_image.cols);
	

	
	cv::GaussianBlur(image1,image1,cv::Size(3, 3), 0, 0);
	cv::GaussianBlur(image2,image2,cv::Size(3, 3), 0, 0);
	cv::GaussianBlur(image3,image3,cv::Size(3, 3), 0, 0);
	cv::GaussianBlur(image4,image4,cv::Size(3, 3), 0, 0);
	cv::GaussianBlur(image5,image5,cv::Size(3, 3), 0, 0);
	
	cv::threshold(image1,image1,252,255,cv::THRESH_TRIANGLE);
	cv::threshold(image2,image2,252,255,cv::THRESH_TRIANGLE);
	cv::threshold(image3,image3,254,255,cv::THRESH_TRIANGLE);
	cv::threshold(image4,image4,252,255,cv::THRESH_TRIANGLE);
	cv::threshold(image5,image5,252,255,cv::THRESH_TRIANGLE);
	
	for(int row=0;row<Guass_image.rows; row++)
	{
		for(int col=0; col<Guass_image.cols; col++)
		{
			if(col < offset1)
			{
				Guass_image.at<uchar>(row,col) = image1.at<uchar>(row,col);
			}
			else if(col < offset2)
			{
				Guass_image.at<uchar>(row,col) = image2.at<uchar>(row,col-offset1);
			}
			else if(col < offset3)
			{
				Guass_image.at<uchar>(row,col) = image3.at<uchar>(row,col-offset2);
			}
			else if(col < offset4)
			{
				Guass_image.at<uchar>(row,col) = image4.at<uchar>(row,col-offset3);
			}
			else
			{
				Guass_image.at<uchar>(row,col) = image5.at<uchar>(row,col-offset4);
			}
		}
	}
	cv::threshold(Guass_image,Guass_image,128,255,cv::THRESH_BINARY_INV);
	
#endif
	
	Guass_image.copyTo(out);
	 */

	
	jbyte  *outCharImage = new jbyte[out.cols * out.rows];
	for (int i = 0; i < out.cols * out.rows; i++) {
		outCharImage[i] = out.data[i];
	}
//	memcpy(outCharImage,out.data,out.cols * out.rows);

	jbyteArray result = env->NewByteArray(out.cols * out.rows);
	env->SetByteArrayRegion(result, 0, out.cols * out.rows, outCharImage);
	delete []outCharImage;
	return result;
}





//绘本阅读功能初始化
JNIEXPORT jint JNICALL Java_ai_aistem_xbot_framework_application_PictureBookAPP_FindObjectInit(JNIEnv *env, jobject instance) {

    int feature_type_bak = my_feature_type;
	Setting::init("/sdcard/data/book/my_find_object.ini");

	contentDetectionNode = new ContentDetection();

    coverDetectionNode = new CoverDetection();
    
    crc32object = new Crc32();
  
	//featureExtractor = new ExtractorThread();
	
	my_feature_type = FEATURE_SIFT;
	featureExtractor_sift = new ExtractorThread();

	my_feature_type = FEATURE_ORB;
	featureExtractor_orb = new ExtractorThread();

	my_feature_type = FEATURE_SURF;
	featureExtractor_surf = new ExtractorThread();
    featureExtractor_surf_2 = new ExtractorThread();

	my_feature_type = feature_type_bak;
	
	//
//	src_mat.put(0, 0, 720 * 0.000, 480 * 0.000, 720 * 1.000, 480 * 0.000, 720 * 0.750, 480 * 0.800, 720 * 0.250, 480 * 0.800);
//	dst_mat.put(0, 0, 720 * 0.500, 480 * 0.000, 720 * 1.500, 480 * 0.000, 720 * 1.500, 480 * 1.500, 720 * 0.500, 480 * 1.500);
	cv::Point2f srcPoints[4];
	srcPoints[0].x = 720*0.336;
	srcPoints[0].y = 480*0.100;
	srcPoints[1].x = 720*0.712;
	srcPoints[1].y = 480*0.100;
	srcPoints[2].x = 720*0.950;
	srcPoints[2].y = 480*0.950;
	srcPoints[3].x = 720*0.050;
	srcPoints[3].y = 480*0.950;

//  src_mat.setTo({0,0,800*0.060,600*0.384,800*0.990,600*0.384,800*0.920,600*0.553,800*0.130,600*0.553});
	cv::Point2f destPoints[4];
	destPoints[0].x = 720*0.65;
	destPoints[0].y = 480*0.35;
	destPoints[1].x = 720*1.35;
	destPoints[1].y = 480*0.35;
	destPoints[2].x = 720*1.35;
	destPoints[2].y = 480*1.75;
	destPoints[3].x = 720*0.65;
	destPoints[3].y = 480*1.75;
	transfromMat_picture = cv::getPerspectiveTransform(srcPoints,destPoints);
	
	float matrix[3][3] = {{406.323745512215, 0, 338.9442334758614}, {0, 406.3633461393865, 243.4507394745308}, {0, 0, 1}};
	float coeffs[5] = {-0.389479193611759, 0.1700349812579111, -0.0003436393557538629, -0.0001742451702881482, -0.03786321813092338};
	Mat cameraMatrix=Mat(3,3,CV_32FC1,matrix); /* 摄像机内参数矩阵 */
	Mat distCoeffs=Mat(1,5,CV_32FC1,coeffs); /* 摄像机的5个畸变系数：k1,k2,p1,p2,k3 */
	Mat R = Mat::eye(3,3,CV_32F);
	
	for(int i=0; i<3; i++)
	{
		for(int j=0; j<3; j++)
		{
			UDEBUG("%f ",cameraMatrix.at<float>(i,j));
		}
	}
	Size image_size;
	image_size.width = 640;
	image_size.height = 480;
	mapx = Mat(image_size,CV_32FC1);
	mapy = Mat(image_size,CV_32FC1);
	
	initUndistortRectifyMap(cameraMatrix,distCoeffs,R,cameraMatrix,image_size,CV_32FC1,mapx,mapy);
	
	return 0;
}


//从数据文件加载封面
JNIEXPORT jint JNICALL Java_ai_aistem_xbot_framework_application_PictureBookAPP_LoadCoverFromFile(JNIEnv *env, jobject instance,int type) {

	if( coverDetectionNode == NULL ){
		return -1;
	}
	coverDetectionNode->setFeatureType(type);
	coverDetectionNode->clear();
	
	if( type == FEATURE_SURF ){
	   return coverDetectionNode->loadFromFile("/sdcard/data/book/cover/data/cover_surf.data");
	}
	else if ( type == FEATURE_ORB ){
		return coverDetectionNode->loadFromFile("/sdcard/data/book/cover/data/cover_orb.data");
	}
	else if ( type == FEATURE_SIFT ){
		return coverDetectionNode->loadFromFile("/sdcard/data/book/cover/data/cover_sift.data");
	}
	return 0;
}

//从文件图片加载封面
JNIEXPORT jint JNICALL Java_ai_aistem_xbot_framework_application_PictureBookAPP_LoadCoverFromDir(JNIEnv *env, jobject instance,int type) {

	coverDetectionNode->setFeatureType(type);
	coverDetectionNode->clear();
	coverDetectionNode->loadObjects("/sdcard/data/book/cover/image/");
	return 0;
}
//增加单个封面
JNIEXPORT jint JNICALL Java_ai_aistem_xbot_framework_application_PictureBookAPP_AddOneCover(JNIEnv *env, jobject instance,jstring file) {
      const char* str;
      str = env->GetStringUTFChars(file,0);
      if(str == NULL) {
          return NULL;
      }
  string fileName = str;
	coverDetectionNode->addObjectAndUpdate(fileName,"/sdcard/data/book/cover/image/");
	return 0;
}


//保存封面数据文件
JNIEXPORT jint JNICALL Java_ai_aistem_xbot_framework_application_PictureBookAPP_SaveCover(JNIEnv *env, jobject instance) {

  int type = coverDetectionNode->getFeatureType();
  
	if( type == FEATURE_SURF ){
	   coverDetectionNode->saveToFile("/sdcard/data/book/cover/data/cover_surf.data");
	}
	else if ( type == FEATURE_ORB ){
		coverDetectionNode->saveToFile("/sdcard/data/book/cover/data/cover_orb.data");
	}
	else if ( type == FEATURE_SIFT ){
		coverDetectionNode->saveToFile("/sdcard/data/book/cover/data/cover_sift.data");
	}
	return 0;
}

//清除封面数据
JNIEXPORT jint JNICALL Java_ai_aistem_xbot_framework_application_PictureBookAPP_ClearCover(JNIEnv *env, jobject instance) {
	coverDetectionNode->clear();
	return 0;
}



//内容数据重新加载
JNIEXPORT jint JNICALL Java_ai_aistem_xbot_framework_application_PictureBookAPP_ReloadContenData(JNIEnv *env, jobject instance,int ID) {
	if(my_feature_type != ID%10 )
	{
		UDEBUG("recreat featureExtractor ..... ");
		my_feature_type = ID%10;
	}
    UDEBUG("ReloadContenData .....ID=%d ",ID);
	contentDetectionNode->ReloadDataFromID(ID);
	return 0;
}
//清除内容数据
JNIEXPORT jint JNICALL Java_ai_aistem_xbot_framework_application_PictureBookAPP_ClearContent(JNIEnv *env, jobject instance) {
	contentDetectionNode->clear();
	return 0;
}

//图片特征提取
JNIEXPORT jint JNICALL Java_ai_aistem_xbot_framework_application_PictureBookAPP_FeatureCompute(JNIEnv *env, jobject instance,long mat) {
	//UDEBUG("FeatureCompute..... ");
	Mat imgData = (*((Mat*)mat));
	//cv::resize(imgData,imgData,cv::Size(400,imgData.rows*400/imgData.cols));
	if(my_feature_type == FEATURE_SURF ){  
		featureExtractor_surf->computerDescriptors(imgData);
	}
	else if(my_feature_type == FEATURE_ORB ){
		featureExtractor_orb->computerDescriptors(imgData);
	}
	else if(my_feature_type == FEATURE_SIFT ){
		featureExtractor_sift->computerDescriptors(imgData);
	}
	//featureExtractor->computerDescriptors(imgData);
	//UDEBUG("feature size:%d %d",featureExtractor->descriptors().cols,featureExtractor->keypoints().size());
	return 0;
}


JNIEXPORT jint JNICALL Java_ai_aistem_xbot_framework_application_PictureBookAPP_FeatureCompute2(JNIEnv *env, jobject instance,long mat) {
    //UDEBUG("FeatureCompute..... ");
    Mat imgData = (*((Mat*)mat));
	//cv::resize(imgData,imgData,cv::Size(400,imgData.rows*400/imgData.cols));
    if(my_feature_type == FEATURE_SURF ){
        featureExtractor_surf_2->computerDescriptors(imgData);
    }
    else if(my_feature_type == FEATURE_ORB ){
        featureExtractor_orb->computerDescriptors(imgData);
    }
    else if(my_feature_type == FEATURE_SIFT ){
        featureExtractor_sift->computerDescriptors(imgData);
    }
    //featureExtractor->computerDescriptors(imgData);
    //UDEBUG("feature size:%d %d",featureExtractor->descriptors().cols,featureExtractor->keypoints().size());
    return 0;
}

JNIEXPORT jint JNICALL Java_ai_aistem_xbot_framework_application_PictureBookAPP_ContentDetection(JNIEnv *env, jobject instance) {
	
	cv::Mat descriptors_;
	std::vector<cv::KeyPoint> keyPoints_;
	if(my_feature_type == FEATURE_SURF ){
//		featureExtractor_surf->descriptors().copyTo(descriptors_);
//		 keyPoints_ = featureExtractor_surf->keypoints();
        return contentDetectionNode->detect(featureExtractor_surf->descriptors(),featureExtractor_surf->keypoints());
	}
	else if(my_feature_type == FEATURE_ORB ){
		//featureExtractor_orb->descriptors().copyTo(descriptors_);
		//keyPoints_ = featureExtractor_orb->keypoints();
        return contentDetectionNode->detect(featureExtractor_orb->descriptors(),featureExtractor_orb->keypoints());
	}
	else if(my_feature_type == FEATURE_SIFT ){
		//featureExtractor_sift->descriptors().copyTo(descriptors_);
		 //keyPoints_ = featureExtractor_sift->keypoints();
        return contentDetectionNode->detect(featureExtractor_sift->descriptors(),featureExtractor_sift->keypoints());
	}
	//featureExtractor->descriptors().copyTo(descriptors_);
	//std::vector<cv::KeyPoint> keyPoints_ = featureExtractor->keypoints();
	//return contentDetectionNode->detect(descriptors_,keyPoints_);
    return -1;
}

JNIEXPORT jint JNICALL Java_ai_aistem_xbot_framework_application_PictureBookAPP_ContentDetection2(JNIEnv *env, jobject instance) {

    cv::Mat descriptors_;
    std::vector<cv::KeyPoint> keyPoints_;
    if(my_feature_type == FEATURE_SURF ){
//		featureExtractor_surf->descriptors().copyTo(descriptors_);
//		 keyPoints_ = featureExtractor_surf->keypoints();
        return contentDetectionNode->detect(featureExtractor_surf_2->descriptors(),featureExtractor_surf_2->keypoints());
    }
    else if(my_feature_type == FEATURE_ORB ){
        //featureExtractor_orb->descriptors().copyTo(descriptors_);
        //keyPoints_ = featureExtractor_orb->keypoints();
        return contentDetectionNode->detect(featureExtractor_orb->descriptors(),featureExtractor_orb->keypoints());
    }
    else if(my_feature_type == FEATURE_SIFT ){
        //featureExtractor_sift->descriptors().copyTo(descriptors_);
        //keyPoints_ = featureExtractor_sift->keypoints();
        return contentDetectionNode->detect(featureExtractor_sift->descriptors(),featureExtractor_sift->keypoints());
    }
    //featureExtractor->descriptors().copyTo(descriptors_);
    //std::vector<cv::KeyPoint> keyPoints_ = featureExtractor->keypoints();
    //return contentDetectionNode->detect(descriptors_,keyPoints_);
    return -1;
}

JNIEXPORT jint JNICALL Java_ai_aistem_xbot_framework_application_PictureBookAPP_CoverDetection(JNIEnv *env, jobject instance) {
	//cv::Mat descriptors_;
	//std::vector<cv::KeyPoint> keyPoints_;
	if(my_feature_type == FEATURE_SURF ){
		//featureExtractor_surf->descriptors().copyTo(descriptors_);
		//keyPoints_ = featureExtractor_surf->keypoints();
        return coverDetectionNode->detect(featureExtractor_surf->descriptors(),featureExtractor_surf->keypoints());
	}
	else if(my_feature_type == FEATURE_ORB ){
		//featureExtractor_orb->descriptors().copyTo(descriptors_);
		//keyPoints_ = featureExtractor_orb->keypoints();
        return coverDetectionNode->detect(featureExtractor_orb->descriptors(),featureExtractor_orb->keypoints());
	}
	else if(my_feature_type == FEATURE_SIFT ){
		//featureExtractor_sift->descriptors().copyTo(descriptors_);
		//keyPoints_ = featureExtractor_sift->keypoints();
        return coverDetectionNode->detect(featureExtractor_sift->descriptors(),featureExtractor_sift->keypoints());
	}

    return -1;
	//featureExtractor->descriptors().copyTo(descriptors_);
	//std::vector<cv::KeyPoint> keyPoints_ = featureExtractor->keypoints();
	//return coverDetectionNode->detect(descriptors_,keyPoints_);
//	return coverDetectionNode->detect(featureExtractor->descriptors(),featureExtractor->keypoints());
}


JNIEXPORT jint JNICALL Java_ai_aistem_xbot_framework_application_PictureBookAPP_CoverDetection2(JNIEnv *env, jobject instance) {
    //cv::Mat descriptors_;
    //std::vector<cv::KeyPoint> keyPoints_;
    if(my_feature_type == FEATURE_SURF ){
        //featureExtractor_surf->descriptors().copyTo(descriptors_);
        //keyPoints_ = featureExtractor_surf->keypoints();
        return coverDetectionNode->detect(featureExtractor_surf_2->descriptors(),featureExtractor_surf_2->keypoints());
    }
    else if(my_feature_type == FEATURE_ORB ){
        //featureExtractor_orb->descriptors().copyTo(descriptors_);
        //keyPoints_ = featureExtractor_orb->keypoints();
        return coverDetectionNode->detect(featureExtractor_orb->descriptors(),featureExtractor_orb->keypoints());
    }
    else if(my_feature_type == FEATURE_SIFT ){
        //featureExtractor_sift->descriptors().copyTo(descriptors_);
        //keyPoints_ = featureExtractor_sift->keypoints();
        return coverDetectionNode->detect(featureExtractor_sift->descriptors(),featureExtractor_sift->keypoints());
    }

    return -1;
    //featureExtractor->descriptors().copyTo(descriptors_);
    //std::vector<cv::KeyPoint> keyPoints_ = featureExtractor->keypoints();
    //return coverDetectionNode->detect(descriptors_,keyPoints_);
//	return coverDetectionNode->detect(featureExtractor->descriptors(),featureExtractor->keypoints());
}
JNIEXPORT jint JNICALL Java_ai_aistem_xbot_framework_application_PictureBookAPP_IsValidImage(JNIEnv *env, jobject instance,long mat) {
	Mat imgData = (*((Mat*)mat));
	int row = imgData.rows;
	int col = imgData.cols;
	
	cv::Mat subImage1 = imgData.colRange(col*0.2,col*0.8).rowRange(row*0.45,row*0.55);
	double result1 = contentDetectionNode->getEntropy(subImage1);
	cv::Mat subImage2 = imgData.colRange(col*0.55,col*0.65).rowRange(row*0.2,row*0.8);
	double result2 = contentDetectionNode->getEntropy(subImage2);
//	UDEBUG("result: %f  --  %f  \n",result1,result2);
	if(result1 > 5.0 || result2 > 5.0)
	{
		return 1;
	}
	else
	{
		return 0;
	}
}
JNIEXPORT jbyteArray JNICALL Java_ai_aistem_xbot_framework_application_LetterRecognitionAPP_imageTransform(JNIEnv *env, jclass type, jlong mat)
{
	// TODO
	int colorType = CV_32S;
	Mat imgData = (*((Mat*)mat));
	Mat out;
	
//	cv::resize(imgData,imgData,cv::Size(imgData.cols,imgData.rows));
	warpPerspective(imgData,out,transfromMat,imgData.size(),cv::INTER_LINEAR,cv::BORDER_CONSTANT,cv::Scalar(255));
	
	jbyte  *outCharImage = new jbyte[out.cols * out.rows];
	for (int i = 0; i < out.cols * out.rows; i++) {
		outCharImage[i] = out.data[i];
	}
	
	jbyteArray result = env->NewByteArray(out.cols * out.rows);
	env->SetByteArrayRegion(result, 0, out.cols * out.rows, outCharImage);
	delete []outCharImage;
	return result;
}

JNIEXPORT jbyteArray JNICALL Java_ai_aistem_xbot_framework_application_PictureBookAPP_imageTransform(JNIEnv *env, jclass type, jlong mat) {
	
	// TODO
//	int colorType = CV_32S;
	Mat imgData = (*((Mat*)mat));
	if(imgData.empty()) return env->NewByteArray(0);
	Mat out = imagePreprocessor.preprocess(imgData);
	//Mat out = Mat(imgData.rows*2,imgData.cols*2,imgData.type());
//	Mat out;
	//remap(imgData,imgData,mapx, mapy, INTER_LINEAR);
	//flip(imgData, imgData, -1);
	//warpPerspective(imgData,out,transfromMat_picture,out.size(),cv::INTER_LINEAR);
	resize(out,out,imgData.size());
	jbyte  *outCharImage = new jbyte[out.cols * out.rows];
	for (int i = 0; i < out.cols * out.rows; i++) {
		outCharImage[i] = out.data[i];
	}
	
	jbyteArray result = env->NewByteArray(out.cols * out.rows);
	env->SetByteArrayRegion(result, 0, out.cols * out.rows, outCharImage);
	delete []outCharImage;
	return result;
}
JNIEXPORT void JNICALL Java_ai_aistem_xbot_framework_application_LetterRecognitionAPP_setWordLength(JNIEnv *env, jclass type, jint WordLength)
{
	charRecognitionVocabulary->setCurrentWordLength(WordLength);
}
}