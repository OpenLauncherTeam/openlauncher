package com.benny.openlauncher.util

import android.widget.TextView

import com.benny.openlauncher.interfaces.IconDrawer
import com.benny.openlauncher.interfaces.IconProvider

abstract class BaseIconProvider : IconProvider {

    fun loadIconIntoIconDrawer(iconDrawer: IconDrawer, forceSize: Int, index: Int) {
        loadIcon(IconProvider.IconTargetType.IconDrawer, forceSize, iconDrawer, index)
    }

    fun cancelLoad(iconDrawer: IconDrawer) {
        cancelLoad(IconProvider.IconTargetType.IconDrawer, iconDrawer)
    }

    fun loadIconIntoTextView(tv: TextView, forceSize: Int, gravity: Int) {
        loadIcon(IconProvider.IconTargetType.TextView, forceSize, tv, gravity)
    }

    fun cancelLoad(tv: TextView) {
        cancelLoad(IconProvider.IconTargetType.TextView, tv)
    }
}
