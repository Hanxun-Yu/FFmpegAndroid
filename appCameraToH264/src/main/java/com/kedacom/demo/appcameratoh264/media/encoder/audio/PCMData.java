package com.kedacom.demo.appcameratoh264.media.encoder.audio;

import com.kedacom.demo.appcameratoh264.media.encoder.api.IPacketData;

public class PCMData implements IPacketData {
    private byte[] data;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public PCMData(byte[] data) {
        this.data = data;
    }
}
