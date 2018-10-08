package com.kedacom.demo.appcameratoh264.media.collecter.video.camera2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.kedacom.demo.appcameratoh264.media.base.YuvFormat;
import com.kedacom.demo.appcameratoh264.media.collecter.video.AbstractVideoCollecter;
import com.kedacom.demo.appcameratoh264.media.collecter.video.VideoCollecterParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by yuhanxun
 * 2018/10/8
 * description:
 */
public class Camera2Collecter extends AbstractVideoCollecter {
    private final CameraManager manager;
    private HandlerThread mBackgroundThread;//An additional thread for running tasks that shouldn't block the UI.
    private Handler mBackgroundHandler;
    private ImageReader mRealTimeFrameReader;
    private static final int MAX_PREVIEW_WIDTH = 1920;//Max preview width that is guaranteed by Camera2 API
    private static final int MAX_PREVIEW_HEIGHT = 1080;//Max preview height that is guaranteed by Camera2 API
    private Object render;
    private Semaphore mCameraOpenCloseLock;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest mPreviewRequest;

    //
    private StreamConfigurationMap configurationMap;
    //Camera
    private CameraCharacteristics characteristics;
    String cameraID;

    Handler mainHandler = new Handler();

    public Camera2Collecter(Context context) {
        super(context);
        manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            /*
                一般来说当你的Android智能设备有前后摄像头的话，那么后置摄像头的id为0 前置的为1
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                //前置摄像头
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }else if(facing != null && facing == CameraCharacteristics.LENS_FACING_BACK){
                    continue;
                }*/
            characteristics = manager.getCameraCharacteristics(cameraID = manager.getCameraIdList()[0]);
            configurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void _config(VideoCollecterParam param) {
        setUpCameraOutputs(param.getWidth(), param.getHeight());
    }

    @Override
    protected boolean isFormatSupported(YuvFormat yuvFormat) {
        int[] formats = configurationMap.getOutputFormats();
        boolean ret = false;
        for (int format : formats) {
            if (format == getCameraFormat(yuvFormat)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    @Override
    public void setRender(Object object) {
        this.render = object;

    }

    @Override
    public void init() {
        startBackgroundThread();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void start() {
        try {
            manager.openCamera(cameraID, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        stopBackgroundThread();
    }

    @Override
    public void release() {

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

    private void setUpCameraOutputs(int width, int height) {

        // For still image captures, we use the largest available size.
        Size largest = Collections.max(Arrays.asList(configurationMap.getOutputSizes(ImageFormat.JPEG)), new Comparator<Size>() {
            @Override
            public int compare(Size o1, Size o2) {
                return Long.signum((long) o1.getWidth() * o1.getHeight() - (long) o2.getWidth() * o2.getHeight());
            }
        });
//                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, /*maxImages*/2);//初始化ImageReader
//                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);//设置ImageReader监听

        mRealTimeFrameReader = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 1);
        mRealTimeFrameReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {

            @Override
            public void onImageAvailable(ImageReader imageReader) {
                long start = System.currentTimeMillis();
                Image image = null;
                try {
                    image = imageReader.acquireLatestImage();
//                            Log.d("PreviewListener", "GetPreviewImage");
                    if (image == null) {
                        return;
                    }

//                            if (onRealFrameListener != null)
//                                onRealFrameListener.onRealFrame(image);
//                            Log.d(TAG,"getPlanes len:"+image.getPlanes().length);
//
//                            Log.d(TAG,"getPlanes y:"+image.getPlanes()[0].getBuffer().remaining());
//                            Log.d(TAG,"getPlanes u:"+image.getPlanes()[1].getBuffer().remaining());
//                            Log.d(TAG,"getPlanes v:"+image.getPlanes()[2].getBuffer().remaining());


//                            byte[] bytes = ImageUtil.imageToByteArray(image);
//                            if (pushFlag == false)
//                                uploadImg(bytes);
                } finally {
                    if (image != null) {
                        image.close();
                    }
                }
                Log.d(TAG, "onImageAvailable t:" + (System.currentTimeMillis() - start));
//                        ByteBuffer buffer = imageReader.acquireNextImage().getPlanes()[0].getBuffer();
//                        byte[] bytes = new byte[buffer.remaining()];
            }

        }, mBackgroundHandler);

        //处理图片方向相关
        int displayRotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
        Integer mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        int degrees = 0;
        switch (displayRotation) {
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
        this.displayDegree = (mSensorOrientation - degrees + 360) % 360;
//                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    textureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//                } else {
//                    textureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
//                }
        // 设置是否支持闪光灯
        Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        boolean mFlashSupported = available == null ? false : available;
    }

    /**
     * 开启HandlerThread
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        if (mBackgroundThread == null) {
            return;
        }
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    CameraDevice mCameraDevice;
    //实现监听CameraDevice状态回调
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();//要想预览、拍照等操作都是需要通过会话来实现，所以创建会话用于预览
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    private void createCameraPreviewSession() {
        Log.d(TAG, "createCameraPreviewSession");
        Surface surface = null;
        Log.d(TAG, "degree:" + displayDegree);
        Log.d(TAG, "collecterParam:" + collecterParam.getWidth() + "x" + collecterParam.getHeight());

        try {
            if (render instanceof SurfaceTexture) {
                ((SurfaceTexture) render).setDefaultBufferSize(collecterParam.getWidth(), collecterParam.getHeight());
                surface = new Surface((SurfaceTexture) render);
            } else if (render instanceof SurfaceHolder) {
//                mainHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        ((SurfaceHolder) render).setFixedSize(collecterParam.getWidth(), collecterParam.getHeight());
//                    }
//                });
                surface = ((SurfaceHolder) render).getSurface();
            }
            // 将默认缓冲区的大小配置为我们想要的相机预览的大小。
            // This is the output Surface we need to start preview.
            // set up a CaptureRequest.Builder with the output Surface.
            final CaptureRequest.Builder mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);// 把显示预览界面的TextureView添加到到CaptureRequest.Builder中
            // create a CameraCaptureSession for camera preview.
            mPreviewRequestBuilder.addTarget(mRealTimeFrameReader.getSurface());
            mCameraDevice.createCaptureSession(Arrays.asList(surface,
                    mRealTimeFrameReader.getSurface()), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    // The camera is already closed
                    if (null == mCameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    mCaptureSession = cameraCaptureSession;
                    try {
                        // 设置自动对焦参数并把参数设置到CaptureRequest.Builder中
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        //设置闪光灯自动模式
//                        setAutoFlash(mPreviewRequestBuilder);

                        // 封装好CaptureRequest.Builder后，调用build 创建封装好CaptureRequest 并发送请求
                        mPreviewRequest = mPreviewRequestBuilder.build();
                        mCaptureSession.setRepeatingRequest(mPreviewRequest, null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 为了避免太大的预览大小会超过相机总线的带宽限
     */
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth, int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight && option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new Comparator<Size>() {
                @Override
                public int compare(Size o1, Size o2) {
                    return Long.signum((long) o1.getWidth() * o1.getHeight() - (long) o2.getWidth() * o2.getHeight());
                }
            });
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new Comparator<Size>() {
                @Override
                public int compare(Size o1, Size o2) {
                    return Long.signum((long) o1.getWidth() * o1.getHeight() - (long) o2.getWidth() * o2.getHeight());
                }
            });
        } else {
            return choices[0];
        }
    }


}
