package com.benny.openlauncher.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.benny.openlauncher.manager.Setup;

import org.jetbrains.annotations.NotNull;

public class App {
    public String label, packageName, className;
    public BaseIconProvider iconProvider;
    public ResolveInfo info;

    public App(Context context, ResolveInfo info, PackageManager pm) {
        this.info = info;

        iconProvider = Setup.imageLoader().createIconProvider(info.loadIcon(pm));
        label = info.loadLabel(pm).toString();
        packageName = info.activityInfo.packageName;
        className = info.activityInfo.name;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof App) {
            App temp = (App) o;
            return this.packageName.equals(temp.packageName);
        } else {
            return false;
        }
    }

    public String getLabel() {
        return label;
    }

    public String getPackageName() {
        return packageName;
    }


    public String getClassName() {
        return className;
    }


    public BaseIconProvider getIconProvider() {
        return iconProvider;
    }


    public void setLabel(@NotNull String s) {

    }


    public void setPackageName(@NotNull String s) {

    }


    public void setClassName(@NotNull String s) {

    }


    public void setIconProvider(@NotNull BaseIconProvider baseIconProvider) {

    }
}
