package com.benny.openlauncher.drawable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import com.benny.openlauncher.util.Tool;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: LauncherCircleDrawable.kt */
public final class LauncherCircleDrawable extends Drawable {
    private float _currentScale = 1.0f;
    private boolean _hidingOldIcon;
    private Bitmap _icon;
    private final int _iconPadding;
    private final int _iconSize;
    private final int _iconSizeReal;
    private Bitmap _iconToFade;
    private final Paint _paint;
    private final Paint _paint2;
    private final float _scaleStep = 0.1f;



    public void draw(@NotNull Canvas canvas) {
        canvas.drawCircle((_iconSize / 2f), (_iconSize / 2f), (_iconSize / 2f), _paint);

        if (_iconToFade != null) {
            canvas.save();
            if (_hidingOldIcon) {
                _currentScale -= _scaleStep;
            } else {
                _currentScale += _scaleStep;
            }
            _currentScale = Tool.clampFloat(_currentScale, 0.4f, 1f);
            canvas.scale(_currentScale, _currentScale, (_iconSize / 2f), (_iconSize / 2f));
            canvas.drawBitmap(_hidingOldIcon ? _iconToFade : _icon, (_iconSize / 2f - _iconSizeReal / 2f), (_iconSize / 2f - _iconSizeReal / 2f), _paint2);
            canvas.restore();

            if (_currentScale >= 0.39f && _currentScale <= 0.41f) {
                _hidingOldIcon = false;
            }

            if (!_hidingOldIcon && _scaleStep >= 0.99f && _scaleStep <= 1.01f) {
                _iconToFade = null;
            }

            invalidateSelf();
        } else {
            canvas.drawBitmap(_icon, (_iconSize / 2f - _iconSizeReal / 2f), (_iconSize / 2f - _iconSizeReal / 2f), _paint2);
        }
    }

    public LauncherCircleDrawable(@NotNull Context context, @NotNull Drawable icon, int color) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(icon, "_icon");
        _icon = Tool.drawableToBitmap(icon);
        _iconPadding = Tool.dp2px(6, context);
        _iconSizeReal = icon.getIntrinsicHeight();
        _iconSize = icon.getIntrinsicHeight() + (_iconPadding * 2);
        _paint = new Paint(1);
        _paint.setColor(color);
        _paint.setAlpha(100);
        _paint.setStyle(Style.FILL);
        _paint2 = new Paint(1);
        _paint2.setFilterBitmap(true);
    }

    public final void setIcon(@NotNull Drawable icon) {
        Intrinsics.checkParameterIsNotNull(icon, "_icon");
        _iconToFade = _icon;
        _hidingOldIcon = true;
        _icon = Tool.drawableToBitmap(icon);
        invalidateSelf();
    }

    public int getIntrinsicWidth() {
        return _iconSize;
    }

    public int getIntrinsicHeight() {
        return _iconSize;
    }

    public void setAlpha(int i) {
    }

    public void setColorFilter(@Nullable ColorFilter colorFilter) {
    }

    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }
}