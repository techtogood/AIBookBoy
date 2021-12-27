#ifndef LOG_H
#define LOG_H
#include <stdio.h>

#ifdef __linux__

#define UDEBUG(...)  printf(__VA_ARGS__)

#else

#include <android/log.h>
#define UDEBUG(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#endif


#endif // LOG_H
