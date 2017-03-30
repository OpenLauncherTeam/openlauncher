package com.benny.openlauncher.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by BennyKok on 11/16/2016.
 */

public class SearchBar extends android.support.v7.widget.AppCompatEditText implements View.OnClickListener {

    public SearchBar(Context context) {
        super(context);

        init();
    }

    public SearchBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public void init() {
        setFocusable(false);
        setFocusableInTouchMode(false);

        setOnClickListener(this);
    }

    @Override
    public boolean onDragEvent(DragEvent event) {
        return false;
    }

    @Override
    public void onEditorAction(int actionCode) {
        super.onEditorAction(actionCode);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            getText().clear();
            setFocusable(false);
            setFocusableInTouchMode(false);

            return false;
        }
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
    }
}
