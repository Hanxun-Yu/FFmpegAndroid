//
// Created by yuhanxun on 2018/8/3.
//

#include "AudioEncoder.h"

AudioEncoder::AudioEncoder(int channels, int sampleRate, int bitRate)
        : channels(channels), sampleRate(sampleRate), bitRate(bitRate) {

}

AudioEncoder::~AudioEncoder() {

}

int AudioEncoder::init() {
    //打开AAC音频编码引擎，创建AAC编码句柄
    if (aacEncOpen(&handle, 0, channels) != AACENC_OK) {
        LOGI("Unable to open fdkaac encoder\n");
        return -1;
    }

    // AACENC_AOT设置为aac lc
    if (aacEncoder_SetParam(handle, AACENC_AOT, 2) != AACENC_OK) {
        LOGI("Unable to set the AOT\n");
        return -1;
    }

    if (aacEncoder_SetParam(handle, AACENC_SAMPLERATE, sampleRate) != AACENC_OK) {
        LOGI("Unable to set the sampleRate\n");
        return -1;
    }

    // AACENC_CHANNELMODE设置为双通道
    if (aacEncoder_SetParam(handle, AACENC_CHANNELMODE, MODE_2) != AACENC_OK) {
        LOGI("Unable to set the channel mode\n");
        return -1;
    }

    if (aacEncoder_SetParam(handle, AACENC_CHANNELORDER, 1) != AACENC_OK) {
        LOGI("Unable to set the wav channel order\n");
        return 1;
    }
    if (aacEncoder_SetParam(handle, AACENC_BITRATE, bitRate) != AACENC_OK) {
        LOGI("Unable to set the bitrate\n");
        return -1;
    }
    if (aacEncoder_SetParam(handle, AACENC_TRANSMUX, 2) != AACENC_OK) { //0-raw 2-adts
        LOGI("Unable to set the ADTS transmux\n");
        return -1;
    }

    if (aacEncoder_SetParam(handle, AACENC_AFTERBURNER, 1) != AACENC_OK) {
        LOGI("Unable to set the ADTS AFTERBURNER\n");
        return -1;
    }

    if (aacEncEncode(handle, NULL, NULL, NULL, NULL) != AACENC_OK) {
        LOGI("Unable to initialize the encoder\n");
        return -1;
    }

    AACENC_InfoStruct info = {0};
    if (aacEncInfo(handle, &info) != AACENC_OK) {
        LOGI("Unable to get the encoder info\n");
        return -1;
    }

    //返回数据给上层，表示每次传递多少个数据最佳，这样encode效率最高
    int inputSize = channels * 2 * info.frameLength;
    LOGI("inputSize = %d", inputSize);

    return inputSize;
}

/**
 * Fdk-AAC库压缩裸音频PCM数据，转化为AAC，这里为什么用fdk-aac，这个库相比普通的aac库，压缩效率更高
 * @param inBytes
 * @param length
 * @param outBytes
 * @param outLength
 * @return
 */
int AudioEncoder::encodeAudio(unsigned char *inBytes, int length, unsigned char *outBytes,
                              int outlength) {
    void *in_ptr, *out_ptr;
    AACENC_BufDesc in_buf = {0};
    int in_identifier = IN_AUDIO_DATA;
    int in_elem_size = 2;
    //传递input数据给in_buf
    in_ptr = inBytes;
    in_buf.bufs = &in_ptr;
    in_buf.numBufs = 1;
    in_buf.bufferIdentifiers = &in_identifier;
    in_buf.bufSizes = &length;
    in_buf.bufElSizes = &in_elem_size;

    AACENC_BufDesc out_buf = {0};
    int out_identifier = OUT_BITSTREAM_DATA;
    int elSize = 1;
    //out数据放到out_buf中
    out_ptr = outBytes;
    out_buf.bufs = &out_ptr;
    out_buf.numBufs = 1;
    out_buf.bufferIdentifiers = &out_identifier;
    out_buf.bufSizes = &outlength;
    out_buf.bufElSizes = &elSize;

    AACENC_InArgs in_args = {0};
    in_args.numInSamples = length / 2;  //size为pcm字节数

    AACENC_OutArgs out_args = {0};
    AACENC_ERROR err;

    //利用aacEncEncode来编码PCM裸音频数据，上面的代码都是fdk-aac的流程步骤
    if ((err = aacEncEncode(handle, &in_buf, &out_buf, &in_args, &out_args)) != AACENC_OK) {
        LOGI("Encoding aac failed\n");
        return err;
    }
    //返回编码后的有效字段长度
    return out_args.numOutBytes;
}

bool AudioEncoder::close() {
    if (handle) {
        aacEncClose(&handle);
        handle = NULL;
    }
    return true;
}
