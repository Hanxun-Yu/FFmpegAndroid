//
// Created by xunxun on 2018/8/12.
//

#include <string.h>
#include "RGBRender.h"

pthread_t threads2;

void *runnable2(void *threadargs);

void RGBRender::setRenderView(JNIEnv *jniEnv, jobject obj) {
    this->renderView = jniEnv->NewGlobalRef(obj);
    int rc = pthread_create(&threads2, NULL,
                            runnable2, this);
    if (rc) {
        LOGE("Error:unable to create thread");
        exit(-1);
    } else {
    }
}

void RGBRender::putRGB(YuvData *yuvData) {
    syncQueue->put(yuvData);
}


RGBRender::RGBRender(JavaVM *javaVM, jobject jobj) {
    this->javaVM = javaVM;
    this->jobj = jobj;
    this->syncQueue = new SyncQueue<YuvData>(100);
    this->isStop = false;

}

void *runnable2(void *threadargs) {
    RGBRender *threadHandler = (RGBRender *) threadargs;
    JNIEnv *jniEnv;
    threadHandler->javaVM->AttachCurrentThread(&jniEnv, NULL);
    ANativeWindow *nativeWindow = ANativeWindow_fromSurface(jniEnv, threadHandler->renderView);


    ANativeWindow_Buffer windowBuffer;

    uint32_t width = 0;
    uint32_t height = 0;
    while (!threadHandler->isStop) {
//        LOGD("taking");
        YuvData *bean = threadHandler->syncQueue->take();


        if(width != bean->getWidth() || height != bean->getWidth()) {
            // 设置native window的buffer大小,可自动拉伸
            ANativeWindow_setBuffersGeometry(nativeWindow, bean->getWidth(), bean->getHeight(),
                                             WINDOW_FORMAT_RGBA_8888);
            width = bean->getWidth();
            height = bean->getHeight();
        }

        // lock native window buffer
        ANativeWindow_lock(nativeWindow, &windowBuffer, 0);

        // 获取stride
        uint8_t *dst = (uint8_t *) windowBuffer.bits;
        int dstStride = windowBuffer.stride * 4;
        uint8_t *src = bean->getP_data();
        int srcStride = bean->getWidth() * 4;

        // 由于window的stride和帧的stride不同,因此需要逐行复制
        int h;
        for (h = 0; h < bean->getHeight(); h++) {
            memcpy(dst + h * dstStride, src + h * srcStride, srcStride);
        }

        ANativeWindow_unlockAndPost(nativeWindow);

        delete bean;

    }
}
