package com.benny.openlauncher.core.viewutil;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.benny.openlauncher.core.manager.Setup;
import com.benny.openlauncher.core.model.Item;

public class ItemGestureListener extends GestureDetector.SimpleOnGestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    public enum Type {
        Click,
        SwipeUp,
        SwipeDown,
        SwipeLeft,
        SwipeRight,
//        DoubleTap,
//        LongPress
    }

    private static final int SWIPE_THRESHOLD = 30;
    private static final int SWIPE_VELOCITY_THRESHOLD = 20;

    private final ItemGestureCallback callback;
    private final Item item;
    private GestureDetectorCompat detector;

    public ItemGestureListener(Context context, Item item, ItemGestureCallback callback) {
        detector = new GestureDetectorCompat(context, this);
        detector.setOnDoubleTapListener(this);
        this.item = item;
        this.callback = callback;
    }

    public boolean onTouchEvent(MotionEvent event) {
        return detector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        boolean result = false;
        try {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        result = callback.onItemGesture(item, Type.SwipeRight);
                    } else {
                        result = callback.onItemGesture(item, Type.SwipeLeft);
                    }
                }
            } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0) {
                    result = callback.onItemGesture(item, Type.SwipeDown);
                } else {
                    result = callback.onItemGesture(item, Type.SwipeUp);
                }
            }
        } catch (Exception exception) {
            Setup.Companion.logger().log(this, Log.ERROR, null, exception.getMessage());
        }
        return result;
    }

//    @Override
//    public void onLongPress(MotionEvent event) {
//        callback.onItemGesture(item, Type.LongPress);
//    }
//
//    @Override
//    public boolean onDoubleTapEvent(MotionEvent event) {
//        return callback.onItemGesture(item, Type.DoubleTap);
//    }

    public boolean onSingleTapConfirmed(MotionEvent e) {
        return callback.onItemGesture(item, Type.Click);
    }

    public interface ItemGestureCallback {
        boolean onItemGesture(Item item, Type event);
    }
}