package com.kedacom.demo.appcameratoh264;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.pm.ActivityInfo;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.widget.AudioWaveView;
import com.kedacom.demo.appcameratoh264.media.base.YuvFormat;
import com.kedacom.demo.appcameratoh264.media.collecter.video.VideoCollecterParam;
import com.kedacom.demo.appcameratoh264.media.collecter.video.camera2.Camera2Collecter;
import com.kedacom.demo.appcameratoh264.media.encoder.EncoderConfig;
import com.kedacom.demo.appcameratoh264.media.encoder.EncoderManager;
import com.kedacom.demo.appcameratoh264.media.encoder.EncoderType;
import com.kedacom.demo.appcameratoh264.media.encoder.api.IFrameData;
import com.kedacom.demo.appcameratoh264.media.encoder.api.IMediaEncoder;
import com.kedacom.demo.appcameratoh264.media.encoder.video.VideoEncoderParam;
import com.kedacom.demo.appcameratoh264.media.encoder.audio.PCMData;
import com.kedacom.demo.appcameratoh264.media.encoder.video.VideoPacketData;
import com.kedacom.demo.appcameratoh264.media.collecter.AudioRecoderManager;
import com.kedacom.demo.appcameratoh264.media.collecter.api.ICollectData;
import com.kedacom.demo.appcameratoh264.media.collecter.api.IMediaCollecter;
import com.kedacom.demo.appcameratoh264.media.collecter.api.IVideoCollecter;
import com.kedacom.demo.appcameratoh264.media.collecter.video.camera1.CameraCollecter;
import com.kedacom.demo.appcameratoh264.media.collecter.video.VideoCollectData;
import com.kedacom.demo.appcameratoh264.widget.AutoFitTextureView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.TimeZone;

