package com.benny.openlauncher.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.Log;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.App;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.Definitions;
import com.benny.openlauncher.util.Tool;

public class ShortcutReceiver extends BroadcastReceiver {

    public ShortcutReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() == null) return;

        String name = intent.getExtras().getString(Intent.EXTRA_SHORTCUT_NAME);
        Intent newIntent = (Intent) intent.getExtras().get(Intent.EXTRA_SHORTCUT_INTENT);
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

        App app = Setup.appLoader().createApp(newIntent);
        Item item;
        if (app != null) {
            item = Item.newAppItem(app);
        } else {
            item = Item.newShortcutItem(newIntent, shortcutIconDrawable, name);
        }
        Point preferredPos = HomeActivity.Companion.getLauncher().getDesktop().getPages().get(HomeActivity.Companion.getLauncher().getDesktop().getCurrentItem()).findFreeSpace();
        if (preferredPos == null) {
            Tool.toast(HomeActivity.Companion.getLauncher(), R.string.toast_not_enough_space);
        } else {
            item.setX(preferredPos.x);
            item.setY(preferredPos.y);
            HomeActivity.Companion.getDb().saveItem(item, HomeActivity.Companion.getLauncher().getDesktop().getCurrentItem(), Definitions.ItemPosition.Desktop);
            boolean added = HomeActivity.Companion.getLauncher().getDesktop().addItemToPage(item, HomeActivity.Companion.getLauncher().getDesktop().getCurrentItem());
            Log.d(null, String.format("Shortcut installed - %s => Intent: %s (Item _type: %s; x = %d, y = %d, added = %b)", name, newIntent, item.getType(), item.getX(), item.getY(), added));
        }
    }
}
