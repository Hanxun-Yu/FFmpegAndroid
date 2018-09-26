package com.example.apph264render.mediacodec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import com.example.apph264render.api.IMediaCodec;
import com.example.apph264render.jni.YuvPlayerJni;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * @author zed
 * @date 2017/11/22 上午10:38
 * @desc
 */

public class MediaCodecDecoderYuv implements IMediaCodec {

    private final String TAG = MediaCodecDecoderYuv.class.getSimpleName() + "_xunxun";

    //设置解码分辨率
    private int VIDEO_WIDTH = 1280;//2592
    private int VIDEO_HEIGHT = 720;//1520

    //解码帧率 1s解码30帧
    private final int FRAME_RATE = 25;

    //支持格式
    private final String VIDEOFORMAT_H264 = "video/avc";
    private final String VIDEOFORMAT_MPEG4 = "video/mp4v-es";
    private final String VIDEOFORMAT_HEVC = "video/hevc";

    //默认格式
    private String mMimeType = VIDEOFORMAT_H264;

    //接收的视频帧队列
    private volatile ArrayList<DataInfo> mFrmList = new ArrayList<>();

    //解码支持监听器
    private OnSupportListener mSupportListener;
    //解码结果监听
    private OnDecodeListener mOnDecodeListener;

    private MediaCodec mMediaCodec;
    private DecodeThread mDecodeThread;



    private boolean isStop = true;

    @Override
    public void init() {
        Log.i(TAG, "init");
        stopDecoderThread();
        releaseCodec();

        try {
            //通过多媒体格式名创建一个可用的解码器
            mMediaCodec = MediaCodec.createDecoderByType(mMimeType);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Init Exception " + e.getMessage());
        }
        mFrmList.clear();
        initMediaFormat();
        yuvPlayerJni.init();
    }

    YuvPlayerJni yuvPlayerJni = new YuvPlayerJni();
    Surface obj;

    @Override
    public void setRenderView(Object obj) {
        if (isStop) {
            //crypto:数据加密 flags:编码器/编码器
//            mMediaCodec.configure(mediaformat, (Surface) obj, null, 0);
            mMediaCodec.configure(mediaformat, null, null, 0);
            mMediaCodec.start();
            startDecoderThread();
            isStop = false;
            yuvPlayerJni.setRender(obj);
            yuvPlayerJni.start();
        } else {
            mMediaCodec.setOutputSurface((Surface) obj);
        }
    }


