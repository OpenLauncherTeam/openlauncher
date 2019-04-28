package com.benny.openlauncher.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.MoreInfoActivity;
import com.benny.openlauncher.activity.SettingsActivity;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.widget.AppDrawerController;

import net.gsantner.opoc.preference.GsPreferenceFragmentCompat;

import java.util.Locale;

public class SettingsMasterFragment extends GsPreferenceFragmentCompat<AppSettings> {
    public static final String TAG = "com.benny.openlauncher.fragment.SettingsMasterFragment";
    protected AppSettings _as;
    private int activityRetVal;

    @Override
    public int getPreferenceResourceForInflation() {
        return R.xml.preferences_master;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    protected AppSettings getAppSettings(Context context) {
        if (_as == null) {
            _as = new AppSettings(context);
        }
        return _as;
    }

    @Override
    public Integer getIconTintColor() {
        return Color.BLACK;
    }

    @Override
    protected void onPreferenceScreenChanged(PreferenceFragmentCompat preferenceFragmentCompat, PreferenceScreen preferenceScreen) {
        super.onPreferenceScreenChanged(preferenceFragmentCompat, preferenceScreen);
        if (!TextUtils.isEmpty(preferenceScreen.getTitle())) {
            SettingsActivity a = (SettingsActivity) getActivity();
            if (a != null) {
                a.toolbar.setTitle(preferenceScreen.getTitle());
            }
        }
    }

    @Override
    public void doUpdatePreferences() {
        updateSummary(R.string.pref_key__cat_desktop, String.format(Locale.ENGLISH, "%s: %d x %d", getString(R.string.pref_title__size), _as.getDesktopColumnCount(), _as.getDesktopRowCount()));
        updateSummary(R.string.pref_key__cat_dock, String.format(Locale.ENGLISH, "%s: %d x %d", getString(R.string.pref_title__size), _as.getDockColumnCount(), _as.getDockRowCount()));
        updateSummary(R.string.pref_key__cat_appearance, String.format(Locale.ENGLISH, "Icons: %ddp", _as.getIconSize()));

        switch (_as.getDrawerStyle()) {
            case AppDrawerController.Mode.GRID:
                updateSummary(R.string.pref_key__cat_app_drawer, String.format("%s: %s", getString(R.string.pref_title__style), getString(R.string.vertical_scroll_drawer)));
                break;
            case AppDrawerController.Mode.PAGE:
            default:
                updateSummary(R.string.pref_key__cat_app_drawer, String.format("%s: %s", getString(R.string.pref_title__style), getString(R.string.horizontal_paged_drawer)));
                break;
        }
    }

    @Override
    protected void onPreferenceChanged(SharedPreferences prefs, String key) {
        super.onPreferenceChanged(prefs, key);
        activityRetVal = 1;
    }

    @Override
    @SuppressWarnings({"ConstantIfStatement"})
    public Boolean onPreferenceClicked(Preference preference, String key, int keyResId) {
        switch (keyResId) {
            case R.string.pref_key__about: {
                startActivity(new Intent(getActivity(), MoreInfoActivity.class));
                return true;
            }
        }
        return null;
    }

    @Override
    public boolean isDividerVisible() {
        return true;

    }
}