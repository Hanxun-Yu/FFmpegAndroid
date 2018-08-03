package com.kedacom.demo.appcameratoh264.media.video;


import android.util.Log;

import com.kedacom.demo.appcameratoh264.jni.FFmpegjni;
import com.kedacom.demo.appcameratoh264.media.FileManager;
import com.kedacom.demo.appcameratoh264.media.audio.AudioData;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * 编码类MediaEncoder，主要是把视频流YUV420P格式编码为h264格式,把PCM裸音频转化为AAC格式
 */
public class MediaEncoder {
    private static final String TAG = "MediaEncoder_xunxun";

    private Thread videoEncoderThread, audioEncoderThread;
    private boolean videoEncoderLoop, audioEncoderLoop;

    //视频流队列
    private LinkedBlockingQueue<VideoData420> videoQueue;
    //音频流队列
    private LinkedBlockingQueue<AudioData> audioQueue;

    private FileManager videoFileManager;
    private FileManager audioFileManager;
    private static final boolean SAVE_FILE_FOR_TEST = true;
    private int fps = 0;
    private int audioEncodeBuffer;

    private MediaEncoderCallback sMediaEncoderCallback;

    FFmpegjni ffmpegjni = new FFmpegjni();

    public interface MediaEncoderCallback {
        /**
         * @param videoData   总字节
         * @param totalLength 总长度
         * @param segment     每个nalu的大小
         */
        void receiveEncoderVideoData(byte[] videoData, int totalLength, int[] segment);

        void receiveEncoderAudioData(byte[] audioData, int size);
    }

    public void setsMediaEncoderCallback(MediaEncoderCallback callback) {
        sMediaEncoderCallback = callback;
    }

    public MediaEncoder() {
        if (SAVE_FILE_FOR_TEST) {
            videoFileManager = new FileManager(FileManager.TEST_H264_FILE);
            audioFileManager = new FileManager(FileManager.TEST_AAC_FILE);
        }
        videoQueue = new LinkedBlockingQueue<>();
        audioQueue = new LinkedBlockingQueue<>();
        //这里我们初始化音频数据，为什么要初始化音频数据呢？音频数据里面我们做了什么事情？
//        audioEncodeBuffer = StreamProcessManager.encoderAudioInit(Contacts.SAMPLE_RATE,
//                Contacts.CHANNELS, Contacts.BIT_RATE);
    }

    int bitrate_kbps = 0;

    public void setMediaSize(int widthIn, int heightIn, int widthOut, int heightOut, int bitrate_kbps) {
        ffmpegjni.encoderVideoinit(widthIn, heightIn, widthOut, heightOut, bitrate_kbps);
        this.bitrate_kbps = bitrate_kbps;
    }

    public void start() {
//        startAudioEncode();
        startVideoEncode();
    }

    public void stop() {
        stopAudioEncode();
        stopVideoEncode();
        saveFileForTest();
    }

    private long firstInputTime = 0;

    //摄像头的YUV420P数据，put到队列中，生产者模型
    VideoData420 lastPuttedVideoData;

    public void putVideoData(VideoData420 videoData) {
        if (firstInputTime == 0)
            firstInputTime = System.currentTimeMillis();
        putYUVCount++;

        if(checkInsertVideo(videoData))
            doPutVideoData(videoData);
    }

