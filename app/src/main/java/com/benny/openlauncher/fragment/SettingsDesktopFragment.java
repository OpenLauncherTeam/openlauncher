package com.benny.openlauncher.fragment;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.util.LauncherAction;

import net.gsantner.opoc.util.ContextUtils;

public class SettingsDesktopFragment extends SettingsBaseFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_desktop);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        HomeActivity homeActivity = HomeActivity._launcher;
        int key = new ContextUtils(homeActivity).getResId(ContextUtils.ResType.STRING, preference.getKey());
        switch (key) {
            case R.string.pref_key__minibar:
                LauncherAction.RunAction(LauncherAction.Action.EditMinibar, getActivity());
                return true;
        }
        return false;
    }
}
