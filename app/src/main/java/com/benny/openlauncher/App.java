package com.benny.openlauncher;

import android.app.Application;

/**
 * Created by gregor on 07.05.17.
 */

public class App extends Application {
    private static App instance;

    public static App get() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}