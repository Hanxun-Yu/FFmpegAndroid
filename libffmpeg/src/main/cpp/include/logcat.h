//
// Created by yuhanxun on 2018/6/15.
//

#ifndef SAMPLEJNI_LOGCAT_H
#define SAMPLEJNI_LOGCAT_H

#include <android/log.h>
#define  LOG "JNILOG_xunxun"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG,__VA_ARGS__)
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG,__VA_ARGS__)

#endif
