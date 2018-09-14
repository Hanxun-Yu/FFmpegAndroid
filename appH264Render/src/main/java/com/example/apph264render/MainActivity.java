package com.example.apph264render;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.example.apph264render.api.IDataGenerator;
import com.example.apph264render.api.IMediaCodec;
import com.example.apph264render.data.file.H264FileGenerator;
import com.example.apph264render.data.rtsp.RtspGenerator;
import com.example.apph264render.ffmpegcodec.FFmpegCodec;
import com.example.apph264render.mediacodec.MediaCodecDecoder;

public class MainActivity extends AppCompatActivity {
    final String TAG = "MainActivity_xunxun";
    TextureView textureView;
    boolean isRenderViewEnable = false;

    IMediaCodec videoCodec = new MediaCodecDecoder();
//    IMediaCodec videoCodec = new FFmpegCodec();

//        IDataGenerator dataGenerator
//            = new H264FileGenerator("/sdcard/264/123.h264", "/sdcard/264/h264_len.txt",false);
    IDataGenerator dataGenerator
            = new RtspGenerator("rtsp://admin:admin123@172.16.192.194:554/realtime?id=0;aid=0,10000;agent=cgi;");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkPermission())
            init();
    }

    void init() {
        Log.d(TAG, "init");
        setContentView(R.layout.activity_main);
        textureView = findViewById(R.id.textureview);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                Log.d(TAG, "onSurfaceTextureAvailable");
                isRenderViewEnable = true;
                videoCodec.setRenderView(new Surface(surfaceTexture));
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                Log.d(TAG, "onSurfaceTextureDestroyed");
                isRenderViewEnable = false;
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        });
        dataGenerator.init();
        dataGenerator.setOnDataListener(new IDataGenerator.OnDataReceiverListener() {
            @Override
            public void onReceiveData(byte[] data, int size) {
                if (!videoCodec.isStop() && isRenderViewEnable) {
                    videoCodec.putEncodeData(data, size);
                }
            }
        });
        videoCodec.init();
        dataGenerator.start();

    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy");
        super.onDestroy();
        dataGenerator.stop();
        videoCodec.release();
    }


    final int REQUEST_CODE = 99;
    String[] permissions = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};

    private boolean checkPermission() {
        Log.d(TAG, "checkPermission");

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
