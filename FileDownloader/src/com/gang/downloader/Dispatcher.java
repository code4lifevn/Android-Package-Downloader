package com.gang.downloader;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.gang.downloader.Listener.State;
import com.gang.downloader.net.ChannelTask.ChannelListener;
import com.gang.downloader.net.Client;
import com.gang.downloader.net.FileWriter;
import com.gang.downloader.storage.Item;
import com.gang.downloader.storage.StorageHelper;

public class Dispatcher {
	private Map<ID, Receiver> id_receivers;
	private Map<ID, Listeners> id_listeners;
	
	private Client client;
	
	private final int MAX_RUNNING_COUNT = 5;
	
	public Dispatcher() {
		id_receivers = new ConcurrentHashMap<ID, Receiver>();
		client = new Client();
		
		id_listeners = new ConcurrentHashMap<ID, Listeners>();
	}
	
	public void initialize(Context context) {
		if (id_receivers.size() > 0) {
			stopAll(context);
			return;
		}
		
		List<Item> items = StorageHelper.readAll(context);
		
		for (Item i : items) {
			ID id = new ID(i.id);
			id_receivers.put(id, new Receiver(context, i));
		}
	}
	
	
	private void stopAll(Context context) {
		List<Receiver> list = new ArrayList<Receiver>(id_receivers.values());
		if (list.size() <= 0) {
			return;
		}
	
		for(Receiver r : list) {
			r.stop(context);
		}
	}
	
	public void addTask(Context context, String id, String fileName, String url) {
		Receiver o = getReceiver(id);
		if (o == null) {
			Item i = new Item();
			i.file_size = Long.MAX_VALUE;
			i.id = id;
			i.unique_file_name = fileName;
			i.url = url;
			StorageHelper.insert(context, i);
			
			id_receivers.put(new ID(id), new Receiver(context, i));
		} 
		
		_schedule(context);
	}
	
	private Receiver getReceiver(String id) {
		Receiver o = id_receivers.get(new ID(id));
		return o;
	}
	
	private Listeners getListener(String id) {
		return id_listeners.get(new ID(id));
	}
	
	public void startTask(Context context, String id) {
		Receiver o = getReceiver(id);
		if (o == null) {
			return;
		}
		o.start(context);
	}
	
	public void stopTask(Context context, String id) {
		Receiver o = getReceiver(id);
		if (o == null) {
			return;
		}
		o.stop(context);
	}
	
	public void deleteTask(Context context, String id) {
		Receiver o = getReceiver(id);
		if (o == null) {
			return;
		}
		o.cancel(context);

		dispatchTask_None(id);
	}
	
	public void addTaskListener(Context context, String id, Listener listener) {
		Listeners listeners = getListener(id);
		if (listeners == null) {
			id_listeners.put(new ID(id), new Listeners());
		}
		listeners = getListener(id);
		listeners.add(listener);
		Receiver o = getReceiver(id);
		if (o == null) {
			dispatchTask_None(listener);
		} else {
			o.dispatch(listener);
		}
	}
	
	public void removeTaskListener(Context context, String id, Listener listener) {
		Listeners listeners = getListener(id);
		if (listeners == null) {
			return;
		}
		if(listeners.remove(listener)) {
			id_listeners.remove(new ID(id));
		}
	}
	
	private void dispatchTask_None(Listener listener) {
		if (listener != null) {
			listener.onState(State.NONE, 0, 0, null);
		}
	}
	
	private void dispatchTask_None(String id) {
		Listeners listeners = getListener(id);
		if (listeners == null) {
			return;
		}
		listeners.dispatch_TaskNone();
	}
	
	private void dispatch_TaskStatus(String id, TaskData data) {
		Listeners listeners = getListener(id);
		if (listeners == null) {
			return;
		}
		listeners.dispatch(data);
	}
	
