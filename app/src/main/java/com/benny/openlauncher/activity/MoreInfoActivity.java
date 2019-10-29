package com.benny.openlauncher.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import com.benny.openlauncher.R;
import com.benny.openlauncher.fragment.SettingsAboutFragment;

public class MoreInfoActivity extends ColorActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(_appSettings.getPrimaryColor());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        SettingsAboutFragment settingsAboutFragment = SettingsAboutFragment.newInstance();
        transaction.replace(R.id.fragment_holder, settingsAboutFragment, SettingsAboutFragment.TAG).commit();
    }
}
