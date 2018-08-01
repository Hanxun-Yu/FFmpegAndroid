package com.kedacom.demo.appcameratoh264;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kedacom.demo.appcameratoh264.media.video.MediaEncoder;
import com.kedacom.demo.appcameratoh264.media.video.VideoData420;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity implements
        Camera2Helper.AfterDoListener, Camera2Helper.OnRealFrameListener,
        MediaEncoder.MediaEncoderCallback {
    private AutoFitTextureView textureView;
    private Button recordBtn;
    private Button stopBtn;
    private TextView infoText;

    final String TAG = "MainActivity_xunxun";
    private Camera2Helper camera2Helper;
    private File file;
    public static final String PHOTO_PATH = Environment.getExternalStorageDirectory().getPath();
    public static final String PHOTO_NAME = "camera2";

    MediaEncoder mediaEncoder;

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
        mediaEncoder = new MediaEncoder();
        textureView = findViewById(R.id.textureview);
//        imageView= (ImageView) findViewById(R.id.imv_photo);
        recordBtn = findViewById(R.id.recordBtn);
        stopBtn = findViewById(R.id.stopBtn);
        infoText = findViewById(R.id.infoText);

//        progressBar= (ProgressBar) findViewById(R.id.progressbar_loading);
        file = new File(PHOTO_PATH, PHOTO_NAME + ".jpg");
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                camera2Helper.takePicture();
//                camera2Helper.startCallbackFrame();
                recording = true;
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recording = false;
//                camera2Helper.stopCallbackFrame();
            }
        });
        camera2Helper = Camera2Helper.getInstance(MainActivity.this, textureView, file);
        camera2Helper.setRealTimeFrameSize(widthIN, heightIN);
        camera2Helper.setOnRealFrameListener(this);
        camera2Helper.startCameraPreView();
        camera2Helper.setAfterDoListener(this);

        //写死了，应该与widthin and heightin应该与camera内yuv一致
        mediaEncoder.setMediaSize(widthIN, heightIN, widthOUT, heightOUT, 1024);
        mediaEncoder.setsMediaEncoderCallback(this);
        mediaEncoder.startVideoEncode();


        initEncoderThread();
    }

    HandlerThread handlerThread;
    Handler putEncoderHandler;

    int widthIN = 1280;
    int heightIN = 720;
    int widthOUT = 1280;
    int heightOUT = 720;
    private void initEncoderThread() {
        handlerThread = new HandlerThread("putEncoderThread");
        handlerThread.start();
        putEncoderHandler = new Handler(handlerThread.getLooper()) {
            VideoData420 vd420Temp;

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                vd420Temp = new VideoData420((byte[]) msg.obj, 1280, 720);
                mediaEncoder.putVideoData(vd420Temp);
            }
        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mediaEncoder.stop();
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
    String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private boolean checkPermission() {
        //如果返回true表示已经授权了
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
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

    VideoData420 vd420Temp;

    @Override
    public void onRealFrame(Image image) {
        Log.d(TAG, "onRealFrame image w:"+image.getWidth()+" h:"+image.getHeight()+" time:"+image.getTimestamp());

        if (recording) {

//            getByte(image.getPlanes()[0].getBuffer(),
//                    image.getPlanes()[1].getBuffer(),
//                    image.getPlanes()[2].getBuffer());
            long start = System.currentTimeMillis();
//            Log.d(TAG, "onRealFrame: isMainThread:" + (Looper.myLooper() == Looper.getMainLooper()));

            notifyEncoder(getByte(image.getPlanes()[0].getBuffer(),
                    image.getPlanes()[1].getBuffer(),
                    image.getPlanes()[2].getBuffer()));
            putYUVCount++;
            Log.d(TAG, "recording time:" + (System.currentTimeMillis() - start));

        }
    }


    private void notifyEncoder(byte[] bytes) {
        Message msg = putEncoderHandler.obtainMessage();
        msg.obj = bytes;
        msg.sendToTarget();
    }

    boolean recording = false;


    private byte[] getByte(ByteBuffer yb, ByteBuffer ub, ByteBuffer vb) {
//        long start = System.currentTimeMillis();
        byte[] ret = new byte[yb.remaining() + ub.remaining() + vb.remaining()];
        int position = 0;
        while (yb.hasRemaining()) {
//            Log.d(TAG, "bb.remaining():" + bb.remaining());
            byte[] t = new byte[yb.remaining()];
            yb.get(t);
            System.arraycopy(t, 0, ret, position, t.length);
            position += t.length;
        }

        while (ub.hasRemaining()) {
//            Log.d(TAG, "bb.remaining():" + bb.remaining());
            byte[] t = new byte[ub.remaining()];
            ub.get(t);
            System.arraycopy(t, 0, ret, position, t.length);
            position += t.length;
        }

        while (vb.hasRemaining()) {
//            Log.d(TAG, "bb.remaining():" + bb.remaining());
            byte[] t = new byte[vb.remaining()];
            vb.get(t);
            System.arraycopy(t, 0, ret, position, t.length);
            position += t.length;
        }
//        Log.d(TAG, "getByte time:" + (System.currentTimeMillis() - start));
        return ret;
    }


    long h264TotalSize = 0l;
    int putYUVCount = 0;
    int recvH264Count = 0;

    long lastFPSCheckTime = 0;
    int checkFPSYUVStart = 0;
    int checkFPSH264Start = 0;
    int yuvFPS = 0;
    int h264FPS = 0;

    @Override
    public void receiveEncoderVideoData(byte[] videoData, int totalLength, int[] segment) {
        Log.d(TAG, "recv h264 len:" + totalLength + " nalCount:" + segment.length);
        recvH264Count++;
        h264TotalSize += totalLength;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                infoText.setText("size:" + getSize(h264TotalSize) + "\n"
                        + "putYUV:" + putYUVCount + "\n"
                        + "recvH264:" + recvH264Count + "\n"
                        + getFPS());
            }
        });
    }

    private String getFPS() {
        if (lastFPSCheckTime == 0) {
            lastFPSCheckTime = System.currentTimeMillis();
        } else {
            if (System.currentTimeMillis() - lastFPSCheckTime < 1000) {
                if (checkFPSYUVStart == 0)
                    checkFPSYUVStart = putYUVCount;
                if (checkFPSH264Start == 0)
                    checkFPSH264Start = recvH264Count;
            } else {
                lastFPSCheckTime = System.currentTimeMillis();
                yuvFPS = putYUVCount - checkFPSYUVStart;
                h264FPS = recvH264Count - checkFPSH264Start;
                checkFPSYUVStart = 0;
                checkFPSH264Start = 0;
            }
        }

        return "yuvfps:" + yuvFPS + "\n"
                + "h264fps:" + h264FPS;
    }


    private String getSize(long sizel) {
        String ret = null;
        String unit = null;
        if (sizel < 1024) {
            unit = "B";
        } else if (sizel < 1024 * 1024) {
            unit = "KB";
            sizel = sizel / 1024;
        } else if (sizel < 1024 * 1024 * 1024) {
            unit = "MB";
            sizel = sizel / 1024 / 1024;
        }

        return sizel + unit;
    }

    @Override
    public void receiveEncoderAudioData(byte[] audioData, int size) {

    }
}