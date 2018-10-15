package com.kedacom.demo.appcameratoh264.api;

import com.kedacom.demo.appcameratoh264.media.collecter.CollecterConfig;
import com.kedacom.demo.appcameratoh264.media.collecter.api.IMediaCollecter;
import com.kedacom.demo.appcameratoh264.media.collecter.api.IVideoCollecter;
import com.kedacom.demo.appcameratoh264.media.encoder.EncoderConfig;
import com.kedacom.demo.appcameratoh264.media.encoder.api.IMediaEncoder;

/**
 * Created by yuhanxun
 * 2018/10/12
 * description:
 */
public interface IWorker {
    void setCollecterParam(CollecterConfig config);

    void setEncoderParam(EncoderConfig config);

    void startWork();

    void stopWork();

    void setVideoCollecterCB(IMediaCollecter.Callback callback);
    void setAudioCollecterCB(IMediaCollecter.Callback callback);
    void setViderEncoderCB(IMediaEncoder.Callback callback);
    void setAudioEncdoerCB(IMediaEncoder.Callback callback);

    String getCollecterState();
    String getEncoderState();
    String getCollecterParam();
    String getEncoderParam();
}
