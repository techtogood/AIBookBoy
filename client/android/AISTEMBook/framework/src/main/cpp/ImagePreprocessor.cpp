//
// Created by aistem on 19-3-21.
//

#include "ImagePreprocessor.h"

ImagePreprocessor::ImagePreprocessor() : image_size_(cv::Size(480, 490)) {
    initUndistortMap();
    initPerspectiveTransform();
}

void ImagePreprocessor::initPerspectiveTransform() {
    std::vector<cv::Point2f> corners;

    cv::Point2f tl(0.0, 0.0);
    cv::Point2f tr(image_size_.width * 1.0, 0.0);
    cv::Point2f bl(0.0, image_size_.height * 1.0);
    cv::Point2f br(image_size_.width * 1.0, image_size_.height * 1.0);

    corners.push_back(tl);
    corners.push_back(tr);
    corners.push_back(br);
    corners.push_back(bl);
    cv::Mat quad = cv::Mat::zeros(image_size_.height, image_size_.width, CV_8UC3);

    std::vector<cv::Point2f> quad_pts;
    quad_pts.emplace_back(cv::Point2f(0, 0));
    quad_pts.emplace_back(cv::Point2f(quad.cols, 0));
    quad_pts.emplace_back(cv::Point2f(image_size_.width - image_size_.width * 0.30, quad.rows));
    quad_pts.emplace_back(cv::Point2f(0 + image_size_.width * 0.30, quad.rows));
//    std::vector<cv::Point2f> corners;
//    cv::Point2f tl(160.0, 181.0);
//    cv::Point2f tr(624.0, 177.0);
//    cv::Point2f bl(1.0, 403.0);
//    cv::Point2f br(786.0, 390.0);
//
//    corners.push_back(tl);
//    corners.push_back(tr);
//    corners.push_back(br);
//    corners.push_back(bl);
//
//    cv::Mat quad = cv::Mat::zeros(300, 600, CV_8UC3);
//
//    std::vector<cv::Point2f> quad_pts;
//    quad_pts.emplace_back(cv::Point2f(0, 0));
//    quad_pts.emplace_back(cv::Point2f(quad.cols, 0));
//    quad_pts.emplace_back(cv::Point2f(quad.cols, quad.rows));
//    quad_pts.emplace_back(cv::Point2f(0, quad.rows));
    transfrom_mat_ = cv::getPerspectiveTransform(corners, quad_pts);
}

void ImagePreprocessor::initUndistortMap() {
    cv::Mat camera_matrix = (cv::Mat_<double>(3, 3) << 5.0767982411594704e+02, 0., 3.9460065635631986e+02, 0.,
            5.0767396437527020e+02, 3.0296756610297643e+02, 0., 0., 1.);
    cv::Mat distortion_coefficients = (cv::Mat_<double>(5, 1) << -4.0586834094104890e-01, 2.1789156170862639e-01,
            6.6262249711316105e-04, 7.9537429812495957e-04,
            -7.2256507609112244e-02);

    cv::Mat newMatrix = getOptimalNewCameraMatrix(camera_matrix, distortion_coefficients, this->image_size_, 0.5,
                                                  this->image_size_, 0, true);
    initUndistortRectifyMap(camera_matrix, distortion_coefficients, cv::Mat(),
                            newMatrix,
            //getDefaultNewCameraMatrix(camera_matrix, imageSize),
                            this->image_size_, CV_16SC2, this->map_x_, this->map_y_);
}

cv::Mat ImagePreprocessor::undistort(cv::Mat &_src) {
    cv::Mat dst;
    cv::Mat src = _src.clone();
    cv::remap(src, dst, this->map_x_, this->map_y_, cv::INTER_LINEAR);
    return dst;
}

cv::Mat ImagePreprocessor::transform(cv::Mat &_src) {
    cv::Mat quad = cv::Mat::zeros(490, 480, CV_8UC3);
    cv::warpPerspective(_src, quad, transfrom_mat_, quad.size());
    cv::flip(quad, quad, -1);
    return quad;
}

cv::Mat ImagePreprocessor::preprocess(cv::Mat &src) {
    //this->pre_image_ = this->undistort(src);
    this->pre_image_ = src(Rect(0, 150, 480, 490));
    this->final_image_ = this->transform(this->pre_image_);
    return this->final_image_;
}

cv::Mat ImagePreprocessor::preprocess(char *yuvData, bool resized) {
    this->pre_image_ = Yuv2Mat(yuvData);
    //cv::cvtColor(this->pre_image_, this->pre_image_, cv::COLOR_RGB2GRAY);
#if 1
    //cv::imwrite("./src.bmp", this->pre_image_);
    this->pre_image_ = this->undistort(this->pre_image_);
    //cv::imwrite("./transform.bmp",this->pre_image_);
    this->final_image_ = this->transform(this->pre_image_);
    //cv::imwrite("./dst.bmp",this->final_image_);
    cv::cvtColor(this->pre_image_, this->pre_image_, cv::COLOR_RGB2GRAY);
    if(resized)
        cv::resize(this->final_image_, this->final_image_, cv::Size(320, 240));
#endif
    return this->final_image_;
}

cv::Mat ImagePreprocessor::Yuv2Mat(char *yuvData) {
    cv::Mat yuvImg, srcImage;
    yuvImg.create(image_size_.height * 3 / 2, image_size_.width, CV_8UC1);
    memcpy(yuvImg.data, yuvData, image_size_.width * image_size_.height * 3 / 2);
    cv::cvtColor(yuvImg, srcImage, CV_YUV420sp2RGB);
    return srcImage;
}

ImagePreprocessor::~ImagePreprocessor() = default;



/*
%YAML:1.0
calibration_time: "Tue Jan 15 09:17:46 2019"
nframes: 16
image_width: 800
image_height: 600
board_width: 11
board_height: 8
square_size: 1.
flags: 0
camera_matrix: !!opencv-matrix
        rows: 3
cols: 3
dt: d
        data: [ 5.0767982411594704e+02, 0., 3.9460065635631986e+02, 0.,
5.0767396437527020e+02, 3.0296756610297643e+02, 0., 0., 1. ]
distortion_coefficients: !!opencv-matrix
        rows: 5
cols: 1
dt: d
        data: [ -4.0586834094104890e-01, 2.1789156170862639e-01,
6.6262249711316105e-04, 7.9537429812495957e-04,
-7.2256507609112244e-02 ]
avg_reprojection_error: 1.3329975452043663e-01
*/
