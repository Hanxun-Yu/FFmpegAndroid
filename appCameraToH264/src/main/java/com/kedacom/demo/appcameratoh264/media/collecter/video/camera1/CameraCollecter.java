package com.kedacom.demo.appcameratoh264.media.collecter.video.camera1;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.kedacom.demo.appcameratoh264.media.base.YuvFormat;
import com.kedacom.demo.appcameratoh264.media.collecter.video.AbstractVideoCollecter;
import com.kedacom.demo.appcameratoh264.media.collecter.video.VideoCollecterParam;

import java.io.IOException;
import java.util.List;


/**
 * Created by yuhanxun
 * 2018/9/29
 * description:
 */
public class CameraCollecter extends AbstractVideoCollecter {
    private Camera mCamera;
   


    public CameraCollecter(Context context) {
        super(context);
    }

    @Override
    public void setRender(Object object) {
        try {
            if (object instanceof SurfaceHolder)
                mCamera.setPreviewDisplay((SurfaceHolder) object);
            else if (object instanceof SurfaceTexture)
                mCamera.setPreviewTexture((SurfaceTexture) object);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public void init() {
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    @Override
    protected void _config(VideoCollecterParam param) {
        Camera.Parameters mParams = mCamera.getParameters();
        setCameraDisplayOrientation((Activity) context, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
        mParams.setPreviewSize(param.getWidth(), param.getHeight());

        mParams.setPreviewFormat(getCameraFormat(this.realFormat));
        mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        mParams.setPreviewFpsRange(param.getFps() * 1000, param.getFps() * 1000);

//        List<int[]> supportRange =mParams.getSupportedPreviewFpsRange();
//        for(int i=0;i<supportRange.size();i++){
//            int[] item = supportRange.get(i);
//            Log.d(TAG,"getSupportedPreviewFpsRange:"+Arrays.toString(item));
//        }

        mCamera.setParameters(mParams); // setting camera parameters
        mCamera.addCallbackBuffer(getCallbackBuffer(param));

        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
            byte[] finalData = null;

            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                //copy
                finalData = new byte[data.length];
                System.arraycopy(data, 0, finalData, 0, data.length);

                notifyRTFrame(finalData, finalData.length, camera.getParameters().getPreviewSize().width,
                        camera.getParameters().getPreviewSize().height, realFormat);

                camera.addCallbackBuffer(data);
            }
        });
    }

    @Override
    public void start() {
        mCamera.startPreview();
    }

    @Override
    public void stop() {
        if (mCamera != null)
            mCamera.stopPreview();
    }

    @Override
    public void release() {
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
        Log.d(TAG, "degree:" + degrees + " info.orientation:" + info.orientation);
        int displayDegree;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayDegree = (info.orientation + degrees) % 360;
            displayDegree = (360 - displayDegree) % 360;  // compensate the mirror
        } else {
            displayDegree = (info.orientation - degrees + 360) % 360;
        }
        Log.d(TAG, "displayDegree:" + displayDegree);
        this.displayDegree = displayDegree;
        camera.setDisplayOrientation(displayDegree);
    }




    protected boolean isFormatSupported(YuvFormat yuvFormat) {
        boolean ret = false;
        Camera.Parameters mParams = mCamera.getParameters();
        List<Integer> formats = mParams.getSupportedPreviewFormats();
        for (int cameraFormat : formats) {
            if (cameraFormat == getCameraFormat(yuvFormat)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    private int getCameraFormat(YuvFormat yuvFormat) {
        int ret = 0;
        switch (yuvFormat) {
            case Yuv420p_YV12:
                ret = ImageFormat.YV12;
                break;
            case Yuv420p_I420:
                ret = ImageFormat.YUV_420_888;
                break;
            case Yuv420sp_NV12:

                break;
            case Yuv420sp_NV21:
                ret = ImageFormat.NV21;
                break;
        }
        return ret;
    }


}