# pictureBookProcess


##绘本训练上线数据

###绘本训练上线原理：
PictureBookProcess 是绘本数据训练上线程序，她做的事情是：
读取约定目录命名的绘本数据（图像、音频等）文件，进行图像信息训练生成图像字典文件（图典文件），
并将图典文件、绘本音频上传到阿里OSS，并将数据和关系上传到cover 和  content两个mysql表中，完成上线。


###文件目录预置：
1）创建以下文件夹：
/home/xiaojuan/picture_book/data/scan/
/home/xiaojuan/picture_book/data/cover/
/home/xiaojuan/picture_book/data/content/

2)拷贝 K12英语三年级上册_9787107244674 文件到 /home/xiaojuan/picture_book/data/scan/目录中

3)/home/xiaojuan/picture_book/data 路径在PictureBookProcess和server/src 都有hardcode代码里，如需修改，搜索代码修改；

###修改代码配置：
1）PictureBookProcess中,main.cpp:
配置阿里OSS的bucket名称和目录，xxxx是bucket名称，默认目录名是：picture_book
const string OSSROOT = "https://xxxx.oss-cn-shanghai.aliyuncs.com/picture_book/";  //阿里OSS目录，xxx是bucket名称，默认目录名是：picture_book
const string OSSRELATIVEROOT = "picture_book/"; 

配置mysql
define MYSQL_USRNAME "root"  //mysql user
define MYSQL_PASS "xxxxxx"  //mysql pass
define MYSQL_DBNAME "picture_book"  //mysql db name


2）pictureBookProcess中,ossclientmanager.h:
define OSS_BUCKET "xxxx"  //bucket name 
define OSS_ENDPIONT "http://oss-cn-shanghai.aliyuncs.com" //oss endpoint
define OSS_ID "xxxxxxxxxxxxxxxx"  //oss access id
define OSS_SECRET "yyyyyyyyyyyyyyyy" //oss access secret



###编译pictureBookProcess程序
1）编译安装阿里云OSS_C_SDK，详细参考以下github路径：
https://github.com/aliyun/aliyun-oss-c-sdk

编译aliyun-oss-c-sdk是可能有点坑，找不到apr的头文件，需要手动生成软连接：
ln -s /usr/local/apr/include/apr-1/* /usr/local/include/

2)编译PictureBookProcess程序，进入PictureBookProcess目录，执行以下指令：
mkdir build
cd build
cmake ../
make



##绘本数据训练上线：

1）执行:pictureBookProcess/build/PictureBookProcess 程序；

2）sudo /usr/local/nginx/sbin/nginx -l
成功后生成以下文件（封面的图像字典文件）：
/usr/local/nginx/conf/cover.bin 


3）执行 sudo /usr/local/nginx/sbin/nginx -s reopen


7.至此绘本后台部署完成并上线，根据nginx.conf的配置，提供的接口是：
http://xxx.xxx.xxx.xxx/search_book ( 部署机器的ip)

