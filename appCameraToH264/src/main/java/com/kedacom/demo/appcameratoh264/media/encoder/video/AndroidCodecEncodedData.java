package com.kedacom.demo.appcameratoh264.media.encoder.video;

import com.kedacom.demo.appcameratoh264.media.encoder.api.EncodedData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuhanxun
 * 2018/9/21
 * description:
 */
public class AndroidCodecEncodedData extends EncodedData {

    private List<byte[]> bytes = new ArrayList<>();

    public List<byte[]> getBytes() {
        return bytes;
    }

    public void setBytes(List<byte[]> bytes) {
        this.bytes = bytes;
    }
}
