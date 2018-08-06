package com.example.apph264tortp.jni;

public class FFmpegJni {

    static {
        System.load("ffmpegjni");
    }

    public native int startRTP(String h264Path, String aacPath, String ip, int port);


    public void setOnRTPSendListener(OnRTPSendListener onRTPSendListener) {
        this.onRTPSendListener = onRTPSendListener;
    }

    private OnRTPSendListener onRTPSendListener;

    public interface OnRTPSendListener {
        void onProgress(int percent);
    }

    private void callbackOnNative(int percent) {
        if (onRTPSendListener != null)
            onRTPSendListener.onProgress(percent);
    }

}
