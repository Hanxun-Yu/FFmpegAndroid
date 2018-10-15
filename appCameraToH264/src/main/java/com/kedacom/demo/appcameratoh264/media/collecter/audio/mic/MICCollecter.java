package com.kedacom.demo.appcameratoh264.media.collecter.audio.mic;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.kedacom.demo.appcameratoh264.media.base.AudioChannel;
import com.kedacom.demo.appcameratoh264.media.base.AudioSampleRate;
import com.kedacom.demo.appcameratoh264.media.base.PCMFormat;
import com.kedacom.demo.appcameratoh264.media.collecter.audio.AbstractAudioCollecter;
import com.kedacom.demo.appcameratoh264.media.collecter.audio.AudioCollecterParam;

import java.util.Arrays;

/**
 * Created by yuhanxun
 * 2018/10/9
 * description:
 */
public class MICCollecter extends AbstractAudioCollecter{
    private final static int SOURCE = MediaRecorder.AudioSource.MIC;
    private int bufferSizeInBytes = 0;

    private int SAMPLE_HZ = 0;
    private int CHANNEL_CONFIG = 0;
    private int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord mAudioRecord;
    private int mBufferSize;

    private AudioCollRunn audioCollRunn = null;
    private Thread workThread;
    @Override
    protected void _config(AudioCollecterParam param) {
        SAMPLE_HZ = getSampleRate(param.getSampleRate());
        CHANNEL_CONFIG =getChannel(param.getChannel());
        FORMAT = getFormat(param.getFormat());
        bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_HZ, CHANNEL_CONFIG, FORMAT);
        mAudioRecord = new AudioRecord(SOURCE, SAMPLE_HZ, CHANNEL_CONFIG, FORMAT, bufferSizeInBytes);
        mBufferSize = 4 * 1024;
    }

    private int getSampleRate(AudioSampleRate audioSampleRate) {
       return audioSampleRate.getData();
    }

    private int getChannel(AudioChannel audioChannel) {
        int ret = 0;
        switch (audioChannel) {
            case C1:
                ret = AudioFormat.CHANNEL_IN_MONO;
                break;
            case C2:
                ret = AudioFormat.CHANNEL_IN_STEREO;
                break;
//            case C2p1:
//                break;
//            case C5p1:
//                break;
        }
        return ret;
    }

    private int getFormat(PCMFormat pcmFormat) {
        int ret = 0;
        switch (pcmFormat) {
            case PCM_8BIT:
                ret = AudioFormat.ENCODING_PCM_8BIT;
                break;
            case PCM_16BIT:
                ret = AudioFormat.ENCODING_PCM_16BIT;
                break;
        }
        return ret;
    }

    @Override
    protected boolean isFormatSupported(PCMFormat pcmFormat) {
        boolean ret = false;
        switch (pcmFormat) {
            case PCM_16BIT:
                ret = true;
                break;
        }
        return ret;
    }

    @Override
    protected boolean isChannelSupported(AudioChannel audioChannel) {
        boolean ret = false;
        switch (audioChannel) {
            case C1:
            case C2:
                ret = true;
                break;
        }
        return ret;
    }

    @Override
    protected boolean isSampleRateSupported(AudioSampleRate audioSampleRate) {
        boolean ret = false;
        switch (audioSampleRate) {
            case SR_44100:
                ret = true;
                break;
        }
        return ret;
    }

    @Override
    public void init() {



    }

    @Override
    public void start() {
        workThread = new Thread(new AudioCollRunn());
        workThread.start();
    }

    @Override
    public void stop() {
        if(audioCollRunn != null) {
            audioCollRunn.setStop(true);
            audioCollRunn = null;
        }
        if(workThread != null) {
            workThread.interrupt();
            workThread = null;
        }
        mAudioRecord.stop();
    }

    @Override
    public void release() {
        mAudioRecord.release();
        mAudioRecord = null;
    }

    @Override
    public int getRTBitrate() {
        return 0;
    }

    @Override
    public int getOutputLength() {
        return 0;
    }

    class AudioCollRunn implements Runnable {
        boolean stop = false;
        @Override
        public void run() {
            mAudioRecord.startRecording();
            byte[] audioData = new byte[mBufferSize];
            int readsize = 0;
            //录音，获取PCM裸音频，这个音频数据文件很大，我们必须编码成AAC，这样才能rtmp传输
            while (!stop && !Thread.interrupted()) {
                try {
                    readsize += mAudioRecord.read(audioData, readsize, mBufferSize);
                    byte[] ralAudio = new byte[readsize];
                    //每次录音读取4K数据
                    System.arraycopy(audioData, 0, ralAudio, 0, readsize);


                    notifyRTFrame(ralAudio,ralAudio.length,collecterParam.getFormat(),
                            collecterParam.getChannel(),collecterParam.getSampleRate());
                    readsize = 0;
                    Arrays.fill(audioData, (byte) 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void setStop(boolean stop) {
            this.stop = stop;
        }
    }
 }
