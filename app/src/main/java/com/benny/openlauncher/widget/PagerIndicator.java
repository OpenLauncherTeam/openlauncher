package com.benny.openlauncher.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.SmoothPagerAdapter;
import com.benny.openlauncher.widget.SmoothViewPager.OnPageChangeListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

import kotlin.jvm.internal.Intrinsics;

import static com.benny.openlauncher.widget.PagerIndicator.Mode.ARROW;

/* compiled from: PagerIndicator.kt */
public final class PagerIndicator extends View implements OnPageChangeListener {
    public static final Companion Companion = new Companion();
    private static float pad;
    private HashMap _findViewCache;
    private boolean alphaFade;
    private boolean alphaShow;
    private Paint arrowPaint = new Paint(1);
    private Path arrowPath;
    private final Runnable delayShow;
    private Paint dotPaint = new Paint(1);
    private float dotSize;
    private boolean hasTriggedAlphaShow;
    private int mCurrentPagerState;
    private int mode = Mode.NORMAL;
    private float myX;
    private SmoothViewPager pager;
    private int prePageCount;
    private int previousPage = -1;
    private int realPreviousPage;
    private float scaleFactor = 1.0f;
    private float scaleFactor2 = 1.5f;
    private float scrollOffset;
    private int scrollPagePosition;

    /* compiled from: PagerIndicator.kt */
    public static final class Companion {
        private Companion() {
        }

        private final float getPad() {
            return PagerIndicator.pad;
        }

        private final void setPad(float v) {
            PagerIndicator.pad = v;
        }
    }

    public static class Mode {
        public static final int NORMAL = 0;
        public static final int ARROW = 1;
    }

    protected void onDraw(Canvas canvas) {
        dotSize = getHeight() - pad * 1.25f;

        switch (mode) {
            case Mode.NORMAL: {
                if (pager != null) {

                    dotPaint.setAlpha(255);
                    float circlesWidth = pager.getAdapter().getCount() * (dotSize + pad * 2);
                    canvas.translate(getWidth() / 2 - circlesWidth / 2, 0f);

                    if (realPreviousPage != pager.getCurrentItem()) {
                        scaleFactor = 1f;
                        realPreviousPage = pager.getCurrentItem();
                    }

                    for (int i = 0; i < pager.getAdapter().getCount(); i++) {
                        float targetFactor = 1.5f;
                        float targetFactor2 = 1f;
                        float increaseFactor = 0.05f;
                        if (i == previousPage && i != pager.getCurrentItem()) {
                            scaleFactor2 = Tool.clampFloat(scaleFactor2 - increaseFactor, targetFactor2, targetFactor);
                            Tool.print(scaleFactor2);
                            canvas.drawCircle(dotSize / 2 + pad + (dotSize + pad * 2) * i, (float) (getHeight() / 2), scaleFactor2 * dotSize / 2, dotPaint);
                            if (scaleFactor2 != targetFactor2)
                                invalidate();
                            else {
                                scaleFactor2 = 1.5f;
                                previousPage = -1;
                            }
                        } else if (pager.getCurrentItem() == i) {
                            if (previousPage == -1)
                                previousPage = i;
                            scaleFactor = Tool.clampFloat(scaleFactor + increaseFactor, targetFactor2, targetFactor);
                            canvas.drawCircle(dotSize / 2 + pad + (dotSize + pad * 2) * i, (float) (getHeight() / 2), scaleFactor * dotSize / 2, dotPaint);
                            if (scaleFactor != targetFactor)
                                invalidate();
                        } else {
                            canvas.drawCircle(dotSize / 2 + pad + (dotSize + pad * 2) * i, (float) (getHeight() / 2), dotSize / 2, dotPaint);
                        }
                    }
                }
                break;
            }
            case ARROW: {

                if (pager != null) {
                    arrowPath.reset();
                    arrowPath.moveTo(getWidth() / 2 - dotSize * 1.5f, (float) (getHeight()) - dotSize / 3 - pad / 2);
                    arrowPath.lineTo((getWidth() / 2f), pad / 2);
                    arrowPath.
                            lineTo(getWidth() / 2 + dotSize * 1.5f, (float) (getHeight()) - dotSize / 3 - pad / 2);

                    canvas.drawPath(arrowPath, arrowPaint);

                    float lineWidth = getWidth() / pager.getAdapter().getCount();
                    float currentStartX = scrollPagePosition * lineWidth;

                    myX = currentStartX + scrollOffset * lineWidth;

                    if (myX % lineWidth != 0f)
                        invalidate();

                    if (alphaFade) {
                        dotPaint.setAlpha(Tool.clampInt(dotPaint.getAlpha() - 10, 0, 255));
                        if (dotPaint.getAlpha() == 0)
                            alphaFade = false;
                        invalidate();
                    }

                    if (alphaShow) {
                        dotPaint.setAlpha(Tool.clampInt(dotPaint.getAlpha() + 10, 0, 255));
                        if (dotPaint.getAlpha() == 255) {
                            alphaShow = false;
                        }
                        invalidate();
                    }

                    canvas.drawLine(myX, (float) getHeight(), myX + lineWidth, (float) getHeight(), dotPaint);
                }
            }
            break;
        }
    }

