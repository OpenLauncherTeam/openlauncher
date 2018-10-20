package com.benny.openlauncher.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import com.benny.openlauncher.activity.HomeActivity;

public class InsetView extends View {
    public int topInset;
    public int bottomInset;

    public InsetView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }

    public void useTopInset() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = topInset;
        setLayoutParams(layoutParams);
    }

    public void useBottomInset() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = bottomInset;
        setLayoutParams(layoutParams);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= 20) {
            topInset = insets.getSystemWindowInsetTop();
            bottomInset = insets.getSystemWindowInsetBottom();
            HomeActivity._launcher.setInsets();
        }
        return insets;
    }
}
