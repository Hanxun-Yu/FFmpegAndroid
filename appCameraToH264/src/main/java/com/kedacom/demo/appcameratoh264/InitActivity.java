package com.kedacom.demo.appcameratoh264;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kedacom.demo.appcameratoh264.data.SizeParamUtil;
import com.kedacom.demo.appcameratoh264.fragment.X264ParamFragment;
import com.kedacom.demo.appcameratoh264.jni.X264Param;
import com.kedacom.demo.appcameratoh264.widget.pick.ResolutionSrcDialogFragment;
import com.kedacom.demo.appcameratoh264.widget.pick.ResolutionTargetDialogFragment;

/**
 * Created by yuhanxun
 * 2018/8/2
 * description:
 */
public class InitActivity extends AppCompatActivity {
    private TextView resolutionSrcText;
    private TextView resolutionTargetText;
    private TextView deviceText;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkPermission())
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
                startMain();
            }
        });

        findViewById(R.id.resolution_src_parent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResolutionSrcDialogFragment fragment = new ResolutionSrcDialogFragment();
                fragment.setOnDateChooseListener(new ResolutionSrcDialogFragment.OnDateChooseListener() {
                    @Override
                    public void onDateChoose(String... data) {
                        refreshData();
                    }
                });
                fragment.show(getSupportFragmentManager(), "ResolutionSrcDialogFragment");
            }
        });
        findViewById(R.id.resolution_target_parent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResolutionTargetDialogFragment fragment = new ResolutionTargetDialogFragment();
                fragment.setOnDateChooseListener(new ResolutionTargetDialogFragment.OnDateChooseListener() {
                    @Override
                    public void onDateChoose(String... data) {
                        refreshData();
                    }
                });
                fragment.show(getSupportFragmentManager(), "ResolutionTargetDialogFragment");
            }
        });
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
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("camera", camera);
        intent.putExtra("render", render);
        intent.putExtra("orientation", orientation);
        intent.putExtra("codec", codec);
        intent.putExtra("muxer",muxer);
        X264Param param;
//        if(codec == 0) {
            param = x264ParamFragment.getParams();
//        } else {
//            param = new X264Param();
//        }

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
        intent.putExtra("param", param);

        startActivity(intent);
    }

    final int REQUEST_CODE = 99;
    String[] permissions = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
//            Manifest.permission.CAPTURE_AUDIO_OUTPUT
    };

    private boolean checkPermission() {
        //如果返回true表示已经授权了
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
//                && checkSelfPermission(Manifest.permission.CAPTURE_AUDIO_OUTPUT) == PackageManager.PERMISSION_GRANTED
                ) {
            return true;
        } else {
            // 类似 startActivityForResult()中的REQUEST_CODE
            // 权限列表,将要申请的权限以数组的形式提交。
            // 系统会依次进行弹窗提示。
            // 注意：如果AndroidManifest.xml中没有进行权限声明，这里配置了也是无效的，不会有弹窗提示。

            ActivityCompat.requestPermissions(this,
                    permissions,
                    REQUEST_CODE);
            return false;
        }

    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPermission())
                        init();
                    // 权限同意了，做相应处理
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        // 用户拒绝过这个权限了，应该提示用户，为什么需要这个权限。
                    }
                }
            }
            return;
        }
    }
}
