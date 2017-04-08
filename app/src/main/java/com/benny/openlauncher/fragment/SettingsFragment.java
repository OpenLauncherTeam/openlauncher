package com.benny.openlauncher.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.benny.openlauncher.R;

@Deprecated
public class SettingsFragment extends Fragment {
    OnSettingsSelectedInterface mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout settings = (LinearLayout) inflater.inflate(R.layout.fragment_settings, container, false);
        LinearLayout desktopSettings = (LinearLayout) settings.findViewById(R.id.desktop_settings);
        LinearLayout dockSettings = (LinearLayout) settings.findViewById(R.id.dock_settings);
        LinearLayout drawerSettings = (LinearLayout) settings.findViewById(R.id.app_drawer_settings);
        LinearLayout inputSettings = (LinearLayout) settings.findViewById(R.id.input_settings);
        LinearLayout colorsSettings = (LinearLayout) settings.findViewById(R.id.colors_settings);
        LinearLayout iconsSettings = (LinearLayout) settings.findViewById(R.id.icons_settings);
        LinearLayout otherSettings = (LinearLayout) settings.findViewById(R.id.other_settings);

        desktopSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSettingsSelected(0);
            }
        });

        dockSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSettingsSelected(1);
            }
        });

        drawerSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSettingsSelected(2);
            }
        });

        inputSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSettingsSelected(3);
            }
        });

        colorsSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSettingsSelected(4);
            }
        });

        iconsSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSettingsSelected(5);
            }
        });

        otherSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSettingsSelected(6);
            }
        });

        return settings;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnSettingsSelectedInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement interface");
        }
    }

    public interface OnSettingsSelectedInterface {
        void onSettingsSelected(int settingsCategory);
    }
}