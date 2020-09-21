package com.mymusic.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mymusic.app.adapter.SongAdapter;
import com.mymusic.app.bean.DataBean;
import com.mymusic.app.bean.MediaData;
import com.mymusic.app.util.PicTransform;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


//用不到的活动,已弃用
@Deprecated
public class PlayListActivity extends AppCompatActivity {
	RecyclerView recyclerView;
	public List<MediaData> songList,tempList;
	SongAdapter songAdapter;
	MediaService mediaService;
	MediaService.Binder binder;
	Intent intent;
	ImageView cover;
	@BindView(R.id.cover_nameText)
	TextView coverNameText;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		setContentView(R.layout.layout_playlist);
		ButterKnife.bind(this);
		cover=findViewById(R.id.playlist_cover);
		intent=getIntent();
		String type=intent.getStringExtra("type");
		long id=intent.getLongExtra("id",0);
		songList=new ArrayList<>();
		tempList=new ArrayList<>();
		if (type.equals(DataBean.TYPE_ALBUM)){
			songList= MediaFactory.getMediaList(this,DataBean.TYPE_ALBUM,id);
			coverNameText.setText(songList.get(0).getAlbum());

		}else{
			songList= MediaFactory.getMediaList(this,DataBean.TYPE_ARTIST,id);
			coverNameText.setText(songList.get(0).getArtist());

		}
		FileDescriptor fd=MediaFactory.getAlbumArtGetDescriptor(this, songList.get(0).getAlbumID());
		Bitmap bitmap=fd==null?PicTransform.getBitmap(this,R.drawable.ic_audiotrack_black_24dp): PicTransform.addGradient(BitmapFactory.decodeFileDescriptor(fd));
		Glide.with(this)
				.load(bitmap)
				.placeholder(R.drawable.cover_background)
				.into(cover);
		recyclerView=findViewById(R.id.main_list);
		tempList.addAll(songList);
		songAdapter =new SongAdapter(this,songList,tempList);
		recyclerView.setAdapter(songAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		bindService(new Intent(this,MediaService.class),serviceConnection, Context.BIND_AUTO_CREATE);
//		songAdapter.setOnItemClickListener(new SongAdapter.OnItemClickListener() {
//			@Override
//			public void onItemClick(int position) {
//				binder.play(position);
//			}
//		});
		songAdapter.setOnItemClickListener(position -> {
//			binder.setSongPlayList(songList);
			binder.playList(songList,position);
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnection);
	}
}
