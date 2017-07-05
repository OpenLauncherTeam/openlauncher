package com.benny.openlauncher.core.interfaces;

import android.graphics.drawable.Drawable;

public interface App {
    String getLabel();

    String getPackageName();

    String getClassName();

    Drawable getIcon();
}
