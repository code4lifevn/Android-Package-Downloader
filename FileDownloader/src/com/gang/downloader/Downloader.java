package com.gang.downloader;

import android.content.Context;

/**
 * 下载功能接口
 *
 */
public final class Downloader {
	private Dispatcher dispatcher;
	
	private static class DownloaderHolder {
		static Downloader sInstance = new Downloader();
	}
	
	private Downloader () {
		dispatcher = new Dispatcher();
	}
	
	
	public static Downloader getInstance() {
		return DownloaderHolder.sInstance;
	}
	
	/**
	 * 初始化
	 * @param context
	 */
	public void initialize(Context context) {
		dispatcher.initialize(context);
	}
	
	/**
	 * 新增下载任务
	 * @param context
	 * @param id 下载任务唯一标识
	 * @param fileName 下载保存唯一文件名
	 * @param url 下载地址
	 */
	public void addTask(Context context, String id, String fileName, String url) {
		dispatcher.addTask(context, id, fileName, url);
	}
	
	/**
	 * 删除下载任务
	 * @param context
	 * @param id 下载任务唯一标识
	 */
	public void deleteTask(Context context, String id) {
		dispatcher.deleteTask(context, id);
	}
	/**
	 * 停止下载任务
	 * @param context
	 * @param id 下载任务唯一标识
	 */
	public void stopTask(Context context, String id) {
		dispatcher.stopTask(context, id);
	}
	
	/**
	 * 启动下载任务
	 * @param context
	 * @param id 下载任务唯一标识
	 */
	public void startTask(Context context, String id) {
		dispatcher.startTask(context, id);
	}
	
	/**
	 * 增加下载任务状态通知
	 * @param context
	 * @param id 下载任务唯一标识
	 * @param listener 状态通知回调
	 */
	public void addTaskListener(Context context, String id, Listener listener) {
		dispatcher.addTaskListener(context, id, listener);
	}
	
	/**
	 * 移除下载任务状态通知
	 * @param context
	 * @param id 下载任务唯一标识
	 * @param listener 状态通知回调
	 */
	public void removeTaskListener(Context context, String id, Listener listener) {
		dispatcher.removeTaskListener(context, id, listener);	
	}
}
