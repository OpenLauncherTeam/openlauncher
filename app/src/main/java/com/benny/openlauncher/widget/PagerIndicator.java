package com.benny.openlauncher.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.benny.openlauncher.util.Tool;

public final class PagerIndicator extends View implements ViewPager.OnPageChangeListener {
    private ViewPager _pager;
    private Paint _paint = new Paint(1);

    private int _mode = Mode.DOTS;
    private float _pad;
    private float _dotSize;
    private float myX;
    private int _previousPage = -1;
    private int _realPreviousPage;
    private float _scaleFactor = 1.0f;
    private float _scaleFactor2 = 1.5f;

    // current position and offset
    private float _scrollOffset;
    private int _scrollPosition;

    public static class Mode {
        public static final int DOTS = 0;
        public static final int LINES = 1;
    }

    public PagerIndicator(Context context) {
        this(context, null);
    }

    public PagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        _pad = Tool.toPx(4);
        _paint.setColor(Color.WHITE);
        _paint.setStrokeWidth(Tool.toPx(4));
        _paint.setAntiAlias(true);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        _dotSize = getHeight() / 2;
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (_pager == null) return;
        int pageCount = _pager.getAdapter().getCount();
        switch (_mode) {
            case Mode.DOTS:
                float circlesWidth = pageCount * (_dotSize + _pad * 2);
                canvas.translate(getWidth() / 2 - circlesWidth / 2, 0f);

                if (_realPreviousPage != _pager.getCurrentItem()) {
                    _scaleFactor = 1f;
                    _realPreviousPage = _pager.getCurrentItem();
                }

                for (int dot = 0; dot < pageCount; dot++) {
                    float stepFactor = 0.05f;
                    float smallFactor = 1f;
                    float largeFactor = 1.5f;
                    if (dot == _pager.getCurrentItem()) {
                        // animate shrink
                        if (_previousPage == -1)
                            _previousPage = dot;
                        _scaleFactor = Tool.clampFloat(_scaleFactor + stepFactor, smallFactor, largeFactor);
                        canvas.drawCircle(_dotSize / 2 + _pad + (_dotSize + _pad * 2) * dot, (float) (getHeight() / 2), _scaleFactor * _dotSize / 2, _paint);
                        if (_scaleFactor != largeFactor)
                            invalidate();
                    } else if (dot != _pager.getCurrentItem() && dot == _previousPage) {
                        // animate expand
                        _scaleFactor2 = Tool.clampFloat(_scaleFactor2 - stepFactor, smallFactor, largeFactor);
                        canvas.drawCircle(_dotSize / 2 + _pad + (_dotSize + _pad * 2) * dot, (float) (getHeight() / 2), _scaleFactor2 * _dotSize / 2, _paint);
                        if (_scaleFactor2 != smallFactor)
                            invalidate();
                        else {
                            _scaleFactor2 = 1.5f;
                            _previousPage = -1;
                        }
                    } else {
                        // draw normal circle
                        canvas.drawCircle(_dotSize / 2 + _pad + (_dotSize + _pad * 2) * dot, (float) (getHeight() / 2), _dotSize / 2, _paint);
                    }
                }
                break;
            case Mode.LINES:
                float width = getWidth() / pageCount;
                float start = _scrollPosition * width;

                myX = start + _scrollOffset * width;

                if (myX % width != 0f) invalidate();

                canvas.drawLine(myX, getHeight() / 2, myX + width, getHeight() / 2, _paint);
                break;
        }
    }

    public final void setMode(int mode) {
        _mode = mode;
        invalidate();
    }

    public final void setViewPager(ViewPager pager) {
        if (pager == null && _pager != null) {
            _pager.removeOnPageChangeListener(this);
            _pager = null;
        } else {
            _pager = pager;
            if (pager != null) {
                pager.addOnPageChangeListener(this);
            }
        }
        invalidate();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        _scrollOffset = positionOffset;
        _scrollPosition = position;
        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
        // nothing
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // nothing
    }
}
