package com.example.apph264tortp.jni;

import android.util.Log;

public class FFmpegJni {
    final String TAG = getClass().getSimpleName()+"_xunxun";
    static {
        System.loadLibrary("ffmpegjni");
    }

    public native int startRTPFromFile(String h264Path, String aacPath, String ip, int port);

    public native int init(String ip, int port);
    public native void start();
    public native void putH264(byte[] data,int size);
    public native void stop();
    public native void quit();


    public void setOnRTPFileSendListener(OnRTPFileSendListener onRTPSendListener) {
        this.onRTPSendListener = onRTPSendListener;
    }

    private OnRTPFileSendListener onRTPSendListener;

    public interface OnRTPFileSendListener {
        void onProgress(int percent);
    }


    private void callbackOnNative(int percent) {
        Log.d(TAG,"callbackOnNative percent:"+percent);
        if (onRTPSendListener != null)
            onRTPSendListener.onProgress(percent);
    }

}
