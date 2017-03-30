package com.benny.openlauncher.viewutil;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;

import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.util.Tool;

public class GroupIconDrawable extends Drawable {

    private int outlinepad;
    Bitmap[] icons;
    public float iconSize;
    Paint paint;
    Paint paint2;
    Paint paint4;
    private int iconSizeDiv2;
    private float padding;

    private float scaleFactor = 1;

    private boolean needAnimate, needAnimateScale;

    private float sx = 1;
    private float sy = 1;

    public GroupIconDrawable(Bitmap[] icons, float size) {
        init(icons, size);
    }

    private void init(Bitmap[] icons, float size) {
        this.icons = icons;
        this.iconSize = size;
        iconSizeDiv2 = Math.round(iconSize / 2f);
        padding = iconSize / 25f;

        this.paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAlpha(150);
        paint.setAntiAlias(true);

        this.paint4 = new Paint();
        paint4.setColor(Color.WHITE);
        paint4.setAntiAlias(true);
        paint4.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint4.setStyle(Paint.Style.STROKE);
        outlinepad = Tool.dp2px(2, Home.launcher);
        paint4.setStrokeWidth(outlinepad);

        this.paint2 = new Paint();
        paint2.setAntiAlias(true);
        paint2.setFilterBitmap(true);
    }

    public void popUp() {
        sy = 1;
        sx = 1;
        needAnimate = true;
        needAnimateScale = true;
        invalidateSelf();
    }

    public void popBack() {
        needAnimate = false;
        needAnimateScale = false;
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();

        if (needAnimateScale) {
            scaleFactor = Tool.clampFloat(scaleFactor - 0.09f, 0.5f, 1f);
        } else {
            scaleFactor = Tool.clampFloat(scaleFactor + 0.09f, 0.5f, 1f);
        }

        canvas.scale(scaleFactor, scaleFactor, iconSize / 2, iconSize / 2);

        Path clipp = new Path();
        clipp.addCircle(iconSize / 2, iconSize / 2, iconSize / 2 - outlinepad, Path.Direction.CW);
        canvas.clipPath(clipp, Region.Op.REPLACE);

        canvas.drawCircle(iconSize / 2, iconSize / 2, iconSize / 2 - outlinepad, paint);

        canvas.drawBitmap(icons[0], null, new RectF(padding, padding, iconSizeDiv2 - padding, iconSizeDiv2 - padding), paint2);
        canvas.drawBitmap(icons[1], null, new RectF(iconSizeDiv2 + padding, padding, iconSize - padding, iconSizeDiv2 - padding), paint2);
        canvas.drawBitmap(icons[2], null, new RectF(padding, iconSizeDiv2 + padding, iconSizeDiv2 - padding, iconSize - padding), paint2);
        canvas.drawBitmap(icons[3], null, new RectF(iconSizeDiv2 + padding, iconSizeDiv2 + padding, iconSize - padding, iconSize - padding), paint2);

        canvas.clipRect(0, 0, iconSize, iconSize, Region.Op.REPLACE);

        canvas.drawCircle(iconSize / 2, iconSize / 2, iconSize / 2 - outlinepad, paint4);
        canvas.restore();

        if (needAnimate) {
            paint2.setAlpha(Tool.clampInt(paint2.getAlpha() - 25, 0, 255));
            invalidateSelf();
        } else if (paint2.getAlpha() != 255) {
            paint2.setAlpha(Tool.clampInt(paint2.getAlpha() + 25, 0, 255));
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
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }
}
