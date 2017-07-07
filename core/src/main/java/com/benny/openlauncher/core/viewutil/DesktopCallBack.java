package com.benny.openlauncher.core.viewutil;

import android.view.View;

import com.benny.openlauncher.core.model.Item;
import com.benny.openlauncher.core.util.RevertibleAction;

public interface DesktopCallBack<V extends View> extends RevertibleAction {
    boolean addItemToPoint(Item item, int x, int y);

    boolean addItemToPage(Item item, int page);

    boolean addItemToCell(Item item, int x, int y);

    void removeItem(V view);
}
