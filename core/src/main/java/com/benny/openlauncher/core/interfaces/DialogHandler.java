package com.benny.openlauncher.core.interfaces;

import android.content.Context;
import android.view.DragEvent;

public interface DialogHandler<T extends Item> {

    void showPickAction(Context context, OnAddAppDrawerItemListener listener);

    void showEditDialog(Context context, T item, OnEditDialogListener listener);

    void showDeletePackageDialog(Context context, DragEvent dragEvent);

    interface OnAddAppDrawerItemListener {
        void onAdd();
    }

    interface OnEditDialogListener {
        void onRename(String name);
    }
}