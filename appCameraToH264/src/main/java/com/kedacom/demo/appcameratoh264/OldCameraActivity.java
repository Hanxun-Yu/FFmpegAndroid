package com.kedacom.demo.appcameratoh264;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.kedacom.demo.appcameratoh264.jni.YuvUtil;
import com.kedacom.demo.appcameratoh264.media.video.MediaEncoder;
import com.kedacom.demo.appcameratoh264.media.video.VideoData420;

import java.io.IOException;

public class OldCameraActivity extends Activity implements SurfaceHolder.Callback,
        Camera.PreviewCallback, View.OnClickListener, MediaEncoder.MediaEncoderCallback {
    // raw frame resolution: 1280x720, image format is: YV12
    // you need get all resolution that supported on your devices;
    // my phone is HUAWEI honor 6Plus, most devices can use 1280x720
    private static final int SRC_FRAME_WIDTH = 1280;
    private static final int SRC_FRAME_HEIGHT = 720;
    private static final int IMAGE_FORMAT = ImageFormat.NV21;

    private Camera mCamera;
    private Camera.Parameters mParams;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    HandlerThread handlerThread;
    Handler putEncoderHandler;
    private MediaEncoder mediaEncoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oldcamera);
        if (checkPermission())
            init();
    }

    private void init() {
        initEncoderThread();

        initView();
        setListener();

        initEncoder();
    }

    private void initEncoder() {
        mediaEncoder = new MediaEncoder();
        //写死了，应该与widthin and heightin应该与camera内yuv一致
        mediaEncoder.setMediaSize(SRC_FRAME_WIDTH, SRC_FRAME_HEIGHT, SRC_FRAME_WIDTH, SRC_FRAME_HEIGHT, 512);
        mediaEncoder.setsMediaEncoderCallback(this);
        mediaEncoder.startVideoEncode();
    }


    private void initEncoderThread() {
        handlerThread = new HandlerThread("putEncoderThread");
        handlerThread.start();
        putEncoderHandler = new Handler(handlerThread.getLooper()) {
            VideoData420 vd420Temp;

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                byte[] yuv420sp = (byte[]) msg.obj;
                byte[] yuv420p = new byte[yuv420sp.length];
                YuvUtil.compressYUV(yuv420sp, 1280, 720,
                        yuv420p, 1280, 720, 0, 0, false);


                vd420Temp = new VideoData420(yuv420p, 1280, 720);
                mediaEncoder.putVideoData(vd420Temp);
            }
        };
    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.sv_recording);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setFixedSize(SRC_FRAME_WIDTH, SRC_FRAME_HEIGHT);
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void notifyEncoder(byte[] bytes) {
        Message msg = putEncoderHandler.obtainMessage();
        msg.obj = bytes;
        msg.sendToTarget();
    }

    private void setListener() {
        // set Listener if you want, eg: onClickListener
    }

    @Override
    public void onClick(View v) {
    }

    final String TAG = getClass().getName() + "_xunxun";

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
//        ImageUtils.saveImageData(data);
        Log.d(TAG, "onPreviewFrame len:" + data.length + " main:" + (Looper.myLooper() == Looper.getMainLooper()));
//        byte[] cpyByte = new byte[data.length];
//        System.arraycopy(data, 0, cpyByte, 0, data.length);
//        notifyEncoder(cpyByte);
//        camera.addCallbackBuffer(data);
        notifyEncoder(data);
        camera.addCallbackBuffer(new byte[data.length]);
    }

    private void openCamera(SurfaceHolder holder) {
        releaseCamera(); // release Camera, if not release camera before call camera, it will be locked
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        mParams = mCamera.getParameters();
        setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
        mParams.setPreviewSize(SRC_FRAME_WIDTH, SRC_FRAME_HEIGHT);
        mParams.setPreviewFormat(IMAGE_FORMAT); // setting preview format：YV12
        mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        mCamera.setParameters(mParams); // setting camera parameters
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        mCamera.setPreviewCallback(this);
        mCamera.startPreview();
    }

    private synchronized void releaseCamera() {
        if (mCamera != null) {
            try {
                mCamera.setPreviewCallback(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mCamera = null;
        }
    }

    /**
     * Android API: Display Orientation Setting
     * Just change screen display orientation,
     * the rawFrame data never be changed.
     */
    private void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int displayDegree;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayDegree = (info.orientation + degrees) % 360;
            displayDegree = (360 - displayDegree) % 360;  // compensate the mirror
        } else {
            displayDegree = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(displayDegree);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openCamera(holder); // open camera
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
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

    @Override
    public void receiveEncoderVideoData(byte[] videoData, int totalLength, int[] segment) {
        Log.d(TAG, "recv h264 len:" + totalLength + " nalCount:" + segment.length);
    }

    @Override
    public void receiveEncoderAudioData(byte[] audioData, int size) {

    }
}
