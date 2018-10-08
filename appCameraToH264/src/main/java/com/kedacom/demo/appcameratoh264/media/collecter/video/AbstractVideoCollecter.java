package com.kedacom.demo.appcameratoh264.media.collecter.video;

import android.content.Context;
import android.graphics.ImageFormat;
import android.util.Log;

import com.kedacom.demo.appcameratoh264.media.base.YuvData;
import com.kedacom.demo.appcameratoh264.media.base.YuvFormat;
import com.kedacom.demo.appcameratoh264.media.collecter.api.ICollecterParam;
import com.kedacom.demo.appcameratoh264.media.collecter.api.IVideoCollecter;
import com.kedacom.demo.appcameratoh264.media.util.IYuvUtil;
import com.kedacom.demo.appcameratoh264.media.util.YuvUtil_libyuv;
import com.orhanobut.logger.Logger;

/**
 * Created by yuhanxun
 * 2018/9/29
 * description:
 */
public abstract class AbstractVideoCollecter implements IVideoCollecter {
    protected Context context;
    protected String TAG = getClass().getSimpleName() + "_xunxun";
    protected int displayDegree;
    private Callback callback;
    protected VideoCollecterParam collecterParam;

    protected YuvFormat expectFormat;
    protected YuvFormat realFormat;
    private PacketDataInserter packetDataInserter;

    private IYuvUtil yuvUtil = new YuvUtil_libyuv();

    public AbstractVideoCollecter(Context context) {
        this.context = context;
        //初始化yuvUtil
        //...
    }

    @Override
    public void config(ICollecterParam param) {
        Log.d(TAG, "config param:" + param);
        if (param instanceof VideoCollecterParam) {
            this.collecterParam = (VideoCollecterParam) param;
            VideoCollecterParam vParam = (VideoCollecterParam) param;
            if (vParam.isConstantFps()) {
                packetDataInserter = new PacketDataInserter(vParam.getFps());
                packetDataInserter.setInserterListener(new PacketDataInserter.InserterListener() {
                    @Override
                    public void onInsertData(VideoCollectData data) {
                        //..额外插帧
                        doCallback(data);
                        Logger.d("onInsertData:"+data.getTimestamp());
                    }

                    @Override
                    public void onNormalData(VideoCollectData data) {
                        // 正常帧
                        doCallback(data);
                    }

                    @Override
                    public void onLoseData(VideoCollectData data) {
                        //..丢帧
                        Logger.e("onLoseData:"+data.getTimestamp());
                    }
                });
            }
            expectFormat = ((VideoCollecterParam) param).getFormat();
            realFormat = getSupportFormat(expectFormat);
            if(realFormat == null)
                throw new IllegalArgumentException("Format "+ expectFormat+ " is not supported!");

            _config(vParam);


        } else {
            throw new IllegalArgumentException(param + " is not supported");
        }
    }

    protected abstract void _config(VideoCollecterParam param);
    protected abstract boolean isFormatSupported(YuvFormat yuvFormat);

    protected YuvFormat getSupportFormat(YuvFormat expectFormat) {
        YuvFormat ret = null;
        if (isFormatSupported(expectFormat)) {
            ret = expectFormat;
        } else if (isFormatSupported(YuvFormat.Yuv420p_I420)) {
            ret = YuvFormat.Yuv420p_I420;
        } else if (isFormatSupported(YuvFormat.Yuv420sp_NV21)) {
            ret = YuvFormat.Yuv420sp_NV21;
        } else if (isFormatSupported(YuvFormat.Yuv420sp_NV12)) {
            ret = YuvFormat.Yuv420sp_NV12;
        } else if (isFormatSupported(YuvFormat.Yuv420p_YV12)) {
            ret = YuvFormat.Yuv420p_YV12;
        }
        return ret;
    }
    protected void notifyRTFrame(byte[] data, int length, int w, int h, YuvFormat format) {
        //wrap to YuvData
        YuvData yuvData = new YuvData(data, length, w, h, format);
        //wrap to VideoCollectData
        VideoCollectData ret = new VideoCollectData(yuvData, System.currentTimeMillis());
        if (packetDataInserter != null) {
            packetDataInserter.handleData(ret);
        } else {
            doCallback(ret);
        }
    }

    private void doCallback(VideoCollectData ret) {
        if (expectFormat != realFormat || displayDegree != 0) {
            //转格式或旋转
            ret.setYuvData(yuvUtil.convertFormat(ret.getYuvData(), expectFormat, displayDegree));
        }
        if (callback != null)
            callback.onCollectData(ret);
    }

    protected byte[] getCallbackBuffer(VideoCollecterParam param) {
        byte[] ret;
        switch (expectFormat) {
            case Yuv420p_YV12:
            case Yuv420p_I420:
            case Yuv420sp_NV12:
            case Yuv420sp_NV21:
                ret = new byte[param.getWidth() * param.getHeight() * 3 >> 1];
                break;
            default:
                throw new IllegalArgumentException("Format " + expectFormat + " not support!");
        }
        return ret;
    }

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

}
