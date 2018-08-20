//
// Created by yuhanxun on 2018/8/1.
//

#ifndef FFMPEGPLAYER_FRAMEENCODER_H
#define FFMPEGPLAYER_FRAMEENCODER_H
#include <string>
#include "X264Param.hpp"

extern "C" {
#include <stdio.h>
#include <string.h>
#include "libx264/x264.h"
#include "logcat.h"
#include <jni.h>
#include <malloc.h>
}
using namespace std;

class VideoEncoder {
private:
    X264Param* x264Param;
    /* x264 struct*/
    x264_picture_t pic_in;
    x264_picture_t pic_out;
    x264_param_t params;
    x264_nal_t *nals;
    x264_t *encoder;
    int num_nals;

    FILE *out1;
    FILE *out2;

public:
    VideoEncoder();

    ~VideoEncoder();

    /* open for encoding */
    bool open();

    /* encode the given data */
    int encodeFrame(char *inBytes, int frameSize, int pts, char *outBytes, int *outFrameSize);

    /* close the encoder and file, frees all memory */
    bool close();

    /* validates if all params are set correctly, like width,height, etc.. */
    bool validateSettings();

    /* sets the x264 params */
    void setParams(X264Param* x264Param);

    X264Param* getParams();



};


#endif //FFMPEGPLAYER_FRAMEENCODER_H
