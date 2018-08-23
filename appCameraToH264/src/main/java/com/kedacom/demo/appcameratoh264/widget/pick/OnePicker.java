package com.kedacom.demo.appcameratoh264.widget.pick;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.kedacom.demo.appcameratoh264.R;
import com.ycuwq.datepicker.CommonPicker.ParentPicker;
import com.ycuwq.datepicker.WheelPicker;

/**
 * 日期选择器
 * Created by ycuwq on 2018/1/1.
 */
public class OnePicker extends ParentPicker {

    private WheelPicker<String> profilePicker;

    private OnSelectedListener mOnSelectedListener;

    public OnePicker(Context context) {
        this(context, null);
    }

    public OnePicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OnePicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void addSubPicker() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_one_picker, this);
        initChild();
    }

    private void initChild() {
        profilePicker = findViewById(R.id.picker);
        mSubPickers.add(profilePicker);
    }


    public void initDataList(PickerData data1) {
        profilePicker.setDataList(data1.data);

        if(data1.unit != null)
            profilePicker.setIndicatorText(data1.unit);
    }

    public void setSelectIndex(String i1) {
        profilePicker.setCurrentPosition(profilePicker.getDataList().indexOf(i1));
    }

    /**
     * Gets year.
     *
     * @return the year
     */
    public String getProfile() {
        return profilePicker.getCurrentSelectItem();
    }



}
