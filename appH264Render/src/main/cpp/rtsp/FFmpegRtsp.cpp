//
// Created by yuhanxun on 2018/8/16.
//

#include "FFmpegRtsp.h"

FFmpegRtsp::FFmpegRtsp(JavaVM *javaVM, jobject obj) {
    this->javaVM = javaVM;
    this->obj = obj;
}

FFmpegRtsp::~FFmpegRtsp() {

}

void FFmpegRtsp::play(std::string url) {
    LOGD("FFmpegRtsp::play url:%s", url.c_str());
    this->url = url;
    start();
}

void FFmpegRtsp::setOnDataCallbackListener(FFmpegRtsp::OnDataCallbackListener *listener) {
    this->onDataCallbackListener = listener;
}

void FFmpegRtsp::stopPlay() {
    LOGD("FFmpegRtsp::stopPlay()");
    this->isStop = true;
    stop();
}

void FFmpegRtsp::run() {
    LOGD("FFmpegRtsp::run()");
    try {
        this->javaVM->AttachCurrentThread(&this->jniEnv, NULL);
        pAvFrame = av_frame_alloc();
//        pFrameBGR = av_frame_alloc();

        //初始化
        avcodec_register_all();
        av_register_all();         //注册库中所有可用的文件格式和编码器
        avformat_network_init();
        avdevice_register_all();

        pFormatCtx = avformat_alloc_context();
        LOGD("FFmpegRtsp::run() 1 %s",this->url.c_str());

        AVDictionary* options = NULL;
        av_dict_set(&options, "buffer_size", "1024000", 0);
//        av_dict_set(&options, "max_delay", "500000", 0);
        av_dict_set(&options, "stimeout", "20000000", 0);  //设置超时断开连接时间
        av_dict_set(&options, "rtsp_transport", "udp", 0);

        if (int error = avformat_open_input(&pFormatCtx, this->url.c_str(), NULL, &options) < 0) {
            char errorStr[40];
            av_strerror(error, errorStr, 40);
            LOGD("FFmpegRtsp::run() error:%s", errorStr);

            throw "error";
        }
        LOGD("FFmpegRtsp::run() 2");

        avformat_find_stream_info(pFormatCtx, NULL);

        int videoIndex = -1;
        for (unsigned int i = 0; i < pFormatCtx->nb_streams; i++) //遍历各个流，找到第一个视频流,并记录该流的编码信息
        {
            if (pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
                videoIndex = i;                                     //这里获取到的videoindex的结果为1.
                break;
            }
        }
        LOGD("FFmpegRtsp::run() videoIndex:%d",videoIndex);

//        pCodecCtx = pFormatCtx->streams[videoIndex]->codec;
//        AVCodec *pCodec = avcodec_find_decoder(pCodecCtx->codec_id);
//        avcodec_open2(pCodecCtx, pCodec, NULL);

        pPacket = (AVPacket*)av_malloc(sizeof(AVPacket));
        while (!this->isStop) {
            if (av_read_frame(pFormatCtx, pPacket) >= 0) {
//                LOGD("FFmpegRtsp::run() while");

                if ((pPacket)->stream_index != videoIndex) {
                    // 包不对，不解码
                    continue;
                }
                uint8_t *ret = static_cast<uint8_t *>(malloc(pPacket->size));
                memcpy(ret,pPacket->data,pPacket->size);
                if (this->onDataCallbackListener != NULL) {
                    this->onDataCallbackListener->onDataCallback(this->jniEnv, this->obj,
                                                                 ret, pPacket->size);
                }
            }
            av_packet_unref(pPacket);
        }
    } catch (...) {
        LOGE("catch exception");
    }
    av_free(pPacket);
//    av_free(pFrameBGR);
//    avcodec_close(pCodecCtx);
    avformat_close_input(&pFormatCtx);
}
