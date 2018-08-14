package com.example.apph264render.mediacodec;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author zed
 * @date 2017/12/6 上午9:47
 * @desc
 */

public class MediaCodecUtils {

	private static final String TAG = MediaCodecUtils.class.getSimpleName();

	//xml节点
	private static final String XML_DECODERS   = "Decoders";
	private static final String XML_TYPE       = "type";
	private static final String XML_MEDIACODEC = "MediaCodec";
	private static final String XML_MAX        = "max";
	private static final String XML_LIMIT      = "Limit";

	//预设视频格式
	private static final String MEDIACODEC_H264  = "video/avc";
	private static final String MEDIACODEC_MPEG4 = "video/mp4v-es";
	private static final String MEDIACODEC_HEVC  = "video/hevc";

	/**
	 * @param type MEDIACODEC_H264/MEDIACODEC_MPEG4/MEDIACODEC_HEVC
	 * @return ex:4096x2160
	 */
	public static String getSupportMax(String type) {
		//读取系统配置文件/system/etc/media_codecc.xml
		File file = new File("/system/etc/media_codecs.xml");
		InputStream in = null;
		try {
			in = new FileInputStream(file);
		} catch (Exception e) {
			// TODO: handle exception
		}

		if (in != null) {
			return readXML(in, type);
		}
		return null;
	}

	/**
	 * @param inStream
	 * @param type
	 * @return
	 */
	private static String readXML(InputStream inStream, String type) {

		String supportMax = null;

		XmlPullParser parser = Xml.newPullParser();

		try {
			parser.setInput(inStream, "UTF-8");
			int eventType = parser.getEventType();
			boolean isGetDecodeTAG = false;
			boolean isGetTypeTAG = false;

			while (eventType != XmlPullParser.END_DOCUMENT) {
				String name = parser.getName();

				if (eventType == XmlPullParser.START_TAG) {

					if (XML_DECODERS.equalsIgnoreCase(name)) {
						isGetDecodeTAG = true;
					} else if (XML_MEDIACODEC.equalsIgnoreCase(name) && isGetDecodeTAG) {
						//拿到type
						String videoType = parser.getAttributeValue(null, XML_TYPE);
						if (type.equalsIgnoreCase(videoType)) {
							isGetTypeTAG = true;
						}
					} else if (XML_LIMIT.equalsIgnoreCase(name) && isGetTypeTAG) {
						//拿到分辨率 ex:4096x2160
						supportMax = parser.getAttributeValue(null, XML_MAX);
						Log.i(TAG, type + " supportMax: " + supportMax);
						break;
					}
				}
				// 让解析器解析下一个数据
				eventType = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				inStream = null;
			}
		}

		return supportMax;
	}

}
