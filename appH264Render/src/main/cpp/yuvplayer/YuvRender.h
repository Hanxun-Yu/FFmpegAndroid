//
// Created by xunxun on 2018/8/12.
//

#ifndef CEEWAIPCSDKDEMO_YUVRENDER_H
#define CEEWAIPCSDKDEMO_YUVRENDER_H

#include "../util/YuvData.hpp"
#include "../util/SyncQueue.hpp"

extern "C" {
#include <jni.h>
#include "JniHelper.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include "../include/libyuv.h"
};

class YuvRender {
public:
    YuvRender(JavaVM *javaVM, jobject jobj);

    void setRenderView(JNIEnv *jniEnv,jobject obj);
    void start();
    void stop();
    void putYuv(YuvData* yuvData);
    bool isStop;
    SyncQueue<YuvData>* syncQueue;
    JavaVM *javaVM;
    jobject jobj;
    JNIEnv *jniEnv;
    jobject  renderView;
};


#endif //CEEWAIPCSDKDEMO_YUVRENDER_H
