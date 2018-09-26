//
// Created by xunxun on 2018/8/12.
//

#include <string.h>
#include "YuvRender.h"

pthread_t threads2;

void *runnable2(void *threadargs);

void YuvRender::setRenderView(JNIEnv *jniEnv, jobject obj) {
    this->renderView = jniEnv->NewGlobalRef(obj);
}

void YuvRender::putYuv(YuvData *yuvData) {
    syncQueue->put(yuvData);
}


YuvRender::YuvRender(JavaVM *javaVM, jobject jobj) {
    this->javaVM = javaVM;
    this->jobj = jobj;
    this->syncQueue = new SyncQueue<YuvData>(100);


}

void YuvRender::start() {
    this->isStop = false;
    int rc = pthread_create(&threads2, NULL,
                            runnable2, this);
    if (rc) {
        LOGE("Error:unable to create thread");
        exit(-1);
    } else {
    }
}

void YuvRender::stop() {
    if (!this->isStop) {
        this->isStop = true;
        pthread_kill(threads2, 0);
        this->syncQueue->clear();
    }
}

void *runnable2(void *threadargs) {
    YuvRender *threadHandler = (YuvRender *) threadargs;
    JNIEnv *jniEnv;
    threadHandler->javaVM->AttachCurrentThread(&jniEnv, NULL);
    ANativeWindow *nativeWindow = ANativeWindow_fromSurface(jniEnv, threadHandler->renderView);


    ANativeWindow_Buffer windowBuffer;

    uint32_t width = 0;
    uint32_t height = 0;
    while (!threadHandler->isStop) {
        LOGD("taking");
        YuvData *bean = threadHandler->syncQueue->take();

        //转rgb
        //...
        int w = bean->getWidth();
        int h = bean->getHeight();
        u_int8_t *buffer_dest = (u_int8_t *) malloc(w * h * 4);

        libyuv::I420ToABGR((const uint8 *) bean->getP_data(), w,
                           (const uint8 *) (bean->getP_data() + w * h), w/2 ,
                           (const uint8 *) (bean->getP_data() + w * h * 5 / 4), w/2,
                           buffer_dest, w * 4,
                           w, h);
        free(bean->getP_data());
        bean->setP_data(buffer_dest);

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
        int windowH;
        for (windowH = 0; windowH < bean->getHeight(); windowH++) {
            memcpy(dst + windowH * dstStride, src + windowH * srcStride, srcStride);
        }

        ANativeWindow_unlockAndPost(nativeWindow);

        delete bean;

    }
}
