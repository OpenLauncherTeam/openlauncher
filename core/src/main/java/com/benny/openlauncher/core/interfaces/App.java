package com.benny.openlauncher.core.interfaces;

public interface App {
    String getLabel();

    String getPackageName();

    String getClassName();

    IconProvider getIconProvider();
}
