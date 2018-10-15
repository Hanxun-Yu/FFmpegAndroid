package com.kedacom.demo.appcameratoh264;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.kedacom.demo.appcameratoh264.api.IWorker;
import com.kedacom.demo.appcameratoh264.media.collecter.CollecterConfig;
import com.kedacom.demo.appcameratoh264.media.collecter.CollecterManager;
import com.kedacom.demo.appcameratoh264.media.collecter.api.ICollectData;
import com.kedacom.demo.appcameratoh264.media.collecter.api.IMediaCollecter;
import com.kedacom.demo.appcameratoh264.media.collecter.audio.AudioCollectData;
import com.kedacom.demo.appcameratoh264.media.collecter.video.VideoCollectData;
import com.kedacom.demo.appcameratoh264.media.encoder.EncoderConfig;
import com.kedacom.demo.appcameratoh264.media.encoder.EncoderManager;
import com.kedacom.demo.appcameratoh264.media.encoder.api.IFrameData;
import com.kedacom.demo.appcameratoh264.media.encoder.api.IMediaEncoder;
import com.kedacom.demo.appcameratoh264.media.encoder.audio.AudioPacketData;
import com.kedacom.demo.appcameratoh264.media.encoder.video.VideoPacketData;
import com.kedacom.demo.common.android.base.BaseService;


/**
 * Created by yuhanxun
 * 2018/10/11
 * description:
 */
public class WorkService extends BaseService implements IWorker {

    private EncoderManager encoderManager;
    private CollecterManager collecterManager;

    @Override
    public void onCreate() {
        super.onCreate();
        encoderManager = new EncoderManager();
        collecterManager = new CollecterManager(this);
        initEncoderPutThread();
    }

    HandlerThread handlerThread;
    Handler putEncoderHandler;


    final int HANDLE_VIDEO_MSG = 0;
    final int HANDLE_AUDIO_MSG = 1;

    private void initEncoderPutThread() {
        handlerThread = new HandlerThread("putEncoderThread");
        handlerThread.start();
        putEncoderHandler = new Handler(handlerThread.getLooper()) {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HANDLE_VIDEO_MSG:
                        VideoCollectData vdata = (VideoCollectData) msg.obj;
                        VideoPacketData vtemp = new VideoPacketData(vdata.getYuvData(),vdata.getTimestamp());
                        encoderManager.encodeVideo(vtemp);
                        break;

                    case HANDLE_AUDIO_MSG:
                        AudioCollectData adata = (AudioCollectData) msg.obj;
                        AudioPacketData atemp = new AudioPacketData(adata.getPCMData(), adata.getTimestamp());
                        encoderManager.encodeAudio(atemp);
                        break;
                }
            }
        };
    }

    private void stopEncoderPutThread() {
        putEncoderHandler.getLooper().quit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopEncoderPutThread();
        stopWork();
        releaseManager();
    }


    private void notifyEncoderVideo(ICollectData gatherData) {
        Message msg = putEncoderHandler.obtainMessage(HANDLE_VIDEO_MSG);
        msg.obj = gatherData;
        msg.sendToTarget();
    }



    private void notifyEncoderAudio(ICollectData data) {
        Message msg = putEncoderHandler.obtainMessage(HANDLE_AUDIO_MSG);
        msg.obj = data;
        msg.sendToTarget();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return binder;
    }

    @Override
    public void setCollecterParam(CollecterConfig config) {
        collecterManager.config(config);
        //采集输出
        collecterManager.setVideoCollecterCB(new IMediaCollecter.Callback() {
            @Override
            public void onCollectData(ICollectData data) {
                if(videoCollecterCB != null)
                    videoCollecterCB.onCollectData(data);
                notifyEncoderVideo(data);
            }
        });
        //采集输出
        collecterManager.setAudioCollecterCB(new IMediaCollecter.Callback() {
            @Override
            public void onCollectData(ICollectData data) {
                if(audioCollecterCB != null)
                    audioCollecterCB.onCollectData(data);
                notifyEncoderAudio(data);
            }
        });
    }

    @Override
    public void setEncoderParam(EncoderConfig config) {
        //判断方向
        encoderManager.config(config);
        encoderManager.setVideoEncoderCB(new IMediaEncoder.Callback() {
            int count = 0;
            int frequence = 25;

            @Override
            public void onDataEncoded(IFrameData encodedData) {
                if (count % frequence == 0) {
//                    refreshInfo();
                    count = 0;
                }
                count++;
                if(videoEncoderCB != null)
                    videoEncoderCB.onDataEncoded(encodedData);
            }
        });

        //编码输出
        encoderManager.setAudioEncoderCB(new IMediaEncoder.Callback() {
            @Override
            public void onDataEncoded(IFrameData encodedData) {
                if(audioEncoderCB != null)
                    audioEncoderCB.onDataEncoded(encodedData);
            }
        });
        //编码输出
        encoderManager.setOnStateChangedListener(new IMediaEncoder.OnStateChangedListener() {

            @Override
            public void onState(IMediaEncoder encoder, IMediaEncoder.State state) {
//                refreshInfo();

            }
        });
    }

    @Override
    public void startWork() {
        encoderManager.start();
        collecterManager.start();
    }

    @Override
    public void stopWork() {
        collecterManager.stop();
        encoderManager.stop();
    }


    IMediaCollecter.Callback videoCollecterCB;
    IMediaCollecter.Callback audioCollecterCB;
    IMediaEncoder.Callback videoEncoderCB;
    IMediaEncoder.Callback audioEncoderCB;

    @Override
    public void setVideoCollecterCB(IMediaCollecter.Callback callback) {
        this.videoCollecterCB = callback;
    }

    @Override
    public void setAudioCollecterCB(IMediaCollecter.Callback callback) {
        this.audioCollecterCB = callback;
    }

    @Override
    public void setViderEncoderCB(IMediaEncoder.Callback callback) {
        this.videoEncoderCB = callback;
    }

    @Override
    public void setAudioEncdoerCB(IMediaEncoder.Callback callback) {
        this.audioEncoderCB = callback;
    }

    @Override
    public String getCollecterInfo() {
        return collecterManager.getVideoCollecterInfo()+"\n"+collecterManager.getAudioCollecterInfo();
    }

    @Override
    public String getEncoderInfo() {
        return encoderManager.getVideoEncoderInfo()+"\n"+encoderManager.getAudioEncoderInfo();
    }

    private void releaseManager() {
        encoderManager.release();
        collecterManager.release();
    }

    public class MyBinder extends Binder {
        public WorkService getService() {
            return WorkService.this;
        }
    }


    private MyBinder binder = new MyBinder();
}
