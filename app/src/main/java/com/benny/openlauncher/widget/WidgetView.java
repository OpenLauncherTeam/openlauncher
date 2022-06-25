package com.benny.openlauncher.widget;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.view.MotionEvent;

public class WidgetView extends AppWidgetHostView {
    private OnTouchListener _onTouchListener;
    private OnLongClickListener _longClick;
    private long _down;

    public WidgetView(Context context) {
        super(context);
        setLongClickable(true);
    }

    @Override
    public void setOnTouchListener(OnTouchListener onTouchListener) {
        _onTouchListener = onTouchListener;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        _longClick = l;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (_onTouchListener != null) {
            _onTouchListener.onTouch(this, ev);
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                _down = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                long delta = System.currentTimeMillis() - _down;
                if (delta > 300L) {
                    _longClick.onLongClick(this);
                }
                break;
        }

        return false;
    }
}
