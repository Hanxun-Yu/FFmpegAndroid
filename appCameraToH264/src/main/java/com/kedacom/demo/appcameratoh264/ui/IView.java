package com.kedacom.demo.appcameratoh264.ui;

/**
 * Created by yuhanxun
 * 2018/10/12
 * description:
 */
public interface IView {
    void setOnViewChangedListener(IViewLife viewChangedListener);
    interface IViewLife {
        void onPause();
        void onStop();
        void onResume();
        void onDestroy();
    }
}
