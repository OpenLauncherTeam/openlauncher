package com.benny.openlauncher.viewutil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Region;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.App;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.Tool;

public class GroupDrawable extends Drawable {
    private Drawable[] _icons;
    private int _iconsCount;
    private Paint _paintInnerCircle;
    private Paint _paintOuterCircle;
    private Paint _paintIcon;
    private boolean _needAnimate;
    private boolean _needAnimateScale;
    private float _scaleFactor = 1;
    private float _iconSize;
    private float _padding;

    // For group of 3 icons (1st row extra padding)
    private float _padding31;

    // For group of 3 icons (2st row extra padding)
    private float _padding32;
    private int _outline;
    private int _iconSizeDiv2;
    private int _iconSizeDiv4;

    public GroupDrawable(Context context, Item item, int iconSize) {
        final float size = Tool.dp2px(iconSize);
        final Drawable[] icons = new Drawable[4];
        for (int i = 0; i < 4; i++) {
            icons[i] = null;
        }

        init(icons, item.getItems().size(), size);
        for (int i = 0; i < 4 && i < item.getItems().size(); i++) {
            Item temp = item.getItems().get(i);
            App app = null;
            if (temp != null) {
                app = Setup.appLoader().findItemApp(temp);
            }
            if (app == null) {
                Log.d(this.getClass().getName(), String.format("Item %s has a null app at index %d (Intent: %s)", item.getLabel(), i, temp == null ? "Item is NULL" : temp.getIntent()));
                icons[i] = new ColorDrawable(Color.TRANSPARENT);
            } else {
                _icons[i] = app.getIcon();
            }
        }
    }

    @Override
    public int getIntrinsicHeight() {
        return (int) _iconSize;
    }

    @Override
    public int getIntrinsicWidth() {
        return (int) _iconSize;
    }

    private static final float PADDING31_KOEF = (1f - (float) Math.sqrt(3) / 2f);
    private static final float PADDING32_KOEF = ((float) (Math.sqrt(3) - 1f) / (2f * (float) Math.sqrt(3)));

    private void init(Drawable[] icons, int iconsCount, float size) {
        _icons = icons;
        _iconsCount = iconsCount;
        _iconSize = size;
        _iconSizeDiv2 = Math.round(_iconSize / 2f);
        _iconSizeDiv4 = Math.round(_iconSize / 4f);
        _padding = _iconSize / 25f;
        float b = _iconSize / 2f + 2 * _padding;
        _padding31 = b * PADDING31_KOEF;
        _padding32 = b * (PADDING32_KOEF - PADDING31_KOEF);

        _paintInnerCircle = new Paint();
        _paintInnerCircle.setColor(Setup.appSettings().getDesktopFolderColor());
        _paintInnerCircle.setAlpha(150);
        _paintInnerCircle.setAntiAlias(true);

        _paintOuterCircle = new Paint();
        _paintOuterCircle.setColor(Setup.appSettings().getDesktopFolderColor());
        _paintOuterCircle.setAntiAlias(true);
        _paintOuterCircle.setFlags(Paint.ANTI_ALIAS_FLAG);
        _paintOuterCircle.setStyle(Paint.Style.STROKE);
        _outline = Tool.dp2px(2);
        _paintOuterCircle.setStrokeWidth(_outline);

        _paintIcon = new Paint();
        _paintIcon.setAntiAlias(true);
        _paintIcon.setFilterBitmap(true);
    }

    public void popUp() {
        _needAnimate = true;
        _needAnimateScale = true;
        invalidateSelf();
    }

    public void popBack() {
        _needAnimate = false;
        _needAnimateScale = false;
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();

        if (_needAnimateScale) {
            _scaleFactor = Tool.clampFloat(_scaleFactor - 0.09f, 0.5f, 1f);
        } else {
            _scaleFactor = Tool.clampFloat(_scaleFactor + 0.09f, 0.5f, 1f);
        }

        canvas.scale(_scaleFactor, _scaleFactor, _iconSize / 2, _iconSize / 2);

        Path clip = new Path();
        clip.addCircle(_iconSize / 2, _iconSize / 2, _iconSize / 2 - _outline, Path.Direction.CW);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            canvas.clipPath(clip, Region.Op.REPLACE);
        } else {
            canvas.clipPath(clip);
        }

        canvas.drawCircle(_iconSize / 2, _iconSize / 2, _iconSize / 2 - _outline, _paintInnerCircle);

        if (_iconsCount > 3) {
            if (_icons[0] != null) {
                drawIcon(canvas, _icons[0], _padding, _padding, _iconSizeDiv2 - _padding, _iconSizeDiv2 - _padding, _paintIcon);
            }
            if (_icons[1] != null) {
                drawIcon(canvas, _icons[1], _iconSizeDiv2 + _padding, _padding, _iconSize - _padding, _iconSizeDiv2 - _padding, _paintIcon);
            }
            if (_icons[2] != null) {
                drawIcon(canvas, _icons[2], _padding, _iconSizeDiv2 + _padding, _iconSizeDiv2 - _padding, _iconSize - _padding, _paintIcon);
            }
            if (_icons[3] != null) {
                drawIcon(canvas, _icons[3], _iconSizeDiv2 + _padding, _iconSizeDiv2 + _padding, _iconSize - _padding, _iconSize - _padding, _paintIcon);
            }
        } else if (_iconsCount > 2) {
            if (_icons[0] != null) {
                drawIcon(canvas, _icons[0], _padding, _padding + _padding31, _iconSizeDiv2 - _padding, _iconSizeDiv2 - _padding + _padding31, _paintIcon);
            }
            if (_icons[1] != null) {
                drawIcon(canvas, _icons[1], _iconSizeDiv2 + _padding, _padding + _padding31, _iconSize - _padding, _iconSizeDiv2 - _padding + _padding31, _paintIcon);
            }
            if (_icons[2] != null) {
                drawIcon(canvas, _icons[2], _padding + _iconSizeDiv4, _iconSizeDiv2 + _padding + _padding32, _iconSizeDiv4 + _iconSizeDiv2 - _padding, _iconSize - _padding + _padding32, _paintIcon);
            }
        } else {// if (_iconsCount <= 2) {
            if (_icons[0] != null) {
                drawIcon(canvas, _icons[0], _padding, _padding + _iconSizeDiv4, _iconSizeDiv2 - _padding, _iconSizeDiv4 + _iconSizeDiv2 - _padding, _paintIcon);
            }
            if (_icons[1] != null) {
                drawIcon(canvas, _icons[1], _iconSizeDiv2 + _padding, _padding + _iconSizeDiv4, _iconSize - _padding, _iconSizeDiv4 + _iconSizeDiv2 - _padding, _paintIcon);
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            canvas.clipRect(0, 0, _iconSize, _iconSize, Region.Op.REPLACE);
        }

        canvas.drawCircle(_iconSize / 2, _iconSize / 2, _iconSize / 2 - _outline, _paintOuterCircle);
        canvas.restore();

        if (_needAnimate) {
            _paintIcon.setAlpha(Tool.clampInt(_paintIcon.getAlpha() - 25, 0, 255));
            invalidateSelf();
        } else if (_paintIcon.getAlpha() != 255) {
            _paintIcon.setAlpha(Tool.clampInt(_paintIcon.getAlpha() + 25, 0, 255));
            invalidateSelf();
        }
    }

    private void drawIcon(Canvas canvas, Drawable icon, float l, float t, float r, float b, Paint paint) {
        icon.setBounds((int) l, (int) t, (int) r, (int) b);
        icon.setFilterBitmap(true);
        icon.setAlpha(paint.getAlpha());
        icon.draw(canvas);
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
