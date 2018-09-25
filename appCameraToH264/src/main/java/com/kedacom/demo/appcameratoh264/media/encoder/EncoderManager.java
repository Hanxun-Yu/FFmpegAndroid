package com.kedacom.demo.appcameratoh264.media.encoder;

import android.util.Log;

import com.kedacom.demo.appcameratoh264.media.FileManager;
import com.kedacom.demo.appcameratoh264.media.encoder.api.EncodedData;
import com.kedacom.demo.appcameratoh264.media.encoder.api.IMediaEncoder;
import com.kedacom.demo.appcameratoh264.media.encoder.api.PacketData;
import com.kedacom.demo.appcameratoh264.media.encoder.api.VideoEncoderParam;
import com.kedacom.demo.appcameratoh264.media.encoder.audio.AACEncoder;
import com.kedacom.demo.appcameratoh264.media.encoder.video.AndroidCodecEncodedData;
import com.kedacom.demo.appcameratoh264.media.encoder.video.AndroidCodecEncoder;
import com.kedacom.demo.appcameratoh264.media.encoder.video.X264Encoder;
import com.kedacom.demo.appcameratoh264.media.encoder.video.YuvData;
import com.kedacom.demo.appcameratoh264.media.encoder.video.YuvInserter;

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


    private IMediaEncoder.Callback videoEncoderCB;
    private IMediaEncoder.Callback audioEncoderCB;


    private IMediaEncoder.OnStateChangedListener onStateChangedListener;

    public void config(EncoderConfig config) {
        Log.e(TAG, "config");
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
            videoEncoder.init();
            videoEncoder.config(config.getVideoParam());
            videoEncoder.setCallback(new IMediaEncoder.Callback() {
                @Override
                public void onDataEncoded(EncodedData encodedData) {
                    if (videoSaver != null) {
                        if (encodedData instanceof AndroidCodecEncodedData) {

                            for (byte[] bytes : ((AndroidCodecEncodedData) encodedData).getBytes()) {
                                videoSaver.writeFileData(bytes);
                                videoLenSaver.writeFileData((String.valueOf(bytes.length) + "\n").getBytes());
                            }
                        } else {
                            videoSaver.writeFileData(encodedData.getData());
                            videoLenSaver.writeFileData((String.valueOf(encodedData.getLength()) + "\n").getBytes());
                        }
                    }


                    if (videoEncoderCB != null)
                        videoEncoderCB.onDataEncoded(encodedData);
                }
            });
            videoEncoder.setOnStateChangedListener(new IMediaEncoder.OnStateChangedListener() {
                @Override
                public void onState(IMediaEncoder encoder, IMediaEncoder.State state) {
                    Log.d(TAG, "videoEncoder onState:" + state);
                    if (onStateChangedListener != null)
                        onStateChangedListener.onState(encoder, state);
                }

            });

            inserter = new YuvInserter(videoEncoder, ((VideoEncoderParam) config.getVideoParam()).getFps());

            if (config.getVideoSavePath() != null) {
                videoSaver = new FileManager(config.getVideoSavePath());
                videoLenSaver = new FileManager(config.getVideoSavePath() + "_len");
            }
        }


        if (config.getAudio() != null) {
            switch (config.getAudio()) {
                case AAC:
                    audioEncoder = new AACEncoder();
                    break;
            }
            audioEncoder.init();
            audioEncoder.config(config.getAudioParam());
            audioEncoder.setCallback(new IMediaEncoder.Callback() {
                @Override
                public void onDataEncoded(EncodedData encodedData) {
                    if (audioSaver != null)
                        audioSaver.writeFileData(encodedData.getData());
                    if (audioEncoderCB != null)
                        audioEncoderCB.onDataEncoded(encodedData);
                }
            });
            audioEncoder.setOnStateChangedListener(new IMediaEncoder.OnStateChangedListener() {
                @Override
                public void onState(IMediaEncoder encoder, IMediaEncoder.State state) {
                    Log.d(TAG, "audioEncoder onState:" + state);
                    if (onStateChangedListener != null)
                        onStateChangedListener.onState(encoder, state);
                }

            });
            if (config.getAudioSavePath() != null) {
                audioSaver = new FileManager(config.getAudioSavePath());

            }
        }

    }

    YuvInserter inserter;

    public void encodeVideo(PacketData data) {
        if (videoEncoder != null) {
            if (inserter != null) {
                inserter.putVideoData((YuvData) data);
            } else {
                videoEncoder.putPacket(data);
            }
        }
    }

    public void encodeAudio(PacketData data) {
        if (audioEncoder != null)
            audioEncoder.putPacket(data);
    }


    public void start() {
        Log.e(TAG, "start");
        if (videoSaver != null)
            videoSaver.openFile();
        if (videoLenSaver != null)
            videoLenSaver.openFile();
        if (audioSaver != null)
            audioSaver.openFile();

        if (videoEncoder != null)
            videoEncoder.start();
        if (audioEncoder != null)
            audioEncoder.start();
    }

    public void stop() {
        Log.e(TAG, "stop");
        if (videoEncoder != null)
            videoEncoder.stop();
        if (audioEncoder != null)
            audioEncoder.stop();
        if (videoSaver != null) {
            videoSaver.closeFile();
        }
        if (videoLenSaver != null) {
            videoLenSaver.closeFile();
        }
        if (audioSaver != null) {
            audioSaver.closeFile();
        }
    }

    public void release() {
        Log.e(TAG, "release");

        if (videoEncoder != null)
            videoEncoder.release();
        if (audioEncoder != null)
            audioEncoder.release();
    }

    private String getEncodeInfo(IMediaEncoder encoder) {
        if (encoder == null)
            return null;
        StringBuffer buffer = new StringBuffer();
        buffer.append("state:");
        buffer.append(getEncodeStateInfo(encoder));
        buffer.append("\n");
        buffer.append("intput:");
        buffer.append(encoder.getRTInputRate());
        buffer.append("\n");
        buffer.append("output:");
        buffer.append(encoder.getRTOutputRate());
        buffer.append("\n");
        buffer.append("bitrate:");
        buffer.append(getSize(encoder.getRTBitrate()));
        buffer.append("\n");
        buffer.append("queue:");
        buffer.append(encoder.getQueueSize());
        buffer.append("\n");
        buffer.append("encoded:");
        buffer.append(getSize(encoder.getLengthEncoded()));
        return buffer.toString();
    }

    private String getEncodeStateInfo(IMediaEncoder encoder) {
        StringBuffer buffer = new StringBuffer();
        switch (encoder.getState()) {
            case IDLE:
                buffer.append("<font color=\"green\">");
                break;
            case Encoding:
                buffer.append("<font color=\"red\">");
                break;
            case Starting:
            case EncodeStoping:
                buffer.append("<font color=\"#FFCC66\">");
                break;
        }
        buffer.append(encoder.getState());
        buffer.append("</font>");
        return buffer.toString();
    }

    public String getVideoEncoderInfo() {
        return "--------video--------\n" + getEncodeInfo(videoEncoder);
    }

    public String getVideoEncoderParam() {
        String ret = null;
        return ret;
    }

    public String getAudioEncoderInfo() {
        return "--------audio--------\n" + getEncodeInfo(audioEncoder);
    }

    public String getAudioEncoderParam() {
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

    public void setVideoEncoderCB(IMediaEncoder.Callback videoEncoderCB) {
        this.videoEncoderCB = videoEncoderCB;
    }

    public void setAudioEncoderCB(IMediaEncoder.Callback audioEncoderCB) {
        this.audioEncoderCB = audioEncoderCB;
    }

    public void setOnStateChangedListener(IMediaEncoder.OnStateChangedListener onStateChangedListener) {
        this.onStateChangedListener = onStateChangedListener;
    }

}
