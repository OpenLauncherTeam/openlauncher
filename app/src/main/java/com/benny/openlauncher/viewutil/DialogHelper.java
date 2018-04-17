package com.benny.openlauncher.viewutil;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.R;
import com.benny.openlauncher.model.IconLabelItem;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.App;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.LauncherAction;
//import com.benny.openlauncher.util.copy;
// import com.benny.openlauncher.util.getStartAppIntent;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import java.io.File;
import java.util.*;

public class DialogHelper {
    public static void editItemDialog(String title, String defaultText, Context c, final OnItemEditListener listener) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(c);
        builder.title(title)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .input(null, defaultText, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        listener.itemLabel(input.toString());
                    }
                }).show();
    }

    public static void alertDialog(Context context, String title, String msg) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(title)
                .content(msg)
                .negativeText(R.string.cancel)
                .positiveText(R.string.ok)
                .show();
    }

    public static void alertDialog(Context context, String title, String msg, MaterialDialog.SingleButtonCallback onPositive) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(title)
                .onPositive(onPositive)
                .content(msg)
                .negativeText(R.string.cancel)
                .positiveText(R.string.ok)
                .show();
    }

    public static void alertDialog(Context context, String title, String msg, String positive, MaterialDialog.SingleButtonCallback onPositive) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(title)
                .onPositive(onPositive)
                .content(msg)
                .negativeText(R.string.cancel)
                .positiveText(positive)
                .show();
    }


    public static void addActionItemDialog(final Context context, MaterialDialog.ListCallback callback) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(R.string.desktop_action)
                .items(R.array.entries__desktop_actions)
                .itemsCallback(callback)
                .show();
    }

    public static void selectAppDialog(final Context context, final OnAppSelectedListener onAppSelectedListener) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        FastItemAdapter<IconLabelItem> fastItemAdapter = new FastItemAdapter<>();
        builder.title(R.string.select_app)
                .adapter(fastItemAdapter, new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false))
                .negativeText(R.string.cancel);

        final MaterialDialog dialog = builder.build();
        List<IconLabelItem> items = new ArrayList<>();
        final List<App> apps = AppManager.getInstance(context).getApps(); // TODO: Tool.java migration
        int size = Tool.dp2px(18, context);
        int sizePad = Tool.dp2px(8, context);
        for (int i = 0; i < apps.size(); i++) {
            items.add(new IconLabelItem(context, apps.get(i).getIconProvider(), apps.get(i).getLabel(), size)
                    .withIconGravity(Gravity.START)
                    .withDrawablePadding(context, sizePad));
        }
        fastItemAdapter.set(items);
        fastItemAdapter.withOnClickListener(new com.mikepenz.fastadapter.listeners.OnClickListener<IconLabelItem>() {
            @Override
            public boolean onClick(View v, IAdapter<IconLabelItem> adapter, IconLabelItem item, int position) {
                if (onAppSelectedListener != null) {
                    onAppSelectedListener.onAppSelected(apps.get(position));
                }
                dialog.dismiss();
                return true;
            }
        });
        dialog.show();
    }

    public static void selectActionDialog(final Context context, String title, LauncherAction.ActionItem selected, final OnActionSelectedListener onActionSelectedListener) {
        new MaterialDialog.Builder(context)
                .title(title)
                .negativeText(R.string.cancel)
                .items(R.array.entries__gestures)
                .itemsCallbackSingleChoice(LauncherAction.getActionItemIndex(selected) + 1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        LauncherAction.ActionItem item = null;
                        if (which > 0) {
                            item = LauncherAction.getActionItem(which - 1);
                            if (item != null && item.action == LauncherAction.Action.LaunchApp) {
                                final LauncherAction.ActionItem finalItem = item;
                                selectAppDialog(context, new OnAppSelectedListener() {
                                    @Override
                                    public void onAppSelected(App app) {
                                        finalItem.extraData = Tool.getStartAppIntent(app); // TODO
                                        onActionSelectedListener.onActionSelected(finalItem);
                                    }
                                });
                            } else if (onActionSelectedListener != null) {
                                onActionSelectedListener.onActionSelected(item);
                            }
                        }
                        return true;
                    }
                }).show();
    }


    public static void setWallpaperDialog(final Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.wallpaper)
                .iconRes(R.drawable.ic_photo_black_24dp)
                .items(R.array.entries__wallpaper_options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        switch (position) {
                            case 0:
                                Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
                                context.startActivity(Intent.createChooser(intent, context.getString(R.string.wallpaper_pick)));
                                break;
                            case 1:
                                Tool.toast(context, "This is no longer supported, wait for future updates.");
                                break;
                        }
                    }
                }).show();
    }

    public static void deletePackageDialog(Context context, Item item) {
        if (item.getType() == Item.Type.APP) {
            try {
                Uri packageURI = Uri.parse("package:" + item.getIntent().getComponent().getPackageName());
                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                context.startActivity(uninstallIntent);
            } catch (Exception e) {

            }
        }
    }

    public static void backupDialog(final Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.pref_title__backup)
                .positiveText(R.string.cancel)
                .items(R.array.entries__backup_options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int item, CharSequence text) {
                        PackageManager m = context.getPackageManager();
                        String s = context.getPackageName();

                        if (context.getResources().getStringArray(R.array.entries__backup_options)[item].equals(context.getResources().getString(R.string.dialog__backup_app_settings__backup))) {
                            File directory = new File(Environment.getExternalStorageDirectory() + "/OpenLauncher/");
                            if (!directory.exists()) {
                                directory.mkdir();
                            }
                            try {
                                PackageInfo p = m.getPackageInfo(s, 0);
                                s = p.applicationInfo.dataDir;
                                Tool.copy(context, s + "/databases/home.db", directory + "/home.db");
                                Tool.copy(context, s + "/shared_prefs/app.xml", directory + "/app.xml");
                                Toast.makeText(context, R.string.dialog__backup_app_settings__success, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(context, R.string.dialog__backup_app_settings__error, Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (context.getResources().getStringArray(R.array.entries__backup_options)[item].equals(context.getResources().getString(R.string.dialog__backup_app_settings__restore))) {
                            File directory = new File(Environment.getExternalStorageDirectory() + "/OpenLauncher/");
                            try {
                                PackageInfo p = m.getPackageInfo(s, 0);
                                s = p.applicationInfo.dataDir;
                                Tool.copy(context, directory + "/home.db", s + "/databases/home.db");
                                Tool.copy(context, directory + "/app.xml", s + "/shared_prefs/app.xml");
                                Toast.makeText(context, R.string.dialog__backup_app_settings__success, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(context, R.string.dialog__backup_app_settings__error, Toast.LENGTH_SHORT).show();
                            }
                            System.exit(1);
                        }
                    }
                }).show();
    }

    public interface OnAppSelectedListener {
        void onAppSelected(App app);
    }

    public interface OnActionSelectedListener {
        void onActionSelected(LauncherAction.ActionItem item);
    }

    public interface OnItemEditListener {
        void itemLabel(String label);
    }
}
