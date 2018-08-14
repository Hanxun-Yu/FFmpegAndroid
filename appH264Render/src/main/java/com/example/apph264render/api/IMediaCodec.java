package com.example.apph264render.api;

import android.view.Surface;

/**
 * Created by yuhanxun
 * 2018/8/14
 * description:
 */
public interface IMediaCodec {
    void init();
    void setRenderView(Object obj);
    void release();
    boolean isStop();
    void putEncodeData(byte[] frames,int size);

}
