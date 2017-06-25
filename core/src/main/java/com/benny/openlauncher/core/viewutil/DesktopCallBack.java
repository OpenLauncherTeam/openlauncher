package com.benny.openlauncher.core.viewutil;

import android.view.View;

import com.benny.openlauncher.core.interfaces.IItem;
import com.benny.openlauncher.core.util.RevertibleAction;

/**
 * Created by BennyKok on 11/3/2016.
 */

public interface DesktopCallBack<T extends IItem, V extends View> extends RevertibleAction {
    boolean addItemToPoint(T item, int x, int y);

    boolean addItemToPage(T item, int page);

    boolean addItemToCell(T item, int x, int y);

    void removeItem(V view);
}
