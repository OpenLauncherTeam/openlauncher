package com.benny.openlauncher.util;

import android.widget.TextView;

import com.benny.openlauncher.interfaces.IconDrawer;
import com.benny.openlauncher.interfaces.IconProvider;

import kotlin.jvm.internal.Intrinsics;

public abstract class BaseIconProvider implements IconProvider {
    public final void loadIconIntoIconDrawer(IconDrawer iconDrawer, int forceSize, int index) {
        Intrinsics.checkParameterIsNotNull(iconDrawer, "iconDrawer");
        loadIcon(IconTargetType.IconDrawer, forceSize, iconDrawer, Integer.valueOf(index));
    }

    public final void cancelLoad(IconDrawer iconDrawer) {
        Intrinsics.checkParameterIsNotNull(iconDrawer, "iconDrawer");
        cancelLoad(IconTargetType.IconDrawer, iconDrawer);
    }

    public final void loadIconIntoTextView(TextView tv, int forceSize, int gravity) {
        Intrinsics.checkParameterIsNotNull(tv, "tv");
        loadIcon(IconTargetType.TextView, forceSize, tv, Integer.valueOf(gravity));
    }

    public final void cancelLoad(TextView tv) {
        Intrinsics.checkParameterIsNotNull(tv, "tv");
        cancelLoad(IconTargetType.TextView, tv);
    }
}