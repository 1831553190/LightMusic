package com.mymusic.app.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isPressed()){
            canvas.drawCircle((float)super.mReachedRectF.right,(float) weigtHeight/2, (float) (dotDiameter*1.2),paint);
            canvas.drawCircle((float)super.mReachedRectF.right,(float) weigtHeight/2, dotDiameter*2,prePaint);
        }else{
            canvas.drawCircle((float)super.mReachedRectF.right,(float) weigtHeight/2,dotDiameter,paint);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                stat=1;
                if (onSeekChangeListener!=null){
                    onSeekChangeListener.onStartTrack(this);
                }
                setPressed(true);
                setProgress(len(event.getX())/weigtWidth*getMax());
                break;

            case MotionEvent.ACTION_MOVE:
                if (onSeekChangeListener!=null) {
                    onSeekChangeListener.onProgressChange(this);
                }
                setProgress(len(event.getX())/weigtWidth*getMax());
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setPressed(false);
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
