package com.kedacom.demo.appcameratoh264.media.gather.api;

/**
 * Created by yuhanxun
 * 2018/9/29
 * description:
 */
public interface IMediaGather {
    void init();
    void config(IGatherParam param);
    void start();
    void stop();
    void release();

    void setCallback(Callback callback);

    interface Callback {
        void onGatherData(GatherData data);
    }
}
