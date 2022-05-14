package com.benny.openlauncher.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.NonNull;

import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.interfaces.AppDeleteListener;
import com.benny.openlauncher.interfaces.AppUpdateListener;
import com.benny.openlauncher.model.App;
import com.benny.openlauncher.model.Item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class AppManager {
    private static Logger LOG = LoggerFactory.getLogger("AppManager");

    private static AppManager appManager;

    public static AppManager getInstance(Context context) {
        return appManager == null ? (appManager = new AppManager(context)) : appManager;
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

    public AppManager(Context context) {
        _context = context;
        _packageManager = context.getPackageManager();
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

    public List<App> getAllApps(Context context, boolean includeHidden) {
        return includeHidden ? getNonFilteredApps() : getApps();
    }

    public App findItemApp(Item item) {
        return findApp(item.getIntent());
    }

    public App createApp(Intent intent) {
        try {
            ResolveInfo info = _packageManager.resolveActivity(intent, 0);
            List<ShortcutInfo> shortcutInfo = Tool.getShortcutInfo(getContext(), intent.getComponent().getPackageName());
            return new App(_packageManager, info, shortcutInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void onAppUpdated(Context context, Intent intent) {
        getAllApps();
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
        private List<App> appsTemp;
        private List<App> nonFilteredAppsTemp;
        private List<App> removedApps;

        @Override
        protected void onPreExecute() {
            appsTemp = new ArrayList<>();
            nonFilteredAppsTemp = new ArrayList<>();
            removedApps = new ArrayList<>();
            super.onPreExecute();
        }

        @Override
        protected void onCancelled() {
            appsTemp = null;
            nonFilteredAppsTemp = null;
            removedApps = new ArrayList<>();
            super.onCancelled();
        }

        @Override
        protected Object doInBackground(Object[] p1) {

            // work profile support
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LauncherApps launcherApps = (LauncherApps) _context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
                List<UserHandle> profiles = launcherApps.getProfiles();
                for (UserHandle userHandle : profiles) {
                    List<LauncherActivityInfo> apps = launcherApps.getActivityList(null, userHandle);
                    for (LauncherActivityInfo info : apps) {
                        List<ShortcutInfo> shortcutInfo = Tool.getShortcutInfo(getContext(), info.getComponentName().getPackageName());
                        App app = new App(_packageManager, info, shortcutInfo);
                        app._userHandle = userHandle;
                        LOG.debug("adding work profile to non filtered list: {}, {}, {}", app._label, app._packageName, app._className);
                        nonFilteredAppsTemp.add(app);
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                UserManager userManager = (UserManager) _context.getSystemService(Context.USER_SERVICE);
                // LauncherApps.getProfiles() is not available for API 25, so just get all associated user profile handlers
                List<UserHandle> profiles = userManager.getUserProfiles();
                LauncherApps launcherApps = (LauncherApps) _context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
                for (UserHandle userHandle : profiles) {
                    List<LauncherActivityInfo> apps = launcherApps.getActivityList(null, userHandle);
                    for (LauncherActivityInfo info : apps) {
                        List<ShortcutInfo> shortcutInfo = Tool.getShortcutInfo(getContext(), info.getComponentName().getPackageName());
                        App app = new App(_packageManager, info, shortcutInfo);
                        app._userHandle = userHandle;
                        LOG.debug("adding work profile to non filtered list: {}, {}, {}", app._label, app._packageName, app._className);
                        nonFilteredAppsTemp.add(app);
                    }
                }
            } else {
                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> activitiesInfo = _packageManager.queryIntentActivities(intent, 0);
                for (ResolveInfo info : activitiesInfo) {
                    App app = new App(_packageManager, info, null);
                    LOG.debug("adding app to non filtered list: {}, {}, {}", app._label,  app._packageName, app._className);
                    nonFilteredAppsTemp.add(app);
                }
            }

            // sort the apps by label here
            Collections.sort(nonFilteredAppsTemp, new Comparator<App>() {
                @Override
                public int compare(App one, App two) {
                    return Collator.getInstance().compare(one._label, two._label);
                }
            });

            List<String> hiddenList = AppSettings.get().getHiddenAppsList();
            if (hiddenList != null) {
                for (int i = 0; i < nonFilteredAppsTemp.size(); i++) {
                    boolean shouldGetAway = false;
                    for (String hidItemRaw : hiddenList) {
                        if ((nonFilteredAppsTemp.get(i).getComponentName()).equals(hidItemRaw)) {
                            shouldGetAway = true;
                            break;
                        }
                    }
                    if (!shouldGetAway) {
                        appsTemp.add(nonFilteredAppsTemp.get(i));
                    }
                }
            } else {
                appsTemp.addAll(nonFilteredAppsTemp);
            }

            removedApps = getRemovedApps(_apps, appsTemp);

            for (App app : removedApps) {
                HomeActivity._db.deleteItems(app);
            }

            AppSettings appSettings = AppSettings.get();
            if (!appSettings.getIconPack().isEmpty() && Tool.isPackageInstalled(appSettings.getIconPack(), _packageManager)) {
                IconPackHelper.applyIconPack(AppManager.this, Tool.dp2px(appSettings.getIconSize()), appSettings.getIconPack(), appsTemp);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            _apps = appsTemp;
            _nonFilteredApps = nonFilteredAppsTemp;

            if (removedApps.size() > 0) {
                notifyRemoveListeners(removedApps);
            }

            notifyUpdateListeners(appsTemp);

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
}
