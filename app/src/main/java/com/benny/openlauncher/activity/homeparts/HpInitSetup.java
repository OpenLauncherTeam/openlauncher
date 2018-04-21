package com.benny.openlauncher.activity.homeparts;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.Log;

import com.benny.openlauncher.AppObject;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.BaseIconProvider;
import com.benny.openlauncher.util.DatabaseHelper;
import com.benny.openlauncher.util.SimpleIconProvider;
import com.benny.openlauncher.viewutil.DesktopGestureListener.DesktopGestureCallback;
import com.benny.openlauncher.viewutil.ItemGestureListener;
import com.benny.openlauncher.viewutil.ItemGestureListener.ItemGestureCallback;

import org.jetbrains.annotations.NotNull;

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

    public HpInitSetup(Home home) {
        _appSettings = AppSettings.get();
        _desktopGestureCallback = new HpDesktopGestureCallback(_appSettings);
        _dataManager = new DatabaseHelper(home);
        _appLoader = AppManager.getInstance(home);
        _eventHandler = new HpEventHandler();

        _logger = new Logger() {
            @Override
            public void log(Object source, int priority, String tag, String msg, Object... args) {
                Log.println(priority, tag, String.format(msg, args));
            }
        };

        _imageLoader = new ImageLoader() {
            @NotNull
            public BaseIconProvider createIconProvider(@Nullable Drawable drawable) {
                return new SimpleIconProvider(drawable);
            }

            @NotNull
            public BaseIconProvider createIconProvider(int icon) {
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

    @NotNull
    public Context getAppContext() {
        return AppObject.get();
    }

    @NotNull
    public AppSettings getAppSettings() {
        return _appSettings;
    }

    @NotNull
    public DesktopGestureCallback getDesktopGestureCallback() {
        return _desktopGestureCallback;
    }

    @NotNull
    public ItemGestureCallback getItemGestureCallback() {
        return _itemGestureCallback;
    }

    @NotNull
    public ImageLoader getImageLoader() {
        return _imageLoader;
    }

    @NotNull
    public DataManager getDataManager() {
        return _dataManager;
    }

    @NotNull
    public AppManager getAppLoader() {
        return _appLoader;
    }

    @NotNull
    public EventHandler getEventHandler() {
        return _eventHandler;
    }

    @NotNull
    public Logger getLogger() {
        return _logger;
    }
}