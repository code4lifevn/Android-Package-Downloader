package com.gang.downloader.storage;

import android.net.Uri;

public class Contract {
	
	public static String CONTENT_AUTHORITY = "com.gang.downloader";
	private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	
	public static final String PATH_RECORD    = "record";
	
	interface RecordColumns {
		String RECORD_ID 				 	= "record_id";
		String RECORD_UNIQUE_FILE_NAME 		= "record_unique_file_name";
		String RECORD_URL     		 		= "record_url";
		String RECORD_FILE_SIZE       	    = "record_file_size";
	}
	
	public static class Record implements RecordColumns {
		 public static final Uri CONTENT_URI =
	                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECORD).build();

	        public static final String CONTENT_TYPE =
	                "vnd.android.cursor.dir/vnd.com.gang.downloader.record";
	        public static final String CONTENT_ITEM_TYPE =
	                "vnd.android.cursor.item/vnd.com.gang.downloader.record";
	        
	        public static Uri buildRecordUri(String recordId) {
	            return CONTENT_URI.buildUpon().appendPath(recordId).build();
	        }

	        public static String getRecordId(Uri uri) {
	            return uri.getPathSegments().get(1);
	        }
	}
}
