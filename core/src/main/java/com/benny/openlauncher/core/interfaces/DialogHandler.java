package com.benny.openlauncher.core.interfaces;

import android.content.Context;
import android.view.DragEvent;

public interface DialogHandler<T extends Item> {

    void showPickAction(Context context, OnAddAppDrawerItemListener listener);
    void showEditDialog(Context context, T item, OnEditDialog resultHandler);
    void showDeletePackageDialog(Context context, DragEvent dragEvent);

    interface OnAddAppDrawerItemListener {
        void onAdd();
    }

    interface OnEditDialog {
        void onRename(String name);
    }
}