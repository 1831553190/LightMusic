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

public class MyImageView extends androidx.appcompat.widget.AppCompatImageView {


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

    private void init() {
        paint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(0,widthMeasureSpec),getDefaultSize(0,heightMeasureSpec));
        int childWidthSize=getMeasuredWidth();
        heightMeasureSpec=widthMeasureSpec=MeasureSpec.makeMeasureSpec(childWidthSize,MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }


}
