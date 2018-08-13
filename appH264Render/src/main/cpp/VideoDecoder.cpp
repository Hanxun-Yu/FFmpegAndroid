//
// Created by yuhanxun on 2018/8/10.
//

#include "VideoDecoder.h"

VideoDecoder::VideoDecoder(JavaVM *javaVM, jobject jobject) {
    decodeThread = new H264DecodeThread(javaVM, jobject);
}


void VideoDecoder::putFrame(u_int8_t *p_data, uint32_t size) {
    LOGD("putFrame size:%u", size);
    decodeThread->putData(p_data, size);
}

VideoDecoder::~VideoDecoder() {
    delete decodeThread;
}

void VideoDecoder::init() {
    //ffmpeg init
    decodeThread->setCallback(new VideoDecoder::ThreadCallback(this));
    decodeThread->start();
}

void VideoDecoder::setOnDecoderListener(VideoDecoder::OnDecoderListener *onDecoderListener) {
    this->onDecoderListener = onDecoderListener;
}

AVFrame *pFrame = av_frame_alloc();

void VideoDecoder::ThreadCallback::onCallback(JNIEnv *jniEnv, jobject jobj, YuvData *data) {
    //yuv->rgb
//    LOGD("VideoDecoder::ThreadCallback::onCallback");
    if (data != NULL) {
//        LOGD("callback yun size:%u w:%u h:%u", data->getSize(), data->getWidth(),
//             data->getHeight());



        // 由于解码出来的帧格式不是RGBA的,在渲染之前需要进行格式转换
//        struct SwsContext *sws_ctx = sws_getContext(data->getWidth(),
//                                                    data->getHeight(),
//                                                    AV_PIX_FMT_YUVJ420P,
//                                                    data->getWidth(),
//                                                    data->getHeight(),
//                                                    AV_PIX_FMT_RGBA,
//                                                    SWS_BILINEAR,
//                                                    NULL,
//                                                    NULL,
//                                                    NULL);

//        pFrame->data = data->getP_data();
//        // 格式转换
//        sws_scale(sws_ctx, (uint8_t const *const *) pFrame->data,
//                  pFrame->linesize, 0, pCodecCtx->height,
//                  pFrameRGBA->data, pFrameRGBA->linesize);
//        libyuv::I420ToRGB565()
        int w = data->getWidth();
        int h = data->getHeight();
        u_int8_t *buffer_dest = (u_int8_t *) malloc(w * h * 4);


        libyuv::I420ToABGR((const uint8 *) data->getP_data(), w,
                           (const uint8 *) (data->getP_data() + w * h), w/2 ,
                           (const uint8 *) (data->getP_data() + w * h * 5 / 4), w/2,
                           buffer_dest, w * 4,
                           w, h);
        free(data->getP_data());
        data->setP_data(buffer_dest);
        this->videoDecoder->onDecoderListener->onRGB(jniEnv, jobj, data);
    }
    //callback to ui
//    this->onDecoderListener
}

VideoDecoder::ThreadCallback::ThreadCallback(VideoDecoder *videoDecoder) : videoDecoder(
        videoDecoder) {}


