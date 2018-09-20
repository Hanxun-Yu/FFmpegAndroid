package com.kedacom.demo.appcameratoh264.media.api;

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

}
