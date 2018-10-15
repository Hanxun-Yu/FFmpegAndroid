package com.kedacom.demo.common.android.base;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.orhanobut.logger.Logger;

/**
 * Created by yuhanxun
 * 2018/10/12
 * description:
 */
public class BaseService extends Service{
    private final String TAG = getClass().getSimpleName()+"_xunxun";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Logger.i(TAG,"onBind");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i(TAG,"onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.i(TAG,"onDestroy");

    }
}
