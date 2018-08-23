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
import com.kedacom.demo.appcameratoh264.data.SizeParamUtil;
import com.kedacom.demo.appcameratoh264.data.X264ParamUtil;
import com.ycuwq.datepicker.CommonPicker.ParentPicker;

import java.util.Arrays;
import java.util.List;

/**
 * 时间选择器，弹出框
 * Created by ycuwq on 2018/1/6.
 */
public class ResolutionSrcDialogFragment extends DialogFragment {

    protected OnePicker onePicker;
    private boolean mIsShowAnimation = true;
    protected Button mCancelButton, mDecideButton;

    public void setOnDateChooseListener(OnDateChooseListener onDateChooseListener) {
        this.onDateChooseListener = onDateChooseListener;
    }

    private OnDateChooseListener onDateChooseListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_one_picker, container);
        onePicker = view.findViewById(R.id.parent_picker);
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
                if(pickerRes != null) {
                    sizeParamUtil.setWH_IN(pickerRes[0]);
                }
				if (onDateChooseListener != null && pickerRes != null) {
                    onDateChooseListener.onDateChoose(pickerRes[0]);
				}
                dismiss();
            }
        });
        sizeParamUtil = new SizeParamUtil(getContext());
        initChild();

        return view;
    }

    SizeParamUtil sizeParamUtil;
    String[] pickerRes;
    protected void initChild() {
        List<String> sizeList = Arrays.asList(sizeParamUtil.getWH_IN_List());
        onePicker.initDataList(new ParentPicker.PickerData<>(sizeList,null));
        onePicker.setOnSelectedListener(new ParentPicker.OnSelectedListener() {
            @Override
            public void onSelected(Object... data) {
                Log.d("_xunxun", Arrays.toString(data));
//                if (onDateChooseListener != null) {
//                    onDateChooseListener.onDateChoose((String[]) data);
//                }
                pickerRes = (String[]) data;
            }
        });
        onePicker.setSelectIndex(sizeParamUtil.getWH_IN());
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
