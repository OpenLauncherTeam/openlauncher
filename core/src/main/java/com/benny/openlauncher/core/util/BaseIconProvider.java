package com.benny.openlauncher.core.util;

import android.widget.TextView;

import com.benny.openlauncher.core.interfaces.IconDrawer;
import com.benny.openlauncher.core.interfaces.IconProvider;

public abstract class BaseIconProvider implements IconProvider {

    public void loadIconIntoIconDrawer(IconDrawer iconDrawer, int forceSize, int index) {
        loadIcon(IconTargetType.IconDrawer, forceSize, iconDrawer, index);
    }

    public void cancelLoad(IconDrawer iconDrawer) {
        cancelLoad(IconTargetType.IconDrawer, iconDrawer);
    }

    public void loadIconIntoTextView(TextView tv, int forceSize, int gravity) {
        loadIcon(IconTargetType.TextView, forceSize, tv, gravity);
    }

    public void cancelLoad(TextView tv) {
        cancelLoad(IconTargetType.TextView, tv);
    }
}
