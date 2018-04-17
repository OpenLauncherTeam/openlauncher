package com.benny.openlauncher.drawable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.util.ToolKt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LauncherActionDrawable extends Drawable {
    private final LightingColorFilter _filterDkGray;
    @NotNull
    private final Bitmap _icon;
    private final float _iconRadius;
    private final int _iconSize;
    private final Paint _paint;

    public LauncherActionDrawable(@NotNull Bitmap icon) {
        _icon = icon;
        _iconSize = ToolKt.toPx(Setup.Companion.appSettings().getDesktopIconSize());
        _iconRadius = (((float) _iconSize) / 2.0f) - ((float) ToolKt.toPx(4));
        _paint = new Paint(1);
        _filterDkGray = new LightingColorFilter(-12303292, 1);
        _paint.setColor(-1);
        _paint.setFilterBitmap(true);
    }

    @NotNull
    public final Bitmap getIcon() {
        return _icon;
    }

    public void draw(@Nullable Canvas canvas) {
        if (canvas != null) {
            _paint.setColorFilter((ColorFilter) null);
            canvas.drawCircle(((float) _iconSize) / 2.0f, ((float) _iconSize) / 2.0f, _iconRadius, _paint);
            _paint.setColorFilter(_filterDkGray);
            canvas.drawBitmap(_icon, ((float) (_iconSize - _icon.getWidth())) / 2.0f, ((float) (_iconSize - _icon.getWidth())) / 2.0f, _paint);
        }
    }

    public void setAlpha(int alpha) {
        _paint.setAlpha(alpha);
    }

    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        _paint.setColorFilter(colorFilter);
    }
}