//
// Created by yuhanxun on 2018/8/1.
//

#include "VideoEncoder.h"
#include "media/NdkMediaCodec.h"
VideoEncoder::VideoEncoder() : encoder(NULL), num_nals(0) {
//    AMediaCodec* mMediaCodec =  AMediaCodec_createEncoderByType();
//    AMediaFormat* videoFormat = AMediaFormat_new();
//    AMediaCodec_setParameters();
}

VideoEncoder::~VideoEncoder() {

}

bool VideoEncoder::open() {
    int r = 0;
    int nheader = 0;
    int header_size = 0;

    if (!validateSettings()) {
        LOGE("open failed :validateSettings return false");
        return false;
    }

    if (encoder) {
        LOGI("Already opened. first call close()");
        return false;
    }

    // set encoder parameters
//    setParams();
    //按照色度空间分配内存，即为图像结构体x264_picture_t分配内存，并返回内存的首地址作为指针
    //i_csp(图像颜色空间参数，目前只支持I420/YUV420)为X264_CSP_I420
//    x264_picture_alloc(&pic_in, params.i_csp, params.i_width, params.i_height);
    x264_picture_alloc(&pic_in, params.i_csp,x264Param->getWidthIN(),x264Param->getHeightIN());

    //create the encoder using our params 打开编码器
    encoder = x264_encoder_open(&params);

    if (!encoder) {
        LOGI("Cannot open the encoder");
        close();
        return false;
    }

    // write headers
    r = x264_encoder_headers(encoder, &nals, &nheader);
    if (r < 0) {
        LOGI("x264_encoder_headers() failed");
        return false;
    }

    return true;
}

int VideoEncoder::encodeFrame(char *inBytes, int frameSize, int pts, char *outBytes,
                              int *outFrameSize) {
    //YUV420P数据转化为h264
    int i420_y_size = x264Param->getWidthIN() * x264Param->getHeightIN();
    int i420_u_size = (x264Param->getWidthIN() >> 1) * (x264Param->getHeightIN() >> 1);
    int i420_v_size = i420_u_size;

    uint8_t *i420_y_data = (uint8_t *) inBytes;
    uint8_t *i420_u_data = (uint8_t *) inBytes + i420_y_size;
    uint8_t *i420_v_data = (uint8_t *) inBytes + i420_y_size + i420_u_size;
    //将Y,U,V数据保存到pic_in.img的对应的分量中，还有一种方法是用AV_fillPicture和sws_scale来进行变换
    memcpy(pic_in.img.plane[0], i420_y_data, i420_y_size);
    memcpy(pic_in.img.plane[1], i420_u_data, i420_u_size);
    memcpy(pic_in.img.plane[2], i420_v_data, i420_v_size);

    // and encode and store into pic_out
    pic_in.i_pts = pts;
    //最主要的函数，x264编码，pic_in为x264输入，pic_out为x264输出
    int frame_size = x264_encoder_encode(encoder, &nals, &num_nals, &pic_in,
                                         &pic_out);

    if (frame_size) {
        /*Here first four bytes proceeding the nal unit indicates frame length*/
        int have_copy = 0;
        //编码后，h264数据保存为nal了，我们可以获取到nals[i].type的类型判断是sps还是pps
        //或者是否是关键帧，nals[i].i_payload表示数据长度，nals[i].p_payload表示存储的数据
        //编码后，我们按照nals[i].i_payload的长度来保存copy h264数据的，然后抛给java端用作
        //rtmp发送数据，outFrameSize是变长的，当有sps pps的时候大于1，其它时候值为1
        for (int i = 0; i < num_nals; i++) {
            outFrameSize[i] = nals[i].i_payload;
            memcpy(outBytes + have_copy, nals[i].p_payload, nals[i].i_payload);
            have_copy += nals[i].i_payload;
        }
#ifdef ENCODE_OUT_FILE_1
        fwrite(outBytes, 1, frame_size, out1);
#endif

#ifdef ENCODE_OUT_FILE_2
        for (int i = 0; i < frame_size; i++) {
            outBytes[i] = (char) nals[0].p_payload[i];
        }
        fwrite(outBytes, 1, frame_size, out2);
        *outFrameSize = frame_size;
#endif

        return num_nals;
    }
    return -1;
}

bool VideoEncoder::close() {
    if (encoder) {
        x264_picture_clean(&pic_in);
        memset((char *) &pic_in, 0, sizeof(pic_in));
        memset((char *) &pic_out, 0, sizeof(pic_out));

        x264_encoder_close(encoder);
        encoder = NULL;
    }

#ifdef ENCODE_OUT_FILE_1
    if (out1) {
        fclose(out1);
    }
#endif
#ifdef ENCODE_OUT_FILE_2
    if (out2) {
        fclose(out2);
    }
#endif

    return true;
}

bool VideoEncoder::validateSettings() {
    if (!x264Param->getWidthIN()) {
        LOGI("No in_width set");
        return false;
    }
    if (!x264Param->getHeightIN()) {
        LOGI("No in_height set");
        return false;
    }
    if (!x264Param->getWidthOUT()) {
        LOGI("No out_width set");
        return false;
    }
    if (!x264Param->getHeightOUT()) {
        LOGI("No out_height set");
        return false;
    }

    return true;
}

