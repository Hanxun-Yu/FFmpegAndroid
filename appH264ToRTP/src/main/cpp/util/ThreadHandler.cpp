//
// Created by yuhanxun on 2018/6/19.
//

#include "ThreadHandler.h"

using namespace std;

pthread_t threads;

void *runnable(void *threadargs);

ThreadHandler::ThreadHandler(JavaVM *javaVM, jobject jobj) {
    this->javaVM = javaVM;
    this->jobj = jobj;
    this->syncQueue = new SyncQueue<ByteData>(10000);
//    this->syncQueue = new Queue2<ByteData>(10000);

}

void ThreadHandler::start() {
    LOGD("ThreadHandler::start()");
    this->isStop = false;
    int rc = pthread_create(&threads, NULL,
                            runnable, this);
    if (rc) {
        LOGE("Error:unable to create thread");
        exit(-1);
    } else {
    }
}

void ThreadHandler::stop() {
    LOGD("ThreadHandler::stop()");
    if (!this->isStop) {
        this->isStop = true;
//        pthread_exit(&threads);
        pthread_kill(threads, 0);
    }
}

void ThreadHandler::putData(u_int8_t *p_data, uint32_t size) {
    LOGD("ThreadHandler::putData()");
    ByteData *bean = new ByteData();
    bean->setP_data(p_data);
    bean->setSize(size);
    LOGD("putData():%p", bean);
    syncQueue->put(bean);
    LOGD("putData() end");

}

void ThreadHandler::setCallback(ThreadHandler::ICallback *callback) {
    this->callback = callback;
}

void *runnable(void *threadargs) {
    ThreadHandler *threadHandler = (ThreadHandler *) threadargs;
    JNIEnv *jniEnv;
    threadHandler->javaVM->AttachCurrentThread(&jniEnv, NULL);

    while (!threadHandler->isStop) {
//        LOGD("taking");
        LOGI("runnable taking -----------------------");

        ByteData *bean = threadHandler->syncQueue->take();
        LOGI("runnable taked ++++++++++++++++++++++");

//        LOGD("take bean:%p", bean);
//        LOGD("take size:%u", bean->getSize());
        YuvData *ret = threadHandler->handleData(bean);
        delete bean;
//        if (ret != NULL) {
//            LOGD("take handled size:%d", ret->getSize());
//        }
//        threadHandler->callback->onCallback(jniEnv,threadHandler->jobject1,"filename",true);
        if(ret != NULL)
            threadHandler->callback->onCallback(jniEnv, threadHandler->jobj, ret);
    }
}
