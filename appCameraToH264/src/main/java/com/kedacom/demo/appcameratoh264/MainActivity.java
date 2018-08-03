package com.kedacom.demo.appcameratoh264;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
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
import com.kedacom.demo.appcameratoh264.media.video.MediaEncoder;
import com.kedacom.demo.appcameratoh264.media.video.VideoData420;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements
        Camera2Helper.AfterDoListener, MediaEncoder.MediaEncoderCallback {
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
    int widthIN = 1280;
    int heightIN = 720;
    int widthOUT = 1280;
    int heightOUT = 720;
//    int widthIN = 720;
//    int heightIN = 1280;
//    int widthOUT = 720;
//    int heightOUT = 1280;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int camera = getIntent().getIntExtra("camera", 0);
        int render = getIntent().getIntExtra("render", 0);

        useCameraOne = camera == 0;
        useSurfaceview = render == 0;

        init();
        initCamera();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        camera2Helper.onDestroyHelper();
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
    }

    private void initCamera() {
        if (useCameraOne) {
            camera1Helper = new Camera1Helper(this, widthIN, heightIN);
            camera1Helper.setOnRealFrameListener(new Camera1Helper.OnRealFrameListener() {
                @Override
                public void onRealFrame(byte[] bytes) {
                    if (recording) {
                        notifyEncoder(bytes);
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
                        camera1Helper.releaseCamera();
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
                        camera1Helper.releaseCamera();
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

                        notifyEncoder(getByte(image.getPlanes()[0].getBuffer(),
                                image.getPlanes()[1].getBuffer(),
                                image.getPlanes()[2].getBuffer()));
//                        Log.d(TAG, "recording time:" + (System.currentTimeMillis() - start));

                    }
                }
            });
            camera2Helper.setRealTimeFrameSize(widthIN, heightIN);
            camera2Helper.startCameraPreView();
            camera2Helper.setAfterDoListener(this);
            initEncoder();

        }
    }

    private void initEncoder() {
        //写死了，应该与widthin and heightin应该与camera内yuv一致
        if(useCameraOne) {
            if (camera1Helper.getDisplayOrientation() == 90 || camera1Helper.getDisplayOrientation() == 270) {
                mediaEncoder.setMediaSize(heightIN, widthIN, heightOUT, widthOUT, 2048);
            } else {
                mediaEncoder.setMediaSize(widthIN, heightIN, widthOUT, heightOUT, 2048);
            }
        } else {
            mediaEncoder.setMediaSize(widthIN, heightIN, widthOUT, heightOUT, 2048);
        }
        mediaEncoder.setsMediaEncoderCallback(this);
        mediaEncoder.startVideoEncode();
        initEncoderThread();
    }

    HandlerThread handlerThread;
    Handler putEncoderHandler;


    private void initEncoderThread() {
        handlerThread = new HandlerThread("putEncoderThread");
        handlerThread.start();
        putEncoderHandler = new Handler(handlerThread.getLooper()) {
            VideoData420 vd420Temp;
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
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

    byte[] cpy;
    Message msg;

    private void notifyEncoder(byte[] bytes) {
        cpy = new byte[bytes.length];
        System.arraycopy(bytes, 0, cpy, 0, bytes.length);
        msg = putEncoderHandler.obtainMessage();
        msg.obj = cpy;
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


    @Override
    public void receiveEncoderVideoData(byte[] videoData, int totalLength, int[] segment) {
//        Log.d(TAG, "recv h264 len:" + totalLength + " nalCount:" + segment.length);
        runOnUiThread(runnable);
    }

    int count = 0;
    int frequence = 8;
    Runnable runnable = new Runnable() {
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            if (count % frequence == 0) {
                float[] memory = getMemory();
                DecimalFormat fnum = new DecimalFormat("##0.00");
                infoText.setText(
                        (useCameraOne ? "Camera" : "Camera2") + "\n"
                                + (useSurfaceview ? "SurfaceView" : "TextureView") + "\n"
                                + "size:" + mediaEncoder.getEncodedSize() + "\n"
                                + "putYUV:" + mediaEncoder.getPutYUVCount() + "\n"
                                + "recvH264:" + mediaEncoder.getRecvH264Count() + "\n"
                                + "yuvFPS:" + mediaEncoder.getYuvFPS() + "\n"
                                + "h264FPS:" + mediaEncoder.getH264FPS() + "\n"
                                + "---------memory---------\n"
                                + "max:" + fnum.format(memory[0]) + "\n"
                                + "maxHeap:" + fnum.format(memory[3]) + "\n"

                                + "malloc:" + fnum.format(memory[1]) + "\n"
                                + "free:" + fnum.format(memory[2]));
                if (count == frequence)
                    count = 0;
            }
            count++;
        }
    };


    @Override
    public void receiveEncoderAudioData(byte[] audioData, int size) {

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
}