package com.quackware.handsfreemusic;

/** TODO
 *  - Lyric Search
 *  
 */

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.adwhirl.AdWhirlLayout;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class HandsFreeMusic extends Activity implements OnClickListener {

	public static final String PREFS_NAME = "PreferencesFile";
	
	private static final boolean donate = false;

	private MyDbHelper myDBHelper = null;
	private SQLiteDatabase myDB = null;
	private static final String DATABASE_NAME = "handsFreeMusicDB";
	private static final String SONG_DATABASE_TABLE = "t_Songs";	
	private static final String[] DATABASE_COLUMNS = new String[] {"PATH","SONG","ARTIST","ALBUM","PLAYLIST"};

	/**
	 * Progress thread and dialog variables.
	 */
	static final int PROGRESS_DIALOG = 7786;
	private static final int YOUTUBE_PROGRESS_DIALOG = 1231;
	private static ProgressDialog youtubeProgressDialog;
	
	private static ProgressThread progressThread;
	private static ProgressDialog progressDialog;

	/**
	 * Voice recognition constants.
	 */
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	private static final int SONG_CODE = 1111;
	private static final int ARTIST_CODE = 2222;
	private static final int ALBUM_CODE = 3333;
	private static final int PLAYLIST_CODE = 4444;
	private static final int YOUTUBE_CODE = 5555;

	private static final int GENERIC_ERROR = 1;
	private static final int NOT_FOUND_ERROR = 2;
	private static final int DATABASE_ERROR = 5;
	private static final int MEDIA_ERROR = 6;
	private static final int YOUTUBE_ERROR = 7;
	private static final int CUSTOM_ERROR = 9;

	private static final int CHOOSE_PREFERENCES = 0;

	private static MediaPlayerService mBoundService;
	private boolean mIsBound = false;

	private String oneFilePath = "";

	//private HeadPhoneReceiver headPhoneReceiver;

	public static CharSequence[][] searchResults;
	private static CharSequence[] shuffleSongs;

	private static boolean isLoaded = false;
	private static boolean isDialogShowing = false;
	private static boolean showWelcomeScreen = true;
	private static boolean alreadyShownWelcome = false;
	private static boolean loadingSongList = false;
	private static boolean playFirst = false;
	
	public static boolean playingYoutube = false;

	public static String songText = "No Song Playing!";

	private static boolean shuffle = false;
	private static boolean playAllSongs = false;
	private static boolean playAllArtist = true;
	private static boolean playAllAlbum = true;
	private static boolean playAllPlaylist = true;
	private static boolean broadSearch = true;

	private static MyDataStore dataStore;

	private static SeekBar seekbar;
	private static TextView totalTime;
	private static TextView currentTime;
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		//Make sure that the user can run our application.

		VerifyEnvironment();
		LoadPreferences();
		
		
//		if(!alreadyShownWelcome && showWelcomeScreen)
//		{
//			AlertDialog dialog;
//			AlertDialog.Builder builder = new AlertDialog.Builder(this);
//			builder.setTitle("Welcome to Hands Free Music Beta");
//			//        	builder.setMessage("Welcome to Hands Free Music Beta! Since your new here, click a few of these cool buttons below to "
//			//        			+ "check out the preferences and help screen! These can also be found through the menu bar.");
//			builder.setSingleChoiceItems(new CharSequence[] {"Show Help Screen", "Show Preference Screen", "Don't Show This Anymore!" }, -1,new DialogInterface.OnClickListener() {
//
//				public void onClick(DialogInterface dialog, int which) {
//					dialog.dismiss();
//					switch(which)
//					{
//					case 0:
//						Intent helpIntent = new Intent(getApplicationContext(),HelpActivity.class);
//						startActivity(helpIntent);
//						break;
//					case 1:
//						Intent prefIntent = new Intent(getApplicationContext(),PreferencesActivity.class);
//						startActivityForResult(prefIntent,CHOOSE_PREFERENCES);
//						break;
//					case 2:
//						showWelcomeScreen = false;
//						SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
//						SharedPreferences.Editor editor = settings.edit();
//						editor.putBoolean("showWelcome", showWelcomeScreen);
//						editor.commit();
//						dialog.dismiss();
//						break;
//					}
//					alreadyShownWelcome = true;
//				}
//			});
//			dialog = builder.create();
//			dialog.show();
//
//		}
		//Create all the button objects we will need to register.

		
		ImageButton speakButton = (ImageButton)findViewById(R.id.speakButton);
		speakButton.setOnClickListener(this);
		registerForContextMenu(speakButton);

		ImageButton playPauseButton = (ImageButton)findViewById(R.id.pausePlayButton);
		playPauseButton.setOnClickListener(this);
		ImageButton nextButton = (ImageButton)findViewById(R.id.nextButton);
		nextButton.setOnClickListener(this);
		ImageButton prevButton = (ImageButton)findViewById(R.id.previousButton);
		prevButton.setOnClickListener(this);

		seekbar = (SeekBar)findViewById(R.id.seekBar);
		seekbar.setOnClickListener(this);
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				
				if(fromUser && mBoundService != null && mBoundService.getDataSource() != null)
				{
					mBoundService.seekTo(progress);
					currentTime.setText(mBoundService.getCurrentTime());
				}
				if(mBoundService != null && mBoundService.getDataSource() == null && fromUser)
				{
					seekbar.setProgress(1);
				}
			}

			public void onStartTrackingTouch(SeekBar seekBar) {


			}

			public void onStopTrackingTouch(SeekBar seekBar) {


			} });


		currentTime = (TextView)findViewById(R.id.currentTime);
		totalTime = (TextView)findViewById(R.id.totalTime); 

		DisplayMetrics dm = new DisplayMetrics(); 
		getWindowManager().getDefaultDisplay().getMetrics(dm); 

		startService(new Intent(this,MediaPlayerService.class));
		//headPhoneReceiver = new HeadPhoneReceiver();
		//registerReceiver(headPhoneReceiver , new IntentFilter());
		doBindService();
		if(!donate)
		{
			try
			{
				LinearLayout layout = (LinearLayout)findViewById(R.id.LinearLayout01);
				AdWhirlLayout adWhirlLayout = new AdWhirlLayout(this, "7fd4426fd98e4fd2b69be7a41edf44d1");
				RelativeLayout.LayoutParams adWhirlLayoutParams = new RelativeLayout.LayoutParams(dm.widthPixels, 78);
				layout.addView(adWhirlLayout, adWhirlLayoutParams);
			}
			catch(Exception ex) { }
		}
		

	}

	
	@Override
	protected void onStart()
	{
		super.onStart();

		Log.i("onStart","calling isPlaying from onstart()");
		if(mBoundService != null && (mBoundService.isPlaying() || mBoundService.getDataSource() != null))
		{
			seekbar.setMax(mBoundService.getDuration());
			currentTime.setText(mBoundService.getCurrentTime());
			totalTime.setText(mBoundService.getTotalTime());

			seekbar.setProgress(mBoundService.getCurrentPosition());
			mHandler.removeCallbacks(mUpdateTimeTask);
			mHandler.postDelayed(mUpdateTimeTask, 100); 
			mBoundService.setShuffleSongs(shuffleSongs);

			Resources res = HandsFreeMusic.this.getResources();
			Drawable myImage = res.getDrawable(R.drawable.ic_music_pause);
			findViewById(R.id.pausePlayButton).setBackgroundDrawable(myImage); 
			SetSongText(playingYoutube);

		}
		if(mBoundService != null && !mBoundService.isPlaying() && mBoundService.getDataSource() != null)
		{
			Resources res = HandsFreeMusic.this.getResources();

			Drawable myImage = res.getDrawable(R.drawable.ic_music_play);
			findViewById(R.id.pausePlayButton).setBackgroundDrawable(myImage);
		}
		
		//Get a readable database object
//		myDBHelper = new MyDbHelper(getApplicationContext());
//
//		myDB = myDBHelper.getReadableDatabase();
	}
	@Override
	protected void onPause()
	{
		super.onPause();
		try
		{
			//this.unregisterReceiver(headPhoneReceiver);
		}
		catch(Exception ex) { }

	}

	private void LoadPreferences()
	{

		//Load up the settings from preferences
		SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
		shuffle = settings.getBoolean("shuffle",false);
		playAllSongs = settings.getBoolean("playAllSongs",false);
		playAllArtist = settings.getBoolean("playAllArtist", true);
		playAllAlbum = settings.getBoolean("playAllAlbum",true);
		playAllPlaylist = settings.getBoolean("playAllPlaylist",true);
		showWelcomeScreen = settings.getBoolean("showWelcome", true);
		playFirst = settings.getBoolean("playFirst",false);
		broadSearch = settings.getBoolean("broadSearch",true);



		dataStore = (MyDataStore)getLastNonConfigurationInstance();
		if(dataStore != null)
		{
			isDialogShowing = dataStore.ismIsDialogShowing();
			searchResults = dataStore.getmSongResults();
			if(isDialogShowing)
			{
				DisplaySongDialog(dataStore.ismAlbumSearch(),dataStore.getmCommand(),dataStore.getmRequestCode());
			}
			oneFilePath = dataStore.getmOneFilePath();
			loadingSongList = dataStore.ismLoadingSongList();
		}


	}

	private ServiceConnection mConnection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName className, IBinder service) 
		{
			
			if(mBoundService == null )
			{
				Log.i("Notification","binding mBoundService in onServiceConnected");
				mBoundService = ((MediaPlayerService.LocalBinder)service).getService();
				Log.i("Notification","setting mBoundService handler");
				mBoundService.setHandler(handler);
			}


			

		}

		public void onServiceDisconnected(ComponentName className)
		{
			Log.i("Notification","service disconnected");
			//mBoundService.setHandler(null);
			mBoundService =  null;
		}
	};

	void doBindService()
	{
		bindService(new Intent(HandsFreeMusic.this,MediaPlayerService.class),mConnection,Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService()
	{
		try
		{
			unbindService(mConnection);

			mIsBound = false;
		} catch(Exception ex) { }

	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) 
	{
		if(mBoundService != null && mBoundService.isPlaying())
		{
			Resources res = getResources();
			Drawable myImage = res.getDrawable(R.drawable.ic_music_play);
			findViewById(R.id.pausePlayButton).setBackgroundDrawable(myImage);
			mHandler.removeCallbacks(mUpdateTimeTask);
			mBoundService.pause();
		}
		
		switch(item.getItemId())
		{
		case R.id.menuSong:
			startVoiceRecognitionActivity(SONG_CODE,"Please say a song name.");
			return true;
		case R.id.menuArtist:
			startVoiceRecognitionActivity(ARTIST_CODE,"Please say an artist name.");
			return true;
		case R.id.menuAlbum:
			startVoiceRecognitionActivity(ALBUM_CODE,"Please say an album name.");
			return true;
		case R.id.menuPlaylist:
			startVoiceRecognitionActivity(PLAYLIST_CODE,"Please say a playlist name.");
			return true;
		case R.id.menuYoutube:
			startVoiceRecognitionActivity(YOUTUBE_CODE,"Please say a song name to search for on YouTube");
		default:
			return super.onContextItemSelected(item);
		}
	}
	protected Dialog onCreateDialog(int id)

	{
		switch(id)
		{
		case PROGRESS_DIALOG:
			progressDialog = new ProgressDialog(HandsFreeMusic.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("Updating Song Index. Please wait. This should take less then a minute and only needs to happen once!");
			progressDialog.setCancelable(false);

				progressThread = new ProgressThread(handler);
				progressThread.start();
			return progressDialog;
//		case YOUTUBE_PROGRESS_DIALOG:
//			youtubeProgressDialog = new ProgressDialog(HandsFreeMusic.this);
//			youtubeProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//			youtubeProgressDialog.setMessage("Loading song list. Please wait.");
//			youtubeProgressDialog.setCancelable(false);
//			
//			youtubeProgressThread = new LoadSongListProgressThread(handler);
//			youtubeProgressThread.start();
//			return youtubeProgressDialog;

		default:
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflator = getMenuInflater();
		inflator.inflate(R.menu.menu,menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.preferences:
			//Create a new intent for preferences screen.
			Intent prefIntent = new Intent(getApplicationContext(),PreferencesActivity.class);
			startActivityForResult(prefIntent,CHOOSE_PREFERENCES);
			return true;
		case R.id.setBinds:
			Intent bindIntent = new Intent(getApplicationContext(),BindActivity.class);
			startActivity(bindIntent);
			return true;
		case R.id.help:
			Intent helpIntent = new Intent(getApplicationContext(),HelpActivity.class);
			startActivity(helpIntent);
			return true;
		case R.id.quit:
			try
			{

				mBoundService.stopSelf();

				doUnbindService(); 

				mBoundService = null;
				((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
			}
			catch(Exception ex) { }
			this.finish();
			return true;
		case R.id.donate:
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("market://details?id=com.quackware.handsfreemusic")); 


			this.startActivity(intent); 
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Makes sure that this application is capable of running on the phone environment.
	 * Checks for issues such as missing media, missing voice recorder, etc.
	 */
	private void VerifyEnvironment()
	{
		//Check to see if we need to send a crash report

		ErrorReporter report = new ErrorReporter();
		report.Init(this);
		report.CheckErrorAndSendMail(this);


		/**
		 * START REGION CHECK EXTERNAL STORAGE
		 */
		boolean mExternalStorageAvailable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
		} else {
			// Something else is wrong. It may be one of many other states, but all we need
			//  to know is we can neither read nor write
			mExternalStorageAvailable = false;
		}
		if(!mExternalStorageAvailable)
		{
			ThrowError(CUSTOM_ERROR,new Exception("Cannot access media card: Environment Check"),"Unable to access media. Please ensure that you have a sdcard attached " +
					"to your phone",true);
		}
		/**
		 * END REGION CHECK EXTERNAL STORAGE
		 */

		/**
		 * START REGION CHECK FOR SPEECH RECOGNITION
		 */
		try
		{
			PackageManager pm = getPackageManager();
			List<ResolveInfo> activities = pm.queryIntentActivities(
					new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
			if (activities.size() == 0) {
				//Can't get recognizer, tell the user and terminate.
				ThrowError(CUSTOM_ERROR,new Exception("Voice recognizer not recognized: Environment Check"),"Unable to find voice recognizer. Either the application needs to be reinstalled or your phone is not supported.",true);
			}
		}
		catch(Exception ex)
		{
			ThrowError(CUSTOM_ERROR,ex,"Unable to find voice recognizer. Either the application needs to be reinstalled or your phone is not supported.",true);
		}
		/**
		 * END REGION CHECK FOR SPEECH RECOGNITION
		 */

		/**
		 * START REGION DATABASE CHECK
		 */
		
		//Check to see if we need to create a new database.
		SQLiteDatabase checkDB = null;
		try
		{
			String myPath = "/data/data/" + HandsFreeMusic.this.getPackageName() + "/databases/" + DATABASE_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath,null,SQLiteDatabase.OPEN_READONLY);
		}
		catch(Exception e)
		{
			if(!loadingSongList)
			{
				loadingSongList = true;
				showDialog(PROGRESS_DIALOG);
			}
		}
		finally
		{
			if(checkDB != null)
			{
				checkDB.close();
				checkDB = null;	
			}

		}


		if(!loadingSongList)
		{
			try
			{
				myDBHelper = new MyDbHelper(this);
				SQLiteDatabase tmpDB = myDBHelper.getWritableDatabase();
				if(MyDbHelper.rebuild && !loadingSongList)
				{
					tmpDB.close();
					myDBHelper.close();

					myDBHelper = null;
					
					loadingSongList = true;
					showDialog(PROGRESS_DIALOG);
				}
				else
				{
					tmpDB.close();
					myDBHelper.close();

					myDBHelper = null;
				}
				
				//SQLiteDatabase db = myDBHelper.getReadableDatabase();


			}
			catch(Exception ex)
			{
				ThrowError(DATABASE_ERROR,ex,null,true);
			}
		}
		/**
		 * END REGION DATABASE CHECK
		 */
		
		
	}




	public void onClick(View v) {

		if(mBoundService == null)
		{
			return;
		}
		LoadPreferences();
		Resources res = this.getResources();
		//Check to see what button was clicked and set the appropriate value
		switch(v.getId())
		{
		case R.id.speakButton:
			if(mBoundService.isPlaying())
			{
				Drawable myImage = res.getDrawable(R.drawable.ic_music_play);
				findViewById(R.id.pausePlayButton).setBackgroundDrawable(myImage);
				mHandler.removeCallbacks(mUpdateTimeTask);
				mBoundService.pause();
			}

			startVoiceRecognitionActivity(VOICE_RECOGNITION_REQUEST_CODE,"Please say a command, such as \"song <songname>\" or \"playlist <playlistname>\". View help for a list of commands.");
			//YouTubeUtility.testYouTube("feel+good");


			break;
		case R.id.pausePlayButton:
			if(mBoundService.isPlaying())
			{
				mHandler.removeCallbacks(mUpdateTimeTask);
				mBoundService.pause();

				Drawable myImage = res.getDrawable(R.drawable.ic_music_play);
				findViewById(R.id.pausePlayButton).setBackgroundDrawable(myImage);
			}
			else
			{
				try
				{

					if(mBoundService.getDataSource() != null)
					{
						mHandler.postDelayed(mUpdateTimeTask, 100);
						Log.i("info","start called in pause play button");
						mBoundService.start();

						Drawable myImage = res.getDrawable(R.drawable.ic_music_pause);
						findViewById(R.id.pausePlayButton).setBackgroundDrawable(myImage);
					}
				}
				catch(Exception ex) { }
			}
			break;
		case R.id.nextButton:
			try
			{
				if(mBoundService.getShuffleSongs().length > mBoundService.getShuffleIndex())
				{
					mHandler.removeCallbacks(mUpdateTimeTask);
					mBoundService.playNextSong();
					SetSongText(playingYoutube);
				}
			} catch(Exception ex) { }
			break;
		case R.id.previousButton:
			try
			{
				mHandler.removeCallbacks(mUpdateTimeTask);
				//Check to see if we are only playing one song.
				if(mBoundService.getShuffleSongs() != null)
				{
					mBoundService.playPreviousSong();
				}
				else
				{
					Drawable myImage = res.getDrawable(R.drawable.ic_music_play);
					findViewById(R.id.pausePlayButton).setBackgroundDrawable(myImage);
					if(oneFilePath != null)
					{
						PlayMusic(oneFilePath,playingYoutube);
						SetSongText(playingYoutube);
					}
				}

			} catch(Exception ex) { }
			break;
		default:
			break;
		}

	}
	@Override
	protected void onDestroy()
	{    	
		super.onDestroy();
		//progressThread = null;
		//progressDialog = null;
		doUnbindService();
		if(myDB != null && myDBHelper != null)
		{
			myDBHelper.close();
			myDB.close();
		}
	}

	private void startVoiceRecognitionActivity(int voiceCode, String message) {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, message);
		startActivityForResult(intent, voiceCode);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		switch(requestCode)
		{
		case CHOOSE_PREFERENCES:
			LoadPreferences();
			break;
		case VOICE_RECOGNITION_REQUEST_CODE:
			if(resultCode == RESULT_OK)
			{
				ArrayList<String> matches = data.getStringArrayListExtra(
						RecognizerIntent.EXTRA_RESULTS);
				String command = matches.get(0);
				RetrieveMusic(command.toLowerCase(),0);
			}
			break;
		case SONG_CODE:
		case ALBUM_CODE:
		case ARTIST_CODE:
		case PLAYLIST_CODE:
		case YOUTUBE_CODE:
			if(resultCode == RESULT_OK)
			{
				ArrayList<String> matches = data.getStringArrayListExtra(
						RecognizerIntent.EXTRA_RESULTS);
				String command = matches.get(0);
				RetrieveMusic(command.toLowerCase(),requestCode);
			}
			break;
			
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;

		}

	}
	
	private int DetermineRequestCode(String command)
	{
		//Make sure they did not say something like "play " before the command.
		if(command.startsWith("play "))
		{
			command.replaceFirst("play ", "");
		}
		
		if(command.startsWith("song"))
		{
			return SONG_CODE;
		}
		else if(command.startsWith("artist"))
		{
			return ARTIST_CODE;
		}
		else if (command.startsWith("album"))
		{
			return ALBUM_CODE;
		}
		else if(command.startsWith("playlist"))
		{
			return PLAYLIST_CODE;
		}
		else if(command.startsWith("youtube") || command.startsWith("you tube"))
		{
			return YOUTUBE_CODE;
		}
		else
		{
			//If we cannot find anything that matches just search by song.
			return SONG_CODE;
		}
	}
	
	private String StripPlayCommand(int requestCode, String command)
	{
		switch(requestCode)
		{
		case SONG_CODE:
			return command.replaceFirst("song ", "");
		case ARTIST_CODE:
			return command.replaceFirst("artist ", "");
		case ALBUM_CODE:
			return command.replaceFirst("album ", "");
		case PLAYLIST_CODE:
			return command.replaceFirst("playlist ", "");
		case YOUTUBE_CODE:
			return command.replaceFirst("youtube ", "").replaceFirst("you tube","");
		default:
			return command.replaceFirst("song ", "");
		}
	}

	private void RetrieveMusic(String command, int providedRequest)
	{
		
		//Check to see if there are any binds we need to deal with.
		try{
		command = CheckBinds(command);
		}
		catch(Exception ex) { }
		
		int requestCode;
		if(providedRequest != 0)
		{
			requestCode = providedRequest;
		}
		else
		{
			requestCode = DetermineRequestCode(command);
			command = StripPlayCommand(requestCode,command);
		}
		
		searchResults = Match(command,requestCode);

		if(requestCode != YOUTUBE_CODE)
		{
			playingYoutube = false;
			if(searchResults == null || searchResults[0].length <= 0)
			{
				ThrowError(NOT_FOUND_ERROR,new Exception("searchResults was either null or searchResults[0].length was less then 0"),command,false);
				return; 		
			}
			else
			{
				boolean albumSearch = false;
				shuffleSongs = null;
				mBoundService.setSearchResults(searchResults);

				if(searchResults[0].length == 1)
				{
					oneFilePath = (String)searchResults[0][0];
					dataStore = new MyDataStore(searchResults,shuffleSongs,albumSearch,command,isDialogShowing,oneFilePath,requestCode,loadingSongList);
					PlayMusic(searchResults[0][0],playingYoutube);
					SetSongText(playingYoutube);
				}
				else
				{
					switch(requestCode)
					{
					case SONG_CODE:
						if(playAllSongs)
						{
							shuffleSongs = searchResults[0].clone();
						}
						else
						{
							shuffleSongs = null;
						}
						break;
					case ARTIST_CODE:
						if(playAllArtist)
						{
							shuffleSongs = searchResults[0].clone();
						}
						else
						{
							shuffleSongs = null;
						}
						break;
					case ALBUM_CODE:
						albumSearch = true;
						if(playAllAlbum)
						{
							shuffleSongs = searchResults[0].clone();
						}
						else
						{
							shuffleSongs = null;
						}
						break;
					case PLAYLIST_CODE:
						if(playAllPlaylist)
						{
							shuffleSongs = searchResults[0].clone();
						}
						else
						{
							shuffleSongs = null;
						}
					}

					isDialogShowing = true;
					dataStore = new MyDataStore(searchResults,shuffleSongs,albumSearch,command,isDialogShowing,oneFilePath,requestCode,
							loadingSongList);
					DisplaySongDialog(albumSearch,command,requestCode);
				}
			}
		}
		else
		{
			if(searchResults == null || searchResults[0].length <= 0)
			{
				ThrowError(NOT_FOUND_ERROR,new Exception("searchResults was either null or searchResults[0].length was less then 0"),command,false);
				return; 		
			}
			else
			{
				mBoundService.setSearchResults(searchResults);
				isDialogShowing = true;
				DisplaySongDialog(false,command,requestCode); 
			}
		}
	}

	private CharSequence[][] Match(String command, int requestCode)
	{
		myDBHelper = new MyDbHelper(getApplicationContext());
		myDB = myDBHelper.getReadableDatabase();

		
		//Remove crap like the, and, or
//		command = command.replace("the", "").replace("to","").replace("or", "").replace("and", "").replace("to", "").replace("of","").replace("this","").replace("is","")
//		.replace(" i ","").trim();
//		command = command.replaceAll("\\s+", " ");
		if(command.equals(""))
		{
			return null;
		}
		String[] commandParts = null;
		//Check to see if it is broad search or not, in which case our search will be a little different.
		if(broadSearch)
		{
			commandParts = command.split(" ");
			for(int i = 0;i<commandParts.length;i++)
			{
				commandParts[i] = "%" + commandParts[i].replace("'", "") + "%";
			}
		}

		Cursor c = null;
		//Query based on the command type
		switch(requestCode)
		{
		case SONG_CODE:
			try
			{
				if(broadSearch)
				{
					String commandString = "LOWER(SONG) LIKE ";
					for(int i = 0;i<commandParts.length;i++)
					{
						if(i == 0)
						{
							commandString += "'" + commandParts[i] + "'";
						}
						else if (i > 0)
						{
							commandString += " OR Lower(Song) LIKE '" + commandParts[i] + "'";
						}
					}
					c = myDB.query(SONG_DATABASE_TABLE, DATABASE_COLUMNS, commandString,
							null, null,null,null);
				}
				else
				{
					c = myDB.query(SONG_DATABASE_TABLE, DATABASE_COLUMNS, "LOWER(SONG) LIKE '%?%'",
							new String[] {command.toLowerCase() }, null,null,null);
				}

			}
			catch(Exception ex)
			{
				ThrowError(DATABASE_ERROR,ex,null,false);
				return null;
			}
			break;
		case ALBUM_CODE:
			try
			{
				if(broadSearch)
				{
					String commandString = "LOWER(ALBUM) LIKE ";
					for(int i = 0;i<commandParts.length;i++)
					{
						if(i == 0)
						{
							commandString += "'" + commandParts[i] + "'";
						}
						else if (i > 0)
						{
							commandString += " OR Lower(ALBUM) LIKE '" + commandParts[i] + "'";
						}
					}
					c = myDB.query(SONG_DATABASE_TABLE, DATABASE_COLUMNS, commandString,
							null, null,null,null);
				}
				else
				{
					c = myDB.query(SONG_DATABASE_TABLE, DATABASE_COLUMNS, "LOWER(ALBUM) LIKE '%?%'",
							new String[] {command.toLowerCase() }, null,null,null);
				}

			}
			catch(Exception ex)
			{
				ThrowError(DATABASE_ERROR,ex,null,false);
				return null;
			}
			break;
		case ARTIST_CODE:
			try
			{
				if(broadSearch)
				{
					String commandString = "LOWER(ARTIST) LIKE ";
					for(int i = 0;i<commandParts.length;i++)
					{
						if(i == 0)
						{
							commandString += "'" + commandParts[i] + "'";
						}
						else if (i > 0)
						{
							commandString += " OR Lower(ARTIST) LIKE '" + commandParts[i] + "'";
						}
					}
					c = myDB.query(SONG_DATABASE_TABLE, DATABASE_COLUMNS, commandString,
							null, null,null,null);
				}
				else
				{
					c = myDB.query(SONG_DATABASE_TABLE, DATABASE_COLUMNS, "LOWER(ARTIST) LIKE '%?%'",
							new String[] {command.toLowerCase() }, null,null,null);
				}

			}
			catch(Exception ex)
			{
				ThrowError(DATABASE_ERROR,ex,null,false);
				return null;
			}
			break;
		case PLAYLIST_CODE:
			try
			{
				if(broadSearch)
				{
					String commandString = "LOWER(PLAYLIST) LIKE ";
					for(int i = 0;i<commandParts.length;i++)
					{
						if(i == 0)
						{
							commandString += "'" + commandParts[i] + "'";
						}
						else if (i > 0)
						{
							commandString += " OR LOWER(PLAYLIST) LIKE '" + commandParts[i] + "'";
						}
					}
					c = myDB.query(SONG_DATABASE_TABLE, DATABASE_COLUMNS, commandString,
							null, null,null,null);
				}
				else
				{
					c = myDB.query(SONG_DATABASE_TABLE, DATABASE_COLUMNS, "LOWER(PLAYLIST) LIKE '%?%'",
							new String[] {command.toLowerCase() }, null,null,null);
				}

			}
			catch(Exception ex)
			{
				ThrowError(DATABASE_ERROR,ex,null,false);
				
				return null;
			}
			break;
		case YOUTUBE_CODE:
			//This one is different from the others because we are actually querying youtube for info.
			command = command.replace(" ", "+");
			Object content = null;
			URL url = null;

			try 
			{
				url = new URL("http://m.youtube.com/results?client=mv-google&gl=US&hl=en&q=" + command);
			} 
			catch (MalformedURLException e) 
			{
				// TODO Auto-generated catch block
				ThrowError(YOUTUBE_ERROR,e,null,false);
				return null;
			}
			String html = Utility.getSourceCode(url);
			if(html != null)
			{
				searchResults = Utility.retrieveYouTubeTitleAndUrl(html);
				//showDialog(YOUTUBE_PROGRESS_DIALOG);
			}
			else
			{
				ThrowError(YOUTUBE_ERROR,new Exception("Unable to retrieve the html of youtube page."),null,false);
				return null;
			}

			break;
		default:
			return null;

		}

		if(requestCode != YOUTUBE_CODE)
		{
			if(c.getCount() < 1)
			{
				ThrowError(NOT_FOUND_ERROR,new Exception("c.getCount() < 0. Typical not found error"),command,false);
				return null;
			}
			searchResults = new CharSequence[4][c.getCount()];
			int inc = 0;
			//c.moveToFirst();
			while(c.moveToNext())
			{
				searchResults[0][inc] = c.getString(c.getColumnIndex("PATH"));
				searchResults[1][inc] = c.getString(c.getColumnIndex("SONG"));
				searchResults[2][inc] = c.getString(c.getColumnIndex("ARTIST"));
				searchResults[3][inc] = c.getString(c.getColumnIndex("ALBUM"));
				//searchResults[4][inc] = c.getString(c.getColumnIndex("PLAYLIST"));
				inc++;
			}
			c.close();
		}
		myDB.close();
		myDBHelper.close();
		return searchResults;
	}
	
	private String CheckBinds(String command)
	{
		MyDbHelper helper = new MyDbHelper(this);
		SQLiteDatabase bindDB = helper.getWritableDatabase();
		Cursor c = bindDB.query("t_Binds", new String[] {"BIND","REPLACE"},
				null, null,null,null,null);
		if(c.getCount() > 0)
		{
			c.moveToFirst();
			do
			{
				String bind = c.getString(c.getColumnIndex("BIND"));
				String replace = c.getString(c.getColumnIndex("REPLACE"));
				if(command.contains(bind))
				{
					command = command.replace(bind, replace);
				}
			}
			while(c.moveToNext());
		}
		c.close();
		helper.close();
		bindDB.close();
		return command;
	}


	private void PlayMusic(CharSequence file, boolean playingYoutube)
	{		 

		Resources res = this.getResources();
		Drawable myImage = res.getDrawable(R.drawable.ic_music_pause);
		findViewById(R.id.pausePlayButton).setBackgroundDrawable(myImage);
		mBoundService.playFile(file.toString());

		
		mHandler.removeCallbacks(mUpdateTimeTask);
		Log.i("info","post delayed called 1149");
		mHandler.postDelayed(mUpdateTimeTask, 1);

		totalTime.setText(mBoundService.getTotalTime());

		seekbar.setMax(mBoundService.getDuration());
	}

	private void DisplaySongDialog(final boolean albumSearch,final String command,final int requestCode)
	{
		int length = searchResults[1].length;
		final CharSequence[] tmp = new CharSequence[length];
		if(!albumSearch)
		{
			if(requestCode != YOUTUBE_CODE)
			{
				for(int i = 0;i<length;i++)
				{
					tmp[i] = searchResults[1][i] + "\n" + searchResults[2][i];
				}
			}
			else
			{
				for(int i = 0;i<length;i++)
				{
					tmp[i] = searchResults[1][i];
				}
			}
		}
		else
		{
			for(int i = 0;i<length;i++)
			{
				tmp[i] = searchResults[1][i] + "\n" + searchResults[2][i] + "\n" + searchResults[3][i];
			}
		}

		if(!playFirst)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Select a song. You said: " + command);
			builder.setItems(tmp, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					PerformDialogShuffleAndPlay(item,albumSearch,command,requestCode);
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
		else
		{
			PerformDialogShuffleAndPlay(0,albumSearch,command,requestCode);

		}

	}

	private void PerformDialogShuffleAndPlay(int item, boolean albumSearch, String command,int requestCode)
	{
		//shuffleSongs = searchResults[0].clone();
		if(mBoundService.isPlaying())
		{
			//mBoundService.stop();
			mBoundService.pause();
		}
		
		if(shuffleSongs != null)
		{
			if(shuffle && requestCode != YOUTUBE_CODE)
			{
				Collections.shuffle(Arrays.asList(shuffleSongs));
				int i = Arrays.asList(shuffleSongs).indexOf(searchResults[0][item]);
				CharSequence tmp = shuffleSongs[0];
				shuffleSongs[0] = shuffleSongs[i];
				shuffleSongs[i] = tmp;
			}
			else
			{
				mBoundService.setShuffleIndex(item);
			}
			mBoundService.incShuffleIndex();
			mBoundService.setShuffleSongs(shuffleSongs);
		}
		else
		{
			mBoundService.setShuffleSongs(null);
		}
		isDialogShowing = false;
		switch(requestCode)
		{
		case SONG_CODE:
		case ARTIST_CODE:
		case PLAYLIST_CODE:
		case YOUTUBE_CODE:
			oneFilePath = (String) searchResults[0][item];
			break;
		case ALBUM_CODE:
			albumSearch = true;
			oneFilePath = (String) searchResults[0][item];
			break;
			
		}
		if(requestCode == YOUTUBE_CODE)
		{
			playingYoutube = true;
		}
		dataStore = new MyDataStore(searchResults,shuffleSongs,albumSearch,command,isDialogShowing,oneFilePath,requestCode,
				loadingSongList);
		PlayMusic(searchResults[0][item],playingYoutube);
		SetSongText(playingYoutube);
	}

	public Object onRetainNonConfigurationInstance()
	{
		if(dataStore != null)
		{
			dataStore.setmOneFilePath(oneFilePath);
			return dataStore;
		}
		else
		{


			dataStore = (MyDataStore)getLastNonConfigurationInstance();
			if(dataStore == null)
			{
				dataStore = new MyDataStore(searchResults,shuffleSongs,false,null,false,oneFilePath,0,
						loadingSongList);
			}
			dataStore.setmOneFilePath(oneFilePath);
			return dataStore;

		}

	}

	private void SetSongText(boolean playingYoutube)
	{
		TextView t = (TextView)findViewById(R.id.currentlyPlaying);
		t.setText(mBoundService.getSongText());

	}

	/**
	 * Throw an error based on what is provided.
	 * @param errorCode The error code of the error.
	 * @param ex The exception if any.
	 * @param command The error message for custom error.
	 * @param doExit Whether to exit the program or not.
	 */
	private void ThrowError(int errorCode, Exception ex,String command,boolean doExit)
	{
		int length = Toast.LENGTH_LONG;
		String errorMessage = "";
		switch(errorCode)
		{
		case GENERIC_ERROR:
			errorMessage = "An unexpected error occured";
			length = Toast.LENGTH_LONG;
			break;
		case NOT_FOUND_ERROR:
			length = Toast.LENGTH_SHORT;
			if(!broadSearch)
			{
				errorMessage = "Song not found. Try using broad search or setting a bind!  What we heard you say: " + command;
			}
			else
			{
				errorMessage = "Song not found. Try setting a bind!  What we heard you say: " + command;
			}
			//Also need to set the textview back to its original state.
			TextView t = (TextView)findViewById(R.id.currentlyPlaying);
			songText = "No Song Playing.";
			t.setText(songText);
			break;
		case DATABASE_ERROR:
			errorMessage = "Unable to query database. Please try reinstalling. If problem persists then contact developer.";
			length = Toast.LENGTH_LONG;
			break;
		case MEDIA_ERROR:
			errorMessage = "Unable to find any media on your device. Please ensure you have media on your sd card. If you do, then please contact the developer for further information.";
			length = Toast.LENGTH_LONG;
		case YOUTUBE_ERROR:
			errorMessage = "Unable to retrieve music from YouTube. Please try again in a few minutes.";
			length = Toast.LENGTH_SHORT;
		case CUSTOM_ERROR:
			length = Toast.LENGTH_LONG;
			errorMessage = command;
			break;
		default:
			break;
		}
		Toast toast = Toast.makeText(getApplicationContext(),
				errorMessage, length);
		toast.show();

		if(ex!=null)
		{
			Log.e(errorMessage, ex.getMessage());
		}
		if(doExit)
		{
			finish();
		}
	}

	private Handler mHandler = new Handler();

	final Handler handler = new Handler()
	{


		public void handleMessage(Message msg)
		{
			switch(msg.getData().getInt("messageType"))
			{
			case 1:
				isLoaded = msg.getData().getBoolean("status");
				try
				{
					dismissDialog(PROGRESS_DIALOG);
				}
				catch(Exception ex)
				{ }
				progressDialog.dismiss();
				try {
					this.finalize();
				} catch (Throwable e) {

					e.printStackTrace();
				}
				break;
			case 2:
				SetSongText(playingYoutube);

				totalTime.setText(mBoundService.getTotalTime());
				seekbar.setMax(mBoundService.getDuration());
				seekbar.setProgress(1);
				mHandler.removeCallbacks(mUpdateTimeTask);
				mHandler.postDelayed(mUpdateTimeTask, 100);


				break;
			default:
				break;
			}

		}
	};


	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {

			Log.i("Notification","mUpdateTimeTask is calling isplaying()");
			if (mBoundService != null) {
				
				currentTime.setText(mBoundService.getCurrentTime());
				seekbar.setProgress(mBoundService.getCurrentPosition());
				
				mHandler.postAtTime(this,SystemClock.uptimeMillis() + 1000);
			}
			else
			{
				Resources res = HandsFreeMusic.this.getResources();
				Drawable myImage = res.getDrawable(R.drawable.ic_music_play);
				findViewById(R.id.pausePlayButton).setBackgroundDrawable(myImage);
			}
		}

	};
	
	private class LoadSongListProgressThread extends Thread
	{
		Handler mHandler;

		LoadSongListProgressThread(Handler h)
		{
			mHandler = h;
		}

		public void run()
		{
			//searchResults = Utility.retrieveYouTubeTitleAndUrl(youtubeMatches);
			
			Message msg = mHandler.obtainMessage();
			Bundle b = new Bundle();
			b.putInt("messageType", 3);
			msg.setData(b);
			mHandler.sendMessage(msg);
			mHandler = null;
			try {
				this.finalize();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	private class ProgressThread extends Thread
	{
		Handler mHandler;

		ProgressThread(Handler h)
		{
			mHandler = h;
		}

		public void run()
		{

			try
			{
				LoadSongList();
				MyDbHelper.rebuild = false;
			}
			catch(Exception ex)
			{
				myDBHelper = new MyDbHelper(HandsFreeMusic.this);
		     	myDB = myDBHelper.getWritableDatabase();
		     	myDB.delete("t_Songs",null,null);
		     	myDB.close();
		     	myDBHelper.close();
		     	MyDbHelper.rebuild = true;
			}
			Message msg = mHandler.obtainMessage();
			Bundle b = new Bundle();
			b.putBoolean("status", true);
			b.putInt("messageType", 1);
			msg.setData(b);
			mHandler.sendMessage(msg);
			mHandler = null;
			try {
				this.finalize();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		private void LoadSongList()
		{

			myDBHelper = new MyDbHelper(HandsFreeMusic.this.getApplicationContext());
			myDB = myDBHelper.getReadableDatabase();
			String artist = null;
			String filePath = null;
			String song = null;
			String album = null;
			String playlist = null;

			Uri Uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI; //uri to sd-card
			//        String[] Selection = new String[] {
			//                        android.provider.MediaStore.Audio.Media._ID,
			//                        android.provider.MediaStore.Audio.Media.TITLE,
			//                        android.provider.MediaStore.Audio.Media.DATA,
			//                        android.provider.MediaStore.Audio.Media.ARTIST,
			//                        android.provider.MediaStore.Audio.Media.ALBUM, };
			//        Cursor mCursor = managedQuery(Uri, Selection,null, null, null);
			//        mCursor.moveToFirst();

			ArrayList<ArrayList<String>> songInformation = getRealPathsFromURI(Uri);
			if(songInformation == null)
			{
				Toast toast = Toast.makeText(HandsFreeMusic.this.getApplicationContext(),
						"Unable to find any media on your device. Please ensure you have media on your sd card. If you do, then please contact the developer for further information.", Toast.LENGTH_LONG);
				toast.show();
				return;
			}
			ArrayList<String> realPaths = songInformation.get(0);
			ArrayList<String> songList = songInformation.get(1);
			ArrayList<String> artistList = songInformation.get(2);
			ArrayList<String> albumList = songInformation.get(3);
			ArrayList<String> playlistList = getPlaylistInfo(songInformation);

			if(realPaths == null || songList == null || artistList == null || albumList == null)
			{	
				Toast toast = Toast.makeText(HandsFreeMusic.this.getApplicationContext(),
						"Unable to find any media on your device. Please ensure you have media on your sd card. If you do, then please contact the developer for further information.", Toast.LENGTH_LONG);
				toast.show();
				return;
			}
			int inc = 0;

			while(inc < realPaths.size())
			{

				try {
					artist = artistList.get(inc);
				} catch(Exception ex) {artist = null;}

				try {
					album = albumList.get(inc);
				} catch(Exception ex) {album = null; }
				try {
					song = songList.get(inc);
				} catch(Exception ex) {song = null; }
				try {
					filePath = realPaths.get(inc);
				} 
				catch(Exception ex)
				{
					filePath = null;}
				try {
					playlist = playlistList.get(inc);
				}
				catch(Exception ex)
				{
					playlist = null;
				}

				ContentValues values = new ContentValues(5);
				values.put("PATH", filePath);
				values.put("SONG", song);
				values.put("ARTIST", artist);
				values.put("ALBUM",album);
				values.put("PLAYLIST", playlist);
				myDB.insert("t_Songs", null, values);
				inc++;

			}
				
			try
			{
				myDB.close();
				myDBHelper.close();
			}
			catch(Exception ex)
			{
				
			}
			
		}

		public ArrayList<String> getPlaylistInfo(ArrayList<ArrayList<String>> songInformation)
		{
			String[] columns = new String[] {MediaStore.Audio.Playlists._ID, MediaStore.Audio.Playlists.NAME};
			ArrayList<String> pathList = new ArrayList<String>();
			ArrayList<String> nameList = new ArrayList<String>();
			Uri playlistUri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
			Cursor cursor = managedQuery( playlistUri,  
					columns, // Which columns to return  
					null,       // WHERE clause; which rows to return (all rows)  
					null,       // WHERE clause selection arguments (none)  
					null); // Order-by clause (ascending by name)
			cursor.moveToFirst();
			for(int i = 0;i<cursor.getCount();i++, cursor.moveToNext())
			{
				int id = cursor.getInt(0);
				Uri membersUri = MediaStore.Audio.Playlists.Members.getContentUri("external", id);
				Cursor membersCursor = managedQuery(membersUri,new String[] {MediaStore.Audio.Media.DATA},null,null,null);
				membersCursor.moveToFirst();
				for(int j = 0;j<membersCursor.getCount();j++, membersCursor.moveToNext())
				{
					pathList.add(membersCursor.getString(membersCursor.getColumnIndex("_data")));
					nameList.add(cursor.getString(cursor.getColumnIndex("name")));
				}
				membersCursor.close();
			}
			cursor.close();
			
			ArrayList<String> playlistList = new ArrayList<String>();
			int count = songInformation.get(0).size();
			int j = 0;
			for(int i = 0;i<count;i++)
			{
				if(pathList.indexOf(songInformation.get(0).get(i)) != -1)
				{
					playlistList.add(nameList.get(j));
					j++;
				}
				else
				{
					playlistList.add(null);
				}
			}
			return playlistList;
		}
	
		// And to convert the image URI to the direct file system path of the image file  
		public ArrayList<ArrayList<String>> getRealPathsFromURI(Uri contentUri) {  
			ArrayList<ArrayList<String>> typeList = new ArrayList<ArrayList<String>>();
			ArrayList<String> pathList = new ArrayList<String>();
			ArrayList<String> titleList = new ArrayList<String>();
			ArrayList<String> artistList = new ArrayList<String>();
			ArrayList<String> albumList = new ArrayList<String>();
			// can post image  
			String [] proj={MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.IS_MUSIC,MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM};  
			Cursor cursor = managedQuery( contentUri,  
					proj, // Which columns to return  
					MediaStore.Audio.Media.IS_MUSIC + " <> 0",       // WHERE clause; which rows to return (all rows)  
					null,       // WHERE clause selection arguments (none)  
					null); // Order-by clause (ascending by name)
			int column_index;
			try
			{
			//column_index = cursor.getColumnIndexOrThrow(type);
			} catch(Exception ex) { column_index = 0; }
			try
			{
				if(cursor == null || cursor.getCount() == 0)
				{
					return null;
				}
				cursor.moveToFirst();
			}
			catch(Exception ex)
			{
				return null;
			}
			//typeList.add(cursor.getString(column_index));
			while(cursor.moveToNext())
			{
				column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
				pathList.add(cursor.getString(column_index));
				column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
				titleList.add(cursor.getString(column_index));
				column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
				artistList.add(cursor.getString(column_index));
				column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
				albumList.add(cursor.getString(column_index));
			}
			cursor.close();
			typeList.add(pathList);
			typeList.add(titleList);
			typeList.add(artistList);
			typeList.add(albumList);
		
			return typeList;
		}
	}

//
//	private class HeadPhoneReceiver extends BroadcastReceiver
//	{
//
//		@Override
//		public void onReceive(Context con, Intent intent) {
//			if(intent.getAction().equals(Intent.ACTION_HEADSET_PLUG))
//			{
//				if(mBoundService != null && mBoundService.isPlaying() && intent.getExtras().getInt("state") == 0)
//				{
//					Resources res = HandsFreeMusic.this.getResources();
//					Drawable myImage = res.getDrawable(R.drawable.ic_music_play);
//					findViewById(R.id.pausePlayButton).setBackgroundDrawable(myImage);
//					mBoundService.pause();
//				}
//			}
//
//		}
//
//	}

}






