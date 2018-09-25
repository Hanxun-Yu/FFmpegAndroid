//
// Created by yuhanxun on 2018/9/25.
//


#include "AudioEncoder.h"
#include "JniHelper.h"

extern "C" {

}
AudioEncoder *audioEncoder;

JNIEXPORT jint JNICALL init
        (JNIEnv *env, jclass clazz) {
    LOGE("init");

    return 0;
}

JNIEXPORT jint JNICALL release
        (JNIEnv *env, jclass clazz) {
    LOGE("release");

    return 0;
}
JNIEXPORT jint JNICALL
encoderAudioInit(JNIEnv *env, jclass clazz, jint sampleRate, jint channels, jint bitRate) {
    LOGE("encoderAudioInit");

    audioEncoder = new AudioEncoder(channels, sampleRate, bitRate);
    int value = audioEncoder->init();
    return value;
}

JNIEXPORT jint JNICALL encoderAudioEncode(JNIEnv *env, jclass clazz, jbyteArray srcFrame,
                                          jint frameSize, jbyteArray dstFrame, jint dstSize) {

    jbyte *Src_data = env->GetByteArrayElements(srcFrame, NULL);
    jbyte *Dst_data = env->GetByteArrayElements(dstFrame, NULL);

    int validlength = audioEncoder->encodeAudio((unsigned char *) Src_data, frameSize,
                                                (unsigned char *) Dst_data, dstSize);

    env->ReleaseByteArrayElements(dstFrame, Dst_data, 0);
    env->ReleaseByteArrayElements(srcFrame, Src_data, 0);

    return validlength;
}

JNINativeMethod nativeMethod[] = {
        {"init",               "()I",                                                       (void *) init},
        {"release",            "()I",                                                       (void *) release},
        {"encoderAudioInit",   "(III)I",                                                    (void *) encoderAudioInit},
        {"encoderAudioEncode", "([BI[BI)I",                                                 (void *) encoderAudioEncode},
};


std::string myClassName = "com/kedacom/demo/appcameratoh264/jni/AudioEncoderJni";

JNIEXPORT jint
JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    return JniHelper::handleJNILoad(vm, reserved, myClassName,
                                    nativeMethod, sizeof(nativeMethod) / sizeof(nativeMethod[0]));
}