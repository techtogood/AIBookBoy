# 硬件配置和操作系统
```
1）推荐配置：4核 16G 以上
2）操作系统：ubuntu14.04以上
```
# 部署算法后台（nginx模板）
## 安装部署mysql
安装mysql:
```
sudo apt-get install mysql-server
sudo apt-get install libmysqlclient-dev
```

创建数据库：
```
create database picture_book;
```

创建表：
```
DROP TABLE IF EXISTS `cover`;
CREATE TABLE `cover` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `series_id` varchar(64) NOT NULL DEFAULT '' COMMENT '绘本系列ID',
  `book_name` varchar(255) NOT NULL DEFAULT '' COMMENT '绘本书名',
  `record` varchar(511) NOT NULL DEFAULT '' COMMENT '绘本封面录音文件URL 可以有多个URL（| 垂线间隔）',
  `image` varchar(511) NOT NULL DEFAULT '' COMMENT '绘本封面采集图片URL 可以有多个URL（| 垂线间隔）',
  `picture` varchar(511) NOT NULL DEFAULT '' COMMENT '绘本内容页展示图片URL 可以有多个URL（| 垂线间隔）',
  `feature_type` tinyint(4) unsigned NOT NULL DEFAULT '0' COMMENT '绘本内容算法特征类型：0 SURF 2 ORB 3 SIFT',
  `data` varchar(511) NOT NULL DEFAULT '' COMMENT '绘本内容的算法数据URL：1:surf 2:orb 3:sift（| 垂线间隔）',
  `page_num` smallint(6) NOT NULL DEFAULT '0' COMMENT '绘本页数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL,
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1880 DEFAULT CHARSET=utf8 COMMENT='绘本封面表';


DROP TABLE IF EXISTS `content`;
CREATE TABLE `content` (
  `cover_id` int(11) unsigned NOT NULL,
  `page_id` smallint(6) unsigned NOT NULL,
  `text` text NOT NULL COMMENT '绘本页的文字内容',
  `record` varchar(511) NOT NULL DEFAULT '' COMMENT '绘本内容页录音文件URL，可以有多个URL（| 垂线间隔）',
  `picture` varchar(511) NOT NULL DEFAULT '' COMMENT '绘本内容页展示图片URL 可以有多个URL（| 垂线间隔）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL,
  `delete_time` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='绘本内容表';
```

##安装部署opencv
安装依赖库：
```
sudo apt-get install build-essential
sudo apt-get install cmake git libgtk2.0-dev pkg-config libavcodec-dev libavformat-dev libswscale-dev
sudo apt-get install python-dev python-numpy libtbb2 libtbb-dev libjpeg-dev libpng-dev libtiff-dev libjasper-dev libdc1394-22-dev
```
编译安装：
```
unzip opencv-3.2.0.zip
unzip opencv_contrib-3.2.0.zip 
cd opencv-3.2.0/
mkdir build
cd build/

cmake -D CMAKE_BUILD_TYPE=Release -D OPENCV_EXTRA_MODULES_PATH=../../opencv_contrib-3.2.0/modules/ -D CMAKE_INSTALL_PREFIX=/usr/local .. 
make 
sudo make install
```

##安装部署redis
```
安装redis
sudo apt-get update
sudo apt-get install redis-server

安装hiredis
git clone  https://github.com/redis/hiredis.git
make 
make install
```

##安装jsoncpp
```
tar -zvxf scons-2.1.0.tar.gz
进入到scons解压目录下，执行：
sudo python setup.py install

tar -zvxf jsoncpp-src-0.6.0-rc2.tar.gz
进入到jsoncpp解压目录下，执行命令：
sudo scons platform=linux-gcc

将/jsoncpp-src-0.6.0/include/目录下的json文件夹拷贝到/usr/include/

将jsoncpp-src-0.6.0/libs/linux-gcc-4.9.1/目录下的libjson_linux-gcc-4.9.1_libmt.a 拷贝到/usr/local/lib/下，并为了方便使用，将其重命名为libjson.a

mv libs/linux-gcc-4.4.7/libjson_linux-gcc-4.4.7_libmt.so /lib 
ln /lib/libjson_linux-gcc-4.4.7_libmt.so /lib/libjson.so 
mv include/json/ /usr/include/ 
ldconfig 
./bin/linux-gcc-4.4.7/test_lib_json 
```

