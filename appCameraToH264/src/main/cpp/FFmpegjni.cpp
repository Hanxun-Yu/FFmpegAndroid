

#include "VideoEncoder.h"
#include "AudioEncoder.h"
#include "Mp4Muxer.h"
#include "JniHelper.h"

VideoEncoder *frameEncoder;
AudioEncoder *audioEncoder;
Mp4Muxer *mp4Muxer = new Mp4Muxer();
extern "C" {

}

X264Param *switchX264Param(JNIEnv *jniEnv, jobject obj);

JNIEXPORT jint JNICALL init
        (JNIEnv *env, jclass clazz) {

    return 0;
}

JNIEXPORT jint JNICALL release
        (JNIEnv *env, jclass clazz) {
//    free(temp_i420_data);
//    free(temp_i420_data_scale);
//    free(temp_i420_data_rotate);
    frameEncoder->close();
    free(frameEncoder);
//    free(audioEncoder);
//    free(rtmpLivePublish);
    return 0;
}


JNIEXPORT jint JNICALL encoderVideoinit
        (JNIEnv *env, jclass clazz, jobject params) {
    frameEncoder = new VideoEncoder();
    frameEncoder->setParams(switchX264Param(env, params));
//    frameEncoder->setInWidth(in_width);
//    frameEncoder->setInHeight(in_height);
//    frameEncoder->setOutWidth(out_width);
//    frameEncoder->setOutHeight(out_height);
//    frameEncoder->setBitrate(bitrate);
    frameEncoder->open();
    return 0;
}


jint getIntField(JNIEnv *jniEnv, jobject obj, const char *fieldName) {
    jclass internalObjClass = jniEnv->GetObjectClass(obj);
    jfieldID intFid = jniEnv->GetFieldID(internalObjClass, fieldName, "I");
    jint intNum = jniEnv->GetIntField(obj, intFid);
    jniEnv->DeleteLocalRef(internalObjClass);
    return intNum;
}

jboolean getBooleanField(JNIEnv *jniEnv, jobject obj, const char *fieldName) {
    jclass internalObjClass = jniEnv->GetObjectClass(obj);
    jfieldID fid = jniEnv->GetFieldID(internalObjClass, fieldName, "Z");
    jboolean jboolean1 = jniEnv->GetBooleanField(obj, fid);
    jniEnv->DeleteLocalRef(internalObjClass);
    return jboolean1;
}

jstring getStringField(JNIEnv *jniEnv, jobject obj, const char *fieldName) {
    jclass internalObjClass = jniEnv->GetObjectClass(obj);
    jfieldID strFid = jniEnv->GetFieldID(internalObjClass, fieldName, "Ljava/lang/String;");
    jobject str = jniEnv->GetObjectField(obj, strFid);
    jniEnv->DeleteLocalRef(internalObjClass);
    return static_cast<jstring>(str);
}


std::string getStringFromJString(JNIEnv *jniEnv, jstring str) {
    const char *temp = jniEnv->GetStringUTFChars(str, NULL);
    jint len = jniEnv->GetStringLength(str);
    char *ret = static_cast<char *>(malloc(len));
    strcpy(ret, temp);
    jniEnv->ReleaseStringUTFChars(str, temp);
    return ret;
}

X264Param *switchX264Param(JNIEnv *jniEnv, jobject obj) {
    X264Param *x264Param = new X264Param();

    jint widthIN = getIntField(jniEnv, obj, "widthIN");
    jint heightIN = getIntField(jniEnv, obj, "heightIN");
    jint widthOUT = getIntField(jniEnv, obj, "widthOUT");
    jint heightOUT = getIntField(jniEnv, obj, "heightOUT");
    jint bitrate = getIntField(jniEnv, obj, "bitrate");
    jint fps = getIntField(jniEnv, obj, "fps");
    jint gop = getIntField(jniEnv, obj, "gop");
    jint bFrameCount = getIntField(jniEnv, obj, "bFrameCount");
    jboolean useSlice = getBooleanField(jniEnv, obj, "useSlice");
    jstring preset = getStringField(jniEnv, obj, "preset");
    jstring tune = getStringField(jniEnv, obj, "tune");
    jstring profile = getStringField(jniEnv, obj, "profile");
    jstring bitrateCtrl = getStringField(jniEnv, obj, "bitrateCtrl");

    x264Param->setWidthIN(widthIN);
    x264Param->setHeightIN(heightIN);
    x264Param->setWidthOUT(widthOUT);
    x264Param->setHeightOUT(heightOUT);
    x264Param->setBitrate(bitrate);
    x264Param->setFps(fps);
    x264Param->setGop(gop);
    x264Param->setBFrameCount(bFrameCount);
    x264Param->setUseSlice(useSlice);
    x264Param->setPreset(getStringFromJString(jniEnv, preset));
    x264Param->setTune(getStringFromJString(jniEnv, tune));
    x264Param->setProfile(getStringFromJString(jniEnv, profile));
    x264Param->setBitrateCtrl(getStringFromJString(jniEnv, bitrateCtrl));

    return x264Param;
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
        {"init",               "()I",                                                       (void *) init},
        {"release",            "()I",                                                       (void *) release},
        {"encoderVideoinit",   "(Lcom/kedacom/demo/appcameratoh264/jni/X264Param;)I",                                     (void *) encoderVideoinit},
        {"encoderVideoEncode", "([BII[B[I)I",                                               (void *) encoderVideoEncode},
        {"encoderAudioInit",   "(III)I",                                                    (void *) encoderAudioInit},
        {"encoderAudioEncode", "([BI[BI)I",                                                 (void *) encoderAudioEncode},
        {"muxMp4",             "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I", (void *) muxMp4}

};


std::string myClassName = "com/kedacom/demo/appcameratoh264/jni/FFmpegjni";

JNIEXPORT jint
JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    return JniHelper::handleJNILoad(vm, reserved, myClassName,
                                 nativeMethod, sizeof(nativeMethod) / sizeof(nativeMethod[0]));
}