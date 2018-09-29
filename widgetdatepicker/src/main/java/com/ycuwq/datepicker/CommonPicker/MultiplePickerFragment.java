package com.ycuwq.datepicker.CommonPicker;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;


import com.ycuwq.datepicker.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 时间选择器，弹出框
 * Created by ycuwq on 2018/1/6.
 */
public class MultiplePickerFragment extends DialogFragment {

    protected MultiplePicker picker;
    private boolean mIsShowAnimation = true;
    protected Button mCancelButton, mDecideButton;

    public void setOnDateChooseListener(OnDateChooseListener onDateChooseListener) {
        this.onDateChooseListener = onDateChooseListener;
    }

    private OnDateChooseListener onDateChooseListener;

    private List<PickerData> pickerDataList;

    public static MultiplePickerFragment getInstance(List<PickerData> pickerDataList) {
        MultiplePickerFragment ret = new MultiplePickerFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("param", (ArrayList<? extends Parcelable>) pickerDataList);
        ret.setArguments(bundle);
        return ret;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_multiple_fragment, container);
        picker = view.findViewById(R.id.picker);
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
                if (onDateChooseListener != null && pickerRes != null) {
                    onDateChooseListener.onDateChoose(pickerRes);
                }

                dismiss();
            }
        });
        initChild();

        return view;
    }

    String[] pickerRes;

    protected void initChild() {
        List<PickerData> pickerDataList = (List<PickerData>) getArguments().get("param");
        picker.setData(pickerDataList);
        picker.setOnSelectedListener(new MultiplePicker.OnSelectedListener() {
            @Override
            public void onSelected(Object... data) {
                Log.d("_xunxun", Arrays.toString(data));
//                if (onDateChooseListener != null) {
//                    onDateChooseListener.onDateChoose((String[]) data);
//                }
                pickerRes = (String[]) data;
            }
        });
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