import static com.kedacom.demo.appcameratoh264.media.base.YuvFormat.Yuv420p_I420;


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
        initRender();
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

                encoderManager.start();
                audioGathererManager.startAudioIn();

                recording = true;
                startTime = System.currentTimeMillis();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recording = false;
                audioGathererManager.stopAudioIn();
                encoderManager.stop();
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
            public void onDataEncoded(IFrameData encodedData) {
                if (count % frequence == 0) {
                    refreshInfo();
                    count = 0;
                }
                count++;
            }
        });

        encoderManager.setAudioEncoderCB(new IMediaEncoder.Callback() {
            @Override
            public void onDataEncoded(IFrameData encodedData) {

            }
        });

        encoderManager.setOnStateChangedListener(new IMediaEncoder.OnStateChangedListener() {

            @Override
            public void onState(IMediaEncoder encoder, IMediaEncoder.State state) {
                refreshInfo();
            }
        });
    }

    private void refreshInfo() {
        mainHandler.post(runnable);
    }

    HandlerThread handlerThread;
    Handler putEncoderHandler;


    final int HANDLE_VIDEO_MSG = 0;
    final int HANDLE_AUDIO_MSG = 1;

    private void initEncoderPutThread() {
        handlerThread = new HandlerThread("putEncoderThread");
        handlerThread.start();
        putEncoderHandler = new Handler(handlerThread.getLooper()) {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HANDLE_VIDEO_MSG:
                        VideoCollectData data = (VideoCollectData) msg.obj;
                        VideoPacketData temp = new VideoPacketData(data.getYuvData(),data.getTimestamp());
                        encoderManager.encodeVideo(temp);
                        break;

//                        if (useCameraOne) {
//                            //收到NV21
//                            byte[] yuv420sp = (byte[]) msg.obj;
//                            byte[] yuv420p = new byte[yuv420sp.length];
//                            //就算是竖屏,这里拿到的还是横屏图像,所以要旋转
//                            //这里传入的输出尺寸没什么用,只是格式转换,和根据角度旋转
//                            if (codec == 0) {
//                                //nv21 转 yuv420p
//                                YuvUtil.compressYUV(yuv420sp, cameraWidth, cameraHeight,
//                                        yuv420p, cameraWidth, cameraHeight, 0, camera1Helper.getDisplayOrientation(), false);
//                            } else {
//                                //nv21 转 nv12
//                                YuvUtil.compressNV12(yuv420sp, cameraWidth, cameraHeight,
//                                        yuv420p, cameraWidth, cameraHeight, 0, camera1Helper.getDisplayOrientation(), false);
////                                Log.d(TAG,"compressNV12 time:"+(System.currentTimeMillis()-start));
//
//                            }
////                            if (camera1Helper.getDisplayOrientation() == 90 || camera1Helper.getDisplayOrientation() == 270) {
////                                vd420Temp = new YuvData(yuv420p, yuv420p.length, param.getHeightOUT(), param.getWidthOUT(), System.currentTimeMillis());
////                            } else {
//                            vd420Temp = new YuvData(yuv420p, yuv420p.length, param.getWidthOUT(), param.getHeightOUT(), System.currentTimeMillis());
////                            }
////                            Log.d(TAG, "yuv size:" + yuv420p.length + " w:" + widthOUT + " h:" + heightOUT);
////                            vd420Temp = new YuvData(yuv420sp, widthOUT, heightOUT, System.currentTimeMillis());
//                            encoderManager.encodeVideo(vd420Temp);
//                        } else {
//                            byte[] yuv420p = (byte[]) msg.obj;
//                            //收到420p
//                            vd420Temp = new YuvData(yuv420p, yuv420p.length, param.getWidthOUT(), param.getHeightOUT(), System.currentTimeMillis());
//                            encoderManager.encodeVideo(vd420Temp);
//                        }
//                        break;

                    case HANDLE_AUDIO_MSG:
                        byte[] pcm = (byte[]) msg.obj;
                        PCMData audioData = new PCMData(pcm);
                        encoderManager.encodeAudio(audioData);
                        break;
                }
            }
        };
    }

    ByteArrayOutputStream audioTmpByteOut = new ByteArrayOutputStream();

    private void notifyEncoderVideo(ICollectData gatherData) {
        if (!recording)
            return;
        Message msg = putEncoderHandler.obtainMessage(HANDLE_VIDEO_MSG);
        msg.obj = gatherData;
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
            infoText.setText(Html.fromHtml(getDisplayData().replaceAll("\n","<br>")));
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
        camera.release();
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

    private void initRender() {
        if (useSurfaceview) {
            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    //这里需要判断camera是不是camera2,surface不支持
                    camera.setRender(surfaceHolder);
                    camera.start();

                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    camera.stop();
                }
            });
        } else {
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                    camera.setRender(surfaceTexture);
                    camera.start();
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                    camera.stop();
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                }
            });
        }
    }

    IVideoCollecter camera;
    private void initCamera() {
        if (useCameraOne) {
            camera = new CameraCollecter(this);
            camera.init();
            VideoCollecterParam param = new VideoCollecterParam();
            param.setWidth(cameraWidth);
            param.setHeight(cameraHeight);
            param.setFormat(Yuv420p_I420);
            param.setConstantFps(true);
            param.setFps(24);
            camera.config(param);
            camera.setCallback(new IMediaCollecter.Callback() {
                @Override
                public void onCollectData(ICollectData data) {
                    if (recording) {
                        notifyEncoderVideo(data);
                    }
                }
            });

        } else {

            camera = new Camera2Collecter(this);
            camera.init();
            VideoCollecterParam param = new VideoCollecterParam();
            param.setWidth(cameraWidth);
            param.setHeight(cameraHeight);
            param.setFormat(Yuv420p_I420);
            param.setConstantFps(true);
            param.setFps(24);
            camera.config(param);
            camera.setCallback(new IMediaCollecter.Callback() {
                @Override
                public void onCollectData(ICollectData data) {
                    if (recording) {
                        notifyEncoderVideo(data);
                    }
                }
            });
//            camera2Helper.setOnRealFrameListener(new Camera2Helper.OnRealFrameListener() {
//                @Override
//                public void onRealFrame(Image image) {
////                    Log.d(TAG, "onRealFrame image w:" + image.getWidth() + " h:" + image.getHeight() + " time:" + image.getTimestamp());
//
//                    if (recording) {
//                        long start = System.currentTimeMillis();
////            Log.d(TAG, "onRealFrame: isMainThread:" + (Looper.myLooper() == Looper.getMainLooper()));
//
//                        notifyEncoderVideo(getByte(image.getPlanes()[0].getBuffer(),
//                                image.getPlanes()[1].getBuffer(),
//                                image.getPlanes()[2].getBuffer()));
////                        Log.d(TAG, "recording time:" + (System.currentTimeMillis() - start));
//
//                    }
//                }
//            });
//            camera2Helper.setRealTimeFrameSize(cameraWidth, cameraHeight);
//            camera2Helper.startCameraPreView();
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