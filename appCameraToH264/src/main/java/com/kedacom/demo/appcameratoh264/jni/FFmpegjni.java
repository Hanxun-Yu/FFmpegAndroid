package com.kedacom.demo.appcameratoh264.jni;

/**
 * Created by yuhanxun
 * 2018/8/1
 * description:
 */
public class FFmpegjni {
    static{
        System.loadLibrary("ffmpegjni");
    }

    public native int init(int width, int height, int outWidth, int outHeight);

    public native int release();

    /**
     * 编码视频数据准备工作
     * @param in_width
     * @param in_height
     * @param out_width
     * @param out_height
     * @param bitrate kbps
     * @return
     */
    public native int encoderVideoinit(int in_width, int in_height, int out_width, int out_height,int bitrate);

    /**
     * 编码视频数据接口
     * @param srcFrame      原始数据(YUV420P数据)
     * @param frameSize     帧大小
     * @param fps           fps
     * @param dstFrame      编码后的数据存储
     * @param outFramewSize 编码后的数据大小
     * @return
     */
    public native int encoderVideoEncode(byte[] srcFrame, int frameSize, int fps, byte[] dstFrame, int[] outFramewSize);


}
