package com.example.apph264tortp.rtp;


import java.io.IOException;
import java.nio.ByteBuffer;

public class RtpAvcStream extends RtpStream {
	
	//private final static String TAG = "AvcRtpStream"; 

	public RtpAvcStream(RtpSocket socket) {
		super(96, 90000, socket);
	}

	public void addNalu(ByteBuffer buf, int size, long timeUs) throws IOException{
		byte[] data = new byte[size];
		buf.get(data);

		addNalu(data, 0, size, timeUs);
	}

	public void addNalu(byte[] data, int offset, int size, long timeUs) throws IOException{
		if(size <= 1400){
			createSingleUnit(data, offset, size, timeUs);
		}else{
			createFuA(data, offset, size, timeUs);
		}
	}

	private void createSingleUnit(byte[] data, int offset, int size, long timeUs) throws IOException {

		//Log.i(TAG, "single nalu  type:" +  (data[offset] & 0x1f));

		addPacket(data, offset, size, timeUs);
	}

    
	
	//传入字节必须为NAL单元
	private void createFuA(byte[] data, int offset, int size, long timeUs) throws IOException {

		//获取data[0]，第一个字节NAL头(F:0,NRI：1-2,TYPE:3-7)
		byte originHeader = data[offset++];
		size -= 1;

		//Log.i(TAG, "FuA nalu  type:" +  (originHeader & 0x1f));

		
		//需处理剩余
		int remain = size;
		//分片大小
		int read = 1400;

		
		for(;remain > 0; remain -= read, offset += read){
			//FU indicator 结构  |----F---1bit|----NRI---2bit|----TYPE---5bit|
			//设置为FU-A  &‭11100000 后5位清0(清除type), |‭00011100‬ (28表示FU-A)‬
			byte indicator = (byte)( (originHeader & 0xe0) | 28);
			
			
			//FU Header |----S---1bit|----E---1bit|----R---1bit|----TYPE---5bit|
			//&‭00011111‬ 前3位清0,保留TYPE,与NALU的Header中的Type类型一致
			byte fuHeader = (byte)(originHeader & 0x1f);

			if(remain < read){
				read = remain;
			}
			
			//第一次循环,第一片设置为
			if(remain == size )
				fuHeader = (byte)(fuHeader | (1 << 7)); 	
			else if(remain == read)
				fuHeader = (byte)(fuHeader | (1 << 6));
			
			addPacket(new byte[]{indicator, fuHeader}, data, offset, read, timeUs);
		}		
	}	
}
