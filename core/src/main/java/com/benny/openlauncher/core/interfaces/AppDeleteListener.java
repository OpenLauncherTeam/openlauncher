package com.benny.openlauncher.core.interfaces;

/**
 * Created by Michael on 25.06.2017.
 */

public interface AppDeleteListener<T extends App> {
    void onAppDeleted(T app);
}
