package com.benny.openlauncher.core.interfaces;

import java.text.SimpleDateFormat;

public interface SettingsManager {
    int getDesktopPageCurrent();

    void setDesktopPageCurrent(int page);

    boolean isDesktopShowIndicator();

    int getDesktopColumnCount();

    int getDesktopRowCount();

    int getDesktopStyle();

    boolean isDesktopShowLabel();

    int getDockSize();

    boolean getGestureDockSwipeUp();

    boolean isDesktopLock();

    void setDesktopLock(boolean locked);

    boolean isGestureFeedback();

    int getIconSize();

    boolean isDockShowLabel();

    int getDrawerColumnCount();

    int getDrawerRowCount();

    boolean isDrawerShowIndicator();

    int getDrawerStyle();

    boolean isDrawerShowCardView();

    int getDrawerCardColor();

    boolean isDrawerShowLabel();

    int getDrawerLabelColor();

    /**
     * @return color of a folder popup's CardView or -1 if xml default color should be used
     */
    int getPopupColor();

    /**
     * @return color of a folder popup's text color for icons
     */
    int getPopupLabelColor();

    boolean isDrawerRememberPosition();

    boolean isDesktopFullscreen();

    int getDesktopColor();

    int getDockColor();

    int getDrawerBackgroundColor();

    boolean getDockEnable();

    boolean getSearchBarEnable();

    boolean getAppRestartRequired();

    void setAppRestartRequired(boolean required);

    boolean isAppFirstLaunch();

    void setAppFirstLaunch(boolean isAppFirstLaunch);

    String getSearchBarBaseURI();

    boolean searchBarTimeEnabled();

    SimpleDateFormat getUserDateFormat();

    boolean enableImageCaching();
}
