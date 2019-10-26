package com.mymusic.app.bean;

public class Artist {
	
	private long id;
	private String artist;
	private String artistKey;
	private long numberOfAlbums;
	private long numberOfTracks;
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public void setArtist(String artist) {
		this.artist = artist;
	}
	
	public String getArtist() {
		return artist;
	}
	
	public void setArtistKey(String artistKey) {
		this.artistKey = artistKey;
	}
	
	public String getArtistKey() {
		return artistKey;
	}
	
	public void setNumberOfAlbums(long numberOfAlbums) {
		this.numberOfAlbums = numberOfAlbums;
	}
	
	public long getNumberOfAlbums() {
		return numberOfAlbums;
	}
	
	public void setNumberOfTracks(long numberOfTracks) {
		this.numberOfTracks = numberOfTracks;
	}
	
	public long getNumberOfTracks() {
		return numberOfTracks;
	}
}
