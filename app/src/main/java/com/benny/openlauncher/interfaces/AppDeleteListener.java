package com.benny.openlauncher.interfaces;

import com.benny.openlauncher.model.App;

import java.util.List;

public interface AppDeleteListener {
    boolean onAppDeleted(List<App> apps);
}
