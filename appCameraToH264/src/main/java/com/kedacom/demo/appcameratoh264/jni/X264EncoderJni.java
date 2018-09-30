package com.kedacom.demo.appcameratoh264.jni;

import com.kedacom.demo.appcameratoh264.media.encoder.video.x264.X264Param;

/**
 * Created by yuhanxun
 * 2018/8/1
 * description:
 */
public class X264EncoderJni {
    static {
        System.loadLibrary("x264encoderjni");
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




}
