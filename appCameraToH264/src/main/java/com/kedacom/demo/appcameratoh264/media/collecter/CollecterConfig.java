package com.kedacom.demo.appcameratoh264.media.collecter;

import com.kedacom.demo.appcameratoh264.media.collecter.api.ICollecterParam;
import com.kedacom.demo.appcameratoh264.media.encoder.EncoderType;
import com.kedacom.demo.appcameratoh264.media.encoder.api.IEncoderParam;

/**
 * Created by yuhanxun
 * 2018/9/21
 * description:
 */
public class CollecterConfig {
    private CollecterType.Video video;
    private CollecterType.Audio audio;
    private ICollecterParam videoParam;
    private ICollecterParam audioParam;
    private String videoSavePath;
    private String audioSavePath;

    public CollecterType.Video getVideo() {
        return video;
    }

    public CollecterType.Audio getAudio() {
        return audio;
    }

    public ICollecterParam getVideoParam() {
        return videoParam;
    }

    public ICollecterParam getAudioParam() {
        return audioParam;
    }

    public String getVideoSavePath() {
        return videoSavePath;
    }


    public String getAudioSavePath() {
        return audioSavePath;
    }


    public static class Build {
        CollecterConfig ret;

        public Build() {
            ret = new CollecterConfig();
        }

        public Build setVideo(CollecterType.Video video) {
            ret.video = video;
            return this;
        }

        public Build setAudio(CollecterType.Audio audio) {
            ret.audio = audio;
            return this;
        }

        public Build setVideoParam(ICollecterParam videoParam) {
            ret.videoParam = videoParam;
            return this;
        }

        public Build setAudioParam(ICollecterParam audioParam) {
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

        public CollecterConfig build() {
            return ret;
        }
    }
}
