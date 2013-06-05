package com.gang.downloader;

import android.content.Context;

/**
 * ���ع��ܽӿ�
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
	 * ��ʼ��
	 * @param context
	 */
	public void initialize(Context context) {
		dispatcher.initialize(context);
	}
	
	/**
	 * ������������
	 * @param context
	 * @param id ��������Ψһ��ʶ
	 * @param fileName ���ر���Ψһ�ļ���
	 * @param url ���ص�ַ
	 */
	public void addTask(Context context, String id, String fileName, String url) {
		dispatcher.addTask(context, id, fileName, url);
	}
	
	/**
	 * ɾ����������
	 * @param context
	 * @param id ��������Ψһ��ʶ
	 */
	public void deleteTask(Context context, String id) {
		dispatcher.deleteTask(context, id);
	}
	/**
	 * ֹͣ��������
	 * @param context
	 * @param id ��������Ψһ��ʶ
	 */
	public void stopTask(Context context, String id) {
		dispatcher.stopTask(context, id);
	}
	
	/**
	 * ������������
	 * @param context
	 * @param id ��������Ψһ��ʶ
	 */
	public void startTask(Context context, String id) {
		dispatcher.startTask(context, id);
	}
	
	/**
	 * ������������״̬֪ͨ
	 * @param context
	 * @param id ��������Ψһ��ʶ
	 * @param listener ״̬֪ͨ�ص�
	 */
	public void addTaskListener(Context context, String id, Listener listener) {
		dispatcher.addTaskListener(context, id, listener);
	}
	
	/**
	 * �Ƴ���������״̬֪ͨ
	 * @param context
	 * @param id ��������Ψһ��ʶ
	 * @param listener ״̬֪ͨ�ص�
	 */
	public void removeTaskListener(Context context, String id, Listener listener) {
		dispatcher.removeTaskListener(context, id, listener);	
	}
}
