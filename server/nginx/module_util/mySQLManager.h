#ifndef __MYSQLMANAGER_H__  
#define __MYSQLMANAGER_H__  
#include<iostream>  
#include<string>  
#include<mysql/mysql.h>  
using namespace std;  
  
class mySQLManager
{  
public:  
    mySQLManager();  
    ~mySQLManager();  
    int initDB(string host, string user, string pwd, string db_name);  
    int exeSQLReturnData(string sql); 
    int exeSQLWithoutReturnData(string sql);
    MYSQL *connection = NULL;  
private:  
    MYSQL_RES *result = NULL;  
    MYSQL_ROW row ; 
};  
  
#endif  

