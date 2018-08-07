

#include "Jnicom.h"
#include "RTPPackager.h"

extern "C" {

}
Jnicom *jnicom = new Jnicom();
//ThreadHandler *threadHandler;
//
//class MyCallback : public ThreadHandler::ICallback {
//public:
//    jmethodID strTest_id = NULL;
//    Jnicom *jnicom = NULL;
//
//    void onCallback(JNIEnv *jniEnv, jobject jobject, ByteData* data) override {
//        if (strTest_id == NULL) {
//            jclass cls = jniEnv->GetObjectClass(jobject);
//            if (cls != NULL) {
//                strTest_id = jniEnv->GetMethodID(cls, "callbackOnNative", "(I)V");
//            }
//        }
//
//        if (strTest_id != NULL) {
//            jniEnv->CallVoidMethod(jobject, strTest_id, 0);
//        }
//    }
//
//};

//ThreadHandler::ICallback *callback = new MyCallback();
RTPPackager *rtpPackager;
JNIEXPORT jint JNICALL startRTPFromFile
        (JNIEnv *env, jclass clazz, jstring h264Path, jstring accPath, jstring ip, jint port) {

    return 0;
}

JNIEXPORT jint JNICALL init
        (JNIEnv *env, jclass clazz, jstring ip, jint port) {
    LOGD("init");
    JavaVM *javaVM;
    env->GetJavaVM(&javaVM);
    rtpPackager = new RTPPackager(javaVM,clazz);
    return 0;
}

JNIEXPORT void JNICALL start
        (JNIEnv *env, jclass clazz) {
    LOGD("start");
}

JNIEXPORT void JNICALL putH264
        (JNIEnv *env, jclass clazz, jbyteArray h264byte, jint size) {
    LOGD("putH264");
    jbyte *p_data = env->GetByteArrayElements(h264byte, NULL);
    rtpPackager->putH264(reinterpret_cast<u_int8_t *>(p_data), size);
    env->ReleaseByteArrayElements(h264byte,p_data,0);
}

JNIEXPORT void JNICALL stop
        (JNIEnv *env, jclass clazz) {
    LOGD("stop");
}


JNIEXPORT void JNICALL quit
        (JNIEnv *env, jclass clazz) {
    LOGD("quit");

}

class MyOnPackListener : public RTPPackager::OnPackListener {
public:
    void onRTP(JNIEnv *jniEnv, jobject jobject, u_int8_t *p_data, u_int16_t size) override {

    }
};

class MyOnUnPackListener : public RTPPackager::OnUnPackListener {
public:
    void onH264(JNIEnv *jniEnv, jobject jobject, u_int8_t *p_data, u_int16_t size) override {

    }
};


JNINativeMethod nativeMethod[] = {
        {"startRTPFromFile", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)I", (void *) startRTPFromFile},
        {"init",             "(Ljava/lang/String;I)I",                                     (void *) init},
        {"start",            "()V",                                                        (void *) start},
        {"putH264",          "([BI)V",                                                     (void *) putH264},
        {"stop",             "()V",                                                        (void *) stop},
        {"quit",             "()V",                                                        (void *) quit},
};


std::string myClassName = "com/example/apph264tortp/jni/FFmpegJni";

JNIEXPORT jint
JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    return jnicom->handleJNILoad(vm, reserved, myClassName,
                                 nativeMethod, sizeof(nativeMethod) / sizeof(nativeMethod[0]));
}