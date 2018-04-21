package com.benny.openlauncher.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.view.Gravity;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.interfaces.AppDeleteListener;
import com.benny.openlauncher.interfaces.AppUpdateListener;
import com.benny.openlauncher.model.IconLabelItem;
import com.benny.openlauncher.model.Item;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import org.jetbrains.annotations.NotNull;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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

    private PackageManager packageManager;
    private List<App> apps = new ArrayList<>();
    private List<App> nonFilteredApps = new ArrayList<>();
    public final List<AppUpdateListener> updateListeners = new ArrayList<>();
    public final List<AppDeleteListener> deleteListeners = new ArrayList<>();
    public boolean recreateAfterGettingApps;

    private AsyncTask task;

    public static AppManager getInstance(Context context) {
        return ref == null ? (ref = new AppManager(context)) : ref;
    }

    public AppManager(Context c) {
        this.context = c;
        this.packageManager = c.getPackageManager();
    }

    public App findApp(Intent intent) {
        if (intent == null || intent.getComponent() == null) return null;

        String packageName = intent.getComponent().getPackageName();
        String className = intent.getComponent().getClassName();
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

    public void init() {
        getAllApps();
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
                .title((activity.getString(R.string.dialog__icon_pack_title)))
                .build();

        fastItemAdapter.add(new IconLabelItem(activity, R.drawable.ic_launcher, R.string.label_default, -1)
                .withIconGravity(Gravity.START)
                .withOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        recreateAfterGettingApps = true;
                        AppSettings.get().setIconPack("");
                        getAllApps();
                        d.dismiss();
                    }
                }));

        for (int i = 0; i < resolveInfos.size(); i++) {
            final int mI = i;
            fastItemAdapter.add(new IconLabelItem(activity, resolveInfos.get(i).loadIcon(packageManager), resolveInfos.get(i).loadLabel(packageManager).toString(), -1)
                    .withIconGravity(Gravity.START)
                    .withOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                recreateAfterGettingApps = true;
                                AppSettings.get().setIconPack(resolveInfos.get(mI).activityInfo.packageName);
                                getAllApps();
                                d.dismiss();
                            } else {
                                Tool.toast(context, (activity.getString(R.string.dialog__icon_pack_info_toast)));
                                ActivityCompat.requestPermissions(Home.Companion.getLauncher(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Home.REQUEST_PERMISSION_STORAGE);
                            }
                        }
                    }));
        }
        d.show();
    }

    public void onReceive(Context p1, Intent p2) {
        getAllApps();
    }

    // -----------------------
    // AppLoader interface
    // -----------------------

    public List<App> getAllApps(Context context, boolean includeHidden) {
        return includeHidden ? getNonFilteredApps() : getApps();
    }

    public App findItemApp(Item item) {
        return findApp(item.getIntent());
    }

    public App createApp(Intent intent) {
        try {
            ResolveInfo info = packageManager.resolveActivity(intent, 0);
            App app = new App(getContext(), info, packageManager);
            if (apps != null && !apps.contains(app))
                apps.add(app);
            return app;
        } catch (Exception e) {
            return null;
        }
    }

    public void onAppUpdated(Context p1, Intent p2) {
        onReceive(p1, p2);
    }

    public void addUpdateListener(AppUpdateListener updateListener) {
        updateListeners.add(updateListener);
    }

    public void addDeleteListener(AppDeleteListener deleteListener) {
        deleteListeners.add(deleteListener);
    }

    public void notifyUpdateListeners(@NotNull List<App> apps) {
        Iterator<AppUpdateListener> iter = updateListeners.iterator();
        while (iter.hasNext()) {
            if (iter.next().onAppUpdated(apps)) {
                iter.remove();
            }
        }
    }

    public void notifyRemoveListeners(@NotNull List<App> apps) {
        Iterator<AppDeleteListener> iter = deleteListeners.iterator();
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
                App app = new App(context, info, packageManager);
                nonFilteredApps.add(app);
            }

            List<String> hiddenList = AppSettings.get().getHiddenAppsList();
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
                    apps.add(new App(context, info, packageManager));
            }

            AppSettings appSettings = AppSettings.get();
            if (!appSettings.getIconPack().isEmpty() && Tool.isPackageInstalled(appSettings.getIconPack(), packageManager)) {
                IconPackHelper.themePacs(AppManager.this, Tool.dp2px(appSettings.getIconSize(), context), appSettings.getIconPack(), apps);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {

            notifyUpdateListeners(apps);

            List<App> removed = Tool.getRemovedApps(tempApps, apps);
            if (removed.size() > 0) {
                notifyRemoveListeners(removed);
            }

            if (recreateAfterGettingApps) {
                recreateAfterGettingApps = false;
                if (context instanceof Home)
                    ((Home) context).recreate();
            }

            super.onPostExecute(result);
        }
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
