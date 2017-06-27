package com.benny.openlauncher.core.interfaces;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.mikepenz.fastadapter.IItem;

/**
 * Created by flisar on 26.06.2017.
 */

public interface IconLabelItem<T, VH extends RecyclerView.ViewHolder> extends IItem<T, VH> {

    // Context just for compatibility for the OpenLauncher IconLabelItem which holds drawables in the items
    void setIcon(Context context, int resId);

    String getLabel();

}
