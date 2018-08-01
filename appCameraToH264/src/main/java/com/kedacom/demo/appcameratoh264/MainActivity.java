package com.kedacom.demo.appcameratoh264;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity implements Camera2Helper.AfterDoListener, Camera2Helper.OnRealFrameListener {
    private AutoFitTextureView textureView;
    private Button recordBtn;
    private Button stopBtn;

    final String TAG = "MainActivity_xunxun";
    private Camera2Helper camera2Helper;
    private File file;
    public static final String PHOTO_PATH = Environment.getExternalStorageDirectory().getPath();
    public static final String PHOTO_NAME = "camera2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (checkPermission())
            init();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        camera2Helper.startCameraPreView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        camera2Helper.onDestroyHelper();
    }

    private void init() {
        textureView = findViewById(R.id.textureview);
//        imageView= (ImageView) findViewById(R.id.imv_photo);
        recordBtn = findViewById(R.id.recordBtn);
        stopBtn = findViewById(R.id.stopBtn);

//        progressBar= (ProgressBar) findViewById(R.id.progressbar_loading);
        file = new File(PHOTO_PATH, PHOTO_NAME + ".jpg");
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                camera2Helper.takePicture();
                camera2Helper.startCallbackFrame();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera2Helper.stopCallbackFrame();
            }
        });
        camera2Helper = Camera2Helper.getInstance(MainActivity.this, textureView, file);
        camera2Helper.setOnRealFrameListener(this);
        camera2Helper.startCameraPreView();
        camera2Helper.setAfterDoListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.exit(0);
    }

    @Override
    public void onAfterPreviewBack() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onAfterTakePicture() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                InputStream input = null;
//                try {
//                    input = new FileInputStream(file);
//                    byte[] byt = new byte[input.available()];
//                    input.read(byt);
//                    imageView.setImageBitmap(BitmapUtil.bytes2Bitmap(byt));
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        });
    }

    final int REQUEST_CODE = 99;

    private boolean checkPermission() {
        //如果返回true表示已经授权了
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            // 类似 startActivityForResult()中的REQUEST_CODE
            // 权限列表,将要申请的权限以数组的形式提交。
            // 系统会依次进行弹窗提示。
            // 注意：如果AndroidManifest.xml中没有进行权限声明，这里配置了也是无效的，不会有弹窗提示。
            String[] permissions = {Manifest.permission.CAMERA};
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
                // grantResults是一个数组，和申请的数组一一对应。
                // 如果请求被取消，则结果数组为空。
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                    // 权限同意了，做相应处理
                } else {
                    // 权限被拒绝了


                    //权限再次申请
                    //当用户拒绝了某个权限时，我们可以再次去申请这个权限。但是这个时候，你应该告诉用户，你为什么要申请这个权限。
                    //判断一个权限是否被用户拒绝了,true表示拒绝了
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        // 用户拒绝过这个权限了，应该提示用户，为什么需要这个权限。
                    }
//说明用户勾选了不再提醒
                }
            }
            return;
        }
    }

    ByteArrayOutputStream bao = new ByteArrayOutputStream();

    @Override
    public void onRealFrame(Image image) {
        Log.d(TAG, "onRealFrame----------ssssss-------");
//        Log.d(TAG, "getPlanes y:" + image.getPlanes()[0].getBuffer().remaining());
//        Log.d(TAG, "getPlanes u:" + image.getPlanes()[1].getBuffer().remaining());
//        Log.d(TAG, "getPlanes v:" + image.getPlanes()[2].getBuffer().remaining());
        bao.reset();
        getByte(bao,image.getPlanes()[0].getBuffer());
        getByte(bao,image.getPlanes()[1].getBuffer());
        getByte(bao,image.getPlanes()[2].getBuffer());
        Log.d(TAG, "onRealFrame-----------eeeeeee--------");
        Log.d(TAG, "toByteArray len:"+bao.toByteArray().length);

    }

    private void getByte(ByteArrayOutputStream bao, ByteBuffer bb) {
        while (bb.hasRemaining()) {
            Log.d(TAG, "bb.remaining():" + bb.remaining());
            byte[] t = new byte[bb.remaining()];
            bb.get(t);
            try {
                bao.write(t);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}