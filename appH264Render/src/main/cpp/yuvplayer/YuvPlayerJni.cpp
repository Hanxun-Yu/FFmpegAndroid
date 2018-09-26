

#include "JniHelper.h"
#include "YuvRender.h"
#include "SPSParse.h"

extern "C" {

}


YuvRender *render;
SPSParse *spsParse;

JNIEXPORT void JNICALL init(JNIEnv *env, jclass clazz) {
    LOGD("init");
    JavaVM *javaVM;
    env->GetJavaVM(&javaVM);
    render = new YuvRender(javaVM, clazz);
    spsParse = new SPSParse();
}

JNIEXPORT void JNICALL release(JNIEnv *env, jclass clazz) {
    LOGD("release");
    delete (render);
    delete (spsParse);
}


JNIEXPORT void JNICALL setRender(JNIEnv *env, jclass clazz, jobject surface) {
    LOGD("setRender");
    render->setRenderView(env, surface);
}

JNIEXPORT void JNICALL start(JNIEnv *env, jclass clazz) {
    LOGD("start");
    render->start();
}

JNIEXPORT void JNICALL stop(JNIEnv *env, jclass clazz) {
    LOGD("stop");
    render->stop();
}

JNIEXPORT void JNICALL
putYuv(JNIEnv *env, jclass clazz, jbyteArray srcFrame, jint width, jint height,
       jint frameSize) {
    LOGD("putFrame");

    jbyte *p_data = env->GetByteArrayElements(srcFrame, NULL);
    u_int8_t *cpy = static_cast<u_int8_t *>(malloc(frameSize));
    memcpy(cpy, p_data, frameSize);
//    LOGE("putYuv -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+");
    YuvData *data = new YuvData();
    data->setWidth(width);
    data->setHeight(height);
    data->setSize(frameSize);
    data->setP_data(cpy);
    render->putYuv(data);
    env->ReleaseByteArrayElements(srcFrame, p_data, 0);

}

JNIEXPORT void JNICALL
getSPSWH(JNIEnv *env, jclass clazz, jbyteArray sps, jintArray retHW) {
    LOGD("putFrame");

    jbyte *p_data = env->GetByteArrayElements(sps, NULL);
    int32 w, h;
    int32 ret[2];
    spsParse->getSpsWH(reinterpret_cast<u_int8_t *>(p_data), w, h);
    ret[0] = w;
    ret[1] = h;

    env->SetIntArrayRegion(retHW, 0, 2, ret);
    env->ReleaseByteArrayElements(sps, p_data, 0);

}


JNINativeMethod nativeMethod[] = {
        {"init",      "()V",                   (void *) init},
        {"release",   "()V",                   (void *) release},
        {"setRender", "(Ljava/lang/Object;)V", (void *) setRender},
        {"start",     "()V",                   (void *) start},
        {"stop",      "()V",                   (void *) stop},
        {"putYuv",    "([BIII)V",              (void *) putYuv},
        {"getSPSWH",  "([B[I)V",               (void *) getSPSWH}

};


std::string myClassName = "com/example/apph264render/jni/YuvPlayerJni";

JNIEXPORT jint
JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    return JniHelper::handleJNILoad(vm, reserved, myClassName,
                                    nativeMethod, sizeof(nativeMethod) / sizeof(nativeMethod[0]));
}