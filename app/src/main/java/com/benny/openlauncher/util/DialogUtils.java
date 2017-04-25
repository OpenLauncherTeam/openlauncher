package com.benny.openlauncher.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.R;
import com.benny.openlauncher.widget.AppDrawerController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class DialogUtils {
    public static void desktopStylePicker(final Context context) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(context.getString(R.string.settings_desktopStyle))
                .items(R.array.desktopStyleEntries)
                .itemsCallbackSingleChoice(LauncherSettings.getInstance(context).generalSettings.desktopHomePage, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        LauncherSettings.getInstance(context).setDesktopMode(position);
                        return true;
                    }
                }).show();
    }

    public static void desktopActionPicker(final Context context, MaterialDialog.ListCallbackSingleChoice callback) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title("Desktop Action")
                .items(R.array.desktopActionEntries)
                .itemsCallbackSingleChoice(LauncherSettings.getInstance(context).generalSettings.desktopHomePage, callback).show();
    }

    public static void appDrawerStylePicker(final Context context) {
        final String[] items = new String[AppDrawerController.DrawerMode.values().length];
        int enabled = 0;
        for (int i = 0; i < AppDrawerController.DrawerMode.values().length; i++) {
            items[i] = AppDrawerController.DrawerMode.values()[i].name();
            if (LauncherSettings.getInstance(context).generalSettings.drawerMode == AppDrawerController.DrawerMode.values()[i]) {
                enabled = i;
            }
        }
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(context.getString(R.string.settings_drawerStyle))
                .items(context.getString(R.string.settings_drawer_style_paged), context.getString(R.string.settings_drawer_style_vertical))
                .itemsCallbackSingleChoice(enabled, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        LauncherSettings.getInstance(context).generalSettings.drawerMode = AppDrawerController.DrawerMode.valueOf(items[position]);
                        return true;
                    }
                }).show();
    }

    public static void startGesturePicker(final Context context, int titleId, int selected, MaterialDialog.ListCallbackSingleChoice onSingleChoise) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(context.getString(titleId))
                .negativeText(R.string.cancel)
                .items(R.array.gestureEntries)
                .itemsCallbackSingleChoice(selected, onSingleChoise)
                .show();
    }

    public static void backupDialog(final Context context) {
        final CharSequence[] options = {
                context.getResources().getString(R.string.settings_backup_titleBackup),
                context.getResources().getString(R.string.settings_backup_titleRestore)};

        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(R.string.settings_backup)
                .positiveText(R.string.cancel)
                .items(options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int item, CharSequence text) {
                        PackageManager m = context.getPackageManager();
                        String s = context.getPackageName();

                        if (options[item].equals(context.getResources().getString(R.string.settings_backup_titleBackup))) {
                            File directory = new File(Environment.getExternalStorageDirectory() + "/OpenLauncher/");
                            if (!directory.exists()) {
                                // noinspection ResultOfMethodCallIgnored
                                directory.mkdirs();
                            }

                            try {
                                PackageInfo p = m.getPackageInfo(s, 0);
                                s = p.applicationInfo.dataDir;
                                copy(context, s + "/databases/home.db", directory + "/home.db");
                                copy(context, s + "/files/generalSettings.json", directory + "/generalSettings.json");
                                Toast.makeText(context, R.string.settings_backup_success, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(context, R.string.settings_backup_success_not, Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (options[item].equals(context.getResources().getString(R.string.settings_backup_titleRestore))) {
                            File directory = new File(Environment.getExternalStorageDirectory() + "/OpenLauncher/");

                            try {
                                PackageInfo p = m.getPackageInfo(s, 0);
                                s = p.applicationInfo.dataDir;
                                copy(context, directory + "/home.db", s + "/databases/home.db");
                                copy(context, directory + "/generalSettings.json", s + "/files/generalSettings.json");
                                Toast.makeText(context, R.string.settings_backup_success, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(context, R.string.settings_backup_success_not, Toast.LENGTH_SHORT).show();
                            }
                            // this will stop your application and take out from it
                            // kill off the crashed app
                            System.exit(1);
                        }
                    }
                });
        builder.show();
    }

    private static void copy(Context context, String stringIn, String stringOut) {
        try {
            File desktopData = new File(stringOut);
            desktopData.delete();
            File dockData = new File(stringOut);
            dockData.delete();
            File generalSettings = new File(stringOut);
            generalSettings.delete();
            Tool.print("deleted");

            FileInputStream in = new FileInputStream(stringIn);
            FileOutputStream out = new FileOutputStream(stringOut);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            // write the output file
            out.flush();
            out.close();
            Tool.print("copied");

        } catch (Exception e) {
            Toast.makeText(context, R.string.settings_backup_success_not, Toast.LENGTH_SHORT).show();
        }
    }
}