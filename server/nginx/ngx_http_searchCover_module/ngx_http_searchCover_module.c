#include <ngx_config.h>      
#include <ngx_core.h>      
#include <ngx_http.h>      
#include <stdio.h>  
#include <sys/time.h>   
    
 static ngx_int_t ngx_http_searchCover_handler(ngx_http_request_t *r);      
 static char *    ngx_http_searchCover(ngx_conf_t *cf, ngx_command_t *cmd, void *conf);    
 int main_init();
 long getCurrentTime() ;
 int FormatBookDataToJSON( int id,char*database,char*res );
 int FindObject(char cbuf[]) ;

    
//定义模块配置文件的处理     
static ngx_command_t ngx_http_searchCover_commands[] = {      
     {  //配置项名称    
         ngx_string("searchCover"),     
    //配置项类型，即定义他可以出现的位置     
         NGX_HTTP_MAIN_CONF|NGX_HTTP_SRV_CONF|NGX_HTTP_LOC_CONF|NGX_HTTP_LMT_CONF|NGX_CONF_NOARGS,      
        //处理配置项参数的函数，函数在下面定义     
    ngx_http_searchCover,      
    //在配置文件中的偏移量    
         NGX_HTTP_LOC_CONF_OFFSET,    
    //预设的解析方法配置项      
         0,      
    //配置项读取后的处理方法    
         NULL      
     },      
     //command数组要以ngx_null_command结束    
     //#define ngx_null_command {ngx_null_string,0,NULL,0,0,NULL}    
     ngx_null_command      
 };      
    
    
 //searchCover模块上下文,都为NULL即是说在http框架初始化时没有什么要做      
 static ngx_http_module_t ngx_http_searchCover_module_ctx = {  
     NULL,  //preconfiguration    
     NULL,  //postconfiguration    
     NULL,  //create main configuration    
     NULL,  //init main configuration    
     NULL,  //create server configuration    
     NULL,  //merge server configuration    
     NULL,  //create location configuration    
     NULL  //merge location configuration    
 };      
 //对自己searchCover模块的定义，在编译时加入到全局的ngx_modules数组中，这样在Nginx初始化时会调用模块的所有初始化方法，（上面的ngx_http_module_t类型的ngx_http_searchCover_module_ctx）    
      
 ngx_module_t ngx_http_searchCover_module = {      
     NGX_MODULE_V1, //由Nginx定义的宏来初始化前七个成员     
     &ngx_http_searchCover_module_ctx,  //模块的上下文结构体，指向特定模块的公共方法    
     ngx_http_searchCover_commands,  //处理配置项的结构体数组    
     NGX_HTTP_MODULE,  //模块类型    
    //Nginx在启动停止过程中七个执行点的函数指针    
     NULL,      
     NULL,      
     NULL,      
     NULL,      
     NULL,      
     NULL,      
     NULL,      
    
     NGX_MODULE_V1_PADDING  //由Nginx定义的宏定义剩下的8个保留字段    
 };      


       
 //配置项对应的回调函数，当配置项中出现searchCover配置项时将调用这个函数      
 static char *  ngx_http_searchCover(ngx_conf_t *cf, ngx_command_t *cmd, void *conf)      
{   //ckcf并不是指特定的location块内的数据结构，他可以是mian、srv、loc级别的配置项    
	//每个http{},sever{},location{}都有一个ngx_http_core_loc_conf_t类型的数据结构    
	ngx_http_core_loc_conf_t *clcf;      
	
	main_init();
	//FindObjectInit();
	//找到searchCover配置项所在的配置块    
	clcf = ngx_http_conf_get_module_loc_conf(cf, ngx_http_core_module);      

	//http框架在处理用户请求进行到NGX_HTTP_CONTENT_PHASE阶段是，如果请求的主机名，URI与配置项所在的配置块相匹配时，就调用    
	//clcf中的handle方法处理这个请求    
	//NGX_HTTP_CONTENT_PHASE用于处理http请求内容的阶段，这是大部分http模块通常介入的阶段    
	clcf->handler = ngx_http_searchCover_handler;      

	return NGX_CONF_OK;      
 }      




 static void  ngx_http_read_post_body(ngx_http_request_t *r)
{
    //size_t len = 0;
    ngx_chain_t* bufs = r->request_body->bufs;
    ngx_buf_t* buf = NULL;
    int8_t* data_buf = NULL;
    size_t content_length = 0;
    size_t body_length = 0;
    char res[65536] = {0};  //max response is  65536
    int id = -1;

    ngx_int_t rc = ngx_http_discard_request_body(r);      
    if (rc != NGX_OK) {      
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0, "ngx_http_discard_request_body fail ");
        return ;      
    }      
    
    if ( r->headers_in.content_length == NULL )
    {   
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0, "r->headers_in.content_length == NULL");
        return;
    }   
                
    // malloc space for data_buf
    content_length = atoi( (char*)(r->headers_in.content_length->value.data) );

    data_buf = ( int8_t* )ngx_palloc( r->pool , content_length + 1 );
    size_t buf_length = 0;
    while ( bufs )
    {
        buf = bufs->buf;
        bufs = bufs->next;
        buf_length = buf->last - buf->pos ;
        if( body_length + buf_length > content_length )
        {
            memcpy( data_buf + body_length, buf->pos, content_length - body_length);
            body_length = content_length ;
            break;
        }
        memcpy( data_buf + body_length, buf->pos, buf->last - buf->pos );
        body_length += buf->last - buf->pos;
    }
    
    if ( body_length > 0 )
    {
      data_buf[body_length] = 0;
    }
    


      id = FindObject(data_buf);


      if( id < 1 )
      {
        strcpy(res, "0");
      }
      else
      {
        if( id > 10 ) //封面图片id都大于10，个位数为子id
        {
          //id（10进制）最后一位是 objcet id，除10获取 book id
          #if 0 
          if( FormatBookDataToJSON(id/10,"picture_book",res ) != 0 )  //mysql
          {
            strcpy(res, "0");
          }
          #else
          if( getDataByID(id/10, res ) != 0 )  //redis
          {
            strcpy(res, "0");
          }
          #endif
        }
        else
        {
          strcpy(res, "0");
        }
      }

    ngx_pfree( r->pool,data_buf);

    
    //构造响应头部    
      ngx_str_t type = ngx_string("application/json");      
      ngx_str_t response;
      response.len = strlen(res);
      response.data = (unsigned char*)res;
      r->headers_out.status = NGX_HTTP_OK;      
      r->headers_out.content_length_n = response.len;      
      r->headers_out.content_type = type;      
    //发送http头部，其中也包括响应行    
      rc = ngx_http_send_header(r);      
      if (rc == NGX_ERROR || rc > NGX_OK || r->header_only) {      
          return;      
      }      
        
     ngx_buf_t *b;      
     //根据请求中传来的内存池对象，创建内存buf    
     b = ngx_create_temp_buf(r->pool, response.len);      
      if (b == NULL) {      
          return ;      
      }      
    //有效内容从pos位置开始，复制respon的内容    
      ngx_memcpy(b->pos, response.data, response.len);  

      
      //有效内容到last结束    
      b->last = b->pos + response.len;      
      //因为ngx_buf_t可以由ngx_chain_t链表链起来，last_buf可以标记这是最后一块待处理的缓冲区，简化处理    
      b->last_buf = 1;      
   //将内存buf用链表链起来，作为ngx_http_output_filter的跌入个参数    
      ngx_chain_t out;      
      out.buf = b;      
    //标记这是最后一个ngx_chain_t    
      out.next = NULL;      
        
      ngx_http_output_filter(r, &out);      
      return;
    
}


   
  static ngx_int_t ngx_http_searchCover_handler(ngx_http_request_t *r)      
  {    

    if (!(r->method&NGX_HTTP_POST))//只处理POST请求
    {
    return NGX_HTTP_NOT_ALLOWED;
    }
    //请求不带任何参数情况下直接回复不含响应体的报文
    if (NULL == r->headers_in.content_length || 0 == atoi((const char *)r->headers_in.content_length->value.data))
    {
      r->headers_out.content_length_n = 0;
      r->headers_out.status = NGX_HTTP_OK;
      r->header_only = 1;
      return ngx_http_send_header(r);
    }


    ngx_int_t rc = ngx_http_read_client_request_body(r,ngx_http_read_post_body);//设置读取完请求体后的回调处理函数


    if (rc == NGX_ERROR || rc >= NGX_HTTP_SPECIAL_RESPONSE) {
      return rc;
    }
    return NGX_OK;
	}
