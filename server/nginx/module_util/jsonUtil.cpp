#include<json/json.h>  
#include<iostream>  
using namespace std;  


void FormatJson()
{
    Json::Value root;
    Json::Value arrayObj;
    Json::Value item;
    for (int i=0; i<10; i++)
    {
	    item["key"] = i;
	    arrayObj.append(item);
    }

    root["key1"] = "value1";
    root["key2"] = "value2";
    root["array"] = arrayObj;
    root.toStyledString();
    std::string out = root.toStyledString();
    std::cout << out << std::endl;

}
