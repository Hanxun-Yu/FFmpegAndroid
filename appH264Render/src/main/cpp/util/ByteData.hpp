//
// Created by yuhanxun on 2018/8/7.
//


#ifndef FFMPEGPLAYER_BYTEDATA_HPP
#define FFMPEGPLAYER_BYTEDATA_HPP
#include <sys/types.h>
#include <cstdlib>

class ByteData {

private:
    u_int8_t * p_data;
    u_int32_t size;
public:
    ByteData(){}
    ~ByteData(){
//        free(this->p_data);
//        LOGD("~ByteData() s");
//        LOGD("p_data %p",p_data);

        free(p_data);
//        LOGD("~ByteData() e");
    }
    u_int8_t *getP_data() const {
        return this->p_data;
    }
    void setP_data(u_int8_t *p_data) {
        this->p_data = p_data;
    }

    u_int32_t getSize() const {
        return this->size;
    }

    void setSize(u_int32_t size) {
        this->size = size;
    }
};

#endif //FFMPEGPLAYER_BYTEDATA_HPP

