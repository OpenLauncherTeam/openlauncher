package com.benny.openlauncher.viewutil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import com.benny.openlauncher.util.Tool;

public class CircleDrawable extends Drawable {

    private int iconSize;
    private int iconSizeReal;
    private int iconPadding;
    private Bitmap icon;
    private Bitmap iconToFade;
    private Paint paint;
    private Paint paint2;

    private float scaleStep = 0.08f;
    private float currentScale = 1f;
    private boolean hidingOldIcon;

    public CircleDrawable(Context context, Drawable icon, int color) {
        this.icon = Tool.drawableToBitmap(icon);

        iconPadding = Tool.dp2px(6, context);

        iconSizeReal = icon.getIntrinsicHeight();
        iconSize = icon.getIntrinsicHeight() + iconPadding * 2;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setAlpha(100);
        paint.setStyle(Paint.Style.FILL);

        paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2.setFilterBitmap(true);
    }

    public void setIcon(Drawable icon) {
        iconToFade = this.icon;
        hidingOldIcon = true;

        this.icon = Tool.drawableToBitmap(icon);
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(iconSize / 2, iconSize / 2, iconSize / 2, paint);

        if (iconToFade != null) {
            canvas.save();
            if (hidingOldIcon)
                currentScale -= scaleStep;
            else
                currentScale += scaleStep;
            currentScale = Tool.clampFloat(currentScale, 0, 1);
            canvas.scale(currentScale, currentScale, iconSize / 2, iconSize / 2);
            canvas.drawBitmap(hidingOldIcon ? iconToFade : icon, iconSize / 2 - iconSizeReal / 2, iconSize / 2 - iconSizeReal / 2, paint2);
            canvas.restore();

            if (currentScale == 0)
                hidingOldIcon = false;

            if (!hidingOldIcon && scaleStep == 1)
                iconToFade = null;

            invalidateSelf();
        } else {
            canvas.drawBitmap(icon, iconSize / 2 - iconSizeReal / 2, iconSize / 2 - iconSizeReal / 2, paint2);
        }
    }

    @Override
    public int getIntrinsicWidth() {
        return iconSize;
    }

    @Override
    public int getIntrinsicHeight() {
        return iconSize;
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
