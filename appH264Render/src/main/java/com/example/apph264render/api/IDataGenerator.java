package com.example.apph264render.api;

/**
 * Created by yuhanxun
 * 2018/8/14
 * description:
 */
public interface IDataGenerator {
    void init();

    void start();

    void stop();

    void setOnDataListener(OnDataReceiverListener listener);

    interface OnDataReceiverListener {
        void onReceiveData(byte[] data, int size);
    }
}
