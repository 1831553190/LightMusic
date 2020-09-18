package com.mymusic.app.bean;

import android.net.Uri;

public class MediaData {
	
	int id;
	String title;
	long duration;
	String album;
	long albumID;
	String artist;
	long fileSize;
	String filePath;
	String albumArtCover;
	String albumKey;
	Uri dataUri;


	public Uri getDataUri() {
		return dataUri;
	}

	public void setDataUri(Uri dataUri) {
		this.dataUri = dataUri;
	}

	
	public String getAlbumKey() {
		return albumKey;
	}
	
	public void setAlbumKey(String albumKey) {
		this.albumKey = albumKey;
	}
	
	
	public String getAlbumArtCover() {
		return albumArtCover;
	}
	
	public void setAlbumArtCover(String albumArtCover) {
		this.albumArtCover = albumArtCover;
	}
	
	
	public String getFilePath() {
		return filePath;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	
	

	
	
	public long getFileSize() {
		return fileSize;
	}
	
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public long getDuration() {
		return duration;
	}
	
	public void setDuration(long duration) {
		this.duration = duration;
	}
	
	public String getAlbum() {
		return album;
	}
	
	public void setAlbum(String album) {
		this.album = album;
	}
	
	public long getAlbumID() {
		return albumID;
	}
	
	public void setAlbumID(long albumID) {
		this.albumID = albumID;
	}
	

	
	
	public String getArtist() {
		return artist;
	}
	
	public void setArtist(String artist) {
		this.artist = artist;
	}
	
}
