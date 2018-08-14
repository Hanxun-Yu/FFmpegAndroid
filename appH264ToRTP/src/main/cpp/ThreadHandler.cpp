//
// Created by yuhanxun on 2018/6/19.
//

#include "ThreadHandler.h"

using namespace std;

pthread_t threads;
void *runnable(void *threadargs);
ThreadHandler::ThreadHandler(JavaVM *javaVM, jobject jobject1) {
    this->javaVM = javaVM;
    this->jobject1 = jobject1;
    this->syncQueue = new SyncQueue<ByteData>(100);
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
        pthread_kill(threads,0);
    }
}

void ThreadHandler::putData(u_int8_t *p_data, uint16_t size) {
    LOGD("ThreadHandler::putData()");
    ByteData* bean = new ByteData();
    bean->setP_data(p_data);
    bean->setSize(size);
    syncQueue->put(*bean);
}

void ThreadHandler::setCallback(ThreadHandler::ICallback *callback) {
    this->callback = callback;
}

void *runnable(void *threadargs) {
    ThreadHandler *threadHandler = (ThreadHandler *) threadargs;
    JNIEnv *jniEnv;
    threadHandler->javaVM->AttachCurrentThread(&jniEnv, NULL);

    while (!threadHandler->isStop) {
        LOGD("taking");
        ByteData bean = threadHandler->syncQueue->take();
        LOGD("take size:%d",bean.getSize());
        ByteData* ret = threadHandler->handleData(&bean);
        LOGD("take handled size:%d",ret->getSize());
//        threadHandler->callback->onCallback(jniEnv,threadHandler->jobject1,"filename",true);
        threadHandler->callback->onCallback(jniEnv, threadHandler->jobject1, ret);
    }
    return NULL;
}
