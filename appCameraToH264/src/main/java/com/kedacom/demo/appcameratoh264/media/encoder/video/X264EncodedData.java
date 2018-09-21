package com.kedacom.demo.appcameratoh264.media.encoder.video;

import com.kedacom.demo.appcameratoh264.media.encoder.api.EncodedData;

/**
 * Created by yuhanxun
 * 2018/9/21
 * description:
 */
public class X264EncodedData extends EncodedData {
    private int[] segment;

    public int[] getSegment() {
        return segment;
    }

    public void setSegment(int[] segment) {
        this.segment = segment;
    }
}
