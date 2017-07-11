package com.benny.openlauncher.core.interfaces;

import com.benny.openlauncher.core.util.BaseIconProvider;

public interface App {
    String getLabel();

    String getPackageName();

    String getClassName();

    <T extends BaseIconProvider> T getIconProvider();
}
