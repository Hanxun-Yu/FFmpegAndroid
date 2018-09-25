//
// Created by yuhanxun on 2018/9/25.
//
#include "Mp4Muxer.h"
#include "JniHelper.h"

extern "C" {

}
Mp4Muxer *mp4Muxer = new Mp4Muxer();

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

JNIEXPORT jint JNICALL muxMp4(JNIEnv *env, jclass clazz,
                              jstring inputH264FileName, jstring inputAacFileName,
                              jstring outMP4FileName) {
    LOGE("muxMp4");

    const char *h264FilePath = env->GetStringUTFChars(inputH264FileName, NULL);
    const char *aacFilePath = env->GetStringUTFChars(inputAacFileName, NULL);
    const char *outMp4FilePath = env->GetStringUTFChars(outMP4FileName, NULL);


    int ret = mp4Muxer->muxer_main((char *) h264FilePath, (char *) aacFilePath,
                                   (char *) outMp4FilePath, 0);

    env->ReleaseStringUTFChars(inputH264FileName, h264FilePath);
    env->ReleaseStringUTFChars(inputAacFileName, aacFilePath);
    env->ReleaseStringUTFChars(outMP4FileName, outMp4FilePath);


    return ret;
}


JNINativeMethod nativeMethod[] = {
        {"init",               "()I",                                                       (void *) init},
        {"release",            "()I",                                                       (void *) release},
        {"muxMp4",             "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I", (void *) muxMp4}

};


std::string myClassName = "com/kedacom/demo/appcameratoh264/jni/MuxerJni";

JNIEXPORT jint
JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    return JniHelper::handleJNILoad(vm, reserved, myClassName,
                                    nativeMethod, sizeof(nativeMethod) / sizeof(nativeMethod[0]));
}

