/**
 * @file /src/main.cpp
 *
 * @brief Qt based gui.
 *
 * @date November 2010
 **/
/*****************************************************************************
** Includes
*****************************************************************************/

#include "../include/FindObjectNode.h"
#include <curl/curl.h>
#include "../include/json/json.h"
#include "../include/okhttps.h"
#include "../include/ossclientmanager.h"
#include <oss_c_sdk/aos_log.h>
#include <oss_c_sdk/aos_util.h>
#include <oss_c_sdk/aos_string.h>
#include <oss_c_sdk/aos_status.h>
#include <oss_c_sdk/oss_auth.h>
#include <oss_c_sdk/oss_util.h>
#include <oss_c_sdk/oss_api.h>
#include <sys/stat.h> 
#include <stdio.h>
#include <stdlib.h>
//#include <libxls/xls.h>
#include <mysql/mysql.h>



/*****************************************************************************
** Main
*****************************************************************************/
using namespace my_find_object;

const string PATH = "";
const string COVERFILEPATH = "/home/xiaojuan/picture_book/data/cover/";
const string CENTENTFILEPATH = "/home/xiaojuan/picture_book/data/cover/data/";
const string ROOTPATH = "/home/xiaojuan/picture_book/data/";

//public
const string OSSROOT = "https://xxxx.oss-cn-shanghai.aliyuncs.com/picture_book/";  //阿里OSS目录，xxx是bucket名称，默认目录名是：picture_book
const string OSSRELATIVEROOT = "picture_book/"; 


#define MYSQL_USRNAME "root"  //mysql user
#define MYSQL_PASS "xxxxxx"  //mysql pass
#define MYSQL_DBNAME "picture_book"  //mysql db name




vector<string> book_name;//绘本书名
vector<string> series_id;//绘本条形码

OSSClientManager *oss =  NULL;

MYSQL *g_mySQLConn = NULL;
MYSQL *g_mySQLConn_2 = NULL;



void makeAlgorithmFileAndUpload(const string &contentDirName,const string &id);
void getBookNameAndSID(const string &rootPath );
int getMaxID( string db,string table );
int getDupId(const string &bookName,const string &seriesId);
void SetupCoverData( Cover& cover, std::vector<Content>& contentList );
void SetupData( Cover& cover, std::vector<Content>& contentList );
string HandleSpecialChar( char* src_str  );


string buffer;

int getMaxID( string db,string table )
{
	  char* host = "127.0.0.1";
		char* user = MYSQL_USRNAME;
		char* pwd = MYSQL_PASS;
		char* db_name = (char*)db.c_str();
		char sql_buf[256]={0};
		char value[32]={0};
		MYSQL_RES *result;
		MYSQL_ROW row;
		int id = 0;
		g_mySQLConn = mysql_init(NULL); // 初始化数据库连接变量  

    g_mySQLConn = mysql_real_connect(g_mySQLConn, host,  
            user, pwd, db_name, 0, NULL, 0);  
            
    
    if(g_mySQLConn == NULL)  
    {  

        return NULL ;  
    }  
	  mysql_query(g_mySQLConn,"SET NAMES UTF8");
	  
		sprintf(sql_buf,"select id from %s order by id desc limit 1",table.c_str());
		if(mysql_query(g_mySQLConn, sql_buf))  
		{  

			return NULL;
		}  
		result = mysql_store_result(g_mySQLConn);
		if( mysql_num_rows(result) > 0 )
    {
       row = mysql_fetch_row(result);  
       id = atoi(row[0]);
    }
		
		mysql_free_result(result);
		mysql_close(g_mySQLConn);
		return id;
}



string HandleSpecialChar( char* src_str  )
{

  if( NULL == src_str )
  {
    return "";
  }

	char dst_srt[strlen(src_str)+128];
	memset(dst_srt,0,strlen(src_str)+128);
	char * cur_p = src_str;
	char* src_p = src_str;
	char* dst_p = dst_srt;
	
	while( (*cur_p) != '\0' )
	{

	   if( *cur_p == -95)
	   {

	     if( *(cur_p+1) == -79 || *(cur_p+1) == -80)  //中文双引号
	     {
	       *(dst_p++) = '\"';
	       cur_p+=2;
	       
	     }
	     else if( *(cur_p+1) == -81 )  //中文单引号
	     {
	       *(dst_p++) = '\'';
	       *(dst_p++) = '\'';
	       cur_p+=2;
	       
	     }
	     else if( *(cur_p+1) == -86 )  //中文横杆
	     {
	       *(dst_p++) = '-';
	       cur_p+=2;
	       
	     }
	     else
	     {
  	     *dst_p = *cur_p;
  	     dst_p++;
  	     cur_p++;
	     }
	   }
	   else if ( *cur_p == '\'' ) //英文单引号
	   {
	     *(dst_p++) = '\'';
	     *(dst_p++) = '\'';
	     cur_p++;
	   }
	   else
	   {
	     *dst_p = *cur_p;
	     dst_p++;
	     cur_p++;
	   }
	   
	}

  return dst_srt;
}



string handleEnterNewLine(char* src_str  )
{

  if( NULL == src_str )
  {
    return "";
  }

	char dst_srt[strlen(src_str)+128];
	memset(dst_srt,0,strlen(src_str)+128);
	char * cur_p = src_str;
	char* src_p = src_str;
	char* dst_p = dst_srt;
	
	while( (*cur_p) != '\0' )
	{
	   if ( *cur_p == '\r' ) //回车
	   {
	     *(dst_p++) = 92;
	     *(dst_p++) = 92;
	     *(dst_p++) = 'r';
	     cur_p++;
	   }
	   else if ( *cur_p == '\n' ) //换行
	   {
	     *(dst_p++) = 92;
	     *(dst_p++) = 92;
	     *(dst_p++) = 'n';
	     cur_p++;
	   }
	   else
	   {
	     *dst_p = *cur_p;
	     dst_p++;
	     cur_p++;
	   }
	   
	}

  return dst_srt;
}

