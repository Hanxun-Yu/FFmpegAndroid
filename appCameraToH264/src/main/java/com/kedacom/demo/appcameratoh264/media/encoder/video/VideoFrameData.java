package com.kedacom.demo.appcameratoh264.media.encoder.video;

import com.kedacom.demo.appcameratoh264.media.encoder.api.IFrameData;

/**
 * Created by yuhanxun
 * 2018/9/30
 * description:
 */
public class VideoFrameData implements IFrameData{
    private byte[] data;
    private int width;
    private int height;
    private boolean iFrame;
    private long timestamp;

    @Override
    public int getLength() {
        return data.length;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
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

    public boolean isiFrame() {
        return iFrame;
    }

    public void setiFrame(boolean iFrame) {
        this.iFrame = iFrame;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
