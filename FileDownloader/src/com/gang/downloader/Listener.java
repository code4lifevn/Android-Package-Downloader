package com.gang.downloader;

public interface Listener {	
	enum State {
		NONE, 		/*������*/
		QUEUE,      /*�Ŷ���*/
		RUNNING,    /*��������*/
		PAUSED,     /*����ͣ*/
		FAILED,     /*��ʧ��*/
		DONE		/*�������*/
	};
	/**
	 * ״̬�ص�
	 * @param state ״̬
	 * @param downloadSize �����ش�С
	 * @param fileSize	�ļ���С(Long.MAX_VALUE ��ʾ��Ч��С)
	 * @param filePath	������ɺ��ļ�����·��(״̬�����ʱ��Ч)
	 */
	void onState(State state, long downloadSize, long fileSize, String filePath);
}
