package com.mymusic.app;

import android.animation.Animator;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

public class Setting extends PreferenceFragmentCompat {

    static Setting setting;
    private Animator animator;
    Handler handler=new Handler(Looper.getMainLooper());

    public static Fragment getInstance(){
        if (setting ==null){
            synchronized (Setting.class){
                if (setting ==null){
                    setting =new Setting();
                }
            }
        }
        return setting;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setRetainInstance(true);
        addPreferencesFromResource(R.xml.setting);
        ListPreference listPreference=findPreference("themed");
        listPreference.setOnPreferenceChangeListener((preference, newValue) -> {

            View view= (View) getView().getParent().getParent().getParent();

            Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            ImageView view1=new ImageView(getContext());
            view1.setImageBitmap(bitmap);
            view.setVisibility(View.VISIBLE);
            animator= ViewAnimationUtils.createCircularReveal(view,500,500,0,2560);
            animator.setDuration(600);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    Configuration configuration=new Configuration();
                    configuration.uiMode=Configuration.UI_MODE_NIGHT_NO;
                    if (newValue.equals("night")){
                        configuration.uiMode=Configuration.UI_MODE_NIGHT_YES;
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }else if (newValue.equals("light")){
                        configuration.uiMode=Configuration.UI_MODE_NIGHT_NO;
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }else{
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    }
//                    getActivity().onConfigurationChanged(configuration);
//                    getActivity().setTheme(R.style.Theme_AppCompat_DayNight);
                    ((AppCompatActivity)requireActivity()).getDelegate().applyDayNight();
                    getActivity().recreate();
//                    themeSwitchImageView.setImageBitmap(bitmap);
//                    themeSwitchImageView.setVisibility(View.VISIBLE);
//                    handler.post(() -> {
//                        startActivity(intent);
//                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
//                    });
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            animator.start();


//            getParentFragmentManager().popBackStack();
            return true;
        });


    }

}
