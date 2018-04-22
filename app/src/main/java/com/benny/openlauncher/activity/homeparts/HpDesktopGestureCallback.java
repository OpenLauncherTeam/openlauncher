package com.benny.openlauncher.activity.homeparts;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.benny.openlauncher.R;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.LauncherAction;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DesktopGestureListener;
import com.benny.openlauncher.widget.Desktop;

public class HpDesktopGestureCallback implements DesktopGestureListener.DesktopGestureCallback {
    private AppSettings _appSettings;

    HpDesktopGestureCallback(AppSettings appSettings) {
        _appSettings = appSettings;
    }

    @Override
    public boolean onDrawerGesture(Desktop desktop, DesktopGestureListener.Type event) {
        Context context = _appSettings.getContext();
        PackageManager packageManager = context.getPackageManager();
        int gestureid;
        LauncherAction.ActionItem gesture = null;
        switch (event) {
            case SwipeUp: {
                gestureid = _appSettings.getGestureSwipeUp();
                if (gestureid != 0) {
                    gesture = LauncherAction.getActionItem(gestureid - 1);
                    if (gestureid == 9) {
                        gesture._extraData = new Intent(packageManager.getLaunchIntentForPackage(_appSettings.getString(context.getString(R.string.pref_key__gesture_swipe_up) + "__", "")));
                    }

                }
                break;
            }
            case SwipeDown: {
                gestureid = _appSettings.getGestureSwipeDown();
                if (gestureid != 0) {
                    gesture = LauncherAction.getActionItem(gestureid - 1);
                    if (gestureid == 9) {
                        gesture._extraData = new Intent(packageManager.getLaunchIntentForPackage(_appSettings.getString(context.getString(R.string.pref_key__gesture_swipe_down) + "__", "")));
                    }
                }
                break;
            }
            case SwipeLeft:
            case SwipeRight: {
                break;
            }
            case Pinch: {
                gestureid = _appSettings.getGesturePinch();
                if (gestureid != 0) {
                    gesture = LauncherAction.getActionItem(gestureid - 1);

                    if (gestureid == 9) {
                        gesture._extraData = new Intent(packageManager.getLaunchIntentForPackage(_appSettings.getString(context.getString(R.string.pref_key__gesture_pinch) + "__", "")));
                    }
                }
                break;
            }
            case Unpinch: {
                gestureid = _appSettings.getGestureUnpinch();
                if (gestureid != 0) {
                    gesture = LauncherAction.getActionItem(gestureid - 1);
                    if (gestureid == 9) {
                        gesture._extraData = new Intent(packageManager.getLaunchIntentForPackage(_appSettings.getString(context.getString(R.string.pref_key__gesture_unpinch) + "__", "")));
                    }
                }
                break;
            }
            case DoubleTap: {
                gestureid = _appSettings.getGestureDoubleTap();
                if (gestureid != 0) {
                    gesture = LauncherAction.getActionItem(gestureid - 1);
                    if (gestureid == 9) {
                        gesture._extraData = new Intent(packageManager.getLaunchIntentForPackage(_appSettings.getString(context.getString(R.string.pref_key__gesture_double_tap) + "__", "")));
                    }
                }
                break;
            }
            default: {
                throw new RuntimeException("Type not handled!");
            }
        }
        if (gesture != null) {
            if (_appSettings.isGestureFeedback()) {
                Tool.vibrate(desktop);
            }
            LauncherAction.RunAction(gesture, desktop.getContext());
            return true;
        }
        return false;
    }
}

