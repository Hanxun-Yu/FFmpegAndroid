package com.kedacom.demo.appcameratoh264.media.encoder.video;

import android.util.Log;

import com.kedacom.demo.appcameratoh264.jni.X264EncoderJni;
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
    private X264EncoderJni x264EncoderJni;

    @Override
    public void config(IEncoderParam param) {
        Log.d(TAG,"config param:"+param);
        if (param instanceof X264Param) {
            if (x264EncoderJni != null) {
                x264EncoderJni.release();
            }
            x264EncoderJni = new X264EncoderJni();
            x264EncoderJni.encoderVideoinit((X264Param) param);
        } else {
            throw new IllegalArgumentException("param is not a X264Param!");
        }
    }

    @Override
    public void release() {
        super.release();
        x264EncoderJni.release();
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
            int numNals = x264EncoderJni.encoderVideoEncode(t.getData(), t.getLenght(),
                    fpsIndex++, outbuffer, outbufferLens);
            if(numNals <= 0) {
                return null;
            }
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
