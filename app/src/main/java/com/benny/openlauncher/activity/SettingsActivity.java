package com.benny.openlauncher.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.benny.openlauncher.R;
import com.benny.openlauncher.core.util.DatabaseHelper;
import com.benny.openlauncher.core.widget.AppDrawerController;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.LauncherAction;
import com.benny.openlauncher.viewutil.DialogHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends ThemeActivity {
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;
    protected static Context context;

    private AppSettings appSettings;
    private boolean shouldLauncherRestart = false;

    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        context = this;
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
                case SettingsFragmentGestures.TAG:
                    fragment = new SettingsFragmentGestures();
                    toolbar.setTitle(R.string.pref_title__gestures);
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
        t.replace(R.id.settings_fragment_container, fragment, tag).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        appSettings.setAppRestartRequired(shouldLauncherRestart);
        super.onPause();
    }

    public static class SettingsFragmentMaster extends PreferenceFragment {
        public static final String TAG = "com.benny.openlauncher.settings.SettingsFragmentMaster";

        public void onCreate(Bundle savedInstances) {
            super.onCreate(savedInstances);
            getPreferenceManager().setSharedPreferencesName("app");
            addPreferencesFromResource(R.xml.preferences_master);
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
                } else if (settings.isKeyEqual(key, R.string.pref_key__cat_gestures)) {
                    ((SettingsActivity) getActivity()).showFragment(SettingsFragmentGestures.TAG, true);
                    return true;
                } else if (settings.isKeyEqual(key, R.string.pref_key__cat_icons)) {
                    ((SettingsActivity) getActivity()).showFragment(SettingsFragmentIcons.TAG, true);
                    return true;
                } else if (settings.isKeyEqual(key, R.string.pref_key__cat_miscellaneous)) {
                    ((SettingsActivity) getActivity()).showFragment(SettingsFragmentMiscellaneous.TAG, true);
                    return true;
                } else if (settings.isKeyEqual(key, R.string.pref_key__cat_about)) {
                    startActivity(new Intent(getActivity(), AboutActivity.class));
                    return true;
                }
            }
            return super.onPreferenceTreeClick(screen, preference);
        }

        @Override
        @SuppressLint("DefaultLocale")
        public void onResume() {
            super.onResume();
            ((SettingsActivity) getActivity()).toolbar.setTitle(R.string.settings);

            AppSettings settings = AppSettings.get();

            String desktopSummary = String.format("%s: %d x %d", getString(R.string.pref_title__size), settings.getDesktopColumnCount(), (settings.getDesktopRowCount()));
            findPreference(getString(R.string.pref_key__cat_desktop)).setSummary(desktopSummary);

            String dockSummary = String.format("%s: %d", getString(R.string.pref_title__size), settings.getDockSize());
            findPreference(getString(R.string.pref_key__cat_dock)).setSummary(dockSummary);

            String drawerSummary = String.format("%s: ", getString(R.string.pref_title__style));
            switch (settings.getDrawerStyle()) {
                case AppDrawerController.DrawerMode.HORIZONTAL_PAGED:
                    drawerSummary += getString(R.string.horizontal_paged_drawer);
                    break;
                case AppDrawerController.DrawerMode.VERTICAL:
                    drawerSummary += getString(R.string.vertical_scroll_drawer);
                    break;
            }
            findPreference(getString(R.string.pref_key__cat_app_drawer)).setSummary(drawerSummary);

            String iconsSummary = String.format("%s: %ddp", getString(R.string.pref_title__size), settings.getIconSize());
            findPreference(getString(R.string.pref_key__cat_icons)).setSummary(iconsSummary);
        }
    }


    public static class SettingsFragmentDesktop extends BasePreferenceFragment {
        public static final String TAG = "com.benny.openlauncher.settings.SettingsFragmentDesktop";

        private static final int[] requireRestartPreferenceIds = new int[]{
                R.string.pref_key__desktop_columns,
                R.string.pref_key__desktop_rows,
                R.string.pref_key__desktop_style,
                R.string.pref_key__desktop_fullscreen,
                R.string.pref_key__desktop_show_label,

                R.string.pref_key__search_bar_enable,
                R.string.pref_key__search_bar_show_hidden_apps,

                R.string.pref_key__desktop_background_color,
                R.string.pref_key__minibar_background_color,
        };

        public void onCreate(Bundle savedInstances) {
            super.onCreate(savedInstances);
            getPreferenceManager().setSharedPreferencesName("app");
            addPreferencesFromResource(R.xml.preferences_desktop);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
            if (isAdded() && preference.hasKey()) {
                String key = preference.getKey();

                if (key.equals(getString(R.string.pref_key__minibar))) {
                    LauncherAction.RunAction(LauncherAction.Action.EditMinBar, getActivity());
                    return true;
                }
            }
            return super.onPreferenceTreeClick(screen, preference);
        }

        @Override
        public void onPause() {
            appSettings.unregisterPreferenceChangedListener(this);
            super.onPause();
        }

        @Override
        public void onResume() {
            appSettings.registerPreferenceChangedListener(this);
            super.onResume();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            checkIfPreferenceChangedRequireRestart(requireRestartPreferenceIds, key);
            if (key.equals(getString(R.string.pref_key__desktop_indicator_style))) {
                Home.launcher.desktopIndicator.setMode(appSettings.getDesktopIndicatorMode());
            }
            if (key.equals(getString(R.string.pref_title__desktop_show_position_indicator))) {
                Home.launcher.updateDesktopIndicatorVisibility();
            }
        }
    }

    public static class SettingsFragmentDock extends BasePreferenceFragment {
        public static final String TAG = "com.benny.openlauncher.settings.SettingsFragmentDock";

        private static final int[] requireRestartPreferenceIds = new int[]{
                R.string.pref_key__dock_enable,
                R.string.pref_key__dock_size,
                R.string.pref_key__dock_show_label,
                R.string.pref_key__dock_background_color
        };

        public void onCreate(Bundle savedInstances) {
            super.onCreate(savedInstances);
            getPreferenceManager().setSharedPreferencesName("app");
            addPreferencesFromResource(R.xml.preferences_dock);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
            if (isAdded() && preference.hasKey()) {
                AppSettings settings = AppSettings.get();
                String key = preference.getKey();
            }
            return super.onPreferenceTreeClick(screen, preference);
        }

        @Override
        public void onPause() {
            appSettings.unregisterPreferenceChangedListener(this);
            super.onPause();
        }

        @Override
        public void onResume() {
            appSettings.registerPreferenceChangedListener(this);
            super.onResume();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            checkIfPreferenceChangedRequireRestart(requireRestartPreferenceIds, key);
            if (key.equals(getString(R.string.pref_key__dock_enable))) {
                Home.launcher.updateDock(true);
            }
        }
    }

    public static class SettingsFragmentAppDrawer extends BasePreferenceFragment {
        public static final String TAG = "com.benny.openlauncher.settings.SettingsFragmentAppDrawer";

        private static final int[] requireRestartPreferenceIds = new int[]{
                R.string.pref_key__drawer_columns,
                R.string.pref_key__drawer_rows,
                R.string.pref_key__drawer_style,
                R.string.pref_key__drawer_show_card_view,
                R.string.pref_key__drawer_show_position_indicator,
                R.string.pref_key__drawer_show_label,
                R.string.pref_key__drawer_background_color,
                R.string.pref_key__drawer_card_color,
                R.string.pref_key__drawer_label_color,
                R.string.pref_key__drawer_fast_scroll_color
        };

        public void onCreate(Bundle savedInstances) {
            super.onCreate(savedInstances);
            getPreferenceManager().setSharedPreferencesName("app");
            addPreferencesFromResource(R.xml.preferences_app_drawer);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
            if (isAdded() && preference.hasKey()) {
                String key = preference.getKey();

                if (key.equals(getString(R.string.pref_key__hidden_apps))) {
                    Intent intent = new Intent(getActivity(), HideAppsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    ((SettingsActivity) getActivity()).shouldLauncherRestart = true;
                    return true;
                }

            }
            return super.onPreferenceTreeClick(screen, preference);
        }

        @Override
        public void onPause() {
            appSettings.unregisterPreferenceChangedListener(this);
            super.onPause();
        }

        @Override
        public void onResume() {
            appSettings.registerPreferenceChangedListener(this);
            super.onResume();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            checkIfPreferenceChangedRequireRestart(requireRestartPreferenceIds, key);
        }
    }

    public static class SettingsFragmentGestures extends BasePreferenceFragment {
        public static final String TAG = "com.benny.openlauncher.settings.SettingsFragmentGestures";

        private static final int[] requireRestartPreferenceIds = new int[]{

        };

        public void onCreate(Bundle savedInstances) {
            super.onCreate(savedInstances);
            getPreferenceManager().setSharedPreferencesName("app");
            addPreferencesFromResource(R.xml.preferences_gestures);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
            if (isAdded() && preference.hasKey()) {
                String key = preference.getKey();
            }
            return super.onPreferenceTreeClick(screen, preference);
        }

        @Override
        public void onPause() {
            appSettings.unregisterPreferenceChangedListener(this);
            super.onPause();
        }

        @Override
        public void onResume() {
            appSettings.registerPreferenceChangedListener(this);
            super.onResume();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            checkIfPreferenceChangedRequireRestart(requireRestartPreferenceIds, key);
        }
    }

    public static class SettingsFragmentIcons extends BasePreferenceFragment {
        public static final String TAG = "com.benny.openlauncher.settings.SettingsFragmentIcons";

        private static final int[] requireRestartPreferenceIds = new int[]{
                R.string.pref_key__icon_size,
                R.string.pref_key__icon_pack
        };

        public void onCreate(Bundle savedInstances) {
            super.onCreate(savedInstances);
            getPreferenceManager().setSharedPreferencesName("app");
            addPreferencesFromResource(R.xml.preferences_icons);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
            if (isAdded() && preference.hasKey()) {
                String key = preference.getKey();

                if (key.equals(getString(R.string.pref_key__icon_pack))) {
                    AppManager.getInstance(getActivity()).startPickIconPackIntent(getActivity());
                    return true;
                }
            }
            return super.onPreferenceTreeClick(screen, preference);
        }

        @Override
        public void onPause() {
            appSettings.unregisterPreferenceChangedListener(this);
            super.onPause();
        }

        @Override
        public void onResume() {
            appSettings.registerPreferenceChangedListener(this);
            super.onResume();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            checkIfPreferenceChangedRequireRestart(requireRestartPreferenceIds, key);
        }
    }

    public static class SettingsFragmentMiscellaneous extends BasePreferenceFragment {
        public static final String TAG = "com.benny.openlauncher.settings.SettingsFragmentMiscellaneous";

        private static final int[] requireRestartPreferenceIds = new int[]{
                R.string.pref_summary__backup,
                R.string.pref_title__clear_database,
                R.string.pref_summary__theme
        };

        public void onCreate(Bundle savedInstances) {
            super.onCreate(savedInstances);
            getPreferenceManager().setSharedPreferencesName("app");
            addPreferencesFromResource(R.xml.preferences_miscellaneous);

            if (getResources().getBoolean(R.bool.isTablet)) {
                PreferenceCategory c = (PreferenceCategory) findPreference(getString(R.string.pref_key__cat_miscellaneous));
                Preference p = c.findPreference(getString(R.string.pref_key__desktop_rotate));
                c.removePreference(p);
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
            if (isAdded() && preference.hasKey()) {
                String key = preference.getKey();
                Activity activity = getActivity();

                if (key.equals(getString(R.string.pref_key__backup))) {
                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        DialogHelper.backupDialog(activity);
                    } else {
                        ActivityCompat.requestPermissions(Home.launcher, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Home.REQUEST_PERMISSION_STORAGE);
                    }
                }

                if (key.equals(getString(R.string.pref_key__clear_database))) {
                    if (Home.launcher != null)
                        Home.launcher.recreate();
                    ((DatabaseHelper) Home.launcher.db).onUpgrade(((DatabaseHelper) Home.launcher.db).getWritableDatabase(), 1, 1);
                    getActivity().finish();
                    return true;
                }

                if (key.equals(getString(R.string.pref_key__restart))) {
                    if (Home.launcher != null)
                        Home.launcher.recreate();
                    getActivity().finish();
                    return true;
                }
            }
            return super.onPreferenceTreeClick(screen, preference);
        }

        @Override
        public void onPause() {
            appSettings.unregisterPreferenceChangedListener(this);
            super.onPause();
        }

        @Override
        public void onResume() {
            appSettings.registerPreferenceChangedListener(this);
            super.onResume();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            checkIfPreferenceChangedRequireRestart(requireRestartPreferenceIds, key);
        }
    }

    public static abstract class BasePreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        public AppSettings appSettings;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            appSettings = AppSettings.get();
        }

        public void checkIfPreferenceChangedRequireRestart(int[] ids, String key) {
            SettingsActivity settingsActivity = (SettingsActivity) getActivity();

            if (settingsActivity.shouldLauncherRestart) return;
            for (int id : ids) {
                if (getString(id).equals(key)) {
                    settingsActivity.shouldLauncherRestart = true;
                    return;
                }
            }
        }

    }
}
