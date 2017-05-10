package com.benny.openlauncher.util;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.activity.MinibarEditActivity;
import com.benny.openlauncher.activity.SettingsActivity;
import com.benny.openlauncher.viewutil.DialogHelper;

import java.util.List;

import static com.benny.openlauncher.activity.Home.resources;

public class LauncherAction {

    public static ActionDisplayItem[] actionDisplayItems = new ActionDisplayItem[]{
            new ActionDisplayItem(Action.EditMinBar, resources.getString(R.string.minibar_0), R.drawable.ic_mode_edit_black_24dp),
            new ActionDisplayItem(Action.SetWallpaper, resources.getString(R.string.minibar_1), R.drawable.ic_photo_black_24dp),
            new ActionDisplayItem(Action.LockScreen, resources.getString(R.string.minibar_2), R.drawable.ic_lock_black_24dp),
            new ActionDisplayItem(Action.ClearRam, resources.getString(R.string.minibar_3), R.drawable.ic_donut_large_black_24dp),
            new ActionDisplayItem(Action.DeviceSettings, resources.getString(R.string.minibar_4), R.drawable.ic_settings_applications_black_24dp),
            new ActionDisplayItem(Action.LauncherSettings, resources.getString(R.string.minibar_5), R.drawable.ic_settings_launcher_black_24dp),
            new ActionDisplayItem(Action.VolumeDialog, resources.getString(R.string.minibar_7), R.drawable.ic_volume_up_black_24dp),
            new ActionDisplayItem(Action.OpenAppDrawer, resources.getString(R.string.minibar_8), R.drawable.ic_apps_dark_24dp)
    };
    private static boolean clearingRam = false;

    public static void RunAction(@Nullable ActionItem actionItem, final Context context) {
        if (actionItem != null)
            switch (actionItem.action) {
                case LaunchApp:
                    Tool.startApp(context, actionItem.extraData);
                    break;

                default:
                    RunAction(actionItem.action, context);
            }
    }

    public static void RunAction(Action action, final Context context) {
        switch (action) {
            case EditMinBar:
                context.startActivity(new Intent(context, MinibarEditActivity.class));
                break;
            case SetWallpaper:
                DialogHelper.setWallpaperDialog(context);
                break;
            case LockScreen:
                try {
                    ((DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE)).lockNow();
                } catch (Exception e) {
                    Tool.toast(context, context.getString(R.string.toast_device_admin_required));
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings"));
                    context.startActivity(intent);
                }
                break;
            case ClearRam:
                if (clearingRam) {
                    break;
                }
                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                final ActivityManager activityManager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
                activityManager.getMemoryInfo(mi);
                final long pre = mi.availMem / 1048576L;
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected void onPreExecute() {
                        clearingRam = true;
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
                        clearingRam = false;
                        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                        activityManager.getMemoryInfo(mi);
                        long current = mi.availMem / 1048576L;
                        if (current - pre > 10)
                            Tool.toast(context, context.getResources().getString(R.string.toast_free_ram, current, current - pre));
                        else
                            Tool.toast(context, context.getResources().getString(R.string.toast_free_all_ram, current));
                        super.onPostExecute(result);
                    }
                }.execute();
                break;
            case DeviceSettings:
                context.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                break;
            case LauncherSettings:
                context.startActivity(new Intent(context, SettingsActivity.class));
                break;
            case VolumeDialog:
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamVolume(AudioManager.STREAM_RING), AudioManager.FLAG_SHOW_UI);
                break;
            case OpenAppDrawer:
                Home.launcher.openAppDrawer();
                break;
        }
    }

    public static ActionDisplayItem getActionItemFromString(String string) {
        for (ActionDisplayItem item : actionDisplayItems) {
            if (item.label.toString().equals(string)) {
                return item;
            }
        }
        return null;
    }

    public static ActionItem getActionItem(int position) {
        return new ActionItem(Action.values()[position], null);
    }

    public static int getActionItemIndex(ActionItem item) {
        if (item == null) return -1;
        for (int i = 0; i < Action.values().length; i++) {
            if (item.action == Action.values()[i])
                return i;
        }
        return -1;
    }

    public enum Theme {
        Dark, Light
    }

    public enum Action {
        EditMinBar, SetWallpaper, LockScreen, ClearRam, DeviceSettings, LauncherSettings, VolumeDialog, OpenAppDrawer, LaunchApp
    }

    public static class ActionItem {
        public Action action;
        public Intent extraData;

        public ActionItem(Action action, Intent extraData) {
            this.action = action;
            this.extraData = extraData;
        }
    }

    public static class ActionDisplayItem {
        public Action label;
        public String description;
        public int icon;

        public ActionDisplayItem(Action label, String description, int icon) {
            this.label = label;
            this.description = description;
            this.icon = icon;
        }
    }
}