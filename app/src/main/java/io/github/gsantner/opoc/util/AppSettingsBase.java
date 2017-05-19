/*
 * ----------------------------------------------------------------------------
 * "THE COKE-WARE LIBRARY LICENSE" (Revision 255):
 * Gregor Santner <gsantner.github.io> wrote this file. You can do whatever
 * you want with this stuff. If we meet some day, and you think this stuff is
 * worth it, you can buy me a coke in return. Provided as is without any kind
 * of warranty. No attribution required.                  - Gregor Santner
 * ----------------------------------------------------------------------------
 */

/*
 * Get updates:
 *  https://github.com/gsantner/onePieceOfCode/blob/master/java/AppSettingsBase.java
 * This is a wrapper for settings based on SharedPreferences
 * with keys in resources. Extend from this class and add
 * getters/setters for the app's settings.
 * Example:
    public boolean isAppFirstStart() {
    return getBool(prefApp, R.string.pref_key__app_first_start, true);
    }

    public void setAppFirstStart(boolean value) {
    setBool(prefApp, R.string.pref_key__app_first_start, value);
    }

    public boolean isAppFirstStartCurrentVersion() {
    int value = getInt(prefApp, R.string.pref_key__app_first_start_current_version, -1);
    setInt(prefApp, R.string.pref_key__app_first_start_current_version, BuildConfig.VERSION_CODE);
    return value != BuildConfig.VERSION_CODE && !BuildConfig.IS_TEST_BUILD;
    }

 * Maybe add a singleton for this:
 * Whereas App.get() is returning ApplicationContext
    private AppSettings(Context context) {
        super(context);
    }

    public static AppSettings get() {
        return new AppSettings(App.get());
    }
 */

package io.github.gsantner.opoc.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;


