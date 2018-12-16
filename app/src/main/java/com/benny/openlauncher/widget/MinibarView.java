package com.benny.openlauncher.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ListView;

import com.benny.openlauncher.util.Tool;

public class MinibarView extends ListView {
    private GestureDetector _gestureDetector;

    public void setOnSwipeRight(OnSwipeRight onSwipeRight) {
        _onSwipeRight = onSwipeRight;
    }

    private OnSwipeRight _onSwipeRight;

    public MinibarView(Context context) {
        super(context);
        init();
    }

    public MinibarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MinibarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (isInEditMode()) return;

        final float dis = Tool.dp2px(10);
        final float vDis = Tool.dp2px(30);
        _gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null) return true;
                int dx = (int) (e2.getX() - e1.getX());
                int dy = (int) (e2.getY() - e1.getY());
                if (Math.abs(dx) > dis && Math.abs(dy) < vDis) {
                    if (velocityX > 0) {
                        try {
                            int pos = pointToPosition((int) e1.getX(), (int) e1.getY());
                            if (pos != -1 && _onSwipeRight != null)
                                _onSwipeRight.onSwipe(pos, e1.getX(), e1.getY());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                return true;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        _gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    public interface OnSwipeRight {
        void onSwipe(int pos, float x, float y);
    }
}
