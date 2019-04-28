package com.benny.openlauncher.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.activity.MoreInfoActivity;
import com.benny.openlauncher.activity.SettingsActivity;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.DatabaseHelper;
import com.benny.openlauncher.util.Definitions;
import com.benny.openlauncher.viewutil.DialogHelper;
import com.benny.openlauncher.widget.AppDrawerController;
import com.nononsenseapps.filepicker.FilePickerActivity;

import net.gsantner.opoc.preference.GsPreferenceFragmentCompat;
import net.gsantner.opoc.util.PermissionChecker;

import java.io.File;
import java.util.Locale;

public class SettingsMasterFragment extends GsPreferenceFragmentCompat<AppSettings> {
    public static final String TAG = "com.benny.openlauncher.fragment.SettingsMasterFragment";
    protected AppSettings _as;
    private int activityRetVal;

    @Override
    public int getPreferenceResourceForInflation() {
        return R.xml.preferences_master;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    protected AppSettings getAppSettings(Context context) {
        if (_as == null) {
            _as = new AppSettings(context);
        }
        return _as;
    }

    @Override
    public Integer getIconTintColor() {
        return Color.BLACK;
    }

    @Override
    protected void onPreferenceScreenChanged(PreferenceFragmentCompat preferenceFragmentCompat, PreferenceScreen preferenceScreen) {
        super.onPreferenceScreenChanged(preferenceFragmentCompat, preferenceScreen);
        if (!TextUtils.isEmpty(preferenceScreen.getTitle())) {
            SettingsActivity a = (SettingsActivity) getActivity();
            if (a != null) {
                a.toolbar.setTitle(preferenceScreen.getTitle());
            }
        }
    }

    @Override
    public void doUpdatePreferences() {
        updateSummary(R.string.pref_key__cat_desktop, String.format(Locale.ENGLISH, "%s: %d x %d", getString(R.string.pref_title__size), _as.getDesktopColumnCount(), _as.getDesktopRowCount()));
        updateSummary(R.string.pref_key__cat_dock, String.format(Locale.ENGLISH, "%s: %d x %d", getString(R.string.pref_title__size), _as.getDockColumnCount(), _as.getDockRowCount()));
        updateSummary(R.string.pref_key__cat_appearance, String.format(Locale.ENGLISH, "Icons: %ddp", _as.getIconSize()));

        switch (_as.getDrawerStyle()) {
            case AppDrawerController.Mode.GRID:
                updateSummary(R.string.pref_key__cat_app_drawer, String.format("%s: %s", getString(R.string.pref_title__style), getString(R.string.vertical_scroll_drawer)));
                break;
            case AppDrawerController.Mode.PAGE:
            default:
                updateSummary(R.string.pref_key__cat_app_drawer, String.format("%s: %s", getString(R.string.pref_title__style), getString(R.string.horizontal_paged_drawer)));
                break;
        }
    }

    @Override
    protected void onPreferenceChanged(SharedPreferences prefs, String key) {
        super.onPreferenceChanged(prefs, key);
        activityRetVal = 1;
    }

    @Override
    @SuppressWarnings({"ConstantIfStatement"})
    public Boolean onPreferenceClicked(Preference preference, String key, int keyResId) {
        HomeActivity homeActivity = HomeActivity._launcher;
        switch (keyResId) {
            case R.string.pref_key__about: {
                startActivity(new Intent(getActivity(), MoreInfoActivity.class));
                return true;
            }
            case R.string.pref_key__backup: {
                if (new PermissionChecker(getActivity()).doIfExtStoragePermissionGranted()) {
                    Intent i = new Intent(getActivity(), FilePickerActivity.class)
                            .putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true)
                            .putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                    getActivity().startActivityForResult(i, Definitions.INTENT_BACKUP);
                }
                return true;
            }
            case R.string.pref_key__restore: {
                if (new PermissionChecker(getActivity()).doIfExtStoragePermissionGranted()) {
                    Intent i = new Intent(getActivity(), FilePickerActivity.class)
                            .putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false)
                            .putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                    getActivity().startActivityForResult(i, Definitions.INTENT_RESTORE);
                }
                return true;
            }
            case R.string.pref_key__reset_settings: {
                DialogHelper.alertDialog(getActivity(), getString(R.string.pref_title__reset_settings), getString(R.string.are_you_sure), new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        try {
                            PackageInfo p = getActivity().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
                            String dataDir = p.applicationInfo.dataDir;
                            new File(dataDir + "/shared_prefs/app.xml").delete();
                            System.exit(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                return true;
            }
            case R.string.pref_key__reset_database: {
                DialogHelper.alertDialog(getActivity(), getString(R.string.pref_title__reset_database), getString(R.string.are_you_sure), new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        DatabaseHelper db = HomeActivity._db;
                        db.onUpgrade(db.getWritableDatabase(), 1, 1);
                        AppSettings.get().setAppFirstLaunch(true);
                        System.exit(0);
                    }
                });
                return true;
            }
            case R.string.pref_key__restart: {
                homeActivity.recreate();
                getActivity().finish();
                return true;
            }
        }
        return null;
    }

    @Override
    public boolean isDividerVisible() {
        return true;

    }
}