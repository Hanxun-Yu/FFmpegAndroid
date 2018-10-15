package com.kedacom.demo.appcameratoh264.ui.main;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.kedacom.demo.appcameratoh264.WorkService;
import com.kedacom.demo.appcameratoh264.api.IWorker;
import com.kedacom.demo.appcameratoh264.media.collecter.api.ICollectData;
import com.kedacom.demo.appcameratoh264.media.collecter.api.IMediaCollecter;
import com.kedacom.demo.appcameratoh264.media.collecter.audio.AudioCollectData;
import com.kedacom.demo.appcameratoh264.ui.IView;
import com.orhanobut.logger.Logger;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by yuhanxun
 * 2018/10/9
 * description:
 */
public class MainPresenter implements IView.IViewLife, IMainPresenter {
    private Activity activity;
    private IMainView mainView;

    public MainPresenter(IMainView mainView) {
        this.activity = (Activity) mainView;
        this.mainView = mainView;
        mainView.setOnViewChangedListener(this);
        bindWorkService();
        startFreshUI();
    }


    private final int FRESH_INFO = 0x001;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case FRESH_INFO:
                    if (!hasDestroyed) {
                        mainView.showWorkingTime(getTimeData());
                        mainView.showMemoryStatus(getMemoryInfo());
                        mainView.showCollecterState(iWorker.getCollecterState());
                        mainView.showEncoderState(iWorker.getEncoderState());
                    }
                    break;
            }
            return false;
        }
    });


    private void startFreshUI() {
        handler.obtainMessage(FRESH_INFO).sendToTarget();
    }

    private void stopFreshUI() {
        handler.removeCallbacksAndMessages(null);
    }
    private float[] getMemory() {
        float ret[] = new float[4];
        ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        //最大分配内存
        int memory = activityManager.getMemoryClass();
        System.out.println("memory: " + memory);
        //最大分配内存获取方法2
        float maxMemory = (float) (Runtime.getRuntime().maxMemory() * 1.0 / (1024 * 1024));
        //当前分配的总内存
        float totalMemory = (float) (Runtime.getRuntime().totalMemory() * 1.0 / (1024 * 1024));
        //剩余内存
        float freeMemory = (float) (Runtime.getRuntime().freeMemory() * 1.0 / (1024 * 1024));
        ret[0] = maxMemory;
        ret[1] = totalMemory;
        ret[2] = freeMemory;
        ret[3] = memory;
        return ret;
    }

    private String getMemoryInfo() {
        float[] memory = getMemory();
        DecimalFormat fnum = new DecimalFormat("##0.00");
        StringBuffer buffer = new StringBuffer();
        buffer.append("---------memory---------\n");
        buffer.append("max:");
        buffer.append(fnum.format(memory[0]));
        buffer.append("\n");
        buffer.append("maxHeap:");
        buffer.append(fnum.format(memory[3]));
        buffer.append("\n");
        buffer.append("malloc:");
        buffer.append(fnum.format(memory[1]));
        buffer.append("\n");
        buffer.append("free:");
        buffer.append(fnum.format(memory[2]));
        return buffer.toString();
    }

    long startTime;

    private String getTimeData() {
        if (startTime == 0) {
            return "00:00:00";
        }
        long diff = System.currentTimeMillis() - startTime;
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formatter.format(diff);
    }

    private void notifyUIWave(byte[] audioData) {
//        if (count % frequence == 0) {
        for (int i = 0; i < audioData.length; i += 4) {
            //PCM16,采样一次4个字节，左右各2个字节,PCM16
            if (i > audioData.length - 4) {
                break;
            }
            byte left1 = audioData[i];
            byte left2 = audioData[i + 1];
            byte right1 = audioData[i + 2];
            byte right2 = audioData[i + 3];

            short u_left1 = (short) (left1 & 0xff);
            short u_left2 = (short) (left2 & 0xff);
            short u_right1 = (short) (right1 & 0xff);
            short u_right2 = (short) (right2 & 0xff);


            short left = (short) ((u_left1) | (u_left2 << 8));

            mainView.showWave(left);
//            notifyWaveView(left);
        }
    }

    private void configWorker(IWorker worker) {
        worker.setAudioCollecterCB(new IMediaCollecter.Callback() {
            @Override
            public void onCollectData(ICollectData data) {
                if(data instanceof AudioCollectData) {
                    notifyUIWave(((AudioCollectData) data).getPCMData().getData());
                }
            }
        });
    }


    private IWorker iWorker;
    private ServiceConnection connection;
    private boolean isbinding;
    private boolean hasDestroyed;

    private void bindWorkService() {
        if (iWorker == null && !isbinding) {
            isbinding = true;
            Intent intent = new Intent(activity, WorkService.class);
            activity.bindService(intent, connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Logger.e("onServiceConnected");
                    isbinding = false;
                    iWorker = ((WorkService.MyBinder) service).getService();
                    configWorker(iWorker);
                    //如果还没绑定成功就被调用了unbind
                    if (hasDestroyed) {
                        unBindWorkService();
                    }
                }

                @Override
                public void onBindingDied(ComponentName name) {
                    Logger.e("onBindingDied");

                    //need to unbind
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Logger.e("onServiceDisconnected");

                    iWorker = null;
                }

                @Override
                public void onNullBinding(ComponentName name) {
                    Logger.e("onNullBinding");
                }
            }, Service.BIND_AUTO_CREATE);
        }
    }

    private void unBindWorkService() {
        Logger.e("unBindWorkService");

        if (connection != null) {
            Logger.e("do unBindWorkService");
            activity.unbindService(connection);
            connection = null;
        }
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onResume() {

    }


    @Override
    public void onDestroy() {
        hasDestroyed = true;
        unBindWorkService();
        stopFreshUI();
    }

    @Override
    public void startWork() {
        startTime = System.currentTimeMillis();
        if (iWorker != null)
            iWorker.startWork();
    }

    @Override
    public void stopWork() {
        if (iWorker != null)
            iWorker.stopWork();
    }
}
