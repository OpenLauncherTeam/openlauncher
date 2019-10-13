package com.benny.openlauncher.fragment;

import android.os.Bundle;
import android.support.v7.preference.Preference;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.util.LauncherAction;

import net.gsantner.opoc.util.ContextUtils;

public class SettingsDesktopFragment extends SettingsBaseFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        addPreferencesFromResource(R.xml.preferences_desktop);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
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
