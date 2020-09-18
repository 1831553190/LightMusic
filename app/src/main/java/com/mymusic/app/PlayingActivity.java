package com.mymusic.app;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mymusic.app.bean.MediaData;
import com.mymusic.app.inter.ServiceUpdate;
import com.mymusic.app.view.MySmoothSeekBar;
import com.mymusic.app.view.TimerCircleView;

import java.io.FileDescriptor;
import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayingActivity extends AppCompatActivity implements View.OnClickListener, Runnable {

    ImageView img;
    ImageView btnPre, btnPlay, btnNext;
    private MediaService.Binder binder;
    Toolbar toolbar;
    TextView songName, songArtist;

    MediaPlayer mediaPlayer;
    SharedPreferences preferences;
    int reflushRate = 1000;

    @BindView(R.id.leftTime)
    TextView leftTime;
    @BindView(R.id.rightTime)
    TextView rightTime;
    @BindView(R.id.seekBar)
    MySmoothSeekBar progressBar;
    @BindView(R.id.play_background)
    ConstraintLayout constraintLayout;
//    @BindView(R.id.albumCardView)
//    CardView albumCard;

    @BindView(R.id.includeTopLayout)
    View includeTopLayout;

    @BindView(R.id.bottomPlayLayout)
    View bottomLayout;

    @BindView(R.id.timeCircleView)
    TimerCircleView timerCircleView;


    private SimpleDateFormat simpleDateFormat;
    private ValueAnimator animator;
    int progress = 0;

    boolean show = true;


    @Override
    public void run() {
        progress = mediaPlayer.getCurrentPosition();
        leftTime.setText(simpleDateFormat.format(progress));
        progressBar.setProgress(progress);
        if (show) {
            timerCircleView.setProgress(progress);
        }
        handler.postDelayed(this, reflushRate);
    }

    Handler handler = new Handler();
    //服务端回调
    ServiceUpdate serviceUpdate = new ServiceUpdate() {
        @Override
        public void updateSongInfo() {
            if (binder.isPlaying()) {
                btnPlay.setImageResource(R.drawable.ic_pause_black_24dp);
            } else {
                btnPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            }
            MediaData data = binder.getMediaData();
            Uri imgPath = MediaFactory.getAlbumArtGetDescriptorUri(getApplicationContext(), data.getAlbumID());
            Glide.with(getApplicationContext()).load(imgPath)
                    .placeholder(R.mipmap.cover_pic)
                    .transition(DrawableTransitionOptions.withCrossFade()).into(img);
            songName.setText(data.getTitle());
            songArtist.setText(data.getArtist());
            PlayingActivity.this.mediaPlayer = binder.getMediaplay();
            progressBar.setProgress(mediaPlayer.getCurrentPosition());
            progressBar.setMax(mediaPlayer.getDuration());
            timerCircleView.setMax(mediaPlayer.getDuration());
            FileDescriptor fd = MediaFactory.getAlbumArtGetDescriptor(PlayingActivity.this, data.getAlbumID());
            if (fd == null) {
                setColor(getBitmap(PlayingActivity.this, R.drawable.cover_background));
            } else {
                setColor(BitmapFactory.decodeFileDescriptor(fd));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                progressInit();
            }
            rightTime.setText(simpleDateFormat.format(mediaPlayer.getDuration()));
            if (mediaPlayer.isPlaying()) {
                handler.removeCallbacks(PlayingActivity.this);
                handler.post(PlayingActivity.this);
            } else {
                handler.removeCallbacks(PlayingActivity.this);
            }



        }

        @Override
        public void statePlayAndPauseChange() {

        }

        @Override
        public void stateResume() {

        }
    };

    private static Bitmap getBitmap(Context context, int vectorDrawableId) {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Drawable vectorDrawable = context.getDrawable(vectorDrawableId);
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), vectorDrawableId);
        }
        return bitmap;
    }


    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void progressInit() {

        animator = ValueAnimator.ofInt((int) progressBar.getProgress(), mediaPlayer.getCurrentPosition() + 750);
        animator.setDuration(750);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            progressBar.setProgress((int) animation.getAnimatedValue());
        });
        animator.start();
    }


    public void setColor(Bitmap bitmap) {
        Palette palette = Palette.from(bitmap).generate();
        timerCircleView.setColor(palette.getLightVibrantColor(R.attr.colorAccent));
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_play);
        ButterKnife.bind(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        reflushRate = Integer.parseInt(preferences.getString("level", "1000"));
        show = preferences.getBoolean("showCircleView", false);
        timerCircleView.setVisibility(show ? View.VISIBLE : View.GONE);
        bindService(new Intent(this, MediaService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        simpleDateFormat = new SimpleDateFormat("mm:ss", Locale.CHINESE);

        img = findViewById(R.id.playImg);
        btnPre = findViewById(R.id.btnPlayPre);
        btnPlay = findViewById(R.id.btnPlayPlay);
        btnNext = findViewById(R.id.btnPlayNext);
        btnPre.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        songArtist = findViewById(R.id.playSongArtist);
        songName = findViewById(R.id.playSongName);


        progressBar.setOnSeekChangeListener(new MySmoothSeekBar.OnSeekChangeListener() {
            @Override
            public void onStartTrack(MySmoothSeekBar seekBar) {
                handler.removeCallbacks(PlayingActivity.this);
            }

            @Override
            public void onProgressChange(MySmoothSeekBar seekBar) {
                leftTime.setText(simpleDateFormat.format(seekBar.getProgress()));
            }

            @Override
            public void onStopTrack(MySmoothSeekBar seekBar) {
                int pro = (int) seekBar.getProgress();
                mediaPlayer.seekTo(pro);
                handler.post(PlayingActivity.this);
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void shouAnimi() {
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(includeTopLayout, "translationY", -100, 0);
        objectAnimator2.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(bottomLayout, "translationY", 300, 0);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator, objectAnimator2);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.start();
    }

    ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (MediaService.Binder) service;
            binder.addListener("play", serviceUpdate);
            serviceUpdate.updateSongInfo();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                shouAnimi();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

//
//    @OnClick(R.id.albumCardView)
//    public void changeView(View v){
//        if (show){
//            albumCard.setRadius(DensityUtil.dip2px(this,100));
//            return;
//        }
//        albumCard.setRadius(DensityUtil.dip2px(this,2));
//        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) albumCard.getLayoutParams();
//        if (v.getLayoutParams().width==0){
//            layoutParams.width=DensityUtil.dip2px(this,200);
//            layoutParams.height=DensityUtil.dip2px(this,200);
//        }else{
//            layoutParams.width = 0;
//            layoutParams.height = 0;
//        }
//        albumCard.setLayoutParams(layoutParams);
//    }


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
        if (binder != null) {
            binder.addListener("play", serviceUpdate);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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

