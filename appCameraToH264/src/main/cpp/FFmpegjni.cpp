
#include <jni.h>
#include <string>
#include <include/Jnicom.h>
#include <malloc.h>
#include "FrameEncoder.h"

FrameEncoder* frameEncoder;
extern "C" {

}
Jnicom *jnicom = new Jnicom();

JNIEXPORT jint JNICALL init
        (JNIEnv *env, jclass clazz, jint width, jint height, jint outWidth, jint outHeight) {

    return 0;
}

JNIEXPORT jint JNICALL release
        (JNIEnv *env, jclass clazz) {
//    free(temp_i420_data);
//    free(temp_i420_data_scale);
//    free(temp_i420_data_rotate);
    free(frameEncoder);
//    free(audioEncoder);
//    free(rtmpLivePublish);
    return 0;
}


JNIEXPORT jint JNICALL encoderVideoinit
        (JNIEnv *env, jclass clazz, jint in_width, jint in_height, jint out_width, jint out_height,jint bitrate) {
    frameEncoder = new FrameEncoder();
    frameEncoder->setInWidth(in_width);
    frameEncoder->setInHeight(in_height);
    frameEncoder->setOutWidth(out_width);
    frameEncoder->setOutHeight(out_height);
    frameEncoder->setBitrate(bitrate);
    frameEncoder->open();
    return 0;
}

JNIEXPORT jint JNICALL encoderVideoEncode
        (JNIEnv *env, jclass clazz, jbyteArray srcFrame, jint frameSize, jint fps
                , jbyteArray dstFrame,jintArray outFramewSize) {

    jbyte *Src_data = env->GetByteArrayElements(srcFrame, NULL);
    jbyte *Dst_data = env->GetByteArrayElements(dstFrame, NULL);
    jint *dstFrameSize = env->GetIntArrayElements(outFramewSize, NULL);

    int numNals = frameEncoder->encodeFrame((char*)Src_data, frameSize, fps, (char*)Dst_data, dstFrameSize);

    env->ReleaseByteArrayElements(dstFrame, Dst_data, 0);
    env->ReleaseByteArrayElements(srcFrame, Src_data, 0);
    env->ReleaseIntArrayElements(outFramewSize, dstFrameSize, 0);
    return numNals;
}

JNINativeMethod nativeMethod[] = {
        {"init", "(IIII)I", (void *) init},
        {"release", "()I", (void *) release},
        {"encoderVideoinit", "(IIIII)I", (void *) encoderVideoinit},
        {"encoderVideoEncode", "([BII[B[I)I", (void *) encoderVideoEncode}
};


std::string myClassName = "com/kedacom/demo/appcameratoh264/jni/FFmpegjni";

JNIEXPORT jint
JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    return jnicom->handleJNILoad(vm, reserved, myClassName,
                                 nativeMethod, sizeof(nativeMethod) / sizeof(nativeMethod[0]));
}