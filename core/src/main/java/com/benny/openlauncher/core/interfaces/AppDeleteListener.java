package com.benny.openlauncher.core.interfaces;

import java.util.List;

public interface AppDeleteListener<A extends App> {
    /**
     * @param apps list of apps
     * @return true, if the listener should be removed
     */
    boolean onAppDeleted(List<A> apps);
}
