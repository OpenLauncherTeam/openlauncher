package com.benny.openlauncher.core.interfaces;

import java.text.SimpleDateFormat;

public interface SettingsManager {

    // desktop
    int getDesktopColumnCount();

    int getDesktopRowCount();

    int getDesktopStyle();

    boolean isDesktopFullscreen();

    boolean isDesktopShowIndicator();

    boolean isDesktopShowLabel();

    int getDesktopBackgroundColor();

    int getDesktopFolderColor();

    int getFolderLabelColor();

    // not in app settings yet
    int getDesktopIconSize();

    // dock
    boolean getDockEnable();

    int getDockSize();

    boolean isDockShowLabel();

    int getDockColor();

    // not in app settings yet
    int getDockIconSize();

    // app drawer
    int getDrawerColumnCount();

    int getDrawerRowCount();

    int getDrawerStyle();

    boolean isDrawerShowCardView();

    boolean isDrawerRememberPosition();

    boolean isDrawerShowIndicator();

    boolean isDrawerShowLabel();

    int getDrawerBackgroundColor();

    int getDrawerCardColor();

    int getDrawerLabelColor();

    // not in app settings yet
    int getDrawerFastScrollColor();

    // not in app settings yet
    int getVerticalDrawerHorizontalMargin();

    // not in app settings yet
    int getVerticalDrawerVerticalMargin();

    // not in app settings yet
    int getDrawerIconSize();

    // search bar
    boolean getSearchBarEnable();

    String getSearchBarBaseURI();

    boolean getSearchBarForceBrowser();

    boolean getSearchBarShouldShowHiddenApps();

    // not in app settings yet
    boolean isSearchBarTimeEnabled();

    SimpleDateFormat getUserDateFormat();

    int getDesktopDateMode();

    // not in app settings yet
    int getDesktopDateTextColor();


    // not in app settings yet
    boolean isResetSearchBarOnOpen();

    // not in app settings yet
    boolean isSearchGridListSwitchEnabled();

    // not in app settings yet
    boolean isSearchUseGrid();

    // not in app settings yet
    void setSearchUseGrid(boolean enabled);

    // not in app settings yet
    int getSearchGridSize();

    // not in app settings yet
    int getSearchLabelLines();

    // gestures
    boolean getGestureDockSwipeUp();

    boolean isGestureFeedback();

    boolean isDesktopHideGrid();

    void setDesktopHideGrid(boolean hideGrid);

    // internal
    int getDesktopPageCurrent();

    void setDesktopPageCurrent(int page);

    boolean isDesktopLock();

    void setDesktopLock(boolean locked);

    void setDesktopIndicatorMode(int mode);

    int getDesktopIndicatorMode();

    boolean getAppRestartRequired();

    void setAppRestartRequired(boolean value);

    boolean isAppFirstLaunch();

    void setAppFirstLaunch(boolean value);

    boolean enableImageCaching();

    float getDrawerLabelFontSize();

    float getOverallAnimationSpeedModifier();
}
