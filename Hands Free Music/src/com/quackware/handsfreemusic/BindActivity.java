/*******************************************************************************
 * Copyright (c) 2012 Curtis Larson (QuackWare).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.quackware.handsfreemusic;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.TwoLineListItem;
import android.widget.AdapterView.OnItemClickListener;

public class BindActivity extends Activity implements OnClickListener, OnItemClickListener
{
	private static final String DATABASE_TABLE = "t_Binds";
	private static final String[] DATABASE_COLUMNS =  {"_id", "BIND","REPLACE"};
	
	private static final String BIND_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE + " (" +
	"_id INTEGER PRIMARY KEY, BIND TEXT, REPLACE TEXT);";

	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bind);
		
		findViewById(R.id.saveBindButton).setOnClickListener(this);
		findViewById(R.id.clearBindButton).setOnClickListener(this);
		

		try
		{

			LoadBinds();
		}
		catch(Exception ex)
		{
			MyDbHelper myDbHelper = new MyDbHelper(this);
			SQLiteDatabase myDB = myDbHelper.getReadableDatabase();
			//Create the table to prevent warnings.
			myDB.execSQL(BIND_TABLE_CREATE);
			myDbHelper.close();
			myDB.close();
		}

	}
	
	

	private void LoadBinds()
	{
		//ListView lv = (ListView)findViewById(R.id.bindListView);
		
		MyDbHelper myDbHelper = new MyDbHelper(this);
		SQLiteDatabase myDB = myDbHelper.getReadableDatabase();
		//Create the table to prevent warnings.
		myDB.execSQL(BIND_TABLE_CREATE);
		Cursor c = myDB.query(DATABASE_TABLE, DATABASE_COLUMNS, null,null,null,null,null);
		startManagingCursor(c);
		
		int[] mapTo = new int[] {android.R.id.text1, android.R.id.text2};
		
		ListAdapter adapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_2,c,new String[] {"BIND","REPLACE"},mapTo);
		
		GridView gv = (GridView)findViewById(R.id.gridList);
		gv.setOnItemClickListener(this);
		gv.setAdapter(adapter);
		//setListAdapter(adapter);
		
	}
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.saveBindButton:
			EditText bindText = (EditText)findViewById(R.id.bindText);
			EditText replaceText = (EditText)findViewById(R.id.replaceText);
			SaveBind(bindText.getText().toString(),replaceText.getText().toString());
			LoadBinds();
			ClearEditText();
			break;
		case R.id.clearBindButton:
			ClearEditText();
			break;
		default:
			break;
		}
		
	}
	
	private void ClearEditText()
	{
		EditText bindText2 = (EditText)findViewById(R.id.bindText);
		EditText replaceText2 = (EditText)findViewById(R.id.replaceText);
		bindText2.setText("");
		replaceText2.setText("");
	}
	
	private void SaveBind(String bindText, String replaceText)
	{
		bindText = bindText.replace("'", "''");
		replaceText = replaceText.replace("'", "''");
		MyDbHelper myDbHelper = new MyDbHelper(this);
		SQLiteDatabase myDB = myDbHelper.getReadableDatabase();
		ContentValues values = new ContentValues(2);
		values.put("BIND", bindText.toLowerCase());
		values.put("REPLACE",replaceText.toLowerCase());
		myDB.delete(DATABASE_TABLE, "BIND = '" + bindText.toLowerCase() + "'", null);
		myDB.insert(DATABASE_TABLE, "null", values);
		myDB.close();
		myDbHelper.close();
	}



	public void onItemClick(AdapterView<?> arg0, final View v, int position, long arg3) {
		
		String[] items = new String[] {"Edit Bind","Delete Bind","Cancel"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select an option");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch(item)
				{
				case 0:
					TwoLineListItem i = (TwoLineListItem)v;
					TextView one = i.getText1();
					TextView two = i.getText2();
					EditText bindText = (EditText)findViewById(R.id.bindText);
					EditText replaceText = (EditText)findViewById(R.id.replaceText);
					bindText.setText(one.getText());
					replaceText.setText(two.getText());
					
					break;
				case 1:
					

					TwoLineListItem i2 = (TwoLineListItem)v;
					TextView one2 = i2.getText1();
					String deleteBindText = one2.getText().toString().toLowerCase().replace("'", "''");
					MyDbHelper helper = new MyDbHelper(BindActivity.this);
					SQLiteDatabase myDB = helper.getWritableDatabase();
					myDB.delete(DATABASE_TABLE, "BIND = '" + deleteBindText + "'", null);
					myDB.close();
					helper.close();
					LoadBinds();
					break;
				case 2:
					LoadBinds();
					dialog.dismiss();
					break;
				default:
					dialog.dismiss();
					break;
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

}
