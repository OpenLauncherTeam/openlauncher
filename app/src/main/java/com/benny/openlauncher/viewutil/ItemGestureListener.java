package com.benny.openlauncher.viewutil;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.Item;

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

    private final ItemGestureCallback _callback;
    private final Item _item;
    private GestureDetectorCompat _detector;

    public ItemGestureListener(Context context, Item item, ItemGestureCallback callback) {
        _detector = new GestureDetectorCompat(context, this);
        _detector.setOnDoubleTapListener(this);
        _item = item;
        _callback = callback;
    }

    public boolean onTouchEvent(MotionEvent event) {
        return _detector.onTouchEvent(event);
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
                        result = _callback.onItemGesture(_item, Type.SwipeRight);
                    } else {
                        result = _callback.onItemGesture(_item, Type.SwipeLeft);
                    }
                }
            } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0) {
                    result = _callback.onItemGesture(_item, Type.SwipeDown);
                } else {
                    result = _callback.onItemGesture(_item, Type.SwipeUp);
                }
            }
        } catch (Exception exception) {
            Setup.logger().log(this, Log.ERROR, null, exception.getMessage());
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
        return _callback.onItemGesture(_item, Type.Click);
    }

    public interface ItemGestureCallback {
        boolean onItemGesture(Item item, Type event);
    }
}