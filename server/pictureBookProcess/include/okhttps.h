#ifndef OKHTTPS_H
#define OKHTTPS_H

#include <curl/curl.h>
#include <string>
#include <iostream>
#include "../include/json/json.h"
namespace my_find_object {
class Cover
{
public:
  Cover() {}
  Cover(Json::Value value)
  {
    id = value["id"].asString();
    series_id = value["series_id"].asString();
    book_name = value["book_name"].asString();
    record = value["record"].asString();
    image = value["image"].asString();
    picture = value["picture"].asString();
    feature_type = value["feature_type"].asString();
    data = value["data"].asString();
    page_num = value["page_num"].asString();
    update_time = value["update_time"].asString();
    create_time = value["create_time"].asString();
    delete_time = value["delete_time"].asString();
  }

  Json::Value toJSON()
  {
    Json::Value value;

    value["id"] = id;
    value["series_id"] = series_id;
    value["book_name"] = book_name;
    value["record"] = record;
    value["image"] = image;
    value["picture"] = picture;
    value["feature_type"] = feature_type;
    value["data"] = data;
    value["page_num"] = page_num;
//    value["table_name"] = "cover_test";
//    value["update_time"] = update_time;
//    value["create_time"] = create_time;
//    value["delete_time"] = delete_time;

    return value;
  }

  void display()
  {
    std::cout << "id:" << id << std::endl
         << "series_id: " << series_id << std::endl
         << "book_name: " << book_name << std::endl
         << "record: " << record << std::endl
         << "image: " << image << std::endl
         << "picture: " << picture << std::endl
         << "feature_type: " << feature_type << std::endl
         << "data: " << data << std::endl
         << "page_num: " << page_num << std::endl
         << "update_time: " << update_time << std::endl
         << "create_time: " << create_time << std::endl
         << "delete_time: " << delete_time << std::endl;
  }

  void clear()
  {
    id = "";
    series_id ="";
    book_name = "";
    record = "";
    image = "";
    picture = "";
    feature_type = "";
    data = "";
    page_num = "";
    update_time = "";
    create_time = "";
    delete_time = "";
  }

  std::string id;
  std::string series_id;
  std::string book_name;
  std::string record;
  std::string image;
  std::string picture;
  std::string feature_type;
  std::string data;
  std::string page_num;
  std::string update_time;
  std::string create_time;
  std::string delete_time;
};

class Content
{
public:
  Content() {}
  Content(Json::Value value)
  {
    cover_id = value["cover_id"].asString();
    page_id = value["page_id"].asString();
    text = value["text"].asString();
    record = value["record"].asString();
    picture = value["picture"].asString();
    update_time = value["update_time"].asString();
    create_time = value["create_time"].asString();
    delete_time = value["delete_time"].asString();
  }
  void display()
  {
    std::cout << "cover_id:" << cover_id << std::endl
         << "page_id: " << page_id << std::endl
         << "text: " << text << std::endl
         << "record: " << record << std::endl
         << "picture: " << picture << std::endl
         << "update_time: " << update_time << std::endl
         << "create_time: " << create_time << std::endl
         << "delete_time: " << delete_time << std::endl;
  }

  Json::Value toJSON()
  {
    Json::Value value;
 //   Json::Value page_list;
 //   Json::Value temp;
    value["cover_id"] = cover_id;
//    value["table_name"] = "content";

    value["page_id"] = page_id;
    value["text"] = text;
    value["record"] = record;
    value["picture"] = picture;
//    page_list.append(temp);
//    value["page_list"] = page_list;
//    value["page_list"] = page_list;
    return value;
  }

  std::string cover_id;
  std::string page_id;
  std::string text;
  std::string record;
  std::string picture;
  std::string update_time;
  std::string create_time;
  std::string delete_time;
};

class ContentObject
{
public:
  ContentObject() {}

  ContentObject(Json::Value value)
  {
    id = value["id"].asString();
    image = value["image"].asString();
    update_time = value["update_time"].asString();
    create_time = value["create_time"].asString();
    delete_time = value["delete_time"].asString();
  }

  void display()
  {
    std::cout << "id:" << id << std::endl
         << "image: " << image << std::endl
         << "update_time: " << update_time << std::endl
         << "create_time: " << create_time << std::endl
         << "delete_time: " << delete_time << std::endl;
  }

  Json::Value toJSON()
  {
    Json::Value value;
    Json::Value image_list;
    Json::Value temp;
    temp["id"] = id;
    temp["image"] = image;
//    image_list["update_time"] = update_time;
//    image_list["create_time"] = create_time;
//    image_list["delete_time"] = delete_time;
    image_list.append(temp);
    value["image_list"] = image_list;
    value["table_name"] = "content_object";

    return value;
  }

  std::string id;
  std::string image;
  std::string update_time;
  std::string create_time;
  std::string delete_time;
};

class OkHttps
{
public:
  static OkHttps* getOkHttps();
  static void InitOkHttps();
  static void clearOkHttps();
  

  bool POST(std::string &URL,std::string &request,std::string &result);
  bool GET(std::string &URL,std::string &request,std::string &result);
private:
  OkHttps();

  static OkHttps *this_OKHttps;
  static CURL *m_postHttps;
  static CURL *m_getHttps;
};

 class Category
 {
  public:
    Category() {}
    
    void display()
    {
      std::cout << "id:" << id << std::endl
           << "title: " << title << std::endl
           << "order: " << order << std::endl
           << "status: " << status << std::endl
           << "excel_index: " << excel_index << std::endl
           << "num: " << num << std::endl;
    }
  
    int id;
    std::string title;
    int order;
    int status;
    int excel_index;
    int num;
 };

 class Album
 {
  public:
    Album() {}
    void display()
    {
      std::cout << "id:" << id << std::endl
           << "title: " << title << std::endl
           << "excel_index: " << excel_index << std::endl
           << "cate_id: " << cate_id << std::endl
           << "count: " << count << std::endl;

    }
  
    int id;
    std::string title;
    int excel_index;
    int cate_id;
    int count;
 };
}
#endif // OKHTTPS_H
