//
// Created by yuhanxun on 2018/8/16.
//

#ifndef FFMPEGPLAYER_FFMPEGRTSP_H
#define FFMPEGPLAYER_FFMPEGRTSP_H


#include <cstdint>
#include <string>
#include <Thread.hpp>
#include <jni.h>
#include "FFmpegRtsp.h"

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavdevice/avdevice.h>
};

class FFmpegRtsp : public Thread {
public:
    class OnDataCallbackListener {
    public:
        virtual void onDataCallback(JNIEnv *jniEnv, jobject obj, uint8_t *data, int32_t size) = 0;
    };


public:
    FFmpegRtsp(JavaVM *javaVM, jobject obj);

    ~FFmpegRtsp();

    void play(std::string url);

    void setOnDataCallbackListener(OnDataCallbackListener *listener);

    void stopPlay();

    void run() override;

public:
    OnDataCallbackListener *onDataCallbackListener;


private:
    JavaVM *javaVM;
    jobject obj;
    JNIEnv *jniEnv;
    std::string url;
    AVPacket *pPacket;
    AVFrame *pAvFrame, *pFrameBGR;
    AVFormatContext *pFormatCtx;
    AVCodecContext *pCodecCtx;
    bool isStop;
};

#endif //FFMPEGPLAYER_FFMPEGRTSP_H
