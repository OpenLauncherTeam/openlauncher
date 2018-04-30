package com.benny.openlauncher.model;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private String _code;
    private String _name;
    private Drawable _icon;

    public AppInfo(String code, String name, Drawable icon) {
        _code = code;
        _name = name;
        _icon = icon;
    }

    public String getCode() {
        return _code;
    }

    public Drawable getImage() {
        return _icon;
    }

    public String getName() {
        return _name;
    }
}