std::string getFirstURL(std::string url)
{
  int nPos = url.find("|");
  
  if(nPos != -1)
  
  {
  
      return url.substr(0, nPos);
  
  }
  
  return url;

}

int ParseBookDataToDatabase(const char* json )
{
		Json::Value root;
		Json::Value arrayObj;
		Json::Value item;
		Json::Reader reader;  
		char* host = "127.0.0.1";
		char* user = MYSQL_USRNAME;
		char* pwd = MYSQL_PASS;
		char* db_name =MYSQL_DBNAME;
		
		char sql_buf[10240] = {0};
		char value[32]={0};
		MYSQL_RES *result;
		MYSQL_ROW row;
		int cover_id = 0;
		
		g_mySQLConn = mysql_init(NULL); // 初始化数据库连接变量  

    g_mySQLConn = mysql_real_connect(g_mySQLConn, host,  
            user, pwd, db_name, 0, NULL, 0);  
	            
    if(g_mySQLConn == NULL)  
    {  
        printf("MySQL11 Error: %d \n",mysql_error(g_mySQLConn));
        return NULL ;  
    }  

    
	  mysql_query(g_mySQLConn,"SET NAMES UTF8");

    if (reader.parse(json, root))  
    {  
      Json::Value cover = root["cover"];  
      Json::Value arrayObj= root["content"];  
      cout<<cover["record"].asString()<<endl;
      cout << "array size = " << arrayObj.size() << endl;

      // 更新机器人绘本数据
      sprintf(sql_buf,"SELECT id from cover WHERE series_id ='%s' and book_name='%s';",cover["series_id"].asString().c_str(),cover["book_name"].asString().c_str());

  		if(mysql_query(g_mySQLConn, sql_buf)) //查询是否有相同条目
  		{  

  			return NULL;
  		} 
  		
  		result = mysql_store_result(g_mySQLConn);

  		if( mysql_num_rows(result) > 0 ) //如有相同条目
  		{
        row = mysql_fetch_row(result);  
        cover_id = atoi(row[0]);

        sprintf(sql_buf,"UPDATE cover SET series_id='%s', book_name='%s' ,record='%s',image='%s',feature_type='%s',data='%s',page_num='%s' WHERE id='%d';"\
                     ,cover["series_id"].asString().c_str(),cover["book_name"].asString().c_str(),cover["record"].asString().c_str(),cover["image"].asString().c_str(),\
                     cover["feature_type"].asString().c_str(),cover["data"].asString().c_str(),cover["page_num"].asString().c_str(),cover_id);

        if(mysql_query(g_mySQLConn, sql_buf))  
    		{  
    			return -1;
    		} 
    		
    		
  		}
  		else //如果没有相同条目
  		{
  		  cover_id = atoi(cover["id"].asString().c_str());

  		     
        sprintf(sql_buf,"INSERT INTO cover (id, series_id, book_name, record,image,feature_type,data,page_num) VALUES ('%s','%s','%s','%s','%s','%s','%s','%s');"\
                     ,cover["id"].asString().c_str(),cover["series_id"].asString().c_str(),cover["book_name"].asString().c_str(),cover["record"].asString().c_str()\
                     ,cover["image"].asString().c_str(),cover["feature_type"].asString().c_str(),cover["data"].asString().c_str(),cover["page_num"].asString().c_str());
                     
        if(mysql_query(g_mySQLConn, sql_buf))  
    		{  
    			return -1;
    		} 
    		

  		}

      for(unsigned int i = 0; i < arrayObj.size(); i++)
      {
        Json::Value content = arrayObj[i];
        //机器人
        sprintf(sql_buf,"SELECT cover_id from content WHERE cover_id ='%d' and page_id='%s'",cover_id,content["page_id"].asString().c_str());
        if(mysql_query(g_mySQLConn, sql_buf))  
        {  

          return -1;
        } 
        
        string text;

        if(strlen(content["text"].asString().c_str())> 0)
        {
          text = handleEnterNewLine((char*)(content["text"].asString().c_str()));
        }
        else
        {
          text = content["text"].asString();
        }

        cout<<"str_out=== "<<text<<"len="<<strlen(content["text"].asString().c_str())<<endl;
            
        result = mysql_store_result(g_mySQLConn);

        
        if( mysql_num_rows(result) > 0 )
        {
          //cout <<"1111" <<content["text"].asString()<<endl;
          sprintf(sql_buf,"UPDATE content SET text='%s', record='%s' WHERE cover_id='%d' and page_id='%s';"\
                       ,/*content["text"].asString().c_str()*/text.c_str(),content["record"].asString().c_str(), cover_id, content["page_id"].asString().c_str());

          if(mysql_query(g_mySQLConn, sql_buf))  
          {  
            return -1;
          } 
        }
        else
        {
          sprintf(sql_buf,"INSERT INTO content (cover_id, page_Id, text, record) VALUES ('%d','%s','%s','%s')"\
                       ,cover_id,content["page_id"].asString().c_str(),/*content["text"].asString().c_str()*/text.c_str(),content["record"].asString().c_str());

          if(mysql_query(g_mySQLConn, sql_buf))  
          {  
            return -1;
          } 
        }

      }
    }  

    mysql_free_result(result);
		mysql_close(g_mySQLConn);


    return 0;


}


