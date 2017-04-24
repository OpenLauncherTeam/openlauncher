package com.benny.openlauncher.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;

import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.widget.Desktop;

import java.util.ArrayList;
import java.util.List;

public class ShortcutReceiver extends BroadcastReceiver {

    public ShortcutReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() == null) return;

        String name = intent.getExtras().getString(Intent.EXTRA_SHORTCUT_NAME);
        Intent newIntent = (Intent) intent.getExtras().get(Intent.EXTRA_SHORTCUT_INTENT);

//        if (newIntent != null && name != null){
//            List<AppManager.App> apps = AppManager.getInstance(context).getApps();
//            for (int i = 0; i < apps.size(); i++) {
//                AppManager.App app = apps.get(i);
//                if (app.className.equals(newIntent.getCl))
//                    Tool.startApp();
//            }
//        }

        Drawable shortcutIconDrawable = null;
        try {
            Parcelable iconResourceParcelable = intent.getExtras().getParcelable(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (iconResourceParcelable != null && iconResourceParcelable instanceof Intent.ShortcutIconResource) {
                Intent.ShortcutIconResource iconResource = (Intent.ShortcutIconResource) iconResourceParcelable;
                Resources resources = context.getPackageManager().getResourcesForApplication(iconResource.packageName);
                if (resources != null) {
                    int id = resources.getIdentifier(iconResource.resourceName, null, null);
                    shortcutIconDrawable = resources.getDrawable(id);
                }
            }
        } catch (Exception ignore) {
        } finally {
            if (shortcutIconDrawable == null)
                shortcutIconDrawable = new BitmapDrawable(context.getResources(), (Bitmap) intent.getExtras().getParcelable(Intent.EXTRA_SHORTCUT_ICON));
        }

        Desktop.Item item = Desktop.Item.newShortcutItem(context, name, newIntent, Tool.drawableToBitmap(shortcutIconDrawable));
        Point perferedPos = Home.launcher.desktop.pages.get(Home.launcher.desktop.getCurrentItem()).findFreeSpace();

        if (perferedPos == null) {
            item.x = 0;
            item.y = 0;
        } else {
            item.x = perferedPos.x;
            item.y = perferedPos.y;
        }
        if (LauncherSettings.getInstance(context).desktopData.size() < Home.launcher.desktop.getCurrentItem() + 1) {
            LauncherSettings.getInstance(context).desktopData.add(Home.launcher.desktop.getCurrentItem(), new ArrayList<Desktop.Item>());
        }
        LauncherSettings.getInstance(context).desktopData.get(Home.launcher.desktop.getCurrentItem()).add(item);
        Home.launcher.desktop.addItemToPage(item, Home.launcher.desktop.getCurrentItem());
    }
}
