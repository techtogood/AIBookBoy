#include "../include/ossclientmanager.h"

namespace my_find_object {


OSSClientManager* OSSClientManager::m_oss = new OSSClientManager();


/*************************************
 *
 *
 *
 *
 *
 ************************************/
OSSClientManager *OSSClientManager::getOSSClientManager()
{
  if(m_oss == NULL)
  {
    m_oss = new OSSClientManager();
  }
  return m_oss;
}


/*************************************
 *
 *
 *
 ************************************/
OSSClientManager::~OSSClientManager()
{
  aos_pool_destroy(pool);
  //程序结束前，调用aos_http_io_deinitialize方法释放之前分配的全局资源
  aos_http_io_deinitialize();
}

void OSSClientManager::destroy()
{
  if(m_oss != NULL)
  {
    delete m_oss;
    m_oss = NULL;
  }
}

/*************************************
 *
 *
 *
 *
 *
 ************************************/
bool OSSClientManager::UpdateFile(const std::string &srcPATH, const std::string &destPATH)
{
  //return false; // for testing, dont always upload file;
  
  printf("UpdateFile=%s -> %s \n",srcPATH.c_str(),destPATH.c_str());
  if(srcPATH.empty() || destPATH.empty())
  {
    return false;
  }
  aos_string_t bucket;
  aos_string_t object;
  aos_table_t *headers = NULL;
  aos_table_t *resp_headers = NULL;
  aos_status_t *s = NULL;
  aos_string_t file;

  headers = aos_table_make(options->pool, 1);
  aos_str_set(&bucket, OSS_BUCKET);
  aos_str_set(&object, destPATH.c_str());
  aos_str_set(&file, srcPATH.c_str());
  s = oss_put_object_from_file(options, &bucket, &object, &file,headers, &resp_headers);
  if (aos_status_is_ok(s)) {
      printf("put object from file succeeded\n");
      return true;
  } else {
      printf("put object from file failed, code:%d, error_code:%s, error_msg:%s, request_id:%s\n",
          s->code, s->error_code, s->error_msg, s->req_id);
      return false;
  }
  return false;
}



/*************************************
 *
 *
 *
 ************************************/
bool OSSClientManager::DeleteFile(const std::string &filePATH)
{
  aos_string_t bucket;
  aos_string_t object;
  aos_table_t *resp_headers = NULL;
  aos_status_t *s = NULL;

  aos_str_set(&bucket, OSS_BUCKET);
  aos_str_set(&object, filePATH.c_str());
  /* 删除文件 */
  s = oss_delete_object(options, &bucket, &object, &resp_headers);
  /* 判断是否删除成功 */
  if (aos_status_is_ok(s)) {
      printf("delete object succeed\n");
      return true;
  } else {
      printf("delete object failed\n");
      return false;
  }
  return false;
}


/*************************************
 *
 *
 *
 ************************************/
OSSClientManager::OSSClientManager()
{
  //程序入口调用aos_http_io_initialize方法，这个方法内部会做一些全局资源的初始化，涉及网络，内存等部分
  aos_http_io_initialize(NULL, 0);
  aos_pool_create(&pool,NULL);

  options = oss_request_options_create(pool);
  options->config = oss_config_create(options->pool);
  options->config->is_cname = 0;
  options->ctl = aos_http_controller_create(options->pool, 0);
  aos_str_set(&options->config->endpoint, OSS_ENDPIONT);
  aos_str_set(&options->config->access_key_id, OSS_ID);
  aos_str_set(&options->config->access_key_secret, OSS_SECRET);

}
}