void testHttps()
{
  OkHttps *URL = OkHttps::getOkHttps();
  std::string url = "http://192.168.51.228:8087/cover";
  std::string request1 = "260";
  if(true != URL->GET(url,request1,buffer))
  {
    cout << "Get false" << endl;
    return;
  }
  buffer = "["+buffer + "]";
  cout << buffer << endl;

  Json::CharReaderBuilder builder;
  Json::CharReader *reader = builder.newCharReader();
  Json::Value root;
  string err;
  if(reader->parse(buffer.data(),buffer.data()+buffer.size(),&root,&err))
  {

    for (auto i = 0; i < root.size(); i++)//遍历数组[]
    {
      cout << "i = " << i << endl;
      Cover cover(root[i]);
      //cover.display();
      cover.page_num = "101";
      std::string request = cover.toJSON().toStyledString();
      //cout << "cover.toJSON().toStyledString()\n" << request << endl;
      /*if(true != URL->POST(url,request))
      {
        cout << "POST false" << endl;
        return;
      }*/
    }
  }
  cout << "endl" << endl;

//  OkHttps::deleteOkHttps();
}

//aos_str_set(&options->config->endpoint, "http://oss-cn-hangzhou.aliyuncs.com");
//aos_str_set(&options->config->access_key_id, "LTAIEQcgMdrxsgOo");
//aos_str_set(&options->config->access_key_secret, "sSLulNPmjECmduLAJHxJBA7p0IwoVY");
void testOSS()
{
  OSSClientManager *oss = OSSClientManager::getOSSClientManager();
  oss->UpdateFile("/home/xiaojuan/res/data/cover.bin","Log/1/cover.bin");
  //oss->DeleteFile("Log/1/11.data");
  OSSClientManager::destroy();
}

int main(int argc, char **argv)
{
  Cover cover = Cover();
  std::vector<Content> contentList;


  //解析文件夹 加载绘本数据，包括机器人绘本数据和手机APP绘本数据
  int start_id = getMaxID("picture_book","cover"); 
  cout <<"start_id"<<start_id<< endl;
  
  if ( start_id < 1000 ) //导入数据id以1000开始，预留一些id
  {
    start_id = 1000;
  }
  else
  {
    start_id++;
  }
  getBookNameAndSID(ROOTPATH);


  for (size_t i =0; i < book_name.size(); i ++) //book_name's size and series_id's size should be same;
  {
    cover.clear();
    contentList.clear();
    Json::Value root;
  	Json::Value arrayObj;

    //cout<<book_name[i]<<endl;
  	//fprintf(fp,"%s\n",book_name[i].c_str());

  	//continue;
  	int dupId = getDupId(book_name[i],series_id[i]);

  	
  	
  	if(dupId == 0)
  	{
      cover.id = std::to_string(start_id++);
    }
    else
    {
      //continue;
      cout <<"====start====bookName=="<<book_name[i]<< "dupId=="<<dupId<<endl;
      cover.id = std::to_string(dupId);
    }
    cover.book_name = book_name[i];
    cover.series_id = series_id[i];
    cover.feature_type = "1";//default is SUFR

    //SetupCoverData(cover, contentList);//生成封面算法图像

#if 1
    oss = OSSClientManager::getOSSClientManager();
    
    SetupData(cover, contentList);//生成cover 和content 数据 并将相关文件上传到OSS


    //根据cover 和 content对象生成JSON
    root["cover"] = cover.toJSON();
    for (int i = 0; i < contentList.size(); i++)
    {
  	  arrayObj.append(contentList[i].toJSON());
    }
    root["content"] = arrayObj;

    cout << root.toStyledString() << endl;

    ParseBookDataToDatabase(root.toStyledString().c_str());
#if 0
    buffer = "";
    OkHttps *URL = OkHttps::getOkHttps();
    URL->InitOkHttps();
    std::string url = "http://192.168.51.228/postbook";
    std::string request = root.toStyledString();
    if(true != URL->POST(url,request,buffer))
    {
      cout << "post false" << endl;
      //return -1;
    }
    URL->clearOkHttps();
#endif
    #endif

  }
  
  //fclose(fp);

  cout<<"======successful========="<<endl;
  //解析文件夹 加载绘本数据，包括机器人绘本数据和手机APP绘本数据 end

  
  OSSClientManager::destroy();


  /*std::vector<string> CoverLists = getFiles(CENTENTFILEPATH);
  for(int i=0; i<CoverLists.size(); i++)
  {
    cout << CoverLists[i] << endl;
    makeAlgorithmFile(CoverLists[i]);
  }*/
  //testHttps();
  //testOSS();
  
  //getBookNameAndSID(ROOTPATH);

  /*Content cont;
  cont.cover_id = "10";
  cont.page_id = "100";
  cont.text = "Hello World";
  cout << cont.toJSON().toStyledString() << endl;

  ContentObject cObject;
  cObject.id = "1001";
  cObject.image = "/data/1.png";

  cout << cObject.toJSON().toStyledString() << endl;*/
  return 0;
}


