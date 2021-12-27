##############################################################################
# CMake
##############################################################################

cmake_minimum_required(VERSION 3.2.0)
project(PictureBookProcess)

find_package( OpenCV 3 REQUIRED)
find_package(CURL REQUIRED)
INCLUDE_DIRECTORIES(${OpenCV_INCLUDE_DIRS} ${CURL_INCLUDE_DIRS})
#find_library(APR_LIBRARY apr-1 PATHS /usr/local/apr/lib/)
#find_library(APR_UTIL_LIBRARY aprutil-1 PATHS /usr/local/apr/lib/)
#find_library(MINIXML_LIBRARY mxml)
#find_library(PTHREAD_LIBRARY pthread)
#find_library(RT_LIBRARY rt)
#find_library(M_LIBRARY m)


include_directories (${APR_INCLUDE_DIR})
include_directories (${APR_UTIL_INCLUDE_DIR})
include_directories (${MINIXML_INCLUDE_DIR})
include_directories (${CURL_INCLUDE_DIR})
include_directories ("/usr/local/include/oss_c_sdk")
#include_directories ("/usr/local/libxls/include/libxls")

#find_library(XLS_LIB xlsreader /usr/local/libxls/lib/)
find_library(APR_LIBRARY apr-1  /usr/local/apr/lib/)
find_library(APR_UTIL_LIBRARY aprutil-1  /usr/local/apr/lib/)
find_library(MINIXML_LIBRARY mxml)
find_library(CURL_LIBRARY curl)
find_library(PTHREAD_LIBRARY pthread)
find_library(RT_LIBRARY rt)
find_library(M_LIBRARY m)
find_library(MYSQL_CLIENT mysqlclient)


set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
#add_definitions(-DCOMPILEDWITHC11)


file(GLOB_RECURSE QT_MOC RELATIVE ${CMAKE_CURRENT_SOURCE_DIR} FOLLOW_SYMLINKS include/*.h)

file(GLOB_RECURSE QT_SOURCES RELATIVE ${CMAKE_CURRENT_SOURCE_DIR} FOLLOW_SYMLINKS src/*.cpp)

#
add_executable(PictureBookProcess ${QT_SOURCES} ${QT_RESOURCES_CPP} ${QT_FORMS_HPP} ${QT_MOC_HPP})
target_link_libraries(PictureBookProcess ${OpenCV_LIBS})
target_link_libraries(PictureBookProcess oss_c_sdk)
target_link_libraries(PictureBookProcess ${APR_UTIL_LIBRARY})
target_link_libraries(PictureBookProcess ${APR_LIBRARY})
target_link_libraries(PictureBookProcess ${MINIXML_LIBRARY})
target_link_libraries(PictureBookProcess ${CURL_LIBRARY})
target_link_libraries(PictureBookProcess ${PTHREAD_LIBRARY})
target_link_libraries(PictureBookProcess ${RT_LIBRARY})
target_link_libraries(PictureBookProcess ${M_LIBRARY})
#target_link_libraries(PictureBookProcess ${XLS_LIB})
target_link_libraries(PictureBookProcess ${MYSQL_CLIENT})
#install(TARGETS PictureBookProcess RUNTIME DESTINATION ${CATKIN_PACKAGE_BIN_DESTINATION})

