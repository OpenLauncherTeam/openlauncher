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
import com.benny.openlauncher.core.interfaces.AppItem;
import com.benny.openlauncher.core.interfaces.AppItemView;
import com.benny.openlauncher.core.interfaces.AppUpdateListener;
import com.benny.openlauncher.core.interfaces.DatabaseHelper;
import com.benny.openlauncher.core.interfaces.IconLabelItem;
import com.benny.openlauncher.core.interfaces.Item;
import com.benny.openlauncher.core.interfaces.SettingsManager;
import com.benny.openlauncher.core.interfaces.DialogHandler;
import com.benny.openlauncher.core.viewutil.DesktopCallBack;
import com.benny.openlauncher.core.widget.Desktop;

import java.util.List;

import in.championswimmer.sfg.lib.SimpleFingerGestures;

/*
 * just a fast first helper class;
 * should be removed in the end, so we don't care to keep it clean
 */
public abstract class Setup<H extends Home, A extends App, T extends Item, U extends AppItem, V extends View & AppItemView> {

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

    public abstract IconLabelItem createSearchBarInternetItem(Context context, int label, @Nullable View.OnClickListener listener);
    public abstract IconLabelItem createSearchBarItem(Context context, A app, @Nullable View.OnClickListener listener);
    public abstract IconLabelItem createDesktopOptionsViewItem(Context context, int icon, int label, @Nullable View.OnClickListener listener, Typeface typeface);

    // ----------------
    // Listeners
    // ----------------

    public abstract List<AppUpdateListener<A>> getAppUpdatedListener(Context c);
    public abstract List<AppDeleteListener<A>> getAppDeletedListener(Context c);

    // ----------------
    // Unstructured...
    // ----------------

    public abstract Class<T> getItemClass();
    public abstract DatabaseHelper<T> createDatabaseHelper(Context context);
    public abstract List<A> getAllApps(Context context);
    public abstract List<T> createAllAppItems(Context context);
    public abstract U createDrawerAppItem(A app);
    public abstract View createDrawerAppItemView(Context context, H home, A app, AppItemView.LongPressCallBack longPressCallBack);
    public abstract AppItemView createAppItemViewPopup(Context context, T groupItem, A item);
    public abstract T newGroupItem();
    public abstract T newWidgetItem(int appWidgetId);
    public abstract T newActionItem(int action);
    public abstract View getItemView(Context context, T item, boolean labelsEnabled, DesktopCallBack callBack);
    public abstract SimpleFingerGestures.OnFingerGestureListener getDesktopGestureListener(Desktop desktop);
    public abstract T createShortcut(Intent intent, Drawable icon, String name);
    public abstract void showLauncherSettings(Context context);
    public abstract DialogHandler getDialogHandler();

    public abstract void onAppUpdated(Context p1, Intent p2);
    public abstract A findApp(Context c, Intent intent);
    public abstract void updateIcon(Context context, V appItemView, T currentItem);
    public abstract void onItemViewDismissed(V itemView);
}
