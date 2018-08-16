package com.example.apph264render.data.file;

import com.example.apph264render.api.IDataGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by yuhanxun
 * 2018/8/14
 * description:
 */
public class H264FileGenerator implements IDataGenerator {
    private String h264FilePath;
    private String lenFilePath;
    private boolean loop;
    private OnDataReceiverListener listener;
    private ReadFileRunn readFileRunn;
    private Thread thread;

    String TAG = getClass().getSimpleName() + "_xunxun";

    public H264FileGenerator(String h264FilePath, String lenFilePath,boolean loop) {
        this.h264FilePath = h264FilePath;
        this.lenFilePath = lenFilePath;
        this.loop = loop;
    }

    @Override
    public void init() {
        readFileRunn = new ReadFileRunn(h264FilePath, lenFilePath);
    }

    @Override
    public void start() {
        thread = new Thread(readFileRunn);
        thread.start();
    }

    @Override
    public void stop() {
        readFileRunn.setStop(true);
        try {
            thread.join(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setOnDataListener(OnDataReceiverListener listener) {
        this.listener = listener;
    }


    class ReadFileRunn implements Runnable {
        File file264;
        File file264Len;

        public ReadFileRunn(String h264FilePath, String lenFilePath) {
            file264 = new File(h264FilePath);
            file264Len = new File(lenFilePath);
        }

        BufferedReader file264LenBufReader;
        FileInputStream file264Input;
        boolean isStop = false;

        public void setStop(boolean stop) {
            isStop = stop;
        }

        @Override
        public void run() {
            try {
                file264Input = new FileInputStream(file264);
                file264LenBufReader = new BufferedReader(new FileReader(file264Len));


                while (!isStop) {
                    String len = file264LenBufReader.readLine();
                    if (len == null) {
                        if(!loop)
                            break;
                        file264LenBufReader.close();
                        file264Input.close();
                        file264Input = new FileInputStream(file264);
                        file264LenBufReader = new BufferedReader(new FileReader(file264Len));
                        continue;
                    } else {
                        int lenInt = Integer.parseInt(len);
                        byte[] data264 = new byte[lenInt];
                        file264Input.read(data264, 0, lenInt);

                        listener.onReceiveData(data264, lenInt);
                    }

                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
