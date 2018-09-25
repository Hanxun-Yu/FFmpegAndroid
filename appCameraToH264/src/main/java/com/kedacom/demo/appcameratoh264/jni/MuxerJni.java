package com.kedacom.demo.appcameratoh264.jni;

import com.kedacom.demo.appcameratoh264.media.encoder.video.X264Param;

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
