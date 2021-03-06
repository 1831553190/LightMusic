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
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.mymusic.app.bean.AlbumData;
import com.mymusic.app.bean.Artist;
import com.mymusic.app.bean.DataBean;
import com.mymusic.app.bean.MediaData;
import com.mymusic.app.inter.ServiceUpdate;

import java.io.ByteArrayInputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
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
	private final static int MEDIA_ID=7;

	Bitmap iBitmap;
	ActionOps actionOps;

	float vol=-1f;


	private SharedPreferences sharePreference;
	
	
	public class Binder extends android.os.Binder{

		HashMap<String,ServiceUpdate> listeners=new HashMap<>();
		private int repeatType;


		public void songChange(){
			for (ServiceUpdate service:listeners.values()){
				service.updateSongInfo();
			}
		}

		public void stateResume(){
			for (ServiceUpdate service:listeners.values()){
				service.stateResume();
			}
		}

		public void pause(){
			if (mediaPlayer!=null&&mediaPlayer.isPlaying()){
				mediaPlayer.pause();
				stopForeground(false);
				notificationManager.notify(MEDIA_ID,notifyMedia(currentPosition));
				sharePreference.edit().putInt("position",currentPosition).apply();
				statePlayAndPauseChange();
			}
		}

		public void statePlayAndPauseChange(){
			for (ServiceUpdate service:listeners.values()){
				service.statePlayAndPauseChange();
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
		public List<MediaData> getPlayList(){
			return playList;
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
			if (!playList.isEmpty()){
				return playList.get(currentPosition);
			}
			return null;
		}

		public void playList(List<MediaData> dataList,int postion){
			if (dataList.equals(getPlayList())&&postion==currentPosition){
				playAndPause();
			}else if (dataList.equals(playList)){
				play(postion);
			}else{
				currentPosition=postion;
				playList.clear();
				playList.addAll(dataList);
				play(currentPosition);
			}

		}



		public void play(int position){
//		currentPosition=position;
				if (currentPosition==-1)
					currentPosition=0;
				try {
					mediaPlayer.reset();
//					mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//					mediaPlayer.setDataSource(playList.get(position).getFilePath());
					mediaPlayer.setDataSource(getApplicationContext(),playList.get(position).getDataUri());

					mediaPlayer.prepare();
					mediaPlayer.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					startForegroundService(new Intent(MediaService.this,MediaService.class));
				}
				startForeground(MEDIA_ID,notifyMedia(position));
				currentPosition=position;

			}

			private void stop(){
				stopForeground(false);
				notificationManager.notify(MEDIA_ID,notifyMedia(currentPosition));
			}

		public void volUP(){
			if (vol<1.0f){
				vol+=0.1f;
			}
			if (vol>1.0f){
				vol=1.0f;
			}
			mediaPlayer.setVolume(vol,vol);
			sharePreference.edit().putFloat("vol",vol).commit();
		}
		public void volDOWN(){
			if (vol>0){
				vol-=0.1f;
			}
			if (vol<0.0f){
				vol=0.0f;
			}
			mediaPlayer.setVolume(vol,vol);
			sharePreference.edit().putFloat("vol",vol).commit();
		}
		public float getVol(){
			if (vol==-1f){
				vol=sharePreference.getFloat("vol",1.0f);
			}
			return vol;
		}
		public void setVol(int volume){
			vol=volume/100.0f;
			mediaPlayer.setVolume(vol,vol);
			sharePreference.edit().putFloat("vol",vol).commit();
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
				notificationManager.notify(MEDIA_ID,notifyMedia(currentPosition));
				sharePreference.edit().putInt("position",currentPosition).apply();
			}else{
				mediaPlayer.start();
				startForeground(MEDIA_ID,notifyMedia(currentPosition));
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					startForegroundService(new Intent(MediaService.this,MediaService.class));
				}
			}
			binder.statePlayAndPauseChange();
		}

//		public void play(String path){
//			MediaMetadataRetriever mediaMetadataRetriever=new MediaMetadataRetriever();
//			mediaMetadataRetriever.setDataSource(path);
//			playList.clear();
//			if (currentPosition==-1)
//				currentPosition=0;
//			try {
//				mediaPlayer.reset();
//				mediaPlayer.setDataSource(path);
//				mediaPlayer.prepare();
//				mediaPlayer.start();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//				startForegroundService(new Intent(MediaService.this,MediaService.class));
//			}
//			startForeground(MEDIA_ID,notifyMedia(0));
//		}

		public void play(Uri uri){
			playList.clear();
			try {
				MediaData data = new MediaData();
				MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
				if (uri.getScheme().equals("file")) {
					mediaMetadataRetriever.setDataSource(uri.getPath());
					data.setFilePath(uri.getPath());
				} else {
					mediaMetadataRetriever.setDataSource(MediaService.this, uri);
					playUri(uri);
				}
				currentPosition = 0;
				data.setDataUri(uri);
				String title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
				data.setTitle(title == null || title.isEmpty() ? "无标题" : title);
				data.setAlbum(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST));
				data.setArtist(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
				data.setDuration(Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
				playList.add(data);
				byte[] bitma = mediaMetadataRetriever.getEmbeddedPicture();
				if (bitma != null) {
					InputStream inputStream = new ByteArrayInputStream(bitma);
					iBitmap = BitmapFactory.decodeStream(inputStream);
				}
				if (uri.getScheme().equals("file")) {
					play(0);
				}
			}catch (Exception e){
				e.printStackTrace();
			}

//			if (currentPosition==-1)
//				currentPosition=0;
//			try {
//				mediaPlayer.reset();
//				mediaPlayer.setDataSource(MediaService.this,uri);
//				mediaPlayer.prepare();
//				mediaPlayer.start();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//				startForegroundService(new Intent(MediaService.this,MediaService.class));
//			}
//			startForeground(MEDIA_ID,notifyMedia(0));

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

		private void playUri(Uri uri) {
			if (currentPosition==-1){
				currentPosition=0;
			}
			try{
				mediaPlayer.reset();
				mediaPlayer.setDataSource(MediaService.this,uri);
				mediaPlayer.prepare();
				mediaPlayer.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				startForegroundService(new Intent(MediaService.this,MediaService.class));
			}
		}


		public Bitmap getBitmap() {
			return bitmap1;
		}

		public int getCurrentPosition() {
			return currentPosition;
		}

		public void setRepeatType(int repeatType) {
			this.repeatType=repeatType;
		}
		public int getRepeatType(){
			return repeatType;
		}
	}
	
	
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			update();
		}
		binder.songChange();
		binder.statePlayAndPauseChange();
	}
	
	@Override
	public void onCompletion(MediaPlayer mp) {
		switch (binder.getRepeatType()){
			case 0:
				if (++currentPosition<=playList.size()-1){
					binder.play(currentPosition);
				}else{
					currentPosition=0;
					binder.stop();
					binder.songChange();
				}
				break;
			case 1:
				binder.playNext();
				break;
			case 2:
				binder.play(currentPosition);
				break;
		}

	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopForeground(true);
		mediaPlayer.release();
		unregisterReceiver(actionOps);

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
		TelephonyManager tm= (TelephonyManager) getSystemService(TELEPHONY_SERVICE);//
		PhoneStateListener phoneStateListener=new PhoneStateListener(){
			@Override
			public void onCallStateChanged(int state, String phoneNumber) {
				super.onCallStateChanged(state, phoneNumber);
				switch (state){
					case TelephonyManager.CALL_STATE_OFFHOOK:
					case TelephonyManager.CALL_STATE_RINGING:
						binder.pause();

				}
			}
		};

		tm.listen(phoneStateListener,PhoneStateListener.LISTEN_CALL_STATE);
		mediaDataList= MediaFactory.getMediaList(this, null,0);
		albumDataList= MediaFactory.getAlbumList(this);
		artistList= MediaFactory.getArtistList(this);
		sharePreference= PreferenceManager.getDefaultSharedPreferences(this);
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
		intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		actionOps = new ActionOps();
		registerReceiver(actionOps,intentFilter);
		registerReceiver(headsetPlugReceiver,intentFilter);
		currentPosition=sharePreference.getInt("position",0);
		Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		ComponentName mComponentName = new ComponentName(this.getPackageName(), MediaBtnEvent.class.getName());
		mediaButtonIntent.setComponent(mComponentName);
		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


		//判断音乐焦点是否丢失
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//			AudioAttributes audioAttributes = new AudioAttributes.Builder()
//					.setUsage(AudioAttributes.USAGE_GAME)
//					.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//					.build();
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//				AudioFocusRequest audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
//						.setAudioAttributes(audioAttributes)
//						.setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() {
//							@Override
//							public void onAudioFocusChange(int focusChange) {
//								switch (focusChange){
//									case AudioManager.AUDIOFOCUS_GAIN:
//										if(!mediaPlayer.isPlaying()){
//											mediaPlayer.start();
//										}
//										break;
//									case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
//										break;
//									case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
//										break;
//									case AudioManager.AUDIOFOCUS_LOSS:
//										if(mediaPlayer.isPlaying()){
//											binder.pause();
//										}
//										break;
//									case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
//										if(mediaPlayer.isPlaying()){
//											binder.pause();
//										}
//										break;
//									case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
//										break;
//									default:
//								}
//							}
//						}).build();
//				mAudioManager.requestAudioFocus(audioFocusRequest);
//			}
//
//		}



		mAudioManager.registerMediaButtonEventReceiver(mComponentName);
	}

	AudioManager.OnAudioFocusChangeListener afChangeListener ;

	private void init(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mediaStyle=new Notification.MediaStyle();
			mediaSession=new MediaSession(this,"music");
			mediaStyle.setMediaSession(mediaSession.getSessionToken());
			mediaStyle.setShowActionsInCompactView(0,1,2);
			mediaMetadata=new MediaMetadata.Builder();
			mediaSession.setMetadata(mediaMetadata.build());
			playbackState=new PlaybackState.Builder().setState(PlaybackState.STATE_PLAYING,currentPosition,System.currentTimeMillis()).setActions(PlaybackState.ACTION_PLAY|PlaybackState.ACTION_PAUSE|PlaybackState.ACTION_SKIP_TO_NEXT|PlaybackState.ACTION_SKIP_TO_PREVIOUS).build();
			mediaSession.setPlaybackState(playbackState);
			playCallBack=new MediaSession.Callback() {
				@Override
				public void onPlay() {
					AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
					// Request audio focus for playback, this registers the afChangeListener
					int result = am.requestAudioFocus(afChangeListener,
							// Use the music stream.
							AudioManager.STREAM_MUSIC,
							// Request permanent focus.
							AudioManager.AUDIOFOCUS_GAIN);

					if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
						// Start the service
//						service.start();
						// Set the session active  (and update metadata and state)
						mediaSession.setActive(true);

					}
				}


				@Override
				public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {
					return new MediaBtnEvent().handleEvent(MediaService.this,mediaButtonIntent);
				}
			};
			mediaSession.setCallback(playCallBack);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			notificationChannel=new NotificationChannel("listening_music","music", NotificationManager.IMPORTANCE_LOW);
			notificationManager.createNotificationChannel(notificationChannel);
		}

}


	private MediaSession.Callback playCallBack;


	
	public Notification notifyMedia(int position){
		int icon=mediaPlayer.isPlaying()?R.drawable.ic_pause_black_24dp:R.drawable.ic_play_arrow_black_24dp;
//		action = new Notification.Action(R.drawable.ic_pause_black_24dp,"play",null);
		Intent intent=new Intent(this,MainActivity.class);
		FileDescriptor imgPath=MediaFactory.getAlbumArtGetDescriptor(this,playList.get(position).getAlbumID());
		if (imgPath!=null){
			bitmap1=BitmapFactory.decodeFileDescriptor(imgPath);
		}else if(iBitmap!=null){
			bitmap1=iBitmap;
		} else{
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
						.setStyle(mediaStyle);      //android6.0+的音乐通知
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

	private BroadcastReceiver headsetPlugReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
				if (mediaPlayer.isPlaying()){
					binder.pause();
				}else{
					binder.playAndPause();
				}
			}
		}

	};

}
