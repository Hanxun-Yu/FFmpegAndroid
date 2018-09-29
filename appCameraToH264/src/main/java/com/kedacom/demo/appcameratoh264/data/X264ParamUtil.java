package com.kedacom.demo.appcameratoh264.data;

import android.content.Context;

import com.kedacom.demo.appcameratoh264.R;

/**
 * Created by yuhanxun
 * 2018/8/20
 * description:
 */
public class X264ParamUtil {
    private final String XML_NAME = "X264Param";
    private final String PROFILE_KEY = "PROFILE_KEY";
    private final String PRESET_KEY = "PRESET_KEY";
    private final String TUNE_KEY = "TUNE_KEY";
    private final String BITRATECTRL_KEY = "BITRATECTRL_KEY";
    private final String BITRATE_KEY = "BITRATE_KEY";
    private final String DOSLICE_KEY = "DOSLICE_KEY";
    private final String FPS_KEY = "FPS_KEY";
    private final String GOP_KEY = "GOP_KEY";
    private final String BFRAME_KEY = "BFRAME_KEY";

    private Context context;

    private static String[] ProfileList;
    private static String[] PresetList;
    private static String[] TuneList;
    private static String[] BitrateCtrlList;
    private static String[] BitrateList;
    private static String[] DoSliceList;
    private static String[] FpsList;
    private static String[] GopList;
    private static String[] BFrameList;


    public X264ParamUtil(Context context) {
        this.context = context;
    }

    public String[] getProfileList() {
        if (ProfileList == null) {
            ProfileList = context.getResources().getStringArray(R.array.Profile);
        }
        return ProfileList;
    }

    public String[] getPresetList() {
        if (PresetList == null) {
            PresetList = context.getResources().getStringArray(R.array.Preset);
        }
        return PresetList;
    }

    public String[] getTuneList() {
        if (TuneList == null) {
            TuneList = context.getResources().getStringArray(R.array.Tune);
        }
        return TuneList;
    }

    public String[] getBitrateCtrlList() {
        if (BitrateCtrlList == null) {
            BitrateCtrlList = context.getResources().getStringArray(R.array.BitrateCtrl);
        }
        return BitrateCtrlList;
    }

    public String[] getBitrateList() {
        if (BitrateList == null) {
            BitrateList = context.getResources().getStringArray(R.array.VideoBitrate);
        }
        return BitrateList;
    }

    public String[] getDoSliceList() {
        if (DoSliceList == null) {
            DoSliceList = context.getResources().getStringArray(R.array.DoSlice);
        }
        return DoSliceList;
    }

    public String[] getFpsList() {
        if (FpsList == null) {
            FpsList = context.getResources().getStringArray(R.array.Fps);
        }
        return FpsList;
    }

    public String[] getGopList() {
        if (GopList == null) {
            GopList = context.getResources().getStringArray(R.array.Gop);
        }
        return GopList;
    }

    public String[] getBFrameList() {
        if (BFrameList == null) {
            BFrameList = context.getResources().getStringArray(R.array.BFrameCount);
        }
        return BFrameList;
    }


    public String getProfile() {
        return ShareReferenceUtil.get(context, XML_NAME, PROFILE_KEY,
                context.getResources().getString(R.string.profile_default));
    }

    public void setProfile(String val) {
        ShareReferenceUtil.set(context, XML_NAME, PROFILE_KEY, val);
    }

    public String getPreset() {
        return ShareReferenceUtil.get(context, XML_NAME, PRESET_KEY,
                context.getResources().getString(R.string.preset_default));
    }

    public void setPreset(String val) {
        ShareReferenceUtil.set(context, XML_NAME, PRESET_KEY, val);
    }

    public String getTune() {
        return ShareReferenceUtil.get(context, XML_NAME, TUNE_KEY,
                context.getResources().getString(R.string.tune_default));
    }

    public void setTune(String val) {
        ShareReferenceUtil.set(context, XML_NAME, TUNE_KEY, val);
    }

    public String getBitrateCtrl() {
        return ShareReferenceUtil.get(context, XML_NAME, BITRATECTRL_KEY,
                context.getResources().getString(R.string.bitratectrl_default));
    }

    public void setBitrateCtrl(String val) {
        ShareReferenceUtil.set(context, XML_NAME, BITRATECTRL_KEY, val);
    }

    public String getBitrate() {
        return ShareReferenceUtil.get(context, XML_NAME, BITRATE_KEY,
                context.getResources().getString(R.string.bitrate_default));
    }

    public void setBitrate(String val) {
        ShareReferenceUtil.set(context, XML_NAME, BITRATE_KEY, val);
    }

    public String getDoSlice() {
        return ShareReferenceUtil.get(context, XML_NAME, DOSLICE_KEY,
                context.getResources().getString(R.string.doslice_default));
    }

    public void setDoSlice(String val) {
        ShareReferenceUtil.set(context, XML_NAME, DOSLICE_KEY, val);
    }

    public String getFps() {
        return ShareReferenceUtil.get(context, XML_NAME, FPS_KEY,
                context.getResources().getString(R.string.fps_default));
    }

    public void setFps(String val) {
        ShareReferenceUtil.set(context, XML_NAME, FPS_KEY, val);
    }

    public String getGop() {
        return ShareReferenceUtil.get(context, XML_NAME, GOP_KEY,
                context.getResources().getString(R.string.gop_default));
    }

    public void setGop(String val) {
        ShareReferenceUtil.set(context, XML_NAME, GOP_KEY, val);
    }

    public String getBFrame() {
        return ShareReferenceUtil.get(context, XML_NAME, BFRAME_KEY,
                context.getResources().getString(R.string.bframecount_default));
    }

    public void setBFrame(String val) {
        ShareReferenceUtil.set(context, XML_NAME, BFRAME_KEY, val);
    }
}
