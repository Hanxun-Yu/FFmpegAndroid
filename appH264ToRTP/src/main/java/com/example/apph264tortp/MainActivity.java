package com.example.apph264tortp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.apph264tortp.h264.H264Helper;
import com.example.apph264tortp.jni.FFmpegJni;
import com.example.apph264tortp.rtp.RtpSenderWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button startBtn;
    Button stopBtn;
    Button putBtn;

    FFmpegJni fFmpegJni = new FFmpegJni();

    H264Helper h264Helper = new H264Helper();
    RtpSenderWrapper rtpSenderWrapper = new RtpSenderWrapper("192.168.200.247", 54326, false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkPermission())
            init();

    }

    private void init() {
        setContentView(R.layout.activity_main);
        fFmpegJni.init("ip", 1000);
        initView();
        biz();
    }

    private void initView() {
        startBtn = findViewById(R.id.start);
        stopBtn = findViewById(R.id.stop);
        putBtn = findViewById(R.id.put);
    }

    private void biz() {
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                fFmpegJni.start();
                startSend("/sdcard/264/123.h264");
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fFmpegJni.stop();
            }
        });
        putBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fFmpegJni.putH264(new byte[20], 30);
            }
        });
    }

    final String TAG = getClass().getSimpleName() + "_xunxun";

    private void startSend(String path) {
        h264Helper.findNalFromH264File(path, new H264Helper.OnNalListener() {
            @Override
            public void onFindNalIncludeStart(H264Helper.ByteData data) {
//                Log.d(TAG,"onFindNal:"+data.size);
                super.onFindNalIncludeStart(data);
            }

            int incrementalFps = 90000 / 25;
            long fpsStart = 0;
            long lastFindNalTime = 0;

            @Override
            public void onFindNal(final H264Helper.ByteData data) {
                Log.d(TAG, "onFindNal:" + data.size);
                int incremental = 0;
                if (lastFindNalTime == 0) {
                    lastFindNalTime = System.currentTimeMillis();
                } else {
                    incremental = (int) (System.currentTimeMillis() - lastFindNalTime);
                }
                fpsStart += incremental * 90;

//                fpsStart += incrementalFps;
                rtpSenderWrapper.sendAvcPacket(data.data, 0, data.size, fpsStart);
            }
        });
    }

//    public static void main(String[] args) {
//        byte[] data = new byte[]{
//                0x00, 0x00, 0x02, 0x45, 0x23, 0x00, 0x00, 0x00, 0x02
//        };
//        int[] ret = findNalHeadIndex(data, data.length);
//        System.out.print("" + ret.length);
//        for (int i = 0; i < ret.length; i++) {
//            System.out.print(ret[i] + ":");
//        }
//    }


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
