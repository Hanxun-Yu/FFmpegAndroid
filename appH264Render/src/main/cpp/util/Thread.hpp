#include <pthread.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <functional>
#include <thread>

#ifndef FFMPEGPLAYER_Thread_H
#define FFMPEGPLAYER_Thread_H

class Thread {
private:
//    pthread_t pid;
    std::thread thread;
private:
    static void start_thread(void *arg) {
        LOGD("start_thread start");
        Thread *ptr = (Thread *) arg;
        ptr->run();  //线程的实体是run
        LOGD("start_thread end");
    } //静态成员函数
protected:
    int start(){
        thread = std::thread(std::bind(&Thread::start_thread,this));

//        if (pthread_create(&pid, NULL, std::bind(&Thread::start_thread,this), (void *) this) != 0) //´创建一个线程(必须是全局函数)
//        {
//            return -1;
//        }
        return 0;
    }

    int stop() {
        thread.detach();
//        pthread_kill(pid, 0);
        return 0;
    }

    virtual void run() = 0; //基类中的虚函数要么实现，要么是纯虚函数（绝对不允许声明不实现，也不纯虚）
};


#endif