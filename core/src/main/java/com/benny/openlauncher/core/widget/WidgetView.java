package com.benny.openlauncher.core.widget;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.benny.openlauncher.core.activity.Home;
import com.benny.openlauncher.core.util.Tool;

public class WidgetView extends AppWidgetHostView {

    private static final int LONG_PRESS_TIMEOUT = 500;

    private final int THRESHOLD;
    private OnLongClickListener longClick;

    private boolean mHasPerformedLongPress;
    private float mLongPressDownX;
    private float mLongPressDownY;
    private CheckForLongPress mPendingCheckForLongPress;
    private OnTouchListener mOnTouchListener = null;

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

        if (mOnTouchListener != null && mOnTouchListener.onTouch(this, ev))
            return true;

        // Consume any touch events for ourselves after longpress is triggered
        if (mHasPerformedLongPress) {
            mHasPerformedLongPress = false;
            return true;
        }

        //L.d("onInterceptTouchEvent: ev = %s | x = %f  | y = %f", ev.getAction(), ev.getX(), ev.getY());

        // Watch for longpress events at this level to make sure
        // users can always pick up this widget
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mLongPressDownX = ev.getX();
                mLongPressDownY = ev.getY();
                postCheckForLongClick();
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mHasPerformedLongPress = false;
                if (mPendingCheckForLongPress != null) {
                    removeCallbacks(mPendingCheckForLongPress);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                float diffX = Math.abs(mLongPressDownX - ev.getX());
                float diffY = Math.abs(mLongPressDownY - ev.getY());
                //L.d("onInterceptTouchEvent: diffX = %f | diffY = %f | THRESHOLD = %d", diffX, diffY, THRESHOLD);
                if (diffX >= THRESHOLD || diffY >= THRESHOLD)
                {
                    mHasPerformedLongPress = false;
                    if (mPendingCheckForLongPress != null) {
                        removeCallbacks(mPendingCheckForLongPress);
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

        mHasPerformedLongPress = false;
        if (mPendingCheckForLongPress != null) {
            removeCallbacks(mPendingCheckForLongPress);
        }
    }

    @Override
    public int getDescendantFocusability() {
        return ViewGroup.FOCUS_BLOCK_DESCENDANTS;
    }

    private boolean onLongPress() {
        return longClick.onLongClick(WidgetView.this);
    }

    public void setCustomOnTouchListener(OnTouchListener onTouchListener) {
        mOnTouchListener = onTouchListener;
    }

    public OnTouchListener getCustomOnTouchListener() {
        return mOnTouchListener;
    }

    private void postCheckForLongClick() {
        mHasPerformedLongPress = false;
        if (mPendingCheckForLongPress == null) {
            mPendingCheckForLongPress = new CheckForLongPress();
        }
        mPendingCheckForLongPress.rememberWindowAttachCount();
        postDelayed(mPendingCheckForLongPress, LONG_PRESS_TIMEOUT);
    }

    class CheckForLongPress implements Runnable {
        private int mOriginalWindowAttachCount;

        public void run() {
            if (getParent() != null
                    //    hasWindowFocus()
                    && mOriginalWindowAttachCount == getWindowAttachCount()
                    && !mHasPerformedLongPress)
            {
                if (onLongPress()) {
                    mHasPerformedLongPress = true;
                }
            }
        }

        public void rememberWindowAttachCount() {
            mOriginalWindowAttachCount = getWindowAttachCount();
        }
    }
}