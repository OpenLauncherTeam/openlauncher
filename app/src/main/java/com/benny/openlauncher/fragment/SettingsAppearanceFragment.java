package com.benny.openlauncher.fragment;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.util.AppManager;

import net.gsantner.opoc.util.ContextUtils;

public class SettingsAppearanceFragment extends SettingsBaseFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_appearance);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        HomeActivity homeActivity = HomeActivity._launcher;
        int key = new ContextUtils(homeActivity).getResId(ContextUtils.ResType.STRING, preference.getKey());
        switch (key) {
            case R.string.pref_key__icon_pack:
                AppManager.getInstance(getActivity()).startPickIconPackIntent(getActivity());
                return true;
        }
        return false;
    }
}
