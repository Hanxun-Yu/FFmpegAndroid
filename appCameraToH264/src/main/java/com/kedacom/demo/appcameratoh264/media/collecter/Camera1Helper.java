package com.kedacom.demo.appcameratoh264.media.collecter;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * Created by yuhanxun
 * 2018/8/2
 * description:
 */
public class Camera1Helper implements Camera.PreviewCallback {
    private Camera mCamera;
    private Camera.Parameters mParams;
    private int IMAGE_FORMAT = ImageFormat.NV21;
    private Context context;
    private int previewW;
    private int previewH;
    private int yuvOutW;
    private int yuvOutH;

    String TAG = "Camera1Helper_xunxun";
    public Camera1Helper(Context context, int previewW,int previewH) {
        this.context = context;
        this.previewW = previewW;
        this.previewH = previewH;
        this.yuvOutW = previewW;
        this.yuvOutH = previewH;

    }

    public void openCamera(Object holder) {
        releaseCamera(); // release Camera, if not release camera before call camera, it will be locked
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        mParams = mCamera.getParameters();
        setCameraDisplayOrientation((Activity) context, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
        mParams.setPreviewSize(previewW, previewH);
        mParams.setPreviewFormat(IMAGE_FORMAT); // setting preview format：YV12
        mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        mCamera.setParameters(mParams); // setting camera parameters
        mCamera.addCallbackBuffer(new byte[yuvOutW * yuvOutH * 3 >> 1]);
        try {
            if (holder instanceof SurfaceHolder)
                mCamera.setPreviewDisplay((SurfaceHolder) holder);
            else if (holder instanceof SurfaceTexture)
                mCamera.setPreviewTexture((SurfaceTexture) holder);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        mCamera.setPreviewCallback(this);
        mCamera.startPreview();
    }

    public synchronized void releaseCamera() {
        Log.e(TAG,"releaseCamera");
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
        Log.d(TAG,"degree:"+degrees+" info.orientation:"+info.orientation);
        int displayDegree;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayDegree = (info.orientation + degrees) % 360;
            displayDegree = (360 - displayDegree) % 360;  // compensate the mirror
        } else {
            displayDegree = (info.orientation - degrees + 360) % 360;
        }
        Log.d(TAG,"displayDegree:"+displayDegree);
        this.displayDegree = displayDegree;
        camera.setDisplayOrientation(displayDegree);
    }
    int displayDegree;
    public int getDisplayOrientation() {
        return displayDegree;
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if(onRealFrameListener != null)
            onRealFrameListener.onRealFrame(bytes);
        camera.addCallbackBuffer(bytes);
    }

    public void setOnRealFrameListener(OnRealFrameListener onRealFrameListener) {
        this.onRealFrameListener = onRealFrameListener;
    }

    OnRealFrameListener onRealFrameListener;

    public interface OnRealFrameListener {
        void onRealFrame(byte[] bytes);
    }
}
