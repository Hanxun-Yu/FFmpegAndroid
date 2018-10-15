package com.kedacom.demo.appcameratoh264;

import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.widget.AudioWaveView;
import com.kedacom.demo.appcameratoh264.media.encoder.video.VideoEncoderParam;
import com.kedacom.demo.appcameratoh264.ui.main.IMainView;
import com.kedacom.demo.appcameratoh264.ui.main.MainPresenter;
import com.kedacom.demo.appcameratoh264.ui.render.IRenderView;
import com.kedacom.demo.appcameratoh264.widget.AutoFitTextureView;
import com.kedacom.demo.common.android.base.BaseAppCompatActivity;
import com.orhanobut.logger.Logger;


public class MainActivity_EncoderController extends BaseAppCompatActivity implements IMainView{
    private AutoFitTextureView textureView;
    private SurfaceView surfaceView;
    private Button recordBtn;
    private Button stopBtn;
    private TextView collecterStateText;
    private TextView encoderStateText;
    private TextView collecterParamText;
    private TextView encoderParamText;
    private TextView memoryInfoText;
    private TextView timeText;

    private AudioWaveView audioWaveView;
    boolean useSurfaceview = false;
    boolean usePortrait = false;

    private MainPresenter presenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.e("onCreate");
        setContentView(R.layout.activity_main);

        //获取横竖屏,渲染层信息

        //从service获取横竖屏方式,以及知道使用哪个渲染组件




//        int camera = getIntent().getIntExtra("camera", 0);
        int render = getIntent().getIntExtra("render", 0);
        int orientation = getIntent().getIntExtra("orientation", 0);
//        codec = getIntent().getIntExtra("codec", 0);
//        int muxer = getIntent().getIntExtra("muxer", 0);
//        muxerFormat = muxer == 0 ? "mp4" : "mkv";
//
//        useCameraOne = camera == 0;
        useSurfaceview = render == 0;
        usePortrait = orientation == 0;

        if (!usePortrait)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        initView();

        //Presenter
        presenter = new MainPresenter(this);

    }

    Object render;
    private void initView() {
        textureView = findViewById(R.id.textureview);
        surfaceView = findViewById(R.id.surfaceview);
        audioWaveView = findViewById(R.id.audioWaveView);
        if (useSurfaceview) {
            render = surfaceView;
            textureView.setVisibility(View.GONE);
        } else {
            render = textureView;
            surfaceView.setVisibility(View.GONE);
        }

        recordBtn = findViewById(R.id.recordBtn);
        stopBtn = findViewById(R.id.stopBtn);
        collecterStateText = findViewById(R.id.collecter_state_text);
        collecterParamText = findViewById(R.id.collecter_param_text);
        encoderStateText = findViewById(R.id.encoder_state_text);
        encoderParamText = findViewById(R.id.encoder_param_text);
        timeText = findViewById(R.id.timeText);
        memoryInfoText = findViewById(R.id.memoryText);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.startWork();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.stopWork();
            }
        });
        setRenderCallback(render);
    }


    private void setRenderCallback(Object render) {
        if(render instanceof SurfaceView) {
            ((SurfaceView) render).getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    if(onRenderListener != null)
                        onRenderListener.onRenderChanged(IRenderView);
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {

                }
            });
        } else if(render instanceof TextureView) {
            ((TextureView) render).setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }
            });
        }


    }


    @Override
    public void showWave(short bytes) {
        audioWaveView.putData(bytes);
    }


    IRenderView.IRenderCallback onRenderListener;

    @Override
    public void showCollecterParam(String data) {
        collecterParamText.setText(data);
    }

    @Override
    public void showEncoderParam(String data) {
        encoderParamText.setText(data);
    }

    @Override
    public void showCollecterState(String data) {
        collecterStateText.setText(data);
    }

    @Override
    public void showEncoderState(String data) {
        encoderStateText.setText(data);

    }

    @Override
    public void showMemoryStatus(String data) {
        memoryInfoText.setText(data);
    }

    @Override
    public void showWorkingTime(String time) {
        timeText.setText(time);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(iViewLife != null)
            iViewLife.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(iViewLife != null)
            iViewLife.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(iViewLife != null)
            iViewLife.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(iViewLife != null)
            iViewLife.onResume();
    }

    IViewLife iViewLife;
    @Override
    public void setOnViewChangedListener(IViewLife viewChangedListener) {
        this.iViewLife = viewChangedListener;
    }
}