void makeAlgorithmFileAndUpload(const string &contentDirName, Cover& cover)
{
  // TODO
    UDEBUG("FilePATH = %s\n",contentDirName.data());
    //SUFT
    {
      FeatureExtractor *FeatureExtractor_SURF = new FeatureExtractor(1);
      FindObjectNode *FindObjectNode_SURF = new FindObjectNode(FeatureExtractor_SURF);

      string srcFilePATH = contentDirName;
      string destFilePATH = CENTENTFILEPATH + cover.id + "1.data";
  //		FindObjectNode_SURF->loadFromOneFile(destFilePATH);
      FindObjectNode_SURF->loadObjects(srcFilePATH);
      FindObjectNode_SURF->saveToOneFile(destFilePATH);
      UDEBUG(" %s SURF computer end...\n",destFilePATH.data());
      
      oss->UpdateFile(destFilePATH,OSSRELATIVEROOT+"cover/data/"+ cover.id + "1.data");
      cover.data = OSSROOT+"cover/data/"+ cover.id + "1.data";

      delete FeatureExtractor_SURF;
      delete FindObjectNode_SURF;
    }
    //ORB
    {
      FeatureExtractor *FeatureExtractor_ORB = new FeatureExtractor(2);
      FindObjectNode *FindObjectNode_ORB = new FindObjectNode(FeatureExtractor_ORB);

      string srcFilePATH = contentDirName;
      string destFilePATH = CENTENTFILEPATH + cover.id + "2.data";
      FindObjectNode_ORB->loadObjects(srcFilePATH);
      FindObjectNode_ORB->saveToOneFile(destFilePATH);
      UDEBUG(" %s ORB computer end...\n",destFilePATH.data());
      
      oss->UpdateFile(destFilePATH,OSSRELATIVEROOT+"cover/data/"+ cover.id + "2.data");
      cover.data = cover.data+"|"+OSSROOT+"cover/data/"+ cover.id + "2.data";
      
      delete FindObjectNode_ORB;
      delete FeatureExtractor_ORB;
    }

    //SIFT
    {
      FeatureExtractor *FeatureExtractor_SIFT = new FeatureExtractor(3);
      FindObjectNode *FindObjectNode_SIFT = new FindObjectNode(FeatureExtractor_SIFT);

      string srcFilePATH = contentDirName;
      string destFilePATH = CENTENTFILEPATH + cover.id+ "3.data";
      FindObjectNode_SIFT->loadObjects(srcFilePATH);
      FindObjectNode_SIFT->saveToOneFile(destFilePATH);
      UDEBUG(" %s SIFT computer end...\n",destFilePATH.data());
      oss->UpdateFile(destFilePATH,OSSRELATIVEROOT+"cover/data/"+ cover.id + "3.data");
      cover.data = cover.data+"|"+OSSROOT+"cover/data/"+ cover.id + "3.data";

      delete FindObjectNode_SIFT;
      delete FeatureExtractor_SIFT;
    }

    printf("end...................\n");
}

void getBookNameAndSID(const string &rootPath )
{
  DIR *dir;
  struct dirent *ptr;
  char base[1000];
  char name[256];
  char sid[256];

  book_name.clear();
  series_id.clear();

  printf("rootPath :%s\n",(rootPath+"scan/").c_str());
  
  if ((dir=opendir((rootPath+"scan/").c_str())) == NULL)
  {
      perror("Open dir error...");
      exit(1);
  }

  while ((ptr=readdir(dir)) != NULL)
  {
      printf("d_type :%d\n",ptr->d_type);
      if(strcmp(ptr->d_name,".")==0 || strcmp(ptr->d_name,"..")==0)    ///current dir OR parrent dir
          continue;
      else if(ptr->d_type == 4)    ///dir
      {
      #if 1
          char * p = (ptr->d_name+1); //如果第一个字符是下划线，则文件名有误（无书名）
          memset(name,0,256);
          memset(sid,0,256);
          printf("d_name:%s\n",ptr->d_name);
          while( p != '\0' && p != NULL)
          {
            if(*p == '_' || *p == '-')
            {
               memcpy(name,ptr->d_name,p-ptr->d_name);
               strcpy(sid,p+1);
               printf("bookname= %s and sid = %s \n",name,sid);
               book_name.push_back(name);
               series_id.push_back(sid);
               break;
            }
            p++;
          }
          #else
          book_name.push_back(ptr->d_name);
          #endif
          //files.push_back(ptr->d_name);
      }

  }
  closedir(dir);

  return;
}
#if 0
int getMaxId() //获取数据库中最大的cover id
{
  buffer = "";
  OkHttps *URL = OkHttps::getOkHttps();
  URL->InitOkHttps();
  std::string url = "http://192.168.51.228/search_book";
  std::string request = "get_max_id";
  if(true != URL->POST(url,request,buffer))
  {
    cout << "post false" << endl;
    return -1;
  }
  cout << "res="<<buffer<<endl;
  URL->clearOkHttps();
  return atoi(buffer.c_str());
}
#endif
#if 0
int getDupId(const string &bookName,const string &seriesId)//获取数据库中是否有同名且同系列号的数据，如有则返回书的cover id，否则返回0
{
  buffer = "";
  OkHttps *URL = OkHttps::getOkHttps();
  URL->InitOkHttps();
  std::string url = "http://192.168.51.228/search_book";
  std::string request = "get_dup_id:"+bookName+"_"+seriesId;
  if(true != URL->POST(url,request,buffer))
  {
    cout << "post false" << endl;
    return 0;
  }
  cout << "res="<<buffer<<endl;
  URL->clearOkHttps();
  return atoi(buffer.c_str());
}
#endif

