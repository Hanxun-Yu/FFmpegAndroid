
#include <jni.h>
#include <string>
#include <JniHelper.h>
#include "../include/libyuv.h"

extern "C" {

}

void _nv21ToI420(jbyte *src_nv21_data, jint width, jint height, jbyte *src_i420_data);
void _nv12ToI420Rotate(jbyte *src_nv12_data, jint &width, jint &height, jbyte *src_i420_data,jint degree);

void _nv21ToNv12(jbyte *src_nv21_data, jint width, jint height, jbyte *src_nv12_data);

void _rotateI420(jbyte *src_i420_data, jint &width, jint &height, jbyte *dst_i420_data, jint degree);
void setWHArr(JNIEnv *env,jintArray arr, jint w,jint h);


JNIEXPORT void JNICALL nv21ToI420(JNIEnv *env, jclass clazz, jbyteArray src, jint w, jint h,
                                  jbyteArray dst, jintArray dst_wh, jint degree) {
    jbyte *Src_data = env->GetByteArrayElements(src, NULL);

    int32 dstlen = w * h * 3 >> 1;
    jbyte *Dst_data = (jbyte *) malloc(sizeof(jbyte) * dstlen);

    _nv21ToI420(Src_data, w, h, Dst_data);


    LOGE("nv21ToI420 degree:%d",degree);
    if (degree != 0) {
        jbyte *temp = (jbyte *) malloc(sizeof(jbyte) * w * h * 3 >> 1 );
        _rotateI420(Dst_data, w, h, temp, degree);
        free(Dst_data);
        Dst_data = temp;
//        rotateI420(Src_data, dst_width, dst_height, Dst_data, degree);
    }

    setWHArr(env,dst_wh,w,h);
    env->SetByteArrayRegion(dst,0,dstlen,Dst_data);
    env->ReleaseByteArrayElements(src,Src_data,0);
    env->ReleaseByteArrayElements(dst,Dst_data,0);
}



JNIEXPORT void JNICALL nv12ToI420(JNIEnv *env, jclass clazz, jbyteArray src, jint w, jint h,
                                  jbyteArray dst, jintArray dst_wh, jint degree) {
    jbyte *Src_data = env->GetByteArrayElements(src, NULL);

    int32 dstlen = w * h * 3 >> 1;
    jbyte *Dst_data = (jbyte *) malloc(sizeof(jbyte) * dstlen);

    _nv12ToI420Rotate(Src_data,w,h,Dst_data,degree);

    setWHArr(env,dst_wh,w,h);
    env->SetByteArrayRegion(dst,0,dstlen,Dst_data);

}

JNIEXPORT void JNICALL yv12ToI420(JNIEnv *env, jclass clazz, jbyteArray src, jint w, jint h,
                                  jbyteArray dst, jintArray dst_wh, jint degree) {

}


