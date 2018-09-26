//
// Created by yuhanxun on 2018/9/26.
//

#ifndef FFMPEGPLAYER_SPSPARSE_H
#define FFMPEGPLAYER_SPSPARSE_H


#include <sys/types.h>

class SPSParse {
public:
    void getSpsWH(u_int8_t* sps,int32_t &width,int32_t &height);
};


#endif //FFMPEGPLAYER_SPSPARSE_H
