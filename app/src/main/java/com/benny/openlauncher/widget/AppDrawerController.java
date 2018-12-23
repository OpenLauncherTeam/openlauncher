package com.benny.openlauncher.widget;

import android.animation.Animator;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsets;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.benny.openlauncher.R;
import com.benny.openlauncher.manager.Setup;

import net.gsantner.opoc.util.Callback;

import io.codetail.widget.RevealFrameLayout;

public class AppDrawerController extends RevealFrameLayout {
    public AppDrawerPage _drawerViewPage;
    public AppDrawerGrid _drawerViewGrid;
    public int _drawerMode;
    public boolean _isOpen = false;
    private Callback.a2<Boolean, Boolean> _appDrawerCallback;
    private Animator _appDrawerAnimator;
    private int _drawerAnimationTime;

    public static class Mode {
        public static final int LIST = 0;
        public static final int GRID = 1;
        public static final int PAGE = 2;
    }

    public AppDrawerController(Context context) {
        super(context);
    }

    public AppDrawerController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AppDrawerController(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setCallBack(Callback.a2<Boolean, Boolean> callBack) {
        _appDrawerCallback = callBack;
    }

    public View getDrawer() {
        switch (_drawerMode) {
            case Mode.GRID:
                return _drawerViewGrid;
            case Mode.PAGE:
            default:
                return _drawerViewPage;
        }
    }

    public void open(int cx, int cy) {
        if (_isOpen) return;
        _isOpen = true;

        _drawerAnimationTime = Setup.appSettings().getAnimationSpeed() * 10;
        _appDrawerAnimator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(getDrawer(), cx, cy, 0, Math.max(getWidth(), getHeight()));
        _appDrawerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        _appDrawerAnimator.setDuration(_drawerAnimationTime);
        _appDrawerAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator p1) {
                setVisibility(VISIBLE);
                _appDrawerCallback.callback(true, true);
            }

            @Override
            public void onAnimationEnd(Animator p1) {
                _appDrawerCallback.callback(true, false);
            }

            @Override
            public void onAnimationCancel(Animator p1) {
            }

            @Override
            public void onAnimationRepeat(Animator p1) {
            }
        });

        _appDrawerAnimator.start();
    }

    public void close(int cx, int cy) {
        if (!_isOpen) return;
        _isOpen = false;

        _drawerAnimationTime = Setup.appSettings().getAnimationSpeed() * 10;
        _appDrawerAnimator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(getDrawer(), cx, cy, Math.max(getWidth(), getHeight()), 0);
        _appDrawerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        _appDrawerAnimator.setDuration(_drawerAnimationTime);
        _appDrawerAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator p1) {
                _appDrawerCallback.callback(false, true);
            }

            @Override
            public void onAnimationEnd(Animator p1) {
                _appDrawerCallback.callback(false, false);
                setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator p1) {
            }

            @Override
            public void onAnimationRepeat(Animator p1) {
            }
        });

        _appDrawerAnimator.start();
    }

    public void reset() {
        switch (_drawerMode) {
            case Mode.GRID:
                _drawerViewGrid._recyclerView.scrollToPosition(0);
                break;
            case Mode.PAGE:
            default:
                _drawerViewPage.setCurrentItem(0, false);
                break;
        }
    }

    public void init() {
        if (isInEditMode()) return;
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        _drawerMode = Setup.appSettings().getDrawerStyle();
        setVisibility(GONE);
        setBackgroundColor(Setup.appSettings().getDrawerBackgroundColor());
        switch (_drawerMode) {
            case Mode.GRID:
                _drawerViewGrid = new AppDrawerGrid(getContext());
                addView(_drawerViewGrid);
                break;
            case Mode.PAGE:
            default:
                _drawerViewPage = (AppDrawerPage) layoutInflater.inflate(R.layout.view_app_drawer_page, this, false);
                addView(_drawerViewPage);
                PagerIndicator indicator = (PagerIndicator) layoutInflater.inflate(R.layout.view_drawer_indicator, this, false);
                addView(indicator);
                _drawerViewPage.withHome(indicator);
                break;
        }
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            setPadding(0, insets.getSystemWindowInsetTop(), 0, insets.getSystemWindowInsetBottom());
            return insets;
        }
        return insets;
    }
}
