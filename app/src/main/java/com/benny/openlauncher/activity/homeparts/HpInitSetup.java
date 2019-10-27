package com.benny.openlauncher.activity.homeparts;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.benny.openlauncher.AppObject;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.DatabaseHelper;
import com.benny.openlauncher.viewutil.DesktopGestureListener.DesktopGestureCallback;

public final class HpInitSetup extends Setup {
    private final AppManager _appLoader;
    private final DatabaseHelper _dataManager;
    private final HpGestureCallback _desktopGestureCallback;
    private final HpEventHandler _eventHandler;
    private final AppSettings _appSettings;

    public HpInitSetup(HomeActivity homeActivity) {
        _appSettings = AppSettings.get();
        _desktopGestureCallback = new HpGestureCallback(_appSettings);
        _dataManager = new DatabaseHelper(homeActivity);
        _appLoader = AppManager.getInstance(homeActivity);
        _eventHandler = new HpEventHandler();
    }

    @NonNull
    public Context getAppContext() {
        return AppObject.get();
    }

    @NonNull
    public AppSettings getAppSettings() {
        return _appSettings;
    }

    @NonNull
    public DesktopGestureCallback getDesktopGestureCallback() {
        return _desktopGestureCallback;
    }

    @NonNull
    public DatabaseHelper getDataManager() {
        return _dataManager;
    }

    @NonNull
    public AppManager getAppLoader() {
        return _appLoader;
    }

    @NonNull
    public EventHandler getEventHandler() {
        return _eventHandler;
    }
}