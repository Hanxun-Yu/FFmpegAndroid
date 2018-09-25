//
// Created by yuhanxun on 2018/8/4.
//

#include "Mp4Muxer.h"



/*
 FIX: H.264 in some container format (FLV, MP4, MKV etc.) need
 "h264_mp4toannexb" bitstream filter (BSF)
 *Add SPS,PPS in front of IDR frame
 *Add start code ("0,0,0,1") in front of NALU
 H.264 in some container (MPEG2TS) don't need this BSF.
 */
//'1': Use H.264 Bitstream Filter
#define USE_H264BSF 0 //只有当输入源不是裸流时(在某封装格式内)才需要打开此项,至1

/*
 FIX:AAC in some container format (FLV, MP4, MKV etc.) need
 "aac_adtstoasc" bitstream filter (BSF)
 */
//'1': Use AAC Bitstream Filter
#define USE_AACBSF 0 //只有当输入源不是裸流时(在某封装格式内)才需要打开此项,至1


static void log_callback_null(void *ptr, int level, const char *fmt, va_list vl);

static int open_input_file(const char *filename) {
    FILE *fp;
    fp = fopen(filename, "rb");// localfile文件名
    fseek(fp, 0L, SEEK_END); /* 定位到文件末尾 */
    int flen = ftell(fp); /* 得到文件大小 */
    //LOGD("=====22222======filename %s,length:%d",filename,flen);

    return flen;
}

