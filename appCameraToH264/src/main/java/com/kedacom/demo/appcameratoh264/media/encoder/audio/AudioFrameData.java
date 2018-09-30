package com.kedacom.demo.appcameratoh264.media.encoder.audio;

import com.kedacom.demo.appcameratoh264.media.encoder.api.IFrameData;

/**
 * Created by yuhanxun
 * 2018/9/30
 * description:
 */
public class AudioFrameData implements IFrameData{
    private byte[] data;
    @Override
    public int getLength() {
        return data.length;
    }

    public AudioFrameData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
