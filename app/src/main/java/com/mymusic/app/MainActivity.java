package com.mymusic.app;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mymusic.app.adapter.SongAdapter;
import com.mymusic.app.bean.MediaData;
import com.mymusic.app.controller.VolumeDialogController;
import com.mymusic.app.fragment.FragmentIndex;
import com.mymusic.app.fragment.FragmentMain;
import com.mymusic.app.fragment.FragmentOther;
import com.mymusic.app.fragment.FragmentSinger;
import com.mymusic.app.inter.ActivityToFragment;
import com.mymusic.app.inter.ServiceUpdate;
import com.mymusic.app.inter.UpdateMag;
import com.mymusic.app.util.AlbumUtils;
import com.mymusic.app.util.BitmapTransform;
import com.mymusic.app.util.PicTransform;
import com.mymusic.app.view.BottomLayout;
import com.mymusic.app.view.MyImageView;
import com.mymusic.app.view.MySmoothSeekBar;
import com.mymusic.app.view.SlidingUpPanelLayout;
import com.mymusic.app.view.VolumeDialog;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
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
import kotlin.Pair;

public class MainActivity extends AppCompatActivity implements UpdateMag, View.OnClickListener, Runnable {
	ImageView albumImg;
	TextView songName;
	TextView artist;
	private static final int REQUEST_CODE = 200;
	boolean permissionResult = true;
	View bottomView;
	Toolbar toolbar;

	ImageView btnPre, btnPlay, btnNext;

	MediaService.Binder binder;
//    static final FragmentMain fragmentMain=new FragmentMain();
//    static final Setting setting=new Setting();


	boolean showBottomProgress = false;

	Handler handler = new Handler();
	private ValueAnimator animator;


	ActivityToFragment.UpdateIndex updateIndex;
	ActivityToFragment.UpdateAlbum updateAlbum;
	ActivityToFragment.UpdateArtist updateArtist;
	SlidingUpPanelLayout slidePanel;


	boolean isPlayLayoutVisible = false;
	boolean animiStart = false;


	@BindView(R.id.bottomLayout)
	BottomLayout bottomLayout;
	@BindView(R.id.bottomFrame)
	FrameLayout frameLayout;


	//    FragmentManager fragmentManager;
	SharedPreferences preferences;
	private long albumId = -1;
	float vol = 1.0f;


	/*

	playlayout
	 */
//
//    @BindView(R.id.timeCircleView)
//    TimerCircleView timerCircleView;
	@BindView(R.id.leftTime)
	TextView leftTime;
	@BindView(R.id.rightTime)
	TextView rightTime;
	@BindView(R.id.seekBar)
	MySmoothSeekBar progressBar;

//    @BindView(R.id.albumCardView)
//    CardView albumCard;

	@BindView(R.id.includeTopLayout)
	View includeTopLayout;

	@BindView(R.id.bottomPlayLayout)
	View bottomPlayLayout;

	SimpleDateFormat simpleDateFormat;

	@BindView(R.id.btnPlayPre)
	ImageView btnPlayPre;
	@BindView(R.id.btnPlayPlay)
	FloatingActionButton btnPlayPlay;
	@BindView(R.id.btnPlayNext)
	ImageView btnPlayNext;
	@BindView(R.id.playSongName)
	TextView playSongName;
	@BindView(R.id.playSongArtist)
	TextView playSongArtist;
	@BindView(R.id.playQueue)
	RecyclerView recyclerView;
	@BindView(R.id.playImg)
	MyImageView playAlbumImg;

	List<MediaData> queueList, tempList;
	@BindView(R.id.playQueueSlide)
	SlidingUpPanelLayout secondSlidePanel;
	private int reflushRate;

	@BindView(R.id.queueLayoutText)
	TextView queueText;
	@BindView(R.id.playingText)
	TextView playingText;

	@BindView(R.id.repeatBtn)
	ImageView repeatBtn;
	@BindView(R.id.play_background)
	ConstraintLayout playBackground;

	int repeatType = -1;
	boolean show = false;
	DrawableCrossFadeFactory factory;
	PlayPauseDrawable playPauseDrawable;
	SongAdapter queueAdapter;


	boolean showBlru = false;
	private FragmentManager fragmentManager = getSupportFragmentManager();
	private boolean detachVol = false;

