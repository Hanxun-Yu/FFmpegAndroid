//
// Created by yuhanxun on 2018/6/19.
//

#ifndef SAMPLEJNI_ICALLBACK_H
#define SAMPLEJNI_ICALLBACK_H

#include <string>
#include <jni.h>

class ICallback {
public:
    virtual void onCallback(JNIEnv *jniEnv, jobject jobject, std::string fileName, bool bOK) = 0;
};


#endif //SAMPLEJNI_ICALLBACK_H
