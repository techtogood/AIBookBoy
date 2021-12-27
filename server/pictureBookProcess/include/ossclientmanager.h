#ifndef OSSCLIENTMANAGER_H
#define OSSCLIENTMANAGER_H
#include <oss_c_sdk/aos_log.h>
#include <oss_c_sdk/aos_util.h>
#include <oss_c_sdk/aos_string.h>
#include <oss_c_sdk/aos_status.h>
#include <oss_c_sdk/oss_auth.h>
#include <oss_c_sdk/oss_util.h>
#include <oss_c_sdk/oss_api.h>
#include <string>


#define OSS_BUCKET "xxxx"  //bucket name 
#define OSS_ENDPIONT "http://oss-cn-shanghai.aliyuncs.com" //oss endpoint
#define OSS_ID "xxxxxxxxxxxxxxxx"  //oss access id
#define OSS_SECRET "yyyyyyyyyyyyyyyy" //oss access secret


namespace my_find_object {

class OSSClientManager
{
public:
  static OSSClientManager * getOSSClientManager();
  ~OSSClientManager();
  static void destroy();

  bool UpdateFile(const std::string &srcPATH, const std::string &destPATH);
  bool DeleteFile(const std::string &filePATH);

private:
  OSSClientManager();
  static OSSClientManager *m_oss;
  aos_pool_t *pool = NULL;
  oss_request_options_t *options = NULL;
};
}
#endif // OSSCLIENTMANAGER_H
