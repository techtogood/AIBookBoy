//
// Created by Administrator on 2018/1/29.
//

#ifndef FINDOBJECT_ULOGGER_H
#define FINDOBJECT_ULOGGER_H
#include <android/log.h>
#define LOG_TAG  "findobject"
#if 1
#define LOGD(...)     __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define UDEBUG(...)   __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define UINFO(...)    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define UWARN(...) 	  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define UERROR(...)   __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define UFATAL(...)   __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#else
#define LOGD(...)     
#define UDEBUG(...)   
#define UINFO(...)    
#define UWARN(...) 	 
#define UERROR(...)   
#define UFATAL(...)  
#endif

#define UASSERT(condition) if(!(condition))  UDEBUG("Condition (%s) not met!", #condition)
//#define UASSERT_MSG(condition, msg_str) if(!(condition)) UDEBUG("Condition (%s) not met! [%s]", #condition, msg_str)

#endif //FINDOBJECT_ULOGGER_H
