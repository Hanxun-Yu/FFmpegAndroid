package com.kedacom.demo.appcameratoh264.jni;

public class X264Param {
    public enum Preset {
        ultrafast, superfast, veryfast, faster, fast, medium, slow, slower, veryslow, placebo;

        public static String[] getStrArray() {
            String[] ret = new String[values().length];
            int i = 0;
            for (Preset item : values()) {
                ret[i] = item.name();
                i++;
            }
            return ret;
        }
    }

    public enum Tune {
        zerolatency, film, animation, grain, stillimage, psnr, ssim, fastdecode;

        public static String[] getStrArray() {
            String[] ret = new String[values().length];
            int i = 0;
            for (Tune item : values()) {
                ret[i] = item.name();
                i++;
            }
            return ret;
        }
        }

    public enum Profile {
        baseline, main, high, high10, high422, high444;

        public static String[] getStrArray() {
            String[] ret = new String[values().length];
            int i = 0;
            for (Profile item : values()) {
                ret[i] = item.name();
                i++;
            }
            return ret;
        }
    }

    public enum BitrateCtrl {
        VBR, CBR
    }

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

    public void setPreset(Preset preset) {
        this.preset = preset.name();
    }

    public String getTune() {
        return tune;
    }

    public void setTune(Tune tune) {
        this.tune = tune.name();
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile.name();
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

    public void setBitrateCtrl(BitrateCtrl bitrateCtrl) {
        this.bitrateCtrl = bitrateCtrl.name();
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

    private String preset = Preset.ultrafast.name();
    private String tune = Tune.zerolatency.name();
    private String profile = Profile.baseline.name();
    //Kbit
    private int bitrate = 4096;
    private String bitrateCtrl = BitrateCtrl.CBR.name();

    private int fps = 25;
    //second
    private int gop = 3;
    private boolean useSlice = false;
    //I区间内B帧数量
    private int bFrameCount = 0;
}