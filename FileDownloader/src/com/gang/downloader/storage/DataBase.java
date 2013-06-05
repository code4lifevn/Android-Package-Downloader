package com.gang.downloader.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.gang.downloader.storage.Contract.Record;

class DataBase extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "com_gang_downloader.db";
	private static final int DATABASE_VERSION = 1; 
	
	public DataBase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	interface Tables {
		String RECORD = "record";
	}
	

	@Override
	public void onCreate(SQLiteDatabase db) {
		buildRecordTable(db);
	}
	
	private void buildRecordTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + Tables.RECORD + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Record.RECORD_ID + " TEXT NOT NULL,"
                + Record.RECORD_UNIQUE_FILE_NAME + " TEXT NOT NULL,"
                + Record.RECORD_URL + " TEXT NOT NULL,"
                + Record.RECORD_FILE_SIZE + " INTEGER NOT NULL)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + Tables.RECORD);
		onCreate(db);
	}

}
