package com.example.apph264render.ffmpegcodec;

import android.view.Surface;

import com.example.apph264render.api.IMediaCodec;

/**
 * Created by yuhanxun
 * 2018/8/10
 * description:
 */
public class FFmpegCodec implements IMediaCodec {
    static {
        System.loadLibrary("ffmpegjni");
    }
    public native void init();

    @Override
    public void setRenderView(Object obj) {
        setRender(obj);
    }

    public native void release();

    @Override
    public boolean isStop() {
        return false;
    }

    @Override
    public void putEncodeData(byte[] frames, int size) {
        putFrame(frames,size);
    }


    public native void setRender(Object surface);
    public native void start();
    public native void stop();
    public native void putFrame(byte[] data, int size);
}
