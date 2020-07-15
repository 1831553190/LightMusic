package com.mymusic.app.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.mymusic.app.R;

public class BottomLayout extends ConstraintLayout {

    Paint paint;
    int height;
    private float progress;
    private int max;
    private int color;
    RectF rectF;
    private Paint paintBackground;


    public BottomLayout(Context context) {
        super(context);
    }

    public BottomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        height=getMeasuredHeight();
        paint.setColor(R.attr.colorAccent);
        rectF=new RectF();

//
//        paintBackground=new Paint(Paint.ANTI_ALIAS_FLAG);
//        paintBackground.setAntiAlias(true);
//        paintBackground.setStrokeCap(Paint.Cap.SQUARE);
//        paintBackground.setColor(Color.WHITE);
////        setBackgroundColor(Color.WHITE);
    }

    public void setProgress(float progress){
        this.progress=progress;
        postInvalidate(getLeft(),getTop(),getRight(),getBottom());
    }
    public float getProgress(){
        return progress;
    }
    public void setMax(int max){
        this.max=max;
    }
    public int getMax(){
        return max;
    }

    public void setColor(int color){
        if (this.color==color){
            return;
        }
        ValueAnimator colorAnim = new ValueAnimator();
        colorAnim.setIntValues(this.color,color);
        colorAnim.setDuration(500);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.addUpdateListener(animation -> {
            paint.setColor((Integer) animation.getAnimatedValue());
        });
        colorAnim.start();
        this.color=color;
    }

//    void animTestOut(){
//        ValueAnimator valueAnimator=ValueAnimator.ofFloat(dotDiameter,0);
//        valueAnimator.setInterpolator(new DecelerateInterpolator());
//        valueAnimator.setDuration(150);
//        valueAnimator.addUpdateListener(animation -> {
//            wtest= (float) animation.getAnimatedValue();
//            invalidate();
////            Log.d("progresswtese", "animTest: "+wtest);
//
//        });
//        valueAnimator.start();
//    }




    @Override
    public boolean isFocused() {
        return true;
    }

    @Override
    public boolean isClickable() {
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.drawRect(getLeft(),getTop(),getRight(),getBottom(),paintBackground);
        rectF.left=getLeft();
        rectF.top=getTop();
        rectF.right=((float) progress/max)*getMeasuredWidth();
        rectF.bottom=getBottom();
        canvas.drawRoundRect(rectF,2,2,paint);

    }
}
