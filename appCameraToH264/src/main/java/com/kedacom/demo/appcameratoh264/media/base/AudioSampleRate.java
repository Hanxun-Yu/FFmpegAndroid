package com.kedacom.demo.appcameratoh264.media.base;

public enum AudioSampleRate {
    SR_44100(44100);
    private int data;

    AudioSampleRate(int data) {
        this.data = data;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }
}