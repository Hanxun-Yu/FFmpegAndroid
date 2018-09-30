package com.kedacom.demo.appcameratoh264.media.collecter.api;

/**
 * Created by yuhanxun
 * 2018/9/29
 * description:
 */
public interface IMediaCollecter {
    void init();
    void config(ICollecterParam param);
    void start();
    void stop();
    void release();

    void setCallback(Callback callback);

    interface Callback {
        void onCollectData(ICollectData data);
    }
}
