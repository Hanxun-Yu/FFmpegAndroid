package com.kedacom.demo.appcameratoh264.jni;

/**
 * Created by yuhanxun
 * 2018/8/1
 * description:
 */
public class FFmpegjni {
    static {
        System.loadLibrary("ffmpegjni");
    }

    public native int init();

    public native int release();

    /**
     * 编码视频数据准备工作
     *
     * @param in_width
     * @param in_height
     * @param out_width
     * @param out_height
     * @param bitrate    kbps
     * @return
     */
    public native int encoderVideoinit(X264Param param);

    /**
     * 编码视频数据接口
     *
     * @param srcFrame      原始数据(YUV420P数据)
     * @param frameSize     帧大小
     * @param fps           fps 帧序 递增
     * @param dstFrame      编码后的数据存储
     * @param outFramewSize 编码后的数据大小
     * @return
     */
    public native int encoderVideoEncode(byte[] srcFrame, int frameSize, int fps, byte[] dstFrame, int[] outFramewSize);


    /**
     * @param sampleRate 音频采样频率
     * @param channels   音频通道
     * @param bitRate    音频bitRate
     * @return
     */
    public native int encoderAudioInit(int sampleRate, int channels, int bitRate);

    public native int encoderAudioEncode(byte[] srcFrame, int frameSize, byte[] dstFrame, int dstSize);

    public native int muxMp4(String h264, String aac, String outMp4);



}