int getDupId(const string &bookName,const string &seriesId) //获取数据库中是否有同名且同系列号的数据，如有则返回书的cover id，否则返回0
{
  char* host = "127.0.0.1";
	char* user = MYSQL_USRNAME;
  char* pwd = MYSQL_PASS;
	char* db_name = MYSQL_DBNAME;
	char sql_buf[256]={0};
	char value[32]={0};
	MYSQL_RES *result;
	MYSQL_ROW row;
	int cover_id = 0;


	g_mySQLConn = mysql_init(NULL); // 初始化数据库连接变量  

  g_mySQLConn = mysql_real_connect(g_mySQLConn, host,  
          user, pwd, db_name, 0, NULL, 0);  
          
  
  if( g_mySQLConn == NULL )  
  {  

      return NULL ;  
  }  
  mysql_query(g_mySQLConn,"SET NAMES UTF8");
  sprintf(sql_buf,"SELECT id from cover WHERE series_id ='%s' and book_name='%s';",seriesId.c_str(),bookName.c_str());
  if(mysql_query(g_mySQLConn, sql_buf))  
  {  

    return NULL;
  } 
  
  result = mysql_store_result(g_mySQLConn);
  if( mysql_num_rows(result) > 0 )
  {
     row = mysql_fetch_row(result);  
     cover_id = atoi(row[0]);
  }
	
	mysql_free_result(result);
	mysql_close(g_mySQLConn);
	return cover_id;
}



int copyFile(const string &srcFile, const string &dstFile)
{
  string srcFileQuote = "'"+srcFile+"'";//加上单引号，避免文件名有转义字符（如空格）的错误
  string dstFileQuote = "'"+dstFile+"'";//加上单引号，避免文件名有转义字符（如空格）的错误
  string command = "cp "+srcFileQuote+" "+dstFileQuote; //目标文件绝对路径
  //cout << "command = "<<command<<endl;
  return system((char*)command.c_str());
}



void SetupAudioData( int index ,const string &audioDir,  Cover& cover ,map<int, map<int,string>>& recordMap)
{
    DIR *dir;
    struct dirent *ptr;
    string contentRecDir =(ROOTPATH+"content/"+"record/"+cover.id+"/");
    if ((dir=opendir(audioDir.c_str())) == NULL)
    {
      perror("Open audioDir error...");
      exit(1);
    }
    while ((ptr=readdir(dir)) != NULL)
    {
      string fileName = ptr->d_name;
      if( fileName == "." || fileName == "..")    //current dir OR parrent dir
      {
          continue;
      }
      else if(ptr->d_type == 8)    //file
      {
        int dotIndex = fileName.find(".");
        string f_name = fileName.substr(0, dotIndex); 
        string f_type =fileName.substr(dotIndex+1);  
        if( f_type =="mp3" || f_type =="wav" )
        {
        
          if( f_name == "封面" || f_name == "book" )
          {
            string dstFileName = to_string(stoi(cover.id)*10)+"."+f_type;
            copyFile(audioDir+fileName,COVERFILEPATH+"record/"+dstFileName);
            oss->UpdateFile(COVERFILEPATH+"record/"+dstFileName,OSSRELATIVEROOT+"cover/record/"+dstFileName);
            cover.record = OSSROOT+"cover/record/"+dstFileName;
          }
          else if ( f_name == "封底" || f_name == "底面" )//暂不用到封底的音频
          {
          }
          else//内容音频
          {
            string dstFileName = to_string((stoi(cover.id)*1000+stoi(f_name))*10+index)+"."+f_type;
            int pageNum = stoi(f_name);
            copyFile(audioDir+fileName,contentRecDir+dstFileName);
            oss->UpdateFile(contentRecDir+dstFileName,OSSRELATIVEROOT+"content/record/"+cover.id+"/"+dstFileName);


            map<int, map<int,string>>::iterator it = recordMap.find(pageNum);
            
            if(it==recordMap.end())//页码内容为空
            {

              map<int,string> record = map<int,string>();
              record.insert(pair<int, string>(index, OSSROOT+"content/record/"+cover.id+"/"+dstFileName));
              recordMap.insert(pair<int, map<int, string>>(pageNum, record));
            }
            else
            {

               map<int,string> record = it->second;

               record.insert(pair<int, string>(index, OSSROOT+"content/record/"+cover.id+"/"+dstFileName));
               recordMap[pageNum] = record;
            }

          }
        }
      }
    }
    closedir(dir);
    

}


void SetupImageData( const string &imageDir,  Cover& cover )
{
    DIR *dir;
    struct dirent *ptr;
    string coverDir = ROOTPATH+"cover/";
    string contentDir = ROOTPATH+"content/";
    string contentImgDir =(contentDir+"image/"+cover.id+"/");
    if ((dir=opendir(imageDir.c_str())) == NULL)
    {
      perror("Open imageDir error...");
      exit(1);
    }
    while ((ptr=readdir(dir)) != NULL)
    {
      string fileName = ptr->d_name;
      if( fileName == "." || fileName == "..")    ///current dir OR parrent dir
      {
          continue;
      }
      else if(ptr->d_type == 8)    //file
      {
        int dotIndex = fileName.find(".");
        string f_name = fileName.substr(0, dotIndex); 
        string f_type =fileName.substr(dotIndex+1);  
        if( f_type =="jpg" || f_type =="jpeg" || f_type =="png"  || f_type =="JPG"  || f_type =="JPEG"  || f_type =="PNG"   )
        {
        
          //img 是算法数据图像
          cv::Mat img = cv::imread((imageDir+ptr->d_name).c_str(), cv::IMREAD_GRAYSCALE);
          cv::resize(img,img,cv::Size(600,img.rows*600/img.cols));

        
          if( f_name == "封面" || f_name == "book"  )
          {
            string ossFileName = to_string (stoi(cover.id)*10)+"."+f_type;
            cv::imwrite((coverDir+"image/"+ossFileName).c_str(), img);
            cv::imwrite((contentImgDir+"1"+"."+f_type).c_str(),img); 
            
            oss->UpdateFile(coverDir+"image/"+ossFileName, OSSRELATIVEROOT+"cover/image/"+ossFileName);
            cover.image = OSSROOT+"cover/image/"+ossFileName;
            
            
          }
          else if( f_name == "封底" ||f_name == "底面"  ) //封底只保存预览图片
          {
            cout<<"====封底"<<endl;
          }
          else //其他默认都是全数字命名的绘本内容图像
          {
            #if 1
            string imageName = "";
            int lineIndex = fileName.find("_");
            int dotIndex = fileName.find(".");
            if(lineIndex == string::npos)
            {
              imageName = to_string(stoi(f_name)*10)+"."+f_type;
            }
            else
            {
              string ten_name = fileName.substr(0, lineIndex); 
              string single_name = fileName.substr(lineIndex+1, dotIndex); 
              imageName =  to_string(stoi(ten_name)*10+stoi(single_name))+"."+f_type;
            }
            

            cv::imwrite((contentImgDir+imageName).c_str(), img);
            #endif
            
          }
          //copy(book_dir+ptr->d_name, contentPicDir+ptr->d_name);
        
        }
          //recordMap.insert(pair<int, string>(stoi(f_name), OSSROOT+"content/text/"+cover.id+"/"+dstFileName));
      }
    }
    closedir(dir);
    

}


