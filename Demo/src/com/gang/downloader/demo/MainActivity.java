package com.gang.downloader.demo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.gang.downloader.Downloader;
import com.gang.downloader.Listener;

public class MainActivity extends Activity {
	private ListView list;
	private List<DownloadItem> data = new ArrayList<DownloadItem>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		prepare();
		list = (ListView) findViewById(R.id.list);
	
		list.setAdapter(new DownloadAdapter());
	}

	private void prepare() {	
		{
			DownloadItem item = new DownloadItem();
			item.pkgName = "dlyxzx";
			item.versionCode = 1370;
			item.url = "http://down.androidgame-store.com/201306041711/3ACB5250F2DC0BC05D2316386AAE3714/new/game1/41/110441/dlyxzx_1370323043453.apk";
			item.title = "œ¬‘ÿ»ŒŒÒ_1";	
			data.add(item);
		}
		
	}
	
	final static class DownloadItem {
		public String title;
		public String pkgName;
		public int versionCode;
		public String url;
		
		public String id() {
			return pkgName + '_' + versionCode;
		}
		
		public String fileName() {
			return pkgName + '_' + versionCode + ".apk";
		}
	}

	final class Controller implements Listener{
		public TextView title;
		public TextView desc;
		public Button button;
		
		public DownloadItem item;
		
		@Override
		public void onState(State state, long downloadSize, long fileSize,
				String filePath) {
			title.setText(item.fileName());
			
			desc.setText("");
			if (state == State.NONE) {
				button.setText("None");
				button.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Downloader.getInstance().addTask(MainActivity.this, item.id(), item.fileName(), item.url);
					}
				});
				
			} else if (state == State.QUEUE) {
				button.setText("Queue");
				button.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Downloader.getInstance().addTask(MainActivity.this, item.id(), item.fileName(), item.url);
					}
				});
				
			} else if (state == State.RUNNING) {
				button.setText("Run");
				button.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Downloader.getInstance().stopTask(MainActivity.this, item.id());
					}
				});
				desc.setText("downloadSize:" + downloadSize + "; fileSize:" + fileSize);
			} else if (state == State.PAUSED) {
				button.setText("Pause");
				button.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Downloader.getInstance().startTask(MainActivity.this, item.id());
					}
				});
				desc.setText("downloadSize:" + downloadSize + "; fileSize:" + fileSize);
			} else if (state == State.FAILED) {
				button.setText("Fail");
				button.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Downloader.getInstance().startTask(MainActivity.this, item.id());
					}
				});
				desc.setText("downloadSize:" + downloadSize + "; fileSize:" + fileSize);
			} else if (state == State.DONE) {
				button.setText("Done");
				desc.setText(filePath);
				
				final String path = filePath;
				
				button.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent i = new Intent();
						i.setAction(Intent.ACTION_VIEW);
						i.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
						
						MainActivity.this.startActivity(i);
					}
				});
			}
		}
	}
	
	
	final class DownloadAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Controller controller;
			
			if (convertView == null) {
				convertView = View.inflate(MainActivity.this, R.layout.list_item, null);
				
				controller = new Controller();
				controller.title = (TextView) convertView.findViewById(R.id.title);
				controller.desc = (TextView) convertView.findViewById(R.id.desc);
				controller.button = (Button) convertView.findViewById(R.id.button);	
				convertView.setTag(controller);
			} else {
				controller = (Controller) convertView.getTag();
				DownloadItem item = controller.item;
				
				Downloader.getInstance().removeTaskListener(MainActivity.this, item.id(), controller);
			}
			
			DownloadItem item = (DownloadItem) getItem(position);
			controller.item = item;
			
			Downloader.getInstance().addTaskListener(MainActivity.this, item.id(), controller);
			return convertView;
		}	
	}

}
