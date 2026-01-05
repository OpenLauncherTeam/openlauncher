package com.benny.openlauncher.fragment;

import android.os.Bundle;
import androidx.preference.Preference;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.viewutil.DialogHelper;

import net.gsantner.opoc.util.ContextUtils;

public class SettingsAppearanceFragment extends SettingsBaseFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        addPreferencesFromResource(R.xml.preferences_appearance);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        HomeActivity homeActivity = HomeActivity._launcher;
        int key = new ContextUtils(homeActivity).getResId(ContextUtils.ResType.STRING, preference.getKey());
        if (key == R.string.pref_key__icon_pack) {
            DialogHelper.startPickIconPackIntent(getActivity());
            return true;
        }
        return false;
    }
}
