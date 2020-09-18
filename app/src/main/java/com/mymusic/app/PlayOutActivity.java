package com.mymusic.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mymusic.app.bean.MediaData;
import com.mymusic.app.view.MySmoothSeekBar;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayOutActivity extends AppCompatActivity implements Runnable{

    String path=null;
    Uri uri=null;
    static int stat=0;
    MediaService.Binder binder;
    //    @BindView(R.id.sname)
    //    TextView sname;
    @BindView(R.id.psbar)
    MySmoothSeekBar seekBar;
    @BindView(R.id.pImg)
    ImageView img;
    @BindView(R.id.out_singer)
    TextView osinger;
    @BindView(R.id.out_title)
    TextView otitle;
    Intent i;

    Handler handler=new Handler();
    @Override
    public void run() {
        seekBar.setProgress(binder.getMediaplay().getCurrentPosition());
        handler.post(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.outplay);
        ButterKnife.bind(this);
        i = getIntent();
        if (i!=null){
            Intent intent=new Intent(this, MediaService.class);
            startService(intent);
            bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);
            if (Objects.equals(i.getScheme(), "file")){
                path =i.getData().getPath();
            }else{
                uri=i.getData();
            }
            seekBar.setOnSeekChangeListener(new MySmoothSeekBar.OnSeekChangeListener() {
                @Override
                public void onStartTrack(MySmoothSeekBar seekBar) {
                    handler.removeCallbacks(PlayOutActivity.this);
                }


                @Override
                public void onProgressChange(MySmoothSeekBar seekBar) {

                }

                @Override
                public void onStopTrack(MySmoothSeekBar seekBar) {
                    binder.getMediaplay().seekTo((int) seekBar.getProgress());
                    handler.post(PlayOutActivity.this);
                }
            });
        }
    }


    ServiceConnection serviceConnection= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            binder = (MediaService.Binder) service;
            binder.play(i.getData());
            MediaData data=binder.getMediaData();
            if (data==null){
                Toast.makeText(PlayOutActivity.this,"无效的音乐文件",Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            String title=data.getTitle()==null?"无标题":data.getTitle();
            otitle.setText(title);
            osinger.setText(data.getArtist());
//            setTitle(title);
            Glide.with(PlayOutActivity.this)
                    .load(binder.getBitmap())
                    .placeholder(R.drawable.cover_background)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(img);
//            sname.setText(title);
            seekBar.setMax(binder.getMediaplay().getDuration());
            handler.post(PlayOutActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
