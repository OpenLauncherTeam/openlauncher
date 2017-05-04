package com.benny.openlauncher.util;

public class DragAction {
    public Action action;

    public DragAction(Action action) {
        this.action = action;
    }

    public enum Action {
        APP_DRAWER, APP, SHORTCUT, GROUP, ACTION, WIDGET
    }
}