	VolumeDialog volumeDialog;
	VolumeDialogController controller;
	AudioManager audioManager;
	static int m = 0;

	AlbumUtils colorUtil;


	private void initPlayLayout() {
		showBlru = preferences.getBoolean("showBlru", false);
		show = preferences.getBoolean("showCircleView", true);
//        timerCircleView.setVisibility(show?View.VISIBLE:View.GONE);
//        if (!show){
//            albumCard.setRadius(DensityUtil.dip2px(this,2));
//        }
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		controller = new VolumeDialogController(this);
		volumeDialog = new VolumeDialog(this, controller, WindowManager.LayoutParams.TYPE_APPLICATION_PANEL, new VolumeDialog.SCallBack() {

			@Override
			public void seekChange(VolumeDialog.VolumeRow row) {

			}

			@Override
			public void onProgressChanged(VolumeDialog.VolumeRow row, SeekBar seekBar, int progress, boolean fromUser) {
				if (binder != null) {
					if (row.getStream() == 0) {
						binder.setVol(progress);
					}
				}
			}

			@Override
			public void onStopTrackingTouch(VolumeDialog.VolumeRow row, SeekBar seekBar, int progress) {
				if (binder != null) {
					if (row.getStream() == 1) {
						audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (progress / 100f * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);
					}
				}
			}

			@Override
			public void onStartTrackingTouch(VolumeDialog.VolumeRow row, SeekBar seekBar) {

			}
		});


		playPauseDrawable = new PlayPauseDrawable(this);

		btnPlayPlay.setImageDrawable(playPauseDrawable);

		repeatType = preferences.getInt("repeatType", 0);
		switch (repeatType) {
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
				handler.removeCallbacks(MainActivity.this);
			}

			@Override
			public void onProgressChange(MySmoothSeekBar seekBar) {
				leftTime.setText(simpleDateFormat.format(seekBar.getProgress()));
			}

			@Override
			public void onStopTrack(MySmoothSeekBar seekBar) {
				int pro = (int) seekBar.getProgress();
				binder.getMediaplay().seekTo(pro);
				handler.post(MainActivity.this);
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
		queueList = new ArrayList<>();
		tempList = new ArrayList<>();
		queueAdapter = new SongAdapter(this, queueList, tempList);
		recyclerView.setAdapter(queueAdapter);
		queueAdapter.setOnItemClickListener(position -> binder.play(position));

	}


//    @OnClick(R.id.albumCardView)
//    public void changeView(View v){
//        if (show){
//            albumCard.setRadius(DensityUtil.dip2px(this,100));
//            return;
//        }

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
//    }

	@OnClick(R.id.repeatBtn)
	void onRepeatBtn(View v) {
		repeatType = repeatType == 2 ? 0 : repeatType + 1;
		preferences.edit().putInt("repeatType", repeatType).apply();
		if (binder != null) {
			binder.setRepeatType(repeatType);
		}
		Log.d("TAG", "onRepeatBtn: " + repeatType);
		switch (repeatType) {
			case 0:
				repeatBtn.setImageResource(R.drawable.ic_baseline_notes_24);
				Snackbar.make(v, "顺序播放", Snackbar.LENGTH_SHORT).show();
				break;
			case 1:
				repeatBtn.setImageResource(R.drawable.ic_baseline_repeat_24);
				Snackbar.make(v, "循环播放", Snackbar.LENGTH_SHORT).show();
				break;
			case 2:
				repeatBtn.setImageResource(R.drawable.ic_baseline_repeat_one_24);
				Snackbar.make(v, "单曲循环", Snackbar.LENGTH_SHORT).show();
				break;
		}
	}

	@OnClick({R.id.btnPlayPre, R.id.btnPlayPlay, R.id.btnPlayNext})
	void onPlayBtn(View v) {
		switch (v.getId()) {
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


	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	private void btnTint(ColorStateList... colorStateList) {
//        按钮着色
		btnPlayPre.setImageTintList(colorStateList[1]);

		playPauseDrawable.setColor(colorStateList[1]);
		btnPlayNext.setImageTintList(colorStateList[1]);
		repeatBtn.setImageTintList(colorStateList[1]);
		//文本着色
		playSongName.setTextColor(colorStateList[1]);
		playSongArtist.setTextColor(colorStateList[1]);
		if (queueAdapter != null) {
			queueAdapter.setTextColor(colorStateList[1]);
			queueAdapter.notifyDataSetChanged();

		}

		//文本着色
		leftTime.setTextColor(colorStateList[1]);
		rightTime.setTextColor(colorStateList[1]);
		progressBar.setProgressColor(colorStateList[1].getDefaultColor());
		queueText.setTextColor(colorStateList[1]);
		playingText.setTextColor(colorStateList[1]);

	}


	//    Runnable runnable= new Runnable() {
	@Override
	public void run() {
		if (binder != null) {
			int progress = binder.getMediaplay().getCurrentPosition();
			if (isPlayLayoutVisible) {
//                if (show){
////                    timerCircleView.setProgress(progress);
//                }
				progressBar.setProgress(progress);
				leftTime.setText(simpleDateFormat.format(progress));
			} else {
				bottomLayout.setProgress(progress);
			}
		}

		handler.postDelayed(this, reflushRate);
	}

//    };


	public int getBrighterColor(int color) {     //增加颜色亮度
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv); // convert to hsv

		hsv[1] = hsv[1] - 0.1f; // less saturation
		hsv[2] = hsv[2] + 0.1f; //imageView.setColorFilter(cmcf); more brightness
		return Color.HSVToColor(hsv);
	}

//    public int getDarkerColor(int color){     //降低颜色亮度
//        float[] hsv = new float[3];
//        Color.colorToHSV(color, hsv); // convert to hsv
//
//        hsv[1] = hsv[1] - 0.1f; // less saturation
//        hsv[2] = hsv[2] - 0.1f; // more brightness
//        return Color.HSVToColor(hsv);
//    }

	ServiceUpdate serviceUpdate = new ServiceUpdate() {
		@Override
		public void updateSongInfo() {
			replaceAlbumArt();

			showBottom(binder.getMediaData());


			FileDescriptor fd = MediaFactory.getAlbumArtGetDescriptor(MainActivity.this, binder.getMediaData().getAlbumID());
			Bitmap bitmap = null;
			if (fd == null) {
				bitmap = getBitmap(MainActivity.this, R.drawable.ic_audiotrack_white_24dp);
			} else {
				bitmap = BitmapFactory.decodeFileDescriptor(fd);
			}
			Palette palette = Palette.from(bitmap).generate();
			Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, 10, 10);
//        secondSlidePanel.setBackground(palette.getLightVibrantColor(Color.WHITE));

//            btnPlayPlay.setBackgroundTintList(ColorStateList.valueOf(getBrighterColor(palette.getLightMutedColor(Color.WHITE))));


			if (showBlru) {          //高斯模糊背景
				Bitmap outputBitmap;
				Bitmap inputBitmap = null;
				Bitmap pic = binder.getBitmap();
				if (pic == null) {
					Bitmap bitRes = getBitmap(MainActivity.this, R.drawable.ic_audiotrack_black_24dp);
					Bitmap bitmap2 = Bitmap.createBitmap(bitRes, bitRes.getWidth() / 4, 0, bitRes.getWidth() / 2, bitRes.getHeight());
					inputBitmap = Bitmap.createScaledBitmap(bitmap2, 150, 150, false);
				} else {
					Bitmap bitmap2 = Bitmap.createBitmap(pic, pic.getWidth() / 4, pic.getHeight() / 4, pic.getWidth() / 2, pic.getHeight() / 2);
					inputBitmap = Bitmap.createScaledBitmap(bitmap2, 150, 150, false);
				}
				//创建将在ondraw中使用到的经过模糊处理后的bitmap
				outputBitmap = Bitmap.createBitmap(inputBitmap);

				//创建RenderScript，ScriptIntrinsicBlur固定写法
				RenderScript rs = RenderScript.create(MainActivity.this);
				ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

				//根据inputBitmap，outputBitmap分别分配内存
				Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
				Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);

				//设置模糊半径取值0-25之间，不同半径得到的模糊效果不同
				blurScript.setRadius(25f);
				blurScript.setInput(tmpIn);
				blurScript.forEach(tmpOut);

				// 模糊 outputBitmap
				tmpOut.copyTo(outputBitmap);

				int brightness = 20; //RGB偏移量，变暗为负数
				ColorMatrix matrix = new ColorMatrix();
				matrix.setSaturation(0.7f);
				matrix.set(new float[]{1, 0, 0, 0, brightness, 0, 1, 0, 0, brightness, 0, 0, 1, 0, brightness, 0, 0, 0, 1, 0});
				ColorMatrixColorFilter cmcf = new ColorMatrixColorFilter(matrix);
				Paint paint = new Paint();
				paint.setColorFilter(cmcf);
				Canvas canvas = new Canvas(outputBitmap);
				canvas.drawBitmap(outputBitmap, new Matrix(), paint);

				// 将模糊后的 outputBitmap 设为目标 View 的背景
//                getDarkerColor(outputBitmap)

				secondSlidePanel.setBackground(new BitmapDrawable(getResources(), outputBitmap));
//                secondSlidePanel.setBackgroundColor(palette.getLightMutedColor(Color.WHITE));
				rs.destroy();
				Palette palette1 = Palette.from(outputBitmap).generate();
//                btnPlayPlay.setBackgroundTintList(
//                        ColorStateList.valueOf(palette.getLightMutedColor(getResources().getColor(android.R.color.white))));


				if (ColorUtils.calculateLuminance(outputBitmap.getPixel(0, 0)) > 0.5) {
					btnTint(ColorStateList.valueOf(getResources().getColor(R.color.gray2)),
							ColorStateList.valueOf(palette.getDarkVibrantColor(palette.getDarkMutedColor(
									getResources().getColor(R.color.gray2)))));
				} else {
					btnTint(ColorStateList.valueOf(Color.WHITE), ColorStateList.valueOf(palette.getDarkVibrantColor(
							palette.getDarkMutedColor(
									getResources().getColor(R.color.gray2)))));
				}

			} else {
				Pair<Integer, Integer> colorPair = colorUtil.processNotification(bitmap);
//                secondSlidePanel.setBackgroundColor(palette.getLightMutedColor(Color.WHITE));
//                if (colorPair.component1()<0){
//                    secondSlidePanel.setBackgroundColor(Color.WHITE);
//                }else{
				secondSlidePanel.setBackgroundColor(colorPair.component1());

//                }
//                btnTint(ColorStateList.valueOf(getResources().getColor(R.color.gray2)),ColorStateList.valueOf(palette.getDarkMutedColor(
//                        palette.getDarkVibrantColor(
//                                getResources().getColor(R.color.gray2)))));
//				btnTint(ColorStateList.valueOf(getResources().getColor(R.color.gray2)), ColorStateList.valueOf(
//						colorUtil.isColorLight(colorPair.component1())?
//								palette.getDarkMutedColor(getResources().getColor(R.color.gray2)):palette.getLightMutedColor(Color.WHITE)));
				btnTint(ColorStateList.valueOf(getResources().getColor(R.color.gray2)), ColorStateList.valueOf(colorPair.component2()));
//				btnTint(ColorStateList.valueOf(getResources().getColor(R.color.gray2)), ColorStateList.valueOf(palette.getDominantColor()));
				btnPlayPlay.setBackgroundTintList(ColorStateList.valueOf(getBrighterColor(colorPair.component1())));

			}


			int color = bitmap1.getPixel(0, 0);

			if (color != 0) {      //状态栏图标变色
				int red = Color.red(color);
				int green = Color.green(color);
				int blue = Color.blue(color);
				if (ColorUtils.calculateLuminance(color) > 0.5f) { //浅色
					getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
				} else {
					getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
				}
			}


			MediaData mediaData = binder.getMediaData();
			playSongName.setText(mediaData.getTitle());
			playSongArtist.setText(mediaData.getArtist());
			rightTime.setText(simpleDateFormat.format(mediaData.getDuration()));
//            if (isPlayLayoutVisible){
			MediaPlayer mediaPlayer = binder.getMediaplay();
			progressBar.setMax(mediaPlayer.getDuration());
//                timerCircleView.setMax(medFiaPlayer.getDuration());                //过时
//                timerCircleView.setColor(palette.getLightVibrantColor(R.attr.colorAccent));
//            }


		}


		@Override
		public void statePlayAndPauseChange() {
			if (binder.isPlaying()) {
				playPauseDrawable.setPause(true);
//                btnPlayPlay.setImageResource(R.drawable.ic_pause_black_24dp);
				btnPlay.setImageResource(R.drawable.ic_pause_black_24dp);
			} else {
				playPauseDrawable.setPlay(true);
//                btnPlayPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
				btnPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
			}
		}

		@Override
		public void stateResume() {
//            replaceAlbumArt();
			Bitmap cover = binder.getBitmap();    //获取专辑图
			if (cover == null) {                   //如果专辑图为空
				cover = BitmapFactory.decodeResource(getResources(), R.mipmap.cover_pic);         //用drawable目录的图片
			}
			Palette palette = Palette.from(cover).generate();             //传入图片取色

			if (showBottomProgress && !isPlayLayoutVisible) {          //如果开启底部进度条，并且可见，则显示动画
				if (binder.isPlaying()) {
//                设置底部进度条的值
					bottomLayout.setMax(binder.getMediaplay().getDuration());
					bottomLayout.setColor(palette.getLightVibrantColor(R.attr.colorAccent));
				}
			}



			if (binder.isPlaying()) {            //如果音乐播放更新ui
				handler.removeCallbacks(MainActivity.this);                //发现多次post会有问题，所以先移除
				//更新进度条状态
				progressInit();     //进度条动画
				secondProgressInit();      //显示进度条动画
				handler.post(MainActivity.this);
			} else {
				handler.removeCallbacks(MainActivity.this);
			}
		}
	};


