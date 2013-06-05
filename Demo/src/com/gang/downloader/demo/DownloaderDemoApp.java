package com.gang.downloader.demo;

import com.gang.downloader.Downloader;

import android.app.Application;

public class DownloaderDemoApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		Downloader.getInstance().initialize(this);
	}

	
}
