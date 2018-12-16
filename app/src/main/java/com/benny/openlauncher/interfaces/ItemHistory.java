package com.benny.openlauncher.interfaces;

import android.view.View;

import com.benny.openlauncher.model.Item;

public interface ItemHistory {
    void setLastItem(Item item, View view);

    void revertLastItem();

    void consumeLastItem();
}
