package com.benny.openlauncher.activity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.MenuItem;

import com.benny.openlauncher.R;
import com.danielstone.materialaboutlibrary.ConvenienceBuilder;
import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.model.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.danielstone.materialaboutlibrary.model.MaterialAboutTitleItem;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

public class AboutActivity extends MaterialAboutActivity {

    private static final Notices notices = new Notices();
    static {
        notices.addNotice(new Notice("FastAdapter", "https://github.com/mikepenz/FastAdapter", "Mike Penz", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("CircularReveal", "https://github.com/ozodrukh/CircularReveal", "Abdullaev Ozodrukh", new MITLicense()));
        notices.addNotice(new Notice("MaterialScrollBar", "https://github.com/turing-tech/MaterialScrollBar", "Turing Technologies", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("Material About Library", "https://github.com/daniel-stoneuk/material-about-library", "Daniel Stone", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("Material Dialogs", "https://github.com/afollestad/material-dialogs", "Aidan Follestad", new MITLicense()));
        notices.addNotice(new Notice("Material Ripple Layout", "https://github.com/balysv/material-ripple", "Balys Valentukevicius", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("ImageBlurring ", "https://github.com/qiujuer/ImageBlurring", "Qiujuer", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("SimpleFingerGestures", "https://github.com/championswimmer/SimpleFingerGestures_Android_Library", "Arnav Gupta", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("TextDrawable", "https://github.com/amulyakhare/TextDrawable", "Amulya Khare", new MITLicense()));
        notices.addNotice(new Notice("AndroidOnboarder", "https://github.com/chyrta/AndroidOnboarder", "Dzmitry Chyrta, Daniel Morales", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("CustomActivityOnCrash", "https://github.com/Ereza/CustomActivityOnCrash", "Eduard Ereza Martínez", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("Butter Knife", "https://github.com/JakeWharton/butterknife", "Jake Wharton", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("jaredrummler colorpicker", "https://github.com/jaredrummler/ColorPicker", " jaredrummler", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("Android Support Library", "https://developer.android.com/topic/libraries/support-library/revisions.html", "The Android Open Source Project", new ApacheSoftwareLicense20()));
    }

    @Override
    protected MaterialAboutList getMaterialAboutList(Context context) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        });

        MaterialAboutCard.Builder titleCard = new MaterialAboutCard.Builder();
        titleCard.addItem(new MaterialAboutTitleItem(R.string.app_name, R.drawable.ic_launcher));
        try {
            titleCard.addItem(ConvenienceBuilder.createVersionActionItem(this, getResources().getDrawable(R.drawable.ic_info_outline_dark_24dp), getString(R.string.version), true));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        titleCard.addItem(ConvenienceBuilder.createWebsiteActionItem(this, getResources().getDrawable(R.drawable.ic_github_dark_24dp), "GitHub", false, Uri.parse("https://github.com/OpenLauncherTeam/openlauncher")));
        titleCard.addItem(new MaterialAboutActionItem(getString(R.string.about_libs), null, getResources().getDrawable(R.drawable.ic_library_gray_24dp), new MaterialAboutActionItem.OnClickListener() {
            @Override
            public void onClick() {
                new LicensesDialog.Builder(AboutActivity.this)
                        .setNotices(notices)
                        .setIncludeOwnLicense(true)
                        .build()
                        .show();
            }
        }));
        titleCard.addItem(ConvenienceBuilder.createRateActionItem(this, getResources().getDrawable(R.drawable.ic_thumb_up_dark_24dp), getString(R.string.about_rate), null));

        MaterialAboutCard.Builder opTeamCard = new MaterialAboutCard.Builder();
        opTeamCard.title(getString(R.string.about_team));

        opTeamCard.addItem(new MaterialAboutActionItem.Builder()
                .icon(R.drawable.person_bennykok)
                .text("BennyKok")
                .subText(getString(R.string.about_credit_text_bennykok))
                .setOnClickListener(ConvenienceBuilder.createWebsiteOnClickAction(this, Uri.parse("http://bennykok.weebly.com/contact.html")))
                .build());
        opTeamCard.addItem(new MaterialAboutActionItem.Builder()
                .icon(R.drawable.person_dkanada)
                .text("dkanada")
                .subText(getString(R.string.about_credit_text_dkanada))
                .setOnClickListener(ConvenienceBuilder.createWebsiteOnClickAction(this, Uri.parse("https://github.com/dkanada")))
                .build());
        opTeamCard.addItem(new MaterialAboutActionItem.Builder()
                .icon(R.drawable.person_gsantner)
                .text("Gregor Santner")
                .subText(getString(R.string.about_credit_text_gsantner))
                .setOnClickListener(ConvenienceBuilder.createWebsiteOnClickAction(this, Uri.parse("https://gsantner.github.io/")))
                .build());


        MaterialAboutCard.Builder contributorsCard = new MaterialAboutCard.Builder();
        contributorsCard.title(getString(R.string.about_credit));
        contributorsCard.addItem(new MaterialAboutActionItem.Builder()
                .icon(R.drawable.person_chris_debrodie)
                .text("Chris DeBrodie")
                .subText(R.string.about_credit_text_chris_debrodie)
                .setOnClickListener(ConvenienceBuilder.createWebsiteOnClickAction(this, Uri.parse("https://plus.google.com/111923938461696019967")))
                .build());
        contributorsCard.addItem(new MaterialAboutActionItem.Builder()
                .icon(R.drawable.person_gaukler_faun)
                .text("Gaukler Faun")
                .subText(R.string.about_credit_text_gaukler_faun)
                .setOnClickListener(ConvenienceBuilder.createWebsiteOnClickAction(this, Uri.parse("https://github.com/scoute-dich")))
                .build());
        contributorsCard.addItem(new MaterialAboutActionItem.Builder()
                .text(R.string.about_credit_text_all_contributors)
                .setOnClickListener(ConvenienceBuilder.createWebsiteOnClickAction(this, Uri.parse("https://github.com/OpenLauncherTeam/openlauncher/graphs/contributors")))
                .build());


        //authorCard.addItem(ConvenienceBuilder.createWebsiteActionItem(this,getResources().getDrawable(),"Chris DeBrodie",false, Uri.parse("")));

        return new MaterialAboutList.Builder()
                .addCard(titleCard.build())
                .addCard(opTeamCard.build())
                .addCard(contributorsCard.build())
                .build();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected CharSequence getActivityTitle() {
        return getString(R.string.pref_title__about);
    }
}
