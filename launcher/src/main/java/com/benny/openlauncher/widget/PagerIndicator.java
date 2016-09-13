package com.benny.openlauncher.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.benny.openlauncher.util.Tools;
import com.bennyv5.smoothviewpager.SmoothViewPager;

public class PagerIndicator extends View{

    private SmoothViewPager pager;

    private static float pad;

    private float dotSize;
    private Paint dotPaint;

    private float scaleFactor = 1;
    private float scaleFactor2 = 1.5f;

    private float targetFactor = 1.5f;
    private float targetFactor2 = 1f;

    private float increFactor = 0.05f;

    private int previousPage = -1;

    private int realPreviousPage;

    public PagerIndicator(Context context) {
        super(context);
        init();
    }

    public PagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        pad = Tools.convertDpToPixel(3,getContext());
        setWillNotDraw(false);
        dotPaint = new Paint();
        //TypedValue typedValue = new TypedValue();
        //getContext().getTheme().resolveAttribute(R.attr.colorViewBackground, typedValue, true);
        dotPaint.setColor(Color.WHITE);
        dotPaint.setAntiAlias(true);
    }

    public void setOutlinePaint(){
        dotPaint.setStyle(Paint.Style.STROKE);
        invalidate();
    }

    public void setFillPaint(){
        dotPaint.setStyle(Paint.Style.FILL);
        invalidate();
    }

    public void setColor(int c){
        dotPaint.setColor(c);
        invalidate();
    }

    public void setViewPager(final SmoothViewPager pager){
        if (pager == null)return;
        this.pager = pager;
        pager.addOnPageChangeListener(new SmoothViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                invalidate();
            }

            @Override
            public void onPageSelected(int position) {}

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        Tools.print(pager.getAdapter().getCount());
        getLayoutParams().width = Math.round(this.pager.getAdapter().getCount()*(dotSize+pad*2));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        dotSize = getHeight()-pad;
        if (pager != null) {
            getLayoutParams().width = Math.round(pager.getAdapter().getCount()*(dotSize+pad*2));
            if(realPreviousPage != pager.getCurrentItem()){
                scaleFactor = 1;
                realPreviousPage = pager.getCurrentItem();
            }
            for (int i = 0; i < pager.getAdapter().getCount(); i++) {
                if (i == previousPage && i != pager.getCurrentItem()){
                    scaleFactor2 = Tools.clampFloat(scaleFactor2-increFactor,targetFactor2,targetFactor);
                    Tools.print(scaleFactor2);
                    canvas.drawCircle(dotSize/2+pad + (dotSize + pad * 2) * i,  getHeight() / 2,(scaleFactor2 * dotSize) / 2, dotPaint);
                    if (scaleFactor2 != targetFactor2)
                        invalidate();
                    else{
                        scaleFactor2 = 1.5f;
                        previousPage = -1;
                    }
                }else
                if (pager.getCurrentItem() == i){
                    if(previousPage == -1)
                        previousPage = i;
                    scaleFactor = Tools.clampFloat(scaleFactor+increFactor,targetFactor2,targetFactor);
                    canvas.drawCircle(dotSize/2+pad + (dotSize + pad * 2) * i,  getHeight() / 2,(scaleFactor * dotSize) / 2, dotPaint);
                    if (scaleFactor != targetFactor)
                        invalidate();
                }
                else
                    canvas.drawCircle(dotSize/2+pad + (dotSize + pad * 2) * i, getHeight() / 2, dotSize / 2, dotPaint);
            }
        }
    }
}
