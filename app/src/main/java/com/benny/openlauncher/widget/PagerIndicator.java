package com.benny.openlauncher.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.SmoothPagerAdapter;
import com.benny.openlauncher.widget.SmoothViewPager.OnPageChangeListener;

import kotlin.jvm.internal.Intrinsics;

import static com.benny.openlauncher.widget.PagerIndicator.Mode.ARROW;

public final class PagerIndicator extends View implements OnPageChangeListener {
    public static final Companion _companion = new Companion();
    private static float _pad;
    private boolean _alphaFade;
    private boolean _alphaShow;
    private Paint _arrowPaint = new Paint(1);
    private Path _arrowPath;
    private final Runnable _delayShow;
    private Paint _dotPaint = new Paint(1);
    private float _dotSize;
    private boolean _hasTriggedAlphaShow;
    private int _currentPagerState;
    private int _mode = Mode.NORMAL;
    private float myX;
    private SmoothViewPager _pager;
    private int _prePageCount;
    private int _previousPage = -1;
    private int _realPreviousPage;
    private float _scaleFactor = 1.0f;
    private float _scaleFactor2 = 1.5f;
    private float _scrollOffset;
    private int _scrollPagePosition;

    /* compiled from: PagerIndicator.kt */
    public static final class Companion {
        private Companion() {
        }

        private final float getPad() {
            return PagerIndicator._pad;
        }

        private final void setPad(float v) {
            PagerIndicator._pad = v;
        }
    }

    public static class Mode {
        public static final int NORMAL = 0;
        public static final int ARROW = 1;
    }

    protected void onDraw(Canvas canvas) {
        _dotSize = getHeight() - _pad * 1.25f;

        switch (_mode) {
            case Mode.NORMAL: {
                if (_pager != null) {

                    _dotPaint.setAlpha(255);
                    float circlesWidth = _pager.getAdapter().getCount() * (_dotSize + _pad * 2);
                    canvas.translate(getWidth() / 2 - circlesWidth / 2, 0f);

                    if (_realPreviousPage != _pager.getCurrentItem()) {
                        _scaleFactor = 1f;
                        _realPreviousPage = _pager.getCurrentItem();
                    }

                    for (int i = 0; i < _pager.getAdapter().getCount(); i++) {
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
                }
                break;
            }
            case ARROW: {

                if (_pager != null) {
                    _arrowPath.reset();
                    _arrowPath.moveTo(getWidth() / 2 - _dotSize * 1.5f, (float) (getHeight()) - _dotSize / 3 - _pad / 2);
                    _arrowPath.lineTo((getWidth() / 2f), _pad / 2);
                    _arrowPath.
                            lineTo(getWidth() / 2 + _dotSize * 1.5f, (float) (getHeight()) - _dotSize / 3 - _pad / 2);

                    canvas.drawPath(_arrowPath, _arrowPaint);

                    float lineWidth = getWidth() / _pager.getAdapter().getCount();
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

                    canvas.drawLine(myX, (float) getHeight(), myX + lineWidth, (float) getHeight(), _dotPaint);
                }
            }
            break;
        }
    }

    public PagerIndicator(Context context) {
        this(context,null);
    }

    public PagerIndicator(@NonNull Context context, @NonNull AttributeSet attrs) {
        super(context, attrs);
        _companion.setPad((float) Tool.toPx(3));
        setWillNotDraw(false);
        _dotPaint.setColor(-1);
        _dotPaint.setStrokeWidth((float) Tool.toPx(2));
        _dotPaint.setAntiAlias(true);
        _arrowPaint.setColor(-1);
        _arrowPaint.setAntiAlias(true);
        _arrowPaint.setStyle(Style.STROKE);
        _arrowPaint.setStrokeWidth(_companion.getPad() / 1.5f);
        _arrowPaint.setStrokeJoin(Join.ROUND);
        _arrowPath = new Path();
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


    public final void setViewPager(@Nullable SmoothViewPager pager) {
        if (pager == null) {
            if (_pager != null) {
                SmoothViewPager smoothViewPager = _pager;
                if (smoothViewPager == null) {
                    Intrinsics.throwNpe();
                }
                smoothViewPager.removeOnPageChangeListener(this);
                _pager = null;
                invalidate();
            }
            return;
        }
        _pager = pager;
        SmoothPagerAdapter adapter = pager.getAdapter();
        _prePageCount = adapter.getCount();
        pager.addOnPageChangeListener(this);
        adapter = pager.getAdapter();
        Tool.print(Integer.valueOf(adapter.getCount()));
        invalidate();
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        int i = _prePageCount;
        SmoothViewPager smoothViewPager = _pager;
        if (smoothViewPager == null) {
            Intrinsics.throwNpe();
        }
        SmoothPagerAdapter adapter = smoothViewPager.getAdapter();
        if (i != adapter.getCount()) {
            SmoothViewPager smoothViewPager2 = _pager;
            if (smoothViewPager2 == null) {
                Intrinsics.throwNpe();
            }
            SmoothPagerAdapter adapter2 = smoothViewPager2.getAdapter();
            _prePageCount = adapter2.getCount();
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
        _dotSize = ((float) getHeight()) - (_companion.getPad() * 1.25f);
        super.onLayout(changed, left, top, right, bottom);
    }
}