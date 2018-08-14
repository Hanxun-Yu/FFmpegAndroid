//
// Created by yuhanxun on 2018/6/15.
//

#ifndef SAMPLEJNI_JNICOM_H
#define SAMPLEJNI_JNICOM_H

#include <jni.h>
#include <string>
#include "logcat.h"
class Jnicom {
public:
    Jnicom();
    Jnicom(JNIEnv *env);
    int handleJNILoad(JavaVM *vm, void *reserved,std::string myClassName,const JNINativeMethod* methods,int methodSize);
    const char* strValOf(std::string str);
    const char* strValOf(std::string str,bool isCopy);

    std::string strValOf(jstring str);
    std::string strValOf(jstring str,bool isCopy);
    jstring jstrValOf(std::string str);

    int* jintValOf(jintArray intArr);
    jcharArray jcharValOf(const char *charArr, int length);
    jbyteArray jbyteValOf(std::string str);
    jbyteArray jbyteValOf(const char* charArr,int length);
private:
    JNIEnv *env;
};


#endif //SAMPLEJNI_JNICOM_H
