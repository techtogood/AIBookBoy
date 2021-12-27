#include <map>  
#include <list>
#include <algorithm> 
#include<json/json.h>  
#include<iostream>  

#include <assert.h>
#include <opencv2/core/core.hpp>
//#include "include/FindObject.h"

#include "include/ObjSignature.h"
#include "include/Vocabulary.h"
//#include "include/settings.h"
//#include "include/ULogger.h
#include "include/redis.h"
#include "include/FindObjectNode.h"
#include <stdio.h>  
#include <sys/time.h> 



using namespace std;
using namespace cv;
using namespace my_find_object;

void FormatJson();



//find_object::FindObject*findObject =NULL;
//list<int> idsLoaded;

extern "C"
{
#if 0
    int FindObjectInit() {

        char buf[128]={0};

		printf(" enter FindObjectInit \n");

	    findObject= new find_object::FindObject(true);

		//UINFO("======LoadObject object %d x %d  ",h,w);

		//Mat imgData(h, w, CV_8UC1, (unsigned char*)cbuf);

		cv::Mat imgData ;
		
		for( int i = 1; i<= 4; i++)
		{
			sprintf(buf,"/home/xiaojuan/res/t%d.png",i);
			imgData = cv::imread(buf, cv::IMREAD_GRAYSCALE);
		
			printf("======LoadObject object id =%d  %d x %d  \n",i,imgData.cols, imgData.rows);



			const ObjSignature * s = findObject->addObject(imgData, i, "");
			if(s)
			{
				printf("Added object %d \n", s->id());
				idsLoaded.push_back(s->id());
			}
		}

		if(idsLoaded.size())
	    {
	        printf("idsLoaded=%d objects=%d \n", idsLoaded.size(),findObject->objects().size());
	        findObject->updateObjects(idsLoaded);
	        findObject->updateVocabulary(idsLoaded);
	    }
	    return 0;
	}

    int LoadObject() {

		char buf[128]={0};

		//UINFO("======LoadObject object %d x %d  ",h,w);


		//Mat imgData(h, w, CV_8UC1, (unsigned char*)cbuf);

		cv::Mat imgData ;
		
		for( int i = 0; i< 4; i++)
		{
			sprintf(buf,"/home/xiaojuan/res/t%d.png",i);
			imgData = cv::imread(buf, cv::IMREAD_GRAYSCALE);
		
			UINFO("======LoadObject object id =%d  %d x %d  ",i,imgData.cols, imgData.rows);



			const ObjSignature * s = findObject->addObject(imgData, i, "");
			if(s)
			{
				UINFO("Added object %d ", s->id());
				idsLoaded.push_back(s->id());
			}
		}

		if(idsLoaded.size())
	    {
	        UINFO("idsLoaded=%d objects=%d ", idsLoaded.size(),findObject->objects().size());
	        findObject->updateObjects(idsLoaded);
	        findObject->updateVocabulary(idsLoaded);
	    }



		return 0;
	}


	int UpdateObject() {

	    if(idsLoaded.size())
	    {
	        UINFO("idsLoaded=%d objects=%d ", idsLoaded.size(),findObject->objects().size());
	        findObject->updateObjects(idsLoaded);
	        findObject->updateVocabulary(idsLoaded);
	    }
		
	    return 0;
	}




	int FindObject(char cbuf[]) {
	

		//UINFO("======FindObject object %d x %d	",h,w);

        printf("FindObject cbuf[100]=%d \n",cbuf[100]);
		cv::Mat imgData = cv::Mat(480+240,640, CV_8UC1,cbuf);
		//imgData.put(0, 0, cbuf);

		//Mat imgData=(*((Mat*)mat));

		//Mat dst;
	    //cvtColor(imgData,dst,COLOR_RGB2BGR);

		//cv::Mat imgData = cv::imread("/data/data/ai.deepconv.xbot.findobject/files/target.jpg", cv::IMREAD_GRAYSCALE);
		//cv::Mat imgData = (cv::Mat )(*mat);

	    return findObject->detect(imgData);


	}
	#endif

	FindObjectNode *findObject = NULL;
	

	int FindObject(char cbuf[]) {
	

		//UINFO("======FindObject object %d x %d	",h,w);
    unsigned short height = (((unsigned short)cbuf[1])<<8)|((unsigned short)(cbuf[0]&0x00ff));
    unsigned short width = (((unsigned short)cbuf[3])<<8)|((unsigned short)(cbuf[2]&0x00ff));
    printf("width=%d height=%d \n",width,height);
#if 0
    FILE *pFile;

    pFile = fopen("/home/xiaojuan/mat", "w+");
    if( pFile != NULL )
    {

    for(int i = 0; i< 640*480;i++ )
    {
       fprintf(pFile,"%d, ",cbuf[i+4]);
       if( i !=0 && i%50==0)
       {
         fprintf(pFile,"\n");
       }
    }
    fclose(pFile);
    }
 #endif  
		cv::Mat imgData = cv::Mat(height,width, CV_8UC1,cbuf+4);
		//imgData.put(0, 0, cbuf);

		//Mat imgData=(*((Mat*)mat));

		//Mat dst;
	    //cvtColor(imgData,dst,COLOR_RGB2BGR);

		//cv::Mat imgData = cv::imread("/data/data/ai.deepconv.xbot.findobject/files/target.jpg", cv::IMREAD_GRAYSCALE);
		//cv::Mat imgData = (cv::Mat )(*mat);

	  return findObject->detect(imgData);

	}

	int SaveCover(char cbuf[],int id) {

    char file_name[256]={0};

    printf("cbuf length =%d \n",sizeof(cbuf));

    sprintf(file_name,"/home/xiaojuan/res/data/book/cover/image/%d.jpg",id);
    cv::Mat img = cv::Mat(400,480, CV_8UC1,cbuf);
    cv::imwrite(file_name,img);

	}

	int SaveContent(char cbuf[],int book_id,int page_id) {

    char file_name[256]={0};

    sprintf(file_name,"/home/xiaojuan/res/data/book/content/%d/%d.jpg",book_id,page_id);
    cv::Mat img = cv::Mat(400,480, CV_8UC1,cbuf);
    cv::imwrite(file_name,img);
	}
	
MYSQL *g_mySQLConn = NULL;
MYSQL *g_mySQLConn_2 = NULL;


long getCurrentTime()  
{  
   struct timeval tv;  
   gettimeofday(&tv,NULL);  
   return tv.tv_sec * 1000 + tv.tv_usec / 1000;  
} 

int getDataByID( int id, char*res )
{
  Redis *r = new Redis();
  if(!r->connect("127.0.0.1", 6379))
  {
    printf("redis connect error!\n");
    delete r;
    return 1;
  }

  stringstream stream;  
  stream<<id;  
  std::string s_id =stream.str(); 
  
  strcpy(res, (char*)( (r->get( s_id )).c_str()));

  //std::cout<<res<<endl;

  return 0;

}


int FormatBookDataToJSON( int id,char*database,char*res )
{
  Json::Value root;
  Json::Value arrayObj;
  Json::Value item;
  char* host = "127.0.0.1";
  char* user = "root";
  char* pwd = "Dc123456";
  //char* db_name = "picture_book";
  char sql_buf[256]={0};
  char value[32]={0};
  MYSQL_RES *result =NULL;
  MYSQL_ROW row;
  int objectID = 0;
  MYSQL *t_mySQLConn  = mysql_init(NULL); // 初始化数据库连接变量  

  t_mySQLConn = mysql_real_connect(t_mySQLConn, host,  
        user, pwd, database, 0, NULL, 0);  
          

  if(t_mySQLConn == NULL)  
  {  
      printf("MySQL Error: %d \n",mysql_error(t_mySQLConn));
      return 1 ;  
  }  
  mysql_query(t_mySQLConn,"SET NAMES UTF8");
  sprintf(sql_buf,"SELECT id,book_name,record,image,feature_type,data from cover WHERE id=%d;",id);
  if(mysql_query(t_mySQLConn, sql_buf))  
  {  
    printf("mysql Query Error22:%d \n",mysql_error(t_mySQLConn));
    mysql_close(t_mySQLConn);
    return 1;
  }  
  result = mysql_store_result(t_mySQLConn);

  if( mysql_num_rows(result) <=0 )
  {
     printf("no database id = %d \n",id);
     mysql_free_result(result);
     mysql_close(t_mySQLConn);
     return 1;
  }

  row = mysql_fetch_row(result);
  //printf("row[0]=%s row[1]=%s row[2]=%s row[3]=%s row[4]=%s row[5]=%s \n",row[0],row[1],row[2],row[3],row[4],row[5]);
  root["id"] = row[0];
  root["name"] = row[1];
  root["record"] = row[2];
  root["image"] = row[3];
  root["type"] = row[4];
  root["data"] = row[5];

  mysql_free_result(result);  

  memset(sql_buf,0,256);


//不传递text
#if 0
  sprintf(sql_buf,"SELECT text,record from content WHERE cover_id =%d;",id);
#else
  sprintf(sql_buf,"SELECT record from content WHERE cover_id =%d;",id);
#endif
  if(mysql_query(t_mySQLConn, sql_buf))  
  {  
    printf("mysql Query Error33:%d \n",mysql_error(t_mySQLConn));
    mysql_close(t_mySQLConn);
    return 1;
  } 
  //printf("223443 \n");
  result = mysql_store_result(t_mySQLConn);
  
  if( mysql_num_rows(result) <=0 )
  {
     printf("no database id 222= %d \n",id);
     mysql_free_result(result);
     mysql_close(t_mySQLConn);
     return 1;
  }
  
  sprintf(value,"%d",mysql_num_rows(result));
  root["num"] = value ;
  //printf("num =%s \n", value);
  for(int i=0; i < mysql_num_rows(result); ++i) 
  {  
    // 获取下一行  
    row = mysql_fetch_row(result);  
    if(row <= 0 )  
    {  
        break;  
    }  
    //cout<<row[0]<<row[1]<<row[2]<<endl;
    //不传递text
      #if 0
      item["text"] = row[0];
      item["record"] = row[1];
      #else
      item["text"] = "";
      item["record"] = row[0];
      #endif
      arrayObj.append(item);
  }  
  root["content"] = arrayObj;


  root.toStyledString();
  std::string out = root.toStyledString();
  std::cout << out << std::endl;
  printf("out len==%d\n", strlen(out.c_str()));
  strcpy(res,out.c_str());

  mysql_free_result(result);
  mysql_close(t_mySQLConn);
  
  return 0 ;

}

	int getMaxID()
	{
	  char* host = "127.0.0.1";
		char* user = "root";
		char* pwd = "Dc123456";
		char* db_name = "picture_book";
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
        printf("MySQL Error: %d \n",mysql_error(g_mySQLConn));
        return NULL ;  
    }  
	  mysql_query(g_mySQLConn,"SET NAMES UTF8");
		sprintf(sql_buf,"select id from cover order by id desc limit 1");
		if(mysql_query(g_mySQLConn, sql_buf))  
		{  
			printf("mysql Query Error22:%d \n",mysql_error(g_mySQLConn));
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

	
int getDupID(const char* bookName, const char*seriesId)
{
  char* host = "127.0.0.1";
	char* user = "root";
	char* pwd = "Dc123456";
	char* db_name = "picture_book";
	char sql_buf[256]={0};
	char value[32]={0};
	MYSQL_RES *result;
	MYSQL_ROW row;
	int cover_id = 0;

	printf("bookName=%s  seriesId=%s \n",bookName,seriesId);
	g_mySQLConn = mysql_init(NULL); // 初始化数据库连接变量  

  g_mySQLConn = mysql_real_connect(g_mySQLConn, host,  
          user, pwd, db_name, 0, NULL, 0);  
          
  
  if( g_mySQLConn == NULL )  
  {  
      printf("MySQL Error: %d \n",mysql_error(g_mySQLConn));
      return NULL ;  
  }  
  mysql_query(g_mySQLConn,"SET NAMES UTF8");
  sprintf(sql_buf,"SELECT id from cover WHERE series_id ='%s' and book_name='%s';",seriesId,bookName);
  if(mysql_query(g_mySQLConn, sql_buf))  
  {  
    printf("mysql Query Error33:%d \n",mysql_error(g_mySQLConn));
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
	    //printf(" cur =%d ",*cur_p);
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



int ParseBookDataToDatabase(const char* json )
{
		Json::Value root;
		Json::Value arrayObj;
		Json::Value item;
		Json::Reader reader;  
		char* host = "127.0.0.1";
		char* user = "root";
		char* pwd = "Dc123456";
		char* db_name = "picture_book";
		char sql_buf[1024] = {0};
		char value[32]={0};
		MYSQL_RES *result;
		MYSQL_ROW row;
		int cover_id = 0;
		
		g_mySQLConn = mysql_init(NULL); // 初始化数据库连接变量  

    g_mySQLConn = mysql_real_connect(g_mySQLConn, host,  
            user, pwd, db_name, 0, NULL, 0);  
	            
    if(g_mySQLConn == NULL)  
    {  
        printf("MySQL Error: %d \n",mysql_error(g_mySQLConn));
        return NULL ;  
    }  


	  g_mySQLConn_2 = mysql_init(NULL); // 初始化数据库连接变量  

    g_mySQLConn_2 = mysql_real_connect(g_mySQLConn_2, host,  
            user, pwd, "robot", 0, NULL, 0);  
	            
    if(g_mySQLConn_2 == NULL)  
    {  
        printf("MySQL Error: %d \n",mysql_error(g_mySQLConn_2));
        return NULL ;  
    }  
    
	  mysql_query(g_mySQLConn,"SET NAMES UTF8");
	  mysql_query(g_mySQLConn_2,"SET NAMES UTF8");

    if (reader.parse(json, root))  
    {  
      Json::Value cover = root["cover"];  
      Json::Value arrayObj= root["content"];  
      cout<<cover["record"].asString()<<endl;
      cout << "array size = " << arrayObj.size() << endl;

      // 更新机器人绘本数据
      sprintf(sql_buf,"SELECT id from cover WHERE series_id ='%s' and book_name='%s';",cover["series_id"].asString().c_str(),cover["book_name"].asString().c_str());
      printf("sql=%s \n",sql_buf);
  		if(mysql_query(g_mySQLConn, sql_buf)) //查询是否有相同条目
  		{  
  			printf("mysql Query Error33:%d \n",mysql_error(g_mySQLConn));
  			return NULL;
  		} 
  		
  		result = mysql_store_result(g_mySQLConn);
  		printf("00001111 =%d\n",mysql_num_rows(result));
  		if( mysql_num_rows(result) > 0 ) //如有相同条目
  		{
        row = mysql_fetch_row(result);  
        cover_id = atoi(row[0]);
        printf("1111=%d \n",cover_id);
        sprintf(sql_buf,"UPDATE cover SET series_id='%s', book_name='%s' ,record='%s',image='%s',feature_type='%s',data='%s',page_num='%s' WHERE id='%d';"\
                     ,cover["series_id"].asString().c_str(),cover["book_name"].asString().c_str(),cover["record"].asString().c_str(),cover["image"].asString().c_str(),\
                     cover["feature_type"].asString().c_str(),cover["data"].asString().c_str(),cover["page_num"].asString().c_str(),cover_id);
        printf("2211sql=%s \n",sql_buf);
        if(mysql_query(g_mySQLConn, sql_buf))  
    		{  
    			printf("mysql Query Error3344:%d \n",mysql_error(g_mySQLConn));
    			return -1;
    		} 
    		
        // 更新手机APP绘本数据
    		sprintf(sql_buf,"UPDATE book_volume SET title='%s', cover='%s' ,number='%s', count='%s' WHERE id='%d';",cover["book_name"].asString().c_str(),cover["picture"].asString().c_str(),cover["series_id"].asString().c_str(),cover["page_num"].asString().c_str(),cover_id);
        printf("app sql=%s \n",sql_buf);
    		if(mysql_query(g_mySQLConn_2, sql_buf))  
    		{  
    			printf("mysql Query Error33:%d \n",mysql_error(g_mySQLConn_2));
    			return NULL;
    		}

    		//APP把绘本封面作为第0页更新到内容表中
        sprintf(sql_buf,"UPDATE book_content SET title='%s', attach='%s' ,image='%s' WHERE volume_id ='%d' and page='%s';",cover["book_name"].asString().c_str(),cover["record"].asString().c_str(),cover["picture"].asString().c_str(),cover_id,"0");
        printf("app2 sql=%s \n",sql_buf);
    		if(mysql_query(g_mySQLConn_2, sql_buf))  
    		{  
    			printf("mysql Query Error33:%d \n",mysql_error(g_mySQLConn_2));
    			return NULL;
    		}
    		
  		}
  		else //如果没有相同条目
  		{
  		  cover_id = atoi(cover["id"].asString().c_str());

  		  printf("2222=%d\n",cover_id);
  		     
        sprintf(sql_buf,"INSERT INTO cover (id, series_id, book_name, record,image,feature_type,data,page_num) VALUES ('%s','%s','%s','%s','%s','%s','%s','%s');"\
                     ,cover["id"].asString().c_str(),cover["series_id"].asString().c_str(),cover["book_name"].asString().c_str(),cover["record"].asString().c_str()\
                     ,cover["image"].asString().c_str(),cover["feature_type"].asString().c_str(),cover["data"].asString().c_str(),cover["page_num"].asString().c_str());
        printf("333sql=%s \n",sql_buf);
                     
        if(mysql_query(g_mySQLConn, sql_buf))  
    		{  
    			printf("mysql Query Error44:%d \n",mysql_error(g_mySQLConn));
    			return -1;
    		} 


    		sprintf(sql_buf,"INSERT INTO book_volume (id, title, cover, number,count) VALUES ('%s','%s','%s','%s','%s');"\
                     ,cover["id"].asString().c_str(),cover["book_name"].asString().c_str(),cover["picture"].asString().c_str()\
                     ,cover["series_id"].asString().c_str(),cover["page_num"].asString().c_str());
                     
        printf("app sql4 =%s \n",sql_buf);
        if(mysql_query(g_mySQLConn_2, sql_buf))  
    		{  
    			printf("mysql Query Error app 44:%d \n",mysql_error(g_mySQLConn_2));
    			return -1;
    		}

    		sprintf(sql_buf,"INSERT INTO book_content (volume_id, title, attach, image,page) VALUES ('%s','%s','%s','%s','%s');"\
                     ,cover["id"].asString().c_str(),cover["book_name"].asString().c_str(),cover["record"].asString().c_str()\
                     ,cover["picture"].asString().c_str(),"0");
                     
        printf("app sql5 =%s \n",sql_buf);
        if(mysql_query(g_mySQLConn_2, sql_buf))  
    		{  
    			printf("mysql Query Error44:%d \n",mysql_error(g_mySQLConn_2));
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
          printf("mysql Query Error55:%d \n",mysql_error(g_mySQLConn));
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
          printf("UPDATE content =%s \n",sql_buf);
          if(mysql_query(g_mySQLConn, sql_buf))  
          {  
            printf("mysql Query Error5566:%d \n",mysql_error(g_mySQLConn));
            return -1;
          } 
        }
        else
        {
          sprintf(sql_buf,"INSERT INTO content (cover_id, page_Id, text, record) VALUES ('%d','%s','%s','%s')"\
                       ,cover_id,content["page_id"].asString().c_str(),/*content["text"].asString().c_str()*/text.c_str(),content["record"].asString().c_str());
          printf("INSERT INTO content =%s \n",sql_buf);
          if(mysql_query(g_mySQLConn, sql_buf))  
          {  
            printf("mysql Query Error66:%d \n",mysql_error(g_mySQLConn));
            return -1;
          } 
        }

        // app
        sprintf(sql_buf,"SELECT id from book_content WHERE volume_id ='%d' and page ='%s'",cover_id,content["page_id"].asString().c_str());
        if(mysql_query(g_mySQLConn_2, sql_buf))  
        {  
          printf("mysql Query Error55:%d \n",mysql_error(g_mySQLConn));
          return -1;
        } 
        result = mysql_store_result(g_mySQLConn_2);
        if( mysql_num_rows(result) > 0 )
        {
          sprintf(sql_buf,"UPDATE book_content SET title='%s', attach='%s', image='%s',content='%s' WHERE volume_id='%d' and page='%s';"\
                       ,cover["book_name"].asString().c_str(),content["record"].asString().c_str(), content["picture"].asString().c_str(),text.c_str(),cover_id, content["page_id"].asString().c_str());
                       
          printf("UPDATE book_content =%s \n",sql_buf);
          if(mysql_query(g_mySQLConn_2, sql_buf))  
          {  
            printf("mysql Query Error556611:%d \n",mysql_error(g_mySQLConn_2));
            return -1;
          } 
        }
        else
        {
        
          sprintf(sql_buf,"INSERT INTO book_content (volume_id, title, attach, image,page,content) VALUES ('%d','%s','%s','%s','%s','%s')"\
                       ,cover_id,cover["book_name"].asString().c_str(),content["record"].asString().c_str(),content["picture"].asString().c_str(),content["page_id"].asString().c_str(),text.c_str());
          if(mysql_query(g_mySQLConn_2, sql_buf))  
          {  
            printf("mysql Query Error66:%d \n",mysql_error(g_mySQLConn_2));
            return -1;
          } 
        }
      }
    }  

    mysql_free_result(result);
		mysql_close(g_mySQLConn);

		mysql_close(g_mySQLConn_2);

    return 0;


}

	
int main_init()
{
  //初始化Setting
  my_find_object::Setting::init();

  //特征提取类
  my_find_object::FeatureExtractor *featureNode = new my_find_object::FeatureExtractor();

  //主处理类  处理图片和词典
  findObject = new FindObjectNode(featureNode);

  //FormatBookDataToJSON(1);
  return 0;
}

}


