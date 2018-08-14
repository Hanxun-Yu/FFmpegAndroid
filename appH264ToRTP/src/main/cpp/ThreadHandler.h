//
// Created by yuhanxun on 2018/6/19.
//

#ifndef SAMPLEJNI_THREADHANDLER_H
#define SAMPLEJNI_THREADHANDLER_H

#include <iostream>
#include <cstdlib>
#include <pthread.h>
#include <unistd.h>
#include <jni.h>
#include "logcat.h"
#include "SyncQueue.hpp"
#include "bean/ByteData.hpp"

class ThreadHandler {
public:
    class ICallback {
    public:
//    virtual void onCallback(JNIEnv *jniEnv, jobject jobject, std::string fileName, bool bOK) = 0;
        virtual void onCallback(JNIEnv *jniEnv, jobject jobject, ByteData* data) = 0;
    };
public:
    ThreadHandler(JavaVM* javaVM,jobject jobject1);
    void start();
    void stop();
    void putData(u_int8_t* p_data,uint16_t size);
    virtual ByteData* handleData(ByteData* data) = 0;
    void setCallback(ICallback* callback);
public:
    bool isStop;
    SyncQueue<ByteData>* syncQueue;
    ICallback* callback;
    JavaVM *javaVM;
    jobject jobject1;
    JNIEnv *jniEnv;

};


#endif //SAMPLEJNI_THREADHANDLER_H
