package com.gang.downloader.net;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

public class FileWriter {
	private String basePath;
	private String fileName;
	private RandomAccessFile wFile;
	
	private final static String dirName = "files";
	
	private final static String tempFileSuffix = ".download";
	
	private String filePath = "";
	private String tempFilePath = "";
	
	private Context context;
	
	public FileWriter(Context context, String fileName) {
		this.fileName = fileName;
		this.context = context.getApplicationContext();
	}
	
	private static boolean mounted() {
		return Environment.MEDIA_MOUNTED.equalsIgnoreCase(Environment.getExternalStorageState());
	}
	
	public void init() throws IOException {
		if (mounted()) {
			String sdcardBasePath = Environment.getExternalStorageDirectory().getAbsolutePath();
			basePath = sdcardBasePath + File.separator + context.getPackageName() + File.separator + dirName + File.separator;
			File dir = new File(basePath);
			dir.mkdirs();
			
			
			filePath = dir.getAbsolutePath() + File.separator + fileName;
			tempFilePath = filePath + tempFileSuffix;
			return;
		}
		
		throw new IOException("Sdcard cant't be writable.");
	}
	
	public static String downloadedFileExist(Context context, String fileName) {
		if (mounted()) {
			String sdcardBasePath = Environment.getExternalStorageDirectory().getAbsolutePath();
			String basePath = sdcardBasePath + File.separator + context.getPackageName() + File.separator + dirName;
			String filePath = basePath + File.separator + fileName;
			File f = new File(filePath);
			if ( f.exists() && f.isFile() ) {
				return filePath;
			}
		}
		
		return null;
	}
	
	public static void deleteFile(String filePath) {
		if (TextUtils.isEmpty(filePath)) {
			return;
		}
		File f = new File(filePath);
		if ( f.exists() && f.isFile() ) {
			f.delete();
		}
	}
	
	public static String tempFileExist(Context context, String fileName) {
		String sdcardBasePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		if (TextUtils.isEmpty(sdcardBasePath)) {
			sdcardBasePath = "/sdcard";
		}
		String basePath = sdcardBasePath + File.separator + context.getPackageName() + File.separator + dirName + File.separator;
		
		String filePath = basePath + File.separator + fileName;
		String tempFilePath = filePath + tempFileSuffix;
		
		return tempFilePath;	
	}
	
	public static long fileSize(String filePath) {
		File f = new File(filePath);
		if ( f.exists() && f.isFile() ) {
			return f.length();
		}
		return 0;
	}
	
	
	
	public long open() throws IOException {
		wFile = new RandomAccessFile(tempFilePath, "rw");
		return wFile.length();
	}
	
	public void reset() throws IOException {
		if (wFile != null) {
			wFile.setLength(0);
		}
	}

	public long currentPos() throws IOException {
		return wFile.length();
	}
	public void write(byte[] bytes, int len) throws IOException {
		if (wFile != null) {
			wFile.write(bytes, 0, len); 
		}
	}
	
	public boolean rename() {
		close();
		File dest = new File(filePath);
		File src = new File(tempFilePath);
		return src.renameTo(dest);
	}
	
	public boolean close()  {
		if (wFile != null) {
			try {
				wFile.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				wFile = null;
			}
			return true;
		}
		
		return false;
	}
	
	public String getPath() {
		return filePath;
	}
}
