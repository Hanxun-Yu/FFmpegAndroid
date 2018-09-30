package com.kedacom.demo.appcameratoh264.media.encoder.video;

import com.kedacom.demo.appcameratoh264.media.base.YuvData;
import com.kedacom.demo.appcameratoh264.media.encoder.api.IPacketData;

/**
 * Created by yuhanxun
 * 2018/9/30
 * description:
 */
public class VideoPacketData implements IPacketData {
    private YuvData yuvData;
    private long timestamp;

    public VideoPacketData(YuvData yuvData, long timestamp) {
        this.yuvData = yuvData;
        this.timestamp = timestamp;
    }

    public YuvData getYuvData() {
        return yuvData;
    }

    public void setYuvData(YuvData yuvData) {
        this.yuvData = yuvData;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public VideoPacketData clone() throws CloneNotSupportedException {
        VideoPacketData ret = null;
        ret = (VideoPacketData) super.clone();
        ret.setYuvData(yuvData.clone());
        return ret;
    }
}
