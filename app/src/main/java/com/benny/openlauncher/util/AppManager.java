package com.benny.openlauncher.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.viewutil.IconLabelItem;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class AppManager {
    private static AppManager ref;

    public Context getContext() {
        return context;
    }

    private Context context;

    public PackageManager getPackageManager() {
        return packageManager;
    }

    public static final int ICONPACKREQUESTCODE = 321;

    private PackageManager packageManager;

    private List<App> apps = new ArrayList<>();
    private List<App> nonFilteredApps = new ArrayList<>();
    public List<AppUpdatedListener> updateListeners = new ArrayList<>();
    public List<AppDeletedListener> deleteListeners = new ArrayList<>();
    public boolean recreateAfterGettingApps;

    private List<AppUpdatedListener> updateListenersToRemove = new ArrayList<>();
    private AsyncTask task;

    public static AppManager getInstance(Context context) {
        return ref == null ? (ref = new AppManager(context)) : ref;
    }

    public AppManager(Context c) {
        this.context = c;
        this.packageManager = c.getPackageManager();
    }

    public App findApp(String packageName, String className) {
        for (App app : apps) {
            if (app.className.equals(className) && app.packageName.equals(packageName)) {
                return app;
            }
        }
        return null;
    }

    public List<App> getApps() {
        return apps;
    }

    public List<App> getNonFilteredApps() {
        return nonFilteredApps;
    }

    public void clearListener() {
        updateListeners.clear();
        deleteListeners.clear();
        updateListenersToRemove.clear();
    }

    public void init() {
        getAllApps();
    }

    public void addAppUpdatedListener(AppUpdatedListener listener) {
        updateListeners.add(listener);
    }

    public void removeAppUpdatedListener(AppUpdatedListener listener) {
        updateListenersToRemove.add(listener);
    }

    public void addAppDeletedListener(AppDeletedListener listener) {
        deleteListeners.add(listener);
    }

    private void getAllApps() {
        if (task == null || task.getStatus() == AsyncTask.Status.FINISHED)
            task = new AsyncGetApps().execute();
        else if (task.getStatus() == AsyncTask.Status.RUNNING) {
            task.cancel(false);
            task = new AsyncGetApps().execute();
        }
    }

    public void startPickIconPackIntent(final Activity activity) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory("com.anddoes.launcher.THEME");

        FastItemAdapter<IconLabelItem> fastItemAdapter = new FastItemAdapter<>();

        final List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);
        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(packageManager));
        final MaterialDialog d = new MaterialDialog.Builder(activity)
                .adapter(fastItemAdapter, null)
                .title((activity.getString(R.string.settings_iconPack_title)))
                .build();

        fastItemAdapter.add(new IconLabelItem(activity.getResources().getDrawable(R.drawable.ic_launcher), "Default", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreateAfterGettingApps = true;
                LauncherSettings.getInstance(context).generalSettings.iconPackName = "";
                getAllApps();
                d.dismiss();
            }
        }));

        for (int i = 0; i < resolveInfos.size(); i++) {
            final int mI = i;
            fastItemAdapter.add(new IconLabelItem(resolveInfos.get(i).loadIcon(packageManager), resolveInfos.get(i).loadLabel(packageManager).toString(), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        recreateAfterGettingApps = true;
                        LauncherSettings.getInstance(context).generalSettings.iconPackName = resolveInfos.get(mI).activityInfo.packageName;
                        getAllApps();
                        d.dismiss();
                    } else {
                        Tool.toast(context, (activity.getString(R.string.settings_iconPack_toast)));
                        ActivityCompat.requestPermissions(Home.launcher, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Home.REQUEST_PERMISSION_STORAGE);
                    }

                }
            }));
        }

        d.show();
    }

    public void onReceive(Context p1, Intent p2) {
        getAllApps();
    }

    private class AsyncGetApps extends AsyncTask {
        private List<App> tempApps;

        @Override
        protected void onPreExecute() {
            tempApps = new ArrayList<>(apps);
            super.onPreExecute();
        }

        @Override
        protected void onCancelled() {
            tempApps = null;
            super.onCancelled();
        }

        @Override
        protected Object doInBackground(Object[] p1) {
            apps.clear();
            nonFilteredApps.clear();

            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> activitiesInfo = packageManager.queryIntentActivities(intent, 0);
            Collections.sort(activitiesInfo, new Comparator<ResolveInfo>() {
                @Override
                public int compare(ResolveInfo p1, ResolveInfo p2) {
                    return Collator.getInstance().compare(p1.loadLabel(packageManager).toString(), p2.loadLabel(packageManager).toString());
                }
            });
            for (ResolveInfo info : activitiesInfo) {
                App app = new App(info, packageManager);
                nonFilteredApps.add(app);
            }

            List<String> hiddenList = LauncherSettings.getInstance(getContext()).generalSettings.hiddenList;
            if (hiddenList != null) {
                for (int i = 0; i < nonFilteredApps.size(); i++) {
                    boolean shouldGetAway = false;
                    for (String hidItemRaw : hiddenList) {
                        if ((nonFilteredApps.get(i).packageName + "/" + nonFilteredApps.get(i).className).equals(hidItemRaw)) {
                            shouldGetAway = true;
                            break;
                        }
                    }
                    if (!shouldGetAway) {
                        apps.add(nonFilteredApps.get(i));
                    }
                }
            } else {
                for (ResolveInfo info : activitiesInfo)
                    apps.add(new App(info, packageManager));
            }

            LauncherSettings.GeneralSettings generalSettings = LauncherSettings.getInstance(context).generalSettings;
            if (!generalSettings.iconPackName.isEmpty() && Tool.isPackageInstalled(generalSettings.iconPackName, packageManager)) {
                IconPackHelper.themePacs(AppManager.this, Tool.dp2px(generalSettings.iconSize, context), generalSettings.iconPackName, apps);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            for (AppUpdatedListener listener : updateListeners) {
                listener.onAppUpdated(apps);
            }

            if (tempApps.size() > apps.size()) {
                App temp = null;
                for (int i = 0; i < tempApps.size(); i++) {
                    if (!apps.contains(tempApps.get(i))) {
                        temp = tempApps.get(i);
                        break;
                    }
                }
                for (AppDeletedListener listener : deleteListeners) {
                    listener.onAppDeleted(temp);
                }
            }


            for (AppUpdatedListener listener : updateListenersToRemove) {
                updateListeners.remove(listener);
            }
            updateListenersToRemove.clear();

            if (recreateAfterGettingApps) {
                recreateAfterGettingApps = false;
                if (context instanceof Home)
                    ((Home) context).recreate();
            }

            super.onPostExecute(result);
        }
    }

    public static class App {
        public String label, packageName, className;
        public Drawable icon;
        public ResolveInfo info;

        public App(ResolveInfo info, PackageManager pm) {
            this.info = info;

            icon = info.loadIcon(pm);
            label = info.loadLabel(pm).toString();
            packageName = info.activityInfo.packageName;
            className = info.activityInfo.name;

            if (packageName.equals("com.benny.openlauncher")) {
                label = "OLSettings";
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof App) {
                AppManager.App temp = (App) o;
                return this.packageName.equals(temp.packageName);
            } else {
                return false;
            }
        }
    }

    public static abstract class AppUpdatedListener {
        private String listenerID;

        public AppUpdatedListener() {
            listenerID = UUID.randomUUID().toString();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof AppUpdatedListener && ((AppUpdatedListener) obj).listenerID.equals(this.listenerID);
        }

        public abstract void onAppUpdated(List<App> apps);
    }

    public interface AppDeletedListener {
        public void onAppDeleted(App app);
    }
}