    public PagerIndicator(Context context) {
        super(context);
        Companion.setPad((float) Tool.toPx(3));
        setWillNotDraw(false);
        this.dotPaint.setColor(-1);
        this.dotPaint.setStrokeWidth((float) Tool.toPx(2));
        this.dotPaint.setAntiAlias(true);
        this.arrowPaint.setColor(-1);
        this.arrowPaint.setAntiAlias(true);
        this.arrowPaint.setStyle(Style.STROKE);
        this.arrowPaint.setStrokeWidth(Companion.getPad() / 1.5f);
        this.arrowPaint.setStrokeJoin(Join.ROUND);
        this.arrowPath = new Path();
        this.mode = Setup.appSettings().getDesktopIndicatorMode();
        this.delayShow = new PagerIndicato_delayShow(this);
        this.mCurrentPagerState = -1;
    }

    public PagerIndicator(@NotNull Context context, @NotNull AttributeSet attrs) {
        super(context, attrs);
        Companion.setPad((float) Tool.toPx(3));
        setWillNotDraw(false);
        this.dotPaint.setColor(-1);
        this.dotPaint.setStrokeWidth((float) Tool.toPx(2));
        this.dotPaint.setAntiAlias(true);
        this.arrowPaint.setColor(-1);
        this.arrowPaint.setAntiAlias(true);
        this.arrowPaint.setStyle(Style.STROKE);
        this.arrowPaint.setStrokeWidth(Companion.getPad() / 1.5f);
        this.arrowPaint.setStrokeJoin(Join.ROUND);
        this.arrowPath = new Path();
        this.mode = Setup.appSettings().getDesktopIndicatorMode();
        this.delayShow = new PagerIndicato_delayShow(this);
        this.mCurrentPagerState = -1;
    }

    final class PagerIndicato_delayShow implements Runnable {
        final PagerIndicator _pagerIndicator;

        PagerIndicato_delayShow(PagerIndicator pagerIndicator) {
            _pagerIndicator = pagerIndicator;
        }

        public final void run() {
            _pagerIndicator.alphaFade = true;
            _pagerIndicator.alphaShow = false;
            _pagerIndicator.invalidate();
        }
    }

    public final void setMode(int mode) {
        this.mode = mode;
        invalidate();
    }


    public final void setViewPager(@Nullable SmoothViewPager pager) {
        if (pager == null) {
            if (this.pager != null) {
                SmoothViewPager smoothViewPager = this.pager;
                if (smoothViewPager == null) {
                    Intrinsics.throwNpe();
                }
                smoothViewPager.removeOnPageChangeListener(this);
                this.pager = null;
                invalidate();
            }
            return;
        }
        this.pager = pager;
        SmoothPagerAdapter adapter = pager.getAdapter();
        this.prePageCount = adapter.getCount();
        pager.addOnPageChangeListener(this);
        adapter = pager.getAdapter();
        Tool.print(Integer.valueOf(adapter.getCount()));
        invalidate();
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        int i = this.prePageCount;
        SmoothViewPager smoothViewPager = this.pager;
        if (smoothViewPager == null) {
            Intrinsics.throwNpe();
        }
        SmoothPagerAdapter adapter = smoothViewPager.getAdapter();
        if (i != adapter.getCount()) {
            SmoothViewPager smoothViewPager2 = this.pager;
            if (smoothViewPager2 == null) {
                Intrinsics.throwNpe();
            }
            SmoothPagerAdapter adapter2 = smoothViewPager2.getAdapter();
            this.prePageCount = adapter2.getCount();
        }
        this.scrollOffset = positionOffset;
        this.scrollPagePosition = position;
        invalidate();
    }

    public void onPageSelected(int position) {
    }

    public final void showNow() {
        removeCallbacks(this.delayShow);
        this.alphaShow = true;
        this.alphaFade = false;
        invalidate();
    }

    public final void hideDelay() {
        postDelayed(this.delayShow, 500);
    }

    public void onPageScrollStateChanged(int state) {
        this.mCurrentPagerState = state;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this.dotSize = ((float) getHeight()) - (Companion.getPad() * 1.25f);
        super.onLayout(changed, left, top, right, bottom);
    }
}