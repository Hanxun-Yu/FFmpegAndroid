

#include "VideoEncoder.h"
#include "AudioEncoder.h"
#include "Mp4Muxer.h"
#include "Jnicom.h"

VideoEncoder *frameEncoder;
AudioEncoder *audioEncoder;
Mp4Muxer *mp4Muxer = new Mp4Muxer();
extern "C" {

}
Jnicom *jnicom = new Jnicom();

JNIEXPORT jint JNICALL init
        (JNIEnv *env, jclass clazz) {

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
        (JNIEnv *env, jclass clazz, jobject params) {
    frameEncoder = new VideoEncoder();
//    frameEncoder->setInWidth(in_width);
//    frameEncoder->setInHeight(in_height);
//    frameEncoder->setOutWidth(out_width);
//    frameEncoder->setOutHeight(out_height);
//    frameEncoder->setBitrate(bitrate);
    frameEncoder->open();
    return 0;
}

JNIEXPORT jint JNICALL encoderVideoEncode
        (JNIEnv *env, jclass clazz, jbyteArray srcFrame, jint frameSize, jint fps,
         jbyteArray dstFrame, jintArray outFramewSize) {

    jbyte *Src_data = env->GetByteArrayElements(srcFrame, NULL);
    jbyte *Dst_data = env->GetByteArrayElements(dstFrame, NULL);
    jint *dstFrameSize = env->GetIntArrayElements(outFramewSize, NULL);

    int numNals = frameEncoder->encodeFrame((char *) Src_data, frameSize, fps, (char *) Dst_data,
                                            dstFrameSize);

    env->ReleaseByteArrayElements(dstFrame, Dst_data, 0);
    env->ReleaseByteArrayElements(srcFrame, Src_data, 0);
    env->ReleaseIntArrayElements(outFramewSize, dstFrameSize, 0);
    return numNals;
}

JNIEXPORT jint JNICALL
encoderAudioInit(JNIEnv *env, jclass clazz, jint sampleRate, jint channels, jint bitRate) {
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

JNIEXPORT jint JNICALL muxMp4(JNIEnv *env, jclass clazz,
                              jstring inputH264FileName, jstring inputAacFileName,
                              jstring outMP4FileName) {

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
        {"init",               "(IIII)I",                                                   (void *) init},
        {"release",            "()I",                                                       (void *) release},
        {"encoderVideoinit",   "(IIIII)I",                                                  (void *) encoderVideoinit},
        {"encoderVideoEncode", "([BII[B[I)I",                                               (void *) encoderVideoEncode},
        {"encoderAudioInit",   "(III)I",                                                    (void *) encoderAudioInit},
        {"encoderAudioEncode", "([BI[BI)I",                                                 (void *) encoderAudioEncode},
        {"muxMp4",             "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I", (void *) muxMp4}

};


std::string myClassName = "com/kedacom/demo/appcameratoh264/jni/FFmpegjni";

JNIEXPORT jint
JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    return jnicom->handleJNILoad(vm, reserved, myClassName,
                                 nativeMethod, sizeof(nativeMethod) / sizeof(nativeMethod[0]));
}