package com.benny.openlauncher.activity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;

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
        MaterialAboutCard.Builder titleCard = new MaterialAboutCard.Builder();
        titleCard.addItem(new MaterialAboutTitleItem(R.string.app_name,R.mipmap.ic_launcher));
        try {
            titleCard.addItem(ConvenienceBuilder.createVersionActionItem(this,getResources().getDrawable(R.drawable.ic_info_outline_24dp),"Version",true));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        titleCard.addItem(ConvenienceBuilder.createWebsiteActionItem(this,getResources().getDrawable(R.drawable.ic_github),"GitHub",false, Uri.parse("https://github.com/BennyKok/OpenLauncher")));
        titleCard.addItem(ConvenienceBuilder.createWebsiteActionItem(this,getResources().getDrawable(R.drawable.ic_github),"Open Source libraries",false, Uri.parse("https://github.com/BennyKok/OpenLauncher/wiki/Open-Source-libraries")));
        titleCard.addItem(ConvenienceBuilder.createRateActionItem(this,getResources().getDrawable(R.drawable.ic_thumb_up_24dp),"Rate",null));

        MaterialAboutCard.Builder authorCard = new MaterialAboutCard.Builder();
        authorCard.title("Developer");
        authorCard.addItem(ConvenienceBuilder.createWebsiteActionItem(this,getResources().getDrawable(R.drawable.ic_benny),"BennyKok",false, Uri.parse("http://bennykok.weebly.com/contact.html")));

        MaterialAboutCard.Builder creditCard = new MaterialAboutCard.Builder();
        creditCard.title("Credit");
        creditCard.addItem(new MaterialAboutActionItem.Builder()
                .icon(R.mipmap.ic_chris)
                .text("Chris DeBrodie")
                .subText("Helped me a lot with bugs and suggestion")
                .setOnClickListener(ConvenienceBuilder.createWebsiteOnClickAction(this,Uri.parse("https://plus.google.com/111923938461696019967")))
                .build());
        //authorCard.addItem(ConvenienceBuilder.createWebsiteActionItem(this,getResources().getDrawable(),"Chris DeBrodie",false, Uri.parse("")));

        return new MaterialAboutList.Builder()
                .addCard(titleCard.build())
                .addCard(authorCard.build())
                .addCard(creditCard.build())
                .build();
    }

    @Override
    protected CharSequence getActivityTitle() {
        return "About";
    }
}
