package com.benny.openlauncher.util;

import com.benny.openlauncher.widget.Desktop;

/**
 * Created by BennyKok on 11/3/2016.
 */

public interface DesktopCallBack extends RevertibleAction{
    void removeItemFromSettings(Desktop.Item item);
    void addItemToSettings(Desktop.Item item);
    boolean addItemToPosition(Desktop.Item item, int x, int y);
    void addItemToPagePosition(Desktop.Item item,int page);
}
