//
// Created by yuhanxun on 2018/6/19.
//

#include "ThreadHandler.h"

using namespace std;
void* runnable(void* threadargs);

pthread_t threads;
ThreadHandler::ThreadHandler(JavaVM *javaVM, jobject jobject1) {
    this->javaVM = javaVM;
    this->jobject1 = jobject1;
}

void ThreadHandler::start() {
    LOGD("ThreadHandler::start()");

    this->isStop  = false;
    int rc = pthread_create(&threads, NULL,
                            runnable, this);
    if (rc){
        LOGE("Error:unable to create thread");
        exit(-1);
    }
}

void ThreadHandler::stop() {
    this->isStop = true;
}

void ThreadHandler::setCallback(ICallback *callback) {
    this->callback = callback;
}

void* runnable(void* threadargs) {
    ThreadHandler* threadHandler = (ThreadHandler*)threadargs;
    JNIEnv* jniEnv;
    threadHandler->javaVM->AttachCurrentThread(&jniEnv,NULL);

    while(!threadHandler->isStop) {
//        LOGD("runnable_xunxunhahahahahahahaha");
//        usleep(500000);
        threadHandler->callback->onCallback(jniEnv,threadHandler->jobject1,"filename",true);
        sleep(1);
    }
    pthread_exit(&threads);
}