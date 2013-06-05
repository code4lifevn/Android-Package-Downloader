package com.gang.downloader.net;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ChannelTask  implements Callable<Boolean> {
	private static final int CONNECT_TIMEOUT = 20000; //15s
	private static final int READ_TIMEOUT    = 10000; //10s
	
	private FileWriter writer;
	private Context context;
	private String url;
	
	private ChannelListener listener;
	private long fileSize;
	
	public interface ChannelListener {
		int DONE    = 1;
		int FAIL    = 2;
		int STOP    = 3;
		int RUNNING = 4;
		void onReceive(Context context, int code, long downloadSize, long fileSize, String filePath);
	}
	
	public ChannelTask(Context context, String fileName, String url, long fileSize, ChannelListener listener) {
		writer = new FileWriter(context, fileName);
		this.context = context.getApplicationContext();
		this.url = url;
		
		this.fileSize = fileSize;
		this.listener = listener;
	}
	
	
	public boolean request() {
	    HttpURLConnection connection = null;  
	    long downloadFileSize = 0;
	    
	    try {
	    	writer.init();
	    	long range = writer.open();
	    	downloadFileSize = range;
	    	if (listener != null) {
	    		listener.onReceive(context, ChannelListener.RUNNING, downloadFileSize, fileSize, null);
	    	}
	    	
	    	URL url = new URL(this.url);
		    if (!needSetProxy(context)) {
		    	connection = (HttpURLConnection)url.openConnection();
	        } else {
	        	String proxy = android.net.Proxy.getDefaultHost();
	          	int port = android.net.Proxy.getDefaultPort();
	          	
	          	if (proxy != null && port != -1) {
	          		Proxy host = new Proxy(java.net.Proxy.Type.HTTP, 
	          				  			   new InetSocketAddress(proxy, port));
	          		connection = (HttpURLConnection)url.openConnection(host);
	          	} else {
	          		connection = (HttpURLConnection)url.openConnection();
	          	}
	        }
	     
		    connection.setRequestMethod("GET");
		    connection.setConnectTimeout(CONNECT_TIMEOUT);
		    connection.setReadTimeout(READ_TIMEOUT);
		    connection.setAllowUserInteraction(false);
		    connection.setRequestProperty("Range", "bytes=" + range);
		    connection.setRequestProperty("Connection", "close");
		    connection.setUseCaches (false);

		    int responseCode = connection.getResponseCode();
		    if (responseCode >= 200 && responseCode < 300) {
		    	if (Thread.interrupted()) {
		    		if (listener != null) {
			    		listener.onReceive(context, ChannelListener.STOP, downloadFileSize, fileSize, null);
			    	}
		    		return false;
		    	}
		    	//Content-Range: bytes 1000-3979/3980
		    	String fieldRange = connection.getHeaderField("Content-Range");
		    	if (fieldRange != null) {
		    		Pattern p = Pattern.compile("\\d+");
		    		Matcher m = p.matcher(fieldRange);
		    		int index = 0;
		    		while (m.find()) {
		    			long size = Long.parseLong(m.group());
		    			if (index == 0) {
		    				if (range != size) {
				    			throw new HttpException("Http Range Mismatch.");
				    		}
		    			} else if (index == 1) {
		    				;
		    			} else if (index == 2) {
		    				fileSize = size;
		    			}
		    			
		    			index++;
		    		}
		    	} else {
		    		writer.reset();
		    		fileSize = connection.getContentLength();
		    		downloadFileSize = 0;
		    	}
		    	
		    	InputStream is = connection.getInputStream();
		    	
		        final int MAX_LEN = 1024;    
		    	byte[] tmp = new byte[MAX_LEN];
		    	int len;
		    	
		    	int step = -1;
		    	int latestStep = 0;
		    	while((len = is.read(tmp)) != -1) {
		    		if (Thread.interrupted()) {
		    			if (listener != null) {
				    		listener.onReceive(context, ChannelListener.STOP, downloadFileSize, fileSize, null);
				    	}
			    		return false;
			    	}
		    		writer.write(tmp, len);
		    		downloadFileSize += len;
		    		
		    		latestStep = (int) (downloadFileSize * 100 / fileSize);
		    		
		    		if (latestStep - step >= 1) {
		    			step = latestStep;
		    			if (listener != null) {
		    				listener.onReceive(context, ChannelListener.RUNNING, downloadFileSize, fileSize, null);
		    			}
			    	}
		    	}
		    	is.close();
		    	
		    	if (writer.rename()) {
		    		if (listener != null) {
		    			listener.onReceive(context, ChannelListener.DONE, fileSize, fileSize, writer.getPath());
		    		}
		    		return true;
		    	} else {
		    		if (listener != null) {
		    			listener.onReceive(context, ChannelListener.FAIL, downloadFileSize, fileSize, null);
			    	}
		    	}
		    }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	if (listener != null) {
    			listener.onReceive(context, ChannelListener.FAIL, downloadFileSize, fileSize, null);
	    	}
	    } finally {
	    	if(connection != null) {
	    		connection.disconnect(); 
	    	}
	    	writer.close();
	    }
	    
	    return false;
	} 
	
	private static boolean needSetProxy(Context context) {
		if (context == null) {
			return false;
		}
		ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager == null) {
			return false;
		}
        NetworkInfo mobNetInfo = connectivityManager.getActiveNetworkInfo();
        if (mobNetInfo == null || "wifi".equals(mobNetInfo.getTypeName().toLowerCase())) {
            return false;
        }
        if (mobNetInfo.getSubtypeName().toLowerCase().contains("cdma")) {
            if (android.net.Proxy.getDefaultHost() != null && android.net.Proxy.getDefaultPort() != -1) {
                return true;
            }
        } else if (mobNetInfo.getExtraInfo().contains("wap")) {
            return true;
        }
        
        return false;
	}


	@Override
	public Boolean call() throws Exception {
		return request();
	}


}
