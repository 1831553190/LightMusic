package com.mymusic.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mymusic.app.inter.ActivityToFragment;
import com.mymusic.app.R;
import com.mymusic.app.inter.UpdateMag;
import com.mymusic.app.adapter.SongAdapter;
import com.mymusic.app.bean.MediaData;
import com.mymusic.app.view.MyTextView;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FragmentIndex extends Fragment implements ActivityToFragment.UpdateIndex {
	private static final int UPDATE = 423;
	RecyclerView recyclerView;
	public List<MediaData> songList, tempList;
	SongAdapter songAdapter;
	View view;
	boolean isInit = false;
	UpdateMag updateMag;
	AlertDialog.Builder dialog;


	static FragmentIndex fragmentIndex;

	public static Fragment getInstance() {
		if (fragmentIndex == null) {
			synchronized (FragmentIndex.class) {
				if (fragmentIndex == null) {
					fragmentIndex = new FragmentIndex();
				}
			}
		}
		return fragmentIndex;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_recycler, container, false);
		init(view);
//		setRetainInstance(true);
		return view;
	}

	private void init(View view) {
		songList = new ArrayList<>();
		tempList = new ArrayList<>();
		recyclerView = view.findViewById(R.id.main_list);
		songAdapter = new SongAdapter(getActivity().getBaseContext(), songList, tempList);
		recyclerView.setAdapter(songAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		songAdapter.setOnItemClickListener((position) -> {
			updateMag.playList(songList, position);
		});
		if (updateMag.getBider() != null && songList.isEmpty()) {
			updateMag.getData(0);
		}


		songAdapter.setOnItemLongClickListener((view1, position) -> {
			PopupMenu popupMenu = new PopupMenu(view.getContext(), view1);
			popupMenu.getMenuInflater().inflate(R.menu.song_info, popupMenu.getMenu());
			popupMenu.setOnMenuItemClickListener(item -> {
				View info_view = LayoutInflater.from(getContext()).inflate(R.layout.song_info, null);
				TextView song_size = info_view.findViewById(R.id.text_song_size);
				MyTextView song_path = info_view.findViewById(R.id.text_song_path);
				dialog = new AlertDialog.Builder(view.getContext());
				dialog.setView(info_view);
				song_size.setText(String.format("文件大小：%s", getDataSize(songList.get(position).getFileSize())));
				song_path.setText(songList.get(position).getFilePath());
				dialog.setTitle(songList.get(position).getTitle())
						.setPositiveButton("确定", null)
						.create().show();
				return true;
			});
			popupMenu.show();
		});


//		if (songList.isEmpty()){
//			updateMag.getData(0);
//		}

//			if (binder.getState()!=0){
//				binder.clearState();
//			}
//			binder.play(position);
//			updateMag.update(binder.getMediaList().get(position));


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

	public static String getDataSize(long size) {
		DecimalFormat formater = new DecimalFormat("####.00");
		if (size < 1024) {
			return size + "bytes";
		} else if (size < 1024 * 1024) {
			float kbsize = size / 1024f;
			return formater.format(kbsize) + "KB";
		} else if (size < 1024 * 1024 * 1024) {
			float mbsize = size / 1024f / 1024f;
			return formater.format(mbsize) + "MB";
		} else if (size < 1024 * 1024 * 1024 * 1024) {
			float gbsize = size / 1024f / 1024f / 1024f;
			return formater.format(gbsize) + "GB";
		} else {
			return "size: error";
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof UpdateMag) {
			updateMag = (UpdateMag) context;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void updateData(List<MediaData> dataList) {
		if (songList.isEmpty()) {
			songList.addAll(dataList);
			tempList.addAll(songList);
			songAdapter.notifyDataSetChanged();
		}
	}


	@Override
	public void onResume() {
		super.onResume();
		if (updateMag.getBider() != null && songList.isEmpty()) {
			updateMag.getData(0);
		}
	}

	@Override
	public void serverConnect() {
		if (songList != null && updateMag.getBider() != null && songList.isEmpty()) {
			updateMag.getData(0);
		}
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		inflater.inflate(R.menu.menu_fragment, menu);
		SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				songAdapter.getFilter().filter(newText);
				return true;
			}
		});
		super.onCreateOptionsMenu(menu, inflater);
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
}


