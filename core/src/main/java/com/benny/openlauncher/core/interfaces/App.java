package com.benny.openlauncher.core.interfaces;

import android.graphics.drawable.Drawable;

/**
 * Created by Michael on 25.06.2017.
 */

public interface App {
    String getLabel();
    String getPackageName();
    String getClassName();
    Drawable getIcon();
}
