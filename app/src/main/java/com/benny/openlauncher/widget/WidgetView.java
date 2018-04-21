package com.benny.openlauncher.widget;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.view.MotionEvent;

//Important!! ReadMe
//We are now using the old method to detect widget long press, this fixed all the "randomly disappearing" behaviour of widgets
//However, you will need to move a bit to trigger the long press, when dragging. But this can be useful, as we can implement a
//popup menu of the widget when it was being pressed.
public class WidgetView extends AppWidgetHostView {
    private OnTouchListener _onTouchListener;
    private OnLongClickListener _longClick;
    private long _down;

    public WidgetView(Context context) {
        super(context);
    }

    @Override
    public void setOnTouchListener(OnTouchListener onTouchListener) {
        this._onTouchListener = onTouchListener;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        this._longClick = l;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (_onTouchListener != null)
            _onTouchListener.onTouch(this, ev);
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                _down = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                boolean upVal = System.currentTimeMillis() - _down > 300L;
                if (upVal) {
                    _longClick.onLongClick(WidgetView.this);
                }
                break;
        }
        return false;
    }
}

//Back up

//    private static final int LONG_PRESS_TIMEOUT = 500;
//
//    private final int THRESHOLD;
//    private OnLongClickListener longClick;
//
//    private boolean hasPerformedLongPress;
//    private float longPressDownX;
//    private float longPressDownY;
//    private CheckForLongPress pendingCheckForLongPress;
//    private OnTouchListener onTouchListener = null;
//
//    public WidgetView(Context context) {
//        super(context);
//        THRESHOLD = Tool.dp2px(5, context);
//    }
//
//    @Override
//    public void setOnLongClickListener(OnLongClickListener l) {
//        this.longClick = l;
//    }
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        if (onTouchListener != null && onTouchListener.onTouch(this, ev)) {
//            return true;
//        }
//
//        // Consume any touch events for ourselves after longpress is triggered
//        if (hasPerformedLongPress) {
//            hasPerformedLongPress = false;
//            return true;
//        }
//
//        //L.d("onInterceptTouchEvent: ev = %s | x = %f  | y = %f", ev.getAction(), ev.getX(), ev.getY());
//
//        // Watch for longpress events at this level to make sure
//        // users can always pick up this widget
//        switch (ev.getAction()) {
//            case MotionEvent.ACTION_DOWN: {
//                longPressDownX = ev.getX();
//                longPressDownY = ev.getY();
//                Tool.print("Shit, pressed");
//                postCheckForLongClick();
//                break;
//            }
//
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                cancelLongPressInternally();
//                break;
//
//            case MotionEvent.ACTION_MOVE:
//                float diffX = Math.abs(longPressDownX - ev.getX());
//                float diffY = Math.abs(longPressDownY - ev.getY());
//                //L.d("onInterceptTouchEvent: diffX = %f | diffY = %f | THRESHOLD = %d", diffX, diffY, THRESHOLD);
//                if (diffX >= THRESHOLD || diffY >= THRESHOLD) {
//                    cancelLongPressInternally();
//                }
//                break;
//        }
//
//        // Otherwise continue letting touch events fall through to children
//        return false;
//    }
//
//    @Override
//    public void cancelLongPress() {
//        super.cancelLongPress();
//
//        cancelLongPressInternally();
//    }
//
//    private void cancelLongPressInternally() {
//        Tool.print("Shit, cancel long press");
//        hasPerformedLongPress = false;
//        removeCallbacks(pendingCheckForLongPress);
//    }
//
//    @Override
//    public int getDescendantFocusability() {
//        return ViewGroup.FOCUS_BLOCK_DESCENDANTS;
//    }
//
//    private boolean onLongPress() {
//        return longClick.onLongClick(this);
//    }
//
//    @Override
//    public final void setOnTouchListener(OnTouchListener onTouchListener) {
//        this.onTouchListener = onTouchListener;
//    }
//
//    private void postCheckForLongClick() {
//        hasPerformedLongPress = false;
//        if (pendingCheckForLongPress == null) {
//            pendingCheckForLongPress = new CheckForLongPress();
//        }
//        pendingCheckForLongPress.rememberWindowAttachCount();
//        postDelayed(pendingCheckForLongPress, LONG_PRESS_TIMEOUT);
//        Tool.print("Shit, posted a delay");
//    }
//
//private class CheckForLongPress implements Runnable {
//    private int mOriginalWindowAttachCount;
//
//    public void run() {
//        if (getParent() != null && mOriginalWindowAttachCount == getWindowAttachCount() && !hasPerformedLongPress) {
//            Tool.print("Shit in Runnable");
//            if (onLongPress()) {
//                hasPerformedLongPress = true;
//            }
//        }
//    }
//
//    void rememberWindowAttachCount() {
//        mOriginalWindowAttachCount = getWindowAttachCount();
//    }
//}