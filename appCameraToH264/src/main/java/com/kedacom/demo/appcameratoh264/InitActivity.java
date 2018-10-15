package com.kedacom.demo.appcameratoh264;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kedacom.demo.appcameratoh264.api.IWorker;
import com.kedacom.demo.appcameratoh264.data.SizeParamUtil;
import com.kedacom.demo.appcameratoh264.fragment.X264ParamFragment;
import com.kedacom.demo.appcameratoh264.media.base.AudioChannel;
import com.kedacom.demo.appcameratoh264.media.base.AudioSampleRate;
import com.kedacom.demo.appcameratoh264.media.base.PCMFormat;
import com.kedacom.demo.appcameratoh264.media.collecter.CollecterConfig;
import com.kedacom.demo.appcameratoh264.media.collecter.CollecterType;
import com.kedacom.demo.appcameratoh264.media.collecter.audio.AudioCollecterParam;
import com.kedacom.demo.appcameratoh264.media.collecter.video.VideoCollecterParam;
import com.kedacom.demo.appcameratoh264.media.encoder.EncoderConfig;
import com.kedacom.demo.appcameratoh264.media.encoder.EncoderType;
import com.kedacom.demo.appcameratoh264.media.encoder.video.VideoEncoderParam;
import com.kedacom.demo.appcameratoh264.media.encoder.video.mediacodec.AndroidCodecParam;
import com.ycuwq.datepicker.CommonPicker.MultiplePickerFragment;
import com.ycuwq.datepicker.CommonPicker.PickerData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.kedacom.demo.appcameratoh264.media.base.YuvFormat.Yuv420p_I420;

/**
 * Created by yuhanxun
 * 2018/8/2
 * description:
 */
public class InitActivity extends AppCompatActivity {
    final String TAG = getClass().getSimpleName()+"_xunxun";
    private TextView resolutionSrcText;
    private TextView resolutionTargetText;
    private TextView deviceText;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }


    int camera = 0;
    int render = 0;
    int orientation = 0;
    int codec = 0;
    int muxer = 0;

    final String vendor = android.os.Build.BRAND;
    final String model = Build.MODEL;

    private void init() {
        sizeParamUtil = new SizeParamUtil(this);
        setContentView(R.layout.activity_init);
        resolutionSrcText = findViewById(R.id.resolution_src_text);
        resolutionTargetText = findViewById(R.id.resolution_target_text);
        deviceText = findViewById(R.id.deviceText);
        deviceText.setText(vendor+" "+model );
        ((RadioGroup) findViewById(R.id.group1)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.d("_xunxun", "group1:" + i);
                switch (i) {
                    case R.id.camera:
                        camera = 0;
                        break;
                    case R.id.camera2:
                        camera = 1;
                        break;
                }
            }
        });
        ((RadioGroup) findViewById(R.id.group2)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.d("_xunxun", "group2:" + i);
                switch (i) {
                    case R.id.surfaceview:
                        render = 0;
                        break;
                    case R.id.textureview:
                        render = 1;
                        break;
                }
            }
        });

        ((RadioGroup) findViewById(R.id.group3)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.d("_xunxun", "group3:" + i);
                switch (i) {
                    case R.id.portrait:
                        orientation = 0;
                        break;
                    case R.id.landscape:
                        orientation = 1;
                        break;
                }
            }
        });
        ((RadioGroup) findViewById(R.id.group4)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.d("_xunxun", "group4:" + i);
                switch (i) {
                    case R.id.x264:
                        codec = 0;
                        ((RadioButton)findViewById(R.id.portrait)).setEnabled(true);
                        break;
                    case R.id.mediacodec:
                        codec = 1;
                        if(vendor.equals("Kedacom") && model.equals("P2")) {
                            if(((RadioButton)findViewById(R.id.portrait)).isChecked()) {
                                ((RadioButton)findViewById(R.id.landscape)).setChecked(true);
                            }
                            ((RadioButton)findViewById(R.id.portrait)).setEnabled(false);
                            Toast.makeText(InitActivity.this,"P2 硬编不支持竖屏",Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        });
        ((RadioGroup) findViewById(R.id.group5)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.d("_xunxun", "group5:" + i);
                switch (i) {
                    case R.id.MP4:
                        muxer = 0;
                        break;
                    case R.id.MKV:
                        muxer = 1;
                        break;
                }
            }
        });
        findViewById(R.id.goBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("_xunxun", "camera:" + camera + " render:" + render);
                bindWorkService();
            }
        });

        findViewById(R.id.resolution_parent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ResolutionSrcDialogFragment fragment = new ResolutionSrcDialogFragment();
//                fragment.setOnDateChooseListener(new ResolutionSrcDialogFragment.OnDateChooseListener() {
//                    @Override
//                    public void onDateChoose(String... data) {
//                        refreshData();
//                    }
//                });
//                fragment.show(getSupportFragmentManager(), "ResolutionSrcDialogFragment");
                List<PickerData> param = new ArrayList<>();
                List<String> sizeList = Arrays.asList(sizeParamUtil.getWH_IN_List());
                param.add(new PickerData(sizeList,"a",sizeList.indexOf(sizeParamUtil.getWH_IN())));
                sizeList = Arrays.asList(sizeParamUtil.getWH_OUTList());
                param.add(new PickerData(sizeList,"bbb",sizeList.indexOf(sizeParamUtil.getWH_OUT())));
                MultiplePickerFragment fragment = MultiplePickerFragment.getInstance(param);
                fragment.setOnDateChooseListener(new MultiplePickerFragment.OnDateChooseListener() {
                    @Override
                    public void onDateChoose(String... data) {
                        sizeParamUtil.setWH_IN(data[0]);
                        sizeParamUtil.setWH_OUT(data[1]);
                        refreshData();

                    }
                });
                fragment.show(getSupportFragmentManager(), "MultiplePickerFragment");

            }
        });
