#ifndef SETTING_H
#define SETTING_H

#include <iostream>
#include <map>
#include <vector>
#include <fstream>
#include <time.h>
#include "ULogger.h"
#include <string>
#include <stdlib.h>

//#define HANMING

namespace my_find_object {
#define THREAD
#define FEATURE_LENGTH 64
using namespace std;
class Setting
{
public:
  static void init(const string & fileName = "");
  static void destroy();

  static string getValue_string(const string &key);
  static bool getValue_bool(const string &key);

  static void   setValue(const string &key,const string &value);

  static int getObjectID();
  static double getTime();
  static double getCurTime();
private:
  Setting()
  {
    //NULL
  }
  static map<string, string> m_setting;
  static string m_settingFilePath;
  static int m_objectID_;
  static int m_time_;
};

vector<string> str_spalitby(string &, const string &);
template<class KEY, class VALUE>
using MapIterator = typename std::map<KEY, VALUE>::const_iterator;

template<class KEY, class VALUE>
vector<VALUE> getMapValues(const std::map<KEY, VALUE> &srcMap)
{
  vector<VALUE> result;
  MapIterator<KEY,VALUE> it;
  for(it  = srcMap.begin(); it != srcMap.end(); it ++)
  {
    result.push_back(it->second);
  }
  return result;
}

template<class KEY, class VALUE>
vector<VALUE> getMapValues(const std::multimap<KEY, VALUE> &srcMap, KEY key)
{
  vector<VALUE> result;
  MapIterator<KEY,VALUE> it = srcMap.find(key);
  int KeyCount = srcMap.count(key);
  for(int i=0 ; i<KeyCount; i++, it++)
  {
    result.push_back(it->second);
  }
  return result;
}

template<class KEY, class VALUE>
vector<KEY> getMapKeys(const std::map<KEY, VALUE> &srcMap)
{
  vector<KEY> result;
  MapIterator<KEY,VALUE> it;
  for(it  = srcMap.begin(); it != srcMap.end(); it ++)
  {
    result.push_back(it->first);
  }
  return result;
}


template<class KEY, class VALUE>
vector<KEY> getMapKeys(const std::multimap<KEY, VALUE> &srcMap)
{
  vector<KEY> result;
  MapIterator<KEY,VALUE> it;
  for(it  = srcMap.begin(); it != srcMap.end(); it ++)
  {
    result.push_back(it->first);
  }
  return result;
}

template<class KEY, class VALUE>
VALUE getMapValue(const std::map<KEY,VALUE>&map, const KEY &key)
{
  return map.find(key)->second;
}


}

#endif // SETTING_H
