package com.benny.openlauncher.fragment;

import android.os.Bundle;
import android.support.v7.preference.Preference;
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
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_gestures);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        HomeActivity homeActivity = HomeActivity._launcher;
        int key = new ContextUtils(homeActivity).getResId(ContextUtils.ResType.STRING, preference.getKey());
        switch (key) {
            case R.string.pref_key__gesture_double_tap:
            case R.string.pref_key__gesture_swipe_up:
            case R.string.pref_key__gesture_swipe_down:
            case R.string.pref_key__gesture_pinch:
            case R.string.pref_key__gesture_unpinch:
                DialogHelper.selectGestureDialog(getActivity(), preference.getTitle().toString(), new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        if (position == 1) {
                            DialogHelper.selectActionDialog(getActivity(), new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                    AppSettings.get().setString(key, "1" + position);
                                }
                            });
                        } else if (position == 2) {
                            DialogHelper.selectAppDialog(getActivity(), new DialogHelper.OnAppSelectedListener() {
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