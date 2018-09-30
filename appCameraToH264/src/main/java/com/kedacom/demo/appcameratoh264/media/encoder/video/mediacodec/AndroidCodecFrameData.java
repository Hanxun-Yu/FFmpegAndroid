package com.kedacom.demo.appcameratoh264.media.encoder.video.mediacodec;

import com.kedacom.demo.appcameratoh264.media.encoder.api.IFrameData;
import com.kedacom.demo.appcameratoh264.media.encoder.video.VideoFrameData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuhanxun
 * 2018/9/21
 * description:
 */
public class AndroidCodecFrameData extends VideoFrameData {

    private List<byte[]> bytes = new ArrayList<>();

    public List<byte[]> getBytes() {
        return bytes;
    }

    public void setBytes(List<byte[]> bytes) {
        this.bytes = bytes;
    }

    @Override
    public int getLength() {
        int ret = 0;
        for(int i=0;i<bytes.size();i++ ){
            ret += bytes.get(i).length;
        }
        return ret;
    }
}
