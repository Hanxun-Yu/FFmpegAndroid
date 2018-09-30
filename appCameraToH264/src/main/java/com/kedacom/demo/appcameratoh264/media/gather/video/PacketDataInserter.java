package com.kedacom.demo.appcameratoh264.media.gather.video;

/**
 * Created by yuhanxun
 * 2018/9/25
 * description:
 */
public class PacketDataInserter {

    private long firstInputTime = 0;

    private VideoGatherData lastPuttedVideoData;
    private int fps;
    private int fpsOffset;//累计帧间隔偏差
    private int normalInterval;//根据帧率算出正常帧间隔


    public PacketDataInserter(int fps) {
        this.fps = fps;
        this.normalInterval = 1000 / fps;
    }

    public void handleData(VideoGatherData videoData) {
//        Log.d(TAG,"putVideoData queueSize:"+videoQueue.size()+ " takeYUVCount:"+takeYUVCount);
        if (firstInputTime == 0)
            firstInputTime = System.currentTimeMillis();

        if (checkData(videoData)) {
            //正常加入帧
            if (inserterListener != null) {
                inserterListener.onNormalData(videoData);
            }
            lastPuttedVideoData = videoData;
        } else {
            //丢帧,来满足帧率,一般不会发生,因为Camera根据帧率要求设置了帧率范围的,不可能超过设置的值
            if (inserterListener != null) {
                inserterListener.onLoseData(videoData);
            }
        }

    }


    /**
     * 因为编码器固定帧率为25帧，采集帧率慢，导致播放速度过快，采集快则反之
     * 根据采集速度来判断，是需要补帧，还是需要丢帧
     *
     * @param videoData
     * @return
     */
    private boolean checkData(VideoGatherData videoData) {
        if (lastPuttedVideoData == null)
            return true;

        //当前帧与上一帧间隔
        int interval = (int) (videoData.getTimestamp() - lastPuttedVideoData.getTimestamp());
        //间隔与正常间隔差
        fpsOffset += interval - normalInterval;

//        Log.d(TAG,"fpsOffset:"+fpsOffset);
        boolean ret = true;
        try {
            if (fpsOffset > 0) {
                //采集慢,可能需要插帧
                while (Math.abs(fpsOffset) >= normalInterval) {
                    //间隔差累计到一帧间隔，插帧
                    VideoGatherData insertVideo = lastPuttedVideoData.clone();
                    insertVideo.setTimestamp(insertVideo.getTimestamp()+ normalInterval);

                    //多插入帧,来满足帧率
                    if (inserterListener != null) {
                        inserterListener.onInsertData(insertVideo);
                    }
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
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public interface InserterListener {
        void onInsertData(VideoGatherData data);

        void onNormalData(VideoGatherData data);

        void onLoseData(VideoGatherData data);
    }

    private InserterListener inserterListener;

    public void setInserterListener(InserterListener inserterListener) {
        this.inserterListener = inserterListener;
    }
}
