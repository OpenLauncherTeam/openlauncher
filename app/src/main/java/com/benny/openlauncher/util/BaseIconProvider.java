package com.benny.openlauncher.util;

import android.widget.TextView;

import com.benny.openlauncher.interfaces.IconDrawer;
import com.benny.openlauncher.interfaces.IconProvider;

public abstract class BaseIconProvider implements IconProvider {
    public final void loadIconIntoIconDrawer(IconDrawer iconDrawer, int forceSize, int index) {

        loadIcon(IconTargetType.IconDrawer, forceSize, iconDrawer, Integer.valueOf(index));
    }

    public final void cancelLoad(IconDrawer iconDrawer) {

        cancelLoad(IconTargetType.IconDrawer, iconDrawer);
    }

    public final void loadIconIntoTextView(TextView tv, int forceSize, int gravity) {

        loadIcon(IconTargetType.TextView, forceSize, tv, Integer.valueOf(gravity));
    }

    public final void cancelLoad(TextView tv) {

        cancelLoad(IconTargetType.TextView, tv);
    }
}