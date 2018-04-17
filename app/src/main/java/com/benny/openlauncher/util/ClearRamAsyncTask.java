package com.benny.openlauncher.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;

import com.benny.openlauncher.App;
import com.benny.openlauncher.R;
import com.benny.openlauncher.core.util.Tool;

import java.util.List;

/**
 * Created by BennyKok on 11/12/2017.
 */

public class ClearRamAsyncTask extends AsyncTask<Void, Void, Void> {
    private long pre = 0;
    private ActivityManager activityManager;

    @Override
    protected void onPreExecute() {
        LauncherAction.clearingRam = true;
        Context context = App.get();

        if (context == null) {
            cancel(true);
            return;
        }

        activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(mi);
        pre = mi.availMem / 1048576L;
    }

    @Override
    protected Void doInBackground(Void[] p1) {
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfo = activityManager.getRunningAppProcesses();
        for (int i = 0; i < runningAppProcessInfo.size(); i++) {
            activityManager.killBackgroundProcesses(runningAppProcessInfo.get(i).pkgList[0]);
        }
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        LauncherAction.clearingRam = false;
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(mi);
        long current = mi.availMem / 1048576L;
        Context context = App.get();
        if (context == null) {
            super.onPostExecute(result);
            return;
        }
        if (current - pre > 10)
            Tool.toast(context, context.getResources().getString(R.string.toast_free_ram, current, current - pre));
        else
            Tool.toast(context, context.getResources().getString(R.string.toast_free_all_ram, current));
        super.onPostExecute(result);
    }
}
