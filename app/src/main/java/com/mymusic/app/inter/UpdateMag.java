package com.mymusic.app.inter;

import com.mymusic.app.MediaService;
import com.mymusic.app.bean.MediaData;

import java.util.List;

public interface UpdateMag {
    void update(int position);
    void getData(int index);
    void playList(List<MediaData> dataList ,int pos);
    MediaService.Binder getBider();
    void hideToolBar();
}
