#include "../include/Setting.h"
namespace my_find_object {

using namespace std;

map<string, string>  Setting::m_setting;
string      Setting::m_settingFilePath = "/usr/local/nginx/conf/my_find_object.ini";
int         Setting::m_objectID_ = 1;
int         Setting::m_time_ = clock();
//sqlite3* Setting::m_DB = NULL;
//MYSQL * Setting::m_mySQLConn = NULL;

//sqlite3* Setting::m_DB = NULL;
void Setting::init(const string &fileName)
{
   if(fileName.size())
   {
     m_settingFilePath = fileName;
   }

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

int Setting::getValue_int(const string &key)
{
  map<string, string>::iterator it = m_setting.find(key);
  if(it != m_setting.end())
  {
    return stoi(it->second);
  }
  else
  {
    return -1;
  }
}

float Setting::getValue_float(const string &key)
{
  map<string, string>::iterator it = m_setting.find(key);
  if(it != m_setting.end())
  {
    return stof(it->second);
  }
  else
  {
    return -1.0;
  }
}

double Setting::getValue_double(const string &key)
{
  map<string, string>::iterator it = m_setting.find(key);
  if(it != m_setting.end())
  {
    return stod(it->second);
  }
  else
  {
    return -1.0;
  }
}

bool Setting::getValue_bool(const string &key)
{
  map<string, string>::iterator it = m_setting.find(key);
  if(it != m_setting.end())
  {
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
#if 0
void Setting::setDB(sqlite3 *db)
{
  Setting::m_DB = db;
}

void Setting::setDB(const string &fileName)
{
  if(SQLITE_OK != sqlite3_open(fileName.c_str(),&m_DB))
  {
    UDEBUG("open databases:/data/find_object.db error.\n");
    exit(1);
  }
}

sqlite3 *Setting::getDB()
{
  return Setting::m_DB;
}
#endif
#if 0
void Setting::initDB(string host, string user, string pwd, string db_name)
{
  m_mySQLConn = mysql_init(NULL); // 初始化数据库连接变量  

  m_mySQLConn = mysql_real_connect(m_mySQLConn, host.c_str(),  
            user.c_str(), pwd.c_str(), db_name.c_str(), 0, NULL, 0);  
    if(m_mySQLConn == NULL)  
    {  
        cout << "Error:" << mysql_error(m_mySQLConn); //may use to feedback in feature
        return ;  
    }  
    printf("m_mySQLConn=0x%x\n",m_mySQLConn);
}

MYSQL *Setting::getMySQLDB()
{
  return Setting::m_mySQLConn;
}

void Setting::closeDB()
{
  mysql_close(m_mySQLConn);
}

#endif

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
