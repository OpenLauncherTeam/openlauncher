package com.benny.openlauncher.viewutil;

import android.view.View;

import com.benny.openlauncher.util.RevertibleAction;
import com.benny.openlauncher.widget.Desktop;

/**
 * Created by BennyKok on 11/3/2016.
 */

public interface DesktopCallBack extends RevertibleAction {
    void removeItemFromSettings(Desktop.Item item);

    void addItemToSettings(Desktop.Item item);

    boolean addItemToPoint(Desktop.Item item, int x, int y);

    boolean addItemToPage(Desktop.Item item, int page);

    boolean addItemToCell(Desktop.Item item, int x, int y);

    void removeItemFromPage(View itemView, int page);
}
