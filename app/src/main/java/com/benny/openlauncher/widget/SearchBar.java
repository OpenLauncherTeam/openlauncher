package com.benny.openlauncher.widget;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import com.benny.openlauncher.core.interfaces.App;
import com.benny.openlauncher.core.widget.BaseSearchBar;
import com.benny.openlauncher.util.Tool;

public class SearchBar extends BaseSearchBar
{
    public SearchBar(@NonNull Context context) {
        super(context);
    }

    public SearchBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchBar(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void startApp(Context context, App app) {
        Tool.startApp(context, app);
    }
}
