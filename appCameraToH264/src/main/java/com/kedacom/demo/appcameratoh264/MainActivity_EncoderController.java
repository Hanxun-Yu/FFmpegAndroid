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

import com.example.widget.AudioWaveView;
import com.kedacom.demo.appcameratoh264.jni.YuvUtil;
import com.kedacom.demo.appcameratoh264.media.encoder.EncoderConfig;
import com.kedacom.demo.appcameratoh264.media.encoder.EncoderManager;
import com.kedacom.demo.appcameratoh264.media.encoder.EncoderType;
import com.kedacom.demo.appcameratoh264.media.encoder.api.EncodedData;
import com.kedacom.demo.appcameratoh264.media.encoder.api.IMediaEncoder;
import com.kedacom.demo.appcameratoh264.media.encoder.api.VideoEncoderParam;
import com.kedacom.demo.appcameratoh264.media.encoder.audio.PCMData;
import com.kedacom.demo.appcameratoh264.media.encoder.video.YuvData;
import com.kedacom.demo.appcameratoh264.media.gather.AudioRecoderManager;
import com.kedacom.demo.appcameratoh264.media.gather.Camera1Helper;
import com.kedacom.demo.appcameratoh264.media.gather.Camera2Helper;
import com.kedacom.demo.appcameratoh264.widget.AutoFitTextureView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.TimeZone;

public class MainActivity_EncoderController extends AppCompatActivity {
    private AutoFitTextureView textureView;
    private SurfaceView surfaceView;
    private Button recordBtn;
    private Button stopBtn;
    private TextView infoText;
    private TextView memoryText;
    private TextView paramText;
    private TextView timeText;

    final String TAG = getClass().getSimpleName() + "_xunxun";
    private Camera2Helper camera2Helper;
    private Camera1Helper camera1Helper;
    private AudioWaveView audioWaveView;
    boolean useCameraOne = false;
    boolean useSurfaceview = false;
    boolean usePortrait = false;
    int cameraWidth = 1280;
    int cameraHeight = 720;

    private AudioRecoderManager audioGathererManager;

    String muxerFormat;
    int codec;

    Handler mainHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG,"onCreate");
        setContentView(R.layout.activity_main);
        int camera = getIntent().getIntExtra("camera", 0);
        int render = getIntent().getIntExtra("render", 0);
        int orientation = getIntent().getIntExtra("orientation", 0);
        codec = getIntent().getIntExtra("codec", 0);
        int muxer = getIntent().getIntExtra("muxer", 0);
        muxerFormat = muxer == 0 ? "mp4" : "mkv";

        useCameraOne = camera == 0;
        useSurfaceview = render == 0;
        usePortrait = orientation == 0;

        if (!usePortrait)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        param = getIntent().getParcelableExtra("param");
        cameraWidth = param.getWidthIN();
        cameraHeight = param.getHeightIN();

        Log.d(TAG, "param:" + param);
        if (codec == 0) {
            //x264

        } else if (codec == 1) {
            //mediaCodec
        }

        init();
        initCamera();
        initMicroPhone();
