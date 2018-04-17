package com.benny.openlauncher.interfaces;

public interface DialogListener {

    interface OnAddAppDrawerItemListener {
        void onAdd();
    }

    interface OnEditDialogListener {
        void onRename(String name);
    }
}
