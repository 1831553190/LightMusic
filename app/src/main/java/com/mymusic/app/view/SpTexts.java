package com.mymusic.app.view;


import android.content.Context;
import android.content.res.Resources;
import android.util.ArrayMap;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.widget.TextView;

/**
 * Capture initial sp values for registered textviews, and update properly when configuration
 * changes.
 */
public class SpTexts {

    private final Context mContext;
    private final ArrayMap<TextView, Integer> mTexts = new ArrayMap<>();

    public SpTexts(Context context) {
        mContext = context;
    }

    public int add(final TextView text) {
        if (text == null) return 0;
        final Resources res = mContext.getResources();
        final float fontScale = res.getConfiguration().fontScale;
        final float density = res.getDisplayMetrics().density;
        final float px = text.getTextSize();
        final int sp = (int)(px / fontScale / density);
        mTexts.put(text, sp);
        text.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
            @Override
            public void onViewDetachedFromWindow(View v) {
            }

            @Override
            public void onViewAttachedToWindow(View v) {
                setTextSizeH(text, sp);
            }
        });
        return sp;
    }

    public void update() {
        if (mTexts.isEmpty()) return;
        mTexts.keyAt(0).post(mUpdateAll);
    }

    private void setTextSizeH(TextView text, int sp) {
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
    }

    private final Runnable mUpdateAll = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < mTexts.size(); i++) {
                setTextSizeH(mTexts.keyAt(i), mTexts.valueAt(i));
            }
        }
    };
}