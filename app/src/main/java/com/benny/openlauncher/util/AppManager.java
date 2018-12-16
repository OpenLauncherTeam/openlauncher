package com.benny.openlauncher.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;

import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.interfaces.AppDeleteListener;
import com.benny.openlauncher.interfaces.AppUpdateListener;
import com.benny.openlauncher.model.App;
import com.benny.openlauncher.model.Item;

import android.support.annotation.NonNull;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class AppManager {
    private static AppManager _ref;

    public static AppManager getInstance(Context context) {
        return _ref == null ? (_ref = new AppManager(context)) : _ref;
    }

    private PackageManager _packageManager;
    private List<App> _apps = new ArrayList<>();
    private List<App> _nonFilteredApps = new ArrayList<>();
    public final List<AppUpdateListener> _updateListeners = new ArrayList<>();
    public final List<AppDeleteListener> _deleteListeners = new ArrayList<>();
    public boolean _recreateAfterGettingApps;
    private AsyncTask _task;
    private Context _context;

    public PackageManager getPackageManager() {
        return _packageManager;
    }

    public Context getContext() {
        return _context;
    }

    public AppManager(Context c) {
        _context = c;
        _packageManager = c.getPackageManager();
    }

    public App findApp(Intent intent) {
        if (intent == null || intent.getComponent() == null) return null;

        String packageName = intent.getComponent().getPackageName();
        String className = intent.getComponent().getClassName();
        for (App app : _apps) {
            if (app._className.equals(className) && app._packageName.equals(packageName)) {
                return app;
            }
        }
        return null;
    }

    public List<App> getApps() {
        return _apps;
    }

    public List<App> getNonFilteredApps() {
        return _nonFilteredApps;
    }

    public void init() {
        getAllApps();
    }

    public void getAllApps() {
        if (_task == null || _task.getStatus() == AsyncTask.Status.FINISHED)
            _task = new AsyncGetApps().execute();
        else if (_task.getStatus() == AsyncTask.Status.RUNNING) {
            _task.cancel(false);
            _task = new AsyncGetApps().execute();
        }
    }

    public void onReceive(Context context, Intent intent) {
        getAllApps();
    }

    public List<App> getAllApps(Context context, boolean includeHidden) {
        return includeHidden ? getNonFilteredApps() : getApps();
    }

    public App findItemApp(Item item) {
        return findApp(item.getIntent());
    }

    public App createApp(Intent intent) {
        try {
            ResolveInfo info = _packageManager.resolveActivity(intent, 0);
            return new App(_packageManager, info);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void onAppUpdated(Context p1, Intent p2) {
        onReceive(p1, p2);
    }

    public void addUpdateListener(AppUpdateListener updateListener) {
        _updateListeners.add(updateListener);
    }

    public void addDeleteListener(AppDeleteListener deleteListener) {
        _deleteListeners.add(deleteListener);
    }

    public void notifyUpdateListeners(@NonNull List<App> apps) {
        Iterator<AppUpdateListener> iter = _updateListeners.iterator();
        while (iter.hasNext()) {
            if (iter.next().onAppUpdated(apps)) {
                iter.remove();
            }
        }
    }

    public void notifyRemoveListeners(@NonNull List<App> apps) {
        Iterator<AppDeleteListener> iter = _deleteListeners.iterator();
        while (iter.hasNext()) {
            if (iter.next().onAppDeleted(apps)) {
                iter.remove();
            }
        }
    }

    private class AsyncGetApps extends AsyncTask {
        private List<App> tempApps;

        @Override
        protected void onPreExecute() {
            tempApps = new ArrayList<>(_apps);
            super.onPreExecute();
        }

        @Override
        protected void onCancelled() {
            tempApps = null;
            super.onCancelled();
        }

        @Override
        protected Object doInBackground(Object[] p1) {
            _apps.clear();
            _nonFilteredApps.clear();

            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> activitiesInfo = _packageManager.queryIntentActivities(intent, 0);
            Collections.sort(activitiesInfo, new Comparator<ResolveInfo>() {
                @Override
                public int compare(ResolveInfo p1, ResolveInfo p2) {
                    return Collator.getInstance().compare(p1.loadLabel(_packageManager).toString(), p2.loadLabel(_packageManager).toString());
                }
            });

            for (ResolveInfo info : activitiesInfo) {
                App app = new App(_packageManager, info);
                _nonFilteredApps.add(app);
            }

            List<String> hiddenList = AppSettings.get().getHiddenAppsList();
            if (hiddenList != null) {
                for (int i = 0; i < _nonFilteredApps.size(); i++) {
                    boolean shouldGetAway = false;
                    for (String hidItemRaw : hiddenList) {
                        if ((_nonFilteredApps.get(i)._packageName + "/" + _nonFilteredApps.get(i)._className).equals(hidItemRaw)) {
                            shouldGetAway = true;
                            break;
                        }
                    }
                    if (!shouldGetAway) {
                        _apps.add(_nonFilteredApps.get(i));
                    }
                }
            } else {
                for (ResolveInfo info : activitiesInfo)
                    _apps.add(new App(_packageManager, info));
            }

            AppSettings appSettings = AppSettings.get();
            if (!appSettings.getIconPack().isEmpty() && Tool.isPackageInstalled(appSettings.getIconPack(), _packageManager)) {
                IconPackHelper.applyIconPack(AppManager.this, Tool.dp2px(appSettings.getIconSize()), appSettings.getIconPack(), _apps);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {

            notifyUpdateListeners(_apps);

            List<App> removed = getRemovedApps(tempApps, _apps);
            if (removed.size() > 0) {
                notifyRemoveListeners(removed);
            }

            if (_recreateAfterGettingApps) {
                _recreateAfterGettingApps = false;
                if (_context instanceof HomeActivity)
                    ((HomeActivity) _context).recreate();
            }

            super.onPostExecute(result);
        }
    }

    public static List<App> getRemovedApps(List<App> oldApps, List<App> newApps) {
        List<App> removed = new ArrayList<>();
        // if this is the first call then return an empty list
        if (oldApps.size() == 0) {
            return removed;
        }
        for (int i = 0; i < oldApps.size(); i++) {
            if (!newApps.contains(oldApps.get(i))) {
                removed.add(oldApps.get(i));
                break;
            }
        }
        return removed;
    }

    public static abstract class AppUpdatedListener implements AppUpdateListener {
        private String listenerID;

        public AppUpdatedListener() {
            listenerID = UUID.randomUUID().toString();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof AppUpdatedListener && ((AppUpdatedListener) obj).listenerID.equals(this.listenerID);
        }
    }
}