void VideoEncoder::setParams(X264Param *x264Param) {
    LOGE("*******************x264Param*******************");
    this->x264Param = x264Param;
    x264Param->printSelf();
//preset
    //默认：medium
    //一些在压缩效率和运算时间中平衡的预设值。如果指定了一个预设值，它会在其它选项生效前生效。
    //可选：ultrafast, superfast, veryfast, faster, fast, medium, slow, slower, veryslow and placebo.
    //建议：可接受的最慢的值

    //tune
    //默认：无
    //说明：在上一个选项基础上进一步优化输入。如果定义了一个tune值，它将在preset之后，其它选项之前生效。
    //可选：film, animation, grain, stillimage, psnr, ssim, fastdecode, zerolatency and touhou.
    //建议：根据输入选择。如果没有合适的就不要指定。
    //后来发现设置x264_param_default_preset(&param, "fast" , "zerolatency" );后就能即时编码了
    x264_param_default_preset(&params, x264Param->getPreset().c_str(),
                              x264Param->getTune().c_str());
//    x264_param_default_preset(&params, "ultrafast", "zerolatency");
    //色彩空间设置
    params.i_csp = X264_CSP_I420;
    params.i_width = x264Param->getWidthOUT();
    params.i_height = x264Param->getHeightOUT();

    //并行编码多帧
//    params.i_threads = X264_SYNC_LOOKAHEAD_AUTO;
    params.i_threads = X264_THREADS_AUTO;




    // B frames 两个相关图像间B帧的数目 */
    params.i_bframe = x264Param->getBFrameCount();//getBFrameFrq();
//    params.i_bframe = 0;//尝试降低cpu计算
//    params.b_sliced_threads = true;//多slice
    params.b_sliced_threads = x264Param->isUseSlice();//多slice

    params.b_vfr_input = 0;

    params.i_fps_num = x264Param->getFps();//getFps();
    params.i_fps_den = 1;
    params.i_timebase_num = params.i_fps_den;
    params.i_timebase_den = params.i_fps_num;
//    params.i_timebase_num = 1;
//    params.i_timebase_den = 1000;

//    params.rc.i_lookahead = 0;
//    params.i_sync_lookahead = 0;
//    params.i_bframe = 0;
//    params.b_sliced_threads = 1;
//    params.b_vfr_input = 0;
//    params.rc.b_mb_tree = 0;

    // Intra refres:I帧间隔,单位,帧数
    params.i_keyint_max = x264Param->getGop() * params.i_fps_num;
//    params.i_keyint_max = 3;
    params.i_keyint_min = 1;
    params.b_intra_refresh = 1;
    params.rc.i_bitrate = x264Param->getBitrate();

    //参数i_rc_method表示码率控制，CQP(恒定质量)，CRF(恒定码率)，ABR(平均码率)
    if (strcmp(x264Param->getBitrateCtrl().c_str(), "CRF") == 0) {
        LOGD("------->CRF");
        //恒定码率-------------------------------
        //恒定码率，会尽量控制在固定码率
        params.rc.i_rc_method = X264_RC_CRF;
        //图像质量控制,rc.f_rf_constant是实际质量，越大图像越花，越小越清晰,param.rc.f_rf_constant_max ，图像质量的最大值
        params.rc.f_rf_constant = 25;
        params.rc.f_rf_constant_max = 45;
    } else if(strcmp(x264Param->getBitrateCtrl().c_str(), "ABR") == 0) {
        LOGD("------->ABR");
        //平均码率--------------------------
        params.rc.i_rc_method = X264_RC_ABR;
        params.rc.i_vbv_buffer_size = static_cast<int>(params.rc.i_bitrate * 1.5);
        //瞬时最大码率,平均码率模式下，最大瞬时码率，默认0(与-B设置相同)
        params.rc.i_vbv_max_bitrate = params.rc.i_bitrate * 1.2;
    }


    // For streaming:
    //* 码率(比特率,单位Kbps)x264使用的bitrate需要/1000
    LOGD("params.rc.i_bitrate:%d", params.rc.i_bitrate);

    params.b_annexb = true;
    //是否把SPS和PPS放入每一个关键帧
    //SPS Sequence Parameter Set 序列参数集，PPS Picture Parameter Set 图像参数集
    //为了提高图像的纠错能力,该参数设置是让每个I帧都附带sps/pps。
    params.b_repeat_headers = true;
    //设置Level级别,编码复杂度
    params.i_level_idc = 30;

    //profile
    //默认：无
    //说明：限制输出文件的profile。这个参数将覆盖其它所有值，此选项能保证输出profile兼容的视频流。如果使用了这个选项，将不能进行无损压缩（qp 0 or crf 0）。
    //可选：baseline，main，high
    //建议：不设置。除非解码环境只支持main或者baseline profile的解码。
    x264_param_apply_profile(&params, x264Param->getProfile().c_str());
//    x264_param_apply_profile(&params, "baseline");

}

X264Param *VideoEncoder::getParams() {
    return this->x264Param;
}




