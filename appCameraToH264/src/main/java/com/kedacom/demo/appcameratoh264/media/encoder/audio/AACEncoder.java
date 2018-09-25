package com.kedacom.demo.appcameratoh264.media.encoder.audio;

import com.kedacom.demo.appcameratoh264.jni.AudioEncoderJni;
import com.kedacom.demo.appcameratoh264.media.encoder.api.AbstractEncoder;
import com.kedacom.demo.appcameratoh264.media.encoder.api.EncodedData;
import com.kedacom.demo.appcameratoh264.media.encoder.api.IEncoderParam;
import com.kedacom.demo.appcameratoh264.media.encoder.api.PacketData;
import com.kedacom.demo.appcameratoh264.media.encoder.video.X264EncodedData;

/**
 * Created by yuhanxun
 * 2018/9/21
 * description:
 */
public class AACEncoder extends AbstractEncoder{
    AudioEncoderJni audioEncoderJni;
    private int audioEncodeBuffer;

    @Override
    public void config(IEncoderParam param) {
        if (audioEncoderJni != null) {
            audioEncoderJni.release();
        }
        audioEncoderJni = new AudioEncoderJni();
        audioEncodeBuffer = audioEncoderJni.encoderAudioInit(Contacts.SAMPLE_RATE,
                Contacts.CHANNELS, Contacts.BIT_RATE);
        inbuffer = new byte[audioEncodeBuffer];
    }

    @Override
    public void release() {
        super.release();
        audioEncoderJni.release();
    }

    @Override
    public void changeBitrate(int byterate) {
        throw new IllegalStateException("Not support for now.");
    }

    //我们通过fdk-aac接口获取到了audioEncodeBuffer的数据，即每次编码多少数据为最优
    //这里我这边的手机每次都是返回的4096即4K的数据，其实为了简单点，我们每次可以让
    //MIC录取4K大小的数据，然后把录取的数据传递到AudioEncoder.cpp中取编码

    //每次采集的pcm可能不够一次编码,多次囤积再送入编码器
    int haveCopyLength = 0;
    byte[] outbuffer = new byte[2048];
    byte[] inbuffer;
    @Override
    protected EncodedData getEncodedData(PacketData t) {
        EncodedData ret = null;
        if (haveCopyLength < audioEncodeBuffer) {
            System.arraycopy(t.getData(), 0, inbuffer, haveCopyLength, t.getLenght());
            haveCopyLength += t.getLenght();
            int remain = audioEncodeBuffer - haveCopyLength;
            if (remain == 0) {
                //fdk-aac编码PCM裸音频数据，返回可用长度的有效字段
                int validLength = audioEncoderJni.encoderAudioEncode(inbuffer, audioEncodeBuffer, outbuffer, outbuffer.length);
                //Log.e("lihuzi", " validLength " + validLength);
                final int VALID_LENGTH = validLength;
                if (VALID_LENGTH > 0) {
                    byte[] encodeData = new byte[VALID_LENGTH];
                    System.arraycopy(outbuffer, 0, encodeData, 0, VALID_LENGTH);
                    ret = new X264EncodedData();
                    ret.setLength(validLength);
                    ret.setData(encodeData);
                }
                haveCopyLength = 0;

            }
        }
        return ret;
    }
}
