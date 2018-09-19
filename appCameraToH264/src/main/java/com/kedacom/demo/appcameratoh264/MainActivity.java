package com.kedacom.demo.appcameratoh264;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.media.FaceDetector;
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
import com.google.gson.Gson;
import com.kedacom.demo.appcameratoh264.jni.X264Param;
import com.kedacom.demo.appcameratoh264.jni.YuvUtil;
import com.kedacom.demo.appcameratoh264.media.Camera1Helper;
import com.kedacom.demo.appcameratoh264.media.Camera2Helper;
import com.kedacom.demo.appcameratoh264.media.audio.AudioData;
import com.kedacom.demo.appcameratoh264.media.audio.AudioRecoderManager;
import com.kedacom.demo.appcameratoh264.media.video.MediaEncoder;
import com.kedacom.demo.appcameratoh264.media.video.MediaEncoder2Codec;
import com.kedacom.demo.appcameratoh264.media.video.VideoData420;
import com.kedacom.demo.appcameratoh264.widget.AutoFitTextureView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements
        MediaEncoder.MediaEncoderCallback {
    private AutoFitTextureView textureView;
    private SurfaceView surfaceView;
    private Button recordBtn;
    private Button stopBtn;
    private TextView infoText;
    private TextView memoryText;
    private TextView paramText;
    private TextView timeText;

    final String TAG = "MainActivity_xunxun";
    private Camera2Helper camera2Helper;
    private Camera1Helper camera1Helper;
//    MediaEncoder mediaEncoder;
    MediaEncoder2Codec mediaEncoder;
    private AudioWaveView audioWaveView;
    boolean useCameraOne = false;
    boolean useSurfaceview = false;
    boolean usePortrait = false;
    int widthIN = 1280;
    int heightIN = 720;
    int widthOUT = 1280;
    int heightOUT = 720;

    private AudioRecoderManager audioGathererManager;

    String muxerFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int camera = getIntent().getIntExtra("camera", 0);
        int render = getIntent().getIntExtra("render", 0);
        int orientation = getIntent().getIntExtra("orientation", 0);
        int codec = getIntent().getIntExtra("codec", 0);
        int muxer = getIntent().getIntExtra("muxer", 0);
        muxerFormat = muxer == 0 ? "mp4" : "mkv";

        useCameraOne = camera == 0;
        useSurfaceview = render == 0;
        usePortrait = orientation == 0;

        if (!usePortrait)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        if (codec == 0) {
            //x264
            param = getIntent().getParcelableExtra("param");
            widthIN = param.getWidthIN();
            heightIN = param.getHeightIN();
            widthOUT = param.getWidthOUT();
            heightOUT = param.getHeightOUT();
        }

        init();
        initCamera();
        initMicroPhone();
        initCheckFaceThread();
//        initFile();
//        initSimpleWaveform();
    }

    X264Param param;
    long startTime;

    private void init() {
        mediaEncoder = new MediaEncoder2Codec();
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
//        progressBar= (ProgressBar) findViewById(R.id.progressbar_loading);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                camera2Helper.takePicture();
//                camera2Helper.startCallbackFrame();
                if (camera1Helper.getDisplayOrientation() == 90 || camera1Helper.getDisplayOrientation() == 270) {
                    int temp = param.getWidthIN();
                    param.setWidthIN(param.getHeightIN());
                    param.setHeightIN(temp);
                    temp = param.getWidthOUT();
                    param.setWidthOUT(param.getHeightOUT());
                    param.setHeightOUT(temp);
                }
                if (mediaEncoder.start(param)) {
                    recording = true;
                    startTime = System.currentTimeMillis();
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
                audioGathererManager.stopAudioIn();
//                camera2Helper.stopCallbackFrame();
            }
        });
        showParams();
    }


    private void initMicroPhone() {
        audioGathererManager = new AudioRecoderManager();
        audioGathererManager.setAudioDataListener(new AudioRecoderManager.AudioDataListener() {
            @Override
            public void audioData(byte[] data) {
//                Log.d(TAG,"audioData data size:"+data.length);
                if (recording) {
                    notifyEncoderAudio(data);
                    notifyWave(data);
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

//                    if(checkFaceHandler != null)
//                        checkFaceHandler.obtainMessage(0,bytes).sendToTarget();
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
//        if (useCameraOne) {
//            if (camera1Helper.getDisplayOrientation() == 90 || camera1Helper.getDisplayOrientation() == 270) {
//                mediaEncoder.setMediaSize(x264Param.getHeightIN(),
//                        x264Param.getWidthIN(), x264Param.getHeightOUT(),
//                        x264Param.getWidthOUT(), x264Param.getBitrate());
//            } else {
//                mediaEncoder.setMediaSize(widthIN, heightIN, widthOUT, heightOUT, videoBitrate);
//            }
//        } else {
//            mediaEncoder.setMediaSize(widthIN, heightIN, widthOUT, heightOUT, videoBitrate);
//        }
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

        mediaEncoder.setOnEncoderChangedListener(new MediaEncoder.OnEncoderChangedListener() {
            @Override
            public void onChanged(MediaEncoder.EncoderState state) {
                Log.d(TAG, "Encoder state V:" + state.getVideoEncoderState() + " A:" + state.getAudioEncoderState() +
                        " Mux:" + state.getMuxEncoderState());
                if (state.getAudioEncoderState() == MediaEncoder.State.IDLE
                        && state.getVideoEncoderState() == MediaEncoder.State.IDLE) {
                    mediaEncoder.mux(MediaEncoder.MuxType.valueOf(muxerFormat));
                }
            }
        });
        initEncoderThread();
    }

    HandlerThread handlerThread;
    Handler putEncoderHandler;
    HandlerThread checkFaceThread;
    Handler checkFaceHandler;

    private void initCheckFaceThread() {
        checkFaceThread = new HandlerThread("checkFace");
        checkFaceThread.start();
        checkFaceHandler = new Handler(checkFaceThread.getLooper()) {
            int incremental = 0;
            int frequence = 20;

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
//                Log.d(TAG,"incremental:"+incremental);
                if (incremental % frequence == 0) {
                    checkFace((byte[]) msg.obj, widthIN, heightIN);
                    if (incremental == frequence)
                        incremental = 0;
                }
                incremental++;

            }
        };
    }


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
//                            vd420Temp = new VideoData420(yuv420sp, widthOUT, heightOUT, System.currentTimeMillis());
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

    private void notifyWave(byte[] audioData) {
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
//                short right = (short) ((u_right1 << 8) | u_right2);


//                Log.d(TAG, "audio_left 1:" + Integer.toHexString(u_left1)
//                        + " 2:" + Integer.toHexString(u_left2)
//                        + " 1&2:" + Integer.toHexString(left) + " dex:" + left);
//                Log.d(TAG," dex:" + left);
//                Log.d(TAG, "audio_right 1:" + Integer.toHexString(u_right1)
//                        + " 2:" + Integer.toHexString(u_right2)
//                        + " 1&2:" + Integer.toHexString(right) + " dex:" + right);
            notigyWaveView(left);
        }
//            Log.d(TAG, "audioData:" + size);
//        }
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
//        try {
//            fileOutputStream.write(videoData);
//            writer.write(String.valueOf(totalLength)+"\n");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        count++;
    }

//    //------------------------记录每一帧的大小---------------------------
//    File file = new File("/sdcard/h264.264");
//    File file2 = new File("/sdcard/h264_len.txt");
//    FileOutputStream fileOutputStream;
//    FileWriter writer;
//    private void initFile() {
//        try {
//            fileOutputStream = new FileOutputStream(file);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        try {
//            writer = new FileWriter(file2);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void closeFile(){
//        try {
//            fileOutputStream.flush();
//            writer.flush();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                fileOutputStream.close();
//                writer.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
    //------------------------------------------------------------------


    @Override
    protected void onStop() {
        super.onStop();
//        closeFile();
    }

    @Override
    public void receiveEncoderAudioData(byte[] audioData, int size) {
//        Log.d(TAG, "receiveEncoderAudioData size:" + size);

    }


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

        buffer.append("----------SIZE----------\n");
        buffer.append("h264:");
        buffer.append(mediaEncoder.getVideoEncodedSize());
        buffer.append(" aac:");
        buffer.append(mediaEncoder.getAudioEncodedSize());
        buffer.append("\n");
        //----------------输入输出数--------------------------
        buffer.append("-----------IO------------\n");
        buffer.append("pcm Put:");
        buffer.append(mediaEncoder.getPutPCMCount());
        buffer.append(" Take:");
        buffer.append(mediaEncoder.getTakePCMCount());
        buffer.append("\n");
        buffer.append("yuv Put:");
        buffer.append(mediaEncoder.getPutYUVCount());
        buffer.append(" Take:");
        buffer.append(mediaEncoder.getTakeYUVCount());
        buffer.append("\n");
        buffer.append("aacOUT:");
        buffer.append(mediaEncoder.getRecvAACCount());
        buffer.append("\n");
        buffer.append("h264OUT:");
        buffer.append(mediaEncoder.getRecvH264Count());
        buffer.append("\n");
        buffer.append("queue v:");
        buffer.append(mediaEncoder.getWaitVideoEncodedQueueSize());
        buffer.append(" a:");
        buffer.append(mediaEncoder.getWaitAudioEncodedQueueSize());
        buffer.append("\n");
        //---------------帧率--------------------------------
        buffer.append("-----------FPS-----------\n");
        buffer.append("camera:");
        buffer.append(mediaEncoder.getCamFPS());
        buffer.append("\n");
        buffer.append("yuv:");
        buffer.append(mediaEncoder.getYuvFPS());
        buffer.append("\n");
        buffer.append("h264:");
        buffer.append(mediaEncoder.getH264FPS());
        buffer.append("\n");
        buffer.append("pcm:");
        buffer.append(mediaEncoder.getPcmFPS());
        buffer.append("\n");
        buffer.append("aac:");
        buffer.append(mediaEncoder.getAacFPS());
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
        sb.append("bitrate:");
        sb.append(param.getBitrate());
        sb.append("Kbit\n");
        sb.append("bitrateCtrl:");
        sb.append(param.getBitrateCtrl());
        sb.append("\n");
        sb.append("fps:");
        sb.append(param.getFps());
        sb.append("\n");
        sb.append("GOP:");
        sb.append(param.getGop());
        sb.append("\n");
        sb.append("B帧:");
        sb.append(param.getbFrameCount());
        sb.append("\n");
        sb.append("profile:");
        sb.append(param.getProfile());
        sb.append("\n");
        sb.append("preset:");
        sb.append(param.getPreset());
        sb.append("\n");
        sb.append("tune:");
        sb.append(param.getTune());
        sb.append("\n");
        paramText.setText(sb.toString());
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
        audioGathererManager.release();
    }

    LinkedList<Integer> ampList = new LinkedList<>();

    private void notigyWaveView(final short val) {
//        Log.d(TAG,"notigyWaveView:"+val);
        audioWaveView.putData(val);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                ampList.add((int) val);
//                simpleWaveform.refresh();
//            }
//        });
    }

    private void checkFace(byte[] yuv, int width, int height) {
        YuvImage image = new YuvImage(yuv, ImageFormat.NV21, width, height, null);
        if (yuv != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, width, height), 50, out);
            byte[] datas = out.toByteArray();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap mBitmap = BitmapFactory.decodeByteArray(datas, 0, datas.length, options);
            //FileUtil.saveBitmap("cccc/"+l+"ee.png", mBitmap);
            Matrix matrix = new Matrix();
            //matrix.postRotate((float)90);
            matrix.postScale(0.4f, 0.3125f); //照片的大小使 1280*960 屏幕的大小使 1024*600 这里需要注意换算比例
            // Logger.v("MyCameraManager faceCheckFlag");
            //synchronized (this) {
            //Logger.v("MyCameraManager synchronized");
            //if(faceCheckFlag){
            //setFaceCheckFlag(false);
            //Logger.v("MyCameraManager synchronized" + errornum + mBitmap.getWidth() + "dd " + mBitmap.getHeight());
            Bitmap bitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);
            FaceDetector mFaceDetector = new FaceDetector(width, height, 5);
            FaceDetector.Face[] mFace = new FaceDetector.Face[5];
            int faceResult = mFaceDetector.findFaces(mBitmap, mFace);

//            if (faceResult != 0) {
            Log.d(TAG, "findFace:" + new Gson().toJson(mFace));
//            }
            mBitmap.recycle();
            bitmap.recycle();
        }
    }
}