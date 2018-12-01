package com.benny.openlauncher.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.util.Tool;

public final class PagerIndicator extends View implements ViewPager.OnPageChangeListener {
    private float _pad;
    private boolean _alphaFade;
    private boolean _alphaShow;
    private final Runnable _delayShow;
    private Paint _dotPaint = new Paint(1);
    private float _dotSize;
    private int _currentPagerState;
    private int _mode = Mode.DOTS;
    private float myX;
    private ViewPager _pager;
    private int _prePageCount;
    private int _previousPage = -1;
    private int _realPreviousPage;
    private float _scaleFactor = 1.0f;
    private float _scaleFactor2 = 1.5f;
    private float _scrollOffset;
    private int _scrollPagePosition;

    public static class Mode {
        public static final int DOTS = 0;
        public static final int LINES = 1;
    }

    protected void onDraw(Canvas canvas) {
        if (_pager == null) return;
        switch (_mode) {
            case Mode.DOTS: {
                PagerAdapter adapter = _pager.getAdapter();
                _dotPaint.setAlpha(255);
                float circlesWidth = adapter.getCount() * (_dotSize + _pad * 2);
                canvas.translate(getWidth() / 2 - circlesWidth / 2, 0f);

                if (_realPreviousPage != _pager.getCurrentItem()) {
                    _scaleFactor = 1f;
                    _realPreviousPage = _pager.getCurrentItem();
                }

                for (int i = 0; i < adapter.getCount(); i++) {
                    float targetFactor = 1.5f;
                    float targetFactor2 = 1f;
                    float increaseFactor = 0.05f;
                    if (i == _previousPage && i != _pager.getCurrentItem()) {
                        _scaleFactor2 = Tool.clampFloat(_scaleFactor2 - increaseFactor, targetFactor2, targetFactor);
                        Tool.print(_scaleFactor2);
                        canvas.drawCircle(_dotSize / 2 + _pad + (_dotSize + _pad * 2) * i, (float) (getHeight() / 2), _scaleFactor2 * _dotSize / 2, _dotPaint);
                        if (_scaleFactor2 != targetFactor2)
                            invalidate();
                        else {
                            _scaleFactor2 = 1.5f;
                            _previousPage = -1;
                        }
                    } else if (_pager.getCurrentItem() == i) {
                        if (_previousPage == -1)
                            _previousPage = i;
                        _scaleFactor = Tool.clampFloat(_scaleFactor + increaseFactor, targetFactor2, targetFactor);
                        canvas.drawCircle(_dotSize / 2 + _pad + (_dotSize + _pad * 2) * i, (float) (getHeight() / 2), _scaleFactor * _dotSize / 2, _dotPaint);
                        if (_scaleFactor != targetFactor)
                            invalidate();
                    } else {
                        canvas.drawCircle(_dotSize / 2 + _pad + (_dotSize + _pad * 2) * i, (float) (getHeight() / 2), _dotSize / 2, _dotPaint);
                    }
                }
                break;
            }
            case Mode.LINES: {
                PagerAdapter adapter = _pager.getAdapter();

                float lineWidth = getWidth() / adapter.getCount();
                float currentStartX = _scrollPagePosition * lineWidth;

                myX = currentStartX + _scrollOffset * lineWidth;

                if (myX % lineWidth != 0f)
                    invalidate();

                if (_alphaFade) {
                    _dotPaint.setAlpha(Tool.clampInt(_dotPaint.getAlpha() - 10, 0, 255));
                    if (_dotPaint.getAlpha() == 0)
                        _alphaFade = false;
                    invalidate();
                }

                if (_alphaShow) {
                    _dotPaint.setAlpha(Tool.clampInt(_dotPaint.getAlpha() + 10, 0, 255));
                    if (_dotPaint.getAlpha() == 255) {
                        _alphaShow = false;
                    }
                    invalidate();
                }

                canvas.drawLine(myX, getHeight() / 2, myX + lineWidth, getHeight() / 2, _dotPaint);
            }
            break;
        }
    }

    public PagerIndicator(Context context) {
        this(context, null);
    }

    public PagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        _pad = Tool.toPx(4);
        _dotPaint.setColor(Color.WHITE);
        _dotPaint.setStrokeWidth(Tool.toPx(4));
        _dotPaint.setAntiAlias(true);
        _mode = Setup.appSettings().getDesktopIndicatorMode();
        _delayShow = new Runnable() {
            @Override
            public void run() {
                _alphaFade = true;
                _alphaShow = false;
                invalidate();
            }
        };
        _currentPagerState = -1;
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
            _prePageCount = pager != null ? pager.getAdapter().getCount() : 0;
            if (pager != null) {
                pager.addOnPageChangeListener(this);
            }
        }
        invalidate();
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        PagerAdapter adapter = _pager.getAdapter();
        if (_prePageCount != adapter.getCount()) {
            _prePageCount = _pager.getAdapter().getCount();
        }
        _scrollOffset = positionOffset;
        _scrollPagePosition = position;
        invalidate();
    }

    public void onPageSelected(int position) {
    }

    public final void showNow() {
        removeCallbacks(_delayShow);
        _alphaShow = true;
        _alphaFade = false;
        invalidate();
    }

    public final void hideDelay() {
        postDelayed(_delayShow, 500);
    }

    public void onPageScrollStateChanged(int state) {
        _currentPagerState = state;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        _dotSize = getHeight() / 2;
        super.onLayout(changed, left, top, right, bottom);
    }
}
