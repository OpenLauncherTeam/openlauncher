package com.benny.openlauncher.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.benny.openlauncher.manager.Setup;

import android.support.annotation.NonNull;

public class App {
    public String _label, _packageName, _className;
    public SimpleIconProvider _iconProvider;
    public ResolveInfo _info;

    public App(Context context, ResolveInfo info, PackageManager pm) {
        _info = info;

        _iconProvider = Setup.imageLoader().createIconProvider(info.loadIcon(pm));
        _label = info.loadLabel(pm).toString();
        _packageName = info.activityInfo.packageName;
        _className = info.activityInfo.name;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof App) {
            App temp = (App) o;
            return _packageName.equals(temp._packageName);
        } else {
            return false;
        }
    }

    public String getLabel() {
        return _label;
    }

    public String getPackageName() {
        return _packageName;
    }


    public String getClassName() {
        return _className;
    }


    public SimpleIconProvider getIconProvider() {
        return _iconProvider;
    }

    public void setIconProvider(@NonNull SimpleIconProvider baseIconProvider) {

    }
}
