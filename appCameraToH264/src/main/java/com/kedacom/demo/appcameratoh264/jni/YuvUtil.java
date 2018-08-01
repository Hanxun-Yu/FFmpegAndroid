package com.kedacom.demo.appcameratoh264.jni;

/**
 * Created by yuhanxun
 * 2018/7/31
 * description:
 */
public class YuvUtil {
    /**
     * NV21转化为YUV420P数据
     * @param src         原始数据
     * @param width       原始数据宽度
     * @param height      原始数据高度
     * @param dst         生成数据
     * @param dst_width   生成数据宽度
     * @param dst_height  生成数据高度
     * @param mode        模式
     * @param degree      角度
     * @param isMirror    是否镜像
     * @return
     */
    public static native int compressYUV(byte[] src, int width, int height, byte[] dst, int dst_width
            , int dst_height, int mode, int degree, boolean isMirror);

    /**
     * YUV420P数据的裁剪
     * @param src         原始数据
     * @param width       原始数据宽度
     * @param height      原始数据高度
     * @param dst         生成数据
     * @param dst_width   生成数据宽度
     * @param dst_height  生成数据高度
     * @param left        裁剪的起始x点
     * @param top         裁剪的起始y点
     * @return
     */
    public static native int cropYUV(byte[] src, int width, int height, byte[] dst, int dst_width,
                                     int dst_height, int left, int top);

}
