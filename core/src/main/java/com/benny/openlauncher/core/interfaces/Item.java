package com.benny.openlauncher.core.interfaces;

import android.content.Intent;
import android.os.Parcelable;

import java.util.List;

public interface Item<T extends Item, ID extends Number> extends LabelProvider, Parcelable {

    enum Type {
        APP,
        SHORTCUT,
        GROUP,
        ACTION,
        WIDGET
    }

    ID getItemId();

    Intent getIntent();

    void setLabel(String label);

    Type getType();

    List<T> getGroupItems();

    int getX();

    int getY();

    void setX(int x);

    void setY(int y);

    int getSpanX();

    int getSpanY();

    void setSpanX(int x);

    void setSpanY(int y);

    void reset();
}
