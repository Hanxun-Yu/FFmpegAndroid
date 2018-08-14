//
// Created by yuhanxun on 2018/8/7.
//

#ifndef FFMPEGPLAYER_RTPTOH264THREAD_HPP
#define FFMPEGPLAYER_RTPTOH264THREAD_HPP

#include "bean/ByteData.hpp"
#include "ThreadHandler.h"


class RtpToH264Thread : public ThreadHandler {
public:
    RtpToH264Thread(JavaVM *javaVM, jobject jobject1) : ThreadHandler(javaVM, jobject1) {}

private:
    ByteData* handleData(ByteData* data) override {
        //rtp to h264
        LOGD("rtp->h264 data:%d",data->getSize());
        return NULL;
    }
};


#endif //FFMPEGPLAYER_RTPTOH264THREAD_HPP
