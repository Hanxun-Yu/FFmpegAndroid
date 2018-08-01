package com.example.ffmpeg;

/**
 * Created by yuhanxun
 * 2018/6/21
 */
public class FFmpegNative {
    static {
        System.loadLibrary("ffmpeg");
//        System.loadLibrary("avutil-56");
//        System.loadLibrary("avcodec-58");
//        System.loadLibrary("swresample-3");
//        System.loadLibrary("avformat-58");
//        System.loadLibrary("swscale-5");
//        System.loadLibrary("avfilter-7");
//        System.loadLibrary("avdevice-58");
//        System.loadLibrary("jnicom");
    }

    public static native int play(String path, Object surface);
}
