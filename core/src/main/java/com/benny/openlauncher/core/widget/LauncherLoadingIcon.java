package com.benny.openlauncher.core.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.benny.openlauncher.core.R;

public class LauncherLoadingIcon extends FrameLayout {
    private static final AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
    private static final Long ANIM_DURATION = 250L;
    private ImageView[] iv;
    private boolean loading = false;

    private final Runnable ANIM_1 = new Runnable() {
        @Override
        public void run() {
            if (iv != null && iv[0] != null) {
                iv[0].setScaleX(0);
                iv[1].setScaleY(0);
                iv[2].setScaleX(0);
                iv[0].animate().setDuration(ANIM_DURATION).scaleX(1).alpha(1).withEndAction(ANIM_2).setInterpolator(interpolator);
            }
        }
    };

    private final Runnable ANIM_2 = new Runnable() {
        @Override
        public void run() {
            if (iv != null && iv[1] != null) {
                iv[1].animate().setDuration(ANIM_DURATION).scaleY(1).alpha(1).withEndAction(ANIM_3).setInterpolator(interpolator);
            }
        }
    };

    private final Runnable ANIM_3 = new Runnable() {
        @Override
        public void run() {
            if (iv != null && iv[2] != null) {
                iv[2].animate().setDuration(ANIM_DURATION).scaleX(1).alpha(1).withEndAction(ANIM_4).setInterpolator(interpolator);
            }
        }
    };

    private final Runnable ANIM_4 = new Runnable() {
        @Override
        public void run() {
            iv[0].animate().setDuration(ANIM_DURATION).alpha(0).setInterpolator(interpolator);
            iv[1].animate().setDuration(ANIM_DURATION).alpha(0).setInterpolator(interpolator);
            if (loading)
                iv[2].animate().setDuration(ANIM_DURATION).alpha(0).withEndAction(ANIM_1).setInterpolator(interpolator);
            else
                iv[2].animate().setDuration(ANIM_DURATION).alpha(0).setInterpolator(interpolator);
        }
    };

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        if (loading != this.loading && loading) {
            removeCallbacks(null);
            post(ANIM_1);
        }
        this.loading = loading;
    }


    public LauncherLoadingIcon(Context context) {
        this(context, null);
    }

    public LauncherLoadingIcon(Context context, AttributeSet attrs) {
        super(context, attrs);

        int[] res = {R.drawable.ol_loading_3, R.drawable.ol_loading_2, R.drawable.ol_loading_1};
        iv = new ImageView[3];
        for (int i = 0; i < iv.length; i++) {
            iv[i] = new ImageView(getContext());
            iv[i].setImageResource(res[i]);
            addView(iv[i]);
            iv[i].setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            iv[i].setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        iv[0].setScaleX(0);
        iv[1].setScaleY(0);
        iv[2].setScaleX(0);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        iv[0].setPivotX(getHeight());
        iv[0].setPivotY(getHeight());

        iv[1].setPivotY(getHeight());
        iv[1].setPivotX(0);

        iv[2].setPivotX(0);
        iv[2].setPivotY(0);
        super.onLayout(changed, left, top, right, bottom);
    }
}
