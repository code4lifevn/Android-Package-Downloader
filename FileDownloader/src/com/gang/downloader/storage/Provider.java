package com.gang.downloader.storage;

import java.io.FileNotFoundException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.gang.downloader.storage.Contract.Record;
import com.gang.downloader.storage.DataBase.Tables;

public class Provider extends ContentProvider {
	private DataBase openHelper;
	private static final UriMatcher sUriMatcher = buildUriMatcher();
	
	@Override
	public boolean onCreate() {
		final Context context = getContext();
		openHelper = new DataBase(context);
		return true;
	}
	
	private static final int MATCH_RECORDS 					= 300;
	private static final int MATCH_RECORD 				    = 301;

	private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = Contract.CONTENT_AUTHORITY;
   
        matcher.addURI(authority, Contract.PATH_RECORD, 			MATCH_RECORDS);
        matcher.addURI(authority, Contract.PATH_RECORD + "/*", 	MATCH_RECORD);
 
        return matcher;
    }

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		final SQLiteDatabase db = openHelper.getReadableDatabase();
	    final SelectionBuilder builder = buildSimpleSelection(uri);
	    return builder.where(selection, selectionArgs).query(db, projection, sortOrder);
	}
	
	private SelectionBuilder buildSimpleSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MATCH_RECORDS: {
                return builder.table(Tables.RECORD);
            }
            
            case MATCH_RECORD: {
            	final String recordId = Record.getRecordId(uri);
                return builder.table(Tables.RECORD).where(
                		Record.RECORD_ID + "=?", recordId);
            }
            

            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }
	

	@Override
	public String getType(Uri uri) {
		 final int match = sUriMatcher.match(uri);
	        switch (match) {	
	        	case MATCH_RECORDS:
	        		return Record.CONTENT_TYPE;
	        	case MATCH_RECORD:
	        		return Record.CONTENT_ITEM_TYPE;
	        	
	            default:
	                throw new UnsupportedOperationException("Unknown uri: " + uri);
	        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final SQLiteDatabase db = openHelper.getWritableDatabase();
	    final int match = sUriMatcher.match(uri);
	    switch (match) {      
	        case MATCH_RECORDS: {
	            db.insertOrThrow(Tables.RECORD, null, values);
	            getContext().getContentResolver().notifyChange(uri, null);
	            return Record.buildRecordUri(
	            		values.getAsString(Record.RECORD_ID));
	        }
	            
	     
	        default: {
	            throw new UnsupportedOperationException("Unknown uri: " + uri);
	        }
	   }
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		 final SQLiteDatabase db = openHelper.getWritableDatabase();
	     final SelectionBuilder builder = buildSimpleSelection(uri);
	     int retVal = builder.where(selection, selectionArgs).delete(db);
	     getContext().getContentResolver().notifyChange(uri, null);
	     return retVal;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
        final SQLiteDatabase db = openHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        int retVal = builder.where(selection, selectionArgs).update(db, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return retVal;
	}

	@Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }
}
