package com.gang.downloader.net;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;

public class Client {
	private ThreadPoolExecutor threadPool;
	
	public Client() {
		threadPool = (ThreadPoolExecutor)Executors.newCachedThreadPool(new ThreadFactory() {
	     	private final AtomicInteger mCount = new AtomicInteger(1);
	
	         public Thread newThread(Runnable r) {
	            Thread t = new Thread(r, "Client #" + mCount.getAndIncrement());
	         	if (t.isDaemon())
	                 t.setDaemon(false);
	            if (t.getPriority() != (Thread.NORM_PRIORITY - 1))
	                 t.setPriority((Thread.NORM_PRIORITY - 1));
	            return t;
	         }
	     });
	}
	
	public Future<Boolean> download(Context context, String fileName, String url, long fileSize, ChannelTask.ChannelListener listener) {
		ChannelTask task = new ChannelTask(context, fileName, url, fileSize, listener);
		return threadPool.submit(task);
	}
}
