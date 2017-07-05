package com.benny.openlauncher.core.widget;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.benny.openlauncher.core.util.Tool;

public class WidgetView extends AppWidgetHostView {

    private static final int LONG_PRESS_TIMEOUT = 500;

    private final int THRESHOLD;
    private OnLongClickListener longClick;

    private boolean hasPerformedLongPress;
    private float longPressDownX;
    private float longPressDownY;
    private CheckForLongPress pendingCheckForLongPress;
    private OnTouchListener onTouchListener = null;

    public WidgetView(Context context) {
        super(context);
        THRESHOLD = Tool.dp2px(5, context);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        this.longClick = l;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (onTouchListener != null && onTouchListener.onTouch(this, ev)) {
            return true;
        }

        // Consume any touch events for ourselves after longpress is triggered
        if (hasPerformedLongPress) {
            hasPerformedLongPress = false;
            return true;
        }

        //L.d("onInterceptTouchEvent: ev = %s | x = %f  | y = %f", ev.getAction(), ev.getX(), ev.getY());

        // Watch for longpress events at this level to make sure
        // users can always pick up this widget
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                longPressDownX = ev.getX();
                longPressDownY = ev.getY();
                postCheckForLongClick();
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                hasPerformedLongPress = false;
                if (pendingCheckForLongPress != null) {
                    removeCallbacks(pendingCheckForLongPress);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                float diffX = Math.abs(longPressDownX - ev.getX());
                float diffY = Math.abs(longPressDownY - ev.getY());
                //L.d("onInterceptTouchEvent: diffX = %f | diffY = %f | THRESHOLD = %d", diffX, diffY, THRESHOLD);
                if (diffX >= THRESHOLD || diffY >= THRESHOLD) {
                    hasPerformedLongPress = false;
                    if (pendingCheckForLongPress != null) {
                        removeCallbacks(pendingCheckForLongPress);
                    }
                }
                break;
        }

        // Otherwise continue letting touch events fall through to children
        return false;
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();

        hasPerformedLongPress = false;
        if (pendingCheckForLongPress != null) {
            removeCallbacks(pendingCheckForLongPress);
        }
    }

    @Override
    public int getDescendantFocusability() {
        return ViewGroup.FOCUS_BLOCK_DESCENDANTS;
    }

    private boolean onLongPress() {
        return longClick.onLongClick(WidgetView.this);
    }

    @Override
    public final void setOnTouchListener(OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }

    private void postCheckForLongClick() {
        hasPerformedLongPress = false;
        if (pendingCheckForLongPress == null) {
            pendingCheckForLongPress = new CheckForLongPress();
        }
        pendingCheckForLongPress.rememberWindowAttachCount();
        postDelayed(pendingCheckForLongPress, LONG_PRESS_TIMEOUT);
    }

    class CheckForLongPress implements Runnable {
        private int mOriginalWindowAttachCount;

        public void run() {
            if (getParent() != null
                    //    hasWindowFocus()
                    && mOriginalWindowAttachCount == getWindowAttachCount()
                    && !hasPerformedLongPress)
            {
                if (onLongPress()) {
                    hasPerformedLongPress = true;
                }
            }
        }

        public void rememberWindowAttachCount() {
            mOriginalWindowAttachCount = getWindowAttachCount();
        }
    }
}