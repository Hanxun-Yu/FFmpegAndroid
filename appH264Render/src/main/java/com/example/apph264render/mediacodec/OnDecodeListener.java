package com.example.apph264render.mediacodec;

/**
 * @author zed
 * @date 2017/12/12 下午3:22
 * @desc 解码结果
 */

public interface OnDecodeListener {

	void decodeResult(byte[]data,int length,int w, int h);

}
