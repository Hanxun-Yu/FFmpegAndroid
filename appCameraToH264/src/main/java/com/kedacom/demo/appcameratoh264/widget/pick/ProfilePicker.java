package com.kedacom.demo.appcameratoh264.widget.pick;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.kedacom.demo.appcameratoh264.R;
import com.ycuwq.datepicker.CommonPicker.ParentPicker;
import com.ycuwq.datepicker.WheelPicker;

import java.util.List;

/**
 * 日期选择器
 * Created by ycuwq on 2018/1/1.
 */
public class ProfilePicker extends ParentPicker {

    private WheelPicker<String> profilePicker;
    private WheelPicker<String> presetPicker;
    private WheelPicker<String> tunePicker;

    private OnSelectedListener mOnSelectedListener;

    public ProfilePicker(Context context) {
        this(context, null);
    }

    public ProfilePicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProfilePicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void addSubPicker() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_profile, this);
        initChild();
    }

    private void initChild() {
        profilePicker = findViewById(R.id.profile_picker);
        presetPicker = findViewById(R.id.preset_picker);
        tunePicker = findViewById(R.id.tune_picker);
        mSubPickers.add(profilePicker);
        mSubPickers.add(presetPicker);
        mSubPickers.add(tunePicker);
    }


    public void initDataList(PickerData data1, PickerData data2, PickerData data3) {
        profilePicker.setDataList(data1.data);
        presetPicker.setDataList(data2.data);
        tunePicker.setDataList(data3.data);

        if(data1.unit != null)
            profilePicker.setIndicatorText(data1.unit);
        if(data2.unit != null)
            presetPicker.setIndicatorText(data2.unit);
        if(data3.unit != null)
            tunePicker.setIndicatorText(data3.unit);
    }

    public void setSelectIndex(String i1,String i2, String i3) {
        profilePicker.setCurrentPosition(profilePicker.getDataList().indexOf(i1));
        presetPicker.setCurrentPosition(presetPicker.getDataList().indexOf(i2));
        tunePicker.setCurrentPosition(tunePicker.getDataList().indexOf(i3));
    }

    /**
     * Gets year.
     *
     * @return the year
     */
    public String getProfile() {
        return profilePicker.getCurrentSelectItem();
    }

    /**
     * Gets month.
     *
     * @return the month
     */
    public String getPreset() {
        return presetPicker.getCurrentSelectItem();
    }

    /**
     * Gets day.
     *
     * @return the day
     */
    public String getTune() {
        return tunePicker.getCurrentSelectItem();
    }


}
