package com.kedacom.demo.appcameratoh264.media.gather.video;

import com.kedacom.demo.appcameratoh264.media.base.YuvFormat;
import com.kedacom.demo.appcameratoh264.media.gather.api.IGatherParam;

/**
 * Created by yuhanxun
 * 2018/9/29
 * description:
 */
public class VideoGatherParam implements IGatherParam{

    private int width;
    private int height;
    private int fps;
    private YuvFormat format;
    //恒定帧率
    boolean constantFps = false;





    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public YuvFormat getFormat() {
        return format;
    }

    public void setFormat(YuvFormat format) {
        this.format = format;
    }

    public boolean isConstantFps() {
        return constantFps;
    }

    public void setConstantFps(boolean constantFps) {
        this.constantFps = constantFps;
    }

    @Override
    public String toString() {
        return "VideoGatherParam{" +
                "width=" + width +
                ", height=" + height +
                ", fps=" + fps +
                ", format=" + format +
                ", constantFps=" + constantFps +
                '}';
    }
}
