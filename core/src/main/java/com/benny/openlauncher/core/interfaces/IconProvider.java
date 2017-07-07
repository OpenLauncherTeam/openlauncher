package com.benny.openlauncher.core.interfaces;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

public interface IconProvider {
    void displayIcon(ImageView iv, int forceSize);
    void displayCompoundIcon(TextView tv, int gravity, int forceSize);
    Drawable getDrawable(int forceSize);
    Bitmap getBitmap(int forceSize);

    // temp. function, GroupIconDrawable will be optimised to support image loading via any external library like glide soon
    boolean isGroupIconDrawable();
}