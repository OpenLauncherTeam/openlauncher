package com.benny.openlauncher.core.interfaces;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

import com.benny.openlauncher.core.util.Definitions;

public interface IconProvider {

    enum IconTargetType {
        ImageView,
        TextView,
        IconDrawer
    }

    void loadIcon(IconTargetType type, int forceSize, Object target, Object... args);
    void cancelLoad(IconTargetType type, Object target);

    // temp. function, GroupIconDrawable will be optimised to support image loading via any external library like glide soon
    // otherwise, those two functions are in here for simple synchronous loading and app code compatibility
    boolean isGroupIconDrawable();
    Drawable getDrawableSynchronously(int forceSize);
}