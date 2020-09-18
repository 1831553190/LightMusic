package com.mymusic.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.mymusic.app.MainActivity;
import com.mymusic.app.MediaService;
import com.mymusic.app.R;
import com.mymusic.app.adapter.SongAdapter;
import com.mymusic.app.bean.MediaData;
import com.mymusic.app.inter.UpdateMag;

import java.util.ArrayList;
import java.util.List;

public class PlayListFragment extends Fragment {


    RecyclerView recyclerView;
    public List<MediaData> songList;
    SongAdapter songAdapter;
    MediaService mediaService;
    MediaService.Binder binder;
    Intent intent;
    ImageView cover;
    private UpdateMag updateMag;
    View view;

    static PlayListFragment playListFragment;
    public static Fragment getInstance(){
        if (playListFragment ==null){
            synchronized (PlayListFragment.class){
                if (playListFragment ==null){
                    playListFragment =new PlayListFragment();
                }
            }
        }
        return playListFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        setRetainInstance(true);
//        if (view==null) {
            view = inflater.inflate(R.layout.layout_playlist, container, false);
//        }
        init(view);
        return view;
    }

    private void init(View view) {

            cover=view.findViewById(R.id.playlist_cover);
            if (updateMag.getBider()!=null){

            }
//            intent=getIntent();
//            String type=intent.getStringExtra("type");
//            long id=intent.getLongExtra("id",0);
            songList=new ArrayList<>();
//            if (type.equals(DataBean.TYPE_ALBUM)){
//                songList= MediaFactory.getMediaList(this,DataBean.TYPE_ALBUM,id);
//            }else{
//                songList= MediaFactory.getMediaList(this,DataBean.TYPE_ARTIST,id);
//            }
//            Glide.with(this)
//                    .load(MediaFactory.getAlbumArtGetDescriptorUri(this,songList.get(0).getAlbumID()))
//                    .placeholder(R.drawable.cover_background)
//                    .into(cover);
//            recyclerView=findViewById(R.id.main_list);
//            songAdapter =new SongAdapter(this,songList);
//            recyclerView.setAdapter(songAdapter);
//            recyclerView.setLayoutManager(new LinearLayoutManager(this));
//            bindService(new Intent(this,MediaService.class),serviceConnection, Context.BIND_AUTO_CREATE);
////		songAdapter.setOnItemClickListener(new SongAdapter.OnItemClickListener() {
////			@Override
////			public void onItemClick(int position) {
////				binder.play(position);
////			}
////		});
//            songAdapter.setOnItemClickListener(position -> {
//                binder.setSongPlayList(songList);
//                binder.playList(songList,position);
//            });
//        }

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity){
            updateMag= (UpdateMag) context;
        }
    }
}
