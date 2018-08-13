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

};

class VideoDecoder {
public:
    static class OnDecoderListener {
    public:
        virtual void onRGB(JNIEnv *jniEnv, jobject jobject,YuvData *data) = 0;
    };

    VideoDecoder(JavaVM *javaVM, jobject jobject);
    ~VideoDecoder();

    void init();
    void putFrame(u_int8_t *p_data, uint32_t size);
    void setOnDecoderListener(OnDecoderListener *onDecoderListener);

private:
    ThreadHandler* decodeThread;
    friend void onRGBTransform(JNIEnv *jniEnv, jobject jobject,YuvData *data);
public :
    OnDecoderListener *onDecoderListener;

    class ThreadCallback : public ThreadHandler::ICallback {
        VideoDecoder *videoDecoder;
    public:
        ThreadCallback(VideoDecoder *videoDecoder);

    private:
        void onCallback(JNIEnv *jniEnv, jobject jobject, YuvData *data) override;
    };

};


#endif //CEEWAIPCSDKDEMO_VIDEODECODER_H
