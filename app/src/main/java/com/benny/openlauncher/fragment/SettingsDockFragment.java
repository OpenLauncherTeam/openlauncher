package com.benny.openlauncher.fragment;

import android.os.Bundle;

import com.benny.openlauncher.R;

public class SettingsDockFragment extends SettingsBaseFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_dock);
    }
}
