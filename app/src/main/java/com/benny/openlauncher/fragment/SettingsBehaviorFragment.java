package com.benny.openlauncher.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.model.App;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.LauncherAction;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DialogHelper;

import net.gsantner.opoc.util.ContextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SettingsBehaviorFragment extends SettingsBaseFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        addPreferencesFromResource(R.xml.preferences_behavior);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        HomeActivity homeActivity = HomeActivity._launcher;
        int key = new ContextUtils(homeActivity).getResId(ContextUtils.ResType.STRING, preference.getKey());
        switch (key) {
            case R.string.pref_key__gesture_double_tap:
            case R.string.pref_key__gesture_swipe_up:
            case R.string.pref_key__gesture_swipe_down:
            case R.string.pref_key__gesture_pinch_in:
            case R.string.pref_key__gesture_pinch_out:
                DialogHelper.selectGestureDialog(getActivity(), preference.getTitle().toString(), new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        if (position == 1) {
                            DialogHelper.selectActionDialog(getActivity(), new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                    AppSettings.get().setString(key, LauncherAction.getActionItem(position)._action.toString());
                                }
                            });
                        } else if (position == 2) {
                            DialogHelper.selectAppDialog(getActivity(), new DialogHelper.OnAppSelectedListener() {
                                @Override
                                public void onAppSelected(App app) {
                                    AppSettings.get().setString(key, Tool.getIntentAsString(Tool.getIntentFromApp(app)));
                                }
                            });
                        } else {
                            AppSettings.get().setString(key, "");
                        }
                    }
                });
                break;
        }
        return false;
    }

    @Override
    public void updateSummaries() {
        List<Integer> gestures = new ArrayList<>(Arrays.asList(
                R.string.pref_key__gesture_double_tap,
                R.string.pref_key__gesture_swipe_up,
                R.string.pref_key__gesture_swipe_down,
                R.string.pref_key__gesture_pinch_in,
                R.string.pref_key__gesture_pinch_out
        ));

        for (int resId : gestures) {
            Preference preference = findPreference(getString(resId));
            Object gesture = AppSettings.get().getGesture(resId);
            if (gesture instanceof Intent) {
                preference.setSummary(String.format(Locale.ENGLISH, "%s: %s", getString(R.string.app), AppManager.getInstance(getContext()).findApp((Intent) gesture)._label));
            } else if (gesture instanceof LauncherAction.ActionDisplayItem) {
                preference.setSummary(String.format(Locale.ENGLISH, "%s: %s", getString(R.string.action), ((LauncherAction.ActionDisplayItem) gesture)._label));
            } else {
                preference.setSummary(String.format(Locale.ENGLISH, "%s", getString(R.string.none)));
            }
        }
    }
}