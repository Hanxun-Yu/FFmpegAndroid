package com.kedacom.demo.appcameratoh264.media.gather.video;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.kedacom.demo.appcameratoh264.media.YuvFormat;
import com.kedacom.demo.appcameratoh264.media.gather.api.AbstractVideoGather;
import com.kedacom.demo.appcameratoh264.media.gather.api.GatherData;
import com.kedacom.demo.appcameratoh264.media.gather.api.IGatherParam;
import com.kedacom.demo.appcameratoh264.media.util.YuvData;

import java.io.IOException;


/**
 * Created by yuhanxun
 * 2018/9/29
 * description:
 */
public class CameraGather extends AbstractVideoGather {
    private Camera mCamera;
    private Camera.Parameters mParams;


    public CameraGather(Context context) {
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
    public void config(IGatherParam param) {
        super.config(param);
        if (param instanceof VideoGatherParam) {
            VideoGatherParam vParam = (VideoGatherParam) param;
            mParams = mCamera.getParameters();
            setCameraDisplayOrientation((Activity) context, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
            mParams.setPreviewSize(vParam.getWidth(), vParam.getHeight());
            mParams.setPreviewFormat(getFormat(vParam));
            mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            mParams.setPreviewFpsRange(0, vParam.getFps());
            if (vParam.isConstantFps()) {
                packetDataInserter = new PacketDataInserter(vParam.getFps());
                packetDataInserter.setInserterListener(new PacketDataInserter.InserterListener() {
                    @Override
                    public void onInsertData(GatherData data) {
                        //..额外插帧
                        if (callback != null)
                            callback.onGatherData(data);
                    }

                    @Override
                    public void onNormalData(GatherData data) {
                        // 正常帧
                        if (callback != null)
                            callback.onGatherData(data);
                    }

                    @Override
                    public void onLoseData(GatherData data) {
                        //..丢帧
                    }
                });
            }
            mCamera.setParameters(mParams); // setting camera parameters
            mCamera.addCallbackBuffer(getCallbackBuffer(vParam));
        } else {
            throw new IllegalArgumentException(param + " is not supported");
        }

        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
            GatherData ret = null;
            byte[] finalData = null;
            YuvData yuvData;

            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                //copy
                finalData = new byte[data.length];
                System.arraycopy(data, 0, finalData, 0, data.length);

                notifyRTFrame(finalData,finalData.length,camera.getParameters().getPreviewSize().width,
                        camera.getParameters().getPreviewSize().height, realFormat);

            }
        });

    }


    private int getFormat(VideoGatherParam param) {
        expectFormat = param.getFormat();
        int ret;
        switch (expectFormat) {
            case Yuv420p_YV12:
                realFormat = YuvFormat.Yuv420p_YV12;
                ret = ImageFormat.YV12;
                break;
            case Yuv420p_I420:
            case Yuv420sp_NV12:
            case Yuv420sp_NV21:
                realFormat = YuvFormat.Yuv420sp_NV21;
                ret = ImageFormat.NV21;
                break;

            default:
                throw new IllegalArgumentException("Format " + expectFormat + " not support!");
        }
        return ret;
    }

    private byte[] getCallbackBuffer(VideoGatherParam param) {
        byte[] ret;
        switch (expectFormat) {
            case Yuv420p_YV12:
            case Yuv420p_I420:
            case Yuv420sp_NV12:
            case Yuv420sp_NV21:
                ret = new byte[param.getWidth() * param.getHeight() * 3 >> 1];
                break;
            default:
                throw new IllegalArgumentException("Format " + expectFormat + " not support!");
        }
        return ret;
    }

    @Override
    public void start() {
        mCamera.startPreview();
    }

    @Override
    public void stop() {
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

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
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
}