//NV21格式数据排列方式是YYYYYYYY（w*h）VUVUVUVU(w*h/2)，
//NV21转化为YUV420P数据
void _nv21ToI420(jbyte *src_nv21_data, jint width, jint height, jbyte *src_i420_data) {
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

void setWHArr(JNIEnv *env,jintArray arr, jint w,jint h) {
    jint* dst_wh_arr = (jint *) malloc(sizeof(jint) * 2 );
    dst_wh_arr[0]=w;
    dst_wh_arr[1]=h;
    env->SetIntArrayRegion(arr,0,2,dst_wh_arr);
    env->ReleaseIntArrayElements(arr,dst_wh_arr,0);

}

//对于NV12的格式，排列方式是YYYYYYYY（w*h）UVUVUVUV（w*h/2）
void _nv12ToI420Rotate(jbyte *src_nv12_data, jint &width, jint &height, jbyte *src_i420_data,jint degree) {
    //Y通道数据大小
    jint src_y_size = width * height;
    //U通道数据大小
    jint src_u_size = (width >> 1) * (height >> 1);

    //NV21中Y通道数据
    jbyte *src_nv12_y_data = src_nv12_data;
    //由于是连续存储的Y通道数据后即为VU数据，它们的存储方式是交叉存储的
    jbyte *src_nv12_vu_data = src_nv12_data + src_y_size;

    //YUV420P中Y通道数据
    jbyte *src_i420_y_data = src_i420_data;
    //YUV420P中U通道数据
    jbyte *src_i420_u_data = src_i420_data + src_y_size;
    //YUV420P中V通道数据
    jbyte *src_i420_v_data = src_i420_data + src_y_size + src_u_size;

    //直接调用libyuv中接口，把NV21数据转化为YUV420P标准数据，此时，它们的存储大小是不变的
    libyuv::NV12ToI420Rotate((const uint8 *) src_nv12_y_data, width,
                             (const uint8 *) src_nv12_vu_data, width,
                             (uint8 *) src_i420_y_data, width,
                             (uint8 *) src_i420_u_data, width >> 1,
                             (uint8 *) src_i420_v_data, width >> 1,
                             width, height, static_cast<libyuv::RotationMode>(degree));

    if (degree == libyuv::kRotate90 || degree == libyuv::kRotate270) {
        int32 temp = width;
        width = height;
        height = temp;
    }

}


void _nv21ToNv12(jbyte *src_nv21_data, jint width, jint height, jbyte *src_nv12_data) {
    //Y通道数据大小
    jint src_y_size = width * height;
    //U通道数据大小
    jint src_u_size = (width >> 1) * (height >> 1);


    memcpy(src_nv12_data, src_nv21_data, src_y_size);
    for (int i = 0; i < src_u_size; i++) {
        *(src_nv12_data + src_y_size + i * 2) = *(src_nv21_data + src_y_size + i * 2 + 1);
        *(src_nv12_data + src_y_size + i * 2 + 1) = *(src_nv21_data + src_y_size + i * 2);
    }
}


void
_rotateI420(jbyte *src_i420_data, jint &w, jint &h, jbyte *dst_i420_data, jint degree) {
    jint src_i420_y_size = w * h;
    jint src_i420_u_size = (w >> 1) * (h >> 1);

    jbyte *src_i420_y_data = src_i420_data;
    jbyte *src_i420_u_data = src_i420_data + src_i420_y_size;
    jbyte *src_i420_v_data = src_i420_data + src_i420_y_size + src_i420_u_size;

    jbyte *dst_i420_y_data = dst_i420_data;
    jbyte *dst_i420_u_data = dst_i420_data + src_i420_y_size;
    jbyte *dst_i420_v_data = dst_i420_data + src_i420_y_size + src_i420_u_size;

    //要注意这里的width和height在旋转之后是相反的
    if (degree == libyuv::kRotate90 || degree == libyuv::kRotate270) {
        libyuv::I420Rotate((const uint8 *) src_i420_y_data, w,
                           (const uint8 *) src_i420_u_data, w >> 1,
                           (const uint8 *) src_i420_v_data, w >> 1,
                           (uint8 *) dst_i420_y_data, h,
                           (uint8 *) dst_i420_u_data, h >> 1,
                           (uint8 *) dst_i420_v_data, h >> 1,
                           w, h,
                           (libyuv::RotationMode) degree);
        int32 temp = w;
        w = h;
        h = temp;
    }
}



JNINativeMethod nativeMethod[] = {
        {"nv12ToI420", "([BII[B[II)V", (void *) nv12ToI420},
        {"nv21ToI420", "([BII[B[II)V", (void *) nv21ToI420},
        {"yv12ToI420", "([BII[B[II)V", (void *) yv12ToI420}
};


std::string myClassName = "com/kedacom/demo/appcameratoh264/jni/YuvJni";

JNIEXPORT jint
JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    return JniHelper::handleJNILoad(vm, reserved, myClassName,
                                    nativeMethod,
                                    sizeof(nativeMethod) / sizeof(nativeMethod[0]));
}
