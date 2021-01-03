package com.benny.openlauncher.activity;

import android.app.Activity;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.os.Build;
import android.os.Bundle;

public class AddItemActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null && getIntent().getAction().equalsIgnoreCase(LauncherApps.ACTION_CONFIRM_PIN_SHORTCUT)) {
            LauncherApps launcherApps = (LauncherApps) getSystemService(Context.LAUNCHER_APPS_SERVICE);
            LauncherApps.PinItemRequest request = launcherApps.getPinItemRequest(getIntent());
            if (request == null) {
                finish();
                return;
            }

            if (request.getRequestType() == LauncherApps.PinItemRequest.REQUEST_TYPE_SHORTCUT) {
                
                ShortcutInfoCompat info = new ShortcutInfoCompat(request.getShortcutInfo());
                String shortcutLabel = info.getShortLabel().toString();
                Intent shortcutIntent = info.makeIntent();
                Drawable shortcutIcon = null;

                App app = Setup.appLoader().createApp(shortcutIntent);
                Item item;
                if (app != null) {
                    item = Item.newAppItem(app);
                } else {
                    item = Item.newShortcutItem(shortcutIntent, shortcutIcon, shortcutLabel);
                }
                Point preferredPos = HomeActivity.Companion.getLauncher().getDesktop().getPages().get(HomeActivity.Companion.getLauncher().getDesktop().getCurrentItem()).findFreeSpace();
                if (preferredPos == null) {
                    Tool.toast(HomeActivity.Companion.getLauncher(), R.string.toast_not_enough_space);
                } else {
                    item.setX(preferredPos.x);
                    item.setY(preferredPos.y);
                    HomeActivity._db.saveItem(item, HomeActivity.Companion.getLauncher().getDesktop().getCurrentItem(), Definitions.ItemPosition.Desktop);
                    HomeActivity.Companion.getLauncher().getDesktop().addItemToPage(item, HomeActivity.Companion.getLauncher().getDesktop().getCurrentItem());
                    Log.d(this.getClass().toString(), "shortcut installed");
                }

                request.accept();
                finish();
            }
        }
    }
}
