package com.kedacom.demo.appcameratoh264.media.encoder.video;


import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;

import com.kedacom.demo.appcameratoh264.jni.FFmpegjni;
import com.kedacom.demo.appcameratoh264.media.FileManager;
import com.kedacom.demo.appcameratoh264.media.encoder.audio.AudioData;
import com.kedacom.demo.appcameratoh264.media.encoder.audio.Contacts;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * 编码类MediaEncoder，主要是把视频流YUV420P格式编码为h264格式,把PCM裸音频转化为AAC格式
 */
public class MediaEncoder2Codec extends MediaEncoder{
    private final String TAG = getClass().getSimpleName() + "_xunxun";

    private Thread videoEncoderThread, audioEncoderThread;

    //编码线程是否已经停止
    private boolean isVideoEncoderThreadStop = true;
    private boolean isAudioEncoderThreadStop = true;
    //是否允许编码线程循环
    private boolean videoEncoderLoop, audioEncoderLoop;

    private boolean isMuxing = false;

    //视频流队列
    private LinkedBlockingQueue<YuvData> videoQueue;
    //音频流队列
    private LinkedBlockingQueue<AudioData> audioQueue;

    private FileManager videoFileManager;
    private FileManager video_LenFileManager;
    private FileManager audioFileManager;
    private static final boolean SAVE_FILE_FOR_TEST = true;
    private int audioEncodeBuffer;

    private MediaEncoder.MediaEncoderCallback sMediaEncoderCallback;

    FFmpegjni ffmpegjni;
    private MediaCodec mMediaCodec;



    public void setsMediaEncoderCallback(MediaEncoder.MediaEncoderCallback callback) {
        sMediaEncoderCallback = callback;
    }

    public MediaEncoder2Codec() {
        if (SAVE_FILE_FOR_TEST) {
            videoFileManager = new FileManager(FileManager.TEST_H264_FILE);
            video_LenFileManager = new FileManager(FileManager.TEST_H264_LEN_FILE);
            audioFileManager = new FileManager(FileManager.TEST_AAC_FILE);
        }
        videoQueue = new LinkedBlockingQueue<>();
        audioQueue = new LinkedBlockingQueue<>();

        refreshState();

//        new Thread() {
//            @Override
//            public void run() {
//                while(true) {
//                    Log.d(TAG, "loop--------------------");
//                }
//            }
//        }.start();
    }

    public synchronized boolean start(X264Param param) {
//        startAudioEncode();
        if (ffmpegjni != null) {
            ffmpegjni.release();
        }
        ffmpegjni = new FFmpegjni();
        initMediaCodec(param);
        audioEncodeBuffer = ffmpegjni.encoderAudioInit(Contacts.SAMPLE_RATE,
                Contacts.CHANNELS, Contacts.BIT_RATE);

        if (SAVE_FILE_FOR_TEST) {
            videoFileManager.openFile();
            audioFileManager.openFile();
            video_LenFileManager.openFile();
        }
        startVideoEncode();
        startAudioEncode();
        isStop = false;
        refreshState();
        return true;
    }

    public boolean isStop() {
        return isStop;
    }

    boolean isStop = true;

    public synchronized void stop() {
        isStop = true;
        stopAudioEncode();
        stopVideoEncode();
        refreshState();
    }


    public void release() {
    }

    private long firstInputTime = 0;

    //摄像头的YUV420P数据，put到队列中，生产者模型
    YuvData lastPuttedVideoData;

    public synchronized void putVideoData(YuvData videoData) {
//        Log.d(TAG,"putVideoData queueSize:"+videoQueue.size()+ " takeYUVCount:"+takeYUVCount);
        if (firstInputTime == 0)
            firstInputTime = System.currentTimeMillis();

        putCAMCount++;
        if (checkInsertVideo(videoData))
            doPutVideoData(videoData);
    }

