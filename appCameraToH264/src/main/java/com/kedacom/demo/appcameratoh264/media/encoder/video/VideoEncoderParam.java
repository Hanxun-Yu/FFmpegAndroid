package com.kedacom.demo.appcameratoh264.media.encoder.video;

import android.os.Parcel;
import android.os.Parcelable;

import com.kedacom.demo.appcameratoh264.media.encoder.api.IEncoderParam;

/**
 * Created by yuhanxun
 * 2018/9/21
 * description:
 */
public class VideoEncoderParam implements IEncoderParam,Parcelable {
    private int widthIN;
    private int heightIN;
    private int widthOUT;
    private int heightOUT;
    private int byterate;//bitrate 字节
    private int fps;

    public VideoEncoderParam() {
    }

    protected VideoEncoderParam(Parcel in) {
        widthIN = in.readInt();
        heightIN = in.readInt();
        widthOUT = in.readInt();
        heightOUT = in.readInt();
        byterate = in.readInt();
        fps = in.readInt();
        gop = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(widthIN);
        dest.writeInt(heightIN);
        dest.writeInt(widthOUT);
        dest.writeInt(heightOUT);
        dest.writeInt(byterate);
        dest.writeInt(fps);
        dest.writeInt(gop);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VideoEncoderParam> CREATOR = new Creator<VideoEncoderParam>() {
        @Override
        public VideoEncoderParam createFromParcel(Parcel in) {
            return new VideoEncoderParam(in);
        }

        @Override
        public VideoEncoderParam[] newArray(int size) {
            return new VideoEncoderParam[size];
        }
    };

    public int getGop() {
        return gop;
    }

    public void setGop(int gop) {
        this.gop = gop;
    }

    private int gop;//I帧间隔秒

    public int getWidthIN() {
        return widthIN;
    }

    public void setWidthIN(int widthIN) {
        this.widthIN = widthIN;
    }

    public int getHeightIN() {
        return heightIN;
    }

    public void setHeightIN(int heightIN) {
        this.heightIN = heightIN;
    }

    public int getWidthOUT() {
        return widthOUT;
    }

    public void setWidthOUT(int widthOUT) {
        this.widthOUT = widthOUT;
    }

    public int getHeightOUT() {
        return heightOUT;
    }

    public void setHeightOUT(int heightOUT) {
        this.heightOUT = heightOUT;
    }

    public int getByterate() {
        return byterate;
    }

    public void setByterate(int byterate) {
        this.byterate = byterate;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

}
