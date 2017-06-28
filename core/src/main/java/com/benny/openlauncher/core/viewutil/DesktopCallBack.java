package com.benny.openlauncher.core.viewutil;

import android.view.View;

import com.benny.openlauncher.core.interfaces.Item;
import com.benny.openlauncher.core.util.RevertibleAction;

public interface DesktopCallBack<T extends Item, V extends View> extends RevertibleAction {
    boolean addItemToPoint(T item, int x, int y);

    boolean addItemToPage(T item, int page);

    boolean addItemToCell(T item, int x, int y);

    void removeItem(V view);
}
