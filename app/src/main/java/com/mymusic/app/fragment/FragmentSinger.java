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

import com.mymusic.app.MediaService;
import com.mymusic.app.PlayListActivity;
import com.mymusic.app.R;
import com.mymusic.app.adapter.ArtistAdapter;
import com.mymusic.app.adapter.SongAdapter;
import com.mymusic.app.bean.Artist;
import com.mymusic.app.bean.DataBean;
import com.mymusic.app.bean.MediaData;

import java.util.ArrayList;
import java.util.List;

public class FragmentSinger extends Fragment implements ArtistAdapter.OnItemClickListener {
	private static final int UPDATE=423;
	RecyclerView recyclerView;
	public List<Artist> artistList;
	ArtistAdapter songAdapter;
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
	private void init(View view) {
		artistList = new ArrayList<>();
		recyclerView = view.findViewById(R.id.main_list);
		songAdapter = new ArtistAdapter(getContext(), artistList);
		recyclerView.setAdapter(songAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		getContext().bindService(new Intent(getContext(), MediaService.class), serviceConnection, Context.BIND_AUTO_CREATE);
		songAdapter.setOnItemClickListener(this);
		
	}
	
	ServiceConnection serviceConnection=new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder= (MediaService.Binder) service;
			artistList.addAll(binder.getArtistList());
			songAdapter.notifyDataSetChanged();
			
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
		
		}
	};
	
	@Override
	public void onItemClick(int position) {
		Intent intent=new Intent(getContext(), PlayListActivity.class);
		intent.putExtra("type", DataBean.TYPE_ARTIST);
		intent.putExtra("id",artistList.get(position).getId());
		startActivity(intent);
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		getContext().unbindService(serviceConnection);
	}
}
