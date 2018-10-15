package com.kedacom.demo.appcameratoh264.media.collecter.audio;


import com.kedacom.demo.appcameratoh264.media.base.AudioChannel;
import com.kedacom.demo.appcameratoh264.media.base.AudioSampleRate;
import com.kedacom.demo.appcameratoh264.media.base.PCMData;
import com.kedacom.demo.appcameratoh264.media.base.PCMFormat;
import com.kedacom.demo.appcameratoh264.media.collecter.api.ICollecterParam;
import com.kedacom.demo.appcameratoh264.media.collecter.api.IMediaCollecter;

/**
 * Created by yuhanxun
 * 2018/10/9
 * description:
 */
public abstract class AbstractAudioCollecter implements IMediaCollecter {
    private Callback callback;
    protected AudioCollecterParam collecterParam;

    @Override
    public void config(ICollecterParam param) {
        if (param instanceof AudioCollecterParam) {
            AudioCollecterParam vParam = (AudioCollecterParam) param;


            if(getSupportFormat(vParam.getFormat()) == null)
                throw new IllegalArgumentException("Format "+ vParam.getFormat()+ " is not supported!");
            if(!isChannelSupported(vParam.getChannel())) {
                throw new IllegalArgumentException("Channel "+ vParam.getChannel()+ " is not supported!");
            }
            if(!isSampleRateSupported(vParam.getSampleRate())) {
                throw new IllegalArgumentException("SampleRate "+ vParam.getSampleRate()+ " is not supported!");
            }
            _config(vParam);
        } else {
            throw new IllegalArgumentException(param + " is not supported");
        }
    }

    protected abstract void _config(AudioCollecterParam param);

    protected abstract boolean isFormatSupported(PCMFormat pcmFormat);
    protected abstract boolean isChannelSupported(AudioChannel pcmFormat);
    protected abstract boolean isSampleRateSupported(AudioSampleRate pcmFormat);


    protected void notifyRTFrame(byte[] data, int length, PCMFormat format,AudioChannel audioChannel,AudioSampleRate audioSampleRate) {
        PCMData pcmData = new PCMData(data, length, format,audioSampleRate,audioChannel);
        AudioCollectData ret = new AudioCollectData(pcmData, System.currentTimeMillis());
        doCallback(ret);
    }

    protected PCMFormat getSupportFormat(PCMFormat expectFormat) {
        PCMFormat ret = null;
        if (isFormatSupported(expectFormat)) {
            ret = expectFormat;
        }
        return ret;
    }

    private void doCallback(AudioCollectData ret) {
        if (callback != null)
            callback.onCollectData(ret);
    }


    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}
