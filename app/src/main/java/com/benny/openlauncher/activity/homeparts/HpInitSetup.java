package com.benny.openlauncher.activity.homeparts;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.Log;

import com.benny.openlauncher.AppObject;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.DatabaseHelper;
import com.benny.openlauncher.util.SimpleIconProvider;
import com.benny.openlauncher.viewutil.DesktopGestureListener.DesktopGestureCallback;
import com.benny.openlauncher.viewutil.ItemGestureListener;
import com.benny.openlauncher.viewutil.ItemGestureListener.ItemGestureCallback;

import android.support.annotation.NonNull;

/* compiled from: Home.kt */
public final class HpInitSetup extends Setup {
    private final AppManager _appLoader;
    private final DatabaseHelper _dataManager;
    private final HpDesktopGestureCallback _desktopGestureCallback;
    private final HpEventHandler _eventHandler;
    private final ImageLoader _imageLoader;
    private final ItemGestureCallback _itemGestureCallback;
    private final Logger _logger;
    private final AppSettings _appSettings;

    public HpInitSetup(HomeActivity homeActivity) {
        _appSettings = AppSettings.get();
        _desktopGestureCallback = new HpDesktopGestureCallback(_appSettings);
        _dataManager = new DatabaseHelper(homeActivity);
        _appLoader = AppManager.getInstance(homeActivity);
        _eventHandler = new HpEventHandler();

        _logger = new Logger() {
            @Override
            public void log(Object source, int priority, String tag, String msg, Object... args) {
                Log.println(priority, tag, String.format(msg, args));
            }
        };

        _imageLoader = new ImageLoader() {
            @NonNull
            public SimpleIconProvider createIconProvider(@Nullable Drawable drawable) {
                return new SimpleIconProvider(drawable);
            }

            @NonNull
            public SimpleIconProvider createIconProvider(int icon) {
                return new SimpleIconProvider(icon);
            }
        };
        _itemGestureCallback = new ItemGestureCallback() {
            @Override
            public boolean onItemGesture(Item item, ItemGestureListener.Type event) {
                return false;
            }
        };
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
    public ItemGestureCallback getItemGestureCallback() {
        return _itemGestureCallback;
    }

    @NonNull
    public ImageLoader getImageLoader() {
        return _imageLoader;
    }

    @NonNull
    public DataManager getDataManager() {
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

    @NonNull
    public Logger getLogger() {
        return _logger;
    }
}