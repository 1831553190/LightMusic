package com.mymusic.app.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.SeekBar;

import androidx.appcompat.widget.AppCompatSeekBar;


@Deprecated
public class SmoothSeekBar extends AppCompatSeekBar implements AppCompatSeekBar.OnSeekBarChangeListener {

    private AppCompatSeekBar.OnSeekBarChangeListener onSeekBarChangeListener;
    private boolean needCallListener=true;
    private ValueAnimator animator;

    public SmoothSeekBar(Context context) {
        super(context);
        init(context);
    }

    public SmoothSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SmoothSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);

    }

    public void init(Context context){
        Context mContext=context;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser||needCallListener){
            if (onSeekBarChangeListener!=null){
                onSeekBarChangeListener.onProgressChanged(seekBar,progress,fromUser);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (onSeekBarChangeListener!=null){
            onSeekBarChangeListener.onStartTrackingTouch(seekBar);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (onSeekBarChangeListener!=null){
            onSeekBarChangeListener.onStopTrackingTouch(seekBar);
        }
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
        this.onSeekBarChangeListener = onSeekBarChangeListener;
        super.setOnSeekBarChangeListener(this);
    }

    @Override
    public synchronized void setProgress(final int progress) {
        final int currentProgress=getProgress();
        if (animator!=null){
            animator.cancel();
            animator.removeAllUpdateListeners();
            animator.removeAllListeners();
            animator=null;
            needCallListener=true;
        }
        animator=ValueAnimator.ofInt(currentProgress,progress);
        animator.setDuration(750);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int value= (int) animation.getAnimatedValue();
            if (value==progress){
                needCallListener=true;
            }else {
                needCallListener=false;
            }

            super.setProgress(value);

        });
        animator.start();
    }

}
