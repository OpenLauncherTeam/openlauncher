package com.benny.openlauncher.interfaces;

import android.graphics.drawable.Drawable;

public interface IconDrawer {
    void onIconAvailable(Drawable drawable, int index);

    void onIconCleared(Drawable placeholder, int index);
}