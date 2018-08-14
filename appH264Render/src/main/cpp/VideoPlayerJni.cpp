

#include "Jnicom.h"
#include "VideoDecoder.h"
#include "RGBRender.h"
extern "C" {

}


Jnicom *jnicom = new Jnicom();
VideoDecoder *videoDecoder;
RGBRender *render;

class MyOnDecoderListener : public VideoDecoder::OnDecoderListener {
public:
    virtual void onRGB(JNIEnv *jniEnv, jobject jobject, YuvData *data) override {
        //渲染
        LOGD("onRGB w:%u h:%u len:%u",data->getWidth(),data->getHeight(),data->getSize());
        render->putRGB(data);
//        delete(data);
    }
};

JNIEXPORT void JNICALL init(JNIEnv *env, jclass clazz) {
    LOGD("init");
    JavaVM *javaVM;
    env->GetJavaVM(&javaVM);
    videoDecoder = new VideoDecoder(javaVM, clazz);
    render = new RGBRender(javaVM,clazz);
    videoDecoder->init();
    videoDecoder->setOnDecoderListener(new MyOnDecoderListener());
}

JNIEXPORT void JNICALL release(JNIEnv *env, jclass clazz) {
    LOGD("release");

}


JNIEXPORT void JNICALL setRender(JNIEnv *env, jclass clazz, jobject surface) {
    LOGD("setRender");
    render->setRenderView(env,surface);
}

JNIEXPORT void JNICALL start(JNIEnv *env, jclass clazz) {
    LOGD("start");


}

JNIEXPORT void JNICALL stop(JNIEnv *env, jclass clazz) {
    LOGD("stop");

}

JNIEXPORT void JNICALL putFrame(JNIEnv *env, jclass clazz, jbyteArray srcFrame,
                                jint frameSize) {
    LOGD("putFrame");

    jbyte *p_data = env->GetByteArrayElements(srcFrame, NULL);
    u_int8_t * cpy = static_cast<u_int8_t *>(malloc(frameSize));
    memcpy(cpy,p_data,frameSize);
    LOGE("putFrame -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+");

    videoDecoder->putFrame(cpy, frameSize);
    env->ReleaseByteArrayElements(srcFrame,p_data,0);
}


JNINativeMethod nativeMethod[] = {
        {"init",      "()V",                   (void *) init},
        {"release",   "()V",                   (void *) release},
        {"setRender", "(Ljava/lang/Object;)V", (void *) setRender},
        {"start",     "()V",                   (void *) start},
        {"stop",      "()V",                   (void *) stop},
        {"putFrame",  "([BI)V",                (void *) putFrame},

};



std::string myClassName = "com/example/apph264render/ffmpegcodec/FFmpegCodec";

JNIEXPORT jint
JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    return jnicom->handleJNILoad(vm, reserved, myClassName,
                                 nativeMethod, sizeof(nativeMethod) / sizeof(nativeMethod[0]));
}