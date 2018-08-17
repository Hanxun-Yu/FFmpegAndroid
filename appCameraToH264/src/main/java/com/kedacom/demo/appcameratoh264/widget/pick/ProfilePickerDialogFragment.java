package com.kedacom.demo.appcameratoh264.widget.pick;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;


import com.kedacom.demo.appcameratoh264.R;
import com.kedacom.demo.appcameratoh264.jni.X264Param;
import com.ycuwq.datepicker.CommonPicker.ParentPicker;

import java.util.Arrays;
import java.util.List;

/**
 * 时间选择器，弹出框
 * Created by ycuwq on 2018/1/6.
 */
public class ProfilePickerDialogFragment extends DialogFragment {

    protected ProfilePicker profilePicker;
    private boolean mIsShowAnimation = true;
    protected Button mCancelButton, mDecideButton;

    public void setOnDateChooseListener(OnDateChooseListener onDateChooseListener) {
        this.onDateChooseListener = onDateChooseListener;
    }

    private OnDateChooseListener onDateChooseListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_profile, container);
        profilePicker = view.findViewById(R.id.profile_parent_picker);
        mCancelButton = view.findViewById(R.id.btn_dialog_date_cancel);
        mDecideButton = view.findViewById(R.id.btn_dialog_date_decide);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mDecideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//				if (mOnDateChooseListener != null) {
//					mOnDateChooseListener.onDateChoose(mDatePicker.getYear(),
//							mDatePicker.getMonth(), mDatePicker.getDay());
//				}
                dismiss();
            }
        });

        initChild();

        return view;
    }

    protected void initChild() {
        List<String> profiles = Arrays.asList(X264Param.Profile.getStrArray());
        List<String> presets = Arrays.asList(X264Param.Preset.getStrArray());
        List<String> tunes = Arrays.asList(X264Param.Tune.getStrArray());

        profilePicker.initDataList(new ParentPicker.PickerData<>(profiles, null),
                new ParentPicker.PickerData<>(presets, null),
                new ParentPicker.PickerData<>(tunes, null));
        profilePicker.setOnSelectedListener(new ParentPicker.OnSelectedListener() {
            @Override
            public void onSelected(Object... data) {
                Log.d("_xunxun", Arrays.toString(data));
                if (onDateChooseListener != null) {
                    onDateChooseListener.onDateChoose((String[]) data);
                }
            }
        });
        profilePicker.setSelectIndex(initSelected[0], initSelected[1], initSelected[2]);
    }

    int[] initSelected;

    public void initSelected(int[] selected) {
        initSelected = selected;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), com.ycuwq.datepicker.R.style.DatePickerBottomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置Content前设定

        dialog.setContentView(com.ycuwq.datepicker.R.layout.dialog_date);
        dialog.setCanceledOnTouchOutside(true); // 外部点击取消

        Window window = dialog.getWindow();
        if (window != null) {
            if (mIsShowAnimation) {
                window.getAttributes().windowAnimations = com.ycuwq.datepicker.R.style.DatePickerDialogAnim;
            }
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.BOTTOM; // 紧贴底部
            lp.width = WindowManager.LayoutParams.MATCH_PARENT; // 宽度持平
            lp.dimAmount = 0.35f;
            window.setAttributes(lp);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        return dialog;
    }

    public interface OnDateChooseListener {
        void onDateChoose(String... data);
    }


}
