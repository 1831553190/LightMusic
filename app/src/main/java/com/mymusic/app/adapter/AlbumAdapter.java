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

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.Holder> {
	Context context;
	List<AlbumData> albumDataList;
	private OnItemClickListener onItemClickListener;
	private Holder mHolder;
	
	public AlbumAdapter(Context context, List<AlbumData> albumDataList) {
		this.context = context;
		this.albumDataList = albumDataList;
	}
	public interface OnItemClickListener{
		void onItemClick(int position);
	}
	public View getImageView(){
		if (mHolder!=null){
			return mHolder.cover;
		}
		return null;
	}
	
	public void setOnItemClickListener(OnItemClickListener onItemClickListener){
		this.onItemClickListener=onItemClickListener;
	}
	
	@NonNull
	@Override
	public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view= LayoutInflater.from(context).inflate(R.layout.main_item,parent,false);
		mHolder=new Holder(view);
		return mHolder;
	}
	
	@Override
	public void onBindViewHolder(@NonNull Holder holder, final int position) {
		Glide.with(context)
				.load(MediaFactory.getAlbumArtGetDescriptorUri(context,albumDataList.get(position).getAlbumId()))
				.transition(DrawableTransitionOptions.withCrossFade())
				.into(holder.cover);
		holder.title.setText(albumDataList.get(position).getAlbumTitle());
		holder.artist.setText(albumDataList.get(position).getCount()+"首曲目");
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onItemClickListener.onItemClick(position);
			}
		});
		
	}
	
	@Override
	public int getItemCount() {
		return albumDataList.size();
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
