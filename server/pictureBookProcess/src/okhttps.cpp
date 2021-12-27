#include "../include/okhttps.h"
namespace my_find_object {
OkHttps* OkHttps::this_OKHttps = new OkHttps();
CURL* OkHttps::m_postHttps = NULL;
CURL* OkHttps::m_getHttps = NULL;


static int writer(char *data, size_t size, size_t nmemb,
                  std::string *writerData)
{
  if(writerData == NULL)
    return 0;
  writerData->append(data, size*nmemb);
  return size * nmemb;
}

OkHttps::OkHttps()
{

}
void OkHttps::InitOkHttps()
{
   curl_global_init(CURL_GLOBAL_ALL);
  m_postHttps = curl_easy_init();
  //SET hander fot http Post
  struct curl_slist *list = NULL;
  list = curl_slist_append(list, "Cache-Control: no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
  list = curl_slist_append(list, "Keep-Alive");
  list = curl_slist_append(list, "Content-type: application/json;charset=utf-8");
  list = curl_slist_append(list, "Pragma: no-cache");
  list = curl_slist_append(list, "Server: Apache/2.4.7 (Ubuntu)");
  curl_easy_setopt(m_postHttps, CURLOPT_HTTPHEADER,list);
  curl_easy_setopt(m_postHttps, CURLOPT_POST, 1);

  m_getHttps = curl_easy_init();
  curl_easy_setopt(m_getHttps, CURLOPT_HTTPGET, 1L);
  curl_easy_setopt(m_getHttps, CURLOPT_WRITEFUNCTION, writer);
}


OkHttps *OkHttps::getOkHttps()
{
  if(this_OKHttps == NULL)
  {

  }
  return this_OKHttps;
}

void OkHttps::clearOkHttps()
{
  /*if(this_OKHttps != NULL)
  {
    delete this_OKHttps;
    this_OKHttps = NULL;
  }*/
  curl_easy_cleanup(m_postHttps);
  curl_easy_cleanup(m_getHttps);
  curl_global_cleanup();
}

size_t http_data_writer(void* data, size_t size, size_t nmemb, void* content)
{
    long totalSize = size*nmemb;

    std::string* symbolBuffer = (std::string*)content;

    if(symbolBuffer)
    {
        symbolBuffer->append((char *)data, ((char*)data)+totalSize);
    }

    return totalSize;
}



bool OkHttps::POST(std::string &URL, std::string &request ,std::string &result)
{
//  curl_easy_reset(m_postHttps);
//  m_postHttps = curl_easy_init();


  //std::cout << "URL:\n" << URL << "\n request:\n" << request.data() << std::endl;

  if(CURLE_OK != curl_easy_setopt(m_postHttps, CURLOPT_URL, URL.data()))
  {
    std::cout << "curl_easy_setopt(m_postHttps, CURLOPT_URL, &URL) false" << std::endl;
    return false;
  }

//  if(CURLE_OK != curl_easy_setopt(m_postHttps, CURLOPT_POST, 1))
//  {
//    std::cout << "curl_easy_setopt(m_postHttps, CURLOPT_HTTPPOST, 1L) false" << std::endl;
//    return false;
//  }

  if(CURLE_OK != curl_easy_setopt(m_postHttps, CURLOPT_POSTFIELDS, request.data()))
  {
    std::cout << "curl_easy_setopt(m_postHttps, CURLOPT_POSTFIELDSIZE, request.size()) false" << std::endl;
    return false;
  }

  if(CURLE_OK != curl_easy_setopt(m_postHttps, CURLOPT_WRITEFUNCTION, http_data_writer))
  {
    std::cout << "CURLOPT_WRITEFUNCTION false" << std::endl;
    return false;
  }

  if(CURLE_OK != curl_easy_setopt(m_postHttps, CURLOPT_WRITEDATA, (void*)&result))
  {
    std::cout << "CURLOPT_WRITEDATA false" << std::endl;
    return false;
  }
  
  CURLcode res = curl_easy_perform(m_postHttps);
  if(CURLE_OK != res)
  {
    std::cout << curl_easy_strerror(res) << std::endl;
    return false;
  }
  /*int size;
  curl_easy_getinfo(m_postHttps,CURLINFO_REQUEST_SIZE,&size);
  std::cout << "size:" << size  << " " << request << std::endl;
  */
  std::cout << "success...\n";
  return true;
}

bool OkHttps::GET(std::string &URL, std::string &request, std::string &result)
{
//  curl_easy_reset(m_postHttps);
//  m_postHttps = curl_easy_init();
  std::string destURL = URL + "/" + request;
//  std::cout << URL << std::endl;
  if(CURLE_OK != curl_easy_setopt(m_getHttps, CURLOPT_URL,destURL.data()))
  {
    std::cout << "curl_easy_setopt(m_postHttps, CURLOPT_URL, &URL) false" << std::endl;
    return false;
  }

//  if(CURLE_OK != curl_easy_setopt(m_getHttps, CURLOPT_HTTPGET, 1L))
//  {
//    std::cout << "curl_easy_setopt(m_postHttps, CURLOPT_HTTPGET, 1L) false" << std::endl;
//    return false;
//  }

//  if(CURLE_OK != curl_easy_setopt(m_getHttps, CURLOPT_WRITEFUNCTION, writer))
//  {
//    std::cout << "curl_easy_setopt(m_postHttps, CURLOPT_WRITEFUNCTION, writer) false" << std::endl;
//    return false;
//  }
  if(CURLE_OK != curl_easy_setopt(m_getHttps, CURLOPT_WRITEDATA, &result))
  {
    std::cout << "curl_easy_setopt(m_postHttps, CURLOPT_WRITEDATA, &result) false" << std::endl;
    return false;
  }

  if(CURLE_OK != curl_easy_perform(m_getHttps))
  {
    std::cout << "curl_easy_perform(m_postHttps) false" << std::endl;
    return false;
  }
  return true;
}
}
