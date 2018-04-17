package com.benny.openlauncher;

import android.app.Application;

public class AppObject extends Application {
    private static AppObject instance;

    public static AppObject get() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}