package com.mymusic.app.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.mymusic.app.MediaFactory;
import com.mymusic.app.R;
import com.mymusic.app.bean.MediaData;
import com.mymusic.app.fragment.FragmentIndex;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.Holder> implements Filterable {
	List<MediaData> songList,tempList;
	;
	SongAdapter.OnItemClickListener onItemClickListener;
	SongAdapter.OnItemLongClickListener onItemLongClickListener;
	Context context;
	SongAdapter.Holder holder;
	
	public SongAdapter(Context context, List<MediaData> songList,List<MediaData> tempList){
		this.songList=songList;
		this.context=context;
		this.tempList=tempList;
	}


	public void setTextColor(ColorStateList stateList){
		if (holder!=null){
			holder.song_title.setTextColor(stateList);
			holder.song_artist.setTextColor(stateList);
		}
	}


	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				if (constraint.length()==0){
					tempList.clear();
					tempList.addAll(songList);
				}else{
					tempList.clear();
					String conString=constraint.toString().toUpperCase();
					for (MediaData data:songList){
						if ((data.getTitle()!=null&&data.getTitle().toUpperCase().contains(conString))||
								(data.getArtist()!=null&&data.getArtist().toUpperCase().contains(conString))||
								(data.getAlbumKey()!=null&&data.getAlbumKey().toUpperCase().contains(conString))) {
							tempList.add(data);
						}
					}
				}

				FilterResults filterResults = new FilterResults();
				filterResults.values = tempList;
				return filterResults;
			}
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				notifyDataSetChanged();
			}
		};
	}

	public interface OnItemClickListener{
		void onItemClick(int position);
	}
	public interface OnItemLongClickListener{
		void onItemLongClick(View view,int position);
	}
	
	public void setOnItemClickListener(SongAdapter.OnItemClickListener onItemClickListener){
		this.onItemClickListener=onItemClickListener;
	}
	public void setOnItemLongClickListener(SongAdapter.OnItemLongClickListener onItemLongClickListener){
		this.onItemLongClickListener=onItemLongClickListener;
	}

	@NonNull
	@Override
	public SongAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view= LayoutInflater.from(context).inflate(R.layout.main_item,parent,false);
		holder=new Holder(view);
		return holder;
	}
	
	@Override
	public void onBindViewHolder(@NonNull SongAdapter.Holder holder, final int position) {
		holder.song_title.setText(tempList.get(position).getTitle());
		holder.song_artist.setText(tempList.get(position).getArtist());
		Glide.with(holder.itemView)
				.load(MediaFactory.getAlbumArtGetDescriptorUri(context,tempList.get(position).getAlbumID()))
				.apply(RequestOptions.bitmapTransform(new RoundedCorners(2)).override(200,200))
				.placeholder(R.mipmap.cover_pic)
				.transition(DrawableTransitionOptions.withCrossFade())
				.into(holder.imgAlbumCover);
		holder.itemView.setOnClickListener(v -> {
			for (int i=0;i<songList.size();i++){
				if (tempList.get(position).getId()==songList.get(i).getId()){
					onItemClickListener.onItemClick(i);
					return;
				}
			}
		});
		holder.itemView.setOnLongClickListener(v -> {
			if (onItemLongClickListener!=null){
				onItemLongClickListener.onItemLongClick(v,position);
			}
			return true;
		});
	}

	@Override
	public int getItemCount() {
		return tempList.size();
	}
	
	 class Holder extends RecyclerView.ViewHolder{
		public TextView song_title,song_artist;
		AppCompatImageView imgAlbumCover;
		public Holder(@NonNull View itemView) {
			super(itemView);
			song_title=itemView.findViewById(R.id.song_title);
			song_artist=itemView.findViewById(R.id.song_artist);
			imgAlbumCover=itemView.findViewById(R.id.item_album_cover);
		}
	}
}
//	public DrawableTypeRequest<Uri> loadFromMediaStore(Uri uri) {
//		return (DrawableTypeRequest<Uri>) fromMediaStore().load(uri);
//	}
//
//	public DrawableTypeRequest<Uri> fromMediaStore() {
//		ModelLoader<Uri, InputStream> genericStreamLoader = Glide.buildStreamModelLoader(Uri.class, context);
//		ModelLoader<Uri, InputStream> mediaStoreLoader = new MediaStoreStreamLoader(context, genericStreamLoader);
//		ModelLoader<Uri, ParcelFileDescriptor> fileDescriptorModelLoader =
//				Glide.buildFileDescriptorModelLoader(Uri.class, context);
//		 RequestManager.OptionsApplier optionsApplier = new RequestManager.OptionsApplier();
//		return optionsApplier.apply(new DrawableTypeRequest<Uri>(Uri.class, mediaStoreLoader,
//				fileDescriptorModelLoader, context, glide, requestTracker, lifecycle, optionsApplier));
//	}

//	public static Uri getAlbumArtUri(Context context,long id,long Abm){
//
//		if(Abm < 0){
//			return Uri.parse("content://media/external/audio/media/"
//					+ id + "/albumart");
//		} else {
//			return ContentUris.withAppendedId(albumArtUri, Abm);
//		}
//	}