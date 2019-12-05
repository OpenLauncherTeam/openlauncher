package com.benny.openlauncher.weather;

import android.os.SystemClock;
import android.view.View;

/*
 * Support both Single and Double clicks in a Listener.
 */
public abstract class MultiClickListener implements View.OnClickListener {
    private static long CLICK_TIME = 500l;
    private long prevClickTime = 0;

    @Override
    public void onClick(View v) {
        _onClick(v);
    }

    private synchronized void _onClick(View v){
        long current = SystemClock.elapsedRealtime();
        if (current - prevClickTime > CLICK_TIME){
            onSingleClick(v);
            prevClickTime = SystemClock.elapsedRealtime();
        } else {
            onDoubleClick(v);
            prevClickTime = 0;
        }
    }

    public abstract void onSingleClick(View v);
    public abstract void onDoubleClick(View v);
}