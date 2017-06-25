package com.benny.openlauncher.core.manager;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.benny.openlauncher.core.activity.Home;
import com.benny.openlauncher.core.interfaces.IApp;
import com.benny.openlauncher.core.interfaces.IAppDeleteListener;
import com.benny.openlauncher.core.interfaces.IAppItem;
import com.benny.openlauncher.core.interfaces.IAppItemView;
import com.benny.openlauncher.core.interfaces.IAppUpdateListener;
import com.benny.openlauncher.core.interfaces.IDatabaseHelper;
import com.benny.openlauncher.core.interfaces.IItem;
import com.benny.openlauncher.core.interfaces.ISettingsManager;
import com.benny.openlauncher.core.interfaces.IDialogHandler;
import com.benny.openlauncher.core.viewutil.DesktopCallBack;
import com.benny.openlauncher.core.widget.Desktop;

import java.util.List;

import in.championswimmer.sfg.lib.SimpleFingerGestures;

/**
 * Created by Michael on 25.06.2017.
 */

/*
 * just a fast first helper class;
 * should be removed in the end, so we don't care to keep it clean
 */
public abstract class StaticSetup<H extends Home, A extends IApp, T extends IItem, U extends IAppItem, V extends View & IAppItemView> {

    private static StaticSetup mSetup = null;

    public static void init(StaticSetup setup)
    {
        mSetup = setup;
    }

    public static StaticSetup get()
    {
        if (mSetup == null)
            throw new RuntimeException("StaticSetup has not been initialised!");
        return mSetup;
    }

    public abstract Class<T> getItemClass();
    public abstract ISettingsManager getAppSettings();
    public abstract IDatabaseHelper<T> createDatabaseHelper(Context context);
    public abstract List<A> getAllApps(Context context);
    public abstract List<U> createAllAppItems(Context context);
    public abstract U createAppItem(A app);
    public abstract View createAppItemView(Context context, H home, A app, IAppItemView.LongPressCallBack longPressCallBack);
    public abstract IAppItemView createAppItemViewPopup(Context context, T groupItem, A item);
    public abstract T newGroupItem();
    public abstract T newWidgetItem(int appWidgetId);
    public abstract T newActionItem(int action);
    public abstract View getItemView(Context context, ISettingsManager appSettings, T item, DesktopCallBack callBack);
    public abstract SimpleFingerGestures.OnFingerGestureListener getDesktopGestureListener(Desktop desktop);
    public abstract T createShortcut(Intent intent, Drawable icon, String name);
    public abstract void showLauncherSettings(Context context);
    public abstract IDialogHandler getDialogHandler();
    public abstract void addAppUpdatedListener(Context c, IAppUpdateListener<A> listener);
    public abstract void removeAppUpdatedListener(Context c, IAppUpdateListener<A> listener);
    public abstract void addAppDeletedListener(Context c, IAppDeleteListener<A> listener);
    public abstract void onAppUpdated(Context p1, Intent p2);
    public abstract A findApp(Context c, Intent intent);
    public abstract void updateIcon(Context context, V appItemView, T currentItem);
    public abstract void onItemViewDismissed(IAppItemView itemView);
}
