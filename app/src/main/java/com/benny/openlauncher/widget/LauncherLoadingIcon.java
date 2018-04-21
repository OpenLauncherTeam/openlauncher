package com.benny.openlauncher.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.benny.openlauncher.R;

public class LauncherLoadingIcon extends FrameLayout {
    private static final AccelerateDecelerateInterpolator _interpolator = new AccelerateDecelerateInterpolator();
    private static final Long ANIM_DURATION = 250L;
    private ImageView[] _ivs;
    private boolean _loading = false;

    private final Runnable ANIM_1 = new Runnable() {
        @Override
        public void run() {
            if (_ivs != null && _ivs[0] != null) {
                _ivs[0].setScaleX(0);
                _ivs[1].setScaleY(0);
                _ivs[2].setScaleX(0);
                _ivs[0].animate().setDuration(ANIM_DURATION).scaleX(1).alpha(1).withEndAction(ANIM_2).setInterpolator(_interpolator);
            }
        }
    };

    private final Runnable ANIM_2 = new Runnable() {
        @Override
        public void run() {
            if (_ivs != null && _ivs[1] != null) {
                _ivs[1].animate().setDuration(ANIM_DURATION).scaleY(1).alpha(1).withEndAction(ANIM_3).setInterpolator(_interpolator);
            }
        }
    };

    private final Runnable ANIM_3 = new Runnable() {
        @Override
        public void run() {
            if (_ivs != null && _ivs[2] != null) {
                _ivs[2].animate().setDuration(ANIM_DURATION).scaleX(1).alpha(1).withEndAction(ANIM_4).setInterpolator(_interpolator);
            }
        }
    };

    private final Runnable ANIM_4 = new Runnable() {
        @Override
        public void run() {
            _ivs[0].animate().setDuration(ANIM_DURATION).alpha(0).setInterpolator(_interpolator);
            _ivs[1].animate().setDuration(ANIM_DURATION).alpha(0).setInterpolator(_interpolator);
            if (_loading)
                _ivs[2].animate().setDuration(ANIM_DURATION).alpha(0).withEndAction(ANIM_1).setInterpolator(_interpolator);
            else
                _ivs[2].animate().setDuration(ANIM_DURATION).alpha(0).setInterpolator(_interpolator);
        }
    };

    public LauncherLoadingIcon(Context context) {
        this(context, null);
    }

    public LauncherLoadingIcon(Context context, AttributeSet attrs) {
        super(context, attrs);

        int[] res = {R.drawable.ol_loading_3, R.drawable.ol_loading_2, R.drawable.ol_loading_1};
        _ivs = new ImageView[3];
        for (int i = 0; i < _ivs.length; i++) {
            _ivs[i] = new ImageView(getContext());
            _ivs[i].setImageResource(res[i]);
            addView(_ivs[i]);
            _ivs[i].setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            _ivs[i].setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        _ivs[0].setScaleX(0);
        _ivs[1].setScaleY(0);
        _ivs[2].setScaleX(0);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        _ivs[0].setPivotX(getHeight());
        _ivs[0].setPivotY(getHeight());

        _ivs[1].setPivotY(getHeight());
        _ivs[1].setPivotX(0);

        _ivs[2].setPivotX(0);
        _ivs[2].setPivotY(0);
        super.onLayout(changed, left, top, right, bottom);
    }
}
