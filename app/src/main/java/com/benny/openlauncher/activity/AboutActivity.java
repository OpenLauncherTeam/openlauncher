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

public class AboutActivity extends MaterialAboutActivity {

    @Override
    protected MaterialAboutList getMaterialAboutList(Context context) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        MaterialAboutCard.Builder titleCard = new MaterialAboutCard.Builder();
        titleCard.addItem(new MaterialAboutTitleItem(R.string.app_name, R.drawable.ic_launcher));
        try {
            titleCard.addItem(ConvenienceBuilder.createVersionActionItem(this, getResources().getDrawable(R.drawable.ic_info_outline_24dp), "Version", true));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        titleCard.addItem(ConvenienceBuilder.createWebsiteActionItem(this, getResources().getDrawable(R.drawable.ic_github), "GitHub", false, Uri.parse("https://github.com/BennyKok/OpenLauncher")));
        titleCard.addItem(ConvenienceBuilder.createWebsiteActionItem(this, getResources().getDrawable(R.drawable.ic_github), getString(R.string.about_libs), false, Uri.parse("https://github.com/BennyKok/OpenLauncher/wiki/Open-Source-libraries")));
        titleCard.addItem(ConvenienceBuilder.createRateActionItem(this, getResources().getDrawable(R.drawable.ic_thumb_up_24dp), getString(R.string.about_rate), null));

        MaterialAboutCard.Builder opTeamCard = new MaterialAboutCard.Builder();
        opTeamCard.title(getString(R.string.about_team));

        opTeamCard.addItem(new MaterialAboutActionItem.Builder()
                .icon(R.drawable.person__bennykok)
                .text("BennyKok")
                .subText(getString(R.string.about_credit_text__bennykok))
                .setOnClickListener(ConvenienceBuilder.createWebsiteOnClickAction(this, Uri.parse("http://bennykok.weebly.com/contact.html")))
                .build());
        opTeamCard.addItem(new MaterialAboutActionItem.Builder()
                .icon(R.drawable.person__gsantner)
                .text("Gregor Santner")
                .subText(getString(R.string.about_credit_text__gsantner))
                .setOnClickListener(ConvenienceBuilder.createWebsiteOnClickAction(this, Uri.parse("https://gsantner.github.io/")))
                .build());
        opTeamCard.addItem(new MaterialAboutActionItem.Builder()
                .icon(R.drawable.person__gaukler_faun)
                .text("Gaukler Faun")
                .subText(getString(R.string.about_credit_text__gaukler_faun))
                .setOnClickListener(ConvenienceBuilder.createWebsiteOnClickAction(this, Uri.parse("https://github.com/scoute-dich")))
                .build());
        opTeamCard.addItem(new MaterialAboutActionItem.Builder()
                .icon(R.drawable.person__dkanada)
                .text("dkanada")
                .subText(getString(R.string.about_credit_text__dkanada))
                .setOnClickListener(ConvenienceBuilder.createWebsiteOnClickAction(this, Uri.parse("https://github.com/dkanada")))
                .build());

        MaterialAboutCard.Builder contributorsCard = new MaterialAboutCard.Builder();
        contributorsCard.title(getString(R.string.about_credit));
        contributorsCard.addItem(new MaterialAboutActionItem.Builder()
                .icon(R.drawable.person__chris_debrodie)
                .text("Chris DeBrodie")
                .subText(getString(R.string.about_credit_text__chris_debrodie))
                .setOnClickListener(ConvenienceBuilder.createWebsiteOnClickAction(this, Uri.parse("https://plus.google.com/111923938461696019967")))
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
        return getString(R.string.about);
    }
}
