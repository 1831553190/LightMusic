package com.mymusic.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mymusic.app.MediaFactory;
import com.mymusic.app.R;
import com.mymusic.app.bean.AlbumData;
import com.mymusic.app.bean.Artist;

import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.Holder> {
	Context context;
	List<Artist> artistDataList;
	private OnItemClickListener onItemClickListener;
	MediaFactory mediaFactory;
	
	public ArtistAdapter(Context context, List<Artist> artistDataList) {
		this.context = context;
		this.artistDataList = artistDataList;
		mediaFactory=new MediaFactory();
	}
	public interface OnItemClickListener{
		void onItemClick(int position);
	}
	
	public void setOnItemClickListener(OnItemClickListener onItemClickListener){
		this.onItemClickListener=onItemClickListener;
	}
	
	@NonNull
	@Override
	public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view= LayoutInflater.from(context).inflate(R.layout.main_item,parent,false);
		return new Holder(view);
	}
	
	@Override
	public void onBindViewHolder(@NonNull Holder holder, final int position) {
//		Glide.with(context)
//				.load(mediaFactory.getAlbumArtGetDescriptor(context,artistDataList.get(position).getId()))
//				.transition(DrawableTransitionOptions.withCrossFade())
//				.into(holder.cover);
		holder.title.setText(artistDataList.get(position).getArtist());
		holder.artist.setText(artistDataList.get(position).getNumberOfTracks()+"首曲目");
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onItemClickListener.onItemClick(position);
			}
		});
	}
	
	@Override
	public int getItemCount() {
		return artistDataList.size();
	}
	
	class Holder extends RecyclerView.ViewHolder{
		TextView title,artist;
		AppCompatImageView cover;
		public Holder(@NonNull View itemView) {
			super(itemView);
			cover=itemView.findViewById(R.id.item_album_cover);
			title=itemView.findViewById(R.id.song_title);
			artist=itemView.findViewById(R.id.song_artist);
			
			
		}
	}
}
