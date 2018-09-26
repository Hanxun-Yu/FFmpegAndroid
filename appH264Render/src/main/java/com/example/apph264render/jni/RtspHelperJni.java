package com.example.apph264render.jni;

import android.util.Log;

/**
 * Created by yuhanxun
 * 2018/8/16
 * description:
 */
public class RtspHelperJni {
    final String TAG = this.getClass().getSimpleName()+"_xunxun";
    static {
        System.loadLibrary("rtspjni");
    }
    public native void init();
    public native void release();
    public native void play(String url);

    public void onDataCallback(byte[] data, int size) {
//        Log.e(TAG,"onDataCallback data len:"+data.length+" size:"+size);
        if(onDataCallbackListener != null)
            onDataCallbackListener.onDataCallback(data,size);
    }

    public void setOnDataCallbackListener(OnDataCallbackListener onDataCallbackListener) {
        this.onDataCallbackListener = onDataCallbackListener;
    }

    private OnDataCallbackListener onDataCallbackListener;
    public interface OnDataCallbackListener {
        void onDataCallback(byte[] data, int size);
    }
}
