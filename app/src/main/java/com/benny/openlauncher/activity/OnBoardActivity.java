package com.benny.openlauncher.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.benny.openlauncher.R;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.SlideFragment;
import agency.tango.materialintroscreen.SlideFragmentBuilder;
import agency.tango.materialintroscreen.widgets.OverScrollViewPager;
import agency.tango.materialintroscreen.widgets.SwipeableViewPager;

public class OnBoardActivity extends MaterialIntroActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO remove after sufficient time has passed
        if (!getSharedPreferences("quickSettings", Context.MODE_PRIVATE).getBoolean("firstStart", true)) {
            getSharedPreferences("app", Context.MODE_PRIVATE).edit().putBoolean(getResources().getString(R.string.pref_key__show_intro), false).commit();
        }
        if (!getSharedPreferences("app", Context.MODE_PRIVATE).getBoolean(getResources().getString(R.string.pref_key__show_intro), false)) {
            skipStart();
            return;
        }

        OverScrollViewPager overScrollLayout = findViewById(agency.tango.materialintroscreen.R.id.view_pager_slides);
        SwipeableViewPager viewPager = overScrollLayout.getOverScrollView();
        viewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);

        addSlide(new OnBoardActivity.CustomSlide());

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.materialRed)
                .buttonsColor(R.color.introButton)
                .image(R.drawable.intro_2)
                .title(getString(R.string.minibar))
                .description(getString(R.string.intro2_text))
                .build());

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.materialGreen)
                .buttonsColor(R.color.introButton)
                .image(R.drawable.intro_3)
                .title(getString(R.string.pref_title__app_drawer))
                .description(getString(R.string.intro3_text))
                .build());

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.materialBlue)
                .buttonsColor(R.color.introButton)
                .image(R.drawable.intro_4)
                .title(getString(R.string.pref_title__search_bar))
                .description(getString(R.string.intro4_text))
                .build());
    }

    @Override
    public void onFinish() {
        super.onFinish();
        setState();
    }

    private void skipStart() {
        setState();
        finish();
    }


    private void setState() {
        getSharedPreferences("app", Context.MODE_PRIVATE).edit().putBoolean(getResources().getString(R.string.pref_key__show_intro), false).apply();

        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    public static class CustomSlide extends SlideFragment {
        public CustomSlide() {
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.view_intro, container, false);
        }

        @Override
        public int backgroundColor() {
            return R.color.materialBlue;
        }

        @Override
        public int buttonsColor() {
            return R.color.introButton;
        }
    }
}
