package com.kedacom.demo.appcameratoh264.media.base;

/**
 * Created by yuhanxun
 * 2018/10/9
 * description:
 */
public class PCMData  implements Cloneable{
    private byte[] data;
    private int length;
    private PCMFormat format;
    private AudioSampleRate audioSampleRate;
    private AudioChannel audioChannel;

    public PCMData(byte[] data, int length, PCMFormat format, AudioSampleRate audioSampleRate,
                   AudioChannel audioChannel) {
        this.data = data;
        this.length = length;
        this.format = format;
        this.audioSampleRate = audioSampleRate;
        this.audioChannel = audioChannel;
    }

    public AudioSampleRate getAudioSampleRate() {
        return audioSampleRate;
    }

    public void setAudioSampleRate(AudioSampleRate audioSampleRate) {
        this.audioSampleRate = audioSampleRate;
    }

    public AudioChannel getAudioChannel() {
        return audioChannel;
    }

    public void setAudioChannel(AudioChannel audioChannel) {
        this.audioChannel = audioChannel;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public PCMFormat getFormat() {
        return format;
    }

    public void setFormat(PCMFormat format) {
        this.format = format;
    }

    public PCMData clone() throws CloneNotSupportedException {
        return (PCMData) super.clone();
    }
}