int muxer_main_c(char *inputH264FileName, char *inputAacFileName, char *outMP4FileName,
                 char *angle) {
    av_log_set_callback(log_callback_null);

    AVOutputFormat *ofmt = NULL;
    //Input AVFormatContext and Output AVFormatContext
    AVFormatContext *ifmt_ctx_v = NULL, *ifmt_ctx_a = NULL, *ofmt_ctx = NULL;
    AVPacket pkt;
    AVCodec *dec;
    int ret, i;
    int videoindex_v = -1, videoindex_out = -1;
    int audioindex_a = -1, audioindex_out = -1;
    int frame_index = 0;
    int64_t cur_pts_v = 0, cur_pts_a = 0;

    //const char *in_filename_v = "cuc_ieschool.ts";//Input file URL
    const char *in_filename_v = inputH264FileName;
    //const char *in_filename_a = "cuc_ieschool.mp3";
    //const char *in_filename_a = "gowest.m4a";
    //const char *in_filename_a = "gowest.aac";
    const char *in_filename_a = inputAacFileName;

    const char *out_filename = outMP4FileName;//Output file URL

    LOGD("==========in h264==filename:%s\n", in_filename_v);
    LOGD("==========in aac ===filename:%s\n", in_filename_a);


    //int video_length=open_input_file(in_filename_v);
    //=========判断如果视频中没有音频，会导致avformat_find_stream_info退出程序======
    int acc_length = open_input_file(in_filename_a); //如果音频是空，则不需要放入视频

    avcodec_register_all();
    av_register_all();

    try {
        //Input
        if ((ret = avformat_open_input(&ifmt_ctx_a, in_filename_a, NULL, NULL)) < 0) {
            //        LOGD("=====11========RET:%d\n",ret);
            throw ("Could not open input file.");
        }
        //    LOGD("=====2========RET:%d\n",ret);
        if ((ret = avformat_find_stream_info(ifmt_ctx_a, 0)) < 0) {
            LOGD("Failed to retrieve input stream information");
            if (acc_length > 0) //如果音频是空，则直接到结尾，否则继续运行
                throw "audio empty";
        }

        if ((ret = avformat_open_input(&ifmt_ctx_v, in_filename_v, NULL, NULL)) < 0) {
            throw ("Could not open input file:%d\n", ret);
        }
        //    LOGD("=====0========RET:%d\n",ret);
        if ((ret = avformat_find_stream_info(ifmt_ctx_v, 0)) < 0) {
            throw ("Failed to retrieve input stream information");
        }

        //    /* init the video decoder */
        //    if ((ret = avcodec_open2(ifmt_ctx_a->, dec, NULL)) < 0) {
        //        LOGD( "Cannot open video decoder\n");
        //        return ret;
        //    }
        //

        LOGD("===========CheckOutPutMuxer==========\n");
        AVOutputFormat *outfmt = av_guess_format(NULL, out_filename, NULL);

        if (!outfmt) {
            LOGD("Could not deduce output format from file extension: using %s\n", out_filename);
            outfmt = av_guess_format("mkv", NULL, NULL);
        }

        if (!outfmt) {
            LOGD("Could not find suitable output format %s\n", out_filename);
            throw ("Could not find suitable output format");

        }
        LOGD("======================================\n");

        LOGD("===========Input Information==========\n");
        av_dump_format(ifmt_ctx_v, 0, in_filename_v, 0);
        av_dump_format(ifmt_ctx_a, 0, in_filename_a, 0);
        LOGD("======================================\n");
        //Output
        avformat_alloc_output_context2(&ofmt_ctx, NULL, NULL, out_filename);
        if (!ofmt_ctx) {
            ret = AVERROR_UNKNOWN;
            throw ("Could not create output context\n");

        }
        ofmt = ofmt_ctx->oformat;

        for (i = 0; i < ifmt_ctx_v->nb_streams; i++) {
            //Create output AVStream according to input AVStream
            if (ifmt_ctx_v->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
                AVStream *in_stream = ifmt_ctx_v->streams[i];
                AVStream *out_stream = avformat_new_stream(ofmt_ctx, in_stream->codec->codec);
                videoindex_v = i;
                if (!out_stream) {
                    ret = AVERROR_UNKNOWN;
                    throw ("Failed allocating output stream\n");
                }
                videoindex_out = out_stream->index;
                //Copy the settings of AVCodecContext
                ret = av_dict_set(&out_stream->metadata, "rotate", angle, 0); //设置旋转角度
                if (ret >= 0) {
                    LOGD("=========yes=====set rotate success!===:%s\n", angle);
                }

                if (avcodec_copy_context(out_stream->codec, in_stream->codec) < 0) {
                    throw ("Failed to copy context from input to output stream codec context\n");
                }
                out_stream->codec->codec_tag = 0;
                if (ofmt_ctx->oformat->flags & AVFMT_GLOBALHEADER)
                    out_stream->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;
                break;
            }
        }

        if (acc_length > 0) //如果音频文件有内容，则读取，否则为空读取会失败
        {
            for (i = 0; i < ifmt_ctx_a->nb_streams; i++) {

                LOGD("===========acc=====from======:%d\n", ifmt_ctx_a->nb_streams);
                //Create output AVStream according to input AVStream
                if (ifmt_ctx_a->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
                    AVStream *in_stream = ifmt_ctx_a->streams[i];
                    AVStream *out_stream = avformat_new_stream(ofmt_ctx, in_stream->codec->codec);
                    audioindex_a = i;
                    if (!out_stream) {
                        ret = AVERROR_UNKNOWN;
                        throw ("Failed allocating output stream\n");
                    }
                    audioindex_out = out_stream->index;
                    //Copy the settings of AVCodecContext
                    if (avcodec_copy_context(out_stream->codec, in_stream->codec) < 0) {
                        throw ("Failed to copy context from input to output stream codec context\n");
                    }
                    out_stream->codec->codec_tag = 0;
                    if (ofmt_ctx->oformat->flags & AVFMT_GLOBALHEADER)
                        out_stream->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;

                    break;
                }
            }
        }

        LOGD("==========Output Information==========\n");
        av_dump_format(ofmt_ctx, 0, out_filename, 1);
        LOGD("======================================\n");
        //Open output file
        if (!(ofmt->flags & AVFMT_NOFILE)) {
            if (avio_open(&ofmt_ctx->pb, out_filename, AVIO_FLAG_WRITE) < 0) {
                throw ("Could not open output file '%s'", out_filename);
            }
        }



        //Write file header
        int header_ret = avformat_write_header(ofmt_ctx, NULL);
        if (header_ret < 0) {
            throw ("Error occurred when opening output file:%d\n", header_ret);
        }


        //FIX
#if USE_H264BSF
        AVBitStreamFilterContext *h264bsfc = av_bitstream_filter_init("h264_mp4toannexb");
#endif
#if USE_AACBSF
        AVBitStreamFilterContext *aacbsfc = av_bitstream_filter_init("aac_adtstoasc");
//        AVBitStreamFilterContext *aacbsfc = av_bitstream_filter_init("aac");
#endif

        while (1) {
            AVFormatContext *ifmt_ctx;
            int stream_index = 0;
            AVStream *in_stream, *out_stream;


            //Get an AVPacket

            int compare_tag = -1;
            if (acc_length > 0) //既然没有音频，则直接判断写入视频
            {
                compare_tag = av_compare_ts(cur_pts_v, ifmt_ctx_v->streams[videoindex_v]->time_base,
                                            cur_pts_a,
                                            ifmt_ctx_a->streams[audioindex_a]->time_base);
            }

            if (compare_tag <= 0) {
                ifmt_ctx = ifmt_ctx_v;
                stream_index = videoindex_out;

                if (av_read_frame(ifmt_ctx, &pkt) >= 0) {
                    do {
                        in_stream = ifmt_ctx->streams[pkt.stream_index];
                        out_stream = ofmt_ctx->streams[stream_index];

                        if (pkt.stream_index == videoindex_v) {
                            //FIX£∫No PTS (Example: Raw H.264)
                            //Simple Write PTS
                            if (pkt.pts == AV_NOPTS_VALUE) {
                                //Write PTS
                                AVRational time_base1 = in_stream->time_base;
                                //Duration between 2 frames (us)
                                int64_t calc_duration =
                                        (double) AV_TIME_BASE / av_q2d(in_stream->r_frame_rate);
                                //Parameters
                                pkt.pts = (double) (frame_index * calc_duration) /
                                          (double) (av_q2d(time_base1) * AV_TIME_BASE);
                                pkt.dts = pkt.pts;
                                pkt.duration = (double) calc_duration /
                                               (double) (av_q2d(time_base1) * AV_TIME_BASE);
                                frame_index++;
                            }

                            cur_pts_v = pkt.pts;
                            break;
                        }
                    } while (av_read_frame(ifmt_ctx, &pkt) >= 0);
                } else {
                    break;
                }
            } else {
                ifmt_ctx = ifmt_ctx_a;
                stream_index = audioindex_out;
                if (av_read_frame(ifmt_ctx, &pkt) >= 0) {
                    do {
                        in_stream = ifmt_ctx->streams[pkt.stream_index];
                        out_stream = ofmt_ctx->streams[stream_index];

                        if (pkt.stream_index == audioindex_a) {

                            //FIX£∫No PTS
                            //Simple Write PTS
                            if (pkt.pts == AV_NOPTS_VALUE) {
                                //Write PTS
                                AVRational time_base1 = in_stream->time_base;
                                //Duration between 2 frames (us)
                                int64_t calc_duration =
                                        (double) AV_TIME_BASE / av_q2d(in_stream->r_frame_rate);
                                //Parameters
                                pkt.pts = (double) (frame_index * calc_duration) /
                                          (double) (av_q2d(time_base1) * AV_TIME_BASE);
                                pkt.dts = pkt.pts;
                                pkt.duration = (double) calc_duration /
                                               (double) (av_q2d(time_base1) * AV_TIME_BASE);
                                frame_index++;
                            }
                            cur_pts_a = pkt.pts;

                            break;
                        }
                    } while (av_read_frame(ifmt_ctx, &pkt) >= 0);
                } else {
                    break;
                }

            }

            //FIX:Bitstream Filter
#if USE_H264BSF
            av_bitstream_filter_filter(h264bsfc, in_stream->codec, NULL, &pkt.data, &pkt.size,
                                       pkt.data,
                                       pkt.size, 0);
#endif
#if USE_AACBSF
            av_bitstream_filter_filter(aacbsfc, out_stream->codec, NULL, &pkt.data, &pkt.size,
                                       pkt.data,
                                       pkt.size, 0);
#endif


            //Convert PTS/DTS
            pkt.pts = av_rescale_q_rnd(pkt.pts, in_stream->time_base, out_stream->time_base,
//                                   (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
                                       AV_ROUND_NEAR_INF);

            pkt.dts = av_rescale_q_rnd(pkt.dts, in_stream->time_base, out_stream->time_base,
//                                   (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
                                       AV_ROUND_NEAR_INF);

            pkt.duration = av_rescale_q(pkt.duration, in_stream->time_base, out_stream->time_base);
            pkt.pos = -1;
            pkt.stream_index = stream_index;

            LOGD("Write 1 Packet. size:%5d\tdts:%lld\tpts:%lld\tduring:%lld\n", pkt.size, pkt.dts,
                 pkt.pts, pkt.duration);
            //Write


            if ((ret = av_interleaved_write_frame(ofmt_ctx, &pkt)) < 0) {
                LOGD("AVPacket stream_index %d", pkt.stream_index);
                LOGD("AVFormatContext nb_streams  %d", ofmt_ctx->nb_streams);

                LOGD("Error muxing packet error:%d\n", ret);
                break;
            }


            av_free_packet(&pkt);

        }
        //Write file trailer
        av_write_trailer(ofmt_ctx);

#if USE_H264BSF
        av_bitstream_filter_close(h264bsfc);
#endif
#if USE_AACBSF
        av_bitstream_filter_close(aacbsfc);
#endif
        LOGD("======muxer mp4 success =====!\n");
    } catch (...) {

    }
//    } catch (const std::exception& e) {
//        LOGE("Exception:%s",e.what());
//    }
    avformat_close_input(&ifmt_ctx_v);
    avformat_close_input(&ifmt_ctx_a);
    /* close output */
    if (ofmt_ctx && !(ofmt->flags & AVFMT_NOFILE))
        avio_close(ofmt_ctx->pb);
    avformat_free_context(ofmt_ctx);
    if (ret < 0 && ret != AVERROR_EOF) {
        LOGD("Error occurred.\n");
        return -1;
    }
    return 0;
}

static void log_callback_null(void *ptr, int level, const char *fmt, va_list vl)
{
    static int print_prefix = 1;
    static int count;
    static char prev[1024];
    char line[1024];
    static int is_atty;

    av_log_format_line(ptr, level, fmt, vl, line, sizeof(line), &print_prefix);

    strcpy(prev, line);
    //sanitize((uint8_t *)line);

    if (level <= AV_LOG_WARNING)
    {
        LOGE("%s", line);
    }
    else
    {
        LOGD("%s", line);
    }
}

int Mp4Muxer::muxer_main(char *inputH264FileName, char *inputAacFileName, char *outMP4FileName,
                         char *angle) {
    return muxer_main_c(inputH264FileName, inputAacFileName, outMP4FileName, angle);
}




