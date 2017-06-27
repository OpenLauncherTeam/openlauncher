package com.benny.openlauncher.core.manager;

import android.content.Context;
import android.graphics.Color;

import com.benny.openlauncher.core.R;
import com.benny.openlauncher.core.interfaces.SettingsManager;
import com.benny.openlauncher.core.widget.AppDrawerController;
import com.benny.openlauncher.core.widget.Desktop;

/**
 * Created by flisar on 27.06.2017.
 */

public class DefaultSettings implements SettingsManager {

    private final Context appContext;
    private int currentDesktopPage = 0;
    private boolean desktopLocked = false;
    private boolean appStartRequired = false;
    private boolean appFirstLaunch = true;

    public DefaultSettings(Context context) {
        appContext = context.getApplicationContext();
    }

    @Override
    public int getDesktopPageCurrent() {
        return currentDesktopPage;
    }

    @Override
    public void setDesktopPageCurrent(int page) {
        currentDesktopPage = page;
    }

    @Override
    public boolean isDesktopShowIndicator() {
        return true;
    }

    @Override
    public int getDesktopColumnCount() {
        return 4;
    }

    @Override
    public int getDesktopRowCount() {
        return 5;
    }

    @Override
    public int getDesktopStyle() {
        return Desktop.DesktopMode.NORMAL;
    }

    @Override
    public boolean isDesktopShowLabel() {
        return true;
    }

    @Override
    public int getDockSize() {
        return 5;
    }

    @Override
    public boolean getGestureDockSwipeUp() {
        return true;
    }

    @Override
    public boolean isDesktopLock() {
        return desktopLocked;
    }

    @Override
    public void setDesktopLock(boolean locked) {
        desktopLocked = locked;
    }

    @Override
    public boolean isGestureFeedback() {
        return false;
    }

    @Override
    public int getIconSize() {
        return 52;
    }

    @Override
    public boolean isDockShowLabel() {
        return true;
    }

    @Override
    public int getDrawerColumnCount() {
        return 4;
    }

    @Override
    public int getDrawerRowCount() {
        return 6;
    }

    @Override
    public boolean isDrawerShowIndicator() {
        return true;
    }

    @Override
    public int getDrawerStyle() {
        return AppDrawerController.DrawerMode.VERTICAL;
    }

    @Override
    public boolean isDrawerShowCardView() {
        return true;
    }

    @Override
    public int getDrawerCardColor() {
        return Color.parseColor("#88000000");
    }

    @Override
    public boolean isDrawerShowLabel() {
        return true;
    }

    @Override
    public int getDrawerLabelColor() {
        return Color.WHITE;
    }

    @Override
    public boolean isDrawerRememberPosition() {
        return false;
    }

    @Override
    public boolean isDesktopFullscreen() {
        return false;
    }

    @Override
    public int getDesktopColor() {
        return Color.TRANSPARENT;
    }

    @Override
    public int getDockColor() {
        return Color.TRANSPARENT;
    }

    @Override
    public int getDrawerBackgroundColor() {
        return Color.TRANSPARENT;
    }

    @Override
    public boolean getDockEnable() {
        return true;
    }

    @Override
    public boolean getSearchBarEnable() {
        return true;
    }

    @Override
    public boolean getAppRestartRequired() {
        return appStartRequired;
    }

    @Override
    public void setAppRestartRequired(boolean required) {
        appStartRequired = required;
    }

    @Override
    public boolean isAppFirstLaunch() {
        return appFirstLaunch;
    }

    @Override
    public void setAppFirstLaunch(boolean isAppFirstLaunch) {
        appFirstLaunch = isAppFirstLaunch;
    }

    @Override
    public String getSearchBarBaseURI() {
        return appContext.getString(R.string.pref_default__search_bar_base_uri);
    }
}
