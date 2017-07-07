package com.benny.openlauncher.core.util;

public class Definitions {
    public static final int ACTION_LAUNCHER = 8;

    // Default Dock size is 5, so the center is pos 2 => should be defined via settings!
    public static final int DOCK_DEFAULT_CENTER_ITEM_INDEX_X = 2;

    public static final int NO_SCALE = -1;

    // this string seperates a list of integers
    public static final String INT_SEP = "#";

    // don't change the order, index is saved into db!
    public enum ItemPosition {
        Dock,
        Desktop
    }

    // don't change the order, index is saved into db!
    public enum ItemState {
        Hidden,
        Visible
    }
}
