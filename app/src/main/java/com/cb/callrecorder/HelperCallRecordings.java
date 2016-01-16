package com.cb.callrecorder;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HelperCallRecordings extends SQLiteOpenHelper {

	static String tbname = "CallRecords";
	static String dbname = "Database";
	static int ver = 1;
	SQLiteDatabase sld;
	Context c;
	String col1 = "Number";
	String col2 = "Time";
	String col3 = "FilePath";
	String col4 = "CallType";
	static int counter = 0;
	String cols[] = { col1, col2, col3,col4 };

	public HelperCallRecordings(Context context) {
		super(context, dbname, null, ver);
		c = context;
		sld = getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		if (counter == 0) {
			db.execSQL("create table " + tbname + "(" + col1
					+ " TEXT NOT NULL, " + col2 + " TEXT NOT NULL, " + col3
					+ " TEXT NOT NULL, " + col4 +" TEXT NOT NULL);");
		}
		counter++;

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public void insert(String number, String time, String filepath, String calltype) {
		sld.execSQL("insert into " + tbname + " values('" + number + "', '"
				+ time + "', '" + filepath + "', '"+calltype+"');");
		
		
	}

	public Cursor display() {
		Cursor c = sld.query(tbname, cols, null, null, null, null,"Time DESC");
		return c;
	}
	
	public Cursor refineDisplay(String calltype)
	{	
		Cursor c = sld.query(tbname, cols, "CallType='" + calltype+"'", null, null, null,"Time DESC");
		return c;
	}

	public void clearAll() {
		sld.execSQL("delete from " + tbname + ";");
	}

	public void clearData(String s) {
		sld.execSQL("delete from " + tbname + " where Time='" + s + "';");
	}

	public void closeDatabase() {
		sld.close();
	}
}
