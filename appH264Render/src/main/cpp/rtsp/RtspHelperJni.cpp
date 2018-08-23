//
// Created by yuhanxun on 2018/8/16.
//

#include "FFmpegRtsp.h"

extern "C" {

}


FFmpegRtsp *fFmpegRtsp;
jobject globalObj;
jmethodID callbackID;
class RtspCallback : public FFmpegRtsp::OnDataCallbackListener {
public:
    void onDataCallback(JNIEnv *jniEnv, jobject obj, uint8_t *data, int32_t size) override {

//        LOGE("FFmpegRtsp::OnDataCallback data:%p size:%d", data, size);
        if(size <= 0)
            return;
        jbyteArray ret = jniEnv->NewByteArray(size);
        jniEnv->SetByteArrayRegion(ret, 0, size, reinterpret_cast<const jbyte *>(data));
        jniEnv->CallVoidMethod(obj, callbackID, ret, size);
        jniEnv->ReleaseByteArrayElements(ret, reinterpret_cast<jbyte *>(data), 0);
        //不加这句一会就crash
        jniEnv->DeleteLocalRef(ret);
    }
};


JNIEXPORT void JNICALL init(JNIEnv *env, jobject obj) {
    LOGD("init");
    JavaVM *javaVM;
    env->GetJavaVM(&javaVM);
    fFmpegRtsp = new FFmpegRtsp(javaVM,globalObj = env->NewGlobalRef(obj));
    fFmpegRtsp->setOnDataCallbackListener(new RtspCallback());

    jclass cls = env->GetObjectClass(obj);
    if (cls != NULL) {
        callbackID = env->GetMethodID(cls, "onDataCallback", "([BI)V");
    }
}

JNIEXPORT void JNICALL release(JNIEnv *env, jobject clazz) {
    LOGD("release");
    env->DeleteGlobalRef(globalObj);
    fFmpegRtsp->stopPlay();
}


JNIEXPORT void JNICALL play(JNIEnv *env, jobject clazz, jstring url) {
    LOGD("play s");
    const char *c_url = env->GetStringUTFChars(url, NULL);
    char *cpy = static_cast<char *>(malloc(strlen(c_url)));
    strcpy(cpy, c_url);
    fFmpegRtsp->play(cpy);
    env->ReleaseStringUTFChars(url, c_url);
    LOGD("play e");
}

JNINativeMethod nativeMethod[] = {
        {"init",    "()V",                   (void *) init},
        {"release", "()V",                   (void *) release},
        {"play",    "(Ljava/lang/String;)V", (void *) play},
};


std::string myClassName = "com/example/apph264render/jni/RtspHelperJni";

JNIEXPORT jint
JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    return JniHelper::handleJNILoad(vm, reserved, myClassName,
                                 nativeMethod, sizeof(nativeMethod) / sizeof(nativeMethod[0]));
}