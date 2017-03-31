package com.benny.openlauncher.util;

import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.activity.MinibarEditActivity;
import com.benny.openlauncher.activity.SettingsActivity;

import net.qiujuer.genius.blur.StackBlur;

import java.util.List;

import static com.benny.openlauncher.activity.Home.resources;

public class LauncherAction {

    private static boolean clearingRam = false;

    public static ActionItem[] actionItems = new ActionItem[]{
            new ActionItem(Action.EditMinBar, resources.getString(R.string.minibar_0), R.drawable.ic_mode_edit_black_24dp),
            new ActionItem(Action.SetWallpaper, resources.getString(R.string.minibar_1), R.drawable.ic_photo_black_24dp),
            new ActionItem(Action.LockScreen, resources.getString(R.string.minibar_2), R.drawable.ic_lock_black_24dp),
            new ActionItem(Action.ClearRam, resources.getString(R.string.minibar_3), R.drawable.ic_donut_large_black_24dp),
            new ActionItem(Action.DeviceSettings, resources.getString(R.string.minibar_4), R.drawable.ic_settings_applications_black_24dp),
            new ActionItem(Action.LauncherSettings, resources.getString(R.string.minibar_5), R.drawable.ic_settings_launcher_24dp),
            //new ActionItem(Action.ThemePicker,resources.getString(R.string.minibar_6),R.drawable.ic_brush_black_24dp),
            new ActionItem(Action.VolumeDialog, resources.getString(R.string.minibar_7), R.drawable.ic_volume_up_black_24dp),
            new ActionItem(Action.OpenAppDrawer, resources.getString(R.string.minibar_8), R.drawable.ic_apps_black_24dp)
    };

    public static void RunAction(Action action, final Context context) {
        switch (action) {
            case EditMinBar:
                Intent intent1 = new Intent(context, MinibarEditActivity.class);
                context.startActivity(intent1);
                break;
            case SetWallpaper:
                MaterialDialog.Builder b = new MaterialDialog.Builder(context);
                b.title(R.string.wallpaper);
                b.iconRes(R.drawable.ic_photo_black_24dp);
                String[] s = new String[]{context.getString(R.string.wallpaper_set), context.getString(R.string.wallpaper_blur)};
                b.items(s);
                b.itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        switch (position) {
                            case 0:
                                Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
                                context.startActivity(Intent.createChooser(intent, context.getString(R.string.wallpaper_pick)));
                                break;
                            case 1:
                                try {
                                    WallpaperManager.getInstance(context).setBitmap(StackBlur.blur(Tool.drawableToBitmap(context.getWallpaper()), 10, false));
                                } catch (Exception e) {
                                    Tool.toast(context, context.getString(R.string.wallpaper_unable_to_blur));
                                }
                                break;
                        }
                    }
                });
                b.show();
                break;
            case LockScreen:
                try {
                    ((DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE)).lockNow();
                } catch (Exception e) {
                    Tool.toast(context, context.getString(R.string.toast_plzenabledeviceadmin));
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings"));
                    context.startActivity(intent);
                }
                break;
            case ClearRam:
                if (clearingRam)
                    break;
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
                            Tool.toast(context, context.getResources().getString(R.string.toast_freeram, current, current - pre));
                        else
                            Tool.toast(context, context.getResources().getString(R.string.toast_freeallram, current));
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

    public static class ActionItem {
        public Action label;
        public String des;
        public int icon;

        public ActionItem(Action label, String des, int icon) {
            this.label = label;
            this.des = des;
            this.icon = icon;
        }
    }

    public static ActionItem getActionItemFromString(String string) {
        for (ActionItem item : actionItems) {
            if (item.label.toString().equals(string)) {
                return item;
            }
        }
        return null;
    }

    public enum Theme {
        Dark, Light;

        public static String[] names() {
            Theme[] states = values();
            String[] names = new String[states.length];

            for (int i = 0; i < states.length; i++) {
                names[i] = states[i].name();
            }

            return names;
        }
    }

    public enum Action {
        EditMinBar, SetWallpaper, LockScreen, ClearRam, DeviceSettings, LauncherSettings, ThemePicker, VolumeDialog, OpenAppDrawer
    }
}