package com.example.apph264tortp.h264;


import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

/**
 * Created by yuhanxun
 * 2018/8/8
 * description:
 */
public class H264Helper {
    final String TAG = this.getClass().getSimpleName() +"_xunxun";
    public interface IOnNalListener {
        void onFindNalIncludeStart(ByteData data);
    }

    public static class OnNalListener implements IOnNalListener {

        @Override
        public void onFindNalIncludeStart(ByteData data) {
            byte[] bs = new byte[0];
            //去除起始码
            if(data.data[2] == 0x01) {
                bs = new byte[data.size-3];
                System.arraycopy(data.data, 3, bs, 0, bs.length);
            } else if(data.data[3] == 0x01){
                bs = new byte[data.size-4];
                System.arraycopy(data.data, 4, bs, 0, bs.length);
            }
            data.data = bs;
            data.size = bs.length;
            onFindNal(data);
        }

        public void onFindNal(ByteData data) {

        }
    }

    /**
     * 因为不确定264文件的开始与结束的完整性
     * 需要在文件中搜索nal单元
     */
    public void findNalFromH264File(final String filePath, final IOnNalListener onNalListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String fileName = filePath;
                InputStream in = null;
                ByteBuffer partNal = null;
                try {
                    in = new FileInputStream(fileName);
                    byte[] buffer = new byte[1024 * 4];
                    int n;
                    partNal = ByteBuffer.allocate(1024 * 1024);

                    while ((n = in.read(buffer)) != -1) {
                        int[] nalIndex = findNalHeadIndex(buffer, n);
                        if (nalIndex.length == 0) {
                            //没找到头部,如之前缓存了半个nal,则填上
                            if (partNal.position() != 0) {
                                partNal.put(buffer, 0, n);
                            }
                        } else {
                            //buffer从nal中间开始,则截取补进上次的buffer中
                            if (nalIndex[0] != 0 && partNal.position() != 0) {
                                partNal.put(buffer, 0, nalIndex[0]);
                            }

                            //如果填充过nal数据
                            if (partNal.position() != 0) {
                                ByteData byteData = new ByteData();
                                byte[] data = fromByteBuffer(partNal);
                                byteData.data = data;
                                byteData.size = data.length;
                                partNal.clear();
                                if (onNalListener != null)
                                    onNalListener.onFindNalIncludeStart(byteData);
                            }

                            //处理完整的,最后一项不处理
                            for (int i = 0; i < nalIndex.length - 1; i++) {
                                byte[] temp = new byte[nalIndex[i + 1] - nalIndex[i]];
                                System.arraycopy(buffer, nalIndex[i], temp, 0, temp.length);
                                ByteData byteData = new ByteData();
                                byteData.data = temp;
                                byteData.size = temp.length;
                                if (onNalListener != null)
                                    onNalListener.onFindNalIncludeStart(byteData);
                            }

                            //保留留到下次处理,处理最后一项
                            //填入partNal,不确定下一个起始码位置,下次循环才知道
                            partNal.put(buffer, nalIndex[nalIndex.length - 1], n - nalIndex[nalIndex.length - 1]);
                        }
                    }
                    //如果填充过nal数据
                    if (partNal.position() != 0) {
                        ByteData byteData = new ByteData();
                        byte[] data = fromByteBuffer(partNal);
                        byteData.data = data;
                        byteData.size = data.length;
                        partNal.clear();
                        if (onNalListener != null)
                            onNalListener.onFindNalIncludeStart(byteData);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private byte[] fromByteBuffer(ByteBuffer bb) {
        bb.flip();//使limit为有效值
        byte[] temp = new byte[bb.limit()];
        System.arraycopy(bb.array(), 0, temp, 0, temp.length);
        return temp;
    }

    private int[] findNalHeadIndex(byte[] data, int size) {
        IntBuffer intBuffer = IntBuffer.allocate(size);
        int count_0x00 = 0;
        int indexTemp = 0;
        for (int i = 0; i < size; i++) {
            if (data[i] == 0x01
                    && (count_0x00 == 3 || count_0x00 == 2)) {
                intBuffer.put(indexTemp);
                count_0x00 = 0;
            } else if (data[i] == 0x00) {
                if (count_0x00 == 0) {
                    indexTemp = i;
                }
                count_0x00++;
            } else {
                count_0x00 = 0;
            }
        }
        intBuffer.flip();
        int[] ret = new int[intBuffer.limit()];
        intBuffer.get(ret, 0, ret.length);
//        Log.d(TAG,"ret:"+ Arrays.toString(ret));
        return ret;
    }

    public class ByteData {
        public byte[] data;
        public int size;
    }
}
