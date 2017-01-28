package com.benny.openlauncher.hideApps;

import android.graphics.drawable.Drawable;

public class AppInfo {
	private String code = null;
	private String name = null;
	private final Drawable icon;
	private boolean selected = false;

	public AppInfo(String paramString1, String paramString2, Drawable paramDrawable) {
		code = paramString1;
		name = paramString2;
		icon = paramDrawable;
		selected = false;
	}

	public String getCode()
	{
		return code;
	}

	public Drawable getImage()
	{
		return icon;
	}

	public String getName()
	{
		return name;
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setSelected(boolean paramBoolean)
	{
		selected = paramBoolean;
	}

}