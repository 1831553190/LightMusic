package com.mymusic.app;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.google.android.material.snackbar.Snackbar;
import com.mymusic.app.adapter.SongAdapter;
import com.mymusic.app.bean.MediaData;
import com.mymusic.app.fragment.FragmentIndex;
import com.mymusic.app.fragment.FragmentMain;
import com.mymusic.app.fragment.FragmentOther;
import com.mymusic.app.fragment.FragmentSinger;
import com.mymusic.app.inter.ActivityToFragment;
import com.mymusic.app.inter.ServiceUpdate;
import com.mymusic.app.inter.UpdateMag;
import com.mymusic.app.util.FastBlurUtil;
import com.mymusic.app.view.BottomLayout;
import com.mymusic.app.view.MySmoothSeekBar;
import com.mymusic.app.view.SlidingUpPanelLayout;
import com.mymusic.app.view.TimerCircleView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;


import java.io.FileDescriptor;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements UpdateMag, View.OnClickListener {
    ImageView albumImg;
    TextView songName;
    TextView artist;
    private static final int REQUEST_CODE = 200;
    boolean permissionResult = true;
    View bottomView;
    Toolbar toolbar;

    ImageView btnPre, btnPlay, btnNext;

    MediaService.Binder binder;
    FragmentMain fragmentMain;
    Setting setting;

    boolean showBottomProgress=false;

    Handler handler=new Handler();
    private ValueAnimator animator;


    ActivityToFragment.UpdateIndex updateIndex;
    ActivityToFragment.UpdateAlbum updateAlbum;
    ActivityToFragment.UpdateArtist updateArtist;
    SlidingUpPanelLayout slidePanel;


    boolean isPlayLayoutVisible=false;
    boolean animiStart=false;




    @BindView(R.id.bottomLayout)
    BottomLayout bottomLayout;
    @BindView(R.id.bottomFrame)
    FrameLayout frameLayout;


    FragmentManager fragmentManager;
    SharedPreferences preferences;




    /*

    playlayout
     */

    @BindView(R.id.timeCircleView)
    TimerCircleView timerCircleView;
    @BindView(R.id.leftTime)
    TextView leftTime;
    @BindView(R.id.rightTime)
    TextView rightTime;
    @BindView(R.id.seekBar)
    MySmoothSeekBar progressBar;

    @BindView(R.id.albumCardView)
    CardView albumCard;

    @BindView(R.id.includeTopLayout)
    View includeTopLayout;

    @BindView(R.id.bottomPlayLayout)
    View bottomPlayLayout;

    SimpleDateFormat simpleDateFormat;

    @BindView(R.id.btnPlayPre)
    ImageView btnPlayPre;
    @BindView(R.id.btnPlayPlay)
    ImageView btnPlayPlay;
    @BindView(R.id.btnPlayNext)
    ImageView btnPlayNext;
    @BindView(R.id.playSongName)
    TextView playSongName;
    @BindView(R.id.playSongArtist)
    TextView playSongArtist;
    @BindView(R.id.playQueue)
    RecyclerView recyclerView;
    @BindView(R.id.playImg)
    ImageView playAlbumImg;

    List<MediaData> queueList,tempList;
    @BindView(R.id.playQueueSlide)
    SlidingUpPanelLayout secondSlidePanel;
    private int reflushRate;

    @BindView(R.id.repeatBtn)
    ImageView repeatBtn;
    @BindView(R.id.play_background)
    ConstraintLayout playBackground;

    int repeatType=-1;
    boolean show=false;
    DrawableCrossFadeFactory factory;




    private void initPlayLayout() {

        show=preferences.getBoolean("showCircleView",true);
        timerCircleView.setVisibility(show?View.VISIBLE:View.GONE);
        if (!show){
            albumCard.setRadius(DensityUtil.dip2px(this,2));
        }
        repeatType=preferences.getInt("repeatType",0);
        switch (repeatType){
            case 0:
                repeatBtn.setImageResource(R.drawable.ic_baseline_notes_24);
                break;
            case 1:
                repeatBtn.setImageResource(R.drawable.ic_baseline_repeat_24);
                break;
            case 2:
                repeatBtn.setImageResource(R.drawable.ic_baseline_repeat_one_24);
                break;
        }
        simpleDateFormat = new SimpleDateFormat("mm:ss", Locale.CHINESE);
        progressBar.setOnSeekChangeListener(new MySmoothSeekBar.OnSeekChangeListener() {
            @Override
            public void onStartTrack(MySmoothSeekBar seekBar) {
                handler.removeCallbacks(runnable);
            }

            @Override
            public void onProgressChange(MySmoothSeekBar seekBar) {
                leftTime.setText(simpleDateFormat.format(seekBar.getProgress()));
            }

            @Override
            public void onStopTrack(MySmoothSeekBar seekBar) {
                int pro= (int) seekBar.getProgress();
                binder.getMediaplay().seekTo(pro);
                handler.post(runnable);
            }
        });


        secondSlidePanel.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelCollapsed(View panel) {
                recyclerView.scrollToPosition(binder.getCurrentPosition());
            }

            @Override
            public void onPanelExpanded(View panel) {

            }

            @Override
            public void onPanelAnchored(View panel) {

            }

            @Override
            public void onPanelHidden(View panel) {

            }
        });
        factory = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

        queueList=new ArrayList<>();
        tempList=new ArrayList<>();
        SongAdapter queueAdapter=new SongAdapter(this,queueList,tempList);
        recyclerView.setAdapter(queueAdapter);
        queueAdapter.setOnItemClickListener(position -> binder.play(position));
    }


    @OnClick(R.id.albumCardView)
    public void changeView(View v){
        if (show){
            albumCard.setRadius(DensityUtil.dip2px(this,100));
            return;
        }

//        albumCard.setRadius(DensityUtil.dip2px(this,2));
//        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) albumCard.getLayoutParams();
//        if (v.getLayoutParams().width==0||v.getLayoutParams().height==0){
//            layoutParams.width=DensityUtil.dip2px(this,200);
//            layoutParams.height=DensityUtil.dip2px(this,200);
//        }else{
//            layoutParams.width = 0;
//            layoutParams.height = 0;
//        }
//        albumCard.setLayoutParams(layoutParams);
    }

    @OnClick(R.id.repeatBtn)
    void onRepeatBtn(View v){
        repeatType=repeatType==2?0:repeatType+1;
        preferences.edit().putInt("repeatType",repeatType).apply();
        if (binder!=null){
            binder.setRepeatType(repeatType);
        }
        Log.d("TAG", "onRepeatBtn: "+repeatType);
        switch (repeatType){
            case 0:
                repeatBtn.setImageResource(R.drawable.ic_baseline_notes_24);
                Snackbar.make(v,"顺序播放",Snackbar.LENGTH_SHORT).show();
                break;
            case 1:
                repeatBtn.setImageResource(R.drawable.ic_baseline_repeat_24);
                Snackbar.make(v,"循环播放",Snackbar.LENGTH_SHORT).show();
                break;
            case 2:
                repeatBtn.setImageResource(R.drawable.ic_baseline_repeat_one_24);
                Snackbar.make(v,"单曲循环",Snackbar.LENGTH_SHORT).show();
                break;
        }
    }

    @OnClick({R.id.btnPlayPre,R.id.btnPlayPlay,R.id.btnPlayNext})
    void onPlayBtn(View v){
        switch (v.getId()){
            case R.id.btnPlayPre:
                binder.playPrevious();
                break;
            case R.id.btnPlayPlay:
                binder.playAndPause();
                break;
            case R.id.btnPlayNext:
                binder.playNext();
                break;
        }
    }












    Runnable runnable= new Runnable() {
        @Override
        public void run() {
            int progress=binder.getMediaplay().getCurrentPosition();
            if (isPlayLayoutVisible){
                if (show){
                    timerCircleView.setProgress(progress);
                }
                progressBar.setProgress(progress);
                leftTime.setText(simpleDateFormat.format(progress));
            }else{
                bottomLayout.setProgress(progress);
            }
            handler.postDelayed(this,reflushRate);
        }
    };

    ServiceUpdate serviceUpdate = () -> {
        showBottom(binder.getMediaData());
        if (binder.isPlaying()) {
            btnPlayPlay.setImageResource(R.drawable.ic_pause_black_24dp);
            btnPlay.setImageResource(R.drawable.ic_pause_black_24dp);
        } else {
            btnPlayPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            btnPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
//        if (binder.getBitmap()!=null){
//            Bitmap pic=binder.getBitmap();
//            Bitmap bitmap=Bitmap.createBitmap(pic,pic.getWidth()/4,pic.getHeight()/4,pic.getWidth()/2,pic.getHeight()/2);
//            Bitmap inputBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
//            //创建将在ondraw中使用到的经过模糊处理后的bitmap
//            Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);
//
//            //创建RenderScript，ScriptIntrinsicBlur固定写法
//            RenderScript rs = RenderScript.create(this);
//            ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
//
//            //根据inputBitmap，outputBitmap分别分配内存
//            Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
//            Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
//
//            //设置模糊半径取值0-25之间，不同半径得到的模糊效果不同
//            blurScript.setRadius(25f);
//            blurScript.setInput(tmpIn);
//            blurScript.forEach(tmpOut);
//
//            // 模糊 outputBitmap
//            tmpOut.copyTo(outputBitmap);
//            // 将模糊后的 outputBitmap 设为目标 View 的背景
//            playBackground.setBackground(new BitmapDrawable(getResources(), outputBitmap));
//            rs.destroy();
//        }





        FileDescriptor fd=MediaFactory.getAlbumArtGetDescriptor(this,binder.getMediaData().getAlbumID());
        Bitmap bitmap = null;
        if (fd==null){
            bitmap=getBitmap(this,R.mipmap.cover_pic);
        }else{
            bitmap=BitmapFactory.decodeFileDescriptor(fd);
        }
        Palette palette=Palette.from(bitmap).generate();


        Bitmap bitmap1=Bitmap.createBitmap(bitmap,0,0,10,10);
        Palette palette1=Palette.from(bitmap1).generate();



        int color=bitmap1.getPixel(0,0);

        if (color!=0){
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            if(ColorUtils.calculateLuminance(color)>0.5f) { //浅色
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }else{
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }

        }





        secondProgressInit();
        MediaData mediaData=binder.getMediaData();
        playSongName.setText(mediaData.getTitle());
        playSongArtist.setText(mediaData.getArtist());
        rightTime.setText(simpleDateFormat.format(mediaData.getDuration()));
        if (isPlayLayoutVisible){
            MediaPlayer mediaPlayer=binder.getMediaplay();
            timerCircleView.setMax(mediaPlayer.getDuration());
            progressBar.setMax(mediaPlayer.getDuration());

            timerCircleView.setColor(palette.getLightVibrantColor(R.attr.colorAccent));
        }


        if (showBottomProgress&&!isPlayLayoutVisible){
            progressInit();
            if (binder.isPlaying()){

                bottomLayout.setMax(binder.getMediaplay().getDuration());
                bottomLayout.setColor(palette.getLightVibrantColor(R.attr.colorAccent));
            }
        }
        if (binder.isPlaying()){
            handler.removeCallbacks(runnable);
            handler.post(runnable);
        }else{
            handler.removeCallbacks(runnable);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void secondProgressInit() {
            animator=ValueAnimator.ofInt((int)progressBar.getProgress(),binder.getMediaplay().getCurrentPosition()+750);
            animator.setDuration(750);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                int p=(int) animation.getAnimatedValue();
                progressBar.setProgress(p);
                timerCircleView.setProgress(p);
                leftTime.setText(simpleDateFormat.format(p));
            });
            animator.start();
    }

    private boolean isPlay=false;
    private static Bitmap getBitmap(Context context,int vectorDrawableId) {
        Bitmap bitmap=null;
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP){
            Drawable vectorDrawable = context.getDrawable(vectorDrawableId);
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
        }else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), vectorDrawableId);
        }
        return bitmap;
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            int flagTranslucentNavigation = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                WindowManager.LayoutParams attributes = window.getAttributes();
                attributes.flags |= flagTranslucentNavigation;
                window.setAttributes(attributes);
                getWindow().setStatusBarColor(Color.TRANSPARENT);
            } else {
                Window window = getWindow();
                WindowManager.LayoutParams attributes = window.getAttributes();
                attributes.flags |= flagTranslucentStatus | flagTranslucentNavigation;
                window.setAttributes(attributes);
            }
        }

        ButterKnife.bind(this);
        fragmentManager=getSupportFragmentManager();
        if (fragmentMain==null){
            fragmentMain = new FragmentMain();
        }
        if (setting==null){
            setting=new Setting();
        }
        if (fragmentManager.findFragmentByTag("fragmentMain")==null&&getSupportFragmentManager().findFragmentByTag("setting")==null){
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_layout,fragmentMain,"fragmentMain")
                    .add(R.id.fragment_layout,setting,"setting")
                    .hide(setting)
                    .show(fragmentMain)
                    .commit();
        }
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        showBottomProgress= preferences.getBoolean("showProgress",false);
        reflushRate= Integer.parseInt(preferences.getString("level","100"));
        toolbar = findViewById(R.id.toolbar);
        slidePanel = findViewById(R.id.mainSlideLayout);
        slidePanel.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelCollapsed(View panel) {
                isPlayLayoutVisible=false;
                serviceUpdate.updateSong();
            }

            @Override
            public void onPanelExpanded(View panel) {
                isPlayLayoutVisible=true;
                serviceUpdate.updateSong();
                recyclerView.scrollToPosition(binder.getCurrentPosition());
            }

            @Override
            public void onPanelAnchored(View panel) {

            }

            @Override
            public void onPanelHidden(View panel) {

            }
        });

        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initPermission();
        } else {
            init();
            initPlayLayout();
        }
    }



    private void init() {

        bottomView = findViewById(R.id.bottomView);
        albumImg = findViewById(R.id.bototmAlbumImg);
        songName = findViewById(R.id.bottomSongName);
        artist = findViewById(R.id.bottomArtist);
        btnNext = findViewById(R.id.btnNext);
        btnPlay = findViewById(R.id.btnPlay);
        btnPre = findViewById(R.id.btnPre);
        btnPre.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        bottomView.setOnClickListener(this);
        Intent i = new Intent(this, MediaService.class);
        startService(i);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (MediaService.Binder) service;
            binder.addListener("main", serviceUpdate);
            if (updateIndex != null) {
                updateIndex.serverConnect();
            }
            if (updateArtist != null) {
                updateArtist.serverConnect();
            }
            if (updateAlbum != null) {
                updateAlbum.serverConnect();
            }
            if (binder.isPlaying()) {
                binder.updateListener();
            }
            if (binder.isPlaying()){
                isPlay=true;
                showBottom(binder.getMediaData());
            }
            queueList.clear();
            queueList.addAll(binder.getPlayList());
            tempList.addAll(queueList);
            if(repeatType!=-1){
                binder.setRepeatType(repeatType);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void progressInit() {
        Log.d("bottomLayout", "progressInit: "+bottomLayout.getProgress());
        if (!animiStart){
            animator= ValueAnimator.ofInt((int)bottomLayout.getProgress(),binder.getMediaplay().getCurrentPosition()+750);
            animator.setDuration(750);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                bottomLayout.setProgress((int) animation.getAnimatedValue());
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation, boolean isReverse) {
                    animiStart=true;
                }

                @Override
                public void onAnimationEnd(Animator animation, boolean isReverse) {
                    animiStart=false;
                }
            });
            animator.start();
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initPermission() {
        permissionResult = true;
        String[] permission = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        requestPermissions(permission, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults) {
            System.out.println(result);
            if (result == -1) {
                permissionResult = false;
            }
        }
        if (permissionResult) {
            init();
            initPlayLayout();
        } else {
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
    protected void onDestroy() {
        super.onDestroy();
        if (binder!=null){
            unbindService(serviceConnection);
            binder.removeListener("main");
        }
        handler.removeCallbacks(runnable);
        new Thread(() -> Glide.get(MainActivity.this).clearDiskCache()).start();
        Glide.get(this).clearMemory();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (binder != null) {
            binder.addListener("main", serviceUpdate);
        }
    }

    @Override
    public void update(int position) {
        if (binder.getState() == 0) {
            binder.clearState();
        }
        binder.play(position);
        isPlay=true;
    }

    private void showBottom(MediaData data) {

        if (slidePanel.isPanelHidden()&&isPlay()) {
            slidePanel.showPanel();
//            bottomView.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(bottomView, "translationY", 100, 0);
                objectAnimator.setInterpolator(new DecelerateInterpolator());
                objectAnimator.setDuration(350);
                objectAnimator.start();
            }
        }


        Glide.with(this)
                .load(MediaFactory.getAlbumArtGetDescriptorUri(this, data.getAlbumID()))
                .placeholder(albumImg.getDrawable())
                .transition(DrawableTransitionOptions.withCrossFade(factory))
                .error(R.drawable.cover_background)
                .into(albumImg);
        Glide.with(this)
                .load(MediaFactory.getAlbumArtGetDescriptorUri(this, data.getAlbumID()))
                .placeholder(playAlbumImg.getDrawable())
                .transition(DrawableTransitionOptions.withCrossFade(factory))
                .error(R.drawable.cover_background)
                .into(playAlbumImg);
        artist.setText(data.getArtist());
        songName.setText(data.getTitle());
    }

    private boolean isPlay() {
        return isPlay;
    }

    private void hideBottom() {
        if (!slidePanel.isPanelHidden()) {
            slidePanel.hidePanel();
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(bottomView, "translationY", 0, 80);
            objectAnimator.setInterpolator(new AccelerateInterpolator());
            objectAnimator.setDuration(500);
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
//                    bottomView.setVisibility(View.GONE);
                }
            });
            objectAnimator.start();
//            bottomView.setVisibility(View.GONE);
        }
    }





    @Override
    public void getData(int index) {
        switch (index) {
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
    public void playList(List<MediaData> dataList, int pos) {
        binder.playList(dataList, pos);
        isPlay=true;
        queueList.clear();
        queueList.addAll(dataList);
    }

    @Override
    public MediaService.Binder getBider() {
        return binder;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnNext:
                binder.playNext();
                break;
            case R.id.btnPlay:
                binder.playAndPause();
                break;
            case R.id.btnPre:
                binder.playPrevious();
                break;
        }
    }

    @OnClick(R.id.bottomFrame)
    void onclick(View v) {
        switch (v.getId()) {
            case R.id.bottomFrame:
                slidePanel.expandPanel();
                if (fragmentManager.findFragmentByTag("setting").isVisible()){
                    fragmentManager.popBackStack();
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting:
                fragmentManager.beginTransaction()
                        .show(setting)
                        .hide(fragmentMain)
                        .addToBackStack(null)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
                hideBottom();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        System.out.println(fragment.toString());
        if (fragment instanceof FragmentIndex) {
            updateIndex = (ActivityToFragment.UpdateIndex) fragment;
        } else if (fragment instanceof FragmentOther) {
            updateAlbum = (ActivityToFragment.UpdateAlbum) fragment;
        } else if (fragment instanceof FragmentSinger) {
            updateArtist = (ActivityToFragment.UpdateArtist) fragment;
        }
    }

    @Override
    public void hideToolBar() {
        getSupportActionBar().hide();
    }

    @Override
    public void onBackPressed() {
        if (secondSlidePanel.isPanelExpanded()){
            secondSlidePanel.collapsePanel();
        }else if(slidePanel.isPanelExpanded()){
            slidePanel.collapsePanel();
        }else if (fragmentManager.findFragmentByTag("fragmentMain").isVisible()){
            super.onBackPressed();
            if (binder!=null){
                showBottom(binder.getMediaData());
            }
            showBottomProgress= preferences.getBoolean("showProgress",false);
            show=preferences.getBoolean("showCircleView",true);
            timerCircleView.setVisibility(show?View.VISIBLE:View.GONE);
            if (!show){
                albumCard.setRadius(DensityUtil.dip2px(this,2));
            }else{
                albumCard.setRadius(DensityUtil.dip2px(this,100));

            }
            serviceUpdate.updateSong();
        }else{
            super.onBackPressed();
            if (binder!=null){
                showBottom(binder.getMediaData());
            }
            showBottomProgress= preferences.getBoolean("showProgress",false);
            show=preferences.getBoolean("showCircleView",true);
            timerCircleView.setVisibility(show?View.VISIBLE:View.GONE);
            if (!show){
                albumCard.setRadius(DensityUtil.dip2px(this,2));
            }else{
                albumCard.setRadius(DensityUtil.dip2px(this,100));

            }
            serviceUpdate.updateSong();
        }

    }


}