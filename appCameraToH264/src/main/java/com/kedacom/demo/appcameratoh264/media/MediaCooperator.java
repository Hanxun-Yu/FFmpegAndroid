package com.kedacom.demo.appcameratoh264.media;

import android.content.Context;

import com.kedacom.demo.appcameratoh264.media.collecter.CollecterManager;
import com.kedacom.demo.appcameratoh264.media.encoder.EncoderManager;
import com.ycuwq.datepicker.WheelPicker;

/**
 * Created by yuhanxun
 * 2018/10/9
 * description:
 */
public class MediaCooperator {
    CollecterManager collecterManager;
    EncoderManager encoderManager;
    public MediaCooperator(Context context) {
        collecterManager = new CollecterManager(context);
        encoderManager = new EncoderManager();
    }
}
