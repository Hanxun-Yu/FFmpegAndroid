package com.kedacom.demo.appcameratoh264;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kedacom.demo.appcameratoh264.jni.YuvUtil;
import com.kedacom.demo.appcameratoh264.media.Camera1Helper;
import com.kedacom.demo.appcameratoh264.media.audio.AudioData;
import com.kedacom.demo.appcameratoh264.media.audio.AudioRecoderManager;
import com.kedacom.demo.appcameratoh264.media.video.MediaEncoder;
import com.kedacom.demo.appcameratoh264.media.video.VideoData420;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements
        MediaEncoder.MediaEncoderCallback {
    private AutoFitTextureView textureView;
    private SurfaceView surfaceView;
    private Button recordBtn;
    private Button stopBtn;
    private TextView infoText;

    final String TAG = "MainActivity_xunxun";
    private Camera2Helper camera2Helper;
    private Camera1Helper camera1Helper;
    MediaEncoder mediaEncoder;


    boolean useCameraOne = false;
    boolean useSurfaceview = false;
    boolean usePortrait = false;
    int widthIN = 1280;
    int heightIN = 720;
    int widthOUT = 1280;
    int heightOUT = 720;
//    int widthIN = 720;
//    int heightIN = 1280;
//    int widthOUT = 720;
//    int heightOUT = 1280;

    int videoBitrate = 2048;
    private AudioRecoderManager audioGathererManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int camera = getIntent().getIntExtra("camera", 0);
        int render = getIntent().getIntExtra("render", 0);
        int orientation = getIntent().getIntExtra("orientation", 0);
        useCameraOne = camera == 0;
        useSurfaceview = render == 0;
        usePortrait = orientation == 0;

        if (!usePortrait)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        init();
        initCamera();
        initMicroPhone();
    }


    private void init() {
        mediaEncoder = new MediaEncoder();
        textureView = findViewById(R.id.textureview);
        surfaceView = findViewById(R.id.surfaceview);

        if (useSurfaceview) {
            textureView.setVisibility(View.GONE);
        } else {
            surfaceView.setVisibility(View.GONE);
        }

//        imageView= (ImageView) findViewById(R.id.imv_photo);
        recordBtn = findViewById(R.id.recordBtn);
        stopBtn = findViewById(R.id.stopBtn);
        infoText = findViewById(R.id.infoText);

//        progressBar= (ProgressBar) findViewById(R.id.progressbar_loading);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                camera2Helper.takePicture();
//                camera2Helper.startCallbackFrame();
                if (mediaEncoder.start()) {
                    recording = true;
                } else {
                    Toast.makeText(MainActivity.this, "MediaEncoder launch failer!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recording = false;
                mediaEncoder.stop();
                mediaEncoder.mux(MediaEncoder.MuxType.MP4);
//                camera2Helper.stopCallbackFrame();
            }
        });
    }

    private void initMicroPhone() {
        audioGathererManager = new AudioRecoderManager();
        audioGathererManager.setAudioDataListener(new AudioRecoderManager.AudioDataListener() {
            @Override
            public void audioData(byte[] data) {
//                Log.d(TAG,"audioData data size:"+data.length);
                if (recording) {
                    notifyEncoderAudio(data);
                }
            }
        });
        audioGathererManager.startAudioIn();
    }


    private void initCamera() {
        if (useCameraOne) {
            camera1Helper = new Camera1Helper(this, widthIN, heightIN);
            camera1Helper.setOnRealFrameListener(new Camera1Helper.OnRealFrameListener() {
                @Override
                public void onRealFrame(byte[] bytes) {
                    if (recording) {
                        notifyEncoderVideo(bytes);
                    }
                }
            });
            if (useSurfaceview) {
                surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceCreated(SurfaceHolder surfaceHolder) {
                        camera1Helper.openCamera(surfaceHolder);
                        initEncoder();
                    }

                    @Override
                    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    }
                });
            } else {
                textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                    @Override
                    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                        camera1Helper.openCamera(surfaceTexture);
                        initEncoder();
                    }

                    @Override
                    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

                    }

                    @Override
                    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                        return false;
                    }

                    @Override
                    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

                    }
                });
            }
        } else {
            if (useSurfaceview) {
                Toast.makeText(this, "Camera2 not support for now", Toast.LENGTH_SHORT).show();
                return;
            }
            camera2Helper = Camera2Helper.getInstance(MainActivity.this, textureView, null);
            camera2Helper.setOnRealFrameListener(new Camera2Helper.OnRealFrameListener() {
                @Override
                public void onRealFrame(Image image) {
//                    Log.d(TAG, "onRealFrame image w:" + image.getWidth() + " h:" + image.getHeight() + " time:" + image.getTimestamp());

                    if (recording) {
                        long start = System.currentTimeMillis();
//            Log.d(TAG, "onRealFrame: isMainThread:" + (Looper.myLooper() == Looper.getMainLooper()));

                        notifyEncoderVideo(getByte(image.getPlanes()[0].getBuffer(),
                                image.getPlanes()[1].getBuffer(),
                                image.getPlanes()[2].getBuffer()));
//                        Log.d(TAG, "recording time:" + (System.currentTimeMillis() - start));

                    }
                }
            });
            camera2Helper.setRealTimeFrameSize(widthIN, heightIN);
            camera2Helper.startCameraPreView();
            initEncoder();

        }
    }

    private void initEncoder() {
        //写死了，应该与widthin and heightin应该与camera内yuv一致
        if (useCameraOne) {
            if (camera1Helper.getDisplayOrientation() == 90 || camera1Helper.getDisplayOrientation() == 270) {
                mediaEncoder.setMediaSize(heightIN, widthIN, heightOUT, widthOUT, videoBitrate);
            } else {
                mediaEncoder.setMediaSize(widthIN, heightIN, widthOUT, heightOUT, videoBitrate);
            }
        } else {
            mediaEncoder.setMediaSize(widthIN, heightIN, widthOUT, heightOUT, videoBitrate);
        }
        mediaEncoder.setsMediaEncoderCallback(this);
        mediaEncoder.setOnMuxerListener(new MediaEncoder.OnMuxerListener() {
            @Override
            public void onSuccess(MediaEncoder.MuxType type, final String path) {
                Log.d(TAG, "onSuccess type:" + type + " path:" + path);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "mux OK:" + path, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(final String error) {
                Log.d(TAG, "onError error:" + error);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "mux Error:" + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        initEncoderThread();
    }

    HandlerThread handlerThread;
    Handler putEncoderHandler;


    final int HANDLE_VIDEO_MSG = 0;
    final int HANDLE_AUDIO_MSG = 1;

    private void initEncoderThread() {
        handlerThread = new HandlerThread("putEncoderThread");
        handlerThread.start();
        putEncoderHandler = new Handler(handlerThread.getLooper()) {
            VideoData420 vd420Temp;

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HANDLE_VIDEO_MSG:
                        if (useCameraOne) {
                            //收到NV21
                            byte[] yuv420sp = (byte[]) msg.obj;
                            byte[] yuv420p = new byte[yuv420sp.length];
                            YuvUtil.compressYUV(yuv420sp, widthIN, heightIN,
                                    yuv420p, widthOUT, heightOUT, 0, camera1Helper.getDisplayOrientation(), false);
                            vd420Temp = new VideoData420(yuv420p, widthOUT, heightOUT, System.currentTimeMillis());
                            mediaEncoder.putVideoData(vd420Temp);
                        } else {
                            //收到420p
                            vd420Temp = new VideoData420((byte[]) msg.obj, widthOUT, heightOUT, System.currentTimeMillis());
                            mediaEncoder.putVideoData(vd420Temp);
                        }
                        break;

                    case HANDLE_AUDIO_MSG:
                        byte[] pcm = (byte[]) msg.obj;
                        AudioData audioData = new AudioData(pcm);
                        mediaEncoder.putAudioData(audioData);
                        break;
                }
            }
        };
    }

    ByteArrayOutputStream videoTmpByteOut = new ByteArrayOutputStream();
    ByteArrayOutputStream audioTmpByteOut = new ByteArrayOutputStream();

    private void notifyEncoderVideo(byte[] bytes) {
        videoTmpByteOut.reset();
        try {
            videoTmpByteOut.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        cpy = new byte[bytes.length];
//        System.arraycopy(bytes, 0, cpy, 0, bytes.length);
        Message msg = putEncoderHandler.obtainMessage(HANDLE_VIDEO_MSG);
        msg.obj = videoTmpByteOut.toByteArray();
        msg.sendToTarget();
    }

    private void notifyEncoderAudio(byte[] bytes) {
        audioTmpByteOut.reset();
        try {
            audioTmpByteOut.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        cpy = new byte[bytes.length];
//        System.arraycopy(bytes, 0, cpy, 0, bytes.length);
        Message msg = putEncoderHandler.obtainMessage(HANDLE_AUDIO_MSG);
        msg.obj = audioTmpByteOut.toByteArray();
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

    int count = 0;
    int frequence = 8;

    @Override
    public void receiveEncoderVideoData(byte[] videoData, int totalLength, int[] segment) {
//        Log.d(TAG, "recv h264 len:" + totalLength + " nalCount:" + segment.length);
        if (count % frequence == 0) {
            runOnUiThread(runnable);
            if (count == frequence)
                count = 0;
        }
        count++;
    }

    @Override
    public void receiveEncoderAudioData(byte[] audioData, int size) {
        Log.d(TAG, "receiveEncoderAudioData size:" + size);
        if (count % frequence == 0) {
            for (int i = 0; i < size; i += 4) {
                //PCM16,采样一次4个字节，左右各2个字节,PCM16
                if (i > size - 4) {
                    break;
                }
                byte left1 = audioData[i];
                byte left2 = audioData[i + 1];
                byte right1 = audioData[i + 2];
                byte right2 = audioData[i + 3];

                short u_left1 = (short) (left1 & 0xff);
                short u_left2 = (short) (left2 & 0xff);
                short u_right1 = (short) (right1 & 0xff);
                short u_right2 = (short) (right2 & 0xff);


                short left = (short) ((u_left1 << 8) | u_left2);
                short right = (short) ((u_right1 << 8) | u_right2);


                Log.d(TAG, "audio_left 1:" + Integer.toHexString(u_left1)
                        + " 2:" + Integer.toHexString(u_left2)
                        + " 1&2:" + Integer.toHexString(left) + " dex:" + left);
                Log.d(TAG, "audio_right 1:" + Integer.toHexString(u_right1)
                        + " 2:" + Integer.toHexString(u_right2)
                        + " 1&2:" + Integer.toHexString(right) + " dex:" + right);
            }
//            Log.d(TAG, "audioData:" + size);
        }
    }


    Runnable runnable = new Runnable() {
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            float[] memory = getMemory();
            DecimalFormat fnum = new DecimalFormat("##0.00");
            infoText.setText(
                    (useCameraOne ? "Camera" : "Camera2") + "\n"
                            + (useSurfaceview ? "SurfaceView" : "TextureView") + "\n"
                            + "size:" + mediaEncoder.getEncodedSize() + "\n"
                            + "putPCM:" + mediaEncoder.getPutPCMCount() + "\n"
                            + "putYUV:" + mediaEncoder.getPutYUVCount() + "\n"
                            + "recvH264:" + mediaEncoder.getRecvH264Count() + "\n"
                            + "yuvFPS:" + mediaEncoder.getYuvFPS() + "\n"
                            + "h264FPS:" + mediaEncoder.getH264FPS() + "\n"
                            + "encoderQueue:" + mediaEncoder.getWaitEncodedQueueSize() + "\n"
                            + "---------memory---------\n"
                            + "max:" + fnum.format(memory[0]) + "\n"
                            + "maxHeap:" + fnum.format(memory[3]) + "\n"

                            + "malloc:" + fnum.format(memory[1]) + "\n"
                            + "free:" + fnum.format(memory[2]));
        }

    };


    private float[] getMemory() {
        float ret[] = new float[4];
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        //最大分配内存
        int memory = activityManager.getMemoryClass();
        System.out.println("memory: " + memory);
        //最大分配内存获取方法2
        float maxMemory = (float) (Runtime.getRuntime().maxMemory() * 1.0 / (1024 * 1024));
        //当前分配的总内存
        float totalMemory = (float) (Runtime.getRuntime().totalMemory() * 1.0 / (1024 * 1024));
        //剩余内存
        float freeMemory = (float) (Runtime.getRuntime().freeMemory() * 1.0 / (1024 * 1024));
        ret[0] = maxMemory;
        ret[1] = totalMemory;
        ret[2] = freeMemory;
        ret[3] = memory;
//        System.out.println("maxMemory: "+maxMemory);
//        System.out.println("totalMemory: "+totalMemory);
//        System.out.println("freeMemory: "+freeMemory);
        return ret;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
        releaseMediaEncoder();
        releaseMicroPhone();
    }

    private void releaseMediaEncoder() {
        if (!mediaEncoder.isStop())
            mediaEncoder.stop();
    }

    private void releaseCamera() {
        if (useCameraOne) {
            camera1Helper.releaseCamera();
        } else {
            if (camera2Helper != null)
                camera2Helper.onDestroyHelper();
        }
    }

    private void releaseMicroPhone() {
        audioGathererManager.stopAudioIn();
    }

}