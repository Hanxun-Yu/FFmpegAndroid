package com.kedacom.demo.appcameratoh264.media.gather.video;

import com.kedacom.demo.appcameratoh264.media.YuvFormat;
import com.kedacom.demo.appcameratoh264.media.gather.api.GatherData;
import com.kedacom.demo.appcameratoh264.media.util.YuvData;

/**
 * Created by yuhanxun
 * 2018/9/29
 * description:
 */
public class VideoGatherData extends GatherData implements Cloneable{
    private int width;
    private int height;
    private YuvFormat format;

    public VideoGatherData(byte[] data, int length,int width,int height,YuvFormat format, long timestampMilliSec) {
        super(data, length, timestampMilliSec);
        this.width = width;
        this.height = height;
        this.format = format;
    }

    public VideoGatherData(YuvData yuvData,long timestamp) {
        super(yuvData.getData(),yuvData.getLength(),timestamp);
        setWidth(yuvData.getWidth());
        setHeight(yuvData.getHeight());
        setFormat(yuvData.getFormat());
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
}
