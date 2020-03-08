package com.mymusic.app.view;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;


import com.mymusic.app.DensityUtil;
import com.mymusic.app.R;

import java.text.DecimalFormat;

/**
 * <pre>
 *           .----.
 *        _.'__    `.
 *    .--(Q)(OK)---/$\
 *  .' @          /$$$\
 *  :         ,   $$$$$
 *   `-..__.-' _.-\$$/
 *         `;_:    `"'
 *       .'"""""`.
 *      /,  FLY  ,\
 *     //         \\
 *     `-._______.-'
 *     ___`. | .'___
 *    (______|______)
 * </pre>
 * 包    名 : com.fly.myview.progressbar
 * 作    者 : FLY
 * 创建时间 : 2018/9/6
 * 描述: 更新下载的进度条
 */

public class MyProgressBar extends View {
    /**
     * 进度条最大值
     */
    private float mMax = 100;
    Context context;

    /**
     * 进度条当前进度值
     */
    private float mProgress = 0;

    /**
     * 默认已完成颜色
     */
    private final int DEFAULT_FINISHED_COLOR = getResources().getColor(R.color.colorAccent);

    /**
     * 默认未完成颜色
     */
    private final int DEFAULT_UNFINISHED_COLOR = getResources().getColor(R.color.a66);

    /**
     * 已完成进度颜色
     */
    private int mReachedBarColor;

    /**
     * 未完成进度颜色
     */
    private int mUnreachedBarColor;

    /**
     * 进度条高度
     */
    private float mBarHeight;

    /**
     * the progress text color.
     */
    private int mTextColor;

    /**
     * 后缀
     */
    private String mSuffix = "%";

    /**
     * 前缀
     */
    private String mPrefix = "";

    /**
     * 未完成进度条所在矩形区域
     */
    private RectF mUnreachedRectF = new RectF(0, 0, 0, 0);
    /**
     * 已完成进度条所在矩形区域
     */
    public RectF mReachedRectF = new RectF(0, 0, 0, 0);

    /**
     * 画笔
     */
    private Paint mPaint;

    private boolean mTextVisibility;



    Paint textPaint;
    float textCenter;


    public MyProgressBar(Context context) {
        this(context, null);
    }

    public MyProgressBar(Context context,  AttributeSet attrs) {
        this(context, attrs, 0);
        this.context=context;
        initAttrs(context, attrs);
        initPainters();
    }

    public MyProgressBar(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
        initAttrs(context, attrs);
        initPainters();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.UpdataAPPProgressBar);
        mMax = typedArray.getInteger(R.styleable.UpdataAPPProgressBar_updataAPPMax, (int) mMax);
        mProgress = typedArray.getInteger(R.styleable.UpdataAPPProgressBar_updataAPPProgress, (int) mProgress);
        mReachedBarColor = typedArray.getColor(R.styleable.UpdataAPPProgressBar_updataAPPReachedBarColor, DEFAULT_FINISHED_COLOR);
        mUnreachedBarColor = typedArray.getColor(R.styleable.UpdataAPPProgressBar_updataAPPUnreachedBarColor, DEFAULT_UNFINISHED_COLOR);
        mTextColor = typedArray.getColor(R.styleable.UpdataAPPProgressBar_updataAPPTextColor, DEFAULT_UNFINISHED_COLOR);
        mSuffix = TextUtils.isEmpty(typedArray.getString(R.styleable.UpdataAPPProgressBar_updataAPPSuffix)) ? mSuffix : typedArray.getString(R.styleable.UpdataAPPProgressBar_updataAPPSuffix);
        mPrefix = TextUtils.isEmpty(typedArray.getString(R.styleable.UpdataAPPProgressBar_updataAPPPrefix)) ? mPrefix : typedArray.getString(R.styleable.UpdataAPPProgressBar_updataAPPPrefix);
        mBarHeight = typedArray.getDimension(R.styleable.UpdataAPPProgressBar_updataAPPBarHeight, dp2px());
        mTextVisibility = typedArray.getBoolean(R.styleable.UpdataAPPProgressBar_updataAPPTextVisibility, true);
        typedArray.recycle();
    }

    private void initPainters() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿
        mPaint.setAntiAlias(true);//防抖动
        mPaint.setStrokeCap(Paint.Cap.SQUARE);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿
        textPaint.setAntiAlias(true);//防抖动
        textPaint.setStrokeCap(Paint.Cap.SQUARE);
        textPaint.setColor(Color.GRAY);
        textCenter=0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        calculateDrawRectFWithoutProgressText();
        mPaint.setColor(mUnreachedBarColor);
//        canvas.clipRect(mUnreachedRectF);
        canvas.drawRoundRect(mUnreachedRectF, mBarHeight / 4, mBarHeight / 4, mPaint);
        mPaint.setColor(mReachedBarColor);
//        canvas.clipRect(mReachedRectF);
        canvas.drawRoundRect(mReachedRectF, mBarHeight / 4, mBarHeight / 4, mPaint);
        mPaint.setColor(mTextColor);
        mPaint.setTextSize(mBarHeight * 0.6f);
        String mCurrentDrawText = new DecimalFormat("#").format(getProgress() * 100 / getMax());
        mCurrentDrawText = mPrefix + mCurrentDrawText + mSuffix;
        float mDrawTextWidth = mPaint.measureText(mCurrentDrawText);
        if (mTextVisibility && getProgress() > 0 && mReachedRectF.right >mDrawTextWidth) {
            canvas.drawText(mCurrentDrawText, mReachedRectF.right - mDrawTextWidth - mBarHeight * 0.4f, (int) ((getHeight() / 2.0f) - ((mPaint.descent() + mPaint.ascent()) / 2.0f)), mPaint);
        }
    }

    private void calculateDrawRectFWithoutProgressText() {
        float textLength=DensityUtil.dip2px(context,8);
        mReachedRectF.left = getPaddingLeft();
        mReachedRectF.top = getHeight() / 2.0f - mBarHeight / 2.0f;
        mReachedRectF.right = (getWidth() - getPaddingLeft() - getPaddingRight()) / (getMax() * 1.0f) * getProgress() + getPaddingLeft();
        mReachedRectF.bottom = getHeight() / 2.0f + mBarHeight / 2.0f;

        mUnreachedRectF.left = getPaddingLeft();
        mUnreachedRectF.top = getHeight() / 2.0f + -mBarHeight / 2.0f;
        mUnreachedRectF.right = getWidth() - getPaddingRight();
        mUnreachedRectF.bottom = getHeight() / 2.0f + mBarHeight / 2.0f;
    }

    public float getMax() {
        return mMax;
    }

    public float  getProgress() {
        return mProgress;
    }

    public void setMax(int max) {
        this.mMax = max;
    }

    public synchronized void setProgress(float progress) {
        this.mProgress = checkProgress(progress);
        postInvalidate(getLeft(),getTop(),getRight(),getBottom());
    }


    private float checkProgress(float progress) {
        if (progress < 0) return 0;
        return Math.min(progress, mMax);
    }

    private int dp2px() {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) ((float) 2.0 * scale + 0.5f);
    }
}

