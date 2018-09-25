package com.kedacom.demo.appcameratoh264.media.encoder.video;

import com.kedacom.demo.appcameratoh264.media.encoder.api.IMediaEncoder;

/**
 * Created by yuhanxun
 * 2018/9/25
 * description:
 */
public class YuvInserter {

    private long firstInputTime = 0;

    //摄像头的YUV420P数据，put到队列中，生产者模型
    YuvData lastPuttedVideoData;
    IMediaEncoder encoder;
    int fps;
    int fpsOffset;//累计帧间隔偏差
    int normalInterval;//根据帧率算出正常帧间隔


    public YuvInserter(IMediaEncoder encoder, int fps) {
        this.encoder = encoder;
        this.fps = fps;
        this.normalInterval = 1000 / fps;
    }

    public void putVideoData(YuvData videoData) {
//        Log.d(TAG,"putVideoData queueSize:"+videoQueue.size()+ " takeYUVCount:"+takeYUVCount);
        if (firstInputTime == 0)
            firstInputTime = System.currentTimeMillis();

        if (checkInsertVideo(videoData))
            doPutVideoData(videoData);

    }

    private void doPutVideoData(YuvData videoData) {
//        Log.d(TAG,"videoQueue.size():"+videoQueue.size());
//        Log.d(TAG, "doPutVideoData videoQueue size:"+videoQueue.size());
        encoder.putPacket(videoData);
        lastPuttedVideoData = videoData;
    }


    /**
     * 因为编码器固定帧率为25帧，采集帧率慢，导致播放速度过快，采集快则反之
     * 根据采集速度来判断，是需要补帧，还是需要丢帧
     *
     * @param videoData
     * @return
     */
    private boolean checkInsertVideo(YuvData videoData) {
        if (lastPuttedVideoData == null)
            return true;
        //当前帧与上一帧间隔
        int interval = (int) (videoData.getTimestampMilliSec() - lastPuttedVideoData.getTimestampMilliSec());
        //间隔与正常间隔差
        fpsOffset += interval - normalInterval;

//        Log.d(TAG,"fpsOffset:"+fpsOffset);
        boolean ret = true;
        if (fpsOffset > 0) {
            //采集慢,可能需要插帧
            while (Math.abs(fpsOffset) >= normalInterval) {
                //间隔差累计到一帧间隔，插帧
                YuvData insertVideo = new YuvData(lastPuttedVideoData.getData(), lastPuttedVideoData.getLenght(),
                        lastPuttedVideoData.width, lastPuttedVideoData.height, lastPuttedVideoData.getTimestampMilliSec() + normalInterval);
                doPutVideoData(insertVideo);
                fpsOffset -= normalInterval;
//                Log.d(TAG, "++insert_video_fpsOffset:" + fpsOffset);
            }
        } else {
            //采集快,需要丢帧
            if (Math.abs(fpsOffset) >= normalInterval) {
                //间隔差累计到一帧间隔，丢
                fpsOffset += normalInterval;
                ret = false;
//                Log.d(TAG, "--skip_video fpsOffset:" + fpsOffset);
            }
        }
        return ret;
    }
}
