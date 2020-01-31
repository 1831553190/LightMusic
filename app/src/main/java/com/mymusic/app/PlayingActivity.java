package com.mymusic.app;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mymusic.app.bean.MediaData;
import com.mymusic.app.inter.ServiceUpdate;
import com.mymusic.app.view.SmoothSeekBar;

import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PlayingActivity extends AppCompatActivity implements View.OnClickListener,Runnable {

    ImageView img;
    ImageView btnPre,btnPlay, btnNext;
    private MediaService.Binder binder;
    Toolbar toolbar;
    TextView songName,songArtist;

    MediaPlayer mediaPlayer;

    @BindView(R.id.leftTime)
    TextView leftTime;
    @BindView(R.id.rightTime)
    TextView rightTime;
    @BindView(R.id.seekBar)
    SmoothSeekBar progressBar;
    @BindView(R.id.play_background)
    ConstraintLayout constraintLayout;
    @BindView(R.id.albumCardView)
    CardView albumCard;


    private SimpleDateFormat simpleDateFormat;
    private ValueAnimator animator;


    @Override
    public void run() {
        progressBar.setProgress(mediaPlayer.getCurrentPosition());
        leftTime.setText(simpleDateFormat.format(mediaPlayer.getCurrentPosition()));
        handler.postDelayed(this,1000);
    }

    Handler handler=new Handler();
    //服务端回调
    ServiceUpdate serviceUpdate=()->{
        if (binder.isPlaying()){
            btnPlay.setImageResource(R.drawable.ic_pause_black_24dp);
        }else{
            btnPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
        MediaData data=binder.getMediaData();
        String imgPath=MediaFactory.getAlbumArtCover(getApplicationContext(),data.getAlbumID());
        Glide.with(getApplicationContext()).load(imgPath)
                .error(R.drawable.cover_background)
                .transition(DrawableTransitionOptions.withCrossFade()).into(img);
        songName.setText(data.getTitle());
        songArtist.setText(data.getArtist());
        this.mediaPlayer=binder.getMediaplay();
        progressBar.setMax(mediaPlayer.getDuration());
        rightTime.setText(simpleDateFormat.format(mediaPlayer.getDuration()));
        handler.post(this);
        if (mediaPlayer.isPlaying()){
            handler.post(this);
        }else {
            handler.removeCallbacks(this);
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_play);
        ButterKnife.bind(this);

        bindService(new Intent(this,MediaService.class),serviceConnection, Context.BIND_AUTO_CREATE);
        simpleDateFormat=new SimpleDateFormat("mm:ss");

        img=findViewById(R.id.playImg);
        btnPre=findViewById(R.id.btnPlayPre);
        btnPlay=findViewById(R.id.btnPlayPlay);
        btnNext =findViewById(R.id.btnPlayNext);
        btnPre.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        songArtist=findViewById(R.id.playSongArtist);
        songName=findViewById(R.id.playSongName);
        progressBar.setOnSeekBarChangeListener(new SmoothSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                leftTime.setText(simpleDateFormat.format(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(PlayingActivity.this);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int pro=seekBar.getProgress();
                mediaPlayer.seekTo(pro);
                handler.post(PlayingActivity.this);
            }
        });

    }

    ServiceConnection serviceConnection=new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder= (MediaService.Binder) service;
            Glide.with(getApplicationContext()).load(MediaFactory.getAlbumArtCover(getApplicationContext(),binder.getMediaData().getAlbumID()))
                    .error(R.drawable.cover_background)
                    .transition(DrawableTransitionOptions.withCrossFade()).into(img);
            binder.addListener("play",serviceUpdate);
            serviceUpdate.updateSong();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @OnClick(R.id.albumCardView)
    public void changeView(View v){
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) albumCard.getLayoutParams();
        if (v.getLayoutParams().width==0){
            layoutParams.width=DensityUtil.dip2px(this,200);
            layoutParams.height=DensityUtil.dip2px(this,200);
        }else{
            layoutParams.width = 0;
            layoutParams.height = 0;
        }
        albumCard.setLayoutParams(layoutParams);
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        binder.removeListener("play");
        handler.removeCallbacks(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binder!=null){
            binder.addListener("play",serviceUpdate);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnPlayNext:
                binder.playNext();
                break;
            case R.id.btnPlayPlay:
                binder.playAndPause();
                break;
            case R.id.btnPlayPre:
                binder.playPrevious();
                break;
        }

    }
}

