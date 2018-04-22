package com.benny.openlauncher.interfaces;

import com.benny.openlauncher.util.App;

import java.util.List;

public interface AppUpdateListener {

    /**
     * @param apps list of apps
     * @return true, if the listener should be removed
     */
    boolean onAppUpdated(List<App> apps);
}
