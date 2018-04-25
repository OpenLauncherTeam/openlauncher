package com.benny.openlauncher.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import com.benny.openlauncher.R;
import com.benny.openlauncher.fragment.MoreInfoFragment;

public class MoreInfoActivity extends ThemeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        MoreInfoFragment moreInfoFragment;
        if (savedInstanceState == null) {
            FragmentTransaction t = getSupportFragmentManager().beginTransaction();
            moreInfoFragment = MoreInfoFragment.newInstance();
            t.replace(R.id.more__fragment__placeholder_fragment, moreInfoFragment, MoreInfoFragment.TAG).commit();
        } else {
            moreInfoFragment = (MoreInfoFragment) getSupportFragmentManager().findFragmentByTag(MoreInfoFragment.TAG);
        }
    }

}
