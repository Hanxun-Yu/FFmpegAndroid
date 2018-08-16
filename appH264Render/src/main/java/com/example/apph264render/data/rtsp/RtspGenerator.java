package com.example.apph264render.data.rtsp;

import android.util.Log;

import com.example.apph264render.api.IDataGenerator;
import com.example.apph264render.jni.RtspHelperJni;

/**
 * Created by yuhanxun
 * 2018/8/16
 * description:
 */
public class RtspGenerator implements IDataGenerator {
    String TAG = getClass().getSimpleName()+"_xunxun";
    RtspHelperJni jni = new RtspHelperJni();
    String url;

    public RtspGenerator(String url) {
        this.url = url;
    }

    @Override
    public void init() {
        Log.d(TAG,"init");
        jni.init();
        jni.setOnDataCallbackListener(new RtspHelperJni.OnDataCallbackListener() {
            @Override
            public void onDataCallback(byte[] data, int size) {
                if(listener != null) {
                    listener.onReceiveData(data,size);
                }
            }
        });

    }

    @Override
    public void start() {
        jni.play(url);
    }

    @Override
    public void stop() {
        jni.release();
    }
    OnDataReceiverListener listener;
    @Override
    public void setOnDataListener(OnDataReceiverListener listener) {
        this.listener = listener;
    }
}
