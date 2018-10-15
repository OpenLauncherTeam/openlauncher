package com.benny.openlauncher.fragment;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.model.App;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DialogHelper;

import net.gsantner.opoc.util.ContextUtils;

public class SettingsGesturesFragment extends SettingsBaseFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_gestures);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        HomeActivity homeActivity = HomeActivity._launcher;
        int key = new ContextUtils(homeActivity).getResId(ContextUtils.ResType.STRING, preference.getKey());
        switch (key) {
            case R.string.pref_key__gesture_double_tap:
            case R.string.pref_key__gesture_swipe_up:
            case R.string.pref_key__gesture_swipe_down:
            case R.string.pref_key__gesture_pinch:
            case R.string.pref_key__gesture_unpinch:
                DialogHelper.selectGestureDialog(homeActivity, preference.getTitle().toString(), new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        if (position == 1) {
                            DialogHelper.selectActionDialog(homeActivity, new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                    AppSettings.get().setString(key, "1" + position);
                                }
                            });
                        } else if (position == 2) {
                            DialogHelper.selectAppDialog(homeActivity, new DialogHelper.OnAppSelectedListener() {
                                @Override
                                public void onAppSelected(App app) {
                                    AppSettings.get().setString(key, "2" + Tool.getIntentAsString(Tool.getIntentFromApp(app)));
                                }
                            });
                        } else {
                            AppSettings.get().setString(key, "0");
                        }
                    }
                });
                break;
        }
        return false;
    }
}