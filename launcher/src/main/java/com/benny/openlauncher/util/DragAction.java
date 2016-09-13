package com.benny.openlauncher.util;

public class DragAction
{
	public Action action;
    public int viewID;
	public DragAction (Action action,int viewID){
		this.action = action;
        this.viewID = viewID;
	}

	public enum Action {
		ACTION_GROUP, ACTION_APP, ACTION_APP_DRAWER, ACTION_WIDGET
	}
}
