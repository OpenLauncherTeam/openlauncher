package com.benny.openlauncher.core.widget;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;

import com.benny.openlauncher.core.activity.Home;

public class WidgetView extends AppWidgetHostView {

    private OnLongClickListener longClick;
    private long down = 0L;

    public WidgetView(Context context) {
        super(context);
    }

    public WidgetView(Context context, int animationIn, int animationOut) {
        super(context, animationIn, animationOut);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        this.longClick = l;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //(int) ev.getX();
        //(int) ev.getY();
        Home.touchX = getWidth() / 2;
        Home.touchY = getHeight() / 2;
        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN:
                down = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                boolean upVal = System.currentTimeMillis() - down > 500L;
                if (upVal) {
                    longClick.onLongClick(WidgetView.this);
                    down = 0;
                }
                break;
        }
        onTouchEvent(ev);
        return false;
    }
}