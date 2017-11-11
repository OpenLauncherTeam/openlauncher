package com.benny.openlauncher.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle

import com.benny.openlauncher.R
import com.chyrta.onboarder.OnboarderActivity
import com.chyrta.onboarder.OnboarderPage

import java.util.ArrayList

class InitActivity : OnboarderActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!getSharedPreferences("quickSettings", Context.MODE_PRIVATE).getBoolean("firstStart", true)) {
            skipStart()
            return
        }

        val onBoarderPages = ArrayList<OnboarderPage>()

        // these are the pages in the start activity
        val onBoarderPage1 = OnboarderPage(getString(R.string.intro1_title), getString(R.string.intro1_text), R.drawable.intro_1)
        val onBoarderPage2 = OnboarderPage(getString(R.string.intro2_title), getString(R.string.intro2_text), R.drawable.intro_2)
        val onBoarderPage3 = OnboarderPage(getString(R.string.intro3_title), getString(R.string.intro3_text), R.drawable.intro_3)

        // title and description colors for the pages
        onBoarderPage1.setTitleColor(R.color.colorAccent)
        onBoarderPage1.setBackgroundColor(R.color.colorPrimaryDark)
        onBoarderPage2.setTitleColor(R.color.colorAccent)
        onBoarderPage2.setBackgroundColor(R.color.colorPrimaryDark)
        onBoarderPage3.setTitleColor(R.color.colorAccent)
        onBoarderPage3.setBackgroundColor(R.color.colorPrimaryDark)

        // add pages to the list
        onBoarderPages.add(onBoarderPage1)
        onBoarderPages.add(onBoarderPage2)
        onBoarderPages.add(onBoarderPage3)

        // pass pages to setOnboardPagesReady method
        setActiveIndicatorColor(android.R.color.white)
        setInactiveIndicatorColor(android.R.color.darker_gray)
        shouldDarkenButtonsLayout(true)
        setSkipButtonTitle(getString(R.string.intro_skip))
        setFinishButtonTitle(getString(R.string.intro_finish))
        setOnboardPagesReady(onBoarderPages)
    }

    public override fun onSkipButtonPressed() {
        // skips onboarder to the last page
        // super.onSkipButtonPressed();
        skipStart()
    }

    private fun skipStart() {
        getSharedPreferences("quickSettings", Context.MODE_PRIVATE).edit().putBoolean("firstStart", false).apply()

        val intent = Intent(this@InitActivity, Home::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)

        finish()
    }

    override fun onFinishButtonPressed() {
        skipStart()
    }
}
