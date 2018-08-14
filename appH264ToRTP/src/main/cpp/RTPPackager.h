//
// Created by yuhanxun on 2018/8/7.
//

#ifndef FFMPEGPLAYER_RTPPACKAGER_H
#define FFMPEGPLAYER_RTPPACKAGER_H


#include <sys/types.h>
#include "H264ToRtpThread.hpp"
#include "RtpToH264Thread.hpp"

class RTPPackager {
public:
    class OnPackListener {
    public:
        virtual void onRTP(JNIEnv *jniEnv, jobject jobject, u_int8_t *p_data, u_int16_t size) = 0;
    };

    class OnUnPackListener {
    public:
        virtual void onH264(JNIEnv *jniEnv, jobject jobject, u_int8_t *p_data, u_int16_t size) = 0;
    };

public:
    RTPPackager(JavaVM *javaVM, jobject jobject1);

    ~RTPPackager();

    void putH264(u_int8_t *p_data, u_int16_t size);

    void putRTP(u_int8_t *p_data, u_int16_t size);

    void setOnPackListener(OnPackListener *onPackListener);

    void setOnUnPackListener(OnUnPackListener *onUnPackListener);



private:
    OnPackListener *onPackListener;
    OnUnPackListener *onUnPackListener;
    ThreadHandler *rtpH264Thread;
    ThreadHandler *h264RtpThread;


public:
    class RtpH264Callback : public ThreadHandler::ICallback {
    public:
        RTPPackager *rtpPackager;

        RtpH264Callback(RTPPackager *rtpPackager);

        void onCallback(JNIEnv *jniEnv, jobject jobject, YuvData *data) override;
    };

    class H264RtpCallback : public ThreadHandler::ICallback {
    public:
        RTPPackager *rtpPackager;

        H264RtpCallback(RTPPackager *rtpPackager);

        void onCallback(JNIEnv *jniEnv, jobject jobject, YuvData *data) override;

    };
};


#endif //FFMPEGPLAYER_RTPPACKAGER_H