void SetupCoverImageData( const string &imageDir,  Cover& cover )
{
    DIR *dir;
    struct dirent *ptr;
    string coverDir = ROOTPATH+"cover/";
    string contentDir = ROOTPATH+"content/";
    string contentImgDir =(contentDir+"image/"+cover.id+"/");
    if ((dir=opendir(imageDir.c_str())) == NULL)
    {
      perror("Open imageDir error...");
      exit(1);
    }
    while ((ptr=readdir(dir)) != NULL)
    {
      string fileName = ptr->d_name;
      if( fileName == "." || fileName == "..")    //current dir OR parrent dir
      {
          continue;
      }
      else if(ptr->d_type == 8)    //file
      {
        int dotIndex = fileName.find(".");
        string f_name = fileName.substr(0, dotIndex); 
        string f_type =fileName.substr(dotIndex+1);  
        if( f_type =="jpg" || f_type =="jpeg" || f_type =="png"  || f_type =="JPG"  || f_type =="JPEG"  || f_type =="PNG"   )
        {
        
          //img 是算法数据图像
          cv::Mat img = cv::imread((imageDir+ptr->d_name).c_str(), cv::IMREAD_GRAYSCALE);
          cv::resize(img,img,cv::Size(600,img.rows*600/img.cols));

        
          if( f_name == "封面" || f_name == "book" )
          {
            string ossFileName = to_string (stoi(cover.id)*10)+"."+f_type;
            cv::imwrite((coverDir+"image/"+ossFileName).c_str(), img);            
            
          }

        
        }

      }
    }
    closedir(dir);
    

}



void SetupCoverData( Cover& cover, std::vector<Content>& contentList )
{
  DIR *dir;
  struct dirent *ptr;
  //char base[1000];
  map<int, map<int,string>> recordMap = map<int, map<int,string>>();
  map<int, string> textMap = map<int, string>();
  //map<int, string> imageMap;
  map<int, string> pictureMap = map<int, string>();
  int maxPageNum = 0;
  string backPicType = "jpg";
  bool haveImageDir = false; 
  string command = "";
  
  OSSClientManager *oss = OSSClientManager::getOSSClientManager();

  if(cover.id == "")
    return;

  contentList.clear();

  string book_dir = ROOTPATH+"scan/"+cover.book_name+"_"+cover.series_id+"/";
    

  string coverDir = ROOTPATH+"cover/";
  string contentDir = ROOTPATH+"content/";
  string contentPicDir =(contentDir+"picture/"+cover.id+"/");
  string contentImgDir =(contentDir+"image/"+cover.id+"/");
  string contentTextDir =(contentDir+"text/"+cover.id+"/");
  string contentRecDir =(contentDir+"record/"+cover.id+"/");




  if(access((book_dir+"image/").c_str() ,0)!=0 )
  {
     mkdir( (book_dir+"image/").c_str(), 0755);
     haveImageDir = false;
  }
  else
  {
     haveImageDir = true;
  }


  if ((dir=opendir(book_dir.c_str())) == NULL)
  {
      perror("Open book_dir error...");
      exit(1);
  }

  while ((ptr=readdir(dir)) != NULL)
  {
      string fileName = ptr->d_name;
      if( fileName == "." || fileName == "..")    ///current dir OR parrent dir
      {
          continue;
      }
      else if(ptr->d_type == 4)    //dir
      {
        if(fileName == "audio")
        {
          //SetupAudioData(0,book_dir+fileName+"/",cover,recordMap);
        }
        else if( fileName.substr(0,5) == "audio")
        {
          cout << "id ==" << stoi(fileName.substr(5))<<endl;
          //SetupAudioData(stoi(fileName.substr(5)),book_dir+fileName+"/",cover,recordMap);
        }
          
      }
      else if(ptr->d_type == 8)    //file
      {
        int dotIndex = fileName.find(".");
        string f_name = fileName.substr(0, dotIndex); 
        string f_type =fileName.substr(dotIndex+1);  
        cout << f_name << f_type<<endl;
        
          
        if( f_type =="jpg" || f_type =="jpeg" || f_type =="png"  || f_type =="JPG"  || f_type =="JPEG"  || f_type =="PNG"   )
        {
        

          if( !haveImageDir )
          {
            copyFile(book_dir+fileName,book_dir+"image/"+fileName);
          }
        

        
        }
        
      }

  }
  closedir(dir);


  SetupCoverImageData((book_dir+"image/"),cover);


}



