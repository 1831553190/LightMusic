package com.mymusic.app;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.tabs.TabLayout;
import com.mymusic.app.bean.MediaData;
import com.mymusic.app.fragment.FragmentIndex;
import com.mymusic.app.fragment.FragmentOther;
import com.mymusic.app.fragment.FragmentSinger;
import com.mymusic.app.inter.ActivityToFragment;
import com.mymusic.app.inter.ServiceUpdate;
import com.mymusic.app.inter.UpdateMag;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UpdateMag, View.OnClickListener {
    Toolbar toolbar;
    Fragment fragmentIndex,fragmentOther,fragmentSinger;
    String tabTitle[]={"歌曲","专辑","艺术家"};
    private static final int REQUEST_CODE=200;
    boolean permissionResult=true;
    View bottomView;
    ImageView albumImg;
    TextView songName;
    TextView artist;

    ImageView btnPre,btnPlay,btnNext;


    MediaService.Binder binder;
    ActivityToFragment.UpdateIndex updateIndex;
    ActivityToFragment.UpdateAlbum updateAlbum;
    ActivityToFragment.UpdateArtist updateArtist;
    ViewPager viewPager;


    ServiceUpdate serviceUpdate= () -> {
        showBottom(binder.getMediaData());
        if (binder.isPlaying()){
            btnPlay.setImageResource(R.drawable.ic_pause_black_24dp);
        }else{
            btnPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        toolbar=findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initPermission();
        }else{
            init();
        }
    }
    private void init(){
        viewPager = findViewById(R.id.view_pager);
        TabLayout tabs = findViewById(R.id.tabs);

        bottomView = findViewById(R.id.bottomView);
        albumImg=findViewById(R.id.bototmAlbumImg);
        songName=findViewById(R.id.bottomSongName);
        artist=findViewById(R.id.bottomArtist);
        btnNext=findViewById(R.id.btnNext);
        btnPlay=findViewById(R.id.btnPlay);
        btnPre=findViewById(R.id.btnPre);
        btnPre.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        bottomView.setOnClickListener(this);



        final List<Fragment> fragmentList=new ArrayList<>();
        fragmentIndex=new FragmentIndex();
        fragmentOther=new FragmentOther();
        fragmentSinger=new FragmentSinger();
        fragmentList.add(fragmentIndex);
        fragmentList.add(fragmentOther);
        fragmentList.add(fragmentSinger);
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragmentList.get(position);
            }
    
            @Override
            public int getCount() {
                return fragmentList.size();
            }
    
            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return tabTitle[position];
            }
        });
        tabs.setupWithViewPager(viewPager);
        bindService(new Intent(this,MediaService.class),serviceConnection, Context.BIND_AUTO_CREATE);
    }


    ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder= (MediaService.Binder) service;
            binder.addListener("main",serviceUpdate);
            if (updateIndex!=null) {
                updateIndex.serverConnect();
            }if (updateArtist!=null){
                updateArtist.serverConnect();
            }
            if (updateAlbum!=null){
                updateAlbum.serverConnect();
            }
            if (binder.isPlaying()){
                binder.updateListener();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @TargetApi(Build.VERSION_CODES.M)
    private void initPermission(){
        permissionResult=true;
        String[] permission = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        requestPermissions(permission,REQUEST_CODE);
        
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result:grantResults){
            System.out.println(result);
            if (result==-1){
                permissionResult=false;
            }
        }
        if (permissionResult){
            init();
        }else {
           new AlertDialog.Builder(this)
                   .setTitle("权限不足")
                   .setMessage("软件运行权限不足，无法正常工作请授权后重试")
                   .setCancelable(false)
                   .setPositiveButton("去授权", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           initPermission();
                       }
                   }).setNegativeButton("退出", new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                   finish();
               }
           }).show();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof FragmentIndex){
            updateIndex= (ActivityToFragment.UpdateIndex) fragment;
        }else if(fragment instanceof FragmentOther){
            updateAlbum= (ActivityToFragment.UpdateAlbum) fragment;
        }else if(fragment instanceof FragmentSinger){
            updateArtist= (ActivityToFragment.UpdateArtist) fragment;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        new Thread(() -> Glide.get(MainActivity.this).clearDiskCache()).start();
        Glide.get(this).clearMemory();
        binder.removeListener("main");
    }

//    @Override
//    public void update(MediaData data) {
//        if (bottomView.getVisibility()==View.GONE){
//            bottomView.setVisibility(View.VISIBLE);
//        }
//        if (data!=null){
////            Glide.with(this).load(MediaFactory.getAlbumArtCover(this,data.getAlbumID())).into(albumImg);
//            artist.setText(data.getArtist());
//            songName.setText(data.getTitle());
//        }
//    }


    @Override
    protected void onResume() {
        super.onResume();
        if (binder!=null){
//            if (binder.isPlaying()){
//                showBottom(binder.getMediaData());
//            }
            binder.addListener("main",serviceUpdate);
        }


    }

    @Override
    public void update(int position) {
        if (binder.getState()==0){
            binder.clearState();
        }
        binder.play(position);
//        showBottom(binder.getMediaData());
    }

    private void showBottom(MediaData data){
        if (bottomView.getVisibility()==View.GONE&&binder.isPlaying()){
            bottomView.setVisibility(View.VISIBLE);
            ObjectAnimator objectAnimator=ObjectAnimator.ofFloat(bottomView,"translationY",70,0);
            AnimatorSet animatorSet=new AnimatorSet();
            animatorSet.setDuration(500);
            animatorSet.play(objectAnimator);
            animatorSet.setTarget(bottomView);
            animatorSet.setInterpolator(new DecelerateInterpolator());
            animatorSet.start();
        }
        Glide.with(this)
                .load(MediaFactory.getAlbumArtCover(this,data.getAlbumID()))
                .error(R.drawable.cover_background)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(albumImg);
        artist.setText(data.getArtist());
        songName.setText(data.getTitle());
    }



    @Override
    public void getData(int index) {
        switch (index){
            case 0:
                updateIndex.updateData(binder.getMediaList());
                break;
            case 1:
                updateAlbum.updateData(binder.getAlbumList());
                break;
            case 2:
                updateArtist.updateData(binder.getArtistList());
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnNext:
                binder.playNext();
                break;
            case R.id.btnPlay:
                binder.playAndPause();
                break;
            case R.id.btnPre:
                binder.playPrevious();
                break;
            case R.id.bottomView:
                startActivity(new Intent(this,PlayingActivity.class));
        }
    }
}