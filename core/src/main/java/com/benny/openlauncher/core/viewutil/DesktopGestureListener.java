package com.benny.openlauncher.core.viewutil;

import com.benny.openlauncher.core.widget.Desktop;

import in.championswimmer.sfg.lib.SimpleFingerGestures;

public class DesktopGestureListener implements SimpleFingerGestures.OnFingerGestureListener {

    public enum Type {
        SwipeUp,
        SwipeDown,
        SwipeLeft,
        SwipeRight,
        Pinch,
        Unpinch,
        DoubleTap
    }

    private final DesktopGestureCallback callback;
    private final Desktop desktop;

    public DesktopGestureListener(Desktop desktop, DesktopGestureCallback callback) {
        this.desktop = desktop;
        this.callback = callback;
    }

    @Override
    public boolean onSwipeUp(int i, long l, double v) {
        return callback.onDrawerGesture(desktop, Type.SwipeUp);
    }

    @Override
    public boolean onSwipeDown(int i, long l, double v) {
        return callback.onDrawerGesture(desktop, Type.SwipeDown);
    }

    @Override
    public boolean onSwipeLeft(int i, long l, double v) {
        return callback.onDrawerGesture(desktop, Type.SwipeLeft);
    }

    @Override
    public boolean onSwipeRight(int i, long l, double v) {
        return callback.onDrawerGesture(desktop, Type.SwipeRight);
    }

    @Override
    public boolean onPinch(int i, long l, double v) {
        return callback.onDrawerGesture(desktop, Type.Pinch);
    }

    @Override
    public boolean onUnpinch(int i, long l, double v) {
        return callback.onDrawerGesture(desktop, Type.Unpinch);
    }

    @Override
    public boolean onDoubleTap(int i) {
        return callback.onDrawerGesture(desktop, Type.DoubleTap);
    }

    public interface DesktopGestureCallback {
        boolean onDrawerGesture(Desktop desktop, Type event);
    }
}
