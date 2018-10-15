package com.kedacom.demo.appcameratoh264.media.collecter;

import android.content.Context;
import android.util.Log;

import com.kedacom.demo.appcameratoh264.media.FileManager;
import com.kedacom.demo.appcameratoh264.media.collecter.api.ICollectData;
import com.kedacom.demo.appcameratoh264.media.collecter.api.IMediaCollecter;
import com.kedacom.demo.appcameratoh264.media.collecter.audio.AudioCollectData;
import com.kedacom.demo.appcameratoh264.media.collecter.audio.mic.MICCollecter;
import com.kedacom.demo.appcameratoh264.media.collecter.video.VideoCollectData;
import com.kedacom.demo.appcameratoh264.media.collecter.video.camera1.CameraCollecter;
import com.kedacom.demo.appcameratoh264.media.collecter.video.camera2.Camera2Collecter;
import com.kedacom.demo.appcameratoh264.media.encoder.audio.AudioFrameData;

/**
 * Created by yuhanxun
 * 2018/9/21
 * description:
 */
public class CollecterManager {
    private final String TAG = getClass().getSimpleName() + "_xunxun";

    private CollecterConfig config;
    private IMediaCollecter videoCollecter;
    private IMediaCollecter audioCollecter;

    private FileManager videoSaver;
    private FileManager audioSaver;


    private IMediaCollecter.Callback videoCollecterCB;
    private IMediaCollecter.Callback audioCollecterCB;

    private Context context;
    public CollecterManager(Context context) {
        this.context = context;
    }

    public void config(CollecterConfig config) {
        Log.e(TAG, "config");
        this.config = config;
        if (config.getVideo() != null) {
            switch (config.getVideo()) {
                case Camera:
                    videoCollecter = new CameraCollecter();
                    break;
                case Camera2:
                    videoCollecter = new Camera2Collecter(context);
                    break;
            }
            videoCollecter.init();
            videoCollecter.config(config.getVideoParam());
            videoCollecter.setCallback(new IMediaCollecter.Callback() {
                @Override
                public void onCollectData(ICollectData data) {
                    if (data instanceof VideoCollectData) {
                        if (videoSaver != null) {
                            videoSaver.writeFileData(((VideoCollectData) data).getYuvData().getData());
                        }
                    }

                    if (videoCollecterCB != null)
                        videoCollecterCB.onCollectData(data);
                }
            });


            if (config.getVideoSavePath() != null) {
                videoSaver = new FileManager(config.getVideoSavePath());
            }
        }


        if (config.getAudio() != null) {
            switch (config.getAudio()) {
                case Mic:
                    audioCollecter = new MICCollecter();
                    break;
            }
            audioCollecter.init();
            audioCollecter.config(config.getAudioParam());
            audioCollecter.setCallback(new IMediaCollecter.Callback() {
                @Override
                public void onCollectData(ICollectData data) {
                    if(data instanceof AudioCollectData) {
                        if (audioSaver != null)
                            audioSaver.writeFileData(((AudioFrameData) data).getData());
                        if (audioCollecterCB != null)
                            audioCollecterCB.onCollectData(data);
                    } else {
                        throw new IllegalArgumentException("stub");
                    }
                }

            });
            if (config.getAudioSavePath() != null) {
                audioSaver = new FileManager(config.getAudioSavePath());
            }
        }

    }


    public void start() {
        Log.e(TAG, "start");
        if (videoSaver != null)
            videoSaver.openFile();
        if (audioSaver != null)
            audioSaver.openFile();
        if (videoCollecter != null)
            videoCollecter.start();
        if (audioCollecter != null)
            audioCollecter.start();
    }

    public void stop() {
        Log.e(TAG, "stop");
        if (videoCollecter != null)
            videoCollecter.stop();
        if (audioCollecter != null)
            audioCollecter.stop();
        if (videoSaver != null) {
            videoSaver.closeFile();
        }
        if (audioSaver != null) {
            audioSaver.closeFile();
        }
    }

    public void release() {
        Log.e(TAG, "release");

        if (videoCollecter != null)
            videoCollecter.release();
        if (audioCollecter != null)
            audioCollecter.release();
    }

    private String getCollecterInfo(IMediaCollecter collecter) {
        if (collecter == null)
            return null;
        StringBuffer buffer = new StringBuffer();
        buffer.append("output:");
        buffer.append(getSize(collecter.getOutputLength()));
        buffer.append("\n");
        buffer.append("bitrate:");
        buffer.append(getSize(collecter.getRTBitrate()));
        return buffer.toString();
    }

    public String getVideoCollecterInfo() {
        return "--------video--------\n" + getCollecterInfo(videoCollecter);
    }

    public String getVideoCollecterParam() {
        String ret = null;
        return ret;
    }

    public String getAudioCollecterInfo() {
        return "--------audio--------\n" + getCollecterInfo(audioCollecter);
    }

    public String getAudioCollecterParam() {
        String ret = null;
        return ret;
    }

    private String getSize(long sizel) {
        String ret = null;
        String unit = null;
        if (sizel < 1024) {
            unit = "B";
        } else if (sizel < 1024 * 1024) {
            unit = "KB";
            sizel = sizel / 1024;
        } else if (sizel < 1024 * 1024 * 1024) {
            unit = "MB";
            sizel = sizel / 1024 / 1024;
        }
        return sizel + unit;
    }

    public void setVideoCollecterCB(IMediaCollecter.Callback videoCollecterCB) {
        this.videoCollecterCB = videoCollecterCB;
    }

    public void setAudioCollecterCB(IMediaCollecter.Callback audioCollecterCB) {
        this.audioCollecterCB = audioCollecterCB;
    }
}
