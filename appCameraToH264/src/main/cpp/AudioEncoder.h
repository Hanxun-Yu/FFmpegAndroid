//
// Created by yuhanxun on 2018/8/3.
//

#ifndef FFMPEGPLAYER_AUDIOENCODER_H
#define FFMPEGPLAYER_AUDIOENCODER_H

extern "C" {
#include "fdk-aac/aacenc_lib.h"
}

#include "JniHelper.h"
class AudioEncoder {
private:
    int sampleRate;
    int channels;
    int bitRate;
    HANDLE_AACENCODER handle;

public:
    AudioEncoder(int channels, int sampleRate, int bitRate);

    ~AudioEncoder();

    int init();

    int encodeAudio(unsigned char *inBytes, int length, unsigned char *outBytes, int outlength);

//    int encodeWAVAudioFile();
//    int encodePCMAudioFile();
    bool close();

};


#endif //FFMPEGPLAYER_AUDIOENCODER_H
