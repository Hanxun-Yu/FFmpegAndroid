package com.example.apph264render.ffmpegcodec;

import com.example.apph264render.api.IMediaCodec;
import com.example.apph264render.jni.VideoPlayerJni;
import com.example.apph264render.mediacodec.OnDecodeListener;

/**
 * Created by yuhanxun
 * 2018/8/10
 * description:
 */
public class FFmpegCodec implements IMediaCodec {

    VideoPlayerJni videoPlayerJni = new VideoPlayerJni();

    @Override
    public void init() {
        videoPlayerJni.init();
    }

    @Override
    public void setRenderView(Object obj) {
        videoPlayerJni.setRender(obj);
    }

    @Override
    public void release() {
        videoPlayerJni.release();
    }

    @Override
    public boolean isStop() {
        return false;
    }

    @Override
    public void putEncodeData(byte[] frames, int size) {
        videoPlayerJni.putFrame(frames, size);
    }

    @Override
    public void setOnDecodeListener(OnDecodeListener listener) {

    }

}
