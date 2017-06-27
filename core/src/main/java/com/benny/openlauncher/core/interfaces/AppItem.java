package com.benny.openlauncher.core.interfaces;

import android.support.v7.widget.RecyclerView;

import com.mikepenz.fastadapter.IItem;

/**
 * Created by Michael on 25.06.2017.
 */

public interface AppItem<T, VH extends RecyclerView.ViewHolder> extends IItem<T, VH> {
    App getApp();
}
