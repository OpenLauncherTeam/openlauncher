package com.benny.openlauncher.core.interfaces;

import android.content.Intent;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Michael on 25.06.2017.
 */

public interface IItem<T extends IItem> extends Parcelable {

    enum Type {
        APP,
        SHORTCUT,
        GROUP,
        ACTION,
        WIDGET
    }
    int getId();
    Intent getIntent();
    String getLabel();
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
