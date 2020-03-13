package com.mymusic.app.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;

import androidx.annotation.NonNull;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

public class BitmapTransform extends BitmapTransformation {
    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {

        int w = toTransform.getWidth();
        int h = toTransform.getHeight();
        int GRADIENT_HEIGHT = 100;

        Bitmap overlay = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//        Bitmap overlay=pool.get(w,h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);

        canvas.drawBitmap(toTransform, 0, 0, null);
        Palette palette=Palette.from(toTransform).generate();

        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0,  h -GRADIENT_HEIGHT, 0, h, palette.getLightVibrantColor(Color.WHITE), 0x00000000, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawRect(0, h-GRADIENT_HEIGHT, w, h, paint);

        return overlay;
    }

//    public Bitmap addGradient(Bitmap src) {
//        int w = src.getWidth();
//        int h = src.getHeight();
//        int GRADIENT_HEIGHT = h/2;
//
//        Bitmap overlay = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(overlay);
//
//        canvas.drawBitmap(src, 0, 0, null);
//        Palette palette=Palette.from(src).generate();
//
//        Paint paint = new Paint();
//        LinearGradient shader = new LinearGradient(0,  h -GRADIENT_HEIGHT, 0, h, palette.getLightVibrantColor(Color.WHITE), 0x00000000, Shader.TileMode.CLAMP);
//        paint.setShader(shader);
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
//        canvas.drawRect(0, h - GRADIENT_HEIGHT, w, h, paint);
//
//        return overlay;
//    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

    }
}
