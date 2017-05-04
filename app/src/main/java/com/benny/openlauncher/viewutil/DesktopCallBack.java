package com.benny.openlauncher.viewutil;

import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.RevertibleAction;
import com.benny.openlauncher.widget.AppItemView;

/**
 * Created by BennyKok on 11/3/2016.
 */

public interface DesktopCallBack extends RevertibleAction {
    boolean addItemToPoint(Item item, int x, int y);

    boolean addItemToPage(Item item, int page);

    boolean addItemToCell(Item item, int x, int y);

    void removeItem(AppItemView view);
}
