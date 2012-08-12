/*******************************************************************************
 * Copyright (c) 2012 Curtis Larson (QuackWare).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.quackware.handsfreemusic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDbHelper extends SQLiteOpenHelper
{

	
	
	private static final String DATABASE_NAME = "handsFreeMusicDB";
	private static final String SONG_DATABASE_TABLE = "t_Songs";	
	private static final String BIND_DATABASE_TABLE = "t_Binds";
	private static final int DATABASE_VERSION = 1;
	
	public static boolean rebuild = false;
	
	private static final String SONG_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + SONG_DATABASE_TABLE + " (" +
		"_id INTEGER PRIMARY KEY, PATH TEXT, SONG TEXT, ARTIST TEXT, ALBUM TEXT, PLAYLIST TEXT);";
	
	private static final String BIND_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + BIND_DATABASE_TABLE + " (" +
		"_id INTEGER PRIMARY KEY, BIND TEXT, REPLACE TEXT);";
	
	public MyDbHelper(Context context)
	{

		super(context, DATABASE_NAME,null,DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		db.execSQL(SONG_TABLE_CREATE);
		db.execSQL(BIND_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		rebuild = true;
		db.execSQL("DROP TABLE IF EXISTS t_Songs");
		//db.execSQL("DROP TABLE IF EXISTS t_Binds");
		onCreate(db);
	}

}
