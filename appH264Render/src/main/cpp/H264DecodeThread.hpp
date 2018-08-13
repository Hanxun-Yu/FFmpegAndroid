//
// Created by yuhanxun on 2018/8/7.
//

#ifndef FFMPEGPLAYER_RTPTOH264THREAD_HPP
#define FFMPEGPLAYER_RTPTOH264THREAD_HPP

#include "ThreadHandler.h"

extern "C" {
#include <include/libavcodec/avcodec.h>
#include <libavutil/imgutils.h>
#include <libavutil/samplefmt.h>
#include <libavutil/timestamp.h>
#include <libavformat/avformat.h>

};


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

        //264 -> yuv
//        LOGD("264 -> yuv data:%u", data->getSize());
        avpkt.size = data->getSize();
        avpkt.data = data->getP_data();
        LOGE("1");
        if (int ret = avcodec_send_packet(codec_ctx, &avpkt)) {
            char *s = "haa";
            if (ret == AVERROR(EAGAIN)) {
                s = "EAGAIN";
            } else if (ret == AVERROR_EOF) {
                s = "AVERROR_EOF";
            } else if (ret == AVERROR(EINVAL)) {
                s = "EINVAL";
            } else if (ret == AVERROR(ENOANO)) {
                s = "ENOANO";
            }

            LOGE("%s %d ret:%s avcodec_send_packet fail\n", __func__, __LINE__, s);
            return NULL;
        }
        if (avcodec_receive_frame(codec_ctx, frame)) {
            LOGE("%s %d avcodec_receive_frame fail\n", __func__, __LINE__);
            return NULL;
        }
        uint8_t *outputframe;
//        LOGE("2");

        switch (codec_ctx->pix_fmt) {
            case AV_PIX_FMT_YUVJ420P: {
                case AV_PIX_FMT_YUV422P:
                case AV_PIX_FMT_YUV420P:
//                outputframe = static_cast<uint8_t *>(malloc(frame->width * frame->height * 2));
//                int index = 0;
//                int y_i = 0, u_i = 0, v_i = 0;
//                for (index = 0; index < frame->width * frame->height * 2;) {
//                    outputframe[index++] = frame->data[0][y_i++];
//                    outputframe[index++] = frame->data[1][u_i++];
//                    outputframe[index++] = frame->data[0][y_i++];
//                    outputframe[index++] = frame->data[2][v_i++];
//                }
//                break;
                    int w = frame->width;
                int h = frame->height;
                outputframe = static_cast<uint8_t *>(malloc(w * h * 3 / 2));
                int index = 0;
                int y_start = 0;
                int u_start = w * h;
                int v_start = w * h * 5 / 4;
                for (index = 0; index < w * h; index++) {
                    outputframe[y_start++] = frame->data[0][index];
                    if (index < (w * h / 4)) {
                        outputframe[u_start++] = frame->data[1][index];
                        outputframe[v_start++] = frame->data[2][index];
                    }
                }
                break;
//            case AV_PIX_FMT_YUVJ420P: {
////                YUV420p的像素颜色范围是[16,235]，16表示黑色，235表示白色
////                YUVJ420P的像素颜色范围是[0,255]。0表示黑色，255表示白色
//                LOGD("AV_PIX_FMT_YUVJ420P");
//                break;
//            }
            }
            default: {
                LOGE("default format:%d\n", codec_ctx->pix_fmt);
                return NULL;
            }
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
        return yuvData;
    }

    bool initFFmpeg() {
        LOGD("initFFmpeg");
        avcodec_register_all();
        codec = avcodec_find_decoder(AV_CODEC_ID_H264);
        if (!codec) {
            LOGE("avcodec_find_decoder fail\n");
            return false;
        }
        codec_ctx = avcodec_alloc_context3(codec);
        if (!codec_ctx) {
            LOGE("avcodec_alloc_context3 fail\n");
            return false;
        }
        if (avcodec_open2(codec_ctx, codec, NULL) < 0) {
            LOGE("avcodec_open2 fail\n");
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
