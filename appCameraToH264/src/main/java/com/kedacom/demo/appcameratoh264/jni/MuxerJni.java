package com.kedacom.demo.appcameratoh264.jni;

/**
 * Created by yuhanxun
 * 2018/9/25
 * description:
 */
public class MuxerJni {
    static {
        System.loadLibrary("muxerjni");
    }

    public native int init();

    public native int release();

    public native int muxMp4(String h264, String aac, String outMp4);
}
