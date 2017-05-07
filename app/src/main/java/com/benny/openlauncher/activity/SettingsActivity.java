package com.benny.openlauncher.activity;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.benny.openlauncher.R;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.DialogUtils;
import com.benny.openlauncher.util.LauncherAction;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * SettingsActivity
 * Created by vanitas on 24.10.16.
 */

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    //Toolbar
    @BindView(R.id.settings__appbar)
    protected AppBarLayout appBarLayout;

    @BindView(R.id.settings__toolbar)
    protected Toolbar toolbar;

    private AppSettings appSettings;
    private boolean shouldLauncherRestart = false;

    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.settings);
        setSupportActionBar(toolbar);
        appSettings = AppSettings.get();

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24px));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SettingsActivity.this.onBackPressed();
            }
        });
        showFragment(SettingsFragmentMaster.TAG, false);
    }

    protected void showFragment(String tag, boolean addToBackStack) {
        PreferenceFragment fragment = (PreferenceFragment) getFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            switch (tag) {
                case SettingsFragmentDesktop.TAG:
                    fragment = new SettingsFragmentDesktop();
                    toolbar.setTitle(R.string.pref_title__desktop);
                    break;
                case SettingsFragmentAppDrawer.TAG:
                    fragment = new SettingsFragmentAppDrawer();
                    toolbar.setTitle(R.string.pref_title__app_drawer);
                    break;
                case SettingsFragmentDock.TAG:
                    fragment = new SettingsFragmentDock();
                    toolbar.setTitle(R.string.pref_title__dock);
                    break;
                case SettingsFragmentIcons.TAG:
                    fragment = new SettingsFragmentIcons();
                    toolbar.setTitle(R.string.pref_title__icons);
                    break;
                case SettingsFragmentMiscellaneous.TAG:
                    fragment = new SettingsFragmentMiscellaneous();
                    toolbar.setTitle(R.string.pref_title__miscellaneous);
                    break;
                case SettingsFragmentMaster.TAG:
                default:
                    fragment = new SettingsFragmentMaster();
                    toolbar.setTitle(R.string.settings);
                    break;
            }
        }
        FragmentTransaction t = getFragmentManager().beginTransaction();
        if (addToBackStack) {
            t.addToBackStack(tag);
        }
        t.replace(R.id.settings__fragment_container, fragment, tag).commit();
    }

    @Override
    protected void onResume() {
        appSettings.registerPreferenceChangedListener(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        appSettings.unregisterPreferenceChangedListener(this);
        appSettings.setAppRestartRequired(shouldLauncherRestart);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        shouldLauncherRestart = true;
    }


    public static class SettingsFragmentMaster extends PreferenceFragment {
        public static final String TAG = "com.benny.openlauncher.settings.SettingsFragmentMaster";

        public void onCreate(Bundle savedInstances) {
            super.onCreate(savedInstances);
            getPreferenceManager().setSharedPreferencesName("app");
            addPreferencesFromResource(R.xml.preferences__master);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
            if (isAdded() && preference.hasKey()) {
                AppSettings settings = AppSettings.get();
                String key = preference.getKey();
                if (settings.isKeyEqual(key, R.string.pref_key__cat_desktop)) {
                    ((SettingsActivity) getActivity()).showFragment(SettingsFragmentDesktop.TAG, true);
                    return true;
                } else if (settings.isKeyEqual(key, R.string.pref_key__cat_app_drawer)) {
                    ((SettingsActivity) getActivity()).showFragment(SettingsFragmentAppDrawer.TAG, true);
                    return true;
                } else if (settings.isKeyEqual(key, R.string.pref_key__cat_dock)) {
                    ((SettingsActivity) getActivity()).showFragment(SettingsFragmentDock.TAG, true);
                    return true;
                } else if (settings.isKeyEqual(key, R.string.pref_key__cat_icons)) {
                    ((SettingsActivity) getActivity()).showFragment(SettingsFragmentIcons.TAG, true);
                    return true;
                } else if (settings.isKeyEqual(key, R.string.pref_key__cat_miscellaneous)) {
                    ((SettingsActivity) getActivity()).showFragment(SettingsFragmentMiscellaneous.TAG, true);
                    return true;
                } else if (settings.isKeyEqual(key, R.string.pref_key__about)) {
                    startActivity(new Intent(getActivity(), AboutActivity.class));
                    return true;
                }
            }
            return super.onPreferenceTreeClick(screen, preference);
        }

        @Override
        public void onResume() {
            super.onResume();
            ((SettingsActivity) getActivity()).toolbar.setTitle(R.string.settings);
        }
    }


    public static class SettingsFragmentDesktop extends PreferenceFragment {
        public static final String TAG = "com.benny.openlauncher.settings.SettingsFragmentDesktop";

        public void onCreate(Bundle savedInstances) {
            super.onCreate(savedInstances);
            getPreferenceManager().setSharedPreferencesName("app");
            addPreferencesFromResource(R.xml.preferences__desktop);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
            if (isAdded() && preference.hasKey()) {
                AppSettings settings = AppSettings.get();
                String key = preference.getKey();

                if (key.equals(getString(R.string.pref_key__minibar__arrangement__show_screen))) {
                    LauncherAction.RunAction(LauncherAction.Action.EditMinBar, getActivity().getApplicationContext());
                    return true;
                }
            }
            return super.onPreferenceTreeClick(screen, preference);
        }
    }

    public static class SettingsFragmentAppDrawer extends PreferenceFragment {
        public static final String TAG = "com.benny.openlauncher.settings.SettingsFragmentAppDrawer";

        public void onCreate(Bundle savedInstances) {
            super.onCreate(savedInstances);
            getPreferenceManager().setSharedPreferencesName("app");
            addPreferencesFromResource(R.xml.preferences__app_drawer);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
            if (isAdded() && preference.hasKey()) {
                AppSettings settings = AppSettings.get();
                String key = preference.getKey();

                if (key.equals(getString(R.string.pref_key__hidden_apps_list))) {
                    Intent intent = new Intent(getActivity(), HideAppsSelectionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    ((SettingsActivity) getActivity()).shouldLauncherRestart = true;
                    return true;
                }

            }
            return super.onPreferenceTreeClick(screen, preference);
        }
    }

    public static class SettingsFragmentDock extends PreferenceFragment {
        public static final String TAG = "com.benny.openlauncher.settings.SettingsFragmentDock";

        public void onCreate(Bundle savedInstances) {
            super.onCreate(savedInstances);
            getPreferenceManager().setSharedPreferencesName("app");
            addPreferencesFromResource(R.xml.preferences__dock);
        }
    }

    public static class SettingsFragmentIcons extends PreferenceFragment {
        public static final String TAG = "com.benny.openlauncher.settings.SettingsFragmentIcons";

        public void onCreate(Bundle savedInstances) {
            super.onCreate(savedInstances);
            getPreferenceManager().setSharedPreferencesName("app");
            addPreferencesFromResource(R.xml.preferences__icons);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
            if (isAdded() && preference.hasKey()) {
                AppSettings settings = AppSettings.get();
                String key = preference.getKey();

                if (key.equals(getString(R.string.pref_key__icon_pack_name))) {
                    AppManager.getInstance(getActivity()).startPickIconPackIntent(getActivity());
                    ((SettingsActivity) getActivity()).shouldLauncherRestart = true;
                    return true;
                }
            }
            return super.onPreferenceTreeClick(screen, preference);
        }
    }

    public static class SettingsFragmentMiscellaneous extends PreferenceFragment {
        public static final String TAG = "com.benny.openlauncher.settings.SettingsFragmentMiscellaneous";

        public void onCreate(Bundle savedInstances) {
            super.onCreate(savedInstances);
            getPreferenceManager().setSharedPreferencesName("app");
            addPreferencesFromResource(R.xml.preferences__miscellaneous);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
            if (isAdded() && preference.hasKey()) {
                AppSettings settings = AppSettings.get();
                String key = preference.getKey();
                Activity activity = getActivity();

                if (key.equals(getString(R.string.pref_key__restart_launcher))) {
                    if (Home.launcher != null)
                        Home.launcher.recreate();
                    ((SettingsActivity) activity).shouldLauncherRestart = true;
                    getActivity().finish();
                    return true;
                }

                if (key.equals(getString(R.string.pref_key__backup_app_settings))) {
                    if (ActivityCompat.checkSelfPermission(activity,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        DialogUtils.backupDialog(activity);
                    } else {
                        ActivityCompat.requestPermissions(Home.launcher, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                Home.REQUEST_PERMISSION_STORAGE);
                    }
                }

            }
            return super.onPreferenceTreeClick(screen, preference);
        }
    }
}
