//
// Created by yuhanxun on 2018/8/10.
//

#ifndef CEEWAIPCSDKDEMO_VIDEODECODER_H
#define CEEWAIPCSDKDEMO_VIDEODECODER_H

#include "util/ThreadHandler.h"
#include "H264DecodeThread.hpp"
extern "C" {
#include <jni.h>
#include <sys/types.h>
#include "libswscale/swscale.h"
#include "../include/libyuv.h"
#include <time.h>

};

class VideoDecoder {
public:
    class OnDecoderListener {
    public:
        virtual void onRGB(JNIEnv *jniEnv, jobject jobject,YuvData *data) = 0;
    };

    VideoDecoder(JavaVM *javaVM, jobject jobject);
    ~VideoDecoder();

    void init();
    void putFrame(u_int8_t *p_data, uint32_t size);
    void setOnDecoderListener(OnDecoderListener *onDecoderListener);

private:
    OnDecoderListener *onDecoderListener;
    ThreadHandler* decodeThread;
    void onRGBTransform(JNIEnv *jniEnv, jobject jobject,YuvData *data);
public :

    class ThreadCallback : public ThreadHandler::ICallback {
        VideoDecoder *videoDecoder;
    public:
        ThreadCallback(VideoDecoder *videoDecoder);

    private:
        void onCallback(JNIEnv *jniEnv, jobject jobject, YuvData *data) override;
    };

};


#endif //CEEWAIPCSDKDEMO_VIDEODECODER_H