	private void _schedule(Context context) {
		List<Receiver> list = new ArrayList<Receiver>(id_receivers.values());
		Collections.sort(list);
		int runningCount = 0;
		int queueingCount = 0;
		for(Receiver r : list) {
			if (r.isQueueing()) {
				queueingCount++;
			} else if (r.isRunning()) {
				runningCount++;
			}
		}
		int count = MAX_RUNNING_COUNT - runningCount;
		if (queueingCount < count) {
			count = queueingCount;
		}
		for (int i = 0; i < count; i++) {
			String id = list.get(i).getID().id;
			Receiver r = getReceiver(id);
			if (r != null && r.isQueueing()) {
				r.start(context);
			}
		}
	}
	
	final static class Listeners {
		private List<WeakReference<Listener>> list;
		private ReadWriteLock rwLock;
		
		private static final int MAX_SYNC_COUNT = 2;
		
		private Handler handler;	
		
		public Listeners() {
			list = new ArrayList<WeakReference<Listener>>(4);
			rwLock = new ReentrantReadWriteLock();
			handler = new Handler(Looper.myLooper());
		}
		

		private void sync() {
			Lock wLock = rwLock.writeLock();
			wLock.lock();
			int len = list.size();
			for(int i = len - 1; i >= 0; i--) {
				WeakReference<Listener> refListener = list.get(i);
				if (refListener.get() == null) {
					list.remove(i);
				} 
			}
		
			wLock.unlock();
		}
		
		public void add(Listener listener) {
			Lock wLock = rwLock.writeLock();
			wLock.lock();
			
			list.add(new WeakReference<Listener>(listener));
			
			wLock.unlock();
		}
		
		public boolean remove(Listener listener) {
			boolean isEmpty = false;
			Lock wLock = rwLock.writeLock();
			wLock.lock();
			int len = list.size();
			for(int i = len - 1; i >= 0; i--) {
				Listener item = list.get(i).get();
				if (item != null && item == listener) {
					list.remove(i);
					break;
				} 
			}
			isEmpty = list.size() <= 0;
			wLock.unlock();
			
			return isEmpty;
		}
		
