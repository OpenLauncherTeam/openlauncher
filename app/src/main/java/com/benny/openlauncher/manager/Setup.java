package com.benny.openlauncher.manager;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.benny.openlauncher.interfaces.DialogListener;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.BaseIconProvider;
import com.benny.openlauncher.util.Definitions;
import com.benny.openlauncher.viewutil.DesktopGestureListener;
import com.benny.openlauncher.viewutil.ItemGestureListener;

import java.util.List;

public abstract class Setup {

    // ----------------
    // Class and singleton
    // ----------------

    private static Setup _setup = null;

    public static boolean wasInitialised() {
        return _setup != null;
    }

    public static void init(Setup setup) {
        Setup._setup = setup;
    }

    public static Setup get() {
        if (_setup == null) {
            throw new RuntimeException("Setup has not been initialised!");
        }
        return _setup;
    }

    // ----------------
    // Methods for convenience and shorter code
    // ----------------

    public static Context appContext() {
        return get().getAppContext();
    }

    public static AppSettings appSettings() {
        return get().getAppSettings();
    }

    public static DesktopGestureListener.DesktopGestureCallback desktopGestureCallback() {
        return get().getDesktopGestureCallback();
    }

    public static ItemGestureListener.ItemGestureCallback itemGestureCallback() {
        return get().getItemGestureCallback();
    }

    public static ImageLoader imageLoader() {
        return get().getImageLoader();
    }

    public static DataManager dataManager() {
        return get().getDataManager();
    }

    public static AppManager appLoader() {
        return get().getAppLoader();
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

    public abstract AppSettings getAppSettings();

    public abstract DesktopGestureListener.DesktopGestureCallback getDesktopGestureCallback();

    public abstract ItemGestureListener.ItemGestureCallback getItemGestureCallback();

    public abstract ImageLoader getImageLoader();

    public abstract DataManager getDataManager();

    public abstract AppManager getAppLoader();

    public abstract EventHandler getEventHandler();

    public abstract Logger getLogger();

    // ----------------
    // Interfaces
    // ----------------

    public interface ImageLoader {
        BaseIconProvider createIconProvider(Drawable drawable);

        BaseIconProvider createIconProvider(int icon);
    }

    public interface DataManager {
        void saveItem(Item item);

        void saveItem(Item item, Definitions.ItemState state);

        void saveItem(Item item, int page, Definitions.ItemPosition desktop);

        void deleteItem(Item item, boolean deleteSubItems);

        List<List<Item>> getDesktop();

        List<Item> getDock();

        Item getItem(int id);
    }

    public interface EventHandler {
        void showLauncherSettings(Context context);

        void showPickAction(Context context, DialogListener.OnAddAppDrawerItemListener listener);

        void showEditDialog(Context context, Item item, DialogListener.OnEditDialogListener listener);

        void showDeletePackageDialog(Context context, Item item);
    }

    public interface Logger {
        void log(Object source, int priority, String tag, String msg, Object... args);
    }
}
