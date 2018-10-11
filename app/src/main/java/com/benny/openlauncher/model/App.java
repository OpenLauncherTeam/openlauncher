package com.benny.openlauncher.model;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import android.graphics.drawable.Drawable;

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
        return "ComponentInfo{" + _packageName + "/" + _className + "}";
    }

    public String getFullName() {
        return _packageName + "/" + _className;
    }
}
