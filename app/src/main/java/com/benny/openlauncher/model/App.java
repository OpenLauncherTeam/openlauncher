package com.benny.openlauncher.model;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;

import java.util.List;

public class App {
    public Drawable _icon;
    public String _label;
    public String _packageName;
    public String _className;
    public UserHandle _userHandle;
    public List<ShortcutInfo> _shortcutInfo;

    public App(PackageManager pm, ResolveInfo info, List<ShortcutInfo> shortcutInfo) {
        _icon = info.loadIcon(pm);
        _label = info.loadLabel(pm).toString();
        _packageName = info.activityInfo.packageName;
        _className = info.activityInfo.name;
        _shortcutInfo = shortcutInfo;
    }

    @SuppressLint("NewApi")
    public App(PackageManager pm, LauncherActivityInfo info, List<ShortcutInfo> shortcutInfo) {
        _icon = info.getIcon(0);
        _label = info.getLabel().toString();
        _packageName = info.getComponentName().getPackageName();
        _className = info.getName();
        _shortcutInfo = shortcutInfo;
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

    public List<ShortcutInfo> getShortcutInfo() {
        return _shortcutInfo;
    }
}
