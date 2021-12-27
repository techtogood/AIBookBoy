#include "../include/Setting.h"
namespace my_find_object {

using namespace std;

map<string, string>  Setting::m_setting;
string     Setting::m_settingFilePath = "/data/PictureBookRead/my_find_object.ini";
int         Setting::m_objectID_ = 1;
int         Setting::m_time_ = clock();
void Setting::init(const string &fileName)
{
   if(fileName.size())
   {
     m_settingFilePath = fileName;
   }
   #if 0

   ifstream infile;
   infile.open(m_settingFilePath, std::ios::in);
   if(!infile)
   {
     UDEBUG("open file: %s error.\n", m_settingFilePath.data());
   }
   string str;
   string strHead;
   while (getline(infile,str))
   {
     if(!str.size())
     {
       continue ;
     }
     if(';' == str[0])
     {
       continue ;
     }
     if('[' == str[0])
     {
       if("ORB" == str.substr(1,3))
       {
         strHead = "ORB/";
       }
       else if("SIFT" == str.substr(1,4))
       {
         strHead = "SIFT/";
       }
       else if("SURF" == str.substr(1,4))
       {
         strHead = "SURF/";
       }
       else
       {
         strHead = "";
       }
     }
     else
     {
       vector<string> set = str_spalitby(str,"=");
       m_setting.insert(make_pair((strHead+set[0]),set[1]));
     }
   }
   infile.close();
   #else
   m_setting.insert(make_pair("FeatureType","ORB"));
   m_setting.insert(make_pair("ImageRecvTopic","image_raw"));
   m_setting.insert(make_pair("filePath","/sdcard/data/book/cover/image/"));
   m_setting.insert(make_pair("initData","initData.txt"));
   m_setting.insert(make_pair("ImageCorrection","false"));
   m_setting.insert(make_pair("Rate","5"));
   m_setting.insert(make_pair("vocabularyFixed","false"));
   m_setting.insert(make_pair("invertedSearch","true"));
   m_setting.insert(make_pair("convertType","float"));
   m_setting.insert(make_pair("nndrRatioUsed","true"));
   m_setting.insert(make_pair("nndrRatio","0.8"));
   m_setting.insert(make_pair("minDistanceUsed","false"));
   m_setting.insert(make_pair("minDistance","35"));
   m_setting.insert(make_pair("threadCount","1"));
   m_setting.insert(make_pair("bruteSearch","false"));
   
   m_setting.insert(make_pair("ORB/nFeatures","800"));
   m_setting.insert(make_pair("ORB/scaleFactor","1.2"));
   m_setting.insert(make_pair("ORB/nLevel","8"));
   m_setting.insert(make_pair("ORB/edgeThreshold","31"));
   m_setting.insert(make_pair("ORB/firstLevel","0"));
   m_setting.insert(make_pair("ORB/WTA_K","2"));
   m_setting.insert(make_pair("ORB/scoreType","0"));
   m_setting.insert(make_pair("ORB/pathSize","31"));


   m_setting.insert(make_pair("SIFT/nFeatures","0"));
   m_setting.insert(make_pair("SIFT/nOctaveLayers","3"));
   m_setting.insert(make_pair("SIFT/contrastThreshold ","0.04"));
   m_setting.insert(make_pair("SIFT/edgeThreshold","10"));
   m_setting.insert(make_pair("SIFT/sigma","1.6"));


   m_setting.insert(make_pair("SURF/hessianThreshold","600.0"));
   m_setting.insert(make_pair("SURF/nOctaves","4"));
   m_setting.insert(make_pair("SURF/nOctaveLayers","2"));
   m_setting.insert(make_pair("SURF/extended","false"));
   m_setting.insert(make_pair("SURF/upright","false"));
   #endif
}

void Setting::destroy()
{
}

string Setting::getValue_string(const string &key)
{
  map<string, string>::iterator it = m_setting.find(key);
  if(it != m_setting.end())
  {
    return it->second;
  }
  else
  {
    return "";
  }

}

bool Setting::getValue_bool(const string &key)
{
  map<string, string>::iterator it = m_setting.find(key);
  if(it != m_setting.end())
  {
//    UDEBUG("%s = %s\n",key.data(),it->second.data());
    return ("true" == it->second);
  }
  else
  {
    return false;
  }
}

void Setting::setValue(const string &key, const string &value)
{
  //null
}

int Setting::getObjectID()
{
  m_objectID_ ++;
  return (m_objectID_ - 1);
}

double Setting::getTime()
{
  int time = m_time_;
  m_time_ = clock();
  return (((double)(m_time_-time)/CLOCKS_PER_SEC));

}

double Setting::getCurTime()
{
  return (((double)(clock())/CLOCKS_PER_SEC));

}

vector<string> str_spalitby(string &s, const string &seperator)
{
  vector<string> result;
    typedef string::size_type string_size;
    string_size i = 0;

    while(i != s.size()){
      //找到字符串中首个不等于分隔符的字母；
      int flag = 0;
      while(i != s.size() && flag == 0){
        flag = 1;
        for(string_size x = 0; x < seperator.size(); ++x)
        if(s[i] == seperator[x]){
          ++i;
          flag = 0;
          break;
        }
      }

      //找到又一个分隔符，将两个分隔符之间的字符串取出；
      flag = 0;
      string_size j = i;
      while(j != s.size() && flag == 0){
        for(string_size x = 0; x < seperator.size(); ++x)
        if(s[j] == seperator[x]){
          flag = 1;
          break;
        }
        if(flag == 0)
        ++j;
      }
      if(i != j){
        result.push_back(s.substr(i, j-i));
        i = j;
      }
    }
    return result;
}

}//my_find_object  namespace
