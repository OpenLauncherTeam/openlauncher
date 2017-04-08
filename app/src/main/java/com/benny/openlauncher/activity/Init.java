package com.benny.openlauncher.activity;

import android.content.Intent;
import android.os.Bundle;

import com.benny.openlauncher.R;
import com.chyrta.onboarder.OnboarderActivity;
import com.chyrta.onboarder.OnboarderPage;

import java.util.ArrayList;
import java.util.List;


public class Init extends OnboarderActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getSharedPreferences("quickSettings", MODE_PRIVATE).getBoolean("firstStart", true)) {
            RockIt();
            return;
        }

        List<OnboarderPage> onBoarderPages = new ArrayList<>();

        // Create your first page
        OnboarderPage onBoarderPage1 = new OnboarderPage(getString(R.string.intro1_title), getString(R.string.intro1_text), R.drawable.ic_launcher_intro);
        OnboarderPage onBoarderPage2 = new OnboarderPage(getString(R.string.intro2_title), getString(R.string.intro2_text), R.drawable.screenshot_1);
        OnboarderPage onBoarderPage3 = new OnboarderPage(getString(R.string.intro3_title), getString(R.string.intro3_text), R.drawable.screenshot_2);

        // You can define title and description colors (by default white)
        onBoarderPage1.setTitleColor(R.color.colorAccent);
        onBoarderPage1.setBackgroundColor(R.color.colorPrimaryDark);
        onBoarderPage2.setTitleColor(R.color.colorAccent);
        onBoarderPage2.setBackgroundColor(R.color.colorPrimaryDark);
        onBoarderPage3.setTitleColor(R.color.colorAccent);
        onBoarderPage3.setBackgroundColor(R.color.colorPrimaryDark);

        // Add your pages to the list
        onBoarderPages.add(onBoarderPage1);
        onBoarderPages.add(onBoarderPage2);
        onBoarderPages.add(onBoarderPage3);

        // And pass your pages to 'setOnboardPagesReady' method
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
        // defines the skip button behavior
        RockIt();
    }

    private void RockIt() {
        getSharedPreferences("quickSettings", MODE_PRIVATE).edit().putBoolean("firstStart", false).apply();

        Intent intent = new Intent(Init.this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);

        finish();
    }

    @Override
    public void onFinishButtonPressed() {
        // Define your actions when the user press 'Finish' button
        RockIt();
    }
}
