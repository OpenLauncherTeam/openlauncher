package com.benny.openlauncher.viewutil;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.view.DragEvent;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.LauncherAction;
import com.benny.openlauncher.util.Tool;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import net.qiujuer.genius.blur.StackBlur;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DialogHelper {
    public static void editItemDialog(String title, String defaultText, Context c, final onItemEditListener listener) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(c);
        builder.title(title)
                .input(null, defaultText, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        listener.itemLabel(input.toString());
                    }
                })
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .show();
    }

    public static void alertDialog(Context context, String title, String msg) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(title)
                .content(msg)
                .positiveText(R.string.ok)
                .show();
    }

    public static void addActionItemDialog(final Context context, MaterialDialog.ListCallback callback) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title("Desktop Action")
                .items(R.array.desktopActionEntries)
                .itemsCallback(callback)
                .show();
    }

    public static void selectAppDialog(final Context context, final OnAppSelectedListener onAppSelectedListener) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        FastItemAdapter<IconLabelItem> fastItemAdapter = new FastItemAdapter<>();
        builder.title("Select App")
                .adapter(fastItemAdapter, new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false))
                .negativeText(R.string.cancel);

        final MaterialDialog dialog = builder.build();
        List<IconLabelItem> items = new ArrayList<>();
        final List<AppManager.App> apps = AppManager.getInstance(context).getApps();
        int size = Tool.dp2px(46, context);
        int sizePad = Tool.dp2px(8, context);
        for (int i = 0; i < apps.size(); i++) {
            items.add(new IconLabelItem(context, apps.get(i).icon, apps.get(i).label, null, sizePad, size));
        }
        fastItemAdapter.set(items);
        fastItemAdapter.withOnClickListener(new FastAdapter.OnClickListener<IconLabelItem>() {
            @Override
            public boolean onClick(View v, IAdapter<IconLabelItem> adapter, IconLabelItem item, int position) {
                if (onAppSelectedListener != null)
                    onAppSelectedListener.onAppSelected(apps.get(position));

                dialog.dismiss();
                return true;
            }
        });
        dialog.show();
    }

    public static void selectActionDialog(final Context context, int titleId, LauncherAction.ActionItem selected, final int id, final OnActionSelectedListener onActionSelectedListener) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(context.getString(titleId))
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
                                    public void onAppSelected(AppManager.App app) {
                                        finalItem.extraData = Tool.getStartAppIntent(app);
                                        onActionSelectedListener.onActionSelected(finalItem);
                                        Home.launcher.db.setGesture(id, finalItem);
                                    }
                                });
                            } else if (onActionSelectedListener != null) {
                                onActionSelectedListener.onActionSelected(item);
                                Home.launcher.db.setGesture(id, item);
                            }
                        } else {
                            Home.launcher.db.deleteGesture(id);
                        }
                        return true;
                    }
                })
                .show();
    }

    public static void setWallpaperDialog(final Context context) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(R.string.wallpaper)
                .iconRes(R.drawable.ic_photo_black_24dp)
                .items(R.array.wallpaperOptionEntries)
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

    public static void deletePackageDialog(Context context, DragEvent dragEvent) {
        Intent intent = dragEvent.getClipData().getItemAt(0).getIntent();
        intent.setExtrasClassLoader(Item.class.getClassLoader());
        Item item = intent.getParcelableExtra("mDragData");
        if (item.type == Item.Type.APP) {
            try {
                Uri packageURI = Uri.parse("package:" + item.intent.getComponent().getPackageName());
                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                context.startActivity(uninstallIntent);
            } catch (Exception e) {

            }
        }
    }

    public static void backupDialog(final Context context) {
        final CharSequence[] options = {context.getResources().getString(R.string.dialog__backup_app_settings__backup), context.getResources().getString(R.string.dialog__backup_app_settings__restore)};
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(R.string.pref_title__backup)
                .positiveText(R.string.cancel)
                .items(options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int item, CharSequence text) {
                        PackageManager m = context.getPackageManager();
                        String s = context.getPackageName();

                        if (options[item].equals(context.getResources().getString(R.string.dialog__backup_app_settings__backup))) {
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
                        if (options[item].equals(context.getResources().getString(R.string.dialog__backup_app_settings__restore))) {
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
        void onAppSelected(AppManager.App app);
    }

    public interface OnActionSelectedListener {
        void onActionSelected(LauncherAction.ActionItem item);
    }

    public interface onItemEditListener {
        void itemLabel(String label);
    }
}
