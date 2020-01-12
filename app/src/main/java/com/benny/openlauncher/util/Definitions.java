package com.benny.openlauncher.util;

public class Definitions {
    public static final int BUFFER_SIZE = 2048;
    public static final int INTENT_BACKUP = 5;
    public static final int INTENT_RESTORE = 3;
    public static final int ACTION_LAUNCHER = 8;

    // separates a list of integers
    public static final String DELIMITER = "#";

    // DO NOT REARRANGE
    // enum ordinal used for db
    public enum ItemPosition {
        Dock,
        Desktop,
        Group
    }

    public enum ItemState {
        Hidden,
        Visible
    }

    public enum WallpaperScroll {
        Normal,
        Inverse,
        Off
    }
}
