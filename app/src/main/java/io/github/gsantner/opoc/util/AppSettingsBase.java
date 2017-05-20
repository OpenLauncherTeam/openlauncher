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
import java.util.Arrays;
import java.util.List;


/**
 * Wrapper for settings based on SharedPreferences
 * with keys in resources
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class AppSettingsBase {
    protected static final String ARRAY_SEPARATOR = "%%%";
    protected static final String ARRAY_SEPARATOR_SUBSTITUTE = "§§§";
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
    //## Getter for resources
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

    public String getString(@StringRes int keyResourceId, String defaultValue) {
        return getString(prefApp, keyResourceId, defaultValue);
    }

    public String getString(SharedPreferences pref, @StringRes int keyResourceId, String defaultValue) {
        return pref.getString(rstr(keyResourceId), defaultValue);
    }

    public String getString(@StringRes int keyResourceId, @StringRes int keyResourceIdDefaultValue) {
        return getString(prefApp, keyResourceId, keyResourceIdDefaultValue);
    }

    public String getString(SharedPreferences pref, @StringRes int keyResourceId, @StringRes int keyResourceIdDefaultValue) {
        return pref.getString(rstr(keyResourceId), rstr(keyResourceIdDefaultValue));
    }

    public void setStringArray(@StringRes int keyResourceId, Object[] values) {
        setStringArray(prefApp, keyResourceId, values);
    }

    public void setStringArray(SharedPreferences pref, @StringRes int keyResourceId, Object[] values) {
        StringBuilder sb = new StringBuilder();
        for (Object value : values) {
            sb.append(ARRAY_SEPARATOR);
            sb.append(value.toString().replace(ARRAY_SEPARATOR, ARRAY_SEPARATOR_SUBSTITUTE));
        }
        setString(pref, keyResourceId, sb.toString().replaceFirst(ARRAY_SEPARATOR, ""));
    }

    @NonNull
    public String[] getStringArray(@StringRes int keyResourceId) {
        return getStringArray(prefApp, keyResourceId);
    }

    @NonNull
    public String[] getStringArray(SharedPreferences pref, @StringRes int keyResourceId) {
        String value = pref.getString(rstr(keyResourceId), ARRAY_SEPARATOR);
        if (value.equals(ARRAY_SEPARATOR)) {
            return new String[0];
        }
        return value.split(ARRAY_SEPARATOR);
    }

    public void setStringList(@StringRes int keyResourceId, List<String> values) {
        setStringList(prefApp, keyResourceId, values);
    }

    public void setStringList(SharedPreferences pref, @StringRes int keyResourceId, List<String> values) {
        setStringArray(pref, keyResourceId, values.toArray(new String[values.size()]));
    }

    public ArrayList<String> getStringList(@StringRes int keyResourceId) {
        return getStringList(prefApp, keyResourceId);
    }

    public ArrayList<String> getStringList(SharedPreferences pref, @StringRes int keyResourceId) {
        return new ArrayList<>(Arrays.asList(getStringArray(pref, keyResourceId)));
    }

    public void setLong(@StringRes int keyResourceId, long value) {
        setLong(prefApp, keyResourceId, value);
    }

    public void setLong(SharedPreferences pref, @StringRes int keyResourceId, long value) {
        pref.edit().putLong(rstr(keyResourceId), value).apply();
    }

    public long getLong(@StringRes int keyResourceId, long defaultValue) {
        return getLong(prefApp, keyResourceId, defaultValue);
    }

    public long getLong(SharedPreferences pref, @StringRes int keyResourceId, long defaultValue) {
        return pref.getLong(rstr(keyResourceId), defaultValue);
    }

    public void setBool(@StringRes int keyResourceId, boolean value) {
        setBool(prefApp, keyResourceId, value);
    }

    public void setBool(SharedPreferences pref, @StringRes int keyResourceId, boolean value) {
        pref.edit().putBoolean(rstr(keyResourceId), value).apply();
    }

    public boolean getBool(@StringRes int keyResourceId, boolean defaultValue) {
        return getBool(prefApp, keyResourceId, defaultValue);
    }

    public boolean getBool(SharedPreferences pref, @StringRes int keyResourceId, boolean defaultValue) {
        return pref.getBoolean(rstr(keyResourceId), defaultValue);
    }

    public int getColor(String key, int defaultColor) {
        return getColor(prefApp, key, defaultColor);
    }

    public int getColor(SharedPreferences pref, String key, int defaultColor) {
        return pref.getInt(key, defaultColor);
    }

    public int getColor(@StringRes int keyResourceId, int defaultColor) {
        return getColor(prefApp, keyResourceId, defaultColor);
    }

    public int getColor(SharedPreferences pref, @StringRes int keyResourceId, int defaultColor) {
        return pref.getInt(rstr(keyResourceId), defaultColor);
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

    public int getIntOfStringPref(@StringRes int keyResId, int defaultValue) {
        String strNum = prefApp.getString(context.getString(keyResId), Integer.toString(defaultValue));
        return Integer.valueOf(strNum);
    }

    public void setInt(@StringRes int keyResourceId, int value) {
        setInt(prefApp, keyResourceId, value);
    }

    public void setInt(SharedPreferences pref, @StringRes int keyResourceId, int value) {
        pref.edit().putInt(rstr(keyResourceId), value).apply();
    }

    public int getInt(@StringRes int keyResourceId, int defaultValue) {
        return getInt(prefApp, keyResourceId, defaultValue);
    }

    public int getInt(SharedPreferences pref, @StringRes int keyResourceId, int defaultValue) {
        return pref.getInt(rstr(keyResourceId), defaultValue);
    }

    public void setIntList(@StringRes int keyResId, List<Integer> values) {
        setIntList(prefApp, keyResId, values);
    }

    public void setIntList(SharedPreferences pref, @StringRes int keyResId, List<Integer> values) {
        StringBuilder sb = new StringBuilder();
        for (int value : values) {
            sb.append(ARRAY_SEPARATOR);
            sb.append(Integer.toString(value));
        }
        setString(prefApp, keyResId, sb.toString().replaceFirst(ARRAY_SEPARATOR, ""));
    }

    @NonNull
    public ArrayList<Integer> getIntList(@StringRes int keyResId) {
        return getIntList(prefApp, keyResId);
    }

    @NonNull
    public ArrayList<Integer> getIntList(SharedPreferences pref, @StringRes int keyResId) {
        ArrayList<Integer> ret = new ArrayList<>();
        String value = getString(prefApp, keyResId, ARRAY_SEPARATOR);
        if (value.equals(ARRAY_SEPARATOR)) {
            return ret;
        }
        for (String s : value.split(ARRAY_SEPARATOR)) {
            ret.add(Integer.parseInt(s));
        }
        return ret;
    }
}
