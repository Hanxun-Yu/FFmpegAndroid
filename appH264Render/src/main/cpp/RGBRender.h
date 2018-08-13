//
// Created by xunxun on 2018/8/12.
//

#ifndef CEEWAIPCSDKDEMO_RGBRENDER_H
#define CEEWAIPCSDKDEMO_RGBRENDER_H

#include "util/YuvData.hpp"
#include "util/SyncQueue.hpp"

extern "C" {
#include <jni.h>
#include "Jnicom.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>
};

class RGBRender {
public:
    RGBRender(JavaVM *javaVM, jobject jobj);

    void setRenderView(JNIEnv *jniEnv,jobject obj);
    void putRGB(YuvData* yuvData);
    bool isStop;
    SyncQueue<YuvData>* syncQueue;
    JavaVM *javaVM;
    jobject jobj;
    JNIEnv *jniEnv;
    jobject  renderView;
};


#endif //CEEWAIPCSDKDEMO_RGBRENDER_H