	private void replaceAlbumArt() {

		MediaData mediaData = binder.getMediaData();
		if (mediaData.getAlbumID() != albumId) {
			albumId = mediaData.getAlbumID();
			FileDescriptor fd = MediaFactory.getAlbumArtGetDescriptor(this, mediaData.getAlbumID());
			Bitmap bitmap = fd == null ? getBitmap(this, R.drawable.ic_audiotrack_black_24dp) : PicTransform.addGradient(BitmapFactory.decodeFileDescriptor(fd));
//            Bitmap sourceBitmap=fd==null?getBitmap(this,R.drawable.ic_audiotrack_black_24dp):BitmapFactory.decodeFileDescriptor(fd);
			Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
			int ori = mConfiguration.orientation; //获取屏幕方向
			if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
				Glide.with(this)
						.load(binder.getBitmap())
//                    .placeholder(playAlbumImg.getDrawable())
						.transition(DrawableTransitionOptions.withCrossFade())
						.error(new BitmapDrawable(bitmap))
						.into(playAlbumImg);
				Glide.with(this)
						.load(bitmap)
						.error(R.mipmap.cover_pic)
						.transition(DrawableTransitionOptions.withCrossFade())
						.into(albumImg);
			} else if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
				//竖屏


				Glide.with(this)
						.load(bitmap)
//                    .placeholder(playAlbumImg.getDrawable())
						.transition(DrawableTransitionOptions.withCrossFade())
						.transform(new BitmapTransform())
						.error(new BitmapDrawable(bitmap))
						.into(playAlbumImg);
				Glide.with(this)
						.load(binder.getBitmap())
						.error(R.mipmap.cover_pic)
						.transition(DrawableTransitionOptions.withCrossFade())
						.into(albumImg);
			}

		}
	}


	@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
	private void secondProgressInit() {
		animator = ValueAnimator.ofInt((int) progressBar.getProgress(), binder.getMediaplay().getCurrentPosition() + 1200);
		animator.setDuration(1200);
		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.addUpdateListener(animation -> {
			int p = (int) animation.getAnimatedValue();
			progressBar.setProgress(p);
			leftTime.setText(simpleDateFormat.format(p));
//                timerCircleView.setProgress(p);
		});
		animator.start();

	}

	private boolean isPlay = false;

	private static Bitmap getBitmap(Context context, int vectorDrawableId) {
		Bitmap bitmap = null;
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
			Drawable vectorDrawable = context.getDrawable(vectorDrawableId);
			bitmap = Bitmap.createBitmap(400,
					400, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			vectorDrawable.draw(canvas);
		} else {
			bitmap = BitmapFactory.decodeResource(context.getResources(), vectorDrawableId);
		}
		return bitmap;
	}


	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		handler.removeCallbacks(MainActivity.this);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

    }


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
		Log.d("TAG", "onCreate: " + (savedInstanceState == null));
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

		if (savedInstanceState == null) {
			fragmentManager.beginTransaction()
					.add(R.id.fragment_layout, FragmentMain.getInstance(), "fragmentMain")
					.add(R.id.fragment_layout, Setting.getInstance(), "setting").hide(Setting.getInstance()).commit();
		}

		colorUtil = new AlbumUtils();

