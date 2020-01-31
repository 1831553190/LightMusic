package com.mymusic.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.mymusic.app.bean.AlbumData;
import com.mymusic.app.bean.Artist;
import com.mymusic.app.bean.DataBean;
import com.mymusic.app.bean.MediaData;
import com.mymusic.app.inter.ServiceUpdate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MediaService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
	
	Binder binder=new Binder();
	MediaPlayer mediaPlayer;
	private List<MediaData> mediaDataList,playList,queryList;
	private List<AlbumData> albumDataList;
	private List<Artist> artistList;
	
	NotificationManager notificationManager;
	Notification notification;
	private NotificationChannel notificationChannel;
	Notification.MediaStyle mediaStyle=null;
	int currentPosition=-1;
	MediaMetadata.Builder mediaMetadata;
	MediaSession mediaSession;
	PlaybackState playbackState;
	Bitmap bitmap1=null;
	Notification.Builder notifiBuider;
	Notification.Action notificationAction;
	private int state=0;
	Notification.Action action;
	boolean isCreate=true;
	
	

	private SharedPreferences sharePreference;
	
	
	public class Binder extends android.os.Binder{

		HashMap<String,ServiceUpdate> listeners=new HashMap<>();


		public void updateListener(){
			for (ServiceUpdate service:listeners.values()){
				service.updateSong();
			}
		}

		public MediaPlayer getMediaplay(){
			return mediaPlayer;
		}

		public void removeListener(String lisName){
			listeners.remove(lisName);
		}


		public void addListener(String lisName,ServiceUpdate serviceUpdate){
			listeners.put(lisName,serviceUpdate);
		}
		
		public List<MediaData> getMediaList(){
			return mediaDataList;
		}
		public List<MediaData> getMediaDataList(){
			return mediaDataList;
		}
		public List<AlbumData> getAlbumList(){
			return albumDataList;
		}
		public List<Artist> getArtistList(){
			return artistList;
		}
		public MediaData getMediaData(){
			if (!mediaDataList.isEmpty()){
				return mediaDataList.get(currentPosition);
			}
			return null;
		}

		public void play(int position){
//		currentPosition=position;
				if (currentPosition==-1)
					currentPosition=0;
				try {
					mediaPlayer.reset();
					mediaPlayer.setDataSource(playList.get(position).getFilePath());
					mediaPlayer.prepare();
					mediaPlayer.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					startForegroundService(new Intent(MediaService.this,MediaService.class));
				}
				startForeground(1,notifyMedia(position));
				currentPosition=position;

			}

			private void stop(){
				stopForeground(false);
				notificationManager.notify(1,notifyMedia(currentPosition));
			}

		
		public int getState(){
			return state;
		}
		public void clearState(){
			playList.clear();
			playList.addAll(mediaDataList);
			state=0;
		}

		public boolean isPlaying(){
			return mediaPlayer.isPlaying();
		}
		
		public void setSongPlayList(List<MediaData> list) {
			playList.clear();
			playList.addAll(list);
			state=1;
		}


		public void playAndPause(){
			if (mediaPlayer.isPlaying()){
				mediaPlayer.pause();
				stopForeground(false);
				notificationManager.notify(1,notifyMedia(currentPosition));
				sharePreference.edit().putInt("position",currentPosition).apply();
			}else{
				mediaPlayer.start();
				startForeground(1,notifyMedia(currentPosition));
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					startForegroundService(new Intent(MediaService.this,MediaService.class));
				}
			}
			binder.updateListener();
		}

		public void playPrevious(){
			if (currentPosition<=0){
				currentPosition=playList.size()-1;
			}else{
				--currentPosition;
			}
			binder.play(currentPosition);
		}
		public void playNext(){
			if(currentPosition>=playList.size()-1){
				currentPosition=0;

			}else{
				++currentPosition;
			}
			binder.play(currentPosition);

		}

		private void play() {
			if (currentPosition==-1){
				currentPosition=0;
			}
			try{
				mediaPlayer.reset();
				mediaPlayer.setDataSource(playList.get(currentPosition).getFilePath());
				mediaPlayer.prepare();
				mediaPlayer.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				startForegroundService(new Intent(MediaService.this,MediaService.class));
			}
		}


	}
	
	
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			update();
		}
		binder.updateListener();
	}
	
	@Override
	public void onCompletion(MediaPlayer mp) {
		if (++currentPosition<=playList.size()-1){
			binder.play(currentPosition);
		}else{
			currentPosition=0;
			binder.stop();
			binder.updateListener();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
	}
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	private void update(){
		mediaMetadata.putString(MediaMetadata.METADATA_KEY_TITLE,playList.get(currentPosition).getTitle());
		mediaMetadata.putString(MediaMetadata.METADATA_KEY_ARTIST,playList.get(currentPosition).getArtist());
		mediaMetadata.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART,bitmap1);
		mediaSession.setMetadata(mediaMetadata.build());
		
	}
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent!=null){
			String command=intent.getAction();
			if (command!=null){
//			prepare();
				switch (command){
					case DataBean.ACTION_PLAYANDPASE:
						binder.playAndPause();
						break;
					case DataBean.ACTION_PLAYPRIVAOUS:
						binder.playPrevious();
						break;
					case DataBean.ACTION_PLAYNEXT:
						binder.playNext();
						break;
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}
	

	
	@Override
	public void onCreate() {
		super.onCreate();
		mediaPlayer=new MediaPlayer();
		mediaDataList= MediaFactory.getMediaList(this, null,0);
		albumDataList= MediaFactory.getAlbumList(this);
		artistList= MediaFactory.getArtistList(this);
		playList=new ArrayList<>();
		queryList=new ArrayList<>();
		playList.addAll(mediaDataList);
		notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setOnCompletionListener(this);
		init();
		IntentFilter intentFilter=new IntentFilter();
		intentFilter.addAction(DataBean.ACTION_PLAYANDPASE);
		intentFilter.addAction(DataBean.ACTION_PLAYPRIVAOUS);
		intentFilter.addAction(DataBean.ACTION_PLAYNEXT);
		registerReceiver(new ActionOps(),intentFilter);
		sharePreference= PreferenceManager.getDefaultSharedPreferences(this);
		currentPosition=sharePreference.getInt("position",0);
		Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		ComponentName mComponentName = new ComponentName(this.getPackageName(), MediaBtnEvent.class.getName());
		mediaButtonIntent.setComponent(mComponentName);
		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.registerMediaButtonEventReceiver(mComponentName);
	}
	
	
	private void init(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mediaStyle=new Notification.MediaStyle();
			mediaSession=new MediaSession(this,"music");
			mediaStyle.setMediaSession(mediaSession.getSessionToken());
			mediaMetadata=new MediaMetadata.Builder();
			mediaSession.setMetadata(mediaMetadata.build());
			playbackState=new PlaybackState.Builder().setState(PlaybackState.STATE_PLAYING,currentPosition,System.currentTimeMillis()).setActions(PlaybackState.ACTION_PLAY|PlaybackState.ACTION_PAUSE|PlaybackState.ACTION_SKIP_TO_NEXT|PlaybackState.ACTION_SKIP_TO_PREVIOUS).build();
			mediaSession.setPlaybackState(playbackState);
			mediaSession.setCallback(playCallBack);

		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			notificationChannel=new NotificationChannel("listening_music","music", NotificationManager.IMPORTANCE_LOW);
			notificationManager.createNotificationChannel(notificationChannel);
		}
	
	
}


	@RequiresApi (Build.VERSION_CODES.LOLLIPOP)
	private MediaSession.Callback playCallBack=new MediaSession.Callback() {
		@Override
		public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {
			return new MediaBtnEvent().handleEvent(MediaService.this,mediaButtonIntent);
		}
		
		@Override
		public void onPrepare() {
			super.onPrepare();
		}
		
		@Override
		public void onPlay() {
			super.onPlay();
		}
		
		@Override
		public void onPause() {
			super.onPause();
		}
		
		@Override
		public void onSkipToNext() {
			super.onSkipToNext();
		}
		
		@Override
		public void onStop() {
			super.onStop();
		}
		
		@Override
		public void onSeekTo(long pos) {
			super.onSeekTo(pos);
		}
	};
	


	


	
	
	public static final Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap.createBitmap( drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
				drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}
	
	public Notification notifyMedia(int position){
		int icon=mediaPlayer.isPlaying()?R.drawable.ic_pause_black_24dp:R.drawable.ic_play_arrow_black_24dp;
		action = new Notification.Action(R.drawable.ic_pause_black_24dp,"play",null);
		Intent intent=new Intent(this,MainActivity.class);
		String imgPath=MediaFactory.getAlbumArtCover(this,playList.get(position).getAlbumID());
		if (imgPath!=null){
			bitmap1=BitmapFactory.decodeFile(imgPath);
		}else{
			bitmap1=null;
		}
		PendingIntent pendingIntent= (PendingIntent) PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
		
		notifiBuider = new Notification.Builder(this)
				.setSmallIcon(R.drawable.ic_audiotrack_black_24dp)
				.setLargeIcon(bitmap1)
				.addAction(R.drawable.ic_skip_previous_black_24dp,"previous",PendingIntent.getBroadcast(this,0,new Intent(DataBean.ACTION_PLAYPRIVAOUS),0))
				.addAction(icon,"play",PendingIntent.getBroadcast(this,0,new Intent(DataBean.ACTION_PLAYANDPASE),0))
				.addAction(R.drawable.ic_skip_next_black_24dp,"next",PendingIntent.getBroadcast(this,0,new Intent(DataBean.ACTION_PLAYNEXT),0))
				.setContentTitle(playList.get(position).getTitle())
				.setContentText(playList.get(position).getArtist())
				.setAutoCancel(true)
				.setContentIntent(pendingIntent)
				.setPriority(Notification.PRIORITY_LOW);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				notifiBuider.setCategory(Notification.CATEGORY_TRANSPORT)
						.setStyle(mediaStyle);
		}
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			notifiBuider.setChannelId("listening_music");
		}
		notification=notifiBuider.build();
		return notification;
	}

	class ActionOps extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			if (action!=null){
				switch (action){
					case DataBean.ACTION_PLAYANDPASE:
						binder.playAndPause();
						break;
					case DataBean.ACTION_PLAYPRIVAOUS:
						binder.playPrevious();
						break;
					case DataBean.ACTION_PLAYNEXT:
						binder.playNext();
						break;
				}
				
			}
		}
	}
	
	
}
