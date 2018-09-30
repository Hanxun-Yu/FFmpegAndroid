

#include "VideoEncoder.h"
#include "JniHelper.h"

extern "C" {

}
VideoEncoder *frameEncoder;
X264Param *switchX264Param(JNIEnv *jniEnv, jobject obj);

JNIEXPORT jint JNICALL init
        (JNIEnv *env, jclass clazz) {
    LOGE("init");

    return 0;
}

JNIEXPORT jint JNICALL release
        (JNIEnv *env, jclass clazz) {
    LOGE("release");

//    free(temp_i420_data);
//    free(temp_i420_data_scale);
//    free(temp_i420_data_rotate);
    frameEncoder->close();
    delete frameEncoder;
//    free(audioEncoder);
//    free(rtmpLivePublish);
    return 0;
}


JNIEXPORT jint JNICALL encoderVideoinit
        (JNIEnv *env, jclass clazz, jobject params) {
    LOGE("encoderVideoinit");

    frameEncoder = new VideoEncoder();
    frameEncoder->setParams(switchX264Param(env, params));
//    frameEncoder->setInWidth(in_width);
//    frameEncoder->setInHeight(in_height);
//    frameEncoder->setOutWidth(out_width);
//    frameEncoder->setOutHeight(out_height);
//    frameEncoder->setBitrate(bitrate);
    frameEncoder->open();
    frameEncoder->getParams()->printSelf();
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
    LOGE("switchX264Param");

    X264Param *x264Param = new X264Param();

    jint widthIN = getIntField(jniEnv, obj, "widthIN");
    jint heightIN = getIntField(jniEnv, obj, "heightIN");
    jint widthOUT = getIntField(jniEnv, obj, "widthOUT");
    jint heightOUT = getIntField(jniEnv, obj, "heightOUT");
    jint bitrate = getIntField(jniEnv, obj, "byterate");
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

    LOGE("encoderVideoEncode");
    jbyte *Src_data = env->GetByteArrayElements(srcFrame, NULL);
    jbyte *Dst_data = env->GetByteArrayElements(dstFrame, NULL);
    jint *dstFrameSize = env->GetIntArrayElements(outFramewSize, NULL);
    frameEncoder->getParams()->printSelf();


    int numNals = frameEncoder->encodeFrame((char *) Src_data, frameSize, fps, (char *) Dst_data,
                                            dstFrameSize);
//    int numNals = -1;
    env->ReleaseByteArrayElements(dstFrame, Dst_data, 0);
    env->ReleaseByteArrayElements(srcFrame, Src_data, 0);
    env->ReleaseIntArrayElements(outFramewSize, dstFrameSize, 0);
    return numNals;
}



JNINativeMethod nativeMethod[] = {
        {"init",               "()I",                                                       (void *) init},
        {"release",            "()I",                                                       (void *) release},
        {"encoderVideoinit",   "(Lcom/kedacom/demo/appcameratoh264/media/encoder/video/x264/X264Param;)I",                                     (void *) encoderVideoinit},
        {"encoderVideoEncode", "([BII[B[I)I",                                               (void *) encoderVideoEncode},

};


std::string myClassName = "com/kedacom/demo/appcameratoh264/jni/X264EncoderJni";

JNIEXPORT jint
JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    return JniHelper::handleJNILoad(vm, reserved, myClassName,
                                 nativeMethod, sizeof(nativeMethod) / sizeof(nativeMethod[0]));
}