package com.kedacom.demo.appcameratoh264.media.collecter.video;

import com.kedacom.demo.appcameratoh264.media.base.YuvData;
import com.kedacom.demo.appcameratoh264.media.collecter.api.ICollectData;

/**
 * Created by yuhanxun
 * 2018/9/29
 * description:
 */
public class VideoCollectData implements ICollectData, Cloneable{
    private YuvData yuvData;
    private long timestamp;

    public VideoCollectData(YuvData yuvData, long timestamp) {
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

    public VideoCollectData clone() throws CloneNotSupportedException {
        VideoCollectData ret = null;
        ret = (VideoCollectData) super.clone();
        ret.setYuvData(yuvData.clone());
        return ret;
    }
}
