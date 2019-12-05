package com.benny.openlauncher.weather;

import android.os.SystemClock;
import android.view.View;

import com.benny.openlauncher.activity.HomeActivity;

/*
 * Support both Single and Double clicks in a Listener.
 */
public abstract class MultiClickListener implements View.OnClickListener {
    private static long CLICK_TIME = 500l;
    private Runnable _tapRunnable = null;

    @Override
    public void onClick(View v) {
        _onClick(v);
    }

    private synchronized void _onClick(View v) {
        // Need to delay this in case of a Double Tap in which case we do not want to have any side effects from the first tap.
        if (_tapRunnable == null) {
            _tapRunnable = new Runnable() {
                @Override
                public void run() {
                    onSingleClick(v);
                }
            };

            HomeActivity.Companion.getLauncher().getSearchBar().postDelayed(_tapRunnable, CLICK_TIME);
        } else {
            HomeActivity.Companion.getLauncher().getSearchBar().removeCallbacks(_tapRunnable);
            onDoubleClick(v);
        }
    }

    public void onSingleClick(View v) {
        HomeActivity.Companion.getLauncher().getSearchBar().removeCallbacks(_tapRunnable);
        _tapRunnable = null;
    }

    public void onDoubleClick(View v) {
        HomeActivity.Companion.getLauncher().getSearchBar().removeCallbacks(_tapRunnable);
        _tapRunnable = null;
    }
}