    private void stopDecoderThread() {
        Log.e(TAG, "stopDecoderThread");

        if (mDecodeThread != null) {
            mDecodeThread.stopThread();
            try {
                mDecodeThread.join(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mDecodeThread = null;
        }
    }

    private void startDecoderThread() {
        Log.d(TAG, "startDecoderThread");
        mDecodeThread = new DecodeThread();
        mDecodeThread.start();
    }

    MediaFormat mediaformat;

    private void initMediaFormat() {
        //初始化解码器格式
        mediaformat = MediaFormat.createVideoFormat(mMimeType, VIDEO_WIDTH, VIDEO_HEIGHT);
        //设置帧率
        mediaformat.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        mediaformat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
    }

    public boolean isStop() {
        return isStop;
    }

    private void releaseCodec() {
        try {
            if (mMediaCodec != null) {
                mMediaCodec.stop();
                mMediaCodec.release();
                mMediaCodec = null;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }


    public void setOnSupportListener(OnSupportListener listener) {
        mSupportListener = listener;
    }

    public void setOnDecodeListener(OnDecodeListener listener) {
        mOnDecodeListener = listener;
    }


    @Override
    public void release() {
        Log.e(TAG, "unInit");
        isStop = true;
        stopDecoderThread();
        releaseCodec();
        mFrmList.clear();
    }

    boolean hasIFrame = false;


    @Override
    public void putEncodeData(byte[] frames, int size) {
        if (!isStop) {

            if (frames[0] == 0
                    && frames[1] == 0
                    && frames[2] == 0
                    && frames[3] == 1
                    && frames[4] == 0x67
                    ) {
                byte[] sps = new byte[frames.length - 4];
                System.arraycopy(frames, 4, sps, 0, frames.length - 4);
                int wh[] = new int[2];
                yuvPlayerJni.getSPSWH(sps,wh);
                int width = wh[0];
                int height = wh[1];
                if (width != VIDEO_WIDTH || height != VIDEO_HEIGHT) {
                    Log.e(TAG, "MediaCodec size changed width:" + width + " height:" + height);
                    VIDEO_WIDTH = width;
                    VIDEO_HEIGHT = height;
                    Bundle bundle = new Bundle();
                    bundle.putInt(MediaFormat.KEY_WIDTH, VIDEO_WIDTH);
                    bundle.putInt(MediaFormat.KEY_HEIGHT, VIDEO_HEIGHT);
                    mMediaCodec.setParameters(bundle);
                }
                hasIFrame = true;
            }

            if (!hasIFrame) {
                return;
            }
//            Log.d(TAG, "putEncodeData data len:" + frames.length);
            DataInfo dataInfo = new DataInfo();
            dataInfo.mDataBytes = frames;
            dataInfo.receivedDataTime = System.currentTimeMillis();
            mFrmList.add(dataInfo);
        }
    }

    /**
     * @author zed
     * @description 解码线程
     * @time 2017/11/22
     */
    private class DecodeThread extends Thread {

        private boolean isRunning = true;

        public synchronized void stopThread() {
            isRunning = false;
        }

        public boolean isRunning() {
            return isRunning;
        }

        @Override
        public void run() {

            Log.i(TAG, "===start DecodeThread===");

            //存放目标文件的数据
            ByteBuffer byteBuffer = null;
            //解码后的数据，包含每一个buffer的元数据信息，例如偏差，在相关解码器中有效的数据大小
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            long startMs = System.currentTimeMillis();
            DataInfo dataInfo = null;
            while (isRunning) {

                if (mFrmList.isEmpty()) {

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                dataInfo = mFrmList.remove(0);

                long startDecodeTime = System.currentTimeMillis();

                //1 准备填充器
                int inIndex = -1;

                if (dataInfo == null) {
                    Log.e(TAG, "DecodeThread:dataInfo null");
                    continue;
                }
                try {
                    inIndex = mMediaCodec.dequeueInputBuffer(dataInfo.receivedDataTime);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    Log.e(TAG, "IllegalStateException dequeueInputBuffer ");
                    if (mSupportListener != null) {
                        mSupportListener.UnSupport();
                    }
                }

                if (inIndex >= 0) {
                    //2 准备填充数据
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        byteBuffer = mMediaCodec.getInputBuffers()[inIndex];
                        byteBuffer.clear();
                    } else {
                        byteBuffer = mMediaCodec.getInputBuffer(inIndex);
                    }

                    if (byteBuffer == null) {
                        continue;
                    }

                    byteBuffer.put(dataInfo.mDataBytes, 0, dataInfo.mDataBytes.length);
                    //3 把数据传给解码器
                    mMediaCodec.queueInputBuffer(inIndex, 0, dataInfo.mDataBytes.length, 0, 0);

                } else {
                    SystemClock.sleep(5);
                    continue;
                }

                //这里可以根据实际情况调整解码速度
//                long sleep = 40;
//
//                if (mFrmList.size() > 20) {
//                    sleep = 0;
//                }
//
//                SystemClock.sleep(sleep);


//                int outIndex = MediaCodec.INFO_TRY_AGAIN_LATER;

                //4 开始解码
                try {
                    int outIndex = mMediaCodec.dequeueOutputBuffer(info, info.presentationTimeUs);

                    //循环解码，直到数据全部解码完成
                    while (outIndex >= 0) {
                        Log.d(TAG, "outIndex:" + outIndex);
                        Log.d(TAG, "info:" + info.offset);
                        Log.d(TAG, "info:" + info.size);
                        Log.d(TAG, "info:" + info.presentationTimeUs);


                        //logger.d("outputBufferIndex = " + outputBufferIndex);
                        //true : 将解码的数据显示到surface上
                        ByteBuffer outBuffer = mMediaCodec.getOutputBuffer(outIndex);
                        outBuffer.position(info.offset);
                        outBuffer.limit(info.offset + info.size);
                        byte[] encodeData = new byte[outBuffer.remaining()];
                        outBuffer.get(encodeData);


                        yuvPlayerJni.putYuv(encodeData, VIDEO_WIDTH, VIDEO_HEIGHT, encodeData.length);
                        if (mOnDecodeListener != null) {
                            mOnDecodeListener.decodeResult(encodeData, encodeData.length, VIDEO_WIDTH, VIDEO_HEIGHT);
                        }

                        mMediaCodec.releaseOutputBuffer(outIndex, info.presentationTimeUs);
                        outIndex = mMediaCodec.dequeueOutputBuffer(info, 0);


                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    Log.e(TAG, "IllegalStateException dequeueOutputBuffer " + e.getMessage());
                }

//                if (outIndex >= 0) {
//
//                    //帧控制
////                    while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
////                        try {
////                            Thread.sleep(100);
////                        } catch (InterruptedException e) {
////                            e.printStackTrace();
////                        }
////                    }
////                    boolean doRender = (info.size != 0);
////
////                    //对outputbuffer的处理完后，调用这个函数把buffer重新返回给codec类。
////                    //调用这个api之后，SurfaceView才有图像
////                    mMediaCodec.releaseOutputBuffer(outIndex, doRender);
//                    mMediaCodec.releaseOutputBuffer(outIndex, info.presentationTimeUs);
//
//                    if (mOnDecodeListener != null) {
//                        mOnDecodeListener.decodeResult(mVideoWidth, mVideoHeight);
//                    }
//
////                    Log.i(TAG, "DecodeThread delay = " + (System.currentTimeMillis() - dataInfo.receivedDataTime) + " spent = " + (System.currentTimeMillis() - startDecodeTime) + " size = " + mFrmList.size());
//                    System.gc();
//
//                } else {
//                    switch (outIndex) {
//                        case MediaCodec.INFO_TRY_AGAIN_LATER: {
//
//                        }
//                        break;
//                        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED: {
//                            MediaFormat newFormat = mMediaCodec.getOutputFormat();
//                            mVideoWidth = newFormat.getInteger("width");
//                            mVideoHeight = newFormat.getInteger("height");
//
//                            //是否支持当前分辨率
//                            String support = MediaCodecUtils.getSupportMax(mMimeType);
//                            if (support != null) {
//                                String width = support.substring(0, support.indexOf("x"));
//                                String height = support.substring(support.indexOf("x") + 1, support.length());
//                                Log.i(TAG, " current " + mVideoWidth + "x" + mVideoHeight + " mMimeType " + mMimeType);
//                                Log.i(TAG, " Max " + width + "x" + height + " mMimeType " + mMimeType);
//                                if (Integer.parseInt(width) < mVideoWidth || Integer.parseInt(height) < mVideoHeight) {
//                                    if (mSupportListener != null) {
//                                        mSupportListener.UnSupport();
//                                    }
//                                }
//                            }
//                        }
//                        break;
//                        case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED: {
//
//                        }
//                        break;
//                        default: {
//
//                        }
//                    }
//                }
            }

            Log.i(TAG, "===stop DecodeThread===");
        }

    }

    /**
     * @author zed
     * @date 2017/12/12 下午2:08
     * @desc
     */

    public class DataInfo {
        public byte[] mDataBytes;
        public long receivedDataTime;
    }
}
