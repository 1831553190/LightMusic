package com.mymusic.app;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.mymusic.app.fragment.FragmentIndex;
import com.mymusic.app.fragment.FragmentOther;
import com.mymusic.app.fragment.FragmentSinger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  {
    Toolbar toolbar;
    Fragment fragmentIndex,fragmentOther,fragmentSinger;
    String tabTitle[]={"歌曲","专辑","艺术家"};
    private static final int REQUEST_CODE=200;
    boolean permissionResult=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initPermission();
        }else{
            init();
        }
    }
    private void init(){
        ViewPager viewPager = findViewById(R.id.view_pager);
        TabLayout tabs = findViewById(R.id.tabs);
        final List<Fragment> fragmentList=new ArrayList<>();
        fragmentIndex=new FragmentIndex();
        fragmentOther=new FragmentOther();
        fragmentSinger=new FragmentSinger();
        fragmentList.add(fragmentIndex);
        fragmentList.add(fragmentOther);
        fragmentList.add(fragmentSinger);
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragmentList.get(position);
            }
    
            @Override
            public int getCount() {
                return fragmentList.size();
            }
    
            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return tabTitle[position];
            }
        });
        tabs.setupWithViewPager(viewPager);
    }
    
    @TargetApi(Build.VERSION_CODES.M)
    private void initPermission(){
        permissionResult=true;
        String[] permission = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        requestPermissions(permission,REQUEST_CODE);
        
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result:grantResults){
            System.out.println(result);
            if (result==-1){
                permissionResult=false;
            }
        }
        if (permissionResult){
            init();
        }else {
           new AlertDialog.Builder(this)
                   .setTitle("权限不足")
                   .setMessage("软件运行权限不足，无法正常工作请授权后重试")
                   .setCancelable(false)
                   .setPositiveButton("去授权", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           initPermission();
                       }
                   }).setNegativeButton("退出", new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                   finish();
               }
           }).show();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Glide.get(MainActivity.this).clearDiskCache();
            }
        }).start();
        Glide.get(this).clearMemory();
    }
}