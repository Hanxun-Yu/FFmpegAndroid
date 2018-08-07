//
// Created by yuhanxun on 2018/8/7.
//


#ifndef FFMPEGPLAYER_BYTEDATA_HPP
#define FFMPEGPLAYER_BYTEDATA_HPP
#include <sys/types.h>
class ByteData {

private:
    u_int8_t * p_data;
    u_int32_t size;
public:
    ByteData(){}
    ~ByteData(){}
    u_int8_t *getP_data() const {
        return p_data;
    }
    void setP_data(u_int8_t *p_data) {
        ByteData::p_data = p_data;
    }

    u_int32_t getSize() const {
        return size;
    }

    void setSize(u_int32_t size) {
        ByteData::size = size;
    }
};

#endif //FFMPEGPLAYER_BYTEDATA_HPP

