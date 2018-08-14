//
// Created by yuhanxun on 2018/8/7.
//


#ifndef FFMPEGPLAYER_YuvData_HPP
#define FFMPEGPLAYER_YuvData_HPP
#include <sys/types.h>
#include <cstdlib>


class YuvData {

private:
    u_int8_t * p_data;
    u_int32_t size;
    u_int32_t width;
    u_int32_t height;

public:
    YuvData(){}
    ~YuvData(){
//        free(this->p_data);
//        delete(p_data);
        free(p_data);
    }

    u_int8_t *getP_data() const {
        return p_data;
    }

    void setP_data(u_int8_t *p_data) {
        YuvData::p_data = p_data;
    }

    u_int32_t getSize() const {
        return size;
    }

    void setSize(u_int32_t size) {
        YuvData::size = size;
    }

    u_int32_t getWidth() const {
        return width;
    }

    void setWidth(u_int32_t width) {
        YuvData::width = width;
    }

    u_int32_t getHeight() const {
        return height;
    }

    void setHeight(u_int32_t height) {
        YuvData::height = height;
    }
};

#endif //FFMPEGPLAYER_YuvData_HPP

