package com.kedacom.demo.appcameratoh264.media.encoder.video;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;

import com.kedacom.demo.appcameratoh264.media.encoder.api.AbstractEncoder;
import com.kedacom.demo.appcameratoh264.media.encoder.api.EncodedData;
import com.kedacom.demo.appcameratoh264.media.encoder.api.IEncoderParam;
import com.kedacom.demo.appcameratoh264.media.encoder.api.PacketData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by yuhanxun
 * 2018/9/21
 * description:
 */
public class AndroidCodecEncoder extends AbstractEncoder {
    private MediaCodec mMediaCodec;
    private static final String VCODEC_MIME = "video/avc";
//    private static final String VCODEC_MIME = "video/hevc";


    private AndroidCodecParam param;

    @Override
    public void config(IEncoderParam param) {
        if (param instanceof AndroidCodecParam) {
            this.param = (AndroidCodecParam) param;
            _config();
        } else {
            throw new IllegalArgumentException("param is not a AndroidCodecParam!");
        }
    }


    private void _config() {
        try {
            MediaCodecInfo mediaCodecInfo = selectCodec(VCODEC_MIME);
            if (mediaCodecInfo == null) {
                throw new RuntimeException("mediaCodecInfo is Empty");
            }
            Log.w(TAG, "MediaCodecInfo " + mediaCodecInfo.getName());
            mMediaCodec = MediaCodec.createByCodecName(mediaCodecInfo.getName());
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(VCODEC_MIME, param.getWidthOUT(), param.getHeightOUT());

            MediaCodecInfo.CodecCapabilities codecCapabilities = mediaCodecInfo.getCapabilitiesForType(VCODEC_MIME);
            MediaCodecInfo.VideoCapabilities videoCapabilities = codecCapabilities.getVideoCapabilities();
            MediaCodecInfo.EncoderCapabilities encoderCapabilities = codecCapabilities.getEncoderCapabilities();
            Log.d(TAG, "codec:" + Arrays.toString(codecCapabilities.colorFormats));
            Log.d(TAG, "video widths:" + videoCapabilities.getSupportedWidths());
            Log.d(TAG, "video heights:" + videoCapabilities.getSupportedHeights());

            Log.d(TAG, "video rate:" + videoCapabilities.getBitrateRange().toString());
            Log.d(TAG, "video fps:" + videoCapabilities.getSupportedFrameRates().toString());


            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, param.getByterate());
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, param.getFps());

            //p2 支持 COLOR_FormatSurface COLOR_FormatYUV420Flexible  COLOR_FormatYUV420SemiPlanar
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
            mediaFormat.setFloat(MediaFormat.KEY_I_FRAME_INTERVAL, param.getGop());
            Log.d(TAG, "mediaFormat:" + mediaFormat);

            mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        mMediaCodec.start();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();

    }

    @Override
    public void release() {
        mMediaCodec.stop();
        mMediaCodec.release();
    }

    @Override
    public void changeBitrate(int byterate) {

    }

    private ByteBuffer inputBuffer;
    private ByteBuffer outputBuffer;
    private byte[] encodeData;
    private byte[] finalData;
    private byte[] spsppsData;

    @Override
    protected EncodedData getEncodedData(PacketData t) {
        AndroidCodecEncodedData ret = null;
        int bufferIndex = mMediaCodec.dequeueInputBuffer(-1);
//        Log.d(TAG, "start IF bufferIndex:" + bufferIndex + " -------------------------------------");

        int totalLenght = 0;
        if (t != null && bufferIndex >= 0) {
            inputBuffer = mMediaCodec.getInputBuffer(bufferIndex);
            inputBuffer.clear();
            inputBuffer.put(t.getData(), 0, t.getLenght());
            mMediaCodec.queueInputBuffer(bufferIndex, 0,
                    inputBuffer.position(),
                    System.nanoTime() / 1000, 0);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 40);
            Log.d(TAG,"outputBufferIndex:"+outputBufferIndex);

            while (outputBufferIndex >= 0) {
                if (ret == null)
                    ret = new AndroidCodecEncodedData();

                outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                outputBuffer.position(bufferInfo.offset);
                outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                encodeData = new byte[outputBuffer.remaining()];
                outputBuffer.get(encodeData);
                //进行处理
                //...
                //存储sps,pps
                if (spsppsData == null) {
                    ByteBuffer bb = ByteBuffer.wrap(encodeData);
                    if (bb.getInt() == 0x00000001 && bb.get() == 0x67) {
//                    if (bb.getInt() == 0x00000001 && bb.get() == 0x40) {
                        spsppsData = new byte[encodeData.length];
                        System.arraycopy(encodeData, 0, spsppsData, 0, encodeData.length);
                    }
                } else {
                    //i帧前插入sps,pps
                    ByteBuffer bb = ByteBuffer.wrap(encodeData);
                    if (bb.getInt() == 0x00000001 && bb.get() == 0x65) {
//                    if (bb.getInt() == 0x00000001 && bb.get() == 0x26) {
                        finalData = new byte[encodeData.length + spsppsData.length];
                        System.arraycopy(spsppsData, 0, finalData, 0, spsppsData.length);
                        System.arraycopy(encodeData, 0, finalData, spsppsData.length, encodeData.length);
                        encodeData = null;
                    } else {
                        finalData = encodeData;
                    }


                    //对YUV420P进行h264编码，返回一个数据大小，里面是编码出来的h264数据
                    //编码后的h264数据
//                                encodeData = new byte[bytes.length];
//                                System.arraycopy(bytes, 0, encodeData, 0, encodeData.length);
                    totalLenght += finalData.length;

                    ret.getBytes().add(finalData);
                }
                Log.d(TAG,"spsppsData:"+spsppsData);
                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 40);
            }
            if (ret != null)
                ret.setLength(totalLenght);

        }

        return ret;
    }

    private MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            //是否是编码器
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            Log.w(TAG, Arrays.toString(types));
            for (String type : types) {
                if (mimeType.equalsIgnoreCase(type)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }
}
