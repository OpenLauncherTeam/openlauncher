package com.benny.openlauncher.core.interfaces;

public interface AppDeleteListener<T extends App> {
    void onAppDeleted(T app);
}
