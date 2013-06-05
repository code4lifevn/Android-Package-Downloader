package com.gang.downloader;

public interface Listener {	
	enum State {
		NONE, 		/*无下载*/
		QUEUE,      /*排队中*/
		RUNNING,    /*正在下载*/
		PAUSED,     /*已暂停*/
		FAILED,     /*已失败*/
		DONE		/*下载完成*/
	};
	/**
	 * 状态回调
	 * @param state 状态
	 * @param downloadSize 已下载大小
	 * @param fileSize	文件大小(Long.MAX_VALUE 表示无效大小)
	 * @param filePath	下载完成后文件保存路径(状态是完成时有效)
	 */
	void onState(State state, long downloadSize, long fileSize, String filePath);
}
