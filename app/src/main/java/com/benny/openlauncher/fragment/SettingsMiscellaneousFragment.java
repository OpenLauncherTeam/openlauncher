package com.benny.openlauncher.fragment;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.DatabaseHelper;
import com.benny.openlauncher.util.Definitions;
import com.benny.openlauncher.viewutil.DialogHelper;
import com.nononsenseapps.filepicker.FilePickerActivity;

import net.gsantner.opoc.util.ContextUtils;
import net.gsantner.opoc.util.PermissionChecker;

import java.io.File;

public class SettingsMiscellaneousFragment extends SettingsBaseFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        addPreferencesFromResource(R.xml.preferences_advanced);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        HomeActivity homeActivity = HomeActivity._launcher;
        int key = new ContextUtils(homeActivity).getResId(ContextUtils.ResType.STRING, preference.getKey());
        switch (key) {
            case R.string.pref_key__backup:
                if (new PermissionChecker(getActivity()).doIfExtStoragePermissionGranted()) {
                    Intent i = new Intent(getActivity(), FilePickerActivity.class)
                            .putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true)
                            .putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                    getActivity().startActivityForResult(i, Definitions.INTENT_BACKUP);
                }
                return true;
            case R.string.pref_key__restore:
                if (new PermissionChecker(getActivity()).doIfExtStoragePermissionGranted()) {
                    Intent i = new Intent(getActivity(), FilePickerActivity.class)
                            .putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false)
                            .putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                    getActivity().startActivityForResult(i, Definitions.INTENT_RESTORE);
                }
                return true;
            case R.string.pref_key__reset_settings:
                DialogHelper.alertDialog(getActivity(), getString(R.string.pref_title__reset_settings), getString(R.string.are_you_sure), new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        AppSettings.get().resetSettings();
                        homeActivity.recreate();
                        Toast.makeText(HomeActivity._launcher, R.string.toast_settings_restored, Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            case R.string.pref_key__reset_database:
                DialogHelper.alertDialog(getActivity(), getString(R.string.pref_title__reset_database), getString(R.string.are_you_sure), new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        DatabaseHelper db = HomeActivity._db;
                        db.onUpgrade(db.getWritableDatabase(), 1, 1);
                        AppSettings.get().setAppFirstLaunch(true);
                        homeActivity.recreate();
                        Toast.makeText(HomeActivity._launcher, R.string.toast_database_deleted, Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            case R.string.pref_key__restart:
                homeActivity.recreate();
                getActivity().finish();
                return true;
        }
        return false;
    }
}
