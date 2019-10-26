package com.mymusic.app.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mymusic.app.MediaService;
import com.mymusic.app.R;
import com.mymusic.app.adapter.SongAdapter;
import com.mymusic.app.bean.MediaData;


import java.util.ArrayList;
import java.util.List;

public class FragmentIndex extends Fragment {
	private static final int UPDATE=423;
	RecyclerView recyclerView;
	public List<MediaData> songList;
	SongAdapter songAdapter;
	MediaService mediaService;
	MediaService.Binder binder;
	View view;
	boolean isInit=false;
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (!isInit){
			view = inflater.inflate(R.layout.fragment_main,container,false);
			init(view);
			isInit=true;
		}
		return view;
	}
	private void init(View view){
		songList=new ArrayList<>();
		recyclerView=view.findViewById(R.id.main_list);
		songAdapter =new SongAdapter(getContext(),songList);
		recyclerView.setAdapter(songAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		getContext().bindService(new Intent(getContext(),MediaService.class),serviceConnection,Context.BIND_AUTO_CREATE);
		songAdapter.setOnItemClickListener(new SongAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(int position) {
				if (binder.getState()!=0){
					binder.clearState();
				}
				binder.play(position);
			}
		});
//		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//			@Override
//			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//				super.onScrolled(recyclerView, dx, dy);
//				if (Math.abs(dy)<50){
//					Glide.with(getContext()).resumeRequests();
//				}else{
//					Glide.with(getContext()).pauseRequests();
//				}
//
//			}
//
//			@Override
//			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//				super.onScrollStateChanged(recyclerView, newState);
//				if (newState==RecyclerView.SCROLL_STATE_IDLE){
//					Glide.with(getContext()).resumeRequests();
//				}
//			}
//		});
		
	}
	
	ServiceConnection serviceConnection=new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder= (MediaService.Binder) service;
			songList.addAll(binder.getMediaList());
			songAdapter.notifyDataSetChanged();
			
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
		
		}
	};
	
	
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		getContext().unbindService(serviceConnection);
	}
}
