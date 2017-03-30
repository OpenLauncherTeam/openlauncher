package com.benny.openlauncher.util;

public class DragAction {
    public Action action;

    public DragAction(Action action) {
        this.action = action;
    }

    public enum Action {
        ACTION_GROUP, ACTION_APP, ACTION_APP_DRAWER, ACTION_WIDGET, ACTION_SHORTCUT, ACTION_LAUNCHER
    }
}
