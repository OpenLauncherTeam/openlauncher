package com.benny.openlauncher.core.interfaces;

import android.support.v7.widget.RecyclerView;

import com.mikepenz.fastadapter.IItem;

public interface FastItem {
    interface AppItem<T, VH extends RecyclerView.ViewHolder> extends IItem<T, VH> {
        App getApp();
    }

    interface DesktopOptionsItem<T, VH extends RecyclerView.ViewHolder> extends IItem<T, VH> {
        void setIcon(int icon);
    }

    interface LabelItem<T, VH extends RecyclerView.ViewHolder> extends IItem<T, VH>, LabelProvider {
        String getLabel();
    }
}
