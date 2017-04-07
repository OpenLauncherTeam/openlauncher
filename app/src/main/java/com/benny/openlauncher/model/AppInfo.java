package com.benny.openlauncher.model;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private String code = null;
    private String name = null;
    private final Drawable icon;
    private boolean selected = false;

    public AppInfo(String code, String name, Drawable icon, boolean selected) {
        this.code = code;
        this.name = name;
        this.icon = icon;
        this.selected = selected;
    }

    public String getCode() {
        return code;
    }

    public Drawable getImage() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean paramBoolean) {
        selected = paramBoolean;
    }

}