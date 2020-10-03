//
// Created by yuhanxun on 2018/8/7.
//

#ifndef FFMPEGPLAYER_RTPTOH264THREAD_HPP
#define FFMPEGPLAYER_RTPTOH264THREAD_HPP

#include "ThreadHandler.h"

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavutil/imgutils.h>
#include <libavutil/samplefmt.h>
#include <libavutil/timestamp.h>
#include <libavformat/avformat.h>
#include <libavcodec/jni.h>

};

//解码线程
class H264DecodeThread : public ThreadHandler {
public:
    H264DecodeThread(JavaVM *javaVM, jobject jobj) : ThreadHandler(javaVM, jobj) {
        initFFmpeg();
    }

private:
    AVCodec *codec = NULL;
    AVCodecContext *codec_ctx = NULL;
    AVFrame *frame = NULL;
    AVPacket avpkt;
    bool codecInited = false;

    YuvData *handleData(ByteData *data) override {
        if (!codecInited) {
            LOGE("ffmpeg not init");
            return NULL;
        }
        clock_t start, ends;
        start = clock();

        //264 -> yuv
//        LOGD("264 -> yuv data:%u", data->getSize());
        avpkt.size = data->getSize();
        avpkt.data = data->getP_data();
//        LOGE("1");
        if (avcodec_send_packet(codec_ctx, &avpkt)) {
//            char *s = "haa";
//            if (ret == AVERROR(EAGAIN)) {
//                s = "EAGAIN";
//            } else if (ret == AVERROR_EOF) {
//                s = "AVERROR_EOF";
//            } else if (ret == AVERROR(EINVAL)) {
//                s = "EINVAL";
//            } else if (ret == AVERROR(ENOANO)) {
//                s = "ENOANO";
//            }

            LOGE("%s %d avcodec_send_packet fail\n", __func__, __LINE__);
            return NULL;
        }
        if (avcodec_receive_frame(codec_ctx, frame)) {
            LOGE("%s %d avcodec_receive_frame fail\n", __func__, __LINE__);
            return NULL;
        }

        uint8_t *outputframe;
//        LOGE("2");

        switch (codec_ctx->pix_fmt) {
//                  YUV420p的像素颜色范围是[16,235]，16表示黑色，235表示白色
//                YUVJ420P的像素颜色范围是[0,255]。0表示黑色，255表示白色
            case AV_PIX_FMT_YUVJ420P:
            case AV_PIX_FMT_YUV422P:
            case AV_PIX_FMT_YUV420P: {
                int w = frame->width;
                int h = frame->height;
                outputframe = static_cast<uint8_t *>(malloc(w * h * 3 / 2));
                int index = 0;
                int y_start = 0;
                int u_start = w * h;
                int v_start = w * h * 5 / 4;
                int count = w * h / 4;

                memcpy(outputframe, frame->data[0], u_start);
                memcpy(outputframe + u_start, frame->data[1], count);
                memcpy(outputframe + v_start, frame->data[2], count);

                break;
            }
            default:
                LOGE("default format:%d\n", codec_ctx->pix_fmt);
                return NULL;
        }
//        LOGE("3");
        YuvData *yuvData = new YuvData();
        yuvData->setWidth(codec_ctx->width);
        yuvData->setHeight(codec_ctx->height);
        yuvData->setSize(codec_ctx->width * codec_ctx->height * 3 / 2);
        yuvData->setP_data(outputframe);
//        LOGE("4");

//        *width = codec_ctx->width;
//        *height = codec_ctx->height;
//        *pixfmt = codec_ctx->pix_fmt;
        ends = clock();
        LOGE("handle time:%lf", (double) (ends - start) / CLOCKS_PER_SEC);
        return yuvData;
    }

    bool initFFmpeg() {
        av_jni_set_java_vm(this->javaVM, NULL);
        LOGD("initFFmpeg");
        avcodec_register_all();
        codec = avcodec_find_decoder(AV_CODEC_ID_H264);

//        codec = avcodec_find_decoder_by_name("h264_mediacodec");
        if (!codec) {
            LOGE("avcodec_find_decoder fail\n");
            return false;
        }
        codec_ctx = avcodec_alloc_context3(codec);
        if (!codec_ctx) {
            LOGE("avcodec_alloc_context3 fail\n");
            return false;
        }
//        if (codec->capabilities & CODEC_CAP_TRUNCATED)
//            codec_ctx->flags |= CODEC_FLAG_TRUNCATED;
        codec_ctx->thread_count = 20;
        int error;
        if ((error = avcodec_open2(codec_ctx, codec, NULL)) < 0) {
            char *errbuf = static_cast<char *>(malloc(100));
            av_strerror(error, errbuf, 100);
            LOGE("avcodec_open2 fail %s \n", errbuf);

            return false;
        }
//        codec_ctx->pix_fmt = AV_PIX_FMT_YUV422P;
        frame = av_frame_alloc();
        if (!frame) {
            LOGE("av_frame_alloc fail\n");
            return false;
        }
        av_init_packet(&avpkt);
        codecInited = true;
        return true;
    }
};


#endif //FFMPEGPLAYER_RTPTOH264THREAD_HPP
