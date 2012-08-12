/*******************************************************************************
 * Copyright (c) 2012 Curtis Larson (QuackWare).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.quackware.handsfreemusic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;





public class YouTubeUtility
{
	private static final String YOUTUBE_VIDEO_INFORMATION_URL = "http://www.youtube.com/get_video_info?&video_id=";
	private String youtubeUrl = "";
	
	private static final String YOUTUBE_DEV_KEY = "AI39si56w6GQUPD1lgFi16am5JJjHMJIhCZGHF7tadpfZMlZCHZXd_40BvQAMfIiB1BvbRaASFvJ4c30YstRBp6QSwLqQjoNdw";
	private static final String YOUTUBE_FEED_START = "	http://gdata.youtube.com/feeds/api/videos?q=";
	private static final String YOUTUBE_FEED_END = "&max-results=10&v=2";

	
	public YouTubeUtility(String youtubeVideoId)
	{
		String realYoutubeUrl = "";
		try {
			realYoutubeUrl = calculateYouTubeUrl(youtubeVideoId);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setYoutubeUrl(realYoutubeUrl);

		
	}
	
	public static void testYouTube(String query)
	{

		
		
		
	}
	
	private void setYoutubeUrl(String realYoutubeUrl)
	{
		youtubeUrl = realYoutubeUrl;
	}
	
	public String getYoutubeUrl()
	{
		return youtubeUrl;
	}
	
	
	public static String calculateYouTubeUrl(String youtubeVideoId) throws IOException, ClientProtocolException, UnsupportedEncodingException
	{
		HttpClient client = new DefaultHttpClient();
		HttpGet getMethod = new HttpGet(YOUTUBE_VIDEO_INFORMATION_URL + youtubeVideoId);
		HttpResponse response = null;

		response = client.execute(getMethod);

		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		String infoStr = null;
		response.getEntity().writeTo(bos);
		infoStr = new String(bos.toString("UTF-8"));
		
		String[] args = infoStr.split("&");
		Map<String,String> argMap = new HashMap<String,String>();
		for(int i = 0;i<args.length;i++)
		{
			String[] argValStrArr = args[i].split("=");
			if(argValStrArr != null)
			{
				if(argValStrArr.length >= 2)
				{
					argMap.put(argValStrArr[0], URLDecoder.decode(argValStrArr[1]));
				}
			}
		}
		String tokenStr = null;
		try
		{
			tokenStr = URLDecoder.decode(argMap.get("token"));
		}
		catch(Exception ex) {return null;}
		String uriStr = "http://www.youtube.com/get_video?video_id=" + youtubeVideoId + "&t=" +
		URLEncoder.encode(tokenStr) + "&fmt=18";
		return uriStr;
	}
}
