package com.mymusic.app.inter;

import com.mymusic.app.bean.AlbumData;
import com.mymusic.app.bean.Artist;
import com.mymusic.app.bean.MediaData;

import java.util.List;

public interface ActivityToFragment {
    interface UpdateIndex{
        void updateData(List<MediaData> dataList);
        void serverConnect();
    }
    interface UpdateAlbum{
        void updateData(List<AlbumData> dataList);
        void serverConnect();
    }
    interface UpdateArtist{
        void updateData(List<Artist> dataList);
        void serverConnect();
    }
}