////            if (fragmentMain==null){
////                fragmentMain = new FragmentMain();
////            }
////            if (setting==null){
////                setting=new Setting();
////            }
////            fragmentManager=getSupportFragmentManager();
//            Log.d("TAG", "onCreate: ");
//
//
////            if (getSupportFragmentManager().findFragmentByTag("fragmentMain")==null&&getSupportFragmentManager().findFragmentByTag("setting")==null){
//
//
////            }
//
//        }
//		if (!getSupportFragmentManager().findFragmentByTag("setting").isVisible()){
//			fragmentManager.beginTransaction().hide(Setting.getInstance())
//					.show(FragmentMain.getInstance())
//					.commit();
//		}
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		detachVol = preferences.getBoolean("detachVol", false);
		showBottomProgress = preferences.getBoolean("showProgress", false);
		reflushRate = Integer.parseInt(preferences.getString("level", "100"));
		toolbar = findViewById(R.id.toolbar);
		slidePanel = findViewById(R.id.mainSlideLayout);
		slidePanel.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
			@Override
			public void onPanelSlide(View panel, float slideOffset) {

			}

			@Override
			public void onPanelCollapsed(View panel) {
				isPlayLayoutVisible = false;
				serviceUpdate.stateResume();
			}

			@Override
			public void onPanelExpanded(View panel) {
				isPlayLayoutVisible = true;
				serviceUpdate.stateResume();
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
				binder.songChange();
			}
			if (binder.isPlaying()) {
				isPlay = true;
				showBottom(binder.getMediaData());
			}
            binder.stateResume();
			queueList.clear();
			queueList.addAll(binder.getPlayList());
			tempList.addAll(queueList);
			if (repeatType != -1) {
				binder.setRepeatType(repeatType);
			}
			binder.statePlayAndPauseChange();
			Log.d("TAG", "onServiceConnected: " + binder.getVol());
			volumeDialog.initSeekBar((int) (binder.getVol() * 1000), (int) (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 66.666f));
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	};


	@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
	private void progressInit() {
		Log.d("bottomLayout", "progressInit: " + bottomLayout.getProgress());
		if (!animiStart) {
			int dua = binder.getMediaplay().getCurrentPosition() * 500 / binder.getMediaplay().getDuration();
			Log.d("currdua", "progressInit: " + dua);
			animator = ValueAnimator.ofInt((int) bottomLayout.getProgress(), binder.getMediaplay().getCurrentPosition() + (1250 - dua));
			animator.setDuration(1250 - dua);
			animator.setInterpolator(new AccelerateDecelerateInterpolator());
			animator.addUpdateListener(animation -> {
				bottomLayout.setProgress((int) animation.getAnimatedValue());
			});
			animator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationStart(Animator animation, boolean isReverse) {
					animiStart = true;
				}

				@Override
				public void onAnimationEnd(Animator animation, boolean isReverse) {
					animiStart = false;
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
					.setPositiveButton("去授权", (dialog, which) -> initPermission()).setNegativeButton("退出", (dialog, which) -> finish()).show();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (binder != null) {
			unbindService(serviceConnection);
//			binder.removeListener("main");
		}
		handler.removeCallbacks(MainActivity.this);
		new Thread(() -> Glide.get(MainActivity.this).clearDiskCache()).start();
		Glide.get(this).clearMemory();

	}


	@Override
	protected void onResume() {
		super.onResume();
		if (binder != null && binder.getMediaplay().isPlaying()) {
			binder.stateResume();
			binder.statePlayAndPauseChange();
			binder.songChange();
		}
	}

	@Override
	public void update(int position) {
		if (binder.getState() == 0) {
			binder.clearState();
		}
		binder.play(position);
		isPlay = true;
	}

	private void showBottom(MediaData data) {

		if (data != null && slidePanel.isPanelHidden() && isPlay()) {
			slidePanel.showPanel();
//            bottomView.setVisibility(View.VISIBLE);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(bottomView, "translationY", 100, 0);
				objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				objectAnimator.setDuration(350);
				objectAnimator.start();
			}
		}

		if (data != null) {
			artist.setText(data.getArtist());
			songName.setText(data.getTitle());
		}
	}


	@OnClick({R.id.bottomLayout, R.id.bototmAlbumImg})
	public void bottomClick() {
		slidePanel.expandPanel();
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
		isPlay = true;
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
				if (fragmentManager.findFragmentByTag("setting").isVisible()) {
					fragmentManager.popBackStack();
				}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.setting:
//                Log.d("TAG", "settingIsVisible: "+getSupportFragmentManager().findFragmentByTag("setting").isVisible());
//                Log.d("TAG", "mainIsVisible: "+getSupportFragmentManager().findFragmentByTag("fragmentMain").isVisible());
				fragmentManager.beginTransaction()
						.show(Setting.getInstance())
						.hide(FragmentMain.getInstance())
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
//        System.out.println(fragment.toString());
		if (fragment instanceof FragmentIndex) {
			updateIndex = (ActivityToFragment.UpdateIndex) fragment;
		} else if (fragment instanceof FragmentOther) {
			updateAlbum = (ActivityToFragment.UpdateAlbum) fragment;
		} else if (fragment instanceof FragmentSinger) {
			updateArtist = (ActivityToFragment.UpdateArtist) fragment;
		}
	}


	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		m = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		if (detachVol && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			if (binder != null) {
				if (volumeDialog.idx() == 0) {
					binder.volDOWN();
					volumeDialog.show(0);
					volumeDialog.setSeekPro((int) (binder.getVol() * 1000), volumeDialog.idx());
				} else if (volumeDialog.idx() == 1) {
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, m - 1, AudioManager.FLAG_PLAY_SOUND);
					volumeDialog.show(0);
					volumeDialog.setSeekPro((int) ((m - 1) * 66.666f), volumeDialog.idx());
				}

			}
			return true;
		} else if (detachVol && keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			if (binder != null) {
				if (volumeDialog.idx() == 0&&!volumeDialog.getSeekIsMax()) {
					binder.volUP();
					volumeDialog.show(0);
					volumeDialog.setSeekPro((int) (binder.getVol() * 1000), volumeDialog.idx());
				} else  {
					Log.d("TAGM", "onKeyUp: " + m);
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, m + 1, AudioManager.FLAG_PLAY_SOUND);
					volumeDialog.show(0);
					volumeDialog.setSeekPro(((int) ((m + 1) * 66.666f)), volumeDialog.idx());
				}
			}
			return true;
		} else {
			return super.onKeyUp(keyCode, event);
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
//        m=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		// TODO Auto-generated method stub
		if (detachVol && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			//什么都不做
			return true;
		} else if (detachVol && keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			//什么都不做
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}


	@Override
	public void onBackPressed() {

		if (secondSlidePanel.isPanelExpanded()) {
			secondSlidePanel.collapsePanel();
		} else if (slidePanel.isPanelExpanded()) {
			slidePanel.collapsePanel();
		} else if (fragmentManager.findFragmentByTag("setting").isVisible()) {
			super.onBackPressed();
			if (binder != null) {
				showBottom(binder.getMediaData());
			}
			showBottomProgress = preferences.getBoolean("showProgress", false);
			show = preferences.getBoolean("showCircleView", false);
			showBlru = preferences.getBoolean("showBlru", false);
//            timerCircleView.setVisibility(show?View.VISIBLE:View.GONE);
//            if (!show){
//                albumCard.setRadius(DensityUtil.dip2px(this,2));
//            }else{
//                albumCard.setRadius(DensityUtil.dip2px(this,100));
//
//            }
			if (binder != null && binder.getMediaplay().isPlaying()) {
				serviceUpdate.updateSongInfo();
			}
		} else {
			super.onBackPressed();
			if (binder != null) {
				showBottom(binder.getMediaData());
			}
			showBottomProgress = preferences.getBoolean("showProgress", false);
			show = preferences.getBoolean("showCircleView", false);
			showBlru = preferences.getBoolean("showBlru", false);
//            timerCircleView.setVisibility(show?View.VISIBLE:View.GONE);
//            if (!show){
//                albumCard.setRadius(DensityUtil.dip2px(this,2));
//            }else{
//                albumCard.setRadius(DensityUtil.dip2px(this,100));
//
//            }
			if (binder != null && binder.getMediaplay().isPlaying()) {
				serviceUpdate.updateSongInfo();
			}
		}
		detachVol = preferences.getBoolean("detachVol", false);
		getSupportActionBar().show();

	}


}