//        initCheckFaceThread();

        configEncoder();
        initEncoderPutThread();

    }

    VideoEncoderParam param;
    long startTime;

    private void init() {
        textureView = findViewById(R.id.textureview);
        surfaceView = findViewById(R.id.surfaceview);
        audioWaveView = findViewById(R.id.audioWaveView);
//        simpleWaveform = findViewById(R.id.simplewaveform);
        if (useSurfaceview) {
            textureView.setVisibility(View.GONE);
        } else {
            surfaceView.setVisibility(View.GONE);
        }

//        imageView= (ImageView) findViewById(R.id.imv_photo);
        recordBtn = findViewById(R.id.recordBtn);
        stopBtn = findViewById(R.id.stopBtn);
        infoText = findViewById(R.id.infoText);
        memoryText = findViewById(R.id.memoryText);
        paramText = findViewById(R.id.paramText);
        timeText = findViewById(R.id.timeText);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                audioGathererManager.startAudioIn();
                encoderManager.start();
                recording = true;
                startTime = System.currentTimeMillis();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recording = false;
                encoderManager.stop();
                audioGathererManager.stopAudioIn();
//                camera2Helper.stopCallbackFrame();
            }
        });
        showParams();
    }

    EncoderManager encoderManager = new EncoderManager();

    private void configEncoder() {
        if (usePortrait) {
            //如果是竖屏,camera采集的还是横屏,经过yuv处理变成了竖屏,所以进编码器也是竖屏
            int temp = param.getWidthOUT();
            param.setWidthOUT(param.getHeightOUT());
            param.setHeightOUT(temp);
        }
        //判断方向
        encoderManager.config(new EncoderConfig.Build()
                .setVideo(codec == 0 ? EncoderType.Video.X264 : EncoderType.Video.MediaCodec)
                .setVideoParam(param)
                .setVideoSavePath("/sdcard/264/123.h264")
                .setAudio(EncoderType.Audio.AAC)
                .setAudioSavePath("/sdcard/264/123.aac")
                .build());

        encoderManager.setVideoEncoderCB(new IMediaEncoder.Callback() {
            int count = 0;
            int frequence = 25;

            @Override
            public void onDataEncoded(EncodedData encodedData) {
                if (count % frequence == 0) {
                    mainHandler.post(runnable);
                    count = 0;
                }
                count++;
            }
        });

        encoderManager.setAudioEncoderCB(new IMediaEncoder.Callback() {
            @Override
            public void onDataEncoded(EncodedData encodedData) {

            }
        });
    }

    HandlerThread handlerThread;
    Handler putEncoderHandler;


    final int HANDLE_VIDEO_MSG = 0;
    final int HANDLE_AUDIO_MSG = 1;

    private void initEncoderPutThread() {
        handlerThread = new HandlerThread("putEncoderThread");
        handlerThread.start();
        putEncoderHandler = new Handler(handlerThread.getLooper()) {
            YuvData vd420Temp;

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HANDLE_VIDEO_MSG:
                        if (useCameraOne) {
                            //收到NV21
                            byte[] yuv420sp = (byte[]) msg.obj;
                            byte[] yuv420p = new byte[yuv420sp.length];
                            //就算是竖屏,这里拿到的还是横屏图像,所以要旋转
                            //这里传入的输出尺寸没什么用,只是格式转换,和根据角度旋转
                            if (codec == 0) {
                                //nv21 转 yuv420p
                                YuvUtil.compressYUV(yuv420sp, cameraWidth, cameraHeight,
                                        yuv420p, cameraWidth, cameraHeight, 0, camera1Helper.getDisplayOrientation(), false);
                            } else {
                                //nv21 转 nv12
                                YuvUtil.compressNV12(yuv420sp, cameraWidth, cameraHeight,
                                        yuv420p, cameraWidth, cameraHeight, 0, camera1Helper.getDisplayOrientation(), false);
//                                Log.d(TAG,"compressNV12 time:"+(System.currentTimeMillis()-start));

                            }
//                            if (camera1Helper.getDisplayOrientation() == 90 || camera1Helper.getDisplayOrientation() == 270) {
//                                vd420Temp = new YuvData(yuv420p, yuv420p.length, param.getHeightOUT(), param.getWidthOUT(), System.currentTimeMillis());
//                            } else {
                            vd420Temp = new YuvData(yuv420p, yuv420p.length, param.getWidthOUT(), param.getHeightOUT(), System.currentTimeMillis());
//                            }
//                            Log.d(TAG, "yuv size:" + yuv420p.length + " w:" + widthOUT + " h:" + heightOUT);
//                            vd420Temp = new YuvData(yuv420sp, widthOUT, heightOUT, System.currentTimeMillis());
                            encoderManager.encodeVideo(vd420Temp);
                        } else {
                            byte[] yuv420p = (byte[]) msg.obj;
                            //收到420p
                            vd420Temp = new YuvData(yuv420p, yuv420p.length, param.getWidthOUT(), param.getHeightOUT(), System.currentTimeMillis());
                            encoderManager.encodeVideo(vd420Temp);
                        }
                        break;

                    case HANDLE_AUDIO_MSG:
                        byte[] pcm = (byte[]) msg.obj;
                        PCMData audioData = new PCMData(pcm, pcm.length);
                        encoderManager.encodeAudio(audioData);
                        break;
                }
            }
        };
    }

    ByteArrayOutputStream videoTmpByteOut = new ByteArrayOutputStream();
    ByteArrayOutputStream audioTmpByteOut = new ByteArrayOutputStream();

    private void notifyEncoderVideo(byte[] bytes) {
        if (!recording)
            return;

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

    private void notifyUIWave(byte[] audioData) {
//        if (count % frequence == 0) {
        for (int i = 0; i < audioData.length; i += 4) {
            //PCM16,采样一次4个字节，左右各2个字节,PCM16
            if (i > audioData.length - 4) {
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


            short left = (short) ((u_left1) | (u_left2 << 8));
            notifyWaveView(left);
        }
    }

    private void notifyEncoderAudio(byte[] bytes) {
        if (!recording)
            return;
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


    Runnable runnable = new Runnable() {
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            infoText.setText(getDisplayData());
            memoryText.setText(getMemoryInfo());
            timeText.setText(getTimeData());
        }

    };

    private String getTimeData() {
        if (startTime == 0) {
            return "00:00:00";
        }
        long diff = System.currentTimeMillis() - startTime;
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formatter.format(diff);
    }

    private String getDisplayData() {
        StringBuffer buffer = new StringBuffer();

        buffer.append((useCameraOne ? "Camera" : "Camera2"));
        buffer.append("\n");
        buffer.append((useSurfaceview ? "SurfaceView" : "TextureView"));
        buffer.append("\n");
        buffer.append(encoderManager.getVideoEncoderInfo());
        buffer.append("\n");
        buffer.append(encoderManager.getAudioEncoderInfo());


        return buffer.toString();
    }

    private String getMemoryInfo() {
        float[] memory = getMemory();
        DecimalFormat fnum = new DecimalFormat("##0.00");
        StringBuffer buffer = new StringBuffer();
        buffer.append("---------memory---------\n");
        buffer.append("max:");
        buffer.append(fnum.format(memory[0]));
        buffer.append("\n");
        buffer.append("maxHeap:");
        buffer.append(fnum.format(memory[3]));
        buffer.append("\n");
        buffer.append("malloc:");
        buffer.append(fnum.format(memory[1]));
        buffer.append("\n");
        buffer.append("free:");
        buffer.append(fnum.format(memory[2]));
        return buffer.toString();
    }

    private void showParams() {
        StringBuffer sb = new StringBuffer();
        sb.append("IN:");
        sb.append(param.getWidthIN());
        sb.append("x");
        sb.append(param.getHeightIN());
        sb.append("\n");
        sb.append("OUT:");
        sb.append(param.getWidthOUT());
        sb.append("x");
        sb.append(param.getHeightOUT());
        sb.append("\n");
//        sb.append("bitrate:");
//        sb.append(param.getBitrate());
//        sb.append("Kbit\n");
//        sb.append("bitrateCtrl:");
//        sb.append(param.getBitrateCtrl());
//        sb.append("\n");
//        sb.append("fps:");
//        sb.append(param.getFps());
//        sb.append("\n");
//        sb.append("GOP:");
//        sb.append(param.getGop());
//        sb.append("\n");
//        sb.append("B帧:");
//        sb.append(param.getbFrameCount());
//        sb.append("\n");
//        sb.append("profile:");
//        sb.append(param.getProfile());
//        sb.append("\n");
//        sb.append("preset:");
//        sb.append(param.getPreset());
//        sb.append("\n");
//        sb.append("tune:");
//        sb.append(param.getTune());
//        sb.append("\n");
//        paramText.setText(sb.toString());
    }

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
        Log.e(TAG,"onDestroy");
        releaseCamera();
        releaseMediaEncoder();
        releaseMicroPhone();
    }

    private void releaseMediaEncoder() {
        encoderManager.stop();
        encoderManager.release();
    }

    private void releaseCamera() {
        if (useCameraOne) {
            if (camera1Helper != null)
                camera1Helper.releaseCamera();
        } else {
            if (camera2Helper != null)
                camera2Helper.onDestroyHelper();
        }
    }

    private void releaseMicroPhone() {
        audioGathererManager.release();
    }

    LinkedList<Integer> ampList = new LinkedList<>();

    private void notifyWaveView(final short val) {
        audioWaveView.putData(val);
    }


    private void initMicroPhone() {
        audioGathererManager = new AudioRecoderManager();
        audioGathererManager.setAudioDataListener(new AudioRecoderManager.AudioDataListener() {
            @Override
            public void audioData(byte[] data) {
//                Log.d(TAG,"audioData data size:"+data.length);
                if (recording) {
                    notifyEncoderAudio(data);
                    notifyUIWave(data);
                }
            }
        });
    }


    private void initCamera() {
        if (useCameraOne) {
            camera1Helper = new Camera1Helper(this, cameraWidth, cameraHeight);
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
            camera2Helper = Camera2Helper.getInstance(MainActivity_EncoderController.this, textureView, null);
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
            camera2Helper.setRealTimeFrameSize(cameraWidth, cameraHeight);
            camera2Helper.startCameraPreView();
        }
    }


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
}