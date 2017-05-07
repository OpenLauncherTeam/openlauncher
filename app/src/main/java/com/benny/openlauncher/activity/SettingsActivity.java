package com.benny.openlauncher.activity;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.benny.openlauncher.R;
import com.benny.openlauncher.util.AppSettings;
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
        appSettings.registerPreferenceChangedListener(this);
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
    protected void onStop() {
        appSettings.unregisterPreferenceChangedListener(this);
        super.onStop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

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
                } /*else if (settings.isKeyEqual(key, R.string.pref_key__cat_dock)) {
                    ((SettingsActivity) getActivity()).showFragment(SettingsFragmentDock.TAG, true);
                    return true;
                } else if (settings.isKeyEqual(key, R.string.pref_key__cat_gestures)) {
                    ((SettingsActivity) getActivity()).showFragment(SettingsFragmentGestures.TAG, true);
                    return true;
                } else if (settings.isKeyEqual(key, R.string.pref_key__cat_icons)) {
                    ((SettingsActivity) getActivity()).showFragment(SettingsFragmentIcons.TAG, true);
                    return true;
                } else if (settings.isKeyEqual(key, R.string.pref_key__cat_various)) {
                    ((SettingsActivity) getActivity()).showFragment(SettingsFragmentVarious.TAG, true);
                    return true;
                }*/
            }
            return super.onPreferenceTreeClick(screen, preference);
        }

        @Override
        public void onResume() {
            super.onResume();
            ((SettingsActivity)getActivity()).toolbar.setTitle(R.string.settings);
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
            return super.onPreferenceTreeClick(screen, preference);
        }
    }
}
