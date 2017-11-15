package com.benny.openlauncher.core.util;

public class DragAction {
    public Action action;

    public DragAction(Action action) {
        this.action = action;
    }

    public enum Action {
        APP_DRAWER, APP, SEARCH_RESULT, SHORTCUT, GROUP, ACTION, WIDGET
    }
}
