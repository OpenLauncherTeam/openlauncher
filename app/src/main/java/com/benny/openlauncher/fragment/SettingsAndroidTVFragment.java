package com.benny.openlauncher.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.Preference;


import com.benny.openlauncher.R;
import com.benny.openlauncher.util.Definitions;
import com.nononsenseapps.filepicker.FilePickerActivity;

public class SettingsAndroidTVFragment extends SettingsBaseFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        addPreferencesFromResource(R.xml.preferences_android_tv);

        Preference preference = findPreference(getContext().getResources().getString(R.string.pref_key__android_tv_settings_choose_wallpaper));
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent intent = new Intent(getActivity(), FilePickerActivity.class)
                        .putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false)
                        .putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                getActivity().startActivityForResult(intent, Definitions.ANDROID_TV_PICK_WALLPAPER);
                return true;

            }
        });
    }

}
