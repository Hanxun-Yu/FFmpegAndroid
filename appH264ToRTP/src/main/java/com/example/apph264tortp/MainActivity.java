package com.example.apph264tortp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.apph264tortp.jni.FFmpegJni;

public class MainActivity extends AppCompatActivity {

    Button startBtn;
    Button stopBtn;
    Button putBtn;

    FFmpegJni fFmpegJni = new FFmpegJni();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fFmpegJni.init("ip",1000);

        initView();
        biz();
    }

    private void initView() {
        startBtn = findViewById(R.id.start);
        stopBtn = findViewById(R.id.stop);
        putBtn = findViewById(R.id.put);
    }

    private void biz() {
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fFmpegJni.start();
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fFmpegJni.stop();
            }
        });
        putBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fFmpegJni.putH264(new byte[20],30);
            }
        });
    }
}
