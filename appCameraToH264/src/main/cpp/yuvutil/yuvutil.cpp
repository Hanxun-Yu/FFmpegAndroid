
#include <jni.h>
#include <string>
#include <Jnicom.h>
#include "../include/libyuv.h"

extern "C" {

}
Jnicom *jnicom = new Jnicom();

void nv21ToI420(jbyte *src_nv21_data, jint width, jint height, jbyte *src_i420_data);

void scaleI420(jbyte *src_i420_data, jint width, jint height, jbyte *dst_i420_data, jint dst_width,
               jint dst_height, jint mode);

JNIEXPORT jint JNICALL compressYUV
        (JNIEnv *env, jclass clazz, jbyteArray src, jint width, jint height,
         jbyteArray dst, jint dst_width, jint dst_height, jint mode, jint degree,
         jboolean isMirror) {
    jbyte *Src_data = env->GetByteArrayElements(src, NULL);
    jbyte *Dst_data = env->GetByteArrayElements(dst, NULL);

//    jbyteArray temp_i420_array = env->NewByteArray(env->GetArrayLength(src));
//    jbyte *temp_i420_data = env->GetByteArrayElements(temp_i420_array, NULL);

    jbyte *temp_i420_data = static_cast<jbyte *>(malloc(env->GetArrayLength(src)));
    jbyte *temp_i420_data_scale = static_cast<jbyte *>(malloc(env->GetArrayLength(src)));


    //nv21转化为i420(标准YUV420P数据) 这个temp_i420_data大小是和Src_data是一样的
//    nv21ToI420(Src_data, width, height, temp_i420_data);
    //进行缩放的操作，这个缩放，会把数据压缩
//    scaleI420(temp_i420_data, width, height, temp_i420_data_scale, dst_width, dst_height, mode);
//    //如果是前置摄像头，进行镜像操作
//    if (isMirror) {
//        //进行旋转的操作
//        rotateI420(temp_i420_data_scale, dst_width, dst_height, temp_i420_data_rotate, degree);
//        //因为旋转的角度都是90和270，那后面的数据width和height是相反的
//        mirrorI420(temp_i420_data_rotate, dst_height, dst_width, Dst_data);
//    } else {
//        //进行旋转的操作
//        rotateI420(temp_i420_data_scale, dst_width, dst_height, Dst_data, degree);
//    }
    nv21ToI420(Src_data, width, height, Dst_data);
    env->ReleaseByteArrayElements(dst, Dst_data, 0);
    env->ReleaseByteArrayElements(src, Src_data, 0);
    return 0;
}

JNIEXPORT jint JNICALL cropYUV
        (JNIEnv *env, jclass clazz, jbyteArray src, jint width, jint height,
         jbyteArray dst, jint dst_width, jint dst_height, jint left, jint top) {

    return 0;
}

//NV21格式数据排列方式是YYYYYYYY（w*h）VUVUVUVU(w*h/2)，
//对于NV12的格式，排列方式是YYYYYYYY（w*h）UVUVUVUV（w*h/2）
//NV21转化为YUV420P数据
void nv21ToI420(jbyte *src_nv21_data, jint width, jint height, jbyte *src_i420_data) {
    //Y通道数据大小
    jint src_y_size = width * height;
    //U通道数据大小
    jint src_u_size = (width >> 1) * (height >> 1);

    //NV21中Y通道数据
    jbyte *src_nv21_y_data = src_nv21_data;
    //由于是连续存储的Y通道数据后即为VU数据，它们的存储方式是交叉存储的
    jbyte *src_nv21_vu_data = src_nv21_data + src_y_size;

    //YUV420P中Y通道数据
    jbyte *src_i420_y_data = src_i420_data;
    //YUV420P中U通道数据
    jbyte *src_i420_u_data = src_i420_data + src_y_size;
    //YUV420P中V通道数据
    jbyte *src_i420_v_data = src_i420_data + src_y_size + src_u_size;

    //直接调用libyuv中接口，把NV21数据转化为YUV420P标准数据，此时，它们的存储大小是不变的
    libyuv::NV21ToI420((const uint8 *) src_nv21_y_data, width,
                       (const uint8 *) src_nv21_vu_data, width,
                       (uint8 *) src_i420_y_data, width,
                       (uint8 *) src_i420_u_data, width >> 1,
                       (uint8 *) src_i420_v_data, width >> 1,
                       width, height);
}

//进行缩放操作，此时是把1080 * 1920的YUV420P的数据 ==> 480 * 640的YUV420P的数据
void scaleI420(jbyte *src_i420_data, jint width, jint height, jbyte *dst_i420_data, jint dst_width,
               jint dst_height, jint mode) {
    //Y数据大小width*height，U数据大小为1/4的width*height，V大小和U一样，一共是3/2的width*height大小
    jint src_i420_y_size = width * height;
    jint src_i420_u_size = (width >> 1) * (height >> 1);
    //由于是标准的YUV420P的数据，我们可以把三个通道全部分离出来
    jbyte *src_i420_y_data = src_i420_data;
    jbyte *src_i420_u_data = src_i420_data + src_i420_y_size;
    jbyte *src_i420_v_data = src_i420_data + src_i420_y_size + src_i420_u_size;

    //由于是标准的YUV420P的数据，我们可以把三个通道全部分离出来
    jint dst_i420_y_size = dst_width * dst_height;
    jint dst_i420_u_size = (dst_width >> 1) * (dst_height >> 1);
    jbyte *dst_i420_y_data = dst_i420_data;
    jbyte *dst_i420_u_data = dst_i420_data + dst_i420_y_size;
    jbyte *dst_i420_v_data = dst_i420_data + dst_i420_y_size + dst_i420_u_size;

    //调用libyuv库，进行缩放操作
    libyuv::I420Scale((const uint8 *) src_i420_y_data, width,
                      (const uint8 *) src_i420_u_data, width >> 1,
                      (const uint8 *) src_i420_v_data, width >> 1,
                      width, height,
                      (uint8 *) dst_i420_y_data, dst_width,
                      (uint8 *) dst_i420_u_data, dst_width >> 1,
                      (uint8 *) dst_i420_v_data, dst_width >> 1,
                      dst_width, dst_height,
                      (libyuv::FilterMode) mode);
}


JNINativeMethod nativeMethod[] = {
        {"play", "([BII[BIIIIZ)I", (void *) compressYUV},
        {"play", "([BII[BIIII)I",  (void *) cropYUV}
};


std::string myClassName = "com/kedacom/demo/appcameratoh264/jni/YuvUtil";

JNIEXPORT jint
JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    return jnicom->handleJNILoad(vm, reserved, myClassName,
                                 nativeMethod, sizeof(nativeMethod) / sizeof(nativeMethod[0]));
}