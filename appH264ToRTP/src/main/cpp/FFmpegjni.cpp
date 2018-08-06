

#include "Jnicom.h"

extern "C" {

}
Jnicom *jnicom = new Jnicom();

JNIEXPORT jint JNICALL startRTP
        (JNIEnv *env, jclass clazz, jstring h264Path, jstring accPath, jstring ip, jint port) {

    return 0;
}

JNIEXPORT jint JNICALL release
        (JNIEnv *env, jclass clazz) {
    return 0;
}


JNINativeMethod nativeMethod[] = {
        {"startRTP", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)I", (void *) startRTP},

};


std::string myClassName = "com/example/apph264tortp/jni/FFmpegJni";

JNIEXPORT jint
JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    return jnicom->handleJNILoad(vm, reserved, myClassName,
                                 nativeMethod, sizeof(nativeMethod) / sizeof(nativeMethod[0]));
}