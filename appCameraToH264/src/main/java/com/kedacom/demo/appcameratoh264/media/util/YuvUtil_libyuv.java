package com.kedacom.demo.appcameratoh264.media.util;

import android.util.Log;

import com.kedacom.demo.appcameratoh264.jni.YuvJni;
import com.kedacom.demo.appcameratoh264.media.base.YuvData;
import com.kedacom.demo.appcameratoh264.media.base.YuvFormat;
import com.orhanobut.logger.Logger;

/**
 * Created by yuhanxun
 * 2018/9/30
 * description:
 */
public class YuvUtil_libyuv implements IYuvUtil {

    final String TAG = getClass().getSimpleName() + "_xunxun";
    YuvJni yuvJni = new YuvJni();

    @Override
    public YuvData nv12ToI420(YuvData data) {
        return nv12ToI420(data, 0);
    }

    @Override
    public YuvData nv21ToI420(YuvData data) {
        return nv21ToI420(data, 0);
    }

    @Override
    public YuvData yv12ToI420(YuvData data) {
        return null;
    }

    @Override
    public YuvData nv12ToI420(YuvData data, int degree) {
        YuvData ret = new YuvData();
        byte[] dst = new byte[data.getLength()];
        int[] wh = new int[2];
        yuvJni.nv12ToI420(data.getData(), data.getWidth(), data.getHeight(), dst, wh, degree);

        ret.setData(dst);
        ret.setLength(dst.length);
        ret.setWidth(wh[0]);
        ret.setHeight(wh[1]);
        ret.setFormat(YuvFormat.Yuv420p_I420);
        return ret;
    }

    @Override
    public YuvData nv21ToI420(YuvData data, int degree) {
        YuvData ret = new YuvData();
        byte[] dst = new byte[data.getLength()];
        int[] wh = new int[2];
        yuvJni.nv21ToI420(data.getData(), data.getWidth(), data.getHeight(), dst, wh, degree);

        ret.setData(dst);
        ret.setLength(dst.length);
        ret.setWidth(wh[0]);
        ret.setHeight(wh[1]);
        ret.setFormat(YuvFormat.Yuv420p_I420);
        return ret;
    }

    @Override
    public YuvData yv12ToI420(YuvData data, int degree) {
        return null;
    }

    @Override
    public YuvData convertFormat(YuvData data, YuvFormat targetFormat) {
        return convertFormat(data, targetFormat, 0);
    }

    @Override
    public YuvData convertFormat(YuvData data, YuvFormat targetFormat, int degree) {
//        Logger.d("data format:" + data.getFormat() + " to " + targetFormat + " degree:" + degree);
        YuvData ret;
        if (data.getFormat() == targetFormat)
            ret = data;
        else if (data.getFormat() == YuvFormat.Yuv420sp_NV12 && targetFormat == YuvFormat.Yuv420p_I420)
            ret = nv12ToI420(data, degree);
        else if (data.getFormat() == YuvFormat.Yuv420sp_NV21 && targetFormat == YuvFormat.Yuv420p_I420)
            ret = nv21ToI420(data, degree);
//        else if (data.getFormat() == YuvFormat.Yuv420p_YV12 && targetFormat == YuvFormat.Yuv420p_I420)
//            ret = yv12ToI420(data, degree);
        else
            throw new IllegalArgumentException(data.getFormat() + " to " + targetFormat + " not support!");

        return ret;
    }
}
