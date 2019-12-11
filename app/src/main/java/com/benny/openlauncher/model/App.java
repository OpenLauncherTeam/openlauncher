package com.benny.openlauncher.model;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;

public class App {
    public Drawable _icon;
    public String _label;
    public String _packageName;
    public String _className;
    public UserHandle _userHandle;

    public App(PackageManager pm, ResolveInfo info) {
        _icon = info.loadIcon(pm);
        _label = info.loadLabel(pm).toString();
        _packageName = info.activityInfo.packageName;
        _className = info.activityInfo.name;
    }

    @SuppressLint("NewApi")
    public App(PackageManager pm, LauncherActivityInfo info) {
        _icon = info.getIcon(0);
        _label = info.getLabel().toString();
        _packageName = info.getComponentName().getPackageName();
        _className = info.getName();
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
