package com.kedacom.demo.appcameratoh264.media.encoder.api;

/**
 * Created by yuhanxun
 * 2018/9/20
 * description:
 */
public class EncodedData {
    private int length;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    private byte[] data;

    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
    }
}
