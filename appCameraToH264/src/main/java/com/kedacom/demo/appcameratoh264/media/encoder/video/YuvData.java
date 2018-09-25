package com.kedacom.demo.appcameratoh264.media.encoder.video;

import com.kedacom.demo.appcameratoh264.media.encoder.api.PacketData;

public class YuvData extends PacketData{
    public int width;
    public int height;

    public YuvData(byte[] videoData,int length, int width, int height, long timestamp) {
        setData(videoData);
        setLenght(length);
        this.width = width;
        this.height = height;
        setTimestampMilliSec(timestamp);
    }


}
