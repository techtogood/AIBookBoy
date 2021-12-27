
##编译调试
1.安装andorid studio(建议3.0.0版本以上）

2.下载配置相关SDK和工具等

3.在线APP配置后台API，在HttpApiEndPoint.java中：
public static final String BOOK_SEARCH_URL = "http://xxx.xxx.xxx.xxx/search_book";


4.打开项目编译开发调试


## 代码说明：
1、摄像头图像采集在通过service实现，无预览的情况下，采集到图像数据，在以下文件中：
RobotCameraService.java

2、绘本阅读的逻辑代码都在PictureBookAPP.java中，这个文件代码逻辑比较复杂，主要是多线程的异步、同步处理，读懂这个文件代码，基本了解绘本阅读在前端的实现

3、算法C++代码在NDK中，封装JNI接口到java层