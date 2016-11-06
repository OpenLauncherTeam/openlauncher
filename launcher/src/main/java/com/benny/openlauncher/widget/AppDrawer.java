package com.benny.openlauncher.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.util.LauncherSettings;

import io.codetail.widget.RevealFrameLayout;

/**
 * Created by BennyKok on 11/5/2016.
 */

public class AppDrawer extends RevealFrameLayout{

    private PagedAppDrawer drawerViewp;
    private GridAppDrawer drawerViewg;
    private DrawerMode drawerMode;

    public AppDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public AppDrawer(Context context) {
        super(context);

        init();
    }

    public AppDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    public void init(){
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        drawerMode = LauncherSettings.getInstance(getContext()).generalSettings.drawerMode;
        switch (drawerMode){
            case Paged:
                drawerViewp = (PagedAppDrawer) layoutInflater.inflate(R.layout.view_pageddrawer,this,false);
                addView(drawerViewp);
                addView(layoutInflater.inflate(R.layout.view_drawerindicator,this,false));
                break;
            case Grid:
                drawerViewg = (GridAppDrawer) layoutInflater.inflate(R.layout.view_griddrawer,this,false);
                addView(drawerViewg);
                break;
        }
    }

    public void scrollToStart(){
        switch (drawerMode){
            case Paged:
                drawerViewp.setCurrentItem(0,false);
                break;
            case Grid:
                drawerViewg.rv.scrollToPosition(0);
                break;
        }
    }

    public void withHome(Home home){
        switch (drawerMode){
            case Paged:
                drawerViewp.withHome(home, (PagerIndicator) findViewById(R.id.appDrawerIndicator));
                break;
            case Grid:
                break;
        }
    }


    public static void startStylePicker(final Context context){
        final String[] items = new String[DrawerMode.values().length];
        for (int i = 0; i < DrawerMode.values().length; i++) {
            items[i] = DrawerMode.values()[i].name();
        }
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title("App drawer style")
                .items(items)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        LauncherSettings.getInstance(context).generalSettings.drawerMode = DrawerMode.valueOf(items[position]);
                    }
                }).show();
    }

    public enum DrawerMode{
        Paged,Grid
    }
}
