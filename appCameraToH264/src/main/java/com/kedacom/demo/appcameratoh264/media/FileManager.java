package com.kedacom.demo.appcameratoh264.media;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

public class FileManager {

    public static final String TEST_PCM_FILE = "/sdcard/264/123.pcm";
    public static final String TEST_WAV_FILE = "/sdcard/264/123.wav";
    public static final String TEST_YUV_FILE = "/sdcard/264/123.yuv";
    public static final String TEST_H264_FILE = "/sdcard/264/123.h264";
    public static final String TEST_AAC_FILE = "/sdcard/264/123.aac";
    private String fileName;
    private FileOutputStream fileOutputStream;
    private boolean testForWrite = true;
    final String TAG = "FileManager_xunxun";

    public FileManager(String fileName) {
        this.fileName = fileName;
    }

    private synchronized void internalOpenFile(String name) {
        try {
            File file = new File(fileName);
            file.mkdirs();
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            isClosed = false;
        } catch (Exception e) {
            Log.d(TAG, "create file failed");
            e.printStackTrace();
        }
    }

    public void openFile() {
        internalOpenFile(this.fileName);
    }

//    public void saveFileData(byte[] data, int offset, int length) {
//        try {
//            fileOutputStream.write(data, offset, length);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    boolean isClosed = false;
    public synchronized void saveFileData(byte[] data) {
        if(!isClosed) {
            try {
                fileOutputStream.write(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void closeFile() {
        isClosed = true;
        try {
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
