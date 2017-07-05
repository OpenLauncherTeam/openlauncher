package com.benny.openlauncher.core.interfaces;

import android.graphics.drawable.Drawable;
import android.view.View;

public interface AppItemView {
    View getView();

    Drawable getIcon();

    void setIcon(Drawable icon);

    boolean getShowLabel();

    float getIconSize();

    interface LongPressCallBack {
        boolean readyForDrag(View view);

        void afterDrag(View view);
    }
}
