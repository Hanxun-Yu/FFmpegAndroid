//
// Created by yuhanxun on 2018/6/15.
//

#ifndef SAMPLEJNI_JNIHELPER_H
#define SAMPLEJNI_JNIHELPER_H

#include <jni.h>
#include <string>
#include <cstdlib>
#include <string.h>

#include <android/log.h>

#define  LOG "JNILOG_xunxun"


#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG,__VA_ARGS__)
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG,__VA_ARGS__)

#define  LOGD_TAG(tag, ...)  __android_log_print(ANDROID_LOG_DEBUG,tag,__VA_ARGS__)
#define  LOGI_TAG(tag, ...)  __android_log_print(ANDROID_LOG_INFO,tag,__VA_ARGS__)
#define  LOGW_TAG(tag, ...)  __android_log_print(ANDROID_LOG_WARN,tag,__VA_ARGS__)
#define LOGE_TAG(tag, ...)  __android_log_print(ANDROID_LOG_ERROR,tag,__VA_ARGS__)
#define LOGF_TAG(tag, ...)  __android_log_print(ANDROID_LOG_FATAL,tag,__VA_ARGS__)

#define Jarr "["
#define Jvoid  "V"
#define Jchar  "C"
#define Jbyte  "B"
#define Jshort  "S"
#define Jint  "I"
#define Jlong  "J"
#define Jfloat  "F"
#define Jdouble  "D"
#define Jboolean  "Z"
#define JString  "Ljava/lang/String;"
#define JObject  "Ljava/lang/Object;"
#define JcharArr  Jarr Jchar
#define JbyteArr  Jarr Jbyte
#define JshortArr  Jarr Jshort
#define JintArr  Jarr Jint
#define JlongArr  Jarr Jlong
#define JfloatArr  Jarr Jfloat
#define JdoubleArr  Jarr Jdouble
#define JbooleanArr  Jarr Jboolean
#define JStringArr  Jarr JString
#define JObjectArr  Jarr JObject

/**
 *
 * @param x : method param
 * @param y : method return type
 *
 * You can use the macro like this:
 * SIGN(JString JbyteArr, Jvoid) ->  (Ljava/lang/String;[B)V
 */
#define _SIGN(x, y) "(" x ")" y
#define SIGN(x, y) _SIGN(x,y)


class JniHelper {
public:
    /**
     * In jni file, you can call JniHelper* jni = new JniHelper() in globle region.
     * and call handleJNILoad() in JNI_OnLoad() ,then the *env will be inited automatically
     */
//    JniHelper();

    /**
     * If not in jni file or in any function internal local region ,you must init *env manually
     * you call JniHelper* jni = new JniHelper(env)
     * @param env
     */
    JniHelper(JNIEnv *env);

    /**
     * call in JNI_OnLoad()
     * @param vm
     * @param reserved unused
     * @param myClassName eg. "com/example/libcommon/TestJnicom"
     * @param methods eg. JNINativeMethod nativeMethod[]
     * @param methodSize eg. methodSize = sizeof(nativeMethod)/sizeof(nativeMethod[0])
     * @return
     */
    static int handleJNILoad(JavaVM *vm, void *reserved, std::string myClassName,
                             const JNINativeMethod *methods, int methodSize);

    /**
     * jstring -> string
     * internal <> jstring2char_p()
     * @param jstr
     * @return
     */
    std::string jstring2string(jstring jstr);

    /**
     * jstring -> char*
     * This will malloc a new string,
     * please free it after use.
     * @param jstr
     * @return
     */
    char *jstring2char_p(jstring jstr);

    /**
     * string -> jstring
     * internal <> char_p2jstring()
     * @param str
     * @return
     */
    jstring string2jstring(std::string str);

    /**
     * By default, this use utf-8(env->NewStringUTF) encoder to convert string
     * because String in Java must has Encoder
     * @param char_p
     * @return
     */
    jstring char_p2jstring(char *char_p);

    /**
     * If @param byteArr is NULL,this will malloc automatically according to @param len
     * then please free it after use
     * @param jbyteArr :src
     * @param byteArr :target
     * @param len :The length of byteArr
     */
    void jbyteArr2byteArr(jbyteArray jbyteArr, uint8_t *&byteArr, int32_t &len);


    /**
     * uint8_t* -> jbyteArray
     * @param byteArr
     * @param len
     * @return
     */
    jbyteArray byteArr2jbyteArr(uint8_t *byteArr, int32_t len);

    /**
     * remember free dst* after use
     * @param src
     */
    char *strcpyWrap(char *src);

    /**
     * Java Obj get set
     */
    jobject createObject(char *classpath, char *constructorSignature, ...);

    void invokeVoidMethod(jobject obj, char *methodName, char *methodSignature, ...);

    void invokeVoidStaticMethod(char *classpath, char *methodName, char *methodSignature, ...);


    jfieldID getFieldID(jobject obj, const char *fieldName, const char *typeSignature);

    jint getIntField(jobject obj, const char *fieldName);

    jlong getLongField(jobject obj, const char *fieldName);

    jfloat getFloatField(jobject obj, const char *fieldName);

    jdouble getDoubleField(jobject obj, const char *fieldName);

    jboolean getBooleanField(jobject obj, const char *fieldName);

    jstring getStringField(jobject obj, const char *fieldName);

    jbyte getByteField(jobject obj, const char *fieldName);

    jchar getCharField(jobject obj, const char *fieldName);

    jshort getShortField(jobject obj, const char *fieldName);

    jobject getObjectField(jobject obj, const char *fieldName, const char *classpath);

    jobject getObjectField(jobject obj, const char *fieldName);


    void setIntField(jobject obj, const char *fieldName, jint val);

    void setLongField(jobject obj, const char *fieldName, jlong val);

    void setFloatField(jobject obj, const char *fieldName, jfloat val);

    void setDoubleField(jobject obj, const char *fieldName, jdouble val);

    void setBooleanField(jobject obj, const char *fieldName, jboolean val);

    void setStringField(jobject obj, const char *fieldName, jstring val);

    void setByteField(jobject obj, const char *fieldName, jbyte val);

    void setCharField(jobject obj, const char *fieldName, jchar val);

    void setShortField(jobject obj, const char *fieldName, jshort val);

    void setObjectField(jobject obj, const char *fieldName, const char *classpath, jobject val);

    void setObjectField(jobject obj, const char *fieldName, jobject val);

private:
    JNIEnv *env;
};


#endif //SAMPLEJNI_JNIHELPER_H
