package com.kedacom.demo.appcameratoh264.media.encoder;

import android.os.Bundle;

import com.kedacom.demo.appcameratoh264.media.encoder.api.IEncoderParam;

/**
 * Created by yuhanxun
 * 2018/9/21
 * description:
 */
public class EncoderConfig {
    private EncoderType.Video video;
    private EncoderType.Audio audio;
    private IEncoderParam videoParam;
    private IEncoderParam audioParam;
    private String videoSavePath;
    private String audioSavePath;

    public EncoderType.Video getVideo() {
        return video;
    }


    public EncoderType.Audio getAudio() {
        return audio;
    }


    public IEncoderParam getVideoParam() {
        return videoParam;
    }


    public IEncoderParam getAudioParam() {
        return audioParam;
    }


    public String getVideoSavePath() {
        return videoSavePath;
    }


    public String getAudioSavePath() {
        return audioSavePath;
    }


    public static class Build {
        EncoderConfig ret;

        public Build() {
            ret = new EncoderConfig();
        }

        public Build setVideo(EncoderType.Video video) {
            ret.video = video;
            return this;
        }

        public Build setAudio(EncoderType.Audio audio) {
            ret.audio = audio;
            return this;
        }

        public Build setVideoParam(IEncoderParam videoParam) {
            ret.videoParam = videoParam;
            return this;
        }

        public Build setAudioParam(IEncoderParam audioParam) {
            ret.audioParam = audioParam;
            return this;
        }

        public Build setVideoSavePath(String videoSavePath) {
            ret.videoSavePath = videoSavePath;
            return this;
        }

        public Build setAudioSavePath(String audioSavePath) {
            ret.audioSavePath = audioSavePath;
            return this;
        }

        public EncoderConfig build() {
            return ret;
        }
    }
}
