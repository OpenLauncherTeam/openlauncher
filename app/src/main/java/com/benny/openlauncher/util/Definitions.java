package com.benny.openlauncher.util;

public class Definitions {
    public static final int BUFFER_SIZE = 2048;
    public static final int INTENT_BACKUP = 5;
    public static final int INTENT_RESTORE = 3;

    public static final int ACTION_LAUNCHER = 8;

    // separates a list of integers
    public static final String INT_SEP = "#";

    public enum ItemPosition {
        Dock,
        Desktop
    }

    public enum ItemState {
        Hidden,
        Visible
    }

    // doesn't work reliably yet
    public static final boolean ENABLE_ITEM_TOUCH_LISTENER = false;
}
