package com.kedacom.demo.appcameratoh264.media.gather.api;

import android.content.Context;

import com.kedacom.demo.appcameratoh264.media.YuvFormat;
import com.kedacom.demo.appcameratoh264.media.gather.video.PacketDataInserter;
import com.kedacom.demo.appcameratoh264.media.gather.video.VideoGatherData;
import com.kedacom.demo.appcameratoh264.media.util.IYuvUtil;
import com.kedacom.demo.appcameratoh264.media.util.YuvData;

/**
 * Created by yuhanxun
 * 2018/9/29
 * description:
 */
public abstract class AbstractVideoGather implements IVideoGather {
    protected Context context;
    String TAG = "CameraGather_xunxun";
    private int displayDegree;
    private Callback callback;

    private YuvFormat expectFormat;
    private YuvFormat realFormat;
    private PacketDataInserter packetDataInserter;

    private IYuvUtil yuvUtil;

    //最终可能旋转后,输出的宽高
    protected int widthFinally;
    protected int heightFinally;



    public AbstractVideoGather(Context context) {
        this.context = context;
        //初始化yuvUtil
        //...
    }

    @Override
    public void config(IGatherParam param) {

    }

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
}
