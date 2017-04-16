package com.benny.openlauncher.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.Tool;

import io.codetail.widget.RevealFrameLayout;

import static com.benny.openlauncher.widget.AppDrawerController.DrawerMode.Vertical;

/**
 * Created by BennyKok on 11/5/2016.
 */

public class AppDrawerController extends RevealFrameLayout implements TextWatcher {

    public EditText searchBar;
    public AppDrawer_Paged drawerViewPaged;
    public AppDrawer_Vertical drawerViewGrid;
    public DrawerMode drawerMode;
    private CallBack openCallBack, closeCallBack;

    private Animator appDrawerAnimator;
    private Long drawerAnimationTime = 200L;

    public AppDrawerController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AppDrawerController(Context context) {
        super(context);
    }

    public AppDrawerController(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setCallBack(CallBack openCallBack, CallBack closeCallBack) {
        this.openCallBack = openCallBack;
        this.closeCallBack = closeCallBack;
    }

    public View getDrawer(){
        return getChildAt(0);
    }

    public void open(int cx, int cy, int startRadius, int finalRadius) {
        appDrawerAnimator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(getChildAt(0), cx, cy, startRadius, finalRadius);
        appDrawerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        appDrawerAnimator.setDuration(drawerAnimationTime);
        appDrawerAnimator.setStartDelay(100L);
        openCallBack.onStart();
        appDrawerAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator p1) {
                getChildAt(0).setVisibility(View.VISIBLE);

                ObjectAnimator animator = ObjectAnimator
                        .ofPropertyValuesHolder(getBackground(),
                                PropertyValuesHolder.ofInt("alpha", 0, 255));
                animator.setDuration(200);
                animator.start();

                switch (drawerMode) {
                    case Paged:
                        for (int i = 0; i < drawerViewPaged.pages.size(); i++) {
                            drawerViewPaged.pages.get(i).findViewById(R.id.cc).setAlpha(1);
                        }
                        if (drawerViewPaged.pages.size() > 0) {
                            View mGrid = drawerViewPaged.pages.get(drawerViewPaged.getCurrentItem()).findViewById(R.id.cc);
                            mGrid.setAlpha(0);
                            mGrid.animate().alpha(1).setDuration(150L).setStartDelay(drawerAnimationTime - 50).setInterpolator(new AccelerateDecelerateInterpolator());
                        }
                        break;
                    case Vertical:
                        drawerViewGrid.recyclerView.setAlpha(0);
                        drawerViewGrid.recyclerView.animate().alpha(1).setDuration(150L).setStartDelay(drawerAnimationTime - 50).setInterpolator(new AccelerateDecelerateInterpolator());
                        break;
                }
            }

            @Override
            public void onAnimationEnd(Animator p1) {
                openCallBack.onEnd();
            }

            @Override
            public void onAnimationCancel(Animator p1) {
            }

            @Override
            public void onAnimationRepeat(Animator p1) {
            }
        });

        appDrawerAnimator.start();
    }

    public void close(int cx, int cy, int startRadius, int finalRadius) {
        if (appDrawerAnimator == null || appDrawerAnimator.isRunning())
            return;

        appDrawerAnimator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(getChildAt(0), cx, cy, finalRadius, startRadius);
        appDrawerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        appDrawerAnimator.setDuration(drawerAnimationTime);
        appDrawerAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator p1) {
                closeCallBack.onStart();

                ObjectAnimator animator = ObjectAnimator
                        .ofPropertyValuesHolder(getBackground(),
                                PropertyValuesHolder.ofInt("alpha", 255, 0));
                animator.setDuration(200);
                animator.start();
            }

            @Override
            public void onAnimationEnd(Animator p1) {
                closeCallBack.onEnd();
            }

            @Override
            public void onAnimationCancel(Animator p1) {
            }

            @Override
            public void onAnimationRepeat(Animator p1) {
            }
        });

        switch (drawerMode) {
            case Paged:
                if (drawerViewPaged.pages.size() > 0) {
                    View mGrid = drawerViewPaged.pages.get(drawerViewPaged.getCurrentItem()).findViewById(R.id.cc);
                    mGrid.animate().setStartDelay(0).alpha(0).setDuration(60L).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            appDrawerAnimator.start();
                        }
                    });
                }
                break;
            case Vertical:
                drawerViewGrid.recyclerView.animate().setStartDelay(0).alpha(0).setDuration(60L).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        appDrawerAnimator.start();

                    }
                });
                break;
        }
    }

    public void init() {
        if (isInEditMode())
            return;
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        drawerMode = LauncherSettings.getInstance(getContext()).generalSettings.drawerMode;
        switch (drawerMode) {
            case Paged:
                drawerViewPaged = (AppDrawer_Paged) layoutInflater.inflate(R.layout.view_pageddrawer, this, false);
                addView(drawerViewPaged);
                View indicator = layoutInflater.inflate(R.layout.view_drawerindicator, this, false);
                addView(indicator);
                break;
            case Vertical:
                drawerViewGrid = (AppDrawer_Vertical) layoutInflater.inflate(R.layout.view_griddrawer, this, false);
                if (LauncherSettings.getInstance(getContext()).generalSettings.drawerSearchBar)
                    ((LayoutParams) drawerViewGrid.getLayoutParams()).topMargin += Tool.dp2px(60, getContext());
                addView(drawerViewGrid);
                break;
        }
        if (LauncherSettings.getInstance(getContext()).generalSettings.drawerSearchBar && drawerMode == Vertical) {
            CardView cv = (CardView) LayoutInflater.from(getContext()).inflate(R.layout.view_searchbar_app, this, false);
            addView(cv);
            cv.setBackgroundColor(LauncherSettings.getInstance(getContext()).generalSettings.drawerCardColor);
            if (!LauncherSettings.getInstance(getContext()).generalSettings.drawerLight) {
                ImageView searchIcon = (ImageView) cv.findViewById(R.id.searchIcon);
                searchIcon.setImageResource(R.drawable.ic_search_light_24dp);
            }
            searchBar = (EditText) cv.findViewById(R.id.et);
            searchBar.setHintTextColor(LauncherSettings.getInstance(getContext()).generalSettings.drawerLabelColor);
            searchBar.setTextColor(LauncherSettings.getInstance(getContext()).generalSettings.drawerLabelColor);
            searchBar.clearFocus();
            searchBar.addTextChangedListener(this);
        }
    }

    public void reloadDrawerCardTheme() {
        switch (drawerMode) {
            case Paged:
                drawerViewPaged.resetAdapter();
                break;
            case Vertical:
                if (!LauncherSettings.getInstance(getContext()).generalSettings.drawerUseCard) {
                    drawerViewGrid.setCardBackgroundColor(Color.TRANSPARENT);
                    drawerViewGrid.setCardElevation(0);
                } else {
                    drawerViewGrid.setCardBackgroundColor(LauncherSettings.getInstance(getContext()).generalSettings.drawerCardColor);
                    drawerViewGrid.setCardElevation(Tool.dp2px(4, getContext()));
                }
                if (drawerViewGrid.gridDrawerAdapter != null)
                    drawerViewGrid.gridDrawerAdapter.notifyDataSetChanged();
                break;
        }
    }

    public void scrollToStart() {
        switch (drawerMode) {
            case Paged:
                drawerViewPaged.setCurrentItem(0, false);
                break;
            case Vertical:
                drawerViewGrid.recyclerView.scrollToPosition(0);
                break;
        }
    }

    public void setHome(Home home) {
        switch (drawerMode) {
            case Paged:
                drawerViewPaged.withHome(home, (PagerIndicator) findViewById(R.id.appDrawerIndicator));
                break;
            case Vertical:
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        switch (drawerMode) {
            case Paged:
                break;
            case Vertical:
                if (drawerViewGrid.gridDrawerAdapter != null)
                    drawerViewGrid.gridDrawerAdapter.filter(charSequence);
                break;
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    public enum DrawerMode {
        Paged, Vertical
    }

    public interface CallBack {
        void onStart();

        void onEnd();
    }
}
