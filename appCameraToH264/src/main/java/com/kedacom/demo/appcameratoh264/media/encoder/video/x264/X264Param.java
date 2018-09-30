package com.kedacom.demo.appcameratoh264.media.encoder.video.x264;


import android.os.Parcel;

import com.kedacom.demo.appcameratoh264.media.encoder.video.VideoEncoderParam;

public class X264Param extends VideoEncoderParam {

    public X264Param() {
    }

    protected X264Param(Parcel in) {
        super(in);
        preset = in.readString();
        tune = in.readString();
        profile = in.readString();
        bitrateCtrl = in.readString();
        gop = in.readInt();
        useSlice = in.readByte() != 0;
        bFrameCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeString(preset);
        dest.writeString(tune);
        dest.writeString(profile);
        dest.writeString(bitrateCtrl);
        dest.writeInt(gop);
        dest.writeByte((byte) (useSlice ? 1 : 0));
        dest.writeInt(bFrameCount);
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

    @Override
    public int describeContents() {
        return 0;
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


    public String getBitrateCtrl() {
        return bitrateCtrl;
    }

    public void setBitrateCtrl(String bitrateCtrl) {
        this.bitrateCtrl = bitrateCtrl;
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


    private String preset;
    private String tune;
    private String profile;
    //Kbit
    private String bitrateCtrl;

    //second
    private int gop;
    private boolean useSlice;
    //I区间内B帧数量
    private int bFrameCount;




}