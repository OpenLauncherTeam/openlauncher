package com.benny.openlauncher.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.Preference;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.util.Definitions;
import com.benny.openlauncher.util.LauncherAction;
import com.nononsenseapps.filepicker.FilePickerActivity;

import net.gsantner.opoc.util.ContextUtils;
import net.gsantner.opoc.util.PermissionChecker;

public class SettingsAndroidTVFragment extends SettingsBaseFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        addPreferencesFromResource(R.xml.preferences_android_tv);

        Preference preference = findPreference(getContext().getResources().getString(R.string.pref_key__android_tv_settings_choose_wallpaper));
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                //startActivityForResult(Intent.createChooser(intent, "aaaa"), PICK_IMAGE);
                getActivity().startActivityForResult(intent, Definitions.ANDROID_TV_PICK_WALLPAPER);
                return true;
            }
        });
    }

}
