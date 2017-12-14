package com.benny.openlauncher.activity

import android.content.Context
import android.content.res.Resources
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.benny.openlauncher.BuildConfig
import com.benny.openlauncher.R
import com.danielstone.materialaboutlibrary.ConvenienceBuilder
import com.danielstone.materialaboutlibrary.MaterialAboutActivity
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutItemOnClickAction
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20
import de.psdev.licensesdialog.licenses.MITLicense
import de.psdev.licensesdialog.model.Notice
import de.psdev.licensesdialog.model.Notices

class AboutActivity : MaterialAboutActivity() {

    companion object {
        private val notices = Notices()

        init {
            notices.addNotice(Notice("FastAdapter", "https://github.com/mikepenz/FastAdapter", "Mike Penz", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("CircularReveal", "https://github.com/ozodrukh/CircularReveal", "Abdullaev Ozodrukh", MITLicense()))
            notices.addNotice(Notice("MaterialScrollBar", "https://github.com/turing-tech/MaterialScrollBar", "Turing Technologies", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("Material About Library", "https://github.com/daniel-stoneuk/material-about-library", "Daniel Stone", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("Material Dialogs", "https://github.com/afollestad/material-dialogs", "Aidan Follestad", MITLicense()))
            notices.addNotice(Notice("Material Ripple Layout", "https://github.com/balysv/material-ripple", "Balys Valentukevicius", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("ImageBlurring ", "https://github.com/qiujuer/ImageBlurring", "Qiujuer", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("SimpleFingerGestures", "https://github.com/championswimmer/SimpleFingerGestures_Android_Library", "Arnav Gupta", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("TextDrawable", "https://github.com/amulyakhare/TextDrawable", "Amulya Khare", MITLicense()))
            notices.addNotice(Notice("AndroidOnboarder", "https://github.com/chyrta/AndroidOnboarder", "Dzmitry Chyrta, Daniel Morales", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("CustomActivityOnCrash", "https://github.com/Ereza/CustomActivityOnCrash", "Eduard Ereza Martínez", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("Butter Knife", "https://github.com/JakeWharton/butterknife", "Jake Wharton", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("jaredrummler colorpicker", "https://github.com/jaredrummler/ColorPicker", " jaredrummler", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("Android Support Library", "https://developer.android.com/topic/libraries/support-library/revisions.html", "The Android Open Source Project", ApacheSoftwareLicense20()))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_MaterialAboutActivity)
        super.onCreate(savedInstanceState)
    }

    override fun getMaterialAboutList(context: Context): MaterialAboutList {
        runOnUiThread {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        val titleCard = MaterialAboutCard.Builder()
        titleCard.addItem(MaterialAboutTitleItem(R.string.app_name, R.string.truly_launcher, R.drawable.ic_launcher))
        titleCard.addItem(MaterialAboutActionItem.Builder()
                .icon(R.drawable.ic_info_outline_dark_24dp)
                .text(getString(R.string.version) + " " + BuildConfig.VERSION_NAME)
                .setOnClickAction {
                    getSharedPreferences("quickSettings", Context.MODE_PRIVATE).edit().putBoolean("firstStart", true).apply()
                    startActivity(Intent(this, InitActivity::class.java))
                }
                .build())
        titleCard.addItem(ConvenienceBuilder.createWebsiteActionItem(this, resources.getDrawable(R.drawable.ic_github_dark_24dp), "GitHub", false, Uri.parse("https://github.com/OpenLauncherTeam/openlauncher")))
        titleCard.addItem(MaterialAboutActionItem(getString(R.string.about_libs), null, resources.getDrawable(R.drawable.ic_android_dark_24dp), MaterialAboutItemOnClickAction {
            LicensesDialog.Builder(this@AboutActivity)
                    .setNotices(notices)
                    .setIncludeOwnLicense(true)
                    .build()
                    .show()
        }))
        titleCard.addItem(ConvenienceBuilder.createRateActionItem(this, resources.getDrawable(R.drawable.ic_thumb_up_dark_24dp), getString(R.string.about_rate), null))

        val opTeamCard = MaterialAboutCard.Builder()
        opTeamCard.title(getString(R.string.about_team))
        opTeamCard.addItem(MaterialAboutActionItem.Builder()
                .icon(R.drawable.person_bennykok)
                .text("BennyKok")
                .subText(getString(R.string.about_credit_text_bennykok))
                .setOnClickAction(ConvenienceBuilder.createWebsiteOnClickAction(this, Uri.parse("http://bennykok.weebly.com/contact.html")))
                .build())
        opTeamCard.addItem(MaterialAboutActionItem.Builder()
                .icon(R.drawable.person_dkanada)
                .text("dkanada")
                .subText(getString(R.string.about_credit_text_dkanada))
                .setOnClickAction(ConvenienceBuilder.createWebsiteOnClickAction(this, Uri.parse("https://github.com/dkanada")))
                .build())
        opTeamCard.addItem(MaterialAboutActionItem.Builder()
                .icon(R.drawable.person_gsantner)
                .text("Gregor Santner")
                .subText(getString(R.string.about_credit_text_gsantner))
                .setOnClickAction(ConvenienceBuilder.createWebsiteOnClickAction(this, Uri.parse("http://gsantner.net/")))
                .build())

        val contributorsCard = MaterialAboutCard.Builder()
        contributorsCard.title(getString(R.string.about_credit))
        contributorsCard.addItem(MaterialAboutActionItem.Builder()
                .icon(R.drawable.person_chris_debrodie)
                .text("Chris DeBrodie")
                .subText(R.string.about_credit_text_chris_debrodie)
                .setOnClickAction(ConvenienceBuilder.createWebsiteOnClickAction(this, Uri.parse("https://plus.google.com/111923938461696019967")))
                .build())
        contributorsCard.addItem(MaterialAboutActionItem.Builder()
                .icon(R.drawable.person_gaukler_faun)
                .text("Gaukler Faun")
                .subText(R.string.about_credit_text_gaukler_faun)
                .setOnClickAction(ConvenienceBuilder.createWebsiteOnClickAction(this, Uri.parse("https://github.com/scoute-dich")))
                .build())
        contributorsCard.addItem(MaterialAboutActionItem.Builder()
                .text("Nikola Perović")
                .subText("Serbian translation")
                .build())
        contributorsCard.addItem(MaterialAboutActionItem.Builder()
                .text(R.string.about_credit_text_all_contributors)
                .setOnClickAction(ConvenienceBuilder.createWebsiteOnClickAction(this, Uri.parse("https://github.com/OpenLauncherTeam/openlauncher/graphs/contributors")))
                .build())

        return MaterialAboutList.Builder()
                .addCard(titleCard.build())
                .addCard(opTeamCard.build())
                .addCard(contributorsCard.build())
                .build()
    }

    override fun getActivityTitle(): CharSequence = getString(R.string.pref_title__about)

}
