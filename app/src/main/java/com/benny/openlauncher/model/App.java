package com.benny.openlauncher.model;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import android.graphics.drawable.Drawable;
import android.util.Log;

public class App {
    public Drawable _icon;
    public String _label;
    public String _packageName;
    public String _className;

    public App(PackageManager pm, ResolveInfo info) {
        _icon = info.loadIcon(pm);
        _label = info.loadLabel(pm).toString();
        _packageName = info.activityInfo.packageName;
        _className = info.activityInfo.name;
    }

    public App(PackageManager pm, ApplicationInfo info) {
        _icon = info.loadIcon(pm);
        _label = info.loadLabel(pm).toString();
        _packageName = info.packageName;
        _className = info.name;
        try {
            // there is definitely a better way to store the apps
            // should probably just store component name
            Intent intent = pm.getLaunchIntentForPackage(_packageName);
            ComponentName componentName = intent.getComponent();
            _className = componentName.getClassName();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof App) {
            App app = (App) object;
            return _packageName.equals(app._packageName);
        } else {
            return false;
        }
    }

    public void setIcon(Drawable icon) {
        _icon = icon;
    }

    public Drawable getIcon() {
        return _icon;
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

    public String getComponentName() {
        return new ComponentName(_packageName, _className).toString();
    }
}
