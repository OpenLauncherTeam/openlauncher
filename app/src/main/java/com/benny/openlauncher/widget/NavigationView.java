package com.benny.openlauncher.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import com.benny.openlauncher.activity.HomeActivity;

public class NavigationView extends View {
    public NavigationView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // same as status view but uses top inset
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            int inset = insets.getSystemWindowInsetBottom();
            if (inset != 0) {
                ViewGroup.LayoutParams layoutParams = getLayoutParams();
                layoutParams.height = inset;
                setLayoutParams(layoutParams);
                setVisibility(VISIBLE);
            }
        }
        return insets;
    }
}
