package com.bennyv4.project2.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.bennyv4.project2.Home;

public class GroupIconDrawable extends Drawable{

    private final int outlinepad;
    Bitmap[] icons;
    public int iconSize;
    Paint paint;
    Paint paint2;
    Paint paint4;
    private int iconSizeDiv2;
    private int padding;

    private boolean needAnimate;

    public View v;

    private float sx = 1;
    private float sy = 1 ;

    public GroupIconDrawable(Bitmap[] icons,int size){
        this.icons = icons;
        this.iconSize = size;
        iconSizeDiv2 = iconSize / 2;
        padding = iconSize /20;

        this.paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAlpha(200);
        paint.setAntiAlias(true);

        this.paint4 = new Paint();
        paint4.setColor(Color.WHITE);
        paint4.setAntiAlias(true);
        paint4.setStyle(Paint.Style.STROKE);
        outlinepad = Tools.convertDpToPixel(3, Home.desktop.getContext());
        paint4.setStrokeWidth(outlinepad);

        this.paint2 = new Paint();
        paint2.setAntiAlias(true);
        paint2.setFilterBitmap(true);
    }

    public GroupIconDrawable(Bitmap[] icons,int size,View v){
        this.icons = icons;
        this.iconSize = size;
        iconSizeDiv2 = iconSize / 2;
        padding = iconSize /20;

        this.paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAlpha(200);
        paint.setAntiAlias(true);

        this.paint4 = new Paint();
        paint4.setColor(Color.WHITE);
        paint4.setAntiAlias(true);
        paint4.setStyle(Paint.Style.STROKE);
        outlinepad = Tools.convertDpToPixel(3, Home.desktop.getContext());
        paint4.setStrokeWidth(outlinepad);

        this.paint2 = new Paint();
        paint2.setAntiAlias(true);
        paint2.setFilterBitmap(true);

        this.v =v;
    }

    public void popUp(){
        sy = 1;
        sx = 1;
        needAnimate = true;
        invalidateSelf();
    }

    public void popBack(){
        needAnimate = false;
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        if (v!= null)
            canvas.translate(0,v.getHeight()/2-iconSize/2);

        Path clipp = new Path();
        clipp.addCircle(iconSize / 2,iconSize / 2,iconSize / 2-outlinepad, Path.Direction.CW);
        canvas.clipPath(clipp, Region.Op.REPLACE);

        canvas.drawCircle(iconSize / 2, iconSize / 2, iconSize / 2-outlinepad,paint);
        canvas.drawBitmap(icons[0],null,new Rect(padding,padding, iconSizeDiv2-padding, iconSizeDiv2-padding),paint2);
        canvas.drawBitmap(icons[1],null,new Rect(iconSizeDiv2+padding,padding,iconSize-padding, iconSizeDiv2-padding),paint2);
        canvas.drawBitmap(icons[2],null,new Rect(padding, iconSizeDiv2+padding, iconSizeDiv2-padding,iconSize-padding),paint2);
        canvas.drawBitmap(icons[3],null,new Rect(iconSizeDiv2+padding, iconSizeDiv2+padding,iconSize-padding,iconSize-padding),paint2);

        canvas.clipRect(0,0,iconSize,iconSize, Region.Op.REPLACE);

        canvas.drawCircle(iconSize / 2, iconSize / 2, iconSize / 2-outlinepad,paint4);
        canvas.restore();

        if (needAnimate){
            paint2.setAlpha(Tools.clampInt(paint2.getAlpha()-25,0,255));
            invalidateSelf();
        }else if (paint2.getAlpha() != 255){
            paint2.setAlpha(Tools.clampInt(paint2.getAlpha()+25,0,255));
            invalidateSelf();
        }
    }

    @Override
    public void setAlpha(int i) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {return 0;}
}
