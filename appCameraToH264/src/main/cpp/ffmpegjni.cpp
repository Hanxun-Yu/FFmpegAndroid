
#include <jni.h>
#include <string>
#include <include/Jnicom.h>

extern "C" {

}
Jnicom *jnicom = new Jnicom();

JNIEXPORT jint JNICALL play
        (JNIEnv *env, jclass clazz, jstring path, jobject surface) {

    return 0;
}

JNINativeMethod nativeMethod[] = {
        {"play", "(Ljava/lang/String;Ljava/lang/Object;)I", (void *) play}
};


std::string myClassName = "com/example/ffmpeg/FFmpegNative";

JNIEXPORT jint
JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    return jnicom->handleJNILoad(vm, reserved, myClassName,
                                 nativeMethod, sizeof(nativeMethod) / sizeof(nativeMethod[0]));
}