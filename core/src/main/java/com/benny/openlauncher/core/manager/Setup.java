package com.benny.openlauncher.core.manager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.View;

import com.benny.openlauncher.core.activity.Home;
import com.benny.openlauncher.core.interfaces.App;
import com.benny.openlauncher.core.interfaces.AppDeleteListener;
import com.benny.openlauncher.core.interfaces.AppItemView;
import com.benny.openlauncher.core.interfaces.AppUpdateListener;
import com.benny.openlauncher.core.interfaces.DatabaseHelper;
import com.benny.openlauncher.core.viewutil.DesktopGestureListener;
import com.benny.openlauncher.core.interfaces.DialogHandler;
import com.benny.openlauncher.core.interfaces.FastItem;
import com.benny.openlauncher.core.interfaces.Item;
import com.benny.openlauncher.core.interfaces.SettingsManager;
import com.benny.openlauncher.core.viewutil.DesktopCallBack;

import java.util.List;

/*
 * just a fast first helper class;
 * should be removed in the end, so we don't care to keep it clean
 */
public abstract class Setup<H extends Home, A extends App, LauncherItem extends Item, DrawerAppItem extends FastItem.AppItem, V extends View & AppItemView> {

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

    public static SettingsManager appSettings() {
        return get().getAppSettings();
    }

    // ----------------
    // Settings
    // ----------------

    public abstract SettingsManager getAppSettings();

    // ----------------
    // FastAdapter Items
    // ----------------

    public abstract FastItem.LabelItem createSearchBarInternetItem(Context context, int label, @Nullable View.OnClickListener listener);
    public abstract FastItem.LabelItem createSearchBarItem(Context context, A app, @Nullable View.OnClickListener listener);
    public abstract FastItem.DesktopOptionsItem createDesktopOptionsViewItem(Context context, int icon, int label, @Nullable View.OnClickListener listener, Typeface typeface);

    // ----------------
    // Listeners
    // ----------------

    public abstract List<AppUpdateListener<A>> getAppUpdatedListener(Context c);
    public abstract List<AppDeleteListener<A>> getAppDeletedListener(Context c);

    // ----------------
    // Helper class - Dialogs, Database, Drag&Drop Helper
    // ----------------

    public abstract DatabaseHelper<LauncherItem> createDatabaseHelper(Context context);
    public abstract DialogHandler getDialogHandler();
    public abstract DesktopGestureListener.DesktopGestureCallback getDrawerGestureCallback();

    // ----------------
    // Item
    // ----------------

    public abstract Class<LauncherItem> getItemClass();
    public abstract List<LauncherItem> createAllAppItems(Context context);
    public abstract LauncherItem newGroupItem();
    public abstract LauncherItem newWidgetItem(int appWidgetId);
    public abstract LauncherItem newActionItem(int action);
    public abstract LauncherItem createShortcut(Intent intent, Drawable icon, String name);

    // ----------------
    // Unstructured...
    // ----------------

    public abstract List<A> getAllApps(Context context);
    public abstract DrawerAppItem createDrawerAppItem(A app);
    public abstract View createDrawerAppItemView(Context context, H home, A app, AppItemView.LongPressCallBack longPressCallBack);
    public abstract AppItemView createAppItemViewPopup(Context context, LauncherItem groupItem, A item);
    public abstract View getItemView(Context context, LauncherItem item, boolean labelsEnabled, DesktopCallBack callBack);
    public abstract void showLauncherSettings(Context context);
    public abstract void onAppUpdated(Context p1, Intent p2);
    public abstract A findApp(Context c, Intent intent);
    public abstract void updateIcon(Context context, V appItemView, LauncherItem currentItem);
    public abstract void onItemViewDismissed(V itemView);
}
