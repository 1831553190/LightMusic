package com.mymusic.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mymusic.app.MainActivity;
import com.mymusic.app.inter.ActivityToFragment;
import com.mymusic.app.PlayListActivity;
import com.mymusic.app.R;
import com.mymusic.app.adapter.ArtistAdapter;
import com.mymusic.app.bean.Artist;
import com.mymusic.app.bean.DataBean;
import com.mymusic.app.inter.UpdateMag;

import java.util.ArrayList;
import java.util.List;

public class FragmentSinger extends Fragment implements ArtistAdapter.OnItemClickListener, ActivityToFragment.UpdateArtist {
	private static final int UPDATE=423;
	RecyclerView recyclerView;
	public List<Artist> artistList;
	ArtistAdapter songAdapter;
//	MediaService mediaService;
//	MediaService.Binder binder;
	View view;
	boolean isInit=false;

	UpdateMag updateMag;
	
	
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (!isInit){
			view = inflater.inflate(R.layout.fragment_recycler,container,false);
			init(view);
			isInit=true;
		}
		return view;
	}
	private void init(View view) {
		artistList = new ArrayList<>();
		recyclerView = view.findViewById(R.id.main_list);
		songAdapter = new ArtistAdapter(getContext(), artistList);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setAdapter(songAdapter);
//		getContext().bindService(new Intent(getContext(), MediaService.class), serviceConnection, Context.BIND_AUTO_CREATE);
		songAdapter.setOnItemClickListener(this);
		if (updateMag.getBider()!=null&&artistList.isEmpty()){
			updateMag.getData(2);
		}
	}
	
	@Override
	public void onItemClick(int position) {
		Intent intent=new Intent(getContext(), PlayListActivity.class);
		intent.putExtra("type", DataBean.TYPE_ARTIST);
		intent.putExtra("id",artistList.get(position).getId());
		startActivity(intent);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof MainActivity){
			updateMag= (UpdateMag) context;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}


	@Override
	public void onResume() {
		super.onResume();
		if (updateMag.getBider()!=null&&artistList.isEmpty()){
			updateMag.getData(2);
		}
	}

	@Override
	public void updateData(List<Artist> dataList) {
		artistList.clear();
		artistList.addAll(dataList);
		songAdapter.notifyDataSetChanged();
	}

	@Override
	public void serverConnect() {
		if (artistList.isEmpty()){
			updateMag.getData(2);
		}
	}
}
