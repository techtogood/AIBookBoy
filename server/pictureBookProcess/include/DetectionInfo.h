/*
Copyright (c) 2011-2014, Mathieu Labbe - IntRoLab - Universite de Sherbrooke
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Universite de Sherbrooke nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
#ifndef DETECTIONINFO_H_
#define DETECTIONINFO_H_

#include <opencv2/features2d/features2d.hpp>
#include <vector>
#include <map>

namespace my_find_object {

class DetectionInfo
{
public:
	enum TimeStamp{
		kTimeKeypointDetection,
		kTimeDescriptorExtraction,
		kTimeSubPixelRefining,
		kTimeSkewAffine,
		kTimeIndexing,
		kTimeMatching,
		kTimeHomography,
		kTimeTotal
	};
	enum RejectedCode{
		kRejectedUndef,
		kRejectedLowMatches,
		kRejectedLowInliers,
		kRejectedSuperposed,
		kRejectedAllInliers,
		kRejectedNotValid,
		kRejectedCornersOutside,
		kRejectedByAngle
	};

public:
	DetectionInfo() :
		minMatchedDistance_(-1),
		maxMatchedDistance_(-1)
	{}

public:
	// Those maps have the same size
 // QMultiMap<int, QTransform> objDetected_;

  //存放检测结果 key=检测到的image的名称(序号)  value=一个单应矩阵  表示原图像到当前图像的转换


  std::multimap<int, cv::Mat> objDetected_;

 // std::multimap<int, QSize> objDetectedSizes_; // Object ID <width, height> match the number of detected objects
  std::multimap<int, std::string > objDetectedFilePaths_; // Object ID <filename> match the number of detected objects
  std::multimap<int, int> objDetectedInliersCount_; // ObjectID <count> match the number of detected objects
  std::multimap<int, int> objDetectedOutliersCount_; // ObjectID <count> match the number of detected objects
  std::multimap<int, std::multimap<int, int> > objDetectedInliers_; // ObjectID Map< ObjectDescriptorIndex, SceneDescriptorIndex >, match the number of detected objects
  std::multimap<int, std::multimap<int, int> > objDetectedOutliers_; // ObjectID Map< ObjectDescriptorIndex, SceneDescriptorIndex >, match the number of detected objects

  //std::map<TimeStamp, float> timeStamps_;

  //相机传回图像的keyPoints
	std::vector<cv::KeyPoint> sceneKeypoints_;
  //相机传回图像的Descriptor
	cv::Mat sceneDescriptors_;
  //相机传回图像的Descriptor与词典中的descriptors的对应关系
  std::multimap<int, int> sceneWords_;
  std::map<int, std::multimap<int, int> > matches_; // ObjectID Map< ObjectDescriptorIndex, SceneDescriptorIndex >, match the number of objects

	// Those maps have the same size
  ///QMultiMap<int, QMultiMap<int, int> > rejectedInliers_; // ObjectID Map< ObjectDescriptorIndex, SceneDescriptorIndex >
  ///QMultiMap<int, QMultiMap<int, int> > rejectedOutliers_; // ObjectID Map< ObjectDescriptorIndex, SceneDescriptorIndex >
  ///QMultiMap<int, RejectedCode> rejectedCodes_; // ObjectID rejected code

	float minMatchedDistance_;
	float maxMatchedDistance_;
};
} // namespace find_object

#endif /* DETECTIONINFO_H_ */
