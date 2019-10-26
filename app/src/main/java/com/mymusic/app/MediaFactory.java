package com.mymusic.app;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.mymusic.app.bean.AlbumData;
import com.mymusic.app.bean.Artist;
import com.mymusic.app.bean.DataBean;
import com.mymusic.app.bean.MediaData;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class MediaFactory {
	private static int height;
	private static int width;
	private static Matrix matrix;
	
	public List<MediaData> getMediaList(Context context,String type,long id){
		List<MediaData> mediaDataList=new ArrayList<>();
		ContentResolver contentResolver =context.getContentResolver();
		String selection=null;
		if(type!=null&&type.equals(DataBean.TYPE_ALBUM)){
			selection = MediaStore.Audio.Media.ALBUM_ID+"="+id;
		}else if(type!=null&&type.equals(DataBean.TYPE_ARTIST)){
			selection = MediaStore.Audio.Media.ARTIST_ID+"="+id;
		}
		Cursor cursor=contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,selection ,null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		assert cursor != null;
		while(cursor.moveToNext()){
			MediaData mediaData=new MediaData();
			mediaData.setId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
			mediaData.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
			mediaData.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
			mediaData.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
			mediaData.setAlbumID(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
			mediaData.setAlbumKey(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_KEY)));
			mediaData.setDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
			mediaData.setFileSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));
			mediaData.setFilePath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
			mediaDataList.add(mediaData);
		}
		cursor.close();
		return mediaDataList;
	}
	
	
	
	
	public List<Artist> getArtistList(Context context){
		List<Artist> mediaDataList=new ArrayList<>();
		ContentResolver contentResolver =context.getContentResolver();
		Cursor cursor=contentResolver.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,null,null,null, MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);
		assert cursor != null;
		while(cursor.moveToNext()){
			Artist mediaData=new Artist();
			mediaData.setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Artists._ID)));
			mediaData.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.ArtistColumns.ARTIST)));
			mediaData.setArtistKey(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.ArtistColumns.ARTIST_KEY)));
			mediaData.setNumberOfAlbums(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS)));
			mediaData.setNumberOfTracks(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS)));
			mediaDataList.add(mediaData);
		}
		cursor.close();
		return mediaDataList;
	}
	
	
	public List<AlbumData> getAlbumList(Context context){
		List<AlbumData> albumDataList=new ArrayList<>();
		ContentResolver contentResolver =context.getContentResolver();
		Cursor cursor=contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,null,null,null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
		assert cursor != null;
		while(cursor.moveToNext()){
			AlbumData albumData=new AlbumData();
			albumData.setAlbumId(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Albums._ID)));
			albumData.setAlbumTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM)));
			albumData.setAlbumKey(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_KEY)));
			albumData.setAlbumFirstYear(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.FIRST_YEAR)));
			albumData.setAlbumArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST)));
			albumData.setAlbumArtCover(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)));
			albumData.setAlbumLastYear(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.LAST_YEAR)));
			albumData.setCount(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS)));
			albumDataList.add(albumData);
		}
		cursor.close();
		return albumDataList;
	}
	
	
	public Uri getAlbumArtGetDescriptor(Context context,long albumId){
		Uri uri = Uri.parse("content://media/external/audio/albumart");
		FileDescriptor fileDescriptor = null;
		Uri contentUris = null;
		if (albumId!=0) {
			 contentUris = ContentUris.withAppendedId(uri, albumId);
//			try {
//				ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(contentUris, "r");
//				if (parcelFileDescriptor != null) {
//					fileDescriptor = parcelFileDescriptor.getFileDescriptor();
//				}
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
		}
		return contentUris;
	}
	
	
	public static String getAlbumArtCover(Context context,long albumId){
		String albumCoverPath = null;
		Cursor cursor=context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,null, MediaStore.Audio.Albums._ID+"="+albumId,null,null);
		if (cursor!=null){
			cursor.moveToFirst();
			albumCoverPath=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
		}
		if (cursor!=null&&!cursor.isClosed())
			cursor.close();
		return albumCoverPath;
	}
	
	
	public static Bitmap getAlbumArtCoverPicture(final Context context, final long albumId){
		final Bitmap bitmap= BitmapFactory.decodeFile(getAlbumArtCover(context,albumId));
;
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (bitmap != null) {
					
					height = bitmap.getHeight();
					width = bitmap.getWidth();
					matrix = new Matrix();
					matrix.postScale((float)100/width,(float)100/height);
				}
			}
		}).start();
		return Bitmap.createBitmap(bitmap,0,0,width,height,matrix,true);
	}
}
