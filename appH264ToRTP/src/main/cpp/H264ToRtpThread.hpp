//
// Created by yuhanxun on 2018/8/7.
//

#ifndef FFMPEGPLAYER_H264TORTP_HPP
#define FFMPEGPLAYER_H264TORTP_HPP

#include "bean/ByteData.hpp"
#include "ThreadHandler.h"


class H264ToRtpThread : public ThreadHandler {
public:
    H264ToRtpThread(JavaVM *javaVM, _jobject *jobject1) : ThreadHandler(javaVM, jobject1) {}

private:
    ByteData* handleData(ByteData* data) override {
        //h264 to rtp
        LOGD("h264->rtp data:%d",data->getSize());
        ByteData* ret = new ByteData();
        ret->setP_data(new uint8_t[2]);
        ret->setSize(2);
        return ret;
    }
};

#endif //FFMPEGPLAYER_H264TORTP_HPP
