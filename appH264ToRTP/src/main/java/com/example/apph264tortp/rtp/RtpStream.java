package com.example.apph264tortp.rtp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RtpStream {

    private static final String TAG = "RtpStream";
    private int payloadType;
    private int sampleRate;
    private RtpSocket socket;
    private short sequenceNumber;
    private long timeold;


    public RtpStream(int pt, int sampleRate, RtpSocket socket) {
        this.payloadType = pt;
        this.sampleRate = sampleRate;
        this.socket = socket;
    }

    public void addPacket(byte[] data, int offset, int size, long timeUs) throws IOException {
        addPacket(null, data, offset, size, timeUs);
    }

    public void addPacket(byte[] prefixData, byte[] data, int offset, int size, long timeUs) throws IOException {
	
		/*
		RTP packet header
		Bit offset[b]	0-1	2	3	4-7	8	9-15	16-31
		0			Version	P	X	CC	M	PT	Sequence Number  31
		32			Timestamp									 63
		64			SSRC identifier								 95
		*/

        ByteBuffer buffer = ByteBuffer.allocate(1500);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        //1
        buffer.put((byte) ((1 << 7) & 254));
        //2
        buffer.put((byte) (payloadType));//96��һλ0�䵱��M
        //3-4
        buffer.putShort(sequenceNumber++);
        //5-6
        buffer.putInt((int) (timeUs));



//		intToByte(buffer, (int) timeUs);
//        buffer.putInt(12345678);
//        buffer.putInt(size);

        if (prefixData != null)
            buffer.put(prefixData);

        buffer.put(data, offset, size);

        sendPacket(buffer, buffer.position());

    }

    public static byte[] intToByte(ByteBuffer buffer, int number) {
        int temp = number;
        byte[] b = new byte[4];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        buffer.put(b);
        return b;
    }

    protected void sendPacket(ByteBuffer buffer, int size) throws IOException {
        socket.sendPacket(buffer.array(), 0, size);
        buffer.clear();
    }
}
