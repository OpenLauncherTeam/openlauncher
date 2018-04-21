package com.benny.openlauncher;

import android.app.Application;

public class AppObject extends Application {
    private static AppObject _instance;

    public static AppObject get() {
        return _instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
    }
}