package com.kedacom.demo.appcameratoh264.media.gather.video;

import android.content.Context;
import android.graphics.ImageFormat;
import android.util.Log;

import com.kedacom.demo.appcameratoh264.media.base.YuvData;
import com.kedacom.demo.appcameratoh264.media.base.YuvFormat;
import com.kedacom.demo.appcameratoh264.media.encoder.video.VideoPacketData;
import com.kedacom.demo.appcameratoh264.media.gather.api.IGatherData;
import com.kedacom.demo.appcameratoh264.media.gather.api.IGatherParam;
import com.kedacom.demo.appcameratoh264.media.gather.api.IVideoGather;
import com.kedacom.demo.appcameratoh264.media.gather.video.PacketDataInserter;
import com.kedacom.demo.appcameratoh264.media.gather.video.VideoGatherData;
import com.kedacom.demo.appcameratoh264.media.gather.video.VideoGatherParam;
import com.kedacom.demo.appcameratoh264.media.util.IYuvUtil;
import com.kedacom.demo.appcameratoh264.media.util.YuvUtil_libyuv;

/**
 * Created by yuhanxun
 * 2018/9/29
 * description:
 */
public abstract class AbstractVideoGather implements IVideoGather {
    protected Context context;
    protected String TAG = getClass().getSimpleName()+"_xunxun";
    protected int displayDegree;
    private Callback callback;

    protected YuvFormat expectFormat;
    protected YuvFormat realFormat;
    private PacketDataInserter packetDataInserter;

    private IYuvUtil yuvUtil = new YuvUtil_libyuv();

    public AbstractVideoGather(Context context) {
        this.context = context;
        //初始化yuvUtil
        //...
    }

    @Override
    public void config(IGatherParam param) {
        Log.d(TAG,"config param:"+param);
        if (param instanceof VideoGatherParam) {
            VideoGatherParam vParam = (VideoGatherParam) param;
            if (vParam.isConstantFps()) {
                packetDataInserter = new PacketDataInserter(vParam.getFps());
                packetDataInserter.setInserterListener(new PacketDataInserter.InserterListener() {
                    @Override
                    public void onInsertData(VideoGatherData data) {
                        //..额外插帧
                        if (callback != null)
                            callback.onGatherData(data);
                    }

                    @Override
                    public void onNormalData(VideoGatherData data) {
                        // 正常帧
                        if (callback != null)
                            callback.onGatherData(data);
                    }

                    @Override
                    public void onLoseData(VideoGatherData data) {
                        //..丢帧
                    }
                });
            }
            _config(vParam);
        } else {
            throw new IllegalArgumentException(param + " is not supported");
        }
    }

    protected abstract void _config(VideoGatherParam param);

    protected void notifyRTFrame(byte[] data, int length, int w, int h, YuvFormat format) {
        //wrap to YuvData
        YuvData yuvData = new YuvData(data, length, w,
                h, format);


        if (expectFormat != realFormat || displayDegree != 0) {
            //转格式或旋转
            yuvData = yuvUtil.convertFormat(yuvData, expectFormat, displayDegree);
        }
        VideoGatherData ret = new VideoGatherData(yuvData,System.currentTimeMillis());


        if (packetDataInserter != null) {
            packetDataInserter.handleData(ret);
        } else {
            if (callback != null)
                callback.onGatherData(ret);
        }
    }

    protected int getFormat(VideoGatherParam param) {
        expectFormat = param.getFormat();
        int ret;
        switch (expectFormat) {
            case Yuv420p_YV12:
                realFormat = YuvFormat.Yuv420p_YV12;
                ret = ImageFormat.YV12;
                break;
            case Yuv420p_I420:
            case Yuv420sp_NV12:
            case Yuv420sp_NV21:
                realFormat = YuvFormat.Yuv420sp_NV21;
                ret = ImageFormat.NV21;
                break;

            default:
                throw new IllegalArgumentException("Format " + expectFormat + " not support!");
        }
        return ret;
    }

    protected byte[] getCallbackBuffer(VideoGatherParam param) {
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
