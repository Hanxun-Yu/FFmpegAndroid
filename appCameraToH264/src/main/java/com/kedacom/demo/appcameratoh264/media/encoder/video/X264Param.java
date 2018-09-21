package com.kedacom.demo.appcameratoh264.media.encoder.video;

import android.os.Parcel;
import android.os.Parcelable;

import com.kedacom.demo.appcameratoh264.media.encoder.api.IEncoderParam;

public class X264Param implements Parcelable,IEncoderParam{
    public X264Param() {
    }

    protected X264Param(Parcel in) {
        widthIN = in.readInt();
        heightIN = in.readInt();
        widthOUT = in.readInt();
        heightOUT = in.readInt();
        preset = in.readString();
        tune = in.readString();
        profile = in.readString();
        bitrate = in.readInt();
        bitrateCtrl = in.readString();
        fps = in.readInt();
        gop = in.readInt();
        useSlice = in.readByte() != 0;
        bFrameCount = in.readInt();
    }

    public static final Creator<X264Param> CREATOR = new Creator<X264Param>() {
        @Override
        public X264Param createFromParcel(Parcel in) {
            return new X264Param(in);
        }

        @Override
        public X264Param[] newArray(int size) {
            return new X264Param[size];
        }
    };

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

    public String getPreset() {
        return preset;
    }

    public void setPreset(String preset) {
        this.preset = preset;
    }

    public String getTune() {
        return tune;
    }

    public void setTune(String tune) {
        this.tune = tune;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public String getBitrateCtrl() {
        return bitrateCtrl;
    }

    public void setBitrateCtrl(String bitrateCtrl) {
        this.bitrateCtrl = bitrateCtrl;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public int getGop() {
        return gop;
    }

    public void setGop(int gop) {
        this.gop = gop;
    }

    public boolean isUseSlice() {
        return useSlice;
    }

    public void setUseSlice(boolean useSlice) {
        this.useSlice = useSlice;
    }

    public int getbFrameCount() {
        return bFrameCount;
    }

    public void setbFrameCount(int bFrameCount) {
        this.bFrameCount = bFrameCount;
    }

    private int widthIN;
    private int heightIN;
    private int widthOUT;
    private int heightOUT;

    private String preset;
    private String tune;
    private String profile;
    //Kbit
    private int bitrate;
    private String bitrateCtrl;

    private int fps;
    //second
    private int gop;
    private boolean useSlice;
    //I区间内B帧数量
    private int bFrameCount;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(widthIN);
        parcel.writeInt(heightIN);
        parcel.writeInt(widthOUT);
        parcel.writeInt(heightOUT);
        parcel.writeString(preset);
        parcel.writeString(tune);
        parcel.writeString(profile);
        parcel.writeInt(bitrate);
        parcel.writeString(bitrateCtrl);
        parcel.writeInt(fps);
        parcel.writeInt(gop);
        parcel.writeByte((byte) (useSlice ? 1 : 0));
        parcel.writeInt(bFrameCount);
    }


}