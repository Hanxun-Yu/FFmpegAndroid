package com.kedacom.demo.appcameratoh264.media.util;


import com.kedacom.demo.appcameratoh264.media.YuvFormat;

/**
 * Created by yuhanxun
 * 2018/9/29
 * description:
 */
public interface IYuvUtil {
    YuvData nv12ToI420(YuvData data);

    YuvData nv21ToI420(YuvData data);

    YuvData yv12ToI420(YuvData data);

    YuvData nv12ToI420(YuvData data, int degree);

    YuvData nv21ToI420(YuvData data, int degree);

    YuvData yv12ToI420(YuvData data, int degree);

    YuvData convertFormat(YuvData data, YuvFormat targetFormat);

    YuvData convertFormat(YuvData data, YuvFormat targetFormat, int degree);

}
