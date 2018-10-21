package com.benny.openlauncher.fragment;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;

import com.benny.openlauncher.R;
import com.benny.openlauncher.util.AppSettings;

import net.gsantner.opoc.util.ContextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class SettingsBaseFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final List<Integer> noRestart = new ArrayList<>(Arrays.asList(
            R.string.pref_key__gesture_double_tap, R.string.pref_key__gesture_swipe_up,
            R.string.pref_key__gesture_swipe_down, R.string.pref_key__gesture_pinch,
            R.string.pref_key__gesture_unpinch));

    @Override
    public void onResume() {
        super.onResume();
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setTitle(getPreferenceScreen().getTitle());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        PreferenceGroup prefGroup = getPreferenceScreen();
        tintIcons(prefGroup);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (!noRestart.contains(key)) {
            AppSettings.get().setAppRestartRequired(true);
        }
    }

    public void tintIcons(PreferenceGroup prefGroup) {
        if (prefGroup != null && isAdded()) {
            int prefCount = prefGroup.getPreferenceCount();
            for (int i = 0; i < prefCount; i++) {
                Preference preference = prefGroup.getPreference(i);
                if (preference != null) {
                    Drawable drawable = preference.getIcon();
                    if (drawable != null) {
                        drawable.mutate().setColorFilter(getResources().getColor(R.color.Dark_TextColor), PorterDuff.Mode.SRC_IN);
                    }
                    if (preference instanceof PreferenceGroup) {
                        tintIcons((PreferenceGroup) preference);
                    }
                }
            }
        }
    }
}