/**
 * Wrapper for settings based on SharedPreferences
 * with keys in resources
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class AppSettingsBase {
    protected static final String ARRAY_SEPERATOR = "%%";
    public static final String SHARED_PREF_APP = "app";

    //#####################
    //## Members
    //#####################
    protected final SharedPreferences prefApp;
    protected final Context context;

    //#####################
    //## Methods
    //#####################
    public AppSettingsBase(Context context) {
        this(context, SHARED_PREF_APP);
    }

    public AppSettingsBase(Context context, String prefAppName) {
        this.context = context.getApplicationContext();
        prefApp = this.context.getSharedPreferences(prefAppName, Context.MODE_PRIVATE);
    }

    public Context getContext() {
        return context;
    }

    public boolean isKeyEqual(String key, int stringKeyResourceId) {
        return key.equals(rstr(stringKeyResourceId));
    }

    public void resetSettings() {
        resetSettings(prefApp);
    }

    /**
     * Clear all settings in prefApp (related to the App itself)
     * This uses commit instead of apply, since this should be
     * applied immediately.
     */
    @SuppressLint("ApplySharedPref")
    public void resetSettings(SharedPreferences pref) {
        pref.edit().clear().commit();
    }

    public boolean isPrefSet(@StringRes int stringKeyResourceId) {
        return isPrefSet(prefApp, stringKeyResourceId);
    }

    public boolean isPrefSet(SharedPreferences pref, @StringRes int stringKeyResourceId) {
        return pref.contains(rstr(stringKeyResourceId));
    }

    public void registerPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener value) {
        registerPreferenceChangedListener(prefApp, value);
    }

    public void registerPreferenceChangedListener(SharedPreferences pref, SharedPreferences.OnSharedPreferenceChangeListener value) {
        pref.registerOnSharedPreferenceChangeListener(value);
    }

    public void unregisterPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener value) {
        unregisterPreferenceChangedListener(prefApp, value);
    }

    public void unregisterPreferenceChangedListener(SharedPreferences pref, SharedPreferences.OnSharedPreferenceChangeListener value) {
        pref.unregisterOnSharedPreferenceChangeListener(value);
    }

    //#################################
    //## Getter & Setter for resources
    //#################################
    public String rstr(@StringRes int stringKeyResourceId) {
        return context.getString(stringKeyResourceId);
    }

    public int rcolor(@ColorRes int resColorId) {
        return ContextCompat.getColor(context, resColorId);
    }

    //#################################
    //## Getter & Setter for settings
    //#################################
    public void setString(@StringRes int keyResourceId, String value) {
        setString(prefApp, keyResourceId, value);
    }

    public void setString(SharedPreferences pref, @StringRes int keyResourceId, String value) {
        pref.edit().putString(rstr(keyResourceId), value).apply();
    }

    public String getString(@StringRes int ressourceId, String defaultValue) {
        return getString(prefApp, ressourceId, defaultValue);
    }

    public String getString(SharedPreferences pref, @StringRes int ressourceId, String defaultValue) {
        return pref.getString(rstr(ressourceId), defaultValue);
    }

    public String getString(@StringRes int ressourceId, @StringRes int ressourceIdDefaultValue) {
        return getString(prefApp, ressourceId, ressourceIdDefaultValue);
    }

    public String getString(SharedPreferences pref, @StringRes int ressourceId, @StringRes int ressourceIdDefaultValue) {
        return pref.getString(rstr(ressourceId), rstr(ressourceIdDefaultValue));
    }

    public void setStringArray(@StringRes int keyResourceId, Object[] values) {
        setStringArray(prefApp, keyResourceId, values);
    }

    public void setStringArray(SharedPreferences pref, @StringRes int keyResourceId, Object[] values) {
        StringBuilder sb = new StringBuilder();
        for (Object value : values) {
            sb.append("%%%");
            sb.append(value.toString());
        }
        setString(pref, keyResourceId, sb.toString().replaceFirst("%%%", ""));
    }

    @NonNull
    public String[] getStringArray(@StringRes int keyResourceId) {
        return getStringArray(prefApp, keyResourceId);
    }

    @NonNull
    public String[] getStringArray(SharedPreferences pref, @StringRes int keyResourceId) {
        String value = pref.getString(rstr(keyResourceId), "%%%");
        if (value.equals("%%%")) {
            return new String[0];
        }
        return value.split("%%%");
    }

    public void setLong(@StringRes int keyResourceId, long value) {
        setLong(prefApp, keyResourceId, value);
    }

    public void setLong(SharedPreferences pref, @StringRes int keyResourceId, long value) {
        pref.edit().putLong(rstr(keyResourceId), value).apply();
    }

    public long getLong(@StringRes int ressourceId, long defaultValue) {
        return getLong(prefApp, ressourceId, defaultValue);
    }

    public long getLong(SharedPreferences pref, @StringRes int ressourceId, long defaultValue) {
        return pref.getLong(rstr(ressourceId), defaultValue);
    }

    public void setBool(@StringRes int keyResourceId, boolean value) {
        setBool(prefApp, keyResourceId, value);
    }

    public void setBool(SharedPreferences pref, @StringRes int keyResourceId, boolean value) {
        pref.edit().putBoolean(rstr(keyResourceId), value).apply();
    }

    public boolean getBool(@StringRes int ressourceId, boolean defaultValue) {
        return getBool(prefApp, ressourceId, defaultValue);
    }

    public boolean getBool(SharedPreferences pref, @StringRes int ressourceId, boolean defaultValue) {
        return pref.getBoolean(rstr(ressourceId), defaultValue);
    }

    public int getColor(String key, int defaultColor) {
        return getColor(prefApp, key, defaultColor);
    }

    public int getColor(SharedPreferences pref, String key, int defaultColor) {
        return pref.getInt(key, defaultColor);
    }

    public int getColor(@StringRes int ressourceId, int defaultColor) {
        return getColor(prefApp, ressourceId, defaultColor);
    }

    public int getColor(SharedPreferences pref, @StringRes int ressourceId, int defaultColor) {
        return pref.getInt(rstr(ressourceId), defaultColor);
    }

    public void setDouble(@StringRes int keyResId, double value) {
        setDouble(prefApp, keyResId, value);
    }

    public void setDouble(SharedPreferences pref, @StringRes int keyResId, double value) {
        prefApp.edit().putLong(rstr(keyResId), Double.doubleToRawLongBits(value)).apply();
    }

    public double getDouble(@StringRes int keyResId, double defaultValue) {
        return getDouble(prefApp, keyResId, defaultValue);
    }

    public double getDouble(SharedPreferences pref, @StringRes int keyResId, double defaultValue) {
        return Double.longBitsToDouble(prefApp.getLong(rstr(keyResId), Double.doubleToLongBits(defaultValue)));
    }

    public void setInt(@StringRes int keyResourceId, int value) {
        setInt(prefApp, keyResourceId, value);
    }

    public void setInt(SharedPreferences pref, @StringRes int keyResourceId, int value) {
        pref.edit().putInt(rstr(keyResourceId), value).apply();
    }

    public int getInt(@StringRes int ressourceId, int defaultValue) {
        return getInt(prefApp, ressourceId, defaultValue);
    }

    public int getInt(SharedPreferences pref, @StringRes int ressourceId, int defaultValue) {
        return pref.getInt(rstr(ressourceId), defaultValue);
    }

    public void setIntList(@StringRes int keyResId, ArrayList<Integer> values) {
        setIntList(prefApp, keyResId, values);
    }

    public int getIntOfStringPref(@StringRes int keyResId, int defaultValue) {
        String strNum = prefApp.getString(context.getString(keyResId), Integer.toString(defaultValue));
        return Integer.valueOf(strNum);
    }

    public void setIntList(SharedPreferences pref, @StringRes int keyResId, ArrayList<Integer> values) {
        StringBuilder sb = new StringBuilder();
        for (int value : values) {
            sb.append(ARRAY_SEPERATOR);
            sb.append(Integer.toString(value));
        }
        setString(prefApp, keyResId, sb.toString().replaceFirst(ARRAY_SEPERATOR, ""));
    }

    @NonNull
    public ArrayList<Integer> getIntList(@StringRes int keyResId) {
        return getIntList(prefApp, keyResId);
    }

    @NonNull
    public ArrayList<Integer> getIntList(SharedPreferences pref, @StringRes int keyResId) {
        ArrayList<Integer> ret = new ArrayList<>();
        String value = getString(prefApp, keyResId, ARRAY_SEPERATOR);
        if (value.equals(ARRAY_SEPERATOR)) {
            return ret;
        }
        for (String s : value.split(ARRAY_SEPERATOR)) {
            ret.add(Integer.parseInt(s));
        }
        return ret;
    }
}
