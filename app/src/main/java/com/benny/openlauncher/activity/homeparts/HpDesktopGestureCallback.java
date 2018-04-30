package com.benny.openlauncher.activity.homeparts;

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
        LauncherAction.ActionItem gesture = null;
        int gestureIndex;
        switch (event) {
            case SwipeUp:
                gestureIndex = _appSettings.getGestureSwipeUp();
                if (gestureIndex != 0) {
                    gesture = LauncherAction.getActionItem(gestureIndex - 1);
                }
                break;
            case SwipeDown:
                gestureIndex = _appSettings.getGestureSwipeDown();
                if (gestureIndex != 0) {
                    gesture = LauncherAction.getActionItem(gestureIndex - 1);
                }
                break;
            case SwipeLeft:
            case SwipeRight:
                break;
            case Pinch:
                gestureIndex = _appSettings.getGesturePinch();
                if (gestureIndex != 0) {
                    gesture = LauncherAction.getActionItem(gestureIndex - 1);
                }
                break;
            case Unpinch:
                gestureIndex = _appSettings.getGestureUnpinch();
                if (gestureIndex != 0) {
                    gesture = LauncherAction.getActionItem(gestureIndex - 1);
                }
                break;
            case DoubleTap:
                gestureIndex = _appSettings.getGestureDoubleTap();
                if (gestureIndex != 0) {
                    gesture = LauncherAction.getActionItem(gestureIndex - 1);
                }
                break;
            default:
                throw new RuntimeException("type not handled");
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