//        findViewById(R.id.resolution_target_parent).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ResolutionTargetDialogFragment fragment = new ResolutionTargetDialogFragment();
//                fragment.setOnDateChooseListener(new ResolutionTargetDialogFragment.OnDateChooseListener() {
//                    @Override
//                    public void onDateChoose(String... data) {
//                        refreshData();
//                    }
//                });
//                fragment.show(getSupportFragmentManager(), "ResolutionTargetDialogFragment");
//            }
//        });
        switchX264();
        refreshData();
    }

    X264ParamFragment x264ParamFragment;

    private void switchX264() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_place_params, x264ParamFragment = new X264ParamFragment(), "X264ParamFragment").commit();
    }

    private void switchMediacodec() {

    }
    private void refreshData() {
        resolutionSrcText.setText(sizeParamUtil.getWH_IN());
        resolutionTargetText.setText(sizeParamUtil.getWH_OUT());
    }

    SizeParamUtil sizeParamUtil;
    private void startMain() {
        //重构
        //这里会吐出3种参数
        //1.界面参数:横竖屏,渲染层
        //2.采集器参数:采集器类型,以及不同采集器类型各自的参数
        //3.编码器参数:编码器类型,以及不同编码器类型各自的参数


//        Intent intent = new Intent(this, MainActivity.class);
        Intent intent = new Intent(this, MainActivity_EncoderController.class);
        setUIParam(intent);
        startActivity(intent);

        setCollectionParam();
        setEncoderParam();

//        intent.putExtra("muxer",muxer);
//        Log.d(TAG,"codec:"+codec);

//        intent.putExtra("param", param);

    }

    private void setUIParam( Intent intent) {
        intent.putExtra("render", render);
        intent.putExtra("orientation", orientation);
    }

    private void setCollectionParam() {
        VideoCollecterParam videoParam = new VideoCollecterParam();

        if(sizeParamUtil.getWH_IN().equals("1280x720")) {
            videoParam.setWidth(1280);
            videoParam.setHeight(720);
        } else if(sizeParamUtil.getWH_IN().equals("1920x1080")) {
            videoParam.setWidth(1920);
            videoParam.setHeight(1080);
        }
        videoParam.setFormat(Yuv420p_I420);
        videoParam.setConstantFps(true);
        videoParam.setFps(24);

        AudioCollecterParam audioParam = new AudioCollecterParam();
        audioParam.setChannel(AudioChannel.C2);
        audioParam.setFormat(PCMFormat.PCM_16BIT);
        audioParam.setSampleRate(AudioSampleRate.SR_44100);

        CollecterConfig collecterConfig = new CollecterConfig.Build().setVideo(camera == 0?CollecterType.Video.Camera
        :CollecterType.Video.Camera2)
                .setAudio(CollecterType.Audio.Mic)
                .setVideoParam(videoParam)
                .setAudioParam(audioParam)
                .build();
        iWorker.setCollecterParam(collecterConfig);

    }

    private void setEncoderParam() {
        VideoEncoderParam param;
        if(codec == 0) {
            param = x264ParamFragment.getParams();
        } else {
            param = new AndroidCodecParam();
            param.setByterate(4 * 1024 * 1024);
            param.setFps(25);
            param.setGop(1);
        }

        if(sizeParamUtil.getWH_IN().equals("1280x720")) {
            param.setWidthIN(1280);
            param.setHeightIN(720);
        } else if(sizeParamUtil.getWH_IN().equals("1920x1080")) {
            param.setWidthIN(1920);
            param.setHeightIN(1080);
        }

        if(sizeParamUtil.getWH_OUT().equals("1280x720")) {
            param.setWidthOUT(1280);
            param.setHeightOUT(720);
        } else if(sizeParamUtil.getWH_OUT().equals("1920x1080")) {
            param.setWidthOUT(1920);
            param.setHeightOUT(1080);
        }

        if(orientation == 0) {
            int temp = param.getWidthIN();
            param.setWidthIN(param.getHeightIN());
            param.setHeightIN(temp);

            temp = param.getWidthOUT();
            param.setWidthOUT(param.getHeightOUT());
            param.setHeightOUT(temp);
        }


        EncoderConfig encoderConfig =new EncoderConfig.Build()
                .setVideo(codec == 0 ? EncoderType.Video.X264 : EncoderType.Video.MediaCodec)
                .setVideoParam(param)
                .setVideoSavePath("/sdcard/264/123.h264")
                .setAudio(EncoderType.Audio.AAC)
                .setAudioSavePath("/sdcard/264/123.aac")
                .build();

        iWorker.setEncoderParam(encoderConfig);

    }
    IWorker iWorker;
    ServiceConnection connection;
    boolean isbinding;
    void bindWorkService() {
        if(iWorker == null && !isbinding) {
            isbinding = true;
            Intent intent = new Intent(this, WorkService.class);
            bindService(intent, connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    isbinding = false;
                    iWorker = ((WorkService.MyBinder) service).getService();
                    startMain();
                }

                @Override
                public void onBindingDied(ComponentName name) {
                    //need to unbind
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    iWorker = null;
                }
            }, Service.BIND_AUTO_CREATE);
        }
    }

    void unBindWorkService() {
        if(connection != null) {
            unbindService(connection);
            connection = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unBindWorkService();
    }
}
