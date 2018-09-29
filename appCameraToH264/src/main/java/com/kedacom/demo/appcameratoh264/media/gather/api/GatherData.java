package com.kedacom.demo.appcameratoh264.media.gather.api;

/**
 * Created by yuhanxun
 * 2018/9/20
 * description:
 */
public class GatherData implements Cloneable{
    public long getTimestampMilliSec() {
        return timestampMilliSec;
    }

    public void setTimestampMilliSec(long timestampMilliSec) {
        this.timestampMilliSec = timestampMilliSec;
    }

    private long timestampMilliSec;
    private byte[] data;
    private int length;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }


    public GatherData(byte[] data, int lenght, long timestampMilliSec) {
        this.timestampMilliSec = timestampMilliSec;
        this.data = data;
        this.length = lenght;
    }

    public GatherData clone() throws CloneNotSupportedException {
        return (GatherData) super.clone();
    }
}
