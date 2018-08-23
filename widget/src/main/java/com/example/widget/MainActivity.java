package com.example.widget;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    AudioWaveView audioWaveView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioWaveView = (AudioWaveView) findViewById(R.id.audioWaveView);
        new Thread(new TestDataRunn()).start();
    }

    class TestDataRunn implements Runnable {

        @Override
        public void run() {
            while (true) {
                int i = 0;
                Random random = new Random();
                while (i < 44) {
                    i++;
                    audioWaveView.putData((short) (-25000+random.nextInt(33333)));
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
