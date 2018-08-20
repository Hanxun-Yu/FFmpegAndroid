package com.kedacom.demo.appcameratoh264.data;

import android.content.Context;

import com.kedacom.demo.appcameratoh264.R;

/**
 * Created by yuhanxun
 * 2018/8/20
 * description:
 */
public class SizeParamUtil {
    private final String XML_NAME = "SizeParam";
    private final String WH_IN_KEY = "WH_IN_KEY";
    private final String WH_OUT_KEY = "WH_OUT_KEY";

    private Context context;

    private static String[] WH_IN_List;
    private static String[] WH_OUTList;


    public SizeParamUtil(Context context) {
        this.context = context;
    }

    public String[] getWH_IN_List() {
        if (WH_IN_List == null) {
            WH_IN_List = context.getResources().getStringArray(R.array.WidthHeight_IN);
        }
        return WH_IN_List;
    }

    public String[] getWH_OUTList() {
        if (WH_OUTList == null) {
            WH_OUTList = context.getResources().getStringArray(R.array.WidthHeight_OUT);
        }
        return WH_OUTList;
    }


    public String getWH_IN() {
        return ShareReferenceUtil.get(context, XML_NAME, WH_IN_KEY,
                context.getResources().getString(R.string.width_height_in_default));
    }

    public void setWH_IN(String val) {
        ShareReferenceUtil.set(context, XML_NAME, WH_IN_KEY, val);
    }

    public String getWH_OUT() {
        return ShareReferenceUtil.get(context, XML_NAME, WH_OUT_KEY,
                context.getResources().getString(R.string.width_height_out_default));
    }

    public void setWH_OUT(String val) {
        ShareReferenceUtil.set(context, XML_NAME, WH_OUT_KEY, val);
    }
}
