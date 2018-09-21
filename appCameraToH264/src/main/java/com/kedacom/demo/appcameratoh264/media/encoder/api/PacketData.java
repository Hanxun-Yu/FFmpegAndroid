package com.kedacom.demo.appcameratoh264.media.encoder.api;

/**
 * Created by yuhanxun
 * 2018/9/20
 * description:
 */
public class PacketData {
    public long getTimestampMilliSec() {
        return timestampMilliSec;
    }

    public void setTimestampMilliSec(long timestampMilliSec) {
        this.timestampMilliSec = timestampMilliSec;
    }

    private long timestampMilliSec;
    private byte[] data;
    private int lenght;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getLenght() {
        return lenght;
    }

    public void setLenght(int lenght) {
        this.lenght = lenght;
    }
}
