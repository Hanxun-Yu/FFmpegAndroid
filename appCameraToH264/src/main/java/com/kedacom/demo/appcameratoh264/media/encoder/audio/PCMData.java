package com.kedacom.demo.appcameratoh264.media.encoder.audio;

import com.kedacom.demo.appcameratoh264.media.encoder.api.PacketData;

public class PCMData extends PacketData{
    public PCMData(byte[] data,int length) {
        setData(data);
        setLenght(length);
    }
}
