package com.mymusic.app.fragment;

import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
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
import com.mymusic.app.adapter.AlbumAdapter;
import com.mymusic.app.bean.AlbumData;
import com.mymusic.app.bean.DataBean;

import java.util.ArrayList;
import java.util.List;

public class FragmentOther extends Fragment implements AlbumAdapter.OnItemClickListener {
	AlbumAdapter albumAdapter;
	List<AlbumData> albumDataList;
	private MediaService.Binder binder;
	View view;
	private boolean isInit=false;
	RecyclerView recyclerView;
	
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (!isInit) {
			view = inflater.inflate(R.layout.fragment_main, container, false);
			getContext().bindService(new Intent(getContext(), MediaService.class), serviceConnection, Context.BIND_AUTO_CREATE);
			init(view);
			isInit=true;
		}
		return view;
	}
	private void init(View view){
		recyclerView=view.findViewById(R.id.main_list);
		albumDataList=new ArrayList<>();
		albumAdapter=new AlbumAdapter(getContext(),albumDataList);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setAdapter(albumAdapter);
		albumAdapter.setOnItemClickListener(this);
	}
	
	
	ServiceConnection serviceConnection=new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder= (MediaService.Binder) service;
			albumDataList.addAll(binder.getAlbumList());
			albumAdapter.notifyDataSetChanged();
			
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
	
	@Override
	public void onItemClick(int position) {
		Intent intent=new Intent(getContext(), PlayListActivity.class);
		intent.putExtra("type", DataBean.TYPE_ALBUM);
		intent.putExtra("id",albumDataList.get(position).getAlbumId());
		Bundle bundle= ActivityOptions.makeSceneTransitionAnimation(getActivity(),albumAdapter.getImageView(),getString(R.string.transitionName)).toBundle();
		getActivity().startActivity(intent);
	}
}
