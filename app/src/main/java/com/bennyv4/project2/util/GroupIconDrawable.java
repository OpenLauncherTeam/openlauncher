package com.bennyv4.project2.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class GroupIconDrawable extends Drawable{

    Bitmap[] icons;
    int iconSize;
    Paint paint;
    private int iconSizeDiv2;

    public GroupIconDrawable(Bitmap[] icons,int size){
        this.icons = icons;
        this.iconSize = size;
        iconSizeDiv2 = iconSize / 2;
        this.paint = new Paint();

        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(iconSizeDiv2, iconSizeDiv2, iconSizeDiv2,paint);
        canvas.drawBitmap(icons[0],null,new Rect(0,0, iconSizeDiv2, iconSizeDiv2),null);
        canvas.drawBitmap(icons[1],null,new Rect(iconSizeDiv2,0,iconSize, iconSizeDiv2),null);
        canvas.drawBitmap(icons[2],null,new Rect(0, iconSizeDiv2, iconSizeDiv2,iconSize),null);
        canvas.drawBitmap(icons[3],null,new Rect(iconSizeDiv2, iconSizeDiv2,iconSize,iconSize),null);
    }

    @Override
    public void setAlpha(int i) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }
}
