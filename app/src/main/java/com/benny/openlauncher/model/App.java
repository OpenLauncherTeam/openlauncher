package com.benny.openlauncher.model;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Locale;

public class App {
    public Drawable _icon;
    public String _label;

    @Nullable public String _universalLabel;

    public String _packageName;
    public String _className;

    public App(PackageManager pm, ResolveInfo info) {
        _icon = info.loadIcon(pm);
        _label = info.loadLabel(pm).toString();
        _packageName = info.activityInfo.packageName;
        _className = info.activityInfo.name;

        try {
            updateUniversalLabel(pm, info);
            Log.d("AppModel", "Universal label " + getUniversalLabel());
        } catch (Exception e) {
            Log.e("AppModel", "Cannot resolve universal label for " + _label, e);
        }
    }

    private void updateUniversalLabel(PackageManager pm, ResolveInfo info) throws PackageManager.NameNotFoundException {
        ApplicationInfo appInfo = info.activityInfo.applicationInfo;

        Configuration config = new Configuration();
        config.locale = Locale.ROOT;

        Resources resources = pm.getResourcesForApplication(appInfo);
        resources.updateConfiguration(config, null);

        setUniversalLabel(resources.getString(appInfo.labelRes));
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

    /**
     * App label for root locale.
     * @see Locale#ROOT
     */
    @Nullable
    public String getUniversalLabel() {
        return _universalLabel;
    }

    public void setUniversalLabel(@Nullable String universalLabel) {
        this._universalLabel = universalLabel;
    }
}
