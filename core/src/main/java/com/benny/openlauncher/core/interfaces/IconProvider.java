package com.benny.openlauncher.core.interfaces;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

import com.benny.openlauncher.core.model.Item;

public interface IconProvider {
    void displayIcon(ImageView iv, int forceSize);
    void displayCompoundIcon(TextView tv, int gravity, int forceSize);
    void loadDrawable(IconDrawer iconDrawer, int forceSize);

    void cancelLoad(ImageView iv);
    void cancelLoad(TextView tv);
    void cancelLoadDrawable();

    // temp. function, GroupIconDrawable will be optimised to support image loading via any external library like glide soon
    // otherwise, those two functions are in here for simple synchronous loading and app code compatibility
    boolean isGroupIconDrawable();
    Drawable getDrawableSynchronously(int forceSize);
}