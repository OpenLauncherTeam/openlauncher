package com.benny.launcheranim;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by BennyKok on 12/9/2016.
 */

public class LauncherLoadingIcon extends FrameLayout {

    private ImageView iv1, iv2, iv3;

    private static final Long ANIM_DURATION = 250L;

    private static final AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();

    private final Runnable ANIM_1 = new Runnable() {
        @Override
        public void run() {
            if (iv1 != null) {
                iv1.setScaleX(0);
                iv2.setScaleY(0);
                iv3.setScaleX(0);
                iv1.animate().setDuration(ANIM_DURATION).scaleX(1).alpha(1).withEndAction(ANIM_2).setInterpolator(interpolator);
            }
        }
    };

    private final Runnable ANIM_2 = new Runnable() {
        @Override
        public void run() {
            if (iv2 != null) {
                iv2.animate().setDuration(ANIM_DURATION).scaleY(1).alpha(1).withEndAction(ANIM_3).setInterpolator(interpolator);
            }
        }
    };

    private final Runnable ANIM_3 = new Runnable() {
        @Override
        public void run() {
            if (iv3 != null) {
                iv3.animate().setDuration(ANIM_DURATION).scaleX(1).alpha(1).withEndAction(ANIM_4).setInterpolator(interpolator);
            }
        }
    };

    private final Runnable ANIM_4 = new Runnable() {
        @Override
        public void run() {
            iv1.animate().setDuration(ANIM_DURATION).alpha(0).setInterpolator(interpolator);
            iv2.animate().setDuration(ANIM_DURATION).alpha(0).setInterpolator(interpolator);
            if (loading)
                iv3.animate().setDuration(ANIM_DURATION).alpha(0).withEndAction(ANIM_1).setInterpolator(interpolator);
            else
                iv3.animate().setDuration(ANIM_DURATION).alpha(0).setInterpolator(interpolator);
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

    private boolean loading = false;

    public LauncherLoadingIcon(Context context) {
        super(context);

        init();
    }

    public LauncherLoadingIcon(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private static final LayoutParams matchParentLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

    public void init() {
        iv1 = new ImageView(getContext());
        iv2 = new ImageView(getContext());
        iv3 = new ImageView(getContext());

        iv1.setImageResource(R.drawable.launcher_loading_3);
        iv2.setImageResource(R.drawable.launcher_loading_2);
        iv3.setImageResource(R.drawable.launcher_loading_1);

        addView(iv1);
        addView(iv2);
        addView(iv3);

        iv1.setLayoutParams(matchParentLp);
        iv2.setLayoutParams(matchParentLp);
        iv3.setLayoutParams(matchParentLp);

        iv1.setScaleX(0);
        iv2.setScaleY(0);
        iv3.setScaleX(0);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        iv1.setPivotX(getHeight());
        iv1.setPivotY(getHeight());

        iv2.setPivotY(getHeight());
        iv2.setPivotX(0);

        iv3.setPivotX(0);
        iv3.setPivotY(0);
        super.onLayout(changed, left, top, right, bottom);
    }
}
