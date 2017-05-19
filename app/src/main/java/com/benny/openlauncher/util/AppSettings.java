package com.benny.openlauncher.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;

import com.benny.openlauncher.App;
import com.benny.openlauncher.R;
import com.benny.openlauncher.widget.AppDrawerController;
import com.benny.openlauncher.widget.Desktop;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.gsantner.opoc.util.AppSettingsBase;

/**
 * Created by gregor on 07.05.17.
 */

public class AppSettings extends AppSettingsBase {
    private AppSettings(Context context) {
        super(context);
    }

    public static AppSettings get() {
        return new AppSettings(App.get());
    }

    // methods that actually modify the preferences
    public int getIconSize() {
        return getInt(R.string.pref_key__icon_size, 52);
    }

    public boolean isAppFirstLaunch() {
        return getBool(R.string.pref_key__first_start, true);
    }

    @SuppressLint("ApplySharedPref")
    public void setAppFirstLaunch(boolean value) {
        // MUST be committed
        prefApp.edit().putBoolean(context.getString
                (R.string.pref_key__first_start), value).commit();
    }

    public int getDesktopMode() {
        return getIntOfStringPref(R.string.pref_key__desktop_style, Desktop.DesktopMode.NORMAL);
    }

    public boolean isDesktopLocked() {
        return getBool(R.string.pref_key__desktop_lock, false);
    }

    public void setDesktopLocked(boolean value) {
        setBool(R.string.pref_key__desktop_lock, value);
    }

    public int getAppDrawerMode() {
        return getIntOfStringPref(R.string.pref_key__drawer_style, AppDrawerController.DrawerMode.VERTICAL);
    }

    public String getIconPack() {
        return getString(R.string.pref_key__icon_pack, "");
    }

    public void setIconPack(String value) {
        setString(R.string.pref_key__icon_pack, value);
    }

    public boolean isDockShowLabel() {
        return getBool(R.string.pref_key__dock_show_label, false);
    }

    public boolean isDrawerShowLabel() {
        return getBool(R.string.pref_key__drawer_show_label, true);
    }

    public int getDrawerColumnCount_Portrait() {
        return getInt(R.string.pref_key__drawer_columns, 5);
    }

    public int getDrawerRowCount_Portrait() {
        return getInt(R.string.pref_key__drawer_rows, 6);
    }

    public int getDrawerColumnCount_Landscape() {
        return getInt(R.string.pref_key__drawer_columns_landscape, 5);
    }

    public int getDrawerRowCount_Landscape() {
        return getInt(R.string.pref_key__drawer_rows_landscape, 3);
    }

    public int getDesktopColumnCount() {
        return getInt(R.string.pref_key__desktop_columns, 5);
    }

    public int getDesktopRowCount() {
        return getInt(R.string.pref_key__desktop_rows, 6);
    }

    public int getDockSize() {
        return getInt(R.string.pref_key__dock_size, 5);
    }

    public boolean isMinibarEnabled() {
        return getBool(R.string.pref_key__minibar_enable, true);
    }

    public void setMinibarEnabled(boolean value) {
        setBool(R.string.pref_key__minibar_enable, value);
    }

    public ArrayList<String> getMinibarArrangement() {
        ArrayList<String> ret = new ArrayList<>(Arrays.asList(getStringArray(R.string.pref_key__minibar_data)));
        if (ret.isEmpty()) {
            for (LauncherAction.ActionDisplayItem item : LauncherAction.actionDisplayItems) {
                ret.add("0" + item.label.toString());
            }
            setMinibarArrangement(ret);
        }
        return ret;
    }

    public void setMinibarArrangement(ArrayList<String> value) {
        setStringArray(R.string.pref_key__minibar_data, value.toArray(new String[value.size()]));
    }

    public boolean isDesktopSearchbarEnabled() {
        return getBool(R.string.pref_key__search_bar_enable, true);
    }

    public boolean isDesktopFullscreen() {
        return getBool(R.string.pref_key__desktop_fullscreen, false);
    }

    public boolean isDesktopShowPageIndicator() {
        return getBool(R.string.pref_key__desktop_show_position_indicator, true);
    }

    public boolean isDesktopShowLabel() {
        return getBool(R.string.pref_key__desktop_show_label, true);
    }

    public int getDesktopPageCurrent() {
        return getInt(R.string.pref_key__desktop_current_position, 0);
    }

    public void setDesktopPageCurrent(int value) {
        setInt(R.string.pref_key__desktop_current_position, value);
    }

    public boolean isOpenAppDrawerOnSwipe() {
        return getBool(R.string.pref_key__dock_swipe_up, true);
    }

    public boolean isDrawerUseCard() {
        return getBool(R.string.pref_key__drawer_show_card_view, true);
    }

    public boolean isDrawerRememberPage() {
        return getBool(R.string.pref_key__drawer_remember_position, true);
    }

    public boolean isDrawerShowIndicator() {
        return getBool(R.string.pref_key__drawer_show_position_indicator, true);
    }

    public int getDrawerBackgroundColor() {
        return getInt(R.string.pref_key__drawer_background_color, Color.TRANSPARENT);
    }

    public int getDrawerCardColor() {
        return getInt(R.string.pref_key__drawer_card_color, Color.WHITE);
    }

    public int getDrawerLabelColor() {
        return getInt(R.string.pref_key__drawer_label_color, Color.DKGRAY);
    }

    public int getDestkopFolderColor() {
        return getInt(R.string.pref_key__desktop_folder_color, Color.WHITE);
    }

    public int getDesktopColor() {
        return getInt(R.string.pref_key__desktop_background_color, Color.TRANSPARENT);
    }

    public int getDockColor() {
        return getInt(R.string.pref_key__dock_background_color, Color.TRANSPARENT);
    }

    public boolean isDockEnable() {
        return getBool(R.string.pref_key__dock_enable, true);
    }

    public void setDockEnable(boolean enable) {
        setBool(R.string.pref_key__dock_enable, enable);
    }

    public ArrayList<String> getHiddenAppsList() {
        return new ArrayList<>(Arrays.asList(getStringArray(R.string.pref_key__hide_apps)));
    }

    public void setHiddenAppsList(ArrayList<String> value) {
        setStringArray(R.string.pref_key__hide_apps, value.toArray(new String[value.size()]));
    }

    public boolean isAppRestartRequired() {
        return getBool(R.string.pref_key__queue_restart, false);
    }

    @SuppressLint("ApplySharedPref")
    public void setAppRestartRequired(boolean value) {
        // MUST be committed
        prefApp.edit().putBoolean(context.getString
                (R.string.pref_key__queue_restart), value).commit();
    }
}
