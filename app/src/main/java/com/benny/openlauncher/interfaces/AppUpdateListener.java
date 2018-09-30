package com.benny.openlauncher.interfaces;

import com.benny.openlauncher.model.App;

import java.util.List;

public interface AppUpdateListener {
    boolean onAppUpdated(List<App> apps);
}