##安装openssl
```
sudo apt-get install openssl 
sudo apt-get install libssl-dev
```

6）安装部署nginx
进入nginx目录
```
./configure \
--with-debug \ 
--prefix=/usr/local/nginx \
--pid-path=/var/run/nginx/nginx.pid \
--lock-path=/var/lock/nginx.lock \
--error-log-path=/var/log/nginx/error.log \
--http-log-path=/var/log/nginx/access.log \
--with-http_gzip_static_module \
--http-client-body-temp-path=/var/temp/nginx/client \
--http-proxy-temp-path=/var/temp/nginx/proxy \
--http-fastcgi-temp-path=/var/temp/nginx/fastcgi \
--http-uwsgi-temp-path=/var/temp/nginx/uwsgi \
--http-scgi-temp-path=/var/temp/nginx/scgi \
--add-module=./ngx_http_searchCover_module

make
sudo make install

拷贝nginx.conf 到 /usr/local/nginx/conf/ 目录下替换默认配置文件


域名映射到服务器后（如不用域名，此步骤可以忽略，直接IP访问），更新域名到nginx.conf配置中：
server_name xxxx.yyyy.com; #xxxx.yyyy.com 替换为申请的域名

购买HTTPS(ssl)证书，拷贝到/usr/local/nginx/conf/cert目录下，并更新到nginx.conf配置中：
ssl_certificate   cert/215082401730553.pem;   #申请到对应SSL证书后替换此文件
ssl_certificate_key  cert/215082401730553.key; #申请到对应SSL证书后替换此文件
```



##后台部署完成，执行以下命令开启/停止服务：
```
开启：
sudo /usr/local/nginx/sbin/nginx
停止：
sudo /usr/local/nginx/sbin/nginx -s stop
重启：
sudo /usr/local/nginx/sbin/nginx -s reopen
```

如出现相关文件缺少，创建
```
mkdir -p /var/temp/nginx 
mkdir -p /usr/local/nginx/logs
```



#绘本训练上线数据，进入PictureBookProcess目前

##绘本训练上线原理：
PictureBookProcess 是绘本数据训练上线程序，她做的事情是：
读取约定目录命名的绘本数据（图像、音频等）文件，进行图像信息训练生成图像字典文件（图典文件），
并将图典文件、绘本音频上传到阿里OSS，并将数据和关系上传到cover 和  content两个mysql表中，完成上线。


##文件目录预置：
```
1）创建以下文件夹：
/home/xiaojuan/picture_book/data/scan/
/home/xiaojuan/picture_book/data/cover/
/home/xiaojuan/picture_book/data/content/

2)拷贝 K12英语三年级上册_9787107244674 文件到 /home/xiaojuan/picture_book/data/scan/目录中

3)/home/xiaojuan/picture_book/data 路径在PictureBookProcess和server/src 都有hardcode代码里，如需修改，搜索代码修改；
```
##修改代码配置：
```
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
```


##编译pictureBookProcess程序
```
1）编译安装阿里云OSS_C_SDK，详细参考以下github路径：
https://github.com/aliyun/aliyun-oss-c-sdk

编译aliyun-oss-c-sdk是可能有点坑，找不到apr的头文件，需要手动生成软连接：
ln -s /usr/local/apr/include/apr-1/* /usr/local/include/

2)编译PictureBookProcess程序，进入PictureBookProcess目录，执行以下指令：
mkdir build
cd build
cmake ../
make
```


##绘本数据训练上线：
```
1）执行:pictureBookProcess/build/PictureBookProcess 程序；

2）sudo /usr/local/nginx/sbin/nginx -l
成功后生成以下文件（封面的图像字典文件）：
/usr/local/nginx/conf/cover.bin 


3）执行 sudo /usr/local/nginx/sbin/nginx -s reopen


##至此绘本后台部署完成并上线，根据nginx.conf的配置，提供的接口是：
http://xxx.xxx.xxx.xxx/search_book ( 部署机器的ip)
```

