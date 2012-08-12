/*******************************************************************************
 * Copyright (c) 2012 Curtis Larson (QuackWare).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.quackware.handsfreemusic;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;

public class Utility 
{

//	public static String getSourceCode(URL u)
//	{
//		StringBuilder sb = new StringBuilder();
//	    try{
//	      HttpURLConnection uc = (HttpURLConnection) u.openConnection();
//	      int code = uc.getResponseCode();
//	      String response = uc.getResponseMessage();
//	      System.out.println("HTTP/1.x " + code + " " + response);
//	      for(int j = 1; ; j++){
//	        String header = uc.getHeaderField(j);
//	        String key = uc.getHeaderFieldKey(j);
//	        if(header == null || key == null)
//	          break;
//	        System.out.println(uc.getHeaderFieldKey(j) + ": " + header);
//	      }
//	      InputStream in = new BufferedInputStream(uc.getInputStream());
//	      Reader r = new InputStreamReader(in);
//	      int c;
//	      while((c = r.read()) != -1){
//	        sb.append((char)c);
//	      }
//	      return sb.toString();
//	    }
//	    catch(Exception ex)
//	    {
//	    	return null;
//	    }
//	}
	public static String getSourceCode(URL url)
	{
		Object content = null;
		try
		{

			HttpURLConnection uc = (HttpURLConnection)url.openConnection();
			uc.setRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.1; ru; rv:1.9.2.4) Gecko/20100513 Firefox/3.6.4");
			uc.connect();
			InputStream stream = uc.getInputStream();
			if(stream != null)
			{
				content = readStream(uc.getContentLength(),stream);
			}
			else if ( (content = uc.getContent( )) != null &&
					content instanceof java.io.InputStream )
				content = readStream( uc.getContentLength(), (java.io.InputStream)content );
			uc.disconnect( );

		}
		catch(Exception ex)
		{
			return null;
		}
		if(content != null && content instanceof String)
		{
			String html = (String)content;
			return html;
		}
		else
		{
			return null;
		}
	}

	private static Object readStream( int length, java.io.InputStream stream )
	throws java.io.IOException {
		String charset = null;
		final int buflen = Math.max( 1024, Math.max( length, stream.available() ) );
		byte[] buf   = new byte[buflen];;
		byte[] bytes = null;

		for ( int nRead = stream.read(buf); nRead != -1; nRead = stream.read(buf) ) {
			if ( bytes == null ) {
				bytes = buf;
				buf   = new byte[buflen];
				continue;
			}
			final byte[] newBytes = new byte[ bytes.length + nRead ];
			System.arraycopy( bytes, 0, newBytes, 0, bytes.length );
			System.arraycopy( buf, 0, newBytes, bytes.length, nRead );
			bytes = newBytes;
		}

		return new String(bytes);
	}
	
	public static CharSequence[][] retrieveYouTubeTitleAndUrl(String html)
	{
		ArrayList<String> links = new ArrayList<String>();
		ArrayList<String> titles = new ArrayList<String>();
		
		Pattern myPattern1 = Pattern.compile("<a href=\"rtsp.*?\\.3gp.*?\">");
		Pattern myPattern2 = Pattern.compile("<a accesskey=.*?</a>");
		Pattern myPattern3 = Pattern.compile(">.*?</a>");

		Matcher myMatcher = myPattern1.matcher(html);
		while(myMatcher.find() && links.size() < 5)
		{
			links.add(myMatcher.group().replace("<a href=\"", "").replace("\">", ""));
		}
		myMatcher = myPattern2.matcher(html);
		while(myMatcher.find() && titles.size() < 5)
		{
			Matcher myMatcher2 = myPattern3.matcher(myMatcher.group());
			myMatcher2.find();
			titles.add(myMatcher2.group().replace(">", "").replace("</a", ""));
		}
		

		CharSequence[][] searchResults = new CharSequence[2][links.size()];
		for(int i = 0;i<links.size();i++)
		{
			searchResults[0][i] = links.get(i);
			searchResults[1][i] = titles.get(i);
		}
		return searchResults;
	}
}
