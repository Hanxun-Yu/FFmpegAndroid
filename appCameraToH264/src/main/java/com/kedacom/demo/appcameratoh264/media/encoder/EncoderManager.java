package com.kedacom.demo.appcameratoh264.media.encoder;

import android.util.Log;

import com.kedacom.demo.appcameratoh264.media.FileManager;
import com.kedacom.demo.appcameratoh264.media.encoder.api.EncodedData;
import com.kedacom.demo.appcameratoh264.media.encoder.api.IMediaEncoder;
import com.kedacom.demo.appcameratoh264.media.encoder.api.PacketData;
import com.kedacom.demo.appcameratoh264.media.encoder.audio.AACEncoder;
import com.kedacom.demo.appcameratoh264.media.encoder.video.AndroidCodecEncoder;
import com.kedacom.demo.appcameratoh264.media.encoder.video.X264Encoder;

/**
 * Created by yuhanxun
 * 2018/9/21
 * description:
 */
public class EncoderManager {
    private final String TAG = getClass().getSimpleName() + "_xunxun";

    private EncoderConfig config;
    private IMediaEncoder videoEncoder;
    private IMediaEncoder audioEncoder;

    private FileManager videoSaver;
    private FileManager videoLenSaver;
    private FileManager audioSaver;

    public void config(EncoderConfig config) {
        this.config = config;
        if (config.getVideo() != null) {
            switch (config.getVideo()) {
                case X264:
                    videoEncoder = new X264Encoder();
                    break;
                case MediaCodec:
                    videoEncoder = new AndroidCodecEncoder();
                    break;
            }
            videoEncoder.config(config.getVideoParam());
            videoEncoder.setCallback(new IMediaEncoder.Callback() {
                @Override
                public void onDataEncoded(EncodedData encodedData) {
                    if (videoSaver != null) {
                        videoSaver.writeFileData(encodedData.getData());
                        videoLenSaver.writeFileData((String.valueOf(encodedData.getLength()) + "\n").getBytes());
                    }
                }
            });
            videoEncoder.setOnStateChangedListener(new IMediaEncoder.OnStateChangedListener() {
                @Override
                public void onState(IMediaEncoder.State state) {
                    Log.d(TAG, "videoEncoder onState:" + state);
                }
            });

            if (config.getVideoSavePath() != null) {
                videoSaver = new FileManager(config.getVideoSavePath());
                videoLenSaver = new FileManager(config.getVideoSavePath() + "_len");
                videoSaver.openFile();
                videoLenSaver.openFile();
            }
        }


        if (config.getAudio() != null) {
            switch (config.getAudio()) {
                case AAC:
                    audioEncoder = new AACEncoder();
                    break;
            }
            audioEncoder.config(config.getAudioParam());
            audioEncoder.setCallback(new IMediaEncoder.Callback() {
                @Override
                public void onDataEncoded(EncodedData encodedData) {
                    if (audioSaver != null)
                        audioSaver.writeFileData(encodedData.getData());
                }
            });
            audioEncoder.setOnStateChangedListener(new IMediaEncoder.OnStateChangedListener() {
                @Override
                public void onState(IMediaEncoder.State state) {
                    Log.d(TAG, "audioEncoder onState:" + state);
                }
            });
            if (config.getAudioSavePath() != null) {
                audioSaver = new FileManager(config.getAudioSavePath());
                audioSaver.openFile();
            }
        }

    }

    public void encodeVideo(PacketData data) {
        if(videoEncoder != null)
            videoEncoder.putPacket(data);
    }

    public void encodeAudio(PacketData data) {
        if(audioEncoder != null)
            audioEncoder.putPacket(data);
    }


    public void start() {
        if (videoEncoder != null)
            videoEncoder.start();
        if (audioEncoder != null)
            audioEncoder.start();
    }

    public void stop() {
        if (videoEncoder != null)
            videoEncoder.stop();
        if (audioEncoder != null)
            audioEncoder.stop();
    }

    public void release() {
        if (videoEncoder != null)
            videoEncoder.release();
        if (audioEncoder != null)
            audioEncoder.release();
    }
}
