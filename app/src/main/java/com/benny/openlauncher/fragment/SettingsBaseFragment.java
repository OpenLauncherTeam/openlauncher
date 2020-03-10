package com.benny.openlauncher.fragment;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.appcompat.widget.Toolbar;
import android.util.TypedValue;

import com.benny.openlauncher.R;
import com.benny.openlauncher.util.AppSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class SettingsBaseFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final List<Integer> noRestart = new ArrayList<>(Arrays.asList(
            R.string.pref_key__gesture_double_tap,
            R.string.pref_key__gesture_swipe_up,
            R.string.pref_key__gesture_swipe_down,
            R.string.pref_key__gesture_pinch_in,
            R.string.pref_key__gesture_pinch_out
    ));

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName("app");
    }

    @Override
    public void onResume() {
        super.onResume();

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setTitle(getPreferenceScreen().getTitle());

        SharedPreferences sharedPreferences = AppSettings.get().getDefaultPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        updateIcons(getPreferenceScreen());
        updateSummaries();
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences sharedPreferences = AppSettings.get().getDefaultPreferences();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSummaries();
        if (!noRestart.contains(key)) {
            AppSettings.get().setAppRestartRequired(true);
        }
    }

    public void updateSummaries() {
        // override in fragments
    }

    public void updateIcons(PreferenceGroup prefGroup) {
        if (prefGroup != null && isAdded()) {
            int prefCount = prefGroup.getPreferenceCount();
            for (int i = 0; i < prefCount; i++) {
                Preference preference = prefGroup.getPreference(i);
                if (preference != null) {
                    Drawable drawable = preference.getIcon();
                    if (drawable != null) {
                        TypedValue color = new TypedValue();
                        getContext().getTheme().resolveAttribute(android.R.attr.textColor, color, true);
                        drawable.mutate().setColorFilter(getResources().getColor(color.resourceId), PorterDuff.Mode.SRC_IN);
                    }

                    if (preference instanceof PreferenceGroup) {
                        updateIcons((PreferenceGroup) preference);
                    }
                }
            }
        }
    }
}
