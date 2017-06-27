package com.benny.openlauncher.core.interfaces;

import java.util.List;

/**
 * Created by Michael on 25.06.2017.
 */

public interface AppUpdateListener<T extends App> {

    /**
     * @param apps list of apps
     * @return true, if the listener should be removed
     */
    boolean onAppUpdated(List<T> apps);
}
