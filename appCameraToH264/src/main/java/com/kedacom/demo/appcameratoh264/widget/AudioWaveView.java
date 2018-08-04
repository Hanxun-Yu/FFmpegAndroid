package com.kedacom.demo.appcameratoh264.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by yuhanxun
 * 2018/8/4
 * description:
 */
public class AudioWaveView extends View {
    public AudioWaveView(Context context) {
        this(context,null);
    }

    public AudioWaveView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public AudioWaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);




    }
}
