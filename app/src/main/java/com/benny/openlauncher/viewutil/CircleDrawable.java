package com.benny.openlauncher.viewutil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import com.benny.openlauncher.util.Tool;

public class CircleDrawable extends Drawable {

    private int _iconSize;
    private int _iconSizeReal;
    private int _iconPadding;
    private int _iconColor;
    private Bitmap _icon;
    private Bitmap _iconToFade;
    private Paint _paint;
    private Paint _paint2;

    private float _scaleStep = 0.08f;
    private float _currentScale = 1f;
    private boolean _hidingOldIcon;

    public CircleDrawable(Context context, Drawable icon, int colorIcon, int colorBackground, int alphaBackground) {
        icon.setColorFilter(colorIcon, PorterDuff.Mode.SRC_ATOP);
        _icon = Tool.drawableToBitmap(icon);

        _iconPadding = Tool.dp2px(6);
        _iconColor = colorIcon;

        _iconSizeReal = icon.getIntrinsicHeight();
        _iconSize = icon.getIntrinsicHeight() + _iconPadding * 2;

        _paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _paint.setColor(colorBackground);
        _paint.setAlpha(alphaBackground);
        _paint.setStyle(Paint.Style.FILL);

        _paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        _paint2.setColor(colorIcon);
        _paint2.setFilterBitmap(true);
    }

    public void setIcon(Drawable icon) {
        _iconToFade = _icon;
        _hidingOldIcon = true;

        icon.setColorFilter(_iconColor, PorterDuff.Mode.SRC_ATOP);

        _icon = Tool.drawableToBitmap(icon);
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(_iconSize / 2, _iconSize / 2, _iconSize / 2, _paint);

        if (_iconToFade != null) {
            canvas.save();
            if (_hidingOldIcon)
                _currentScale -= _scaleStep;
            else
                _currentScale += _scaleStep;
            _currentScale = Tool.clampFloat(_currentScale, 0, 1);
            canvas.scale(_currentScale, _currentScale, _iconSize / 2, _iconSize / 2);
            canvas.drawBitmap(_hidingOldIcon ? _iconToFade : _icon, _iconSize / 2 - _iconSizeReal / 2, _iconSize / 2 - _iconSizeReal / 2, _paint2);
            canvas.restore();

            if (_currentScale == 0)
                _hidingOldIcon = false;

            if (!_hidingOldIcon && _scaleStep == 1)
                _iconToFade = null;

            invalidateSelf();
        } else {
            canvas.drawBitmap(_icon, _iconSize / 2 - _iconSizeReal / 2, _iconSize / 2 - _iconSizeReal / 2, _paint2);
        }
    }

    @Override
    public int getIntrinsicWidth() {
        return _iconSize;
    }

    @Override
    public int getIntrinsicHeight() {
        return _iconSize;
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
