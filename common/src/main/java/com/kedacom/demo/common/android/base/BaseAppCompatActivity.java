package com.kedacom.demo.common.android.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.orhanobut.logger.Logger;


/**
 * Created by yuhanxun
 * 2018/10/12
 * description:
 */
public class BaseAppCompatActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName() + "_xunxun";

    @Override
    protected void onResume() {
        super.onResume();
        Logger.i(TAG, "onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.i(TAG, "onStop");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.i(TAG, "onPause");

    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.i(TAG, "onStart");

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Logger.i(TAG, "onRestart");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.i(TAG, "onDestroy");

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i(TAG, "onCreate");

    }

}
