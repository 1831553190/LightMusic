package com.mymusic.app.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.mymusic.app.R;

public class MySmoothSeekBar extends MyProgressBar {


    Paint paint,prePaint;
    OnSeekChangeListener onSeekChangeListener;
    int weigtWidth;
    int defaultLength;
    float dotDiameter;
    int weigtHeight;
    static float startX=0, seeklong=0;
    static float progress;
    float va;
    int stat=0;
    static float wtest=0;
    static float animIn;
    static float intx=0;
    ValueAnimator vl;



    public MySmoothSeekBar(Context context) {
        super(context);
    }

    public MySmoothSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        initPaint(context,attrs);
    }

    private void init() {
        defaultLength=100;
        dotDiameter = getResources().getDimension(R.dimen.seekbar_dot_size);
    }
    private void initPaint(Context context, AttributeSet attrs){
//        TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.UpdataAPPProgressBar);
//        typedArray.recycle();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿
        paint.setAntiAlias(true); //防抖动
        paint.setColor(getResources().getColor(R.color.colorAccent));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(dotDiameter);
        prePaint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿
        prePaint.setAntiAlias(true); //防抖动
        prePaint.setColor(getResources().getColor(R.color.a66));
        prePaint.setStrokeCap(Paint.Cap.ROUND);
        prePaint.setStrokeWidth(dotDiameter);
        animIn=dotDiameter;
    }

    public void setOnSeekChangeListener(OnSeekChangeListener onSeekChangeListener){
        this.onSeekChangeListener=onSeekChangeListener;
    }

    public interface OnSeekChangeListener{
        void onStartTrack(MySmoothSeekBar seekBar);
        void onProgressChange(MySmoothSeekBar seekBar);
        void onStopTrack(MySmoothSeekBar seekBar);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode=MeasureSpec.getMode(widthMeasureSpec);
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);
        weigtHeight=MeasureSpec.getSize(heightMeasureSpec);
        weigtWidth = MeasureSpec.getSize(widthMeasureSpec)-getPaddingLeft()-getPaddingRight();
        if (weigtWidth==0){
            weigtWidth=defaultLength;
        }
        if (weigtHeight==0){
            weigtHeight=10;
        }
    }

    @Override
    public synchronized void setProgress(float progress) {
        super.setProgress(progress);
        MySmoothSeekBar.progress =progress;
    }

    @Override
    public void setProgressColor(int color){
        paint.setColor(color);
        super.setProgressColor(color);
    }

    @Override
    public float getProgress() {
        return progress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isPressed()){
            canvas.drawCircle((float)super.mReachedRectF.right,(float) weigtHeight/2, animIn,paint);
            canvas.drawCircle((float)super.mReachedRectF.right,(float) weigtHeight/2, wtest*2,prePaint);
        }else{
            canvas.drawCircle((float)super.mReachedRectF.right,(float) weigtHeight/2,animIn,paint);
            canvas.drawCircle((float)super.mReachedRectF.right,(float) weigtHeight/2, wtest,prePaint);
        }
    }



    void animTest(){
        ValueAnimator valueAnimator=ValueAnimator.ofFloat(0,dotDiameter);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(animation -> {
            wtest= (float) animation.getAnimatedValue();
            invalidate();
//            Log.d("progresswtese", "animTest: "+wtest);

        });
        valueAnimator.start();
    }
    void animIN(){
        ValueAnimator valueAnimator=ValueAnimator.ofFloat(dotDiameter,dotDiameter*1.2F);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(animation -> {
            animIn= (float) animation.getAnimatedValue();
            invalidate();
//            Log.d("progresswtese", "animTest: "+wtest);

        });
        valueAnimator.start();
    }
    void animTestOut(){
        ValueAnimator valueAnimator=ValueAnimator.ofFloat(dotDiameter,0);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(150);
        valueAnimator.addUpdateListener(animation -> {
            wtest= (float) animation.getAnimatedValue();
            invalidate();
//            Log.d("progresswtese", "animTest: "+wtest);
        });
        valueAnimator.start();
    }
    void animOut(){
        ValueAnimator valueAnimator=ValueAnimator.ofFloat(dotDiameter*1.2F,dotDiameter);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(150);
        valueAnimator.addUpdateListener(animation -> {
            animIn= (float) animation.getAnimatedValue();
            invalidate();
//            Log.d("progresswtese", "animTest: "+wtest);

        });
        valueAnimator.start();
    }



    private void animDown(float p){
        vl = ValueAnimator.ofFloat(progress,p+600f);
        vl.setInterpolator(new DecelerateInterpolator());
        vl.setDuration(650);
        vl.addUpdateListener(animation -> {
            progress=(float) animation.getAnimatedValue();
            setProgress(progress);
            postInvalidateOnAnimation();
        });
        vl.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                intx=event.getX();
                stat=1;
                if (onSeekChangeListener!=null){
                    onSeekChangeListener.onStartTrack(this);
                    animDown(len(event.getX())/weigtWidth*getMax());
                }
                setPressed(true);
                animTest();
                animIN();

//                setProgress();
                break;

            case MotionEvent.ACTION_MOVE:
                if (onSeekChangeListener!=null) {
                    onSeekChangeListener.onProgressChange(this);
                }
                if (Math.abs(event.getX()-intx)>5){
                    vl.cancel();
                }
//                setProgress(progress+(event.getX()-intx)/weigtWidth*getMax());
                setProgress(len(event.getX())/weigtWidth*getMax());
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setPressed(false);
                animOut();
                animTestOut();
                setProgress(len(event.getX())/weigtWidth*getMax());
                if (onSeekChangeListener!=null) {
                    onSeekChangeListener.onStopTrack(this);
                }
                break;
        }
        return true;
    }

    private float len(float len) {
        if (len<0){
            return 0;
        }else if (len>weigtWidth){
            return weigtWidth;
        }else {
            return len;
        }
    }

}
