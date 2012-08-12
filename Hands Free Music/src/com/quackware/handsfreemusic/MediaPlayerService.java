/*******************************************************************************
 * Copyright (c) 2012 Curtis Larson (QuackWare).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.quackware.handsfreemusic;


import java.io.IOException;
import java.util.Arrays;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.widget.MediaController.MediaPlayerControl;

public class MediaPlayerService extends Service implements MediaPlayerControl,
MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

	static MediaPlayer player;
	private static CharSequence[] shuffleSongs = null;
	private static CharSequence[][] searchResults = null;
	
	ProgressDialog pDialog;

	public static String songText = "Nothing Playing.";
	private static String titleText = "Nothing Playing.";
	private static String descText = "";

	private static final int NOTIFY_ID = 123;

	private int mPercent = 1;

	private static String dataSource = null;

	private static int shuffleIndex = 0;

	private static Handler mHandler;

	public class LocalBinder extends Binder
	{
		MediaPlayerService getService()
		{
			return MediaPlayerService.this;
		}
	}
	@Override
	public void onCreate()
	{
		super.onCreate();
		player = new MediaPlayer();
		
		player.setOnCompletionListener(this);
		player.setOnBufferingUpdateListener(this);
		player.setOnErrorListener(this);
		
		SetGlobalNotification();

	}

	private void SetGlobalNotification()
	{
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager)getSystemService(ns);
		Notification notification = new Notification(R.drawable.ic_launcher,"Hands Free Music",System.currentTimeMillis());
		notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
		CharSequence contentTitle = titleText;
		CharSequence contentText = Html.fromHtml(descText);
		Intent notificationIntent = new Intent(Intent.ACTION_MAIN,null,getApplicationContext(),HandsFreeMusic.class);
		notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		PendingIntent contentIntent = PendingIntent.getActivity(this,0,notificationIntent,0);
		notification.setLatestEventInfo(getApplicationContext(),contentTitle,contentText,contentIntent);

		mNotificationManager.notify(NOTIFY_ID,notification);
	}

	public void incShuffleIndex()
	{
		shuffleIndex++;
	}

	public void playPreviousSong()
	{
		shuffleIndex -= 2;
		if(shuffleIndex < 0)
		{
			shuffleIndex = 0;
		}
		playNextSong();
	}


	@Override
	public void onDestroy()
	{

		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager)getSystemService(ns);
		mNotificationManager.cancel(NOTIFY_ID); 
		try {
			Log.i("info","onDestroy");
			if(isPlaying())
			{
				stop();
			}
			player.release();
		} catch(Exception ex) { }


		super.onDestroy();

	}

	public void stop()
	{
		try {
			Log.i("info","stop");
			if(isPlaying())
			{
				player.stop();
			}
		} catch(Exception ex) { }
	}

	public void reset()
	{
		Log.i("info","reset");
		player.reset();
	}

	public void playFile(String file)
	{
		//Retrieve the index of the file in search results so we can
		//set the song text based off of it.
		try
		{
			if(!HandsFreeMusic.playingYoutube)
			{
				int i = Arrays.asList(searchResults[0]).indexOf(file);
				songText = searchResults[1][i] + "\n" + searchResults[2][i] + " - " + searchResults[3][i];
				titleText = searchResults[1][i] + "";
				descText = searchResults[2][i] + "\n" + searchResults[3][i];
			}
			else
			{
				int i = Arrays.asList(searchResults[0]).indexOf(file);
				songText = "YouTube \n" + searchResults[1][i];
				titleText = "YouTube";
				descText = (String)searchResults[1][i];
			}
		}
		catch(Exception ex)
		{
			songText = "Unable to set song text";
			titleText = "Error";
			descText = "";
		}


		if(isPlaying())
		{
			stop();
			//reset();
		}


		try 
		{
			if(!HandsFreeMusic.playingYoutube)
			{
				Log.i("info","player.setDataSource called 190");
				player = new MediaPlayer();
				
				player.setOnCompletionListener(this);
				player.setOnBufferingUpdateListener(this);
				player.setOnErrorListener(this);
				
				player.setDataSource(file);
				Log.i("info","prepare?");
				player.prepare();
				setDataSource(file);
				Log.i("info","start?");
				start();
			}
			else
			{
				Uri uri = Uri.parse(file);
				setDataSource(file);
//				FileInputStream fis = openFileInput();

				
				
				player = new MediaPlayer();
				
				player.setOnCompletionListener(this);
				player.setAudioStreamType(AudioManager.STREAM_MUSIC);
				player.setOnBufferingUpdateListener(this);
				player.setOnErrorListener(this);
				player.setDataSource(uri.toString());
				player.prepare();
				player.start();
			}


		}
		catch(IllegalStateException ex)
		{
			String s = ex.getMessage();
			Log.i("info","illegal state exception " + s);
		}
		catch(IOException ex)
		{
			String s = ex.getMessage();
			Log.i("info","ioexception " + s);
		}
		catch(IllegalArgumentException ex)
		{
			String s = ex.getMessage();
			Log.i("info","illegalargumentexception " + s);
		}


		Message msg = mHandler.obtainMessage();
		Bundle b = new Bundle();
		
		b.putString("file", file);
		b.putInt("messageType", 2);
		msg.setData(b);
		mHandler.sendMessage(msg);
		
		SetGlobalNotification();

	}

	public void playNextSong()
	{
		if(shuffleSongs != null && shuffleSongs.length > getShuffleIndex())
		{

			playFile(shuffleSongs[getShuffleIndex()].toString());
			incShuffleIndex();
		}
		//If shuffle songs is null then they probably dont want to play more then 1 song.
	}


	private void setDataSource(String iDataSource)
	{
		dataSource = iDataSource;
	}
	public String getDataSource()
	{
		return dataSource;
	}
	public void setShuffleSongs(CharSequence[] iShuffleSongs)
	{
		shuffleSongs = iShuffleSongs;
	}


	public CharSequence[] getShuffleSongs()
	{
		return shuffleSongs;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private final IBinder mBinder = new LocalBinder();


	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		mPercent = percent;
	}

	public void onCompletion(MediaPlayer mp) {
		if(!HandsFreeMusic.playingYoutube)
		{
			Log.i("Notification","onCompletion() called");
			//player.stop();
			playNextSong();
		}
	}
	public boolean canPause() {
		return true;
	}
	public boolean canSeekBackward() {

		return true;
	}
	public boolean canSeekForward() {
		return true;
	}
	public int getBufferPercentage() {
		return mPercent;
	}
	public int getCurrentPosition() {
		try
		{
			return player.getCurrentPosition();
		}
		catch(Exception ex)
		{
			
			Log.i("info","player.getCurrentPosition() threw an exception");
			return 1;
		}
	}
	public int getDuration() {
		Log.i("Notification","getDuration() called");
		try {
		return player.getDuration();	
		}
		catch(Exception ex)
		{
			Log.i("Notification","getDuration() failed " + ex.getMessage());
			return 1;
		}

	}
	public boolean isPlaying() {
		Log.i("Notification","isplaying() called");
		try
		{
			return player.isPlaying();
		}
		catch(Exception ex) 
		{
			Log.i("Notification","exception in isPlaying " + ex.getMessage());
			reset();
			return false;
		}
	}
	public void pause() {
		Log.i("Notification","pause() called");
		if(player != null)
		{
			player.pause();
		}

	}
	public void seekTo(int pos) {
		Log.i("Notification","seekto() called");
		player.seekTo(pos);

	}
	public void start() {
		Log.i("Notification","start() called");
		try
		{
			player.start();
		}
		catch (Exception ex) { Log.i("info","start failed to be called");}
	}

	public void setHandler(Handler handler) {
		Log.i("Notification","setHandler() called");
		mHandler = handler;

	}

	public MediaPlayer getPlayer() {
		return player;
	}

	public void setShuffleIndex(int shuffleIndex) {
		this.shuffleIndex = shuffleIndex;
	}

	public int getShuffleIndex() {
		return shuffleIndex;
	}

	public String getSongText()
	{
		return songText;
	}

	public void setSearchResults(CharSequence[][] iSearchResults)
	{
		searchResults = iSearchResults;
	}

	public String getCurrentTime()
	{
		String returnTime = "0:00";
		double songTime = getCurrentPosition();
		int seconds = (int) ((songTime / 1000) % 60);
		int minutes = (int) ((songTime / 1000) / 60);
		if (seconds < 10) {
			returnTime = minutes + ":0" + seconds;
		}
		else
		{
			returnTime = minutes + ":" + seconds;
		}
		return returnTime;
	}

	public String getTotalTime()
	{
		String returnTime = "0:00";
		double songTime = getDuration();
		int seconds = (int) ((songTime / 1000) % 60);
		int minutes = (int) ((songTime / 1000) / 60);
		if (seconds < 10) {
			returnTime = minutes + ":0" + seconds;
		}
		else
		{
			returnTime = minutes + ":" + seconds;
		}
		return returnTime;
	}

	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.i("onError","onError called" + mp.toString() + " " + what + " " + extra);
		Log.i("onError","Attempting to reset and start");
		try
		{
			mp.reset();
			mp.start();
			return true;
		}
		catch(Exception ex)
		{
			Log.i("onError","Failed to reset and start: " + ex.getMessage());
			return false;
		}
	}

}
