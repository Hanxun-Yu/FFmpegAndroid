//
// Created by yuhanxun on 2018/8/20.
//

#ifndef FFMPEGPLAYER_X264PARAM_HPP
#define FFMPEGPLAYER_X264PARAM_HPP

#include <string>
#include <ostream>
#include <CoutLogcat.hpp>

using namespace std;

class X264Param {
private:
    int widthIN;
    int heightIN;
    int widthOUT;
    int heightOUT;
    int bitrate;
    int fps;
    int gop;
    int bFrameCount;
    bool useSlice;
    string bitrateCtrl;
    string preset;
    string tune;
    string profile;
public:
    friend ostream &operator<<(ostream &os, const X264Param &param) {
        os << "widthIN: " << param.widthIN << " heightIN: " << param.heightIN << " widthOUT: "
           << param.widthOUT << " heightOUT: " << param.heightOUT << " bitrate: " << param.bitrate
           << " fps: " << param.fps << " gop: " << param.gop << " bFrameCount: "
           << param.bFrameCount << " useSlice: " << param.useSlice << " bitrateCtrl: "
           << param.bitrateCtrl << " preset: " << param.preset << " tune: " << param.tune
           << " profile: " << param.profile;
        return os;
    }
    void printSelf() {
        CoutLogcat coutLogcat;

        std::cout.rdbuf(&coutLogcat);

        //NOTE: std::endl会立即调用sync方法将缓冲区字符写入log，并不只是换行用
        std::cout << *this << std::endl;
    }

public:
    int getWidthIN() const {
        return widthIN;
    }

    void setWidthIN(int widthIN) {
        X264Param::widthIN = widthIN;
    }

    int getHeightIN() const {
        return heightIN;
    }

    void setHeightIN(int heightIN) {
        X264Param::heightIN = heightIN;
    }

    int getWidthOUT() const {
        return widthOUT;
    }

    void setWidthOUT(int widthOUT) {
        X264Param::widthOUT = widthOUT;
    }

    int getHeightOUT() const {
        return heightOUT;
    }

    void setHeightOUT(int heightOUT) {
        X264Param::heightOUT = heightOUT;
    }

    int getBitrate() const {
        return bitrate;
    }

    void setBitrate(int bitrate) {
        X264Param::bitrate = bitrate;
    }

    int getFps() const {
        return fps;
    }

    void setFps(int fps) {
        X264Param::fps = fps;
    }

    int getGop() const {
        return gop;
    }

    void setGop(int gop) {
        X264Param::gop = gop;
    }

    int getBFrameCount() const {
        return bFrameCount;
    }

    void setBFrameCount(int bFrameCount) {
        X264Param::bFrameCount = bFrameCount;
    }

    bool isUseSlice() const {
        return useSlice;
    }

    void setUseSlice(bool useSlice) {
        X264Param::useSlice = useSlice;
    }

    const string &getBitrateCtrl() const {
        return bitrateCtrl;
    }

    void setBitrateCtrl(const string &bitrateCtrl) {
        X264Param::bitrateCtrl = bitrateCtrl;
    }

    const string &getPreset() const {
        return preset;
    }

    void setPreset(const string &preset) {
        X264Param::preset = preset;
    }

    const string &getTune() const {
        return tune;
    }

    void setTune(const string &tune) {
        X264Param::tune = tune;
    }

    const string &getProfile() const {
        return profile;
    }

    void setProfile(const string &profile) {
        X264Param::profile = profile;
    }


};

#endif //FFMPEGPLAYER_X264PARAM_HPP
