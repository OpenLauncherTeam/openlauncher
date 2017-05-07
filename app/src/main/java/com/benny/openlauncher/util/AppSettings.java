package com.benny.openlauncher.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

import com.benny.openlauncher.App;
import com.benny.openlauncher.R;
import com.benny.openlauncher.widget.AppDrawerController;
import com.benny.openlauncher.widget.Desktop;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by gregor on 07.05.17.
 */
public class AppSettings {
    private static final String ARRAY_SEPERATOR = "%%";
    private static final String SHARED_PREF_APP = "app";

    //#####################
    //## Members
    //#####################
    private final SharedPreferences prefApp;
    private final Context context;

    //#####################
    //## Methods
    //#####################

    /**
     * Constructor
     *
     * @param _context Android _context
     */
    private AppSettings(Context _context) {
        context = _context.getApplicationContext();
        prefApp = context.getSharedPreferences(SHARED_PREF_APP, Context.MODE_PRIVATE);
    }

    @SuppressLint("ApplySharedPref")
    public void resetSettings() {
        prefApp.edit().clear().commit();
    }

    public static AppSettings get() {
        return new AppSettings(App.get());
    }

    public Context getApplicationContext() {
        return context;
    }

    public void clearAppSettings() {
        prefApp.edit().clear().commit();
    }

