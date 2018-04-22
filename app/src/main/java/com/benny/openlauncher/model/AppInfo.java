package com.benny.openlauncher.model;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private String _code = null;
    private String _name = null;
    private final Drawable _icon;
    private boolean _selected = false;

    public AppInfo(String code, String name, Drawable icon, boolean selected) {
        _code = code;
        _name = name;
        _icon = icon;
        _selected = selected;
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

    public boolean isSelected() {
        return _selected;
    }

    public void setSelected(boolean paramBoolean) {
        _selected = paramBoolean;
    }
}