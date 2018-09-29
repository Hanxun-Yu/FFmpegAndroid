package com.kedacom.demo.appcameratoh264.media.util;

import com.kedacom.demo.appcameratoh264.media.YuvFormat;

public class YuvData {
    private byte[] data;
    private int length;

    private int width;
    private int height;
    private YuvFormat format;

    public YuvData(byte[] data, int length, int width, int height, YuvFormat format) {
        this.data = data;
        this.length = length;
        this.width = width;
        this.height = height;
        this.format = format;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public YuvFormat getFormat() {
        return format;
    }

    public void setFormat(YuvFormat format) {
        this.format = format;
    }

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

}