package com.example.apph264render.jni;

/**
 * Created by yuhanxun
 * 2018/8/10
 * description:
 */
public class VideoPlayerJni {
    static {
        System.loadLibrary("ffmpegjni");
    }
    public native void init();
    public native void release();
    public native void setRender(Object surface);
    public native void start();
    public native void stop();
    public native void putFrame(byte[] data, int size);
}
