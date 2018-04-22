package com.benny.openlauncher.interfaces;

public interface DialogListener {

    interface OnAddAppDrawerItemListener {
        void onAdd(int type);
    }

    interface OnEditDialogListener {
        void onRename(String name);
    }
}
