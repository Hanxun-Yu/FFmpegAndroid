package com.kedacom.demo.appcameratoh264.media.video;

import android.util.Log;

import com.kedacom.demo.appcameratoh264.media.api.EncodedData;
import com.kedacom.demo.appcameratoh264.media.api.IEncoderParam;
import com.kedacom.demo.appcameratoh264.media.api.IMediaEncoder;
import com.kedacom.demo.appcameratoh264.media.api.PacketData;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by yuhanxun
 * 2018/9/20
 * description:
 */
public abstract class AbstractEncoder implements IMediaEncoder {
    private final String TAG = getClass().getSimpleName()+"_xunxun";
    private Thread encoderThread;
    private EncoderRunn encoderRunn;
    //编码线程是否已经停止
    private boolean isThreadLoopStoped = true;
    //是否允许编码线程循环
    private boolean allowThreadLoop = false;

    //采集流队列
    private LinkedBlockingQueue<PacketData> queue;

    private Callback callback;
    private OnStateChangedListener onStateChangedListener;
    private State curState =State.IDLE;

    private long putQueueDataCount = 0;
    private long takeQueueDataCount = 0;
    @Override
    public void init() {
        queue = new LinkedBlockingQueue<>();
        refreshState();
    }

    @Override
    public void putPacket(PacketData packetData) {
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
        if(curState == State.IDLE) {
            encoderThread = new Thread(encoderRunn = new EncoderRunn());
            allowThreadLoop = true;
            encoderThread.start();
        } else {
            Log.d(TAG,"Encoder busy");
        }
    }

    @Override
    public void stop() {
        allowThreadLoop = false;
        if(encoderThread != null)
            encoderThread.interrupt();
    }

    @Override
    public void changeBitrate(int byterate) {

    }


    @Override
    public long getLengthEncoded() {
        return 0;
    }

    @Override
    public int getRTBitrate() {
        return 0;
    }

    @Override
    public int getRTInputRate() {
        return 0;
    }

    @Override
    public int getRTOutputRate() {
        return 0;
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
        } else if(allowThreadLoop && !isThreadLoopStoped) {
            setState(State.Encoding);
        } else if(!allowThreadLoop && !isThreadLoopStoped) {
            setState(State.EncodeStoping);
        } else {
            setState(State.IDLE);
        }
    }

    private void setState(State state) {
        if(state != this.curState) {
            this.curState = state;
            if (onStateChangedListener != null)
                onStateChangedListener.onState(state);
        }
    }

    private class EncoderRunn implements Runnable {
        @Override
        public void run() {
            isThreadLoopStoped = false;
            refreshState();
            PacketData queueData;
            EncodedData encodedData;
            while(queue.size() != 0 || allowThreadLoop
                    || putQueueDataCount != takeQueueDataCount) {
                try {
                    queueData = queue.take();
                    takeQueueDataCount++;
                    encodedData = getEncodedData(queueData);

                    if(callback != null)
                        callback.onDataEncoded(encodedData);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected abstract EncodedData getEncodedData(PacketData t);

}
