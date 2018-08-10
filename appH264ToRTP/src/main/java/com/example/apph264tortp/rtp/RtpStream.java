package com.example.apph264tortp.rtp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

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

        final ByteBuffer buffer = ByteBuffer.allocate(1500);
        //1
        buffer.put((byte) ((1 << 7) & 254));
        //2
        buffer.put((byte) (payloadType));//96��һλ0�䵱��M
        //3-4
        buffer.putShort(sequenceNumber++);
        //5-8
        buffer.putInt((int) (timeUs));
        //9-10
        buffer.putShort((short) 0);
        //11
        buffer.put((byte) 0);
        //12
        buffer.put((byte) 10);


//		intToByte(buffer, (int) timeUs);
//        buffer.putInt(12345678);
//        buffer.putInt(size);

        if (prefixData != null)
            buffer.put(prefixData);

        buffer.put(data, offset, size);

        sendPacket(buffer, buffer.position());
    }

    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(1500);
        //1
        buffer.put((byte) ((1 << 7) & 254));
        //2
        buffer.put((byte) (96));//96��һλ0�䵱��M
        //3-4
        buffer.putShort((short) 0);
        //5-8
        buffer.putInt((int) (0));
        //9-10
        buffer.putShort((short) 0);
        buffer.put((byte) 0);
        buffer.put((byte) 10);
        System.out.println(Arrays.toString(buffer.array()));
    }

    protected void sendPacket(ByteBuffer buffer, int size) throws IOException {
        socket.sendPacket(buffer.array(), 0, size);
        buffer.clear();
//        try {
//            Thread.sleep(10);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}
