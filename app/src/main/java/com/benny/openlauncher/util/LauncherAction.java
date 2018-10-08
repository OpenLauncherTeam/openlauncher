package com.benny.openlauncher.util;

import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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

import java.util.Arrays;
import java.util.List;

public class LauncherAction {

    public enum Action {
        EditMinibar, SetWallpaper, LockScreen, LauncherSettings, VolumeDialog, DeviceSettings, AppDrawer, SearchBar, MobileNetworkSettings
    }

    public static ActionDisplayItem[] actionDisplayItems = new ActionDisplayItem[]{
            new ActionDisplayItem(Action.EditMinibar, HomeActivity._launcher.getResources().getString(R.string.minibar_title__edit_minibar), HomeActivity._launcher.getResources().getString(R.string.minibar_summary__edit), R.drawable.ic_mode_edit_black_24dp, 98),
            new ActionDisplayItem(Action.SetWallpaper, HomeActivity._launcher.getResources().getString(R.string.minibar_title__set_wallpaper), HomeActivity._launcher.getResources().getString(R.string.minibar_summary__set_wallpaper), R.drawable.ic_photo_black_24dp, 36),
            new ActionDisplayItem(Action.LockScreen, HomeActivity._launcher.getResources().getString(R.string.minibar_title__lock_screen), HomeActivity._launcher.getResources().getString(R.string.minibar_summary__lock_screen), R.drawable.ic_lock_black_24dp, 24),
            new ActionDisplayItem(Action.LauncherSettings, HomeActivity._launcher.getResources().getString(R.string.minibar_title__launcher_settings), HomeActivity._launcher.getResources().getString(R.string.minibar_summary__launcher_settings), R.drawable.ic_settings_launcher_black_24dp, 50),
            new ActionDisplayItem(Action.VolumeDialog, HomeActivity._launcher.getResources().getString(R.string.minibar_title__volume), HomeActivity._launcher.getResources().getString(R.string.minibar_summary__volume), R.drawable.ic_volume_up_black_24dp, 71),
            new ActionDisplayItem(Action.DeviceSettings, HomeActivity._launcher.getResources().getString(R.string.minibar_title__device_settings), HomeActivity._launcher.getResources().getString(R.string.minibar_summary__device_settings), R.drawable.ic_android_minimal, 25),
            new ActionDisplayItem(Action.AppDrawer, HomeActivity._launcher.getResources().getString(R.string.minibar_title__app_drawer), HomeActivity._launcher.getResources().getString(R.string.minibar_summary__app_drawer), R.drawable.ic_apps_dark_24dp, 73),
            new ActionDisplayItem(Action.SearchBar, HomeActivity._launcher.getResources().getString(R.string.minibar_title__search_bar), HomeActivity._launcher.getResources().getString(R.string.minibar_summary__search_bar), R.drawable.ic_search_light_24dp, 89),
            new ActionDisplayItem(Action.MobileNetworkSettings, HomeActivity._launcher.getResources().getString(R.string.minibar_title__mobile_network), HomeActivity._launcher.getResources().getString(R.string.minibar_summary__mobile_network), R.drawable.ic_network_24dp, 46),
    };

    public static List<Action> defaultArrangement = Arrays.asList(
            Action.EditMinibar, Action.SetWallpaper,
            Action.LockScreen, Action.LauncherSettings,
            Action.VolumeDialog, Action.DeviceSettings
    );

    public static void RunAction(Action action, final Context context) {
        LauncherAction.RunAction(getActionItem(action), context);
    }

    public static void RunAction(ActionDisplayItem action, final Context context) {
        switch (action._action) {
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

    public static ActionDisplayItem getActionItem(int position) {
        // used for pick action dialog
        return getActionItem(Action.values()[position]);
    }

    public static ActionDisplayItem getActionItem(Action action) {
        return getActionItem(action.toString());
    }

    public static ActionDisplayItem getActionItem(String action) {
        for (ActionDisplayItem item : actionDisplayItems) {
            if (item._action.toString().equals(action)) {
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
