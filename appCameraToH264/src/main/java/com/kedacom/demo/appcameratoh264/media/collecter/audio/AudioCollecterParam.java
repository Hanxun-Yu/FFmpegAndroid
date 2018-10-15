package com.kedacom.demo.appcameratoh264.media.collecter.audio;

import com.kedacom.demo.appcameratoh264.media.base.AudioChannel;
import com.kedacom.demo.appcameratoh264.media.base.AudioSampleRate;
import com.kedacom.demo.appcameratoh264.media.base.PCMFormat;
import com.kedacom.demo.appcameratoh264.media.collecter.api.ICollecterParam;

/**
 * Created by yuhanxun
 * 2018/9/29
 * description:
 */
public class AudioCollecterParam implements ICollecterParam {


    private PCMFormat format;
    private AudioSampleRate sampleRate;
    private AudioChannel channel;

    public PCMFormat getFormat() {
        return format;
    }

    public void setFormat(PCMFormat format) {
        this.format = format;
    }

    public AudioSampleRate getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(AudioSampleRate sampleRate) {
        this.sampleRate = sampleRate;
    }

    public AudioChannel getChannel() {
        return channel;
    }

    public void setChannel(AudioChannel channel) {
        this.channel = channel;
    }
}
