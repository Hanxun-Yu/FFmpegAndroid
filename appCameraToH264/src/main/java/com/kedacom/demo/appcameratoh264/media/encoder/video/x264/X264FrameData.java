package com.kedacom.demo.appcameratoh264.media.encoder.video.x264;


import com.kedacom.demo.appcameratoh264.media.encoder.video.VideoFrameData;

/**
 * Created by yuhanxun
 * 2018/9/21
 * description:
 */
public class X264FrameData extends VideoFrameData {
    private int[] segment;

    public int[] getSegment() {
        return segment;
    }

    public void setSegment(int[] segment) {
        this.segment = segment;
    }

}
