package com.benny.openlauncher.core.manager;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.view.GestureDetectorCompat;
import android.view.DragEvent;

import com.benny.openlauncher.core.interfaces.App;
import com.benny.openlauncher.core.interfaces.AppDeleteListener;
import com.benny.openlauncher.core.interfaces.AppUpdateListener;
import com.benny.openlauncher.core.interfaces.DialogListener;
import com.benny.openlauncher.core.interfaces.IconProvider;
import com.benny.openlauncher.core.interfaces.SettingsManager;
import com.benny.openlauncher.core.model.Item;
import com.benny.openlauncher.core.util.BaseIconProvider;
import com.benny.openlauncher.core.util.Definitions;
import com.benny.openlauncher.core.util.SimpleIconProvider;
import com.benny.openlauncher.core.viewutil.DesktopGestureListener;
import com.benny.openlauncher.core.viewutil.ItemGestureListener;

import java.util.List;

public abstract class Setup<A extends App> {

    // ----------------
    // Class and singleton
    // ----------------

    private static Setup setup = null;

    public static boolean wasInitialised() {
        return setup != null;
    }

    public static void init(Setup setup) {
        Setup.setup = setup;
    }

    public static Setup get() {
        if (setup == null) {
            throw new RuntimeException("Setup has not been initialised!");
        }
        return setup;
    }

    // ----------------
    // Methods for convenience and shorter code
    // ----------------

    public static Context appContext() {
        return get().getAppContext();
    }

    public static SettingsManager appSettings() {
        return get().getAppSettings();
    }

    public static DesktopGestureListener.DesktopGestureCallback desktopGestureCallback() {
        return get().getDesktopGestureCallback();
    }

    public static ItemGestureListener.ItemGestureCallback itemGestureCallback() {
        return get().getItemGestureCallback();
    }

    public static <IL extends ImageLoader<A>, A extends App> IL imageLoader() {
        return (IL)get().getImageLoader();
    }

    public static DataManager dataManager() {
        return get().getDataManager();
    }

    public static <AL extends AppLoader<A>, A extends App> AL appLoader() {
        return (AL)get().getAppLoader();
    }

    public static EventHandler eventHandler() {
        return get().getEventHandler();
    }

    public static Logger logger() {
        return get().getLogger();
    }

    // ----------------
    // Settings
    // ----------------

    public abstract Context getAppContext();

    public abstract SettingsManager getAppSettings();

    public abstract DesktopGestureListener.DesktopGestureCallback getDesktopGestureCallback();

    public abstract ItemGestureListener.ItemGestureCallback getItemGestureCallback();

    public abstract ImageLoader<A> getImageLoader();

    public abstract DataManager getDataManager();

    public abstract AppLoader<A> getAppLoader();

    public abstract EventHandler getEventHandler();

    public abstract Logger getLogger();

    // ----------------
    // Interfaces
    // ----------------

    public interface ImageLoader<A extends App> {
        BaseIconProvider createIconProvider(Drawable drawable);
        BaseIconProvider createIconProvider(int icon);
    }

    public interface DataManager {
        void saveItem(Item item);
        void saveItem(Item item, int page, Definitions.ItemPosition desktop);
        void updateSate(Item item, Definitions.ItemState state);
        void deleteItem(Item item);
        Item getItem(int id);
        List<List<Item>> getDesktop();
        List<Item> getDock();
    }

    public interface AppLoader<A extends App> {
        void loadItems();
        List<A> getAllApps(Context context);
        A findItemApp(Item item);
        void onAppUpdated(Context p1, Intent p2);
        void addUpdateListener(AppUpdateListener<A> updateListener);
        void removeUpdateListener(AppUpdateListener<A> updateListener);
        void addDeleteListener(AppDeleteListener<A> deleteListener);
        void removeDeleteListener(AppDeleteListener<A> deleteListener);
        void notifyUpdateListeners(List<A> apps);
        void notifyRemoveListeners(List<A> apps);
    }

    public interface EventHandler {
        void showLauncherSettings(Context context);
        void showPickAction(Context context, DialogListener.OnAddAppDrawerItemListener listener);
        void showEditDialog(Context context, Item item, DialogListener.OnEditDialogListener listener);
        void showDeletePackageDialog(Context context, DragEvent dragEvent);
    }

    public interface Logger {
        void log(Object source, int priority, String tag, String msg, Object... args);
    }
}