    private void doPutVideoData(VideoData420 videoData) {
        try {
            videoQueue.put(videoData);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lastPuttedVideoData = videoData;
    }

    int fpsOffset;//累计帧间隔偏差
    int normalInterval = 1000 / 25;//根据帧率算出正常帧间隔

    /**
     * 因为编码器固定帧率为25帧，采集帧率慢，导致播放速度过快，采集快则反之
     * 根据采集速度来判断，是需要补帧，还是需要丢帧
     * @param videoData
     * @return
     */
    private boolean checkInsertVideo(VideoData420 videoData) {
        if(lastPuttedVideoData == null)
            return true;
        //当前帧与上一帧间隔
        int interval = (int) (videoData.timestamp - lastPuttedVideoData.timestamp);
        //间隔与正常间隔差
        int diff = interval - normalInterval;
        fpsOffset += diff;


        boolean ret = true;
        if (fpsOffset > 0) {
            //采集慢,可能需要插帧
            while (Math.abs(fpsOffset) >= normalInterval) {
                //间隔差累计到一帧间隔，插帧
                VideoData420 insertVideo = new VideoData420(lastPuttedVideoData.videoData,
                        lastPuttedVideoData.width, lastPuttedVideoData.height, lastPuttedVideoData.timestamp + normalInterval);
                doPutVideoData(insertVideo);
                fpsOffset -= normalInterval;
            }
        } else {
            //采集快,需要丢帧
            if (Math.abs(fpsOffset) >= normalInterval) {
                //间隔差累计到一帧间隔，丢
                fpsOffset += normalInterval;
                ret = false;
            }
        }

        return ret;
    }

    //麦克风PCM音频数据，put到队列中，生产者模型
    public void putAudioData(AudioData audioData) {
        try {
            audioQueue.put(audioData);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopVideoEncode() {
        videoEncoderLoop = false;
        videoEncoderThread.interrupt();
    }

    public void stopAudioEncode() {
        audioEncoderLoop = false;
        if (audioEncoderThread != null)
            audioEncoderThread.interrupt();
    }

    public void startVideoEncode() {
        if (videoEncoderLoop) {
            throw new RuntimeException("必须先停止");
        }

        videoEncoderThread = new Thread() {
            byte[] outbuffer;
            int[] buffLength;
            int[] segment;
            byte[] encodeData;
            int totalLength;
            int numNals;
            VideoData420 videoData;

            long lastFrameTimestamp = 0;

            @Override
            public void run() {
                //视频消费者模型，不断从队列中取出视频流来进行h264编码
                while (videoEncoderLoop && !Thread.interrupted()) {
                    try {
                        //队列中取视频数据
                        videoData = videoQueue.take();
//                        byte[] outbuffer = new byte[videoData.width * videoData.height];
                        outbuffer = new byte[videoData.videoData.length];
                        buffLength = new int[20];

                        //计算pts
//                        if (lastFrameTimestamp == 0) {
//                            fps++;
//                        } else {
//                            long interval = videoData.timestamp - lastFrameTimestamp;
////                            int fps_unit = (int) ((interval / 1000d) * 25);
//                            int fps_unit = (int) interval;
//                            fps += fps_unit;
//                            Log.d(TAG, "interval:" + interval + " fps_increment:" + fps_unit + " pts:" + fps);
//                        }
//                        lastFrameTimestamp = videoData.timestamp;

                        fps++;

                        //对YUV420P进行h264编码，返回一个数据大小，里面是编码出来的h264数据
                        numNals = ffmpegjni.encoderVideoEncode(videoData.videoData, videoData.videoData.length, fps, outbuffer, buffLength);
                        //Log.e("RiemannLee", "data.length " +  videoData.videoData.length + " h264 encode length " + buffLength[0]);
                        if (numNals > 0) {
                            segment = new int[numNals];
                            System.arraycopy(buffLength, 0, segment, 0, numNals);
                            totalLength = 0;
                            for (int i = 0; i < segment.length; i++) {
                                totalLength += segment[i];
                            }
                            //Log.i("RiemannLee", "###############totalLength " + totalLength);
                            //编码后的h264数据
                            encodeData = new byte[totalLength];
                            System.arraycopy(outbuffer, 0, encodeData, 0, encodeData.length);
                            h264TotalSize += encodeData.length;
                            recvH264Count++;
                            if (sMediaEncoderCallback != null) {
                                sMediaEncoderCallback.receiveEncoderVideoData(encodeData, encodeData.length, segment);
                            }
                            //我们可以把数据在java层保存到文件中，看看我们编码的h264数据是否能播放，h264裸数据可以在VLC播放器中播放
                            if (SAVE_FILE_FOR_TEST) {
                                videoFileManager.saveFileData(encodeData);
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                    refreshFPS();
                }

            }
        };
        videoEncoderLoop = true;
        videoEncoderThread.start();
    }

//    public void startAudioEncode() {
//        if (audioEncoderLoop) {
//            throw new RuntimeException("必须先停止");
//        }
//        audioEncoderThread = new Thread() {
//            @Override
//            public void run() {
//                byte[] outbuffer = new byte[1024];
//                int haveCopyLength = 0;
//                byte[] inbuffer = new byte[audioEncodeBuffer];
//                while (audioEncoderLoop && !Thread.interrupted()) {
//                    try {
//                        AudioData audio = audioQueue.take();
//                        //我们通过fdk-aac接口获取到了audioEncodeBuffer的数据，即每次编码多少数据为最优
//                        //这里我这边的手机每次都是返回的4096即4K的数据，其实为了简单点，我们每次可以让
//                        //MIC录取4K大小的数据，然后把录取的数据传递到AudioEncoder.cpp中取编码
//                        //Log.e("RiemannLee", " audio.audioData.length " + audio.audioData.length + " audioEncodeBuffer " + audioEncodeBuffer);
//                        final int audioGetLength = audio.audioData.length;
//                        if (haveCopyLength < audioEncodeBuffer) {
//                            System.arraycopy(audio.audioData, 0, inbuffer, haveCopyLength, audioGetLength);
//                            haveCopyLength += audioGetLength;
//                            int remain = audioEncodeBuffer - haveCopyLength;
//                            if (remain == 0) {
//                                //fdk-aac编码PCM裸音频数据，返回可用长度的有效字段
//                                int validLength = StreamProcessManager.encoderAudioEncode(inbuffer, audioEncodeBuffer, outbuffer, outbuffer.length);
//                                //Log.e("lihuzi", " validLength " + validLength);
//                                final int VALID_LENGTH = validLength;
//                                if (VALID_LENGTH > 0) {
//                                    byte[] encodeData = new byte[VALID_LENGTH];
//                                    System.arraycopy(outbuffer, 0, encodeData, 0, VALID_LENGTH);
//                                    if (sMediaEncoderCallback != null) {
//                                        //编码后，把数据抛给rtmp去推流
//                                        sMediaEncoderCallback.receiveEncoderAudioData(encodeData, VALID_LENGTH);
//                                    }
//                                    //我们可以把Fdk-aac编码后的数据保存到文件中，然后用播放器听一下，音频文件是否编码正确
//                                    if (SAVE_FILE_FOR_TEST) {
//                                        audioFileManager.saveFileData(encodeData);
//                                    }
//                                }
//                                haveCopyLength = 0;
//                            }
//                        }
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                        break;
//                    }
//                }
//
//            }
//        };
//        audioEncoderLoop = true;
//        audioEncoderThread.start();
//    }

    private void saveFileForTest() {
        if (SAVE_FILE_FOR_TEST) {
            videoFileManager.closeFile();
            audioFileManager.closeFile();
        }
    }

    private long h264TotalSize = 0L;

    public int getPutYUVCount() {
        return putYUVCount;
    }

    public int getRecvH264Count() {
        return recvH264Count;
    }

    private int putYUVCount = 0;
    private int recvH264Count = 0;

    private long lastFPSCheckTime = 0;
    private int checkFPSYUVStart = 0;
    private int checkFPSH264Start = 0;


    private int yuvFPS = 0;
    private int h264FPS = 0;

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
            } else {
                lastFPSCheckTime = now;
                yuvFPS = (int) ((putYUVCount - checkFPSYUVStart) * 1000f / interval);
                h264FPS = (int) ((recvH264Count - checkFPSH264Start) * 1000f / interval);
                checkFPSYUVStart = 0;
                checkFPSH264Start = 0;
            }
        }
    }

    public String getEncodedSize() {
        return getSize(h264TotalSize);
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

}
