package com.example.yuhanxun.ffmpegplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.example.ffmpeg.FFmpegNative;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    TextureView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int ret = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (ret != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        Log.d("_xunxun", "ret:" + ret);

//        surfaceView = findViewById(R.id.surfaceview);
//        // Example of a call to a native
//        surfaceHolder = surfaceView.getHolder();
//        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
//            @Override
//            public void surfaceCreated(SurfaceHolder holder) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
////                        File file = new File("/sdcard/test2.mp4");
////                        FFmpegNative.play(Uri.fromFile(file).toString(), surfaceHolder.getSurface());
//                                        FFmpegNative.play("/sdcard/test2.mp4", surfaceHolder.getSurface());
//
//                    }
//                }).start();
//
//            }
//
//            @Override
//            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//
//            }
//
//            @Override
//            public void surfaceDestroyed(SurfaceHolder holder) {
//
//            }
//        });
        textureView = findViewById(R.id.textureview);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
                Log.d("_xunxun","onSurfaceTextureAvailable");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        File file = new File("/sdcard/test2.mp4");
//                FFmpegNative.play(Uri.fromFile(file).toString(), new Surface(surface));
                        FFmpegNative.play("/sdcard/test2.mp4", new Surface(surface));
                    }
                }).start();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // requestCode即所声明的权限获取码，在checkSelfPermission时传入
            case 1:
                Log.d("_xunxun", "onRequestPermissionsResult:" + grantResults);

                break;
            default:
                break;
        }
    }

}