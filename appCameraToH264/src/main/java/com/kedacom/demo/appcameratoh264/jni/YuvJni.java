package com.kedacom.demo.appcameratoh264.jni;

/**
 * Created by yuhanxun
 * 2018/9/30
 * description:
 */
public class YuvJni {
    static {
        System.loadLibrary("yuvjni");
    }

    public native void nv12ToI420(byte[] src, int srcW, int srcH, byte[] dst, int[] dstWH, int degree);

    public native void nv21ToI420(byte[] src, int srcW, int srcH, byte[] dst, int[] dstWH, int degree);

    public native void yv12ToI420(byte[] src, int srcW, int srcH, byte[] dst, int[] dstWH, int degree);

}
