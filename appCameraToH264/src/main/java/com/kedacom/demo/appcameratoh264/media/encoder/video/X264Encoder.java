package com.kedacom.demo.appcameratoh264.media.encoder.video;

import com.kedacom.demo.appcameratoh264.jni.FFmpegjni;
import com.kedacom.demo.appcameratoh264.media.encoder.api.AbstractEncoder;
import com.kedacom.demo.appcameratoh264.media.encoder.api.EncodedData;
import com.kedacom.demo.appcameratoh264.media.encoder.api.IEncoderParam;
import com.kedacom.demo.appcameratoh264.media.encoder.api.PacketData;

/**
 * Created by yuhanxun
 * 2018/9/21
 * description:
 */
public class X264Encoder extends AbstractEncoder {
    private FFmpegjni ffmpegjni;

    @Override
    public void config(IEncoderParam param) {
        if (param instanceof X264Param) {
            if (ffmpegjni != null) {
                ffmpegjni.release();
            }
            ffmpegjni = new FFmpegjni();
            ffmpegjni.encoderVideoinit((X264Param) param);
        } else {
            throw new IllegalArgumentException("param is not a X264Param!");
        }
    }

    @Override
    public void release() {
        ffmpegjni.release();
    }

    @Override
    public void changeBitrate(int byterate) {
        throw new IllegalStateException("Not support for now.");
    }

    private int fpsIndex = 0;

    @Override
    protected EncodedData getEncodedData(PacketData t) {
        X264EncodedData ret = null;
        if(t != null && t.getData()!=null && t.getLenght() != 0) {
            byte[] outbuffer = new byte[t.getLenght()];
            int[] outbufferLens = new int[20];
            int numNals = ffmpegjni.encoderVideoEncode(t.getData(), t.getLenght(),
                    fpsIndex++, outbuffer, outbufferLens);
            int[] segment = new int[numNals];
            System.arraycopy(outbufferLens, 0, segment, 0, numNals);
            int totalLength = 0;

            for (int i = 0; i < segment.length; i++) {
                totalLength += segment[i];
            }

            byte[] encodecData = new byte[totalLength];
            System.arraycopy(outbuffer, 0, encodecData, 0, totalLength);

            ret = new X264EncodedData();
            ret.setLength(totalLength);
            ret.setSegment(segment);
            ret.setData(encodecData);
        }
        return ret;
    }
}
