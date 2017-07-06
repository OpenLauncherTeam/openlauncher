package com.benny.openlauncher.core.manager;

import android.content.Context;
import android.view.View;

import com.benny.openlauncher.core.activity.Home;
import com.benny.openlauncher.core.interfaces.App;
import com.benny.openlauncher.core.interfaces.AppDeleteListener;
import com.benny.openlauncher.core.interfaces.AppItemView;
import com.benny.openlauncher.core.interfaces.AppUpdateListener;
import com.benny.openlauncher.core.interfaces.FastItem;
import com.benny.openlauncher.core.interfaces.Item;
import com.benny.openlauncher.core.interfaces.SettingsManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class DefaultSetup<ID extends Number, H extends Home, A extends App, LauncherItem extends Item<LauncherItem, ID>, DrawerAppItem extends FastItem.AppItem, V extends View & AppItemView> extends Setup<ID, H, A, LauncherItem, DrawerAppItem, V> {

    private final Context appContext;
    private final SettingsManager settingsManager;
    private final List<AppUpdateListener<A>> updateListener = new ArrayList<>();
    private final List<AppDeleteListener<A>> deleteListeners = new ArrayList<>();

    public DefaultSetup(Context context) {
        appContext = context.getApplicationContext();
        settingsManager = new DefaultSettings(context);
    }

    @Override
    public SettingsManager getAppSettings() {
        return settingsManager;
    }

    @Override
    public List<AppUpdateListener<A>> getAppUpdatedListener(Context c) {
        return updateListener;
    }

    @Override
    public List<AppDeleteListener<A>> getAppDeletedListener(Context c) {
        return deleteListeners;
    }

    public void notifyUpdateListeners(List<A> apps) {
        Iterator<AppUpdateListener<A>> iter = updateListener.iterator();
        while (iter.hasNext()) {
            if (iter.next().onAppUpdated(apps)) {
                iter.remove();
            }
        }
    }
}
