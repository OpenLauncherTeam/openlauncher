package com.benny.openlauncher.util;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.R;
import com.benny.openlauncher.widget.AppDrawerController;

import net.qiujuer.genius.blur.StackBlur;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class DialogUtils {
    public static MaterialDialog.Builder editItem(String title, String defaultText, Context c, final EditItemListener listener) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(c);
        builder.title(title)
                .input(null, defaultText, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        listener.itemLabel(input.toString());
                    }
                })
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel);
        return builder;
    }

    public static MaterialDialog.Builder alert(Context context, String title, String msg) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(title)
                .content(msg)
                .positiveText(R.string.ok);
        return builder;
    }

    public static void addActionItemDialog(final Context context, MaterialDialog.ListCallback callback) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title("Desktop Action")
                .items(R.array.desktopActionEntries)
                .itemsCallback(callback)
                .show();
    }

    public static void desktopStyleDialog(final Context context) {
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

    public static void appDrawerStyleDialog(final Context context) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(context.getString(R.string.settings_drawerStyle))
                .items(R.array.appDrawerStyleEntries)
                .itemsCallbackSingleChoice(LauncherSettings.getInstance(context).generalSettings.desktopHomePage, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        LauncherSettings.getInstance(context).generalSettings.drawerMode = AppDrawerController.DrawerMode.values()[position];
                        return true;
                    }
                }).show();
    }

    public static void selectActionDialog(final Context context, int titleId, int selected, MaterialDialog.ListCallbackSingleChoice onSingleChoice) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(context.getString(titleId))
                .negativeText(R.string.cancel)
                .items(R.array.gestureEntries)
                .itemsCallbackSingleChoice(selected, onSingleChoice)
                .show();
    }

    public static void setWallpaperDialog(final Context context) {
        String[] s = new String[]{context.getString(R.string.wallpaper_set), context.getString(R.string.wallpaper_blur)};
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(R.string.wallpaper)
                .iconRes(R.drawable.ic_photo_black_24dp)
                .items(s)
                .itemsCallback(new MaterialDialog.ListCallback() {
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
                }).show();
    }

    public static void backupDialog(final Context context) {
        final CharSequence[] options = {context.getResources().getString(R.string.settings_backup_titleBackup), context.getResources().getString(R.string.settings_backup_titleRestore)};
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
                }).show();
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

    public interface EditItemListener {
        void itemLabel(String str);
    }
}
