package com.benny.openlauncher.core.interfaces;

public interface AppDeleteListener<A extends App> {
    void onAppDeleted(A app);
}
