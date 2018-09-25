package com.kedacom.demo.appcameratoh264.jni;

/**
 * Created by yuhanxun
 * 2018/9/25
 * description:
 */
public class AudioEncoderJni {
    static {
        System.loadLibrary("audioencoderjni");
    }

    public native int init();

    public native int release();
    /**
     * @param sampleRate 音频采样频率
     * @param channels   音频通道
     * @param bitRate    音频bitRate
     * @return
     */
    public native int encoderAudioInit(int sampleRate, int channels, int bitRate);

    public native int encoderAudioEncode(byte[] srcFrame, int frameSize, byte[] dstFrame, int dstSize);
}
