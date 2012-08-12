package com.quackware.handsfreemusic;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;

import android.content.SharedPreferences;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

public class PreferencesActivity extends Activity implements android.view.View.OnClickListener
{
	
	public static final String PREFS_NAME = "PreferencesFile";
	
	private MyDbHelper myDBHelper = null;
	private SQLiteDatabase myDB = null;
	private static final String DATABASE_NAME = "handsFreeMusicDB";
	private static final String DATABASE_TABLE = "t_Songs";	
	private static final String[] DATABASE_COLUMNS = new String[] {"PATH","SONG","ARTIST","ALBUM"};
	
	/**
	 * Progress thread and dialog variables.
	 */
	static final int PROGRESS_DIALOG = 7421;
	ProgressThread progressThread;
	ProgressDialog progressDialog;

	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        
	        setContentView(R.layout.preferences);
	        
	        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
	        boolean shuffle = settings.getBoolean("shuffle",false);
	        boolean playAllSongs = settings.getBoolean("playAllSongs",false);
	        boolean playAllArtist = settings.getBoolean("playAllArtist",true);
	        boolean playAllAlbum = settings.getBoolean("playAllAlbum",true);
	        boolean playAllPlaylist = settings.getBoolean("playAllPlaylist",true);
	        boolean showWelcome = settings.getBoolean("showWelcome",true);
	        boolean playFirst = settings.getBoolean("playFirst",false);
	        boolean broadSearch = settings.getBoolean("broadSearch",true);
	        
	        CheckBox cb = (CheckBox)findViewById(R.id.shuffleResults);
	        cb.setChecked(shuffle);
	        cb = (CheckBox)findViewById(R.id.playAllSongResults);
	        cb.setChecked(playAllSongs);
	        cb = (CheckBox)findViewById(R.id.playAllArtistResults);
	        cb.setChecked(playAllArtist);
	        cb = (CheckBox)findViewById(R.id.playAllAlbumResults);
	        cb.setChecked(playAllAlbum);
	        cb = (CheckBox)findViewById(R.id.playAllPlaylistResults);
	        cb.setChecked(playAllPlaylist);
	        cb = (CheckBox)findViewById(R.id.showWelcome);
	        cb.setChecked(showWelcome);
	        cb = (CheckBox)findViewById(R.id.playFirst);
	        cb.setChecked(playFirst);
	        cb = (CheckBox)findViewById(R.id.broadSearch);
	        cb.setChecked(broadSearch);
	        
	        Button rebuild = (Button)findViewById(R.id.rebuild);
	        rebuild.setOnClickListener(this);
	        
	        Button save = (Button)findViewById(R.id.save);
	        save.setOnClickListener(this);
	 }
	 
	 @Override
	 protected void onDestroy()
	 {
		 super.onDestroy();
		 SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
		 SharedPreferences.Editor editor = settings.edit();
		 CheckBox cb = (CheckBox)findViewById(R.id.shuffleResults);
		 editor.putBoolean("shuffle",cb.isChecked());
		 cb = (CheckBox)findViewById(R.id.playAllSongResults);
		 editor.putBoolean("playAllSongs", cb.isChecked());
		 cb = (CheckBox)findViewById(R.id.playAllArtistResults);
		 editor.putBoolean("playAllArtist", cb.isChecked());
		 cb = (CheckBox)findViewById(R.id.playAllAlbumResults);
		 editor.putBoolean("playAllAlbum", cb.isChecked());
		 cb = (CheckBox)findViewById(R.id.playAllPlaylistResults);
		 editor.putBoolean("playAllPlaylist", cb.isChecked());
		 cb = (CheckBox)findViewById(R.id.showWelcome);
		 editor.putBoolean("showWelcome", cb.isChecked());
		 cb = (CheckBox)findViewById(R.id.playFirst);
		 editor.putBoolean("playFirst",cb.isChecked());
		 cb = (CheckBox)findViewById(R.id.broadSearch);
		 editor.putBoolean("broadSearch", cb.isChecked());
		 editor.commit();
	 }
	 
	 protected Dialog onCreateDialog(int id)

	    {
	    	switch(id)
	    	{
	    	case PROGRESS_DIALOG:
	    		progressDialog = new ProgressDialog(PreferencesActivity.this);
	    		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	    		progressDialog.setMessage("Updating Song Index. Please wait. This should take less then a minute.");
	    		progressThread = new ProgressThread(handler);
	    		progressThread.start();
	    		return progressDialog;
	    	default:
	    		return null;
	    	}
	    }
	 
		final Handler handler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				//isLoaded = msg.getData().getBoolean("status");
				try
				{
					dismissDialog(PROGRESS_DIALOG);
				} catch(Exception ex) { }	
				try {
					this.finalize();
				} catch (Throwable e) {
					
					e.printStackTrace();
				}
			}
		};
	 


		private class ProgressThread extends Thread
		{
			Handler mHandler;

			ProgressThread(Handler h)
			{
				mHandler = h;
			}

			public void run()
			{
				LoadSongList();

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
				myDBHelper = new MyDbHelper(PreferencesActivity.this);
		     	myDB = myDBHelper.getWritableDatabase();
		     	myDB.delete(DATABASE_TABLE,null,null);
		     	myDB.close();
			    myDB = myDBHelper.getWritableDatabase();
				 
				
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
				
				ArrayList<String> realPaths = songInformation.get(0);
				ArrayList<String> songList = songInformation.get(1);
				ArrayList<String> artistList = songInformation.get(2);
				ArrayList<String> albumList = songInformation.get(3);
				ArrayList<String> playlistList = getPlaylistInfo(songInformation);

				if(realPaths == null || songList == null || artistList == null || albumList == null)
				{	
					Toast toast = Toast.makeText(PreferencesActivity.this.getApplicationContext(),
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
				myDB.close();
				myDBHelper.close();
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
				String [] proj={MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM};  
				Cursor cursor = managedQuery( contentUri,  
						proj, // Which columns to return  
						null,       // WHERE clause; which rows to return (all rows)  
						null,       // WHERE clause selection arguments (none)  
						null); // Order-by clause (ascending by name)
				int column_index;
				try
				{
				//column_index = cursor.getColumnIndexOrThrow(type);
				} catch(Exception ex) { column_index = 0; }
				try
				{
					if(cursor.getCount() == 0)
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





	 
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.rebuild:
			showDialog(PROGRESS_DIALOG);
			break;
		case R.id.save:
			this.finish();
			break;
		default:
			break;
		}
		
	}


}
