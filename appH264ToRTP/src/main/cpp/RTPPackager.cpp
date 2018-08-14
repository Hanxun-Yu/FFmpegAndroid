//
// Created by yuhanxun on 2018/8/7.
//

#include "RTPPackager.h"

RTPPackager::RTPPackager(JavaVM *javaVM, jobject jobject1) {
    rtpH264Thread = new RtpToH264Thread(javaVM, jobject1);
    h264RtpThread = new H264ToRtpThread(javaVM, jobject1);

    rtpH264Thread->setCallback(new RTPPackager::RtpH264Callback(this));
    h264RtpThread->setCallback(new RTPPackager::H264RtpCallback(this));
    rtpH264Thread->start();
    h264RtpThread->start();
}

RTPPackager::RtpH264Callback::RtpH264Callback(RTPPackager *rtpPackager): rtpPackager(rtpPackager) {

}

void RTPPackager::RtpH264Callback::onCallback(JNIEnv *jniEnv, jobject jobject, YuvData *data) {
    if (rtpPackager->onUnPackListener != NULL) {
        rtpPackager->onUnPackListener->onH264(jniEnv,jobject,data->getP_data(), data->getSize());
    }
}

RTPPackager::H264RtpCallback::H264RtpCallback(RTPPackager *rtpPackager) : rtpPackager(rtpPackager)  {

}

void RTPPackager::H264RtpCallback::onCallback(JNIEnv *jniEnv, jobject jobject, YuvData *data) {
    if (rtpPackager->onPackListener != NULL) {
        rtpPackager->onPackListener->onRTP(jniEnv,jobject,data->getP_data(), data->getSize());
    }
}



RTPPackager::~RTPPackager() {
    rtpH264Thread->stop();
    h264RtpThread->stop();
}

void RTPPackager::putH264(u_int8_t *p_data, u_int16_t size) {
    h264RtpThread->putData(p_data, size);
}

void RTPPackager::putRTP(u_int8_t *p_data, u_int16_t size) {
    rtpH264Thread->putData(p_data, size);
}

void RTPPackager::setOnPackListener(RTPPackager::OnPackListener *onPackListener) {
    RTPPackager::onPackListener = onPackListener;
}

void RTPPackager::setOnUnPackListener(RTPPackager::OnUnPackListener *onUnPackListener) {
    RTPPackager::onUnPackListener = onUnPackListener;
}
