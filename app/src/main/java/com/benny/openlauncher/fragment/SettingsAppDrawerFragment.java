package com.benny.openlauncher.fragment;

import android.os.Bundle;

import com.benny.openlauncher.R;

public class SettingsAppDrawerFragment extends SettingsBaseFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        addPreferencesFromResource(R.xml.preferences_app_drawer);
    }
}