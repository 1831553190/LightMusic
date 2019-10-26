package com.mymusic.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mymusic.app.adapter.SongAdapter;
import com.mymusic.app.bean.DataBean;
import com.mymusic.app.bean.MediaData;

import java.util.ArrayList;
import java.util.List;

public class PlayListActivity extends AppCompatActivity {
	RecyclerView recyclerView;
	public List<MediaData> songList;
	SongAdapter songAdapter;
	MediaService mediaService;
	MediaService.Binder binder;
	Intent intent;
	ImageView cover;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		setContentView(R.layout.layout_playlist);
		cover=findViewById(R.id.playlist_cover);
		intent=getIntent();
		String type=intent.getStringExtra("type");
		long id=intent.getLongExtra("id",0);
		MediaFactory mediaFactory=new MediaFactory();
		songList=new ArrayList<>();
		if (type.equals(DataBean.TYPE_ALBUM)){
			songList=mediaFactory.getMediaList(this,DataBean.TYPE_ALBUM,id);
		}else{
			songList=mediaFactory.getMediaList(this,DataBean.TYPE_ARTIST,id);
		}
		Glide.with(this)
				.load(mediaFactory.getAlbumArtGetDescriptor(this,songList.get(0).getAlbumID()))
				.error(R.drawable.cover_background)
				.into(cover);
		recyclerView=findViewById(R.id.main_list);
		songAdapter =new SongAdapter(this,songList);
		recyclerView.setAdapter(songAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		bindService(new Intent(this,MediaService.class),serviceConnection, Context.BIND_AUTO_CREATE);
		songAdapter.setOnItemClickListener(new SongAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(int position) {
				binder.play(position);
			}
		});
		songAdapter.setOnItemClickListener(new SongAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(int position) {
				binder.setSongPlayList(songList);
				binder.play(position);
			}
		});
	
	}
	ServiceConnection serviceConnection=new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder= (MediaService.Binder) service;
			
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
		
		}
	};
	
	
}
