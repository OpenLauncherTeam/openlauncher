package com.benny.openlauncher.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.widget.EditText;

/**
 * Created by BennyKok on 11/16/2016.
 */

public class SearchBar extends EditText {

    public SearchBar(Context context) {
        super(context);
    }

    public SearchBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onDragEvent(DragEvent event) {
        return false;
    }
}
