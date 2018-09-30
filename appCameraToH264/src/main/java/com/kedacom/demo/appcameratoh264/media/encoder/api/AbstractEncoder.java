package com.kedacom.demo.appcameratoh264.media.encoder.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by yuhanxun
 * 2018/9/20
 * description:
 */
public abstract class AbstractEncoder implements IMediaEncoder {
    protected final String TAG = getClass().getSimpleName() + "_xunxun";
    private Thread encoderThread;
    private EncoderRunn encoderRunn;
    private CheckStatusRunn checkStatusRunn;
    //编码线程是否已经停止
    private boolean isThreadLoopStoped = true;
    //是否允许编码线程循环
    private boolean allowThreadLoop = false;

    //采集流队列
    private LinkedBlockingQueue<IPacketData> queue;

    private Callback callback;
    private OnStateChangedListener onStateChangedListener;
    private State curState = State.IDLE;

    private int putQueueDataCount = 0;
    private int takeQueueDataCount = 0;
    private int totalEncodedLength = 0;

    private int inputFps = 0;
    private int outputFps = 0;
    private int rtBitrate = 0;

    private Handler mainLoop = new Handler(Looper.getMainLooper());

    @Override
    public void init() {
        queue = new LinkedBlockingQueue<>();
        refreshState();
    }

    @Override
    public void putPacket(IPacketData packetData) {
        try {
            queue.put(packetData);
            putQueueDataCount++;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public abstract void config(IEncoderParam param);

    @Override
    public void start() {
        if (curState == State.IDLE) {
            encoderThread = new Thread(encoderRunn = new EncoderRunn(), getClass().getSimpleName() + "_encodeThread");
            allowThreadLoop = true;
            encoderThread.start();
            new Thread(checkStatusRunn = new CheckStatusRunn(), getClass().getSimpleName() + "_statusThread").start();
        } else {
            Log.d(TAG, "Encoder busy");
            throw new IllegalStateException("Encoder now state :" + curState + " ,please wait for it enter IDEL state!");
        }
    }

    @Override
    public void stop() {
        allowThreadLoop = false;
        if (encoderThread != null)
            encoderThread.interrupt();


    }

    @Override
    public void release() {
        if (checkStatusRunn != null)
            checkStatusRunn.setStop(true);
    }

    @Override
    public State getState() {
        return curState;
    }

    @Override
    public abstract  void changeBitrate(int byterate);

    protected abstract IFrameData getEncodedData(IPacketData t);


    @Override
    public long getLengthEncoded() {
        return totalEncodedLength;
    }

    @Override
    public int getRTBitrate() {
        return rtBitrate;
    }

    @Override
    public int getRTInputRate() {
        return inputFps;
    }

    @Override
    public int getRTOutputRate() {
        return outputFps;
    }

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void setOnStateChangedListener(OnStateChangedListener onStateChangedListener) {
        this.onStateChangedListener = onStateChangedListener;
    }

    private void refreshState() {
        if (allowThreadLoop && isThreadLoopStoped) {
            setState(State.Starting);
        } else if (allowThreadLoop && !isThreadLoopStoped) {
            setState(State.Encoding);
        } else if (!allowThreadLoop && !isThreadLoopStoped) {
            setState(State.EncodeStoping);
        } else {
            setState(State.IDLE);
        }
    }

    private void setState(final State state) {
        if (state != this.curState) {
            this.curState = state;
            if (onStateChangedListener != null) {
                mainLoop.post(new Runnable() {
                    @Override
                    public void run() {
                        onStateChangedListener.onState(AbstractEncoder.this,state);
                    }
                });
            }
        }
    }

    @Override
    public int getQueueSize() {
        return queue.size();
    }

    private class EncoderRunn implements Runnable {
        @Override
        public void run() {
            isThreadLoopStoped = false;
            refreshState();
            IPacketData queueData;
            IFrameData encodedData;
            while (queue.size() != 0 || allowThreadLoop
                    || putQueueDataCount != takeQueueDataCount) {
                try {
                    queueData = queue.take();
                    takeQueueDataCount++;
                    encodedData = getEncodedData(queueData);

                    //音频需要囤积解码可能给null
                    if(encodedData != null) {
                        totalEncodedLength += encodedData.getLength();
                        if (callback != null)
                            callback.onDataEncoded(encodedData);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            isThreadLoopStoped = true;
        }
    }

    private class CheckStatusRunn implements Runnable {
        long lastCheckTime;
        long nowTime;
        int interval;
        boolean stop = false;

        private int putCount = 0;
        private int takeCount = 0;
        private int totalLength = 0;

        @Override
        public void run() {
            try {
                while (!stop) {
                    if (lastCheckTime == 0) {
                        lastCheckTime = System.currentTimeMillis();
                        putCount = putQueueDataCount;
                        takeCount = takeQueueDataCount;
                        totalLength = totalEncodedLength;
                    } else {
                        nowTime = System.currentTimeMillis();
                        interval = (int) (nowTime - lastCheckTime);
                        if (interval >= 1000) {
                            lastCheckTime = nowTime;
                            inputFps = (int) ((putQueueDataCount - putCount) * 1000f / interval);
                            outputFps = (int) ((takeQueueDataCount - takeCount) * 1000f / interval);
                            rtBitrate = (int) ((totalEncodedLength - totalLength) * 1000f / interval);
                            putCount = putQueueDataCount;
                            takeCount = takeQueueDataCount;
                            totalLength = totalEncodedLength;
                        }
                    }
                    refreshState();
                    Thread.sleep(100);
                }
                lastCheckTime = 0;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void setStop(boolean stop) {
            this.stop = stop;
        }
    }
}
