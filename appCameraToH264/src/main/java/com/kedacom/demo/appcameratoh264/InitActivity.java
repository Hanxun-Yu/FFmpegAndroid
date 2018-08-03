package com.kedacom.demo.appcameratoh264;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;

/**
 * Created by yuhanxun
 * 2018/8/2
 * description:
 */
public class InitActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkPermission())
            init();
    }


    int camera = 0;
    int render = 0;
    int orientation = 0;
    private void init() {
        setContentView(R.layout.activity_init);
        ((RadioGroup)findViewById(R.id.group1)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.d("_xunxun","group1:"+i);
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
        ((RadioGroup)findViewById(R.id.group2)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.d("_xunxun","group2:"+i);
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

        ((RadioGroup)findViewById(R.id.group3)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.d("_xunxun","group3:"+i);
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
        findViewById(R.id.goBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("_xunxun","camera:"+camera+" render:"+render);
                startMain();
            }
        });
    }


    private void startMain() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("camera",camera);
        intent.putExtra("render",render);
        intent.putExtra("orientation",orientation);
        startActivity(intent);
    }

    final int REQUEST_CODE = 99;
    String[] permissions = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};

    private boolean checkPermission() {
        //如果返回true表示已经授权了
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
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
