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
import android.util.Log;

import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.model.App;
import com.benny.openlauncher.util.Tool;

public class GroupIconDrawable extends Drawable {
    private Drawable[] _icons;
    private Paint _paintInnerCircle;
    private Paint _paintOuterCircle;
    private Paint _paintIcon;
    private boolean _needAnimate;
    private boolean _needAnimateScale;
    private float _scaleFactor = 1;
    private float _iconSize;
    private float _padding;
    private int _outline;
    private int _iconSizeDiv2;

    public GroupIconDrawable(Context context, Item item, int iconSize) {
        final float size = Tool.dp2px(iconSize, context);
        final Drawable[] icons = new Drawable[4];
        for (int i = 0; i < 4; i++) {
            icons[i] = null;
        }
        init(icons, size);
        for (int i = 0; i < 4 && i < item.getItems().size(); i++) {
            Item temp = item.getItems().get(i);
            App app = null;
            if (temp != null) {
                app = Setup.appLoader().findItemApp(temp);
            }
            if (app == null) {
                Setup.logger().log(this, Log.DEBUG, null, "Item %s has a null app at index %d (Intent: %s)", item.getLabel(), i, temp == null ? "Item is NULL" : temp.getIntent());
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

    private void init(Drawable[] icons, float size) {
        _icons = icons;
        _iconSize = size;
        _iconSizeDiv2 = Math.round(_iconSize / 2f);
        _padding = _iconSize / 25f;

        _paintInnerCircle = new Paint();
        _paintInnerCircle.setColor(Color.WHITE);
        _paintInnerCircle.setAlpha(150);
        _paintInnerCircle.setAntiAlias(true);

        _paintOuterCircle = new Paint();
        _paintOuterCircle.setColor(Color.WHITE);
        _paintOuterCircle.setAntiAlias(true);
        _paintOuterCircle.setFlags(Paint.ANTI_ALIAS_FLAG);
        _paintOuterCircle.setStyle(Paint.Style.STROKE);
        _outline = Tool.dp2px(2, HomeActivity.Companion.getLauncher());
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
        canvas.clipPath(clip, Region.Op.REPLACE);

        canvas.drawCircle(_iconSize / 2, _iconSize / 2, _iconSize / 2 - _outline, _paintInnerCircle);

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
        canvas.clipRect(0, 0, _iconSize, _iconSize, Region.Op.REPLACE);

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
