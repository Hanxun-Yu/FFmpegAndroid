package com.kedacom.demo.appcameratoh264.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kedacom.demo.appcameratoh264.R;
import com.kedacom.demo.appcameratoh264.data.X264ParamUtil;
import com.kedacom.demo.appcameratoh264.media.encoder.video.X264Param;
import com.kedacom.demo.appcameratoh264.widget.pick.BitCtrlDoSliceDialogFragment;
import com.kedacom.demo.appcameratoh264.widget.pick.FpsGopBFrDialogFragment;
import com.kedacom.demo.appcameratoh264.widget.pick.ProPreTuneDialogFragment;

/**
 * Created by yuhanxun
 * 2018/8/17
 * description:
 */
public class X264ParamFragment extends Fragment {

    private android.widget.LinearLayout parentpart1;
    private android.widget.LinearLayout parentpart2;
    private android.widget.LinearLayout parentpart3;
    private android.widget.TextView profileText;
    private android.widget.TextView presetText;
    private android.widget.TextView tuneText;
    private android.widget.TextView bitrateCtrlText;
    private android.widget.TextView bitrateText;
    private android.widget.TextView doSliceText;
    private android.widget.TextView fpsText;
    private android.widget.TextView gopText;
    private android.widget.TextView bFrameText;

    X264ParamUtil x264ParamUtil;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        x264ParamUtil = new X264ParamUtil(getContext());
        View view = inflater.inflate(R.layout.fragment_x264param, null);
        this.bFrameText = view.findViewById(R.id.bFrameText);
        this.gopText = view.findViewById(R.id.gopText);
        this.fpsText = view.findViewById(R.id.fpsText);
        this.doSliceText = view.findViewById(R.id.doSliceText);
        this.bitrateText = view.findViewById(R.id.bitrateText);
        this.bitrateCtrlText = view.findViewById(R.id.bitrateCtrlText);
        this.tuneText = view.findViewById(R.id.tuneText);
        this.presetText = view.findViewById(R.id.presetText);
        this.profileText = view.findViewById(R.id.profileText);
        this.parentpart3 = view.findViewById(R.id.parent_part3);
        this.parentpart2 = view.findViewById(R.id.parent_part2);
        this.parentpart1 = view.findViewById(R.id.parent_part1);

        parentpart1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProPreTuneDialogFragment fragment = new ProPreTuneDialogFragment();
                fragment.setOnDateChooseListener(new ProPreTuneDialogFragment.OnDateChooseListener() {
                    @Override
                    public void onDateChoose(String... data) {
                        refreshData();
                    }
                });
                fragment.show(getFragmentManager(), "ProfilePickerDialogFragment");
            }
        });
        parentpart2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BitCtrlDoSliceDialogFragment fragment = new BitCtrlDoSliceDialogFragment();
                fragment.setOnDateChooseListener(new BitCtrlDoSliceDialogFragment.OnDateChooseListener() {
                    @Override
                    public void onDateChoose(String... data) {
                        refreshData();
                    }
                });
                fragment.show(getFragmentManager(), "BitCtrlDoSliceDialogFragment");
            }
        });
        parentpart3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FpsGopBFrDialogFragment fragment = new FpsGopBFrDialogFragment();
                fragment.setOnDateChooseListener(new FpsGopBFrDialogFragment.OnDateChooseListener() {
                    @Override
                    public void onDateChoose(String... data) {
                        refreshData();
                    }
                });
                fragment.show(getFragmentManager(), "FpsGopBFrDialogFragment");
            }
        });
        refreshData();
        return view;
    }

    String TAG = "X264ParamFragment_xunxun";

    private void refreshData() {
        profileText.setText(x264ParamUtil.getProfile());
        presetText.setText(x264ParamUtil.getPreset());
        tuneText.setText(x264ParamUtil.getTune());
        bitrateCtrlText.setText(x264ParamUtil.getBitrateCtrl());
        bitrateText.setText(x264ParamUtil.getBitrate());
        doSliceText.setText(x264ParamUtil.getDoSlice());
        fpsText.setText(x264ParamUtil.getFps());
        gopText.setText(x264ParamUtil.getGop());
        bFrameText.setText(x264ParamUtil.getBFrame());
    }

    public X264Param getParams() {
        X264Param ret = new X264Param();
        ret.setProfile(profileText.getText().toString());
        ret.setPreset(presetText.getText().toString());
        ret.setTune(tuneText.getText().toString());
        ret.setBitrateCtrl(bitrateCtrlText.getText().toString());
        ret.setByterate(Integer.parseInt(bitrateText.getText().toString())*1024);
        ret.setUseSlice(Boolean.parseBoolean(doSliceText.getText().toString()));
        ret.setFps(Integer.parseInt(fpsText.getText().toString()));
        ret.setGop(Integer.parseInt(gopText.getText().toString()));
        ret.setbFrameCount(Integer.parseInt(bFrameText.getText().toString()));
        return ret;
    }

    public X264ParamFragment() {
    }

}