    public void registerPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener value) {
        prefApp.registerOnSharedPreferenceChangeListener(value);
    }

    public void unregisterPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener value) {
        prefApp.unregisterOnSharedPreferenceChangeListener(value);
    }
    //###########################################
    //## Helpers for setting/getting based on
    //## type; Key based on resources
    //############################################

    private String getKey(int stringKeyResourceId) {
        return context.getString(stringKeyResourceId);
    }

    public boolean isKeyEqual(String key, int stringKeyRessourceId) {
        return key.equals(getKey(stringKeyRessourceId));
    }

    private void setString(int keyRessourceId, String value) {
        prefApp.edit().putString(context.getString(keyRessourceId), value).apply();
    }

    private void setInt(int keyRessourceId, int value) {
        prefApp.edit().putInt(context.getString(keyRessourceId), value).apply();
    }

    private void setLong(int keyRessourceId, long value) {
        prefApp.edit().putLong(context.getString(keyRessourceId), value).apply();
    }

    private void setBool(int keyRessourceId, boolean value) {
        prefApp.edit().putBoolean(context.getString(keyRessourceId), value).apply();
    }

    private void setStringArray(int keyRessourceId, Object[] values) {
        StringBuilder sb = new StringBuilder();
        for (Object value : values) {
            sb.append("%%%");
            sb.append(value.toString());
        }
        setString(keyRessourceId, sb.toString().replaceFirst("%%%", ""));
    }

    private String[] getStringArray(int keyRessourceId) {
        String value = prefApp.getString(context.getString(keyRessourceId), "%%%");
        if (value.equals("%%%")) {
            return new String[0];
        }
        return value.split("%%%");
    }

    private String getString(int ressourceId, String defaultValue) {
        return prefApp.getString(context.getString(ressourceId), defaultValue);
    }

    private String getString(int ressourceId, int ressourceIdDefaultValue) {
        return prefApp.getString(context.getString(ressourceId), context.getString(ressourceIdDefaultValue));
    }

    private boolean getBool(int ressourceId, boolean defaultValue) {
        return prefApp.getBoolean(context.getString(ressourceId), defaultValue);
    }

    private int getInt(int ressourceId, int defaultValue) {
        return prefApp.getInt(context.getString(ressourceId), defaultValue);
    }

    private int getIntOfStringPref(int ressourceId, int defaultValue) {
        String strNum = prefApp.getString(context.getString(ressourceId), Integer.toString(defaultValue));
        return Integer.valueOf(strNum);
    }

    private long getLong(int ressourceId, long defaultValue) {
        return prefApp.getLong(context.getString(ressourceId), defaultValue);
    }

    private int getColor(String key, int defaultColor) {
        return prefApp.getInt(key, defaultColor);
    }

    private int getColorRes(@ColorRes int resColorId) {
        return ContextCompat.getColor(context, resColorId);
    }

    private void setDouble(int keyResId, double value) {
        prefApp.edit().putLong(context.getString(keyResId), Double.doubleToRawLongBits(value)).apply();
    }

    private double getDouble(int keyResId, double defaultValue) {
        if (!prefApp.contains(context.getString(keyResId))) {
            return defaultValue;
        }
        return Double.longBitsToDouble(prefApp.getLong(context.getString(keyResId), 0));
    }

    private void setIntList(int keyResId, ArrayList<Integer> values) {
        StringBuilder sb = new StringBuilder();
        for (int value : values) {
            sb.append(ARRAY_SEPERATOR);
            sb.append(Integer.toString(value));
        }
        setString(keyResId, sb.toString().replaceFirst(ARRAY_SEPERATOR, ""));
    }

    private ArrayList<Integer> getIntList(int keyResId) {
        ArrayList<Integer> ret = new ArrayList<>();
        String value = getString(keyResId, ARRAY_SEPERATOR);
        if (value.equals(ARRAY_SEPERATOR)) {
            return ret;
        }
        for (String s : value.split(ARRAY_SEPERATOR)) {
            ret.add(Integer.parseInt(s));
        }
        return ret;
    }

    //#################################
    //##
    //## Getter & Setter for settings
    //##
    //#################################
    public int getIconsizeGlobal() {
        return getInt(R.string.pref_key__iconsize_global, 58);
    }

    public boolean isAppFirstLaunch() {
        return getBool(R.string.pref_key__is_app_first_start, true);
    }

    public void setAppFirstLaunch(boolean value) {
        setBool(R.string.pref_key__is_app_first_start, value);
    }

    public int getDesktopMode() {
        return getIntOfStringPref(R.string.pref_key__desktop_mode, Desktop.DesktopMode.NORMAL);
    }

    public boolean isDesktopLocked() {
        return getBool(R.string.pref_key__is_desktop_locked, false);
    }

    public void setDesktopLocked(boolean value) {
        setBool(R.string.pref_key__is_desktop_locked, value);
    }

    // Is saved by ListPreference as string
    public int getAppDrawerMode() {
        return getIntOfStringPref(R.string.pref_key__drawer_mode, AppDrawerController.DrawerMode.VERTICAL);
    }

    public String getIconpackPackage() {
        return getString(R.string.pref_key__icon_pack_name, "");
    }

    public void setIconpackPackage(String value) {
        setString(R.string.pref_key__icon_pack_name, value);
    }

    public boolean isDockShowLabel() {
        return getBool(R.string.pref_key__is_dock_show_label, false);
    }

    public int getDrawerItemCountHorizontal_Portrait() {
        return getInt(R.string.pref_key__drawer_item_count_horizontal__portrait, 5);
    }

    public int getDrawerItemCountVertical_Portrait() {
        return getInt(R.string.pref_key__drawer_item_count_vertical__portrait, 6);
    }

    public int getDrawerItemCountHorizontal_Landscape() {
        return getInt(R.string.pref_key__drawer_item_count_horizontal__landscape, 5);
    }

    public int getDrawerItemCountVertical_Landscape() {
        return getInt(R.string.pref_key__drawer_item_count_vertical__landscape, 3);
    }

    public int getDesktopItemCountHorizontal() {
        return getInt(R.string.pref_key__desktop_item_count_horizontal, 5);
    }

    public int getDesktopItemCountVertical() {
        return getInt(R.string.pref_key__desktop_item_count_vertical, 6);
    }

    public int getDockItemCountHorizontal() {
        return getInt(R.string.pref_key__dock_item_count_horizontal, 5);
    }

    public boolean isMinibarEnabled() {
        return getBool(R.string.pref_key__is_minibar_enabled, true);
    }

    public void setMinibarEnabled(boolean value) {
        setBool(R.string.pref_key__is_minibar_enabled, value);
    }

    public ArrayList<String> getMinibarArrangement() {
        return new ArrayList<>(Arrays.asList(getStringArray(R.string.pref_key__minibar__arrangement__tmp)));
    }

    public void setMinibarArrangement(ArrayList<String> value) {
        setStringArray(R.string.pref_key__minibar__arrangement__tmp, value.toArray(new String[value.size()]));
    }

    public boolean isDesktopSearchbarEnabled() {
        return getBool(R.string.pref_key__is_desktop_searchbar_enabled, true);
    }

    public boolean isDesktopFullscreen() {
        return getBool(R.string.pref_key__is_desktop_fullscreen, false);
    }

    public boolean isDesktopShowPageIndicator() {
        return getBool(R.string.pref_key__is_desktop_show_page_indicator, true);
    }

    public boolean isDesktopShowLabel() {
        return getBool(R.string.pref_key__is_desktop_show_label, true);
    }

    public int getDesktopPageCurrent() {
        return getInt(R.string.pref_key__desktop_page_current, 0);
    }

    public void setDesktopPageCurrent(int value) {
        setInt(R.string.pref_key__desktop_page_current, value);
    }

    public boolean isOpenAppDrawerOnSwipe() {
        return getBool(R.string.pref_key__is_open_app_drawer_on_swipe, true);
    }

    public boolean isDrawerUseCard() {
        return getBool(R.string.pref_key__is_drawer_use_card, true);
    }

    public boolean isDrawerRememberPage() {
        return getBool(R.string.pref_key__is_drawer_remember_page, true);
    }

    public boolean isDrawerShowIndicator() {
        return getBool(R.string.pref_key__is_drawer_show_indicator, true);
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

    public int getDockColor() {
        return getInt(R.string.pref_key__dock_background_color, Color.TRANSPARENT);
    }

    public ArrayList<String> getHiddenAppsList() {
        return new ArrayList<>(Arrays.asList(getStringArray(R.string.pref_key__hidden_apps_list)));
    }

    public void setHiddenAppsList(ArrayList<String> value) {
        setStringArray(R.string.pref_key__hidden_apps_list, value.toArray(new String[value.size()]));
    }

    public boolean isAppRestartRequired() {
        return getBool(R.string.pref_key__is_app_restart_required, false);
    }

    @SuppressLint("ApplySharedPref")
    public void setAppRestartRequired(boolean value) {
        // MUST be committed
        prefApp.edit().putBoolean(context.getString
                (R.string.pref_key__is_app_restart_required), value).commit();
    }
}