		private void dispatchOnUiThread(final Listener listener, final TaskData data) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					listener.onState(data.state, data.downloadSize, data.fileSize, data.filePath);
				}
			});
		}
		
		private void dispatchOnUiThread(final Listener listener) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					listener.onState(State.NONE, 0, 0, null);
				}
			});
		}
		
		public void dispatch(TaskData data) {
			Lock rLock = rwLock.readLock();
			rLock.lock();
			
			int invalidRef = 0;
			for(WeakReference<Listener> refListener : list) {
				Listener listener = refListener.get();
				if (listener != null) {
					dispatchOnUiThread(listener, data);
				} else {
					invalidRef++;
				}
			}
			
			rLock.unlock();
			
			if (invalidRef > MAX_SYNC_COUNT) {
				sync();
			}
		}
		
		public void dispatch_TaskNone() {
			Lock rLock = rwLock.readLock();
			rLock.lock();
			
			int invalidRef = 0;
			for(WeakReference<Listener> refListener : list) {
				final Listener listener = refListener.get();
				if (listener != null) {
					dispatchOnUiThread(listener);
				} else {
					invalidRef++;
				}
			}
			
			rLock.unlock();
			
			if (invalidRef >= MAX_SYNC_COUNT) {
				sync();
			}
		}
	}
	
	
	final class Receiver implements Comparable<Receiver>{
		private Item 			item;	
		private volatile TaskData data; 
		private ChannelListenerDispatcher 	    dispatcher;
		
		private long millis;
		
		@SuppressWarnings("rawtypes")
		private Future channelTask;
		
			
		public ID getID() {
			return new ID(item.id);
		}
		
		public Receiver(Context context, Item item) {
			this.item = item;
			initTaskData(context);
			dispatcher = new ChannelListenerDispatcher();
			
			millis = System.currentTimeMillis();
		}
		
		public boolean isQueueing() {
			return data.state == State.QUEUE;
		}
		
		public boolean isRunning() {
			return data.state == State.RUNNING;
		}
		
		public void start(Context context) {
			final TaskData data = this.data;
			if (data.state != State.DONE && data.state != State.RUNNING) {
				channelTask = client.download(context, item.unique_file_name, item.url, data.fileSize, dispatcher);
			}
		}
		
		public boolean stop(Context context) {
			final TaskData data = this.data;
			if (channelTask != null && data.state == State.RUNNING) {
				channelTask.cancel(true);
				return true;
			}
			return false;
		}
		
		public void dispatch(Listener taskListener) {
			final TaskData data = this.data;
			if (taskListener != null) {
				taskListener.onState(data.state, data.downloadSize, data.fileSize, data.filePath);
			}
		}
		
		public void cancel(Context context) {
			dispatcher.cancel();
			if (channelTask != null) {
				channelTask.cancel(true);
				channelTask = null;
			}
			
			id_receivers.remove(new ID(item.id));
			StorageHelper.delete(context, item.id);
		
			final TaskData data = this.data;
			if (data.state == State.DONE) {
				FileWriter.deleteFile(FileWriter.downloadedFileExist(context, item.unique_file_name));
			} else {
				FileWriter.deleteFile(FileWriter.tempFileExist(context, item.unique_file_name));
			}
		}
		
		private void initTaskData(Context context) {
			State status = State.QUEUE;
			long downloadSize = 0;
			long fileSize = Long.MAX_VALUE;
			String filePath = FileWriter.downloadedFileExist(context, item.unique_file_name);
			
			if (!TextUtils.isEmpty(filePath)) {
				status = State.DONE;
				fileSize = downloadSize = FileWriter.fileSize(filePath);
			} else {
				String tempFilePath = FileWriter.tempFileExist(context, item.unique_file_name);
				downloadSize = FileWriter.fileSize(tempFilePath);
				fileSize = item.file_size;
			}
			
			data = new TaskData(status, downloadSize, fileSize, filePath);
		}
		
		final class ChannelListenerDispatcher implements ChannelListener {
			
			private boolean canceled = false;

			@Override
			public void onReceive(Context context, 
					             int code, 
					             long downloadSize,
					             long fileSize, 
					             String filePath) {
				if (canceled) {
					return;
				}
				if (Receiver.this.data.fileSize != fileSize && fileSize > 0) {
					StorageHelper.update(context, item.id, fileSize);
				}
				
				State status = State.FAILED;
				if (code == ChannelListener.DONE) {
					status = State.DONE;
				} else if (code == ChannelListener.FAIL) {
					status = State.FAILED;
				} else if (code == ChannelListener.RUNNING) {
					status = State.RUNNING;
				} else if (code == ChannelListener.STOP) {
					status = State.PAUSED;
				}
				
				Receiver.this.data = new TaskData(status, downloadSize, fileSize, filePath);
				dispatch_TaskStatus(item.id, data);
				
				if (data.state == State.DONE || data.state == State.FAILED || data.state == State.PAUSED) {
					_schedule(context);
				}
			}

			public void cancel() {
				canceled = true;
			}
		}

		@Override
		public int compareTo(Receiver another) {
			boolean selfIsQueueing = this.isQueueing();
			boolean otherIsQueueing = another.isQueueing();
			
			if (selfIsQueueing && otherIsQueueing) {
				if (this.millis < another.millis) {
					return -1;
				} else if (this.millis > another.millis) {
					return 1;
				} else {
					return 0;
				}
			} else if (selfIsQueueing && !otherIsQueueing) {
				return -1;	
			} else if (!selfIsQueueing && otherIsQueueing) {
				return 1;
			} else {
				if (this.millis < another.millis) {
					return -1;
				} else if (this.millis > another.millis) {
					return 1;
				} else {
					return 0;
				}
			}	
		}
	}
	
	final static class TaskData {
		public State state;
		public long downloadSize;
		public long fileSize;
		public String filePath;
		
		public TaskData(State state, 
				      long downloadSize,
				      long fileSize, 
				      String filePath) {
			this.state = state;
			this.downloadSize = downloadSize;
			this.fileSize = fileSize;
			this.filePath = filePath;
		}
	}
	
	final static class ID {
		private String id;
		
		public ID(String id) {
			this.id = id;
		}
			
		@Override
		public boolean equals(Object o) {
			if (o == null) {
				return false;
			}
			if (!(o instanceof ID)) {
				return false;
			}
			return id.equals(((ID)o).id);
		}
		
		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}
}
