//
// Created by yuhanxun on 2018/8/4.
//

#ifndef FFMPEGPLAYER_MP4MUXER_H
#define FFMPEGPLAYER_MP4MUXER_H

#include "Jnicom.h"

extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"

}


class Mp4Muxer {
public:
    int
    muxer_main(char *inputH264FileName, char *inputAacFileName, char *outMP4FileName, char *angle);
};


#endif //FFMPEGPLAYER_MP4MUXER_H
