package com.example.apph264render.jni;

/**
 * Created by yuhanxun
 * 2018/8/10
 * description:
 */
public class YuvPlayerJni {
    static {
        System.loadLibrary("yuvplayerjni");
    }

    public native void init();

    public native void release();

    public native void setRender(Object surface);

    public native void start();

    public native void stop();

    public native void putYuv(byte[] data, int width, int height, int size);

    public native void getSPSWH(byte[] data, int[] wh);
}
