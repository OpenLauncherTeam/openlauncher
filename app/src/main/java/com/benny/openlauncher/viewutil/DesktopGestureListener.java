package com.benny.openlauncher.viewutil;

import com.benny.openlauncher.widget.Desktop;

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

    private final DesktopGestureCallback _callback;
    private final Desktop _desktop;

    public DesktopGestureListener(Desktop desktop, DesktopGestureCallback callback) {
        _desktop = desktop;
        _callback = callback;
    }

    @Override
    public boolean onSwipeUp(int i, long l, double v) {
        return _callback.onDrawerGesture(_desktop, Type.SwipeUp);
    }

    @Override
    public boolean onSwipeDown(int i, long l, double v) {
        return _callback.onDrawerGesture(_desktop, Type.SwipeDown);
    }

    @Override
    public boolean onSwipeLeft(int i, long l, double v) {
        return _callback.onDrawerGesture(_desktop, Type.SwipeLeft);
    }

    @Override
    public boolean onSwipeRight(int i, long l, double v) {
        return _callback.onDrawerGesture(_desktop, Type.SwipeRight);
    }

    @Override
    public boolean onPinch(int i, long l, double v) {
        return _callback.onDrawerGesture(_desktop, Type.Pinch);
    }

    @Override
    public boolean onUnpinch(int i, long l, double v) {
        return _callback.onDrawerGesture(_desktop, Type.Unpinch);
    }

    @Override
    public boolean onDoubleTap(int i) {
        return _callback.onDrawerGesture(_desktop, Type.DoubleTap);
    }

    public interface DesktopGestureCallback {
        boolean onDrawerGesture(Desktop desktop, Type event);
    }
}
