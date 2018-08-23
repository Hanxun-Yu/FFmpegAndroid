package com.example.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yuhanxun
 * 2018/8/4
 * description:
 */
public class AudioWaveView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private final String TAG = "AudioWaveView_xunxun";
    private Thread thread; // SurfaceView通常须要自己单独的线程来播放动画
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;
    private boolean stop = true;
    private boolean isReady = false;

    private long startTime;

    public AudioWaveView(Context context) {
        this(context, null);
    }

    public AudioWaveView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public AudioWaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initPaint();
        this.surfaceHolder = this.getHolder();
        this.surfaceHolder.addCallback(this);
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        freshViewDimen();
        this.thread = new Thread(this);
        this.thread.start();
        stop = false;
        isReady = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        freshViewDimen();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        stop = true;
    }

    @Override
    public void run() {
        try {
            while (!stop) {
                if (isReady) {
                    canvas = this.surfaceHolder.lockCanvas(); // 通过lockCanvas加锁并得到該SurfaceView的画布
                    myDraw(canvas);
                    this.surfaceHolder.unlockCanvasAndPost(canvas); // 释放锁并提交画布进行重绘
                }
                try {
                    Thread.sleep(10); // 这个就相当于帧频了，数值越小画面就越流畅
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    Paint paintForAxis;
    Paint paintForBg;
    Paint paintForWave;

    private void initPaint() {
        paintForBg = new Paint();

        paintForAxis = new Paint();
        paintForAxis.setColor(getResources().getColor(R.color.audioTickmart));

        paintForWave = new Paint();
        paintForWave.setColor(getResources().getColor(R.color.audioWave));
        paintForWave.setAntiAlias(true);
        paintForWave.setStrokeWidth(2);

    }

    private void myDraw(Canvas canvas) {
        try {
            drawBG(canvas);
            drawAxis(canvas);
            drawWave(canvas);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private void drawAxis(Canvas canvas) {
        canvas.drawLine(0, dimen.tickmarkHeight, dimen.width, dimen.tickmarkHeight, paintForAxis);
        canvas.drawLine(0, dimen.height - dimen.tickmarkHeight, dimen.width,
                dimen.height - dimen.tickmarkHeight, paintForAxis);
        canvas.drawLine(0, dimen.heightHalf, dimen.width,
                dimen.heightHalf, paintForAxis);

        //刻度
        for (int i = 0; i < maxSecondRange+1; i++) {
//            int x = dimen.pixelPerMsec*100*i - removeCount
            //秒开始
            int x = (int) (dimen.pixelPerSecond*i - (removeCount % dimen.pixelPerSecond));
            if (x >= 0 && x <= dimen.waveWidth) {
                canvas.drawLine(x, dimen.height - dimen.tickmarkHeight, x,
                        (float) (dimen.height - dimen.tickmarkHeight * 1.5), paintForAxis);

                canvas.drawLine(x, dimen.tickmarkHeight, x,
                        (float) (dimen.tickmarkHeight * 1.5), paintForAxis);
            }


        }

    }

    private void drawBG(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
    }

    private synchronized void drawWave(Canvas canvas) {
        WaveVal waveValItem = null;
        int nextX = 0;
        int startY = dimen.heightHalf;
        for (int i = 0; i < data.size(); i++) {
            nextX = i;
            waveValItem = data.get(i);

            canvas.drawLine(nextX, startY, nextX, startY - waveValItem.positiveVal, paintForWave);
            canvas.drawLine(nextX, startY, nextX, startY + (-waveValItem.negativeVal), paintForWave);

        }
    }


    private void freshViewDimen() {
        dimen.width = getWidth();
        dimen.height = getHeight();
        dimen.widthHalf = getWidth() / 2;
        dimen.heightHalf = getHeight() / 2;
        dimen.tickmarkHeight = 20;
        dimen.waveWidth = dimen.width;
        dimen.waveHeight = dimen.height - 2 * dimen.tickmarkHeight;
        dimen.waveHeightHalf = dimen.waveHeight / 2;


        dimen.pixelPerMsec = dimen.width / (maxSecondRange * 1000);
        dimen.pixelPerSecond = dimen.width / maxSecondRange;
        dimen.msecPerPixel = maxSecondRange * 1000 / dimen.width;
        dimen.pcmValPerPixel = pcmValRangeAbs / dimen.waveHeightHalf;

        //1像素所需采样数 = 1像素对应的毫秒数x1毫秒采样数
        dimen.splCountNeededPerPixel = dimen.msecPerPixel * pcmSampleRate / 1000;

        Log.d(TAG, "dimen:" + dimen.toString());
    }

    ViewDimen dimen = new ViewDimen();

    class ViewDimen {
        int width;
        int height;
        int widthHalf;
        int heightHalf;

        //刻度线高度（上下都有，居上，居下）
        int tickmarkHeight;

        //可绘制波形区域
        int waveWidth;
        int waveHeight;
        int waveHeightHalf;


        //每个像素所占毫秒（横过来看，通过该值，取毫秒内采样值最大与最小值来绘制竖线）
        int msecPerPixel;

        //每个像素所对应的采样值(拿到采样值先除该值，得到需要绘制的竖线长度)
        int pcmValPerPixel;

        //计算每个像素时，所需要的采样数
        int splCountNeededPerPixel;

        //每毫秒占宽度
        double pixelPerMsec;
        int pixelPerSecond;

        @Override
        public String toString() {
            return "ViewDimen{" +
                    "width=" + width +
                    ", height=" + height +
                    ", widthHalf=" + widthHalf +
                    ", heightHalf=" + heightHalf +
                    ", tickmarkHeight=" + tickmarkHeight +
                    ", waveWidth=" + waveWidth +
                    ", waveHeight=" + waveHeight +
                    ", waveHeightHalf=" + waveHeightHalf +
                    ", msecPerPixel=" + msecPerPixel +
                    ", pcmValPerPixel=" + pcmValPerPixel +
                    ", splCountNeededPerPixel=" + splCountNeededPerPixel +
                    ", pixelPerMsec=" + pixelPerMsec +
                    ", pixelPerSecond=" + pixelPerSecond +
                    '}';
        }
    }

    //控件最大数据显示时间段（秒）
    final int maxSecondRange = 5;
    //最大采样值，范围-32767到32767
    final int pcmValRangeAbs = 32767;
    final int pcmValRangeMax = 32767;

    final int pcmSampleRate = 44100;

    List<WaveVal> data = new LinkedList<>();


    int tempPutCount = 0;
    short tempPutMax = 0;
    short tempPutMin = 0;

    private long removeCount = 0;

    public synchronized void putData(short sample) {
        if (!isReady)
            return;

        if (startTime == 0)
            startTime = System.currentTimeMillis();

//        Log.d(TAG,"putData sample:"+sample);
        tempPutCount++;
        if (tempPutCount < dimen.splCountNeededPerPixel) {
            tempPutMax = tempPutMax > sample ? tempPutMax : sample;
            tempPutMin = tempPutMin < sample ? tempPutMin : sample;
        } else {
            WaveVal waveVal = new WaveVal(tempPutMax, tempPutMin, System.currentTimeMillis());
            if (data.size() > dimen.waveWidth) {
                data.remove(0);
                removeCount++;
            }
//            Log.d(TAG, "addData waveVal:" + waveVal);
            data.add(waveVal);
            tempPutCount = 0;
            tempPutMax = 0;
            tempPutMin = 0;
        }
    }

    class WaveVal {
        long timestamp;
        int positiveVal;
        int negativeVal;

        public WaveVal(int positiveVal, int negativeVal, long timestamp) {
            this.positiveVal = positiveVal / dimen.pcmValPerPixel;
            this.negativeVal = negativeVal / dimen.pcmValPerPixel;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "WaveVal{" +
                    "positiveVal=" + positiveVal +
                    ", negativeVal=" + negativeVal +
                    '}';
        }
    }


}
