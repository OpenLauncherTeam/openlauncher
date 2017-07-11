package com.benny.openlauncher.core.interfaces;

import java.text.SimpleDateFormat;

public interface SettingsManager {

    // Desktop
    int getDesktopPageCurrent();
    void setDesktopPageCurrent(int page);
    boolean isDesktopShowIndicator();
    int getDesktopColumnCount();
    int getDesktopRowCount();
    int getDesktopStyle();
    boolean isDesktopShowLabel();
    boolean isDesktopLock();
    void setDesktopLock(boolean locked);
    boolean isGestureFeedback();
    boolean isDesktopFullscreen();
    int getDesktopColor();
    int getDesktopIconSize();

    // Desktop Folder Popup
    int getPopupColor();
    int getPopupLabelColor();

    // Dock
    int getDockSize();
    boolean getGestureDockSwipeUp();
    boolean isDockShowLabel();
    int getDockColor();
    boolean getDockEnable();
    int getDockIconSize();

    // Drawer
    int getDrawerColumnCount();
    int getDrawerRowCount();
    boolean isDrawerShowIndicator();
    int getDrawerStyle();
    boolean isDrawerShowCardView();
    int getDrawerCardColor();
    boolean isDrawerShowLabel();
    int getDrawerLabelColor();
    boolean isDrawerRememberPosition();
    int getDrawerBackgroundColor();
    int getVerticalDrawerHorizontalMargin();
    int getVerticalDrawerVerticalMargin();
    int getDrawerIconSize();

    // SearchBar
    boolean getSearchBarEnable();
    String getSearchBarBaseURI();
    boolean searchBarTimeEnabled();
    SimpleDateFormat getUserDateFormat();

    // Others
    boolean getAppRestartRequired();
    void setAppRestartRequired(boolean required);
    boolean isAppFirstLaunch();
    void setAppFirstLaunch(boolean isAppFirstLaunch);
    boolean enableImageCaching();
}
