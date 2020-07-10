package com.mymusic.app.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.IntDef;
import androidx.annotation.IntegerRes;

import com.mymusic.app.R;


public class MyImageView extends androidx.appcompat.widget.AppCompatImageView {


    int screenOrientation=0;

    Bitmap bitmap;
    private Paint paint;

    public MyImageView(Context context) {
        super(context);
        init();
    }

    public MyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public MyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    @IntegerRes
    public int getScreenOrientation(){
        return screenOrientation;
    }


    public void setScreenOrientation(@IntegerRes int res){
        this.screenOrientation=res;
    }

    private void init() {
        paint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(0,widthMeasureSpec),getDefaultSize(0,heightMeasureSpec));
        int childWidthSize=getMeasuredWidth();
        int childHeightSize=getMeasuredHeight();
        if (childHeightSize>childWidthSize){
            heightMeasureSpec=widthMeasureSpec=MeasureSpec.makeMeasureSpec(childWidthSize,MeasureSpec.EXACTLY);
            setScreenOrientation(R.integer.vertical);
        }else{
            heightMeasureSpec=widthMeasureSpec=MeasureSpec.makeMeasureSpec(childHeightSize,MeasureSpec.EXACTLY);
            setScreenOrientation(R.integer.orientation);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }


}
