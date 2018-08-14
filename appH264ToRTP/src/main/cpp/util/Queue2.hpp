#ifndef CEEWAIPCSDKDEMO_Queue2_H
#define CEEWAIPCSDKDEMO_Queue2_H

#include <list>
#include <mutex>
#include <condition_variable>
#include <iostream>
#include "Jnicom.h"

template<typename T>
class Queue2 {
private:
//    bool IsFull() const {
//        return m_queue.size() == m_maxSize;
//    }
//
//    bool IsEmpty() const {
//        return m_queue.empty();
//    }

public:
    Queue2(int maxSize) : m_maxSize(maxSize) {
    }

    void put(T *x) {
        int tempCount = putCount++;

//        std::lock_guard<std::mutex> locker(m_mutex);
//        LOGE("SyncQueue put %d",tempCount);
//        while (m_queue.size()==m_maxSize) {
////            LOGD("SyncQueue put %d IsFull queue size:%d  max:%d",tempCount,m_queue.size(),m_maxSize);
////            std::cout << "the blocking queue is full,waiting..." << std::endl;
////            LOGE("SyncQueue put %d IsFull wait",tempCount);
//            m_notFull.wait(m_mutex);
////            LOGE("SyncQueue put %d wait end",tempCount);
//
//        }
        m_queue.push_back(x);
//        m_notEmpty.notify_one();
//        LOGE("SyncQueue put %d end size:%d",tempCount,m_queue.size());

    }

    int takeCount = 0;
    int putCount = 0;
    T* take() {
        int tempCount = takeCount++;
//        std::lock_guard<std::mutex> locker(m_mutex);
//        LOGI("SyncQueue take %d",tempCount);
        while (m_queue.empty()) {
//            std::cout << "the blocking queue is empty,wating..." << std::endl;
//            LOGD("SyncQueue take %d IsEmpty queue size:%d  max:%d empty():%d",tempCount,m_queue.size(),m_maxSize,m_queue.empty());
//            LOGI("SyncQueue take %d IsEmpty wait ",tempCount);
//            m_notEmpty.wait(m_mutex);
//            LOGI("SyncQueue take %d wait end",tempCount);

        }
        T* x = m_queue.front();
        m_queue.pop_front();
//        m_notFull.notify_one();
        LOGI("SyncQueue take %d return  size:%d",tempCount,m_queue.size());

        return x;
    }


private:
    std::list<T*> m_queue;                  //缓冲区
    std::mutex m_mutex;                    //互斥量和条件变量结合起来使用
    std::condition_variable_any m_notEmpty;//不为空的条件变量
    std::condition_variable_any m_notFull; //没有满的条件变量
    int m_maxSize;                         //同步队列最大的size
};

#endif