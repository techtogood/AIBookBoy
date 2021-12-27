# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

set(CMAKE_VERBOSE_MAKEFILE on)
set(alllibs "${CMAKE_SOURCE_DIR}/src/main/jniLibs")

include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/include)

add_library(libopencv_java3 SHARED IMPORTED )
set_target_properties(libopencv_java3 PROPERTIES
                      IMPORTED_LOCATION "${alllibs}/${ANDROID_ABI}/libopencv_java3.so")

#set_target_properties(libopencv_java3 PROPERTIES
#                      IMPORTED_LOCATION "${alllibs}/armeabi-v7a/libopencv_java3.so")


add_library( # Sets the name of the library.
             native-lib
             # Sets the library as a shared library.
             SHARED
             # Provides a relative path to your source file(s).
             src/main/cpp/native-lib.cpp
             src/main/cpp/native-lib-media.cpp
             src/main/cpp/Vocabulary.cpp
             src/main/cpp/Setting.cpp
             src/main/cpp/FindObjectNode.cpp
             src/main/cpp/ContentDetection.cpp
             src/main/cpp/CoverDetection.cpp
             src/main/cpp/FeatureExtractor.cpp
             src/main/cpp/FFmpegMusic.cpp
             src/main/cpp/ImagePreprocessor.cpp
             )

 add_library( avcodec-56
              SHARED
              IMPORTED)
 set_target_properties( avcodec-56
                        PROPERTIES IMPORTED_LOCATION
                        ${alllibs}/armeabi/libavcodec-56.so)
 add_library( avfilter-5
              SHARED
              IMPORTED)
 set_target_properties( avfilter-5
                        PROPERTIES IMPORTED_LOCATION
                        ${alllibs}/armeabi/libavfilter-5.so)

 add_library( avformat-56
              SHARED
              IMPORTED)
 set_target_properties( avformat-56
                        PROPERTIES IMPORTED_LOCATION
                        ${alllibs}/armeabi/libavformat-56.so)

 add_library( avutil-54
              SHARED
              IMPORTED)
 set_target_properties( avutil-54
                        PROPERTIES IMPORTED_LOCATION
                        ${alllibs}/armeabi/libavutil-54.so)

 add_library( swresample-1
              SHARED
              IMPORTED)
 set_target_properties( swresample-1
                        PROPERTIES IMPORTED_LOCATION
                        ${alllibs}/armeabi/libswresample-1.so)

 add_library( swscale-3
              SHARED
              IMPORTED)
 set_target_properties( swscale-3
                        PROPERTIES IMPORTED_LOCATION
                        ${alllibs}/armeabi/libswscale-3.so)
 include_directories(${alllibs}/include)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib android log libopencv_java3
                       avcodec-56
                       avfilter-5
                       avformat-56
                       avutil-54
                       swresample-1
                       swscale-3
                       OpenSLES

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )