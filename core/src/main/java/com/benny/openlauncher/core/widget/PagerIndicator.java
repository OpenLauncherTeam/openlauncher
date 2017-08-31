package com.benny.openlauncher.core.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.benny.openlauncher.core.manager.Setup;
import com.benny.openlauncher.core.util.Tool;

import static com.benny.openlauncher.core.widget.PagerIndicator.Mode.ARROW;
import static com.benny.openlauncher.core.widget.PagerIndicator.Mode.NORMAL;

public class PagerIndicator extends View implements SmoothViewPager.OnPageChangeListener {

    private SmoothViewPager pager;

    private int mode = Mode.NORMAL;

    private static float pad;

    private float dotSize;
    private Paint dotPaint;

    private float scaleFactor = 1;
    private float scaleFactor2 = 1.5f;

    private int previousPage = -1;

    private int realPreviousPage;

    private Path arrowPath;
    private Paint arrowPaint;

    public PagerIndicator(Context context) {
        super(context);
        init();
    }

    public PagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        pad = Tool.dp2px(3, getContext());

        setWillNotDraw(false);
        dotPaint = new Paint();
        dotPaint.setColor(Color.WHITE);
        dotPaint.setAntiAlias(true);

        arrowPaint = new Paint();
        arrowPaint.setColor(Color.WHITE);
        arrowPaint.setAntiAlias(true);
        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setStrokeWidth(pad / 1.5f);
        arrowPaint.setStrokeJoin(Paint.Join.ROUND);

        arrowPath = new Path();

        mode = Setup.appSettings().getDesktopIndicatorMode();
    }

    public void setMode(int mode) {
        this.mode = mode;
        invalidate();
    }

    public void setOutlinePaint() {
        dotPaint.setStyle(Paint.Style.STROKE);
        invalidate();
    }

    public void setFillPaint() {
        dotPaint.setStyle(Paint.Style.FILL);
        invalidate();
    }

    public void setColor(int c) {
        dotPaint.setColor(c);
        invalidate();
    }

    public int prePageCount;

    public void setViewPager(final SmoothViewPager pager) {
        if (pager == null) {
            if (this.pager != null) {
                this.pager.removeOnPageChangeListener(this);
                this.pager = null;
                //getLayoutParams().width = 0;
                invalidate();
            }
            return;
        }
        this.pager = pager;
        prePageCount = pager.getAdapter().getCount();
        pager.addOnPageChangeListener(this);
        Tool.print(pager.getAdapter().getCount());
        //getLayoutParams().width = Math.round(this.pager.getAdapter().getCount() * (dotSize + pad * 2));
        invalidate();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (prePageCount != pager.getAdapter().getCount()) {
            //getLayoutParams().width = Math.round(pager.getAdapter().getCount() * (dotSize + pad * 2));
            prePageCount = pager.getAdapter().getCount();
        }
        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        dotSize = getHeight() - pad * 1.25f;
        //if (pager != null)
        //getLayoutParams().width = Math.round(this.pager.getAdapter().getCount() * (dotSize + pad * 2));
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        dotSize = getHeight() - pad * 1.25f;

        switch (mode) {
            case NORMAL:
                if (pager != null) {
                    //getLayoutParams().width = Math.round(pager.getAdapter().getCount() * (dotSize + pad * 2));
                    float circlesWidth = pager.getAdapter().getCount() * (dotSize + pad * 2);
                    canvas.translate(getWidth() / 2 - circlesWidth / 2, 0);
                    getWidth();
                    if (realPreviousPage != pager.getCurrentItem()) {
                        scaleFactor = 1;
                        realPreviousPage = pager.getCurrentItem();
                    }
                    for (int i = 0; i < pager.getAdapter().getCount(); i++) {
                        float targetFactor = 1.5f;
                        float targetFactor2 = 1f;
                        float increaseFactor = 0.05f;
                        if (i == previousPage && i != pager.getCurrentItem()) {
                            scaleFactor2 = Tool.clampFloat(scaleFactor2 - increaseFactor, targetFactor2, targetFactor);
                            Tool.print(scaleFactor2);
                            canvas.drawCircle(dotSize / 2 + pad + (dotSize + pad * 2) * i, getHeight() / 2, (scaleFactor2 * dotSize) / 2, dotPaint);
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
                            canvas.drawCircle(dotSize / 2 + pad + (dotSize + pad * 2) * i, getHeight() / 2, (scaleFactor * dotSize) / 2, dotPaint);
                            if (scaleFactor != targetFactor)
                                invalidate();
                        } else {
                            canvas.drawCircle(dotSize / 2 + pad + (dotSize + pad * 2) * i, getHeight() / 2, dotSize / 2, dotPaint);
                        }
                    }
                }
                break;
            case ARROW:
                arrowPath.reset();
                arrowPath.moveTo(getWidth() / 2 - dotSize, getHeight());
                arrowPath.lineTo(getWidth() / 2, pad);
                arrowPath.lineTo(getWidth() / 2 + dotSize, getHeight());

                canvas.drawPath(arrowPath, arrowPaint);
                break;
        }
    }

    public static class Mode {
        public static final int NORMAL = 0;
        public static final int ARROW = 1;
    }
}
