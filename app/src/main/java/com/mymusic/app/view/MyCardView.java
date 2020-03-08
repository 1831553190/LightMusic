package com.mymusic.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.mymusic.app.DensityUtil;

public class MyCardView extends CardView {
    private static final String TAG = "cardView";
    int cardWidth=-1;
    int cardHeight=-1;
    int defaultLegth= DensityUtil.dip2px(getContext(),200);
    static int len;


    public MyCardView(@NonNull Context context) {
        super(context);
        init();
    }

    public MyCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        if (cardHeight==-1&&cardWidth==-1){
            cardHeight=defaultLegth;
            cardWidth=defaultLegth;
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width=MeasureSpec.getSize(widthMeasureSpec);
        int height=MeasureSpec.getSize(heightMeasureSpec);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        if ((bottom-top)>(right-left)){
            len=(bottom-top)-(right-left);
            Log.d(TAG, "onLayout: height"+len);
            this.getLayoutParams().height=right-left;
        }else if ((bottom-top)<(right-left)){
            len=(right-left)-(bottom-top);
            Log.d(TAG, "onLayout: width"+len);
            this.getLayoutParams().width=bottom-top;
        }
        super.onLayout(changed, left, top, right, bottom);
    }
}
