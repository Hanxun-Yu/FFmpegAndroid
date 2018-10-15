package com.kedacom.demo.appcameratoh264.ui.main;

import com.kedacom.demo.appcameratoh264.ui.IView;
import com.kedacom.demo.appcameratoh264.ui.render.IRenderView;

/**
 * Created by yuhanxun
 * 2018/10/9
 * description:
 */
public interface IMainView extends IView {
    /**
     * 显示波形
     * @param data
     */
    void showWave(short data);

    /**
     * 渲染监听器
     * @param onRenderChangedListener
     */
    void setOnRenderChangedListener(IRenderView.OnRenderListener onRenderChangedListener);

    /**
     * 显示采集器参数
     */
    void showCollecterParam(String data);

    /**
     * 显示编码器参数
     */
    void showEncoderParam(String data);

    /**
     * 显示采集器状态
     */
    void showCollecterState(String data);

    /**
     * 显示编码器状态
     */
    void showEncoderState(String data);

    /**
     * 显示内存状态
     */
    void showMemoryStatus(String data);

    /**
     * 显示工作的总时间
     * @param time
     */
    void showWorkingTime(String time);

}