    private void doPutVideoData(YuvData videoData) {
//        Log.d(TAG,"videoQueue.size():"+videoQueue.size());
//        Log.d(TAG, "doPutVideoData videoQueue size:"+videoQueue.size());
        try {
            putYUVCount++;
            videoQueue.put(videoData);
            lastPuttedVideoData = videoData;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    int fpsOffset;//累计帧间隔偏差
    int normalInterval = 1000 / 25;//根据帧率算出正常帧间隔

    /**
     * 因为编码器固定帧率为25帧，采集帧率慢，导致播放速度过快，采集快则反之
     * 根据采集速度来判断，是需要补帧，还是需要丢帧
     *
     * @param videoData
     * @return
     */
    private boolean checkInsertVideo(YuvData videoData) {
        if (lastPuttedVideoData == null)
            return true;
        //当前帧与上一帧间隔
        int interval = (int) (videoData.timestamp - lastPuttedVideoData.timestamp);
        //间隔与正常间隔差
        fpsOffset += interval - normalInterval;

//        Log.d(TAG,"fpsOffset:"+fpsOffset);
        boolean ret = true;
        if (fpsOffset > 0) {
            //采集慢,可能需要插帧
            while (Math.abs(fpsOffset) >= normalInterval) {
                //间隔差累计到一帧间隔，插帧
                YuvData insertVideo = new YuvData(lastPuttedVideoData.videoData,
                        lastPuttedVideoData.width, lastPuttedVideoData.height, lastPuttedVideoData.timestamp + normalInterval);
                doPutVideoData(insertVideo);
                fpsOffset -= normalInterval;
//                Log.d(TAG, "++insert_video_fpsOffset:" + fpsOffset);
            }
        } else {
            //采集快,需要丢帧
            if (Math.abs(fpsOffset) >= normalInterval) {
                //间隔差累计到一帧间隔，丢
                fpsOffset += normalInterval;
                ret = false;
//                Log.d(TAG, "--skip_video fpsOffset:" + fpsOffset);
            }
        }
        return ret;
    }

    //麦克风PCM音频数据，put到队列中，生产者模型
    public void putAudioData(AudioData audioData) {
        putPCMCount++;
        try {
            audioQueue.put(audioData);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopVideoEncode() {
        videoEncoderLoop = false;
        if (videoEncoderThread != null)
            videoEncoderThread.interrupt();
    }

    public void stopAudioEncode() {
        audioEncoderLoop = false;
        if (audioEncoderThread != null)
            audioEncoderThread.interrupt();
    }

    private static final String VCODEC_MIME = "video/avc";

    private void initMediaCodec(X264Param param) {
        int bitrate = 5 * 1024 * 1024;
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
            Log.d(TAG,"codec:"+Arrays.toString(codecCapabilities.colorFormats));
            Log.d(TAG,"video widths:"+videoCapabilities.getSupportedWidths());
            Log.d(TAG,"video heights:"+videoCapabilities.getSupportedHeights());

            Log.d(TAG,"video rate:"+videoCapabilities.getBitrateRange().toString());
            Log.d(TAG,"video fps:"+videoCapabilities.getSupportedFrameRates().toString());


            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, param.getFps());

            //p2 支持 COLOR_FormatSurface COLOR_FormatYUV420Flexible  COLOR_FormatYUV420SemiPlanar
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            Log.d(TAG,"mediaFormat:"+mediaFormat);

            mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mMediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void setBitrate(int byteRate) {
        Bundle params = new Bundle();
        params.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE,byteRate);
        mMediaCodec.setParameters(params);
    }


    public void startVideoEncode() {
        if (videoEncoderLoop) {
            throw new RuntimeException("必须先停止");
        }

        videoEncoderThread = new Thread() {
            int[] buffLength;
            int[] segment;
            byte[] encodeData;
            byte[] finalData;
            byte[] spsppsData;
            int totalLength;
            int numNals;
            YuvData videoData;

            @Override
            public void run() {
                isVideoEncoderThreadStop = false;
                refreshState();
                long start = 0;
                long end = 0;
//                Log.d(TAG,"taking video videoQueue.size():"+videoQueue.size());
//                Log.d(TAG,"taking video videoEncoderLoop:"+videoEncoderLoop);

                //视频消费者模型，不断从队列中取出视频流来进行h264编码

                ByteBuffer inputBuffer;
                ByteBuffer outputBuffer;
                while (videoQueue.size() != 0 || videoEncoderLoop || putYUVCount != takeYUVCount) {
//                        Log.d(TAG,"taking video");
                    start = System.currentTimeMillis();
                    //队列中取视频数据
                    try {
                        videoData = videoQueue.take();

                        takeYUVCount++;

                        int bufferIndex = mMediaCodec.dequeueInputBuffer(-1);
                        start = System.currentTimeMillis();
                        Log.d(TAG,"start IF bufferIndex:"+bufferIndex +" -------------------------------------");

                        if (videoData != null && bufferIndex >= 0) {
                            inputBuffer = mMediaCodec.getInputBuffer(bufferIndex);
                            inputBuffer.clear();
                            inputBuffer.put(videoData.videoData, 0, videoData.videoData.length);
                            mMediaCodec.queueInputBuffer(bufferIndex, 0,
                                    inputBuffer.position(),
                                    System.nanoTime() / 1000, 0);
                            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

                            int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 40);
                            while (outputBufferIndex >= 0) {
                                outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                                outputBuffer.position(bufferInfo.offset);
                                outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                                encodeData = new byte[outputBuffer.remaining()];
                                outputBuffer.get(encodeData);
                                //进行处理
                                //...
                                //存储sps,pps
                                if(spsppsData == null) {
                                    ByteBuffer bb = ByteBuffer.wrap(encodeData);
                                    if(bb.getInt() == 0x00000001 && bb.get() == 0x67) {
                                        spsppsData = new byte[encodeData.length];
                                        System.arraycopy(encodeData,0,spsppsData,0,encodeData.length);
                                    }
                                } else {
                                    //i帧前插入sps,pps
                                    ByteBuffer bb = ByteBuffer.wrap(encodeData);
                                    if(bb.getInt() == 0x00000001 && bb.get() == 0x65) {
                                        finalData = new byte[encodeData.length+spsppsData.length];
                                        System.arraycopy(spsppsData,0,finalData,0,spsppsData.length);
                                        System.arraycopy(encodeData,0,finalData,spsppsData.length,encodeData.length);
                                        encodeData = null;
                                    } else {
                                        finalData = encodeData;
                                    }


                                    //对YUV420P进行h264编码，返回一个数据大小，里面是编码出来的h264数据
                                    //编码后的h264数据
//                                encodeData = new byte[bytes.length];
//                                System.arraycopy(bytes, 0, encodeData, 0, encodeData.length);
                                    h264TotalSize += finalData.length;
                                    recvH264Count++;
                                    if (sMediaEncoderCallback != null) {
                                        sMediaEncoderCallback.receiveEncoderVideoData(finalData, finalData.length, segment);
                                    }
                                    //我们可以把数据在java层保存到文件中，看看我们编码的h264数据是否能播放，h264裸数据可以在VLC播放器中播放
                                    if (SAVE_FILE_FOR_TEST) {
                                        videoFileManager.writeFileData(finalData);
                                        video_LenFileManager.writeFileData((String.valueOf(finalData.length) + "\n").getBytes());
                                    }
                                    //Log.d(TAG,"encoding out size:"+encodeData.length);
                                    end = System.currentTimeMillis();
//                        Log.d(TAG, "encodeTime:" + (end - start));

                                }
                                refreshFPS();
                                System.gc();
                                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                                outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 40);
                            }
                            Log.d(TAG,"end while recvH264Count:"+recvH264Count);
                        }
                        end = System.currentTimeMillis();
                        Log.d(TAG,"end IF bufferIndex:"+bufferIndex +" time:"+(end-start)+" -------------------------------------");

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                videoFileManager.closeFile();
                video_LenFileManager.closeFile();
                isVideoEncoderThreadStop = true;
                lastFPSCheckTime = 0;
                lastPuttedVideoData = null;
                refreshState();
            }
        };
        videoEncoderLoop = true;
        videoEncoderThread.start();
    }

    public int getRecvAACCount() {
        return recvAACCount;
    }

    public void startAudioEncode() {
        if (audioEncoderLoop) {
            throw new RuntimeException("必须先停止");
        }
        audioEncoderThread = new Thread() {
            @Override
            public void run() {
                byte[] outbuffer = new byte[1024];
                int haveCopyLength = 0;
                byte[] inbuffer = new byte[audioEncodeBuffer];
                Log.e(TAG, "startAudioEncode taking audio");
                isAudioEncoderThreadStop = false;
                refreshState();

                while ((audioQueue.size() != 0 || audioEncoderLoop || recvAACCount != putPCMCount) && !Thread.interrupted()) {
                    try {
                        AudioData audio = audioQueue.take();
                        takePCMCount++;
                        //我们通过fdk-aac接口获取到了audioEncodeBuffer的数据，即每次编码多少数据为最优
                        //这里我这边的手机每次都是返回的4096即4K的数据，其实为了简单点，我们每次可以让
                        //MIC录取4K大小的数据，然后把录取的数据传递到AudioEncoder.cpp中取编码
//                        Log.e(TAG, " audio.audioData.length " + audio.audioData.length + " audioEncodeBuffer " + audioEncodeBuffer);
                        final int audioGetLength = audio.audioData.length;
                        if (haveCopyLength < audioEncodeBuffer) {
                            System.arraycopy(audio.audioData, 0, inbuffer, haveCopyLength, audioGetLength);
                            haveCopyLength += audioGetLength;
                            int remain = audioEncodeBuffer - haveCopyLength;
                            if (remain == 0) {
                                //fdk-aac编码PCM裸音频数据，返回可用长度的有效字段
                                int validLength = ffmpegjni.encoderAudioEncode(inbuffer, audioEncodeBuffer, outbuffer, outbuffer.length);
                                //Log.e("lihuzi", " validLength " + validLength);
                                final int VALID_LENGTH = validLength;
                                if (VALID_LENGTH > 0) {
                                    byte[] encodeData = new byte[VALID_LENGTH];
                                    System.arraycopy(outbuffer, 0, encodeData, 0, VALID_LENGTH);

                                    recvAACCount++;
                                    aacTotalSize += validLength;
                                    if (sMediaEncoderCallback != null) {
                                        //编码后，把数据抛给rtmp去推流
                                        sMediaEncoderCallback.receiveEncoderAudioData(encodeData, VALID_LENGTH);
                                    }
                                    //我们可以把Fdk-aac编码后的数据保存到文件中，然后用播放器听一下，音频文件是否编码正确
                                    if (SAVE_FILE_FOR_TEST) {
                                        audioFileManager.writeFileData(encodeData);
                                    }
                                }
                                haveCopyLength = 0;
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                audioFileManager.closeFile();
                isAudioEncoderThreadStop = true;
                refreshState();
            }
        };
        audioEncoderLoop = true;
        audioEncoderThread.start();
    }

//
//    private void saveFileForTest() {
//        if (SAVE_FILE_FOR_TEST) {
//            videoFileManager.closeFile();
//            audioFileManager.closeFile();
//        }
//    }



    MediaEncoder.OnMuxerListener onMuxerListener;

    public void setOnMuxerListener(MediaEncoder.OnMuxerListener onMuxerListener) {
        this.onMuxerListener = onMuxerListener;
    }

    //编码后的总大小
    private long h264TotalSize = 0L;
    private long aacTotalSize = 0L;


    public int getPutYUVCount() {
        return putYUVCount;
    }

    public int getTakeYUVCount() {
        return takeYUVCount;
    }

    public int getPutPCMCount() {
        return putPCMCount;
    }

    public int getRecvH264Count() {
        return recvH264Count;
    }

    private int putCAMCount = 0;
    private int putYUVCount = 0;
    private int takeYUVCount = 0;
    private int putPCMCount = 0;

    public int getTakePCMCount() {
        return takePCMCount;
    }

    private int takePCMCount = 0;

    private int recvH264Count = 0;
    private int recvAACCount = 0;
    private long lastFPSCheckTime = 0;
    private int checkFPSYUVStart = 0;
    private int checkFPSH264Start = 0;
    private int checkFPSPCMStart = 0;
    private int checkFPSAACStart = 0;
    private int checkFPSCAMStart = 0;


    private int camFPS = 0;//camera fps
    private int yuvFPS = 0;//yuv fps，原本和camera一致，由于camera fps变化，做了插帧处理
    private int h264FPS = 0;//编码器输出h264 fps
    private int pcmFPS = 0;//pcm fps
    private int aacFPS = 0;//编码器输出 aac fps

    private void refreshFPS() {
        if (lastFPSCheckTime == 0) {
            lastFPSCheckTime = System.currentTimeMillis();
        } else {
            long now = System.currentTimeMillis();
            double interval = now - lastFPSCheckTime;
            if (interval < 1000) {
                if (checkFPSYUVStart == 0)
                    checkFPSYUVStart = putYUVCount;
                if (checkFPSH264Start == 0)
                    checkFPSH264Start = recvH264Count;
                if (checkFPSPCMStart == 0)
                    checkFPSPCMStart = putPCMCount;
                if (checkFPSAACStart == 0)
                    checkFPSAACStart = recvAACCount;
                if (checkFPSCAMStart == 0)
                    checkFPSCAMStart = putCAMCount;
            } else {
                lastFPSCheckTime = now;
                yuvFPS = (int) ((putYUVCount - checkFPSYUVStart) * 1000f / interval);
                h264FPS = (int) ((recvH264Count - checkFPSH264Start) * 1000f / interval);
                pcmFPS = (int) ((putPCMCount - checkFPSPCMStart) * 1000f / interval);
                aacFPS = (int) ((recvAACCount - checkFPSAACStart) * 1000f / interval);
                camFPS = (int) ((putCAMCount - checkFPSCAMStart) * 1000f / interval);

                checkFPSYUVStart = 0;
                checkFPSH264Start = 0;
                checkFPSPCMStart = 0;
                checkFPSAACStart = 0;
                checkFPSCAMStart = 0;
            }
        }
    }

    public int getWaitVideoEncodedQueueSize() {
        return videoQueue.size();
    }

    public int getWaitAudioEncodedQueueSize() {
        return audioQueue.size();
    }

    public String getVideoEncodedSize() {
        return getSize(h264TotalSize);
    }

    public String getAudioEncodedSize() {
        return getSize(aacTotalSize);
    }

    private String getSize(long sizel) {
        String ret = null;
        String unit = null;
        if (sizel < 1024) {
            unit = "B";
        } else if (sizel < 1024 * 1024) {
            unit = "KB";
            sizel = sizel / 1024;
        } else if (sizel < 1024 * 1024 * 1024) {
            unit = "MB";
            sizel = sizel / 1024 / 1024;
        }
        return sizel + unit;
    }

    public int getYuvFPS() {
        return yuvFPS;
    }

    public int getH264FPS() {
        return h264FPS;
    }

    public int getPcmFPS() {
        return pcmFPS;
    }

    public int getAacFPS() {
        return aacFPS;
    }

    public int getCamFPS() {
        return camFPS;
    }

    MediaEncoder.EncoderState cacheState = new MediaEncoder.EncoderState();

    private void refreshState() {
        if (videoEncoderLoop && isVideoEncoderThreadStop) {
            cacheState.videoEncoderState = MediaEncoder.State.Starting;
        } else if (videoEncoderLoop && !isVideoEncoderThreadStop) {
            cacheState.videoEncoderState = MediaEncoder.State.Encoding;
        } else if (!videoEncoderLoop && !isVideoEncoderThreadStop) {
            cacheState.videoEncoderState = MediaEncoder.State.EncodeStoping;
        } else {
            cacheState.videoEncoderState = MediaEncoder.State.IDLE;
        }

        if (audioEncoderLoop && isAudioEncoderThreadStop) {
            cacheState.audioEncoderState = MediaEncoder.State.Starting;
        } else if (audioEncoderLoop && !isAudioEncoderThreadStop) {
            cacheState.audioEncoderState = MediaEncoder.State.Encoding;
        } else if (!audioEncoderLoop && !isAudioEncoderThreadStop) {
            cacheState.audioEncoderState = MediaEncoder.State.EncodeStoping;
        } else {
            cacheState.audioEncoderState = MediaEncoder.State.IDLE;
        }

        if (isMuxing)
            cacheState.muxEncoderState = MediaEncoder.State.Encoding;
        else
            cacheState.muxEncoderState = MediaEncoder.State.IDLE;
        MediaEncoder.EncoderState cur = getState();
        if (cur.audioEncoderState != cacheState.audioEncoderState
                || cur.videoEncoderState != cacheState.videoEncoderState
                || cur.muxEncoderState != cacheState.muxEncoderState) {
            setState(cacheState);
        }
    }

    private MediaEncoder.EncoderState ENCODER_STATE = new MediaEncoder.EncoderState();



    private synchronized void setState(MediaEncoder.EncoderState state) {
        Log.d(TAG, "setState :" + state);
        this.ENCODER_STATE.muxEncoderState = state.muxEncoderState;
        this.ENCODER_STATE.videoEncoderState = state.videoEncoderState;
        this.ENCODER_STATE.audioEncoderState = state.audioEncoderState;

        if (onEncoderChangedListener != null)
            onEncoderChangedListener.onChanged(ENCODER_STATE);
    }

    private synchronized MediaEncoder.EncoderState getState() {
        return this.ENCODER_STATE;
    }

    public void setOnEncoderChangedListener(MediaEncoder.OnEncoderChangedListener onEncoderChangedListener) {
        this.onEncoderChangedListener = onEncoderChangedListener;

    }
    public void mux(final MediaEncoder.MuxType type) {
        isMuxing = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                String outputPath;
                if (type == MediaEncoder.MuxType.mp4 || type == MediaEncoder.MuxType.mkv) {
                    outputPath = "/sdcard/264/123." + type.name();
                    ret = ffmpegjni.muxMp4(FileManager.TEST_H264_FILE,
                            FileManager.TEST_AAC_FILE, outputPath);
                } else {
                    if (onMuxerListener != null)
                        onMuxerListener.onError("not support mux type");
                    isMuxing = false;
                    return;
                }
                if (ret < 0) {
                    if (onMuxerListener != null)
                        onMuxerListener.onError("ret:" + ret);
                } else {
                    if (onMuxerListener != null)
                        onMuxerListener.onSuccess(type, outputPath);
                }
                isMuxing = false;
            }
        }).start();
    }

    MediaEncoder.OnEncoderChangedListener onEncoderChangedListener;
}
