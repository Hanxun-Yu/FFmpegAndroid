//
// Created by yuhanxun on 2018/6/19.
//

#include "main.h"
ThreadHandler *threadHandler;
Jnicom *jnicom = new Jnicom();

class MyCallback : public ICallback {
public:
    jmethodID strTest_id = NULL;
//    MyCallback(JavaVM *javaVM, jobject obj) {
//        this->javaVM = javaVM;
//        this->obj = obj;
//        jclass cls = env->GetObjectClass(this->obj);
//        if (cls != NULL) {
//            strTest_id = env->GetMethodID(cls, "callbackFromC", "(Ljava/lang/String;)V");
//        }
//    }
    Jnicom *jnicom = NULL;

    void onCallback(JNIEnv *jniEnv, jobject jobject, std::string fileName, bool bOK) override {
        LOGE("c++callback fileName:%s bOK:%d", fileName.data(), bOK);
        if (strTest_id == NULL) {
            jclass cls = jniEnv->GetObjectClass(jobject);
            if (cls != NULL) {
                strTest_id = jniEnv->GetMethodID(cls, "callbackFromC", "(Ljava/lang/String;)V");
            }
        }

        if (this->jnicom == NULL) {
            this->jnicom = new Jnicom(jniEnv);
        }

        if (strTest_id != NULL) {
            jniEnv->CallVoidMethod(jobject, strTest_id, this->jnicom->jstrValOf(fileName));
        }
    }

};


extern "C" {

JNIEXPORT void JNICALL init(JNIEnv *env, jobject obj) {
    LOGE("init");
    JavaVM *javaVM;
    env->GetJavaVM(&javaVM);
    threadHandler = new ThreadHandler(javaVM, env->NewGlobalRef(obj));
}
JNIEXPORT void JNICALL start(JNIEnv *env, jobject obj) {
    LOGE("start");
    ICallback *callback = new MyCallback();
    threadHandler->setCallback(callback);
    threadHandler->start();
//    jclass cls = env->GetObjectClass(obj);
//    jmethodID strTest_id;
//    if (cls != NULL) {
//        strTest_id = env->GetMethodID(cls, "callbackFromC", "(Ljava/lang/String;)V");
//    }
//    if (strTest_id != NULL) {
//        env->CallVoidMethod(obj, strTest_id, jnicom->jstrValOf("filename"));
//    }
}
JNIEXPORT void JNICALL stop(JNIEnv *env, jobject obj) {
    LOGE("stop");

    threadHandler->stop();
}

}

JNINativeMethod nativeMethod[] = {
        {"init",  "()V", (void *) init},
        {"start", "()V", (void *) start},
        {"stop",  "()V", (void *) stop}
};


std::string myClassName = "com/example/appthreadcallback/MainActivity";

JNIEXPORT jint
JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    return jnicom->handleJNILoad(vm, reserved, myClassName,
                                 nativeMethod, sizeof(nativeMethod) / sizeof(nativeMethod[0]));
}