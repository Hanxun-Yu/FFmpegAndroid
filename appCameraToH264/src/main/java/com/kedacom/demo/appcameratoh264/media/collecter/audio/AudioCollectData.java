package com.kedacom.demo.appcameratoh264.media.collecter.audio;

import com.kedacom.demo.appcameratoh264.media.base.PCMData;
import com.kedacom.demo.appcameratoh264.media.collecter.api.ICollectData;

/**
 * Created by yuhanxun
 * 2018/9/29
 * description:
 */
public class AudioCollectData implements ICollectData, Cloneable {
    private PCMData pcmData;
    private long timestamp;

    public AudioCollectData(PCMData pcmData, long timestamp) {
        this.pcmData = pcmData;
        this.timestamp = timestamp;
    }

    public PCMData getPCMData() {
        return pcmData;
    }

    public void setPCMData(PCMData pcmData) {
        this.pcmData = pcmData;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public AudioCollectData clone() throws CloneNotSupportedException {
        AudioCollectData ret = null;
        ret = (AudioCollectData) super.clone();
        ret.setPCMData(pcmData.clone());
        return ret;
    }
}
