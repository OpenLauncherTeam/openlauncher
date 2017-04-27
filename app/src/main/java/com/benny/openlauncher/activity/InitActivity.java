package com.benny.openlauncher.activity;

import android.content.Intent;
import android.os.Bundle;

import com.benny.openlauncher.R;
import com.chyrta.onboarder.OnboarderActivity;
import com.chyrta.onboarder.OnboarderPage;

import java.util.ArrayList;
import java.util.List;


public class InitActivity extends OnboarderActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getSharedPreferences("quickSettings", MODE_PRIVATE).getBoolean("firstStart", true)) {
            skipStart();
            return;
        }

        List<OnboarderPage> onBoarderPages = new ArrayList<>();

        // these are the pages in the start activity
        OnboarderPage onBoarderPage1 = new OnboarderPage(getString(R.string.intro1_title), getString(R.string.intro1_text), R.drawable.ic_launcher_intro);
        OnboarderPage onBoarderPage2 = new OnboarderPage(getString(R.string.intro2_title), getString(R.string.intro2_text), R.drawable.screenshot_1);
        OnboarderPage onBoarderPage3 = new OnboarderPage(getString(R.string.intro3_title), getString(R.string.intro3_text), R.drawable.screenshot_2);

        // title and description colors for the pages
        onBoarderPage1.setTitleColor(R.color.colorAccent);
        onBoarderPage1.setBackgroundColor(R.color.colorPrimaryDark);
        onBoarderPage2.setTitleColor(R.color.colorAccent);
        onBoarderPage2.setBackgroundColor(R.color.colorPrimaryDark);
        onBoarderPage3.setTitleColor(R.color.colorAccent);
        onBoarderPage3.setBackgroundColor(R.color.colorPrimaryDark);

        // add pages to the list
        onBoarderPages.add(onBoarderPage1);
        onBoarderPages.add(onBoarderPage2);
        onBoarderPages.add(onBoarderPage3);

        // pass pages to setOnboardPagesReady method
        setActiveIndicatorColor(android.R.color.white);
        setInactiveIndicatorColor(android.R.color.darker_gray);
        shouldDarkenButtonsLayout(true);
        setSkipButtonTitle(getString(R.string.intro_skip));
        setFinishButtonTitle(getString(R.string.intro_finish));
        setOnboardPagesReady(onBoarderPages);
    }

    @Override
    public void onSkipButtonPressed() {
        // skips onboarder to the last page
        // super.onSkipButtonPressed();
        skipStart();
    }

    private void skipStart() {
        getSharedPreferences("quickSettings", MODE_PRIVATE).edit().putBoolean("firstStart", false).apply();

        Intent intent = new Intent(InitActivity.this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);

        finish();
    }

    @Override
    public void onFinishButtonPressed() {
        skipStart();
    }
}
