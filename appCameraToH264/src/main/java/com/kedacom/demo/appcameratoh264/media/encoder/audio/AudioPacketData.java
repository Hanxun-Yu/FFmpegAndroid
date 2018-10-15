package com.kedacom.demo.appcameratoh264.media.encoder.audio;

import com.kedacom.demo.appcameratoh264.media.base.PCMData;
import com.kedacom.demo.appcameratoh264.media.encoder.api.IPacketData;

/**
 * Created by yuhanxun
 * 2018/10/12
 * description:
 */
public class AudioPacketData implements IPacketData {
    private PCMData data;
    private long timestamp;

    public AudioPacketData(PCMData data, long timestamp) {
        this.data = data;
        this.timestamp = timestamp;
    }

    public PCMData getData() {
        return data;
    }

    public void setData(PCMData data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
