package com.benny.openlauncher.core.interfaces;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Michael on 25.06.2017.
 */

public interface AppItem<T, VH extends RecyclerView.ViewHolder> extends com.mikepenz.fastadapter.IItem<T, VH>{
    App getApp();
}
