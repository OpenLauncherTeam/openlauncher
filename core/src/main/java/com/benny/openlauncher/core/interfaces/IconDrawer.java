package com.benny.openlauncher.core.interfaces;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

public interface IconDrawer {
    void onIconAvailable(Drawable drawable);
}