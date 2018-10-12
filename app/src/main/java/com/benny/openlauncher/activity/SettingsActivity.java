/*
 * Copyright (c) 2017-2018 Gregor Santner
 *
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package com.benny.openlauncher.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.R;
import com.benny.openlauncher.preference.ColorPreferenceCompat;
import com.benny.openlauncher.model.App;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.BackupHelper;
import com.benny.openlauncher.util.DatabaseHelper;
import com.benny.openlauncher.util.Definitions;
import com.benny.openlauncher.util.LauncherAction;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DialogHelper;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import net.gsantner.opoc.preference.GsPreferenceFragmentCompat;
import net.gsantner.opoc.util.ContextUtils;
import net.gsantner.opoc.util.PermissionChecker;

import java.io.File;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.benny.openlauncher.widget.AppDrawerController.DrawerMode.HORIZONTAL_PAGED;
import static com.benny.openlauncher.widget.AppDrawerController.DrawerMode.VERTICAL;

public class SettingsActivity extends ThemeActivity {
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    public void onCreate(Bundle b) {
        // Must be applied before setContentView
        super.onCreate(b);
        ContextUtils contextUtils = new ContextUtils(this);
        contextUtils.setAppLanguage(_appSettings.getLanguage());

        // Load UI
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        // Custom code
        toolbar.setTitle(R.string.settings);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24px));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setBackgroundColor(_appSettings.getPrimaryColor());
        showFragment(SettingsFragmentMaster.TAG, false);
    }

    protected void showFragment(String tag, boolean addToBackStack) {
        String toolbarTitle = getString(R.string.settings);
        GsPreferenceFragmentCompat prefFrag = (GsPreferenceFragmentCompat) getSupportFragmentManager().findFragmentByTag(tag);
        if (prefFrag == null) {
            switch (tag) {
                case SettingsFragmentMaster.TAG:
                default: {
                    prefFrag = new SettingsFragmentMaster();
                    toolbarTitle = prefFrag.getTitleOrDefault(toolbarTitle);
                    break;
                }
            }
        }
        toolbar.setTitle(toolbarTitle);
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        if (addToBackStack) {
            t.addToBackStack(tag);
        }
        t.replace(R.id.settings__activity__fragment_placeholder, prefFrag, tag).commit();
    }

    @Override
    public void onBackPressed() {
        GsPreferenceFragmentCompat prefFrag = (GsPreferenceFragmentCompat) getSupportFragmentManager().findFragmentByTag(SettingsFragmentMaster.TAG);
        if (prefFrag != null && prefFrag.canGoBack()) {
            prefFrag.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            switch (requestCode) {
                case Definitions.INTENT_BACKUP:
                    BackupHelper.backupConfig(this, new File(Utils.getFileForUri(files.get(0)).getAbsolutePath() + "/openlauncher.zip").toString());
                    break;
                case Definitions.INTENT_RESTORE:
                    BackupHelper.restoreConfig(this, Utils.getFileForUri(files.get(0)).toString());
                    System.exit(0);
                    break;
            }
        }
    }

    public static abstract class OlSettingsFragment extends GsPreferenceFragmentCompat<AppSettings> {
        protected AppSettings _as;

        @Override
        protected AppSettings getAppSettings(Context context) {
            if (_as == null) {
                _as = AppSettings.get();
            }
            return _as;
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
    }

    public static class SettingsFragmentMaster extends OlSettingsFragment {
        public static final String TAG = "SettingsFragmentMaster";


        private static final int[] requireRestartPreferenceIds = new int[]{
                R.string.pref_key__desktop_columns, R.string.pref_key__desktop_rows, R.string.pref_key__desktop_style,
                R.string.pref_key__desktop_fullscreen, R.string.pref_key__desktop_show_label, R.string.pref_key__search_bar_enable,
                R.string.pref_key__search_bar_show_hidden_apps, R.string.pref_key__desktop_background_color,
                R.string.pref_key__minibar_background_color, R.string.pref_key__dock_enable, R.string.pref_key__dock_size,
                R.string.pref_key__dock_show_label, R.string.pref_key__dock_background_color, R.string.pref_key__drawer_columns,
                R.string.pref_key__drawer_rows, R.string.pref_key__drawer_style, R.string.pref_key__drawer_show_card_view,
                R.string.pref_key__drawer_show_position_indicator, R.string.pref_key__drawer_show_label,
                R.string.pref_key__drawer_background_color, R.string.pref_key__drawer_card_color, R.string.pref_key__drawer_label_color,
                R.string.pref_key__drawer_fast_scroll_color, R.string.pref_key__date_bar_date_format_custom_1,
                R.string.pref_key__date_bar_date_format_custom_2, R.string.pref_key__date_bar_date_format_type,
                R.string.pref_key__date_bar_date_text_color, R.string.pref_key__icon_size, R.string.pref_key__icon_pack,
                R.string.pref_key__clear_database, R.string.pref_key__backup, R.string.pref_key__restore, R.string.pref_key__theme
        };

        @Override
        public int getPreferenceResourceForInflation() {
            return R.xml.preferences_master;
        }

        @Override
        public String getFragmentTag() {
            return TAG;
        }

        private boolean requiresRestart(int key) {
            for (int k : requireRestartPreferenceIds) {
                if (k == key) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void doUpdatePreferences() {
            Preference pref;
            String tmp;

            // preferences in master screen
            if ((pref = findPreference(R.string.pref_key__cat_desktop)) != null) {
                tmp = String.format(Locale.ENGLISH, "%s: %d x %d", getString(R.string.pref_title__size), _as.getDesktopColumnCount(), _as.getDesktopRowCount());
                pref.setSummary(tmp);
            }
            if ((pref = findPreference(R.string.pref_key__cat_dock)) != null) {
                tmp = String.format(Locale.ENGLISH, "%s: %d", getString(R.string.pref_title__size), _as.getDockSize());
                pref.setSummary(tmp);
            }
            if ((pref = findPreference(R.string.pref_key__cat_app_drawer)) != null) {
                tmp = String.format("%s: ", getString(R.string.pref_title__style));
                switch (_as.getDrawerStyle()) {
                    case HORIZONTAL_PAGED:
                        tmp += getString(R.string.horizontal_paged_drawer);
                        break;
                    case VERTICAL:
                        tmp += getString(R.string.vertical_scroll_drawer);
                        break;
                }
                pref.setSummary(tmp);
            }
            if ((pref = findPreference(R.string.pref_key__cat_appearance)) != null) {
                tmp = String.format(Locale.ENGLISH, "Icons: %ddp", _as.getIconSize());
                pref.setSummary(tmp);
            }
        }

        @Override
        protected void onPreferenceChanged(SharedPreferences prefs, final String key) {
            super.onPreferenceChanged(prefs, key);
            int keyRes = _cu.getResId(ContextUtils.ResType.STRING, key);
            HomeActivity launcher = HomeActivity.Companion.getLauncher();
            switch (keyRes) {
                case R.string.pref_key__desktop_indicator_style: {
                    launcher.getDesktopIndicator().setMode(_as.getDesktopIndicatorMode());
                    break;
                }
                case R.string.pref_title__desktop_show_position_indicator: {
                    launcher.updateDesktopIndicator(true);
                    break;
                }
                case R.string.pref_key__dock_enable: {
                    launcher.updateDock(true, 0);
                    break;
                }
                case R.string.pref_key__gesture_double_tap:
                case R.string.pref_key__gesture_swipe_up:
                case R.string.pref_key__gesture_swipe_down:
                case R.string.pref_key__gesture_pinch:
                case R.string.pref_key__gesture_unpinch: {
                    if (prefs.getString(key, "0").equals("1")) {
                        DialogHelper.selectActionDialog(getContext(), new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                AppSettings.get().setString(key, "1" + position);
                            }
                        });
                    } else if (prefs.getString(key, "0").equals("2")) {
                        DialogHelper.selectAppDialog(getContext(), new DialogHelper.OnAppSelectedListener() {
                            @Override
                            public void onAppSelected(App app) {
                                AppSettings.get().setString(key, "2" + Tool.getIntentAsString(Tool.getIntentFromApp(app)));
                            }
                        });
                    }
                    break;
                }
            }
            if (requiresRestart(keyRes)) {
                _as.setAppRestartRequired(true);
            }
        }

        @Override
        @SuppressWarnings({"ConstantConditions", "ConstantIfStatement", "StatementWithEmptyBody"})
        public Boolean onPreferenceClicked(Preference preference) {
            int key = _cu.getResId(ContextUtils.ResType.STRING, preference.getKey());
            HomeActivity launcher = HomeActivity.Companion.getLauncher();

            switch (key) {
                case R.string.pref_key__minibar: {
                    LauncherAction.RunAction(LauncherAction.Action.EditMinibar, getActivity());
                    return true;
                }
                case R.string.pref_key__hidden_apps: {
                    Intent intent = new Intent(getActivity(), HideAppsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    return true;
                }
                case R.string.pref_key__icon_pack: {
                    AppManager.getInstance(getActivity()).startPickIconPackIntent(getActivity());
                    return true;
                }
                case R.string.pref_key__clear_database: {
                    DialogHelper.alertDialog(getContext(), getString(R.string.clear_user_data), getString(R.string.clear_user_data_are_you_sure), new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (launcher != null) {
                                launcher.recreate();
                            }
                            DatabaseHelper db = HomeActivity._db;
                            db.onUpgrade(db.getWritableDatabase(), 1, 1);
                            getActivity().finish();
                        }
                    });
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
                case R.string.pref_key__restart: {
                    launcher.recreate();
                    getActivity().finish();
                    return true;
                }
                case R.string.pref_key__about: {
                    startActivity(new Intent(getActivity(), MoreInfoActivity.class));
                    return true;
                }
            }

            if (preference instanceof ColorPreferenceCompat) {
                ColorPickerDialog dialog = ((ColorPreferenceCompat) preference).getDialog();
                dialog.setColorPickerDialogListener(new ColorPickerDialogListener() {
                    public void onColorSelected(int dialogId, int color) {
                        ((ColorPreferenceCompat) preference).saveValue(color);
                    }

                    public void onDialogDismissed(int dialogId) {
                    }
                });
                dialog.show(getActivity().getFragmentManager(), "color-picker-dialog");
            }

            return false;
        }
    }
}