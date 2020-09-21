package com.mymusic.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import com.mymusic.app.DensityUtil;


@Deprecated
public class MyViewGroup extends ViewGroup {
    static int viewHeight;
    private int mStartY;
    private int mEnd;
    private Scroller scroller;
    private int mLastY;
    private int childCount;
    private int realChildCount;


    public MyViewGroup(Context context) {
        this(context, null);
    }

    public MyViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);
    }

    public MyViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        viewHeight =wm.getDefaultDisplay().getHeight();
//
        Log.d("screenHeight", ": "+ viewHeight);
        scroller = new Scroller(context,new DecelerateInterpolator());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        realChildCount = 0;
        childCount = getChildCount();

        MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();

        lp.height = viewHeight;
        setLayoutParams(lp);
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() != View.GONE) {
                realChildCount++;
//                if (i==0){
//                    childView.layout(l, i * viewHeight, r, (i + 1) * viewHeight);
//                }else{
                    childView.layout(l, +i * viewHeight- DensityUtil.dip2px(getContext(),66), r, (i + 1) * viewHeight);
//                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewHeight=MeasureSpec.getSize(heightMeasureSpec);
        Log.d("measure", "onMeasure: "+MeasureSpec.getSize(heightMeasureSpec));
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
        }
    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        if(scroller.computeScrollOffset()){
            scrollTo(0,scroller.getCurrY());
            postInvalidate();
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = y;
                mStartY = getScrollY();

                break;
            case MotionEvent.ACTION_MOVE:
                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                }


                int dY = mLastY - y;
                if (getScrollY() < 0) {
                    dY /= 3;
                }else if (getScrollY() > viewHeight * realChildCount - viewHeight) {
                    dY = 0;
                }
                scrollBy(0, dY);
                mLastY = y;

                break;
            case MotionEvent.ACTION_UP:
                mEnd = getScrollY();
                int dScrollY = mEnd - mStartY;
                if (dScrollY > 0) {   //向上滚动的情况
                    if (dScrollY < viewHeight / 8 || getScrollY() < 0) {
                        scroller.startScroll(0, getScrollY(), 0, -dScrollY);
                    } else {
                        scroller.startScroll(0, getScrollY(), 0, viewHeight - dScrollY);
                    }
                } else {        //向下滚动的情况
                    if (-dScrollY < viewHeight / 8) {
                        scroller.startScroll(0, getScrollY(), 0, -dScrollY);
                    } else {
                        scroller.startScroll(0, getScrollY(), 0, -viewHeight - dScrollY);
                    }
                }

                break;
        }

        postInvalidate();
        return true;
    }
}
