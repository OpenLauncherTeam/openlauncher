package com.benny.openlauncher.core.widget;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.benny.openlauncher.core.R;
import com.benny.openlauncher.core.interfaces.App;
import com.benny.openlauncher.core.interfaces.AppUpdateListener;
import com.benny.openlauncher.core.interfaces.IconLabelItem;
import com.benny.openlauncher.core.manager.Setup;
import com.benny.openlauncher.core.util.Tool;
import com.benny.openlauncher.core.viewutil.CircleDrawable;
import com.mikepenz.fastadapter.IItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class BaseSearchBar extends FrameLayout {
    public TextView searchClock;
    public AppCompatImageView searchButton;
    public AppCompatEditText searchInput;
    public RecyclerView searchRecycler;

    private static final long ANIM_TIME = 200;
    private FastItemAdapter<IconLabelItem> adapter = new FastItemAdapter<>();
    private CallBack callback;
    private boolean expanded;

    private boolean searchInternetEnabled = true;

    public BaseSearchBar(@NonNull Context context) {
        super(context);
        init();
    }

    public BaseSearchBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BaseSearchBar(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setSearchInternetEnabled(boolean enabled) {
        searchInternetEnabled = enabled;
    }

    public void setCallback(CallBack callback) {
        this.callback = callback;
    }

    public boolean collapse() {
        if (!expanded) return false;
        searchButton.callOnClick();
        return !expanded;
    }

    private void init() {
        searchClock = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.view_search_clock,this,false);
        LayoutParams clockParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clockParams.setMargins(Tool.dp2px(16, getContext()), 0, 0, 0);
        clockParams.gravity = Gravity.START;

        final CircleDrawable icon = new CircleDrawable(getContext(), getResources().getDrawable(R.drawable.ic_search_light_24dp), Color.BLACK);
        searchButton = new AppCompatImageView(getContext());
        searchButton.setImageDrawable(icon);
        searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expanded && searchInput.getText().length() > 0) {
                    searchInput.getText().clear();
                    return;
                }
                expanded = !expanded;
                if (expanded) {
                    if (callback != null) {
                        callback.onExpand();
                    }
                    icon.setIcon(getResources().getDrawable(R.drawable.ic_clear_white_24dp));
                    Tool.visibleViews(ANIM_TIME, searchInput, searchRecycler);
                    Tool.goneViews(ANIM_TIME, searchClock);
                } else {
                    if (callback != null) {
                        callback.onCollapse();
                    }
                    icon.setIcon(getResources().getDrawable(R.drawable.ic_search_light_24dp));
                    Tool.visibleViews(ANIM_TIME, searchClock);
                    Tool.goneViews(ANIM_TIME, searchInput, searchRecycler);
                    searchInput.getText().clear();
                }
            }
        });
        LayoutParams buttonParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(0, Tool.dp2px(10, getContext()), Tool.dp2px(16, getContext()), 0);
        buttonParams.gravity = Gravity.END;

        searchInput = new AppCompatEditText(getContext());
        searchInput.setVisibility(View.GONE);
        searchInput.setBackground(null);
        searchInput.setHint(R.string.search_hint);
        searchInput.setHintTextColor(Color.WHITE);
        searchInput.setTextColor(Color.WHITE);
        searchInput.setSingleLine();
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        final int dp8 = Tool.dp2px(8, getContext());
        LayoutParams inputParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputParams.setMargins(dp8, dp8, 0, 0);

        searchRecycler = new RecyclerView(getContext());
        searchRecycler.setVisibility(View.GONE);
        searchRecycler.setAdapter(adapter);
        searchRecycler.setClipToPadding(false);
        searchRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        Setup.get().getAppUpdatedListener(getContext()).add(new AppUpdateListener<App>() {
            @Override
            public boolean onAppUpdated(List<App> apps) {
                adapter.clear();
                List<IconLabelItem> items = new ArrayList<>();
                if (searchInternetEnabled) {
                    items.add(Setup.get().createSearchBarInternetItem(getContext(), R.string.search_online, new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            callback.onInternetSearch(searchInput.getText().toString());
                            searchInput.getText().clear();
                        }
                    }));
                }
                for (int i = 0; i < apps.size(); i++) {
                    final App app = apps.get(i);
                    items.add(Setup.get().createSearchBarItem(getContext(), app, new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startApp(v.getContext(), app);
                        }
                    }));
                }
                adapter.set(items);

                return false;
            }
        });
        adapter.getItemFilter().withFilterPredicate(new IItemAdapter.Predicate<IconLabelItem>() {
            @Override
            public boolean filter(IconLabelItem item, CharSequence constraint) {
                if (item.getLabel().equals(getContext().getString(R.string.search_online)))
                    return false;
                String s = constraint.toString();
                if (s.isEmpty())
                    return true;
                else if (item.getLabel().toLowerCase().contains(s.toLowerCase()))
                    return false;
                else
                    return true;
            }
        });
        final LayoutParams recyclerParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        addView(searchClock, clockParams);
        addView(searchInput, inputParams);
        addView(searchButton, buttonParams);
        addView(searchRecycler, recyclerParams);

        searchInput.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                searchInput.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                recyclerParams.setMargins(0, Tool.dp2px(50, getContext()) + searchInput.getHeight(), 0, 0);
            }
        });
    }

    protected void startApp(Context context, App app)
    {
        Tool.startApp(context, app);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            int topInset = insets.getSystemWindowInsetTop();
            setPadding(getPaddingLeft(), topInset +  Tool.dp2px(10, getContext()), getPaddingRight(), getPaddingBottom());
        }
        return insets;
    }

    public interface CallBack {
        void onInternetSearch(String string);

        void onExpand();

        void onCollapse();
    }
}
