//
// Created by aistem on 19-3-21.
//

#ifndef _IMAGEPREPROCESSOR_H
#define _IMAGEPREPROCESSOR_H

#include <opencv2/opencv.hpp>

using namespace cv;
class ImagePreprocessor {
public:
    ImagePreprocessor();
    ~ImagePreprocessor();

    cv::Mat preprocess(cv::Mat &src);
    cv::Mat preprocess(char *yuvData, bool resized);

private:
    cv::Mat Yuv2Mat(char *yuvData);

    //畸形矫正
    void initUndistortMap();
    cv::Mat undistort(cv::Mat &_src);
    //透视变换
    void initPerspectiveTransform();
    cv::Mat transform(cv::Mat &_src);

    cv::Mat transfrom_mat_;

    cv::Size image_size_;
    cv::Mat map_x_, map_y_;

    cv::Mat pre_image_;
    cv::Mat final_image_;
};


#endif //_IMAGEPREPROCESSOR_H
