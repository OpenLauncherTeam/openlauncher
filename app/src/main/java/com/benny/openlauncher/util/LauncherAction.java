package com.benny.openlauncher.util;

import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.provider.Settings;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.activity.MinibarEditActivity;
import com.benny.openlauncher.activity.SettingsActivity;
import com.benny.openlauncher.viewutil.DialogHelper;

public class LauncherAction {

    public enum Action {
        LaunchApp, EditMinibar, SetWallpaper, LockScreen, LauncherSettings, DeviceSettings, VolumeDialog, AppDrawer, SearchBar, MobileNetworkSettings
    }

    public static ActionDisplayItem[] actionDisplayItems = new ActionDisplayItem[]{
            new ActionDisplayItem(Action.EditMinibar, HomeActivity.Companion.get_resources().getString(R.string.minibar_title__edit), HomeActivity.Companion.get_resources().getString(R.string.minibar_summary__edit), R.drawable.ic_mode_edit_black_24dp, 98),
            new ActionDisplayItem(Action.SetWallpaper, HomeActivity.Companion.get_resources().getString(R.string.minibar_title__set_wallpaper), HomeActivity.Companion.get_resources().getString(R.string.minibar_summary__set_wallpaper), R.drawable.ic_photo_black_24dp, 36),
            new ActionDisplayItem(Action.LockScreen, HomeActivity.Companion.get_resources().getString(R.string.minibar_title__lock_screen), HomeActivity.Companion.get_resources().getString(R.string.minibar_summary__lock_screen), R.drawable.ic_lock_black_24dp, 24),
            new ActionDisplayItem(Action.LauncherSettings, HomeActivity.Companion.get_resources().getString(R.string.minibar_title__launcher_settings), HomeActivity.Companion.get_resources().getString(R.string.minibar_summary__launcher_settings), R.drawable.ic_settings_launcher_black_24dp, 50),
            new ActionDisplayItem(Action.VolumeDialog, HomeActivity.Companion.get_resources().getString(R.string.minibar_title__volume), HomeActivity.Companion.get_resources().getString(R.string.minibar_summary__volume), R.drawable.ic_volume_up_black_24dp, 71),
            new ActionDisplayItem(Action.DeviceSettings, HomeActivity.Companion.get_resources().getString(R.string.minibar_title__device_settings), HomeActivity.Companion.get_resources().getString(R.string.minibar_summary__device_settings), R.drawable.ic_android_minimal, 25),
            new ActionDisplayItem(Action.AppDrawer, HomeActivity.Companion.get_resources().getString(R.string.minibar_title__app_drawer), HomeActivity.Companion.get_resources().getString(R.string.minibar_summary__app_drawer), R.drawable.ic_apps_dark_24dp, 73),
            new ActionDisplayItem(Action.SearchBar, HomeActivity.Companion.get_resources().getString(R.string.minibar_title__search_bar), HomeActivity.Companion.get_resources().getString(R.string.minibar_summary__search_bar), R.drawable.ic_search_light_24dp, 89),
            new ActionDisplayItem(Action.MobileNetworkSettings, HomeActivity.Companion.get_resources().getString(R.string.minibar_title__mobile_network), HomeActivity.Companion.get_resources().getString(R.string.minibar_summary__mobile_network), R.drawable.ic_network_24dp, 46),
    };

    public static void RunAction(Action action, final Context context) {
        LauncherAction.RunAction(getActionItem(action), context);
    }

    public static void RunAction(ActionDisplayItem action, final Context context) {
        switch (action._action) {
            case LaunchApp:
                PackageManager pm = AppManager.getInstance(context).getPackageManager();
                //action._extraData = new Intent(pm.getLaunchIntentForPackage(AppSettings.get().getString(context.getString(R.string.pref_key__gesture_double_tap) + "__", "")));
                break;
            case EditMinibar:
                context.startActivity(new Intent(context, MinibarEditActivity.class));
                break;
            case SetWallpaper:
                context.startActivity(Intent.createChooser(new Intent(Intent.ACTION_SET_WALLPAPER), context.getString(R.string.wallpaper_pick)));
                break;
            case LockScreen:
                try {
                    ((DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE)).lockNow();
                } catch (Exception e) {
                    DialogHelper.alertDialog(context, "Device Admin Required", "OpenLauncher requires the Device Administration permission to lock your screen. Please enable it in the settings to use this feature.", "Enable", new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Tool.toast(context, context.getString(R.string.toast_device_admin_required));
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings"));
                            context.startActivity(intent);
                        }
                    });
                }
                break;
            case DeviceSettings:
                context.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                break;
            case LauncherSettings:
                context.startActivity(new Intent(context, SettingsActivity.class));
                break;
            case VolumeDialog:
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    try {
                        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamVolume(AudioManager.STREAM_RING), AudioManager.FLAG_SHOW_UI);
                    } catch (Exception e) {
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            context.startActivity(intent);
                        }
                    }
                } else {
                    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamVolume(AudioManager.STREAM_RING), AudioManager.FLAG_SHOW_UI);
                }
                break;
            case AppDrawer:
                HomeActivity.Companion.getLauncher().openAppDrawer();
                break;
            case SearchBar: {
                HomeActivity.Companion.getLauncher().getSearchBar().getSearchButton().performClick();
                break;
            }
            case MobileNetworkSettings: {
                context.startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                break;
            }
        }
    }

    public static ActionDisplayItem getActionItemById(int id) {
        for (ActionDisplayItem item : actionDisplayItems) {
            if (Integer.toString(item._id).equals(Integer.toString(id))) {
                return item;
            }
        }
        return null;
    }

    public static ActionDisplayItem getActionItemByPosition(int position) {
        return getActionItem(Action.values()[position]);
    }

    public static ActionDisplayItem getActionItem(Action action) {
        for (ActionDisplayItem item : actionDisplayItems) {
            if (item._action.equals(action)) {
                return item;
            }
        }
        return null;
    }

    public static class ActionDisplayItem {
        public Action _action;
        public String _label;
        public String _description;
        public int _icon;
        public int _id;

        public ActionDisplayItem(Action action, String label, String description, int icon, int id) {
            _action = action;
            _label = label;
            _description = description;
            _icon = icon;
            _id = id;
        }
    }
}
