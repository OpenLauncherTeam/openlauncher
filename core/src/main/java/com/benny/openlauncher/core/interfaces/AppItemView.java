package com.benny.openlauncher.core.interfaces;

import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Created by Michael on 25.06.2017.
 */

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
