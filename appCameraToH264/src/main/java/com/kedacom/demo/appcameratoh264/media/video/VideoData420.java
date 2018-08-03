package com.kedacom.demo.appcameratoh264.media.video;

public class VideoData420 {
    public byte[] videoData;
    public int width;
    public int height;
    public long timestamp;

    public VideoData420(byte[] videoData, int width, int height,long timestamp) {
        this.videoData = videoData;
        this.width = width;
        this.height = height;
        this.timestamp = timestamp;
    }
}
