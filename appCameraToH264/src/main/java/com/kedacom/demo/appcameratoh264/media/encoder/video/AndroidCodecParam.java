package com.kedacom.demo.appcameratoh264.media.encoder.video;


import android.os.Parcel;

import com.kedacom.demo.appcameratoh264.media.encoder.api.VideoEncoderParam;

/**
 * Created by yuhanxun
 * 2018/9/21
 * description:
 */
public class AndroidCodecParam extends VideoEncoderParam {
    public AndroidCodecParam() {
        super();
    }

    public AndroidCodecParam(Parcel in) {
        super(in);
    }

    public static final Creator<AndroidCodecParam> CREATOR = new Creator<AndroidCodecParam>() {
        @Override
        public AndroidCodecParam createFromParcel(Parcel in) {
            return new AndroidCodecParam(in);
        }

        @Override
        public AndroidCodecParam[] newArray(int size) {
            return new AndroidCodecParam[size];
        }
    };
}
