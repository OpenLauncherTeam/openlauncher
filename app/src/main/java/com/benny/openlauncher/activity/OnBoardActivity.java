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

        if (!getSharedPreferences("quickSettings", Context.MODE_PRIVATE).getBoolean("firstStart", true)) {
            skipStart();
            return;
        }

        OverScrollViewPager overScrollLayout = findViewById(agency.tango.materialintroscreen.R.id.view_pager_slides);
        SwipeableViewPager viewPager = overScrollLayout.getOverScrollView();
        viewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);

        addSlide(new OnBoardActivity.CustomSlide());

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.op_red)
                .buttonsColor(R.color.intro_button_color)
                .image(R.drawable.intro_2)
                .description(getString(R.string.intro2_text))
                .build());

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.op_green)
                .buttonsColor(R.color.intro_button_color)
                .image(R.drawable.intro_3)
                .title(getString(R.string.intro3_title))
                .description(getString(R.string.intro3_text))
                .build());

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.op_blue)
                .buttonsColor(R.color.intro_button_color)
                .image(R.drawable.intro_4)
                .description(getString(R.string.intro_finish))
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
        getSharedPreferences("quickSettings", Context.MODE_PRIVATE).edit().putBoolean("firstStart", false).apply();

        Intent intent = new Intent(this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    public static class CustomSlide extends SlideFragment {
        public CustomSlide() {
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.intro_custom_layout, container, false);
        }

        @Override
        public int backgroundColor() {
            return R.color.op_blue;
        }

        @Override
        public int buttonsColor() {
            return R.color.intro_button_color;
        }
    }
}
