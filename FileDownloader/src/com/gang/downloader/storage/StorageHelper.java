package com.gang.downloader.storage;

import java.util.ArrayList;
import java.util.List;

import com.gang.downloader.storage.Contract.Record;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class StorageHelper {
	
	public static List<Item> readAll(Context context) {
		List<Item> result = new ArrayList<Item>();
		
		Cursor cursor = context.getContentResolver().query(Record.CONTENT_URI, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			Item item = new Item();
			item.id = cursor.getString(cursor.getColumnIndexOrThrow(Record.RECORD_ID));
			item.unique_file_name = cursor.getString(cursor.getColumnIndexOrThrow(Record.RECORD_UNIQUE_FILE_NAME));
			item.url = cursor.getString(cursor.getColumnIndexOrThrow(Record.RECORD_URL));
			item.file_size = cursor.getLong(cursor.getColumnIndexOrThrow(Record.RECORD_FILE_SIZE));
			result.add(item);
			cursor.moveToNext();
		}
		cursor.close();
		return result;
	}
	
	
	
	public static void update(Context context, String id, long fileSize) {
		ContentValues values = new ContentValues();
		values.put(Record.RECORD_FILE_SIZE, fileSize);
		context.getContentResolver().update(Record.CONTENT_URI, values, Record.RECORD_ID + "=?", new String[]{id});
	}
	
	public static void delete(Context context, String id) {
		context.getContentResolver().delete(Record.CONTENT_URI, Record.RECORD_ID + "=?", new String[]{id});
	}
	
	public static Uri insert(Context context, Item item) {
		ContentValues values = new ContentValues();
		values.put(Record.RECORD_ID, item.id);
		values.put(Record.RECORD_UNIQUE_FILE_NAME, item.unique_file_name);
		values.put(Record.RECORD_URL, item.url);
		values.put(Record.RECORD_FILE_SIZE, Long.MAX_VALUE);
		return context.getContentResolver().insert(Record.CONTENT_URI, values);
	}
}