void SetupData( Cover& cover, std::vector<Content>& contentList )
{
  DIR *dir;
  struct dirent *ptr;
  //char base[1000];
  map<int, map<int,string>> recordMap = map<int, map<int,string>>();
  map<int, string> textMap = map<int, string>();
  //map<int, string> imageMap;
  map<int, string> pictureMap = map<int, string>();
  int maxPageNum = 0;
  string backPicType = "jpg";
  bool haveImageDir = false; 
  string command = "";
  
  OSSClientManager *oss = OSSClientManager::getOSSClientManager();

  if(cover.id == "")
    return;

  contentList.clear();

  string book_dir = ROOTPATH+"scan/"+cover.book_name+"_"+cover.series_id+"/";
    

  string coverDir = ROOTPATH+"cover/";
  string contentDir = ROOTPATH+"content/";
  string contentPicDir =(contentDir+"picture/"+cover.id+"/");
  string contentImgDir =(contentDir+"image/"+cover.id+"/");
  string contentTextDir =(contentDir+"text/"+cover.id+"/");
  string contentRecDir =(contentDir+"record/"+cover.id+"/");

  if(access(coverDir.c_str() ,0)!=0 )
  {
     mkdir( coverDir.c_str(), 0755);
  }
  
  if(access(contentDir.c_str() ,0)!=0 )
  {
     mkdir( contentDir.c_str(), 0755);
  }

  if(access((contentDir+"picture/").c_str() ,0)!=0 )
  {
     mkdir( (contentDir+"picture/").c_str(), 0755);
  }

  if(access((contentDir+"image/").c_str() ,0)!=0 )
  {
     mkdir( (contentDir+"image/").c_str(), 0755);
  }

  
  if(access((contentDir+"text/").c_str() ,0)!=0 )
  {
     mkdir( (contentDir+"text/").c_str(), 0755);
  }
  
  if(access((contentDir+"record/").c_str() ,0)!=0 )
  {
     mkdir( (contentDir+"record/").c_str(), 0755);
  }

  if(access((coverDir+"image/").c_str() ,0)!=0 )
  {
     mkdir( (coverDir+"image/").c_str(), 0755);
  }

  if(access((coverDir+"data/").c_str() ,0)!=0 )
  {
     mkdir( (coverDir+"data/").c_str(), 0755);
  }

  if(access((coverDir+"record/").c_str() ,0)!=0 )
  {
     mkdir( (coverDir+"record/").c_str(), 0755);
  }

  command = "rm -fr "+contentPicDir; 
  system((char*)command.c_str());
  mkdir( contentPicDir.c_str() , 0755);

  command = "rm -fr "+contentImgDir; 
  system((char*)command.c_str());
  mkdir( contentImgDir.c_str() , 0755);
  
  command = "rm -fr "+contentTextDir; 
  system((char*)command.c_str());
  mkdir( contentTextDir.c_str() , 0755);

  command = "rm -fr "+contentRecDir; 
  system((char*)command.c_str());
  mkdir( contentRecDir.c_str() , 0755);  


  if(access((book_dir+"image/").c_str() ,0)!=0 )
  {
     mkdir( (book_dir+"image/").c_str(), 0755);
     haveImageDir = false;
  }
  else
  {
     haveImageDir = true;
  }


  if ((dir=opendir(book_dir.c_str())) == NULL)
  {
      perror("Open book_dir error...");
      exit(1);
  }

  while ((ptr=readdir(dir)) != NULL)
  {
      string fileName = ptr->d_name;
      if( fileName == "." || fileName == "..")    ///current dir OR parrent dir
      {
          continue;
      }
      else if(ptr->d_type == 4)    //dir
      {
        if(fileName == "audio")
        {
          SetupAudioData(0,book_dir+fileName+"/",cover,recordMap);
        }
        else if( fileName.substr(0,5) == "audio")
        {
          cout << "id ==" << stoi(fileName.substr(5))<<endl;
          SetupAudioData(stoi(fileName.substr(5)),book_dir+fileName+"/",cover,recordMap);
        }
          
      }
      else if(ptr->d_type == 8)    //file
      {
        int dotIndex = fileName.find(".");
        string f_name = fileName.substr(0, dotIndex); 
        string f_type =fileName.substr(dotIndex+1);  
        cout << f_name << f_type<<endl;
        
          
        if( f_type =="jpg" || f_type =="jpeg" || f_type =="png"  || f_type =="JPG"  || f_type =="JPEG"  || f_type =="PNG"   )
        {
        
          //img 是算法数据图像，pic是预览图片
          cv::Mat img = cv::imread((book_dir+ptr->d_name).c_str(), cv::IMREAD_GRAYSCALE);
          //cv::resize(img,img,cv::Size(600,img.rows*600/img.cols));
          #if 0
          cv::Mat pic = cv::imread((book_dir+ptr->d_name).c_str(), CV_LOAD_IMAGE_COLOR);
          cv::resize(pic,pic,cv::Size(400,img.rows*400/img.cols));
          #else
          int p[3];
          IplImage *pic = cvLoadImage((book_dir+ptr->d_name).c_str());
          p[0] = CV_IMWRITE_JPEG_QUALITY;
          p[1] = 10;
          p[2] = 0;
          #endif
          if( !haveImageDir )
          {
            copyFile(book_dir+fileName,book_dir+"image/"+fileName);
          }
        
          if( f_name == "封面" || f_name == "book"  )
          {
            //string ossFileName = to_string (stoi(cover.id)*10)+"."+f_type;
            //cv::imwrite((coverDir+"image/"+ossFileName).c_str(), img);
            #if 0
            cv::imwrite((contentPicDir+"0"+"."+f_type).c_str(), pic);
            #else
            cvSaveImage((contentPicDir+"0"+"."+"jpg").c_str(), pic, p);
            #endif
            //cv::imwrite((contentImgDir+"1"+"."+f_type).c_str(),img); 
            
            //oss->UpdateFile(coverDir+"image/"+ossFileName, OSSRELATIVEROOT+"cover/image/"+ossFileName);
            //cover.image = OSSROOT+"cover/image/"+ossFileName;
            
            oss->UpdateFile(contentPicDir+"0"+"."+"jpg", OSSRELATIVEROOT+"content/picture/"+cover.id+"/"+"0"+"."+"jpg");
            pictureMap.insert(pair<int, string>(0, OSSROOT+"content/picture/"+cover.id+"/"+"0"+"."+"jpg"));
            
          }
          else if( f_name == "封底" ||f_name == "底面"  ) //封底只保存预览图片
          {
            #if 0
            cv::imwrite((contentPicDir+"999"+"."+f_type).c_str(), pic);
            backPicType = f_type;
            #else
            cvSaveImage((contentPicDir+"999"+"."+"jpg").c_str(), pic, p);
            backPicType = "jpg";
            #endif
            
            //oss->UpdateFile(contentPicDir+"999"+"."+f_type, OSSRELATIVEROOT+"content/picture/"+cover.id+"/"+"999"+"."+f_type);
            //pictureMap.insert(pair<int, string>(999, OSSROOT+"content/picture/"+cover.id+"/"+"999"+"."+f_type));
          }
          else //其他默认都是全数字命名的绘本内容图像
          {
            //Content content = Content();
            //content.cover_id = cover.id;
            //content.page_id = stoi(f_name);
            //contentList.push_back(content);
            
            if( stoi(f_name) > maxPageNum )
            {
              maxPageNum = stoi(f_name);
            }

            #if 0
            string imageName = "";
            int lineIndex = fileName.find("_");
            int dotIndex = fileName.find(".");
            if(lineIndex == string::npos)
            {
              imageName = to_string(stoi(f_name)*10)+"."+f_type;
            }
            else
            {
              string ten_name = fileName.substr(0, lineIndex); 
              string single_name = fileName.substr(lineIndex+1, dotIndex); 
              imageName =  to_string(stoi(ten_name)*10+stoi(single_name))+"."+f_type;
            }
            
            cout<< "imageName===" << imageName <<endl;

            cv::imwrite((contentImgDir+imageName).c_str(), img);
            #endif
            #if 0
            cv::imwrite((contentPicDir+fileName).c_str(), pic);
            #else
            cvSaveImage((contentPicDir+f_name+".jpg").c_str(), pic, p);
            #endif
            oss->UpdateFile((contentPicDir+f_name+".jpg"), OSSRELATIVEROOT+"content/picture/"+cover.id+"/"+f_name+".jpg");
            
            pictureMap.insert(pair<int, string>(stoi(f_name), OSSROOT+"content/picture/"+cover.id+"/"+f_name+".jpg"));
          }
          //copy(book_dir+ptr->d_name, contentPicDir+ptr->d_name);

          cvReleaseImage(&pic);
        
        }
        else if( f_type =="txt" )
        {

        }
      }

  }
  closedir(dir);

  string backFileName = contentPicDir+to_string(maxPageNum+1)+"."+backPicType;
  rename((contentPicDir+"999."+backPicType).c_str(),backFileName.c_str());//重命名封底为最大页码+1
  oss->UpdateFile(backFileName, OSSRELATIVEROOT+"content/picture/"+cover.id+"/"+to_string(maxPageNum+1)+"."+backPicType);
  pictureMap.insert(pair<int, string>(maxPageNum+1, OSSROOT+"content/picture/"+cover.id+"/"+to_string(maxPageNum+1)+"."+backPicType));


  SetupImageData((book_dir+"image/"),cover);

  makeAlgorithmFileAndUpload(contentImgDir, cover);

  #if 1
  map<int, string>::iterator it =pictureMap.begin() ;
  map<int,string>::iterator it_recordList;
  map<int,string>::iterator it_text;
  map<int, map<int,string>>::iterator it_record;
  bool first_time = true;
  int page_count = 0;

  while(it != pictureMap.end())
  {
     it_text = textMap.begin();
     it_record = recordMap.begin();
     cout <<"page"<<it->first<<endl;
     if( it->first == 0 ) //封面
     {
       cover.picture = it->second;
       it++;
       continue;
     }
     Content content = Content();
     content.cover_id = cover.id;
     content.page_id = to_string(it->first);
     content.picture = it->second;
     page_count++;
     
     while( it_text != textMap.end() )
     {
       if( it->first == it_text->first )
       {
         content.text = it_text->second;
       }
       it_text++;
     }

     while( it_record != recordMap.end() )
     {
       if( it->first == it_record->first )
       {
         it_recordList = (it_record->second).begin();
         first_time = true;
         while( it_recordList != (it_record->second).end() )
         {
           if( first_time )
           {
             content.record = it_recordList->second;
             first_time = false;
           }
           else
           {
             content.record = content.record+"|"+it_recordList->second;
           }
           it_recordList++;
         }
       }
       it_record++;
     }

     contentList.push_back(content);
     it ++;         
  }
  
  cover.page_num = to_string(page_count);

#endif
}



