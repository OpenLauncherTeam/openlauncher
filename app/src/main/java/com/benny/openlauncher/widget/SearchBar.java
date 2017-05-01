package com.benny.openlauncher.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ScaleDrawable;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import com.benny.openlauncher.R;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.CircleColorable;
import com.benny.openlauncher.viewutil.IconLabelItem;
import com.mikepenz.fastadapter.IItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BennyKok on 4/27/2017.
 */

public class SearchBar extends FrameLayout {

    private static final long ANIM_TIME = 200;
    public AppCompatImageView searchButton;
    public AppCompatEditText searchBox;
    public RecyclerView searchItemRecycler;
    private FastItemAdapter<IconLabelItem> adapter = new FastItemAdapter<>();
    private boolean expanded;
    private CallBack callBack;

    public SearchBar(@NonNull Context context) {
        super(context);
        init();
    }

    public SearchBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SearchBar(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public boolean collapse() {
        if (!expanded) return false;
        searchButton.callOnClick();
        return !expanded;
    }

    private void init() {
        searchButton = new AppCompatImageView(getContext());
        final CircleColorable imageDrawable = new CircleColorable(getContext(), getResources().getDrawable(R.drawable.ic_search_light_24dp), Color.BLACK);
        searchButton.setImageDrawable(imageDrawable);
        searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                expanded = !expanded;

                if (expanded) {
                    if (callBack != null)
                        callBack.onExpand();

                    imageDrawable.setIcon(getResources().getDrawable(R.drawable.ic_clear_white_24dp));

                    Tool.visibleViews(ANIM_TIME, searchBox, searchItemRecycler);
                } else {
                    if (callBack != null)
                        callBack.onCollapse();

                    imageDrawable.setIcon(getResources().getDrawable(R.drawable.ic_search_light_24dp));

                    searchBox.getText().clear();
                    Tool.invisibleViews(ANIM_TIME, searchBox, searchItemRecycler);
                }
            }
        });
        LayoutParams sblp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sblp.setMargins(0, Tool.dp2px(16, getContext()), Tool.dp2px(4, getContext()), 0);
        sblp.gravity = Gravity.END;


        searchBox = new AppCompatEditText(getContext());
        searchBox.setVisibility(View.INVISIBLE);
        searchBox.setBackground(null);
        searchBox.setHint("Search...");
        searchBox.setHintTextColor(Color.WHITE);
        searchBox.setTextColor(Color.WHITE);
        searchBox.setSingleLine();
        searchBox.addTextChangedListener(new TextWatcher() {
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
        LayoutParams sboxlp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sboxlp.setMargins(Tool.dp2px(8, getContext()), Tool.dp2px(12, getContext()), 0, 0);


        searchItemRecycler = new RecyclerView(getContext());
        searchItemRecycler.setVisibility(INVISIBLE);
        searchItemRecycler.setAdapter(adapter);
        searchItemRecycler.setClipToPadding(false);
        searchItemRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        final int dp8 = Tool.dp2px(8, getContext());
        final int dp36 = Tool.dp2px(36, getContext());
        AppManager.getInstance(getContext()).addAppUpdatedListener(new AppManager.AppUpdatedListener() {
            @Override
            public void onAppUpdated(List<AppManager.App> apps) {
                adapter.clear();
                List<IconLabelItem> items = new ArrayList<>();
                for (int i = 0; i < apps.size(); i++) {
                    final AppManager.App app = apps.get(i);
                    items.add(new IconLabelItem(getContext(), app.icon, app.label, new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Tool.startApp(v.getContext(), app);
                        }
                    }, Color.WHITE, dp8, dp36));
                }
                adapter.set(items);
            }
        });
        adapter.withFilterPredicate(new IItemAdapter.Predicate<IconLabelItem>() {
            @Override
            public boolean filter(IconLabelItem item, CharSequence constraint) {
                String s = constraint.toString();
                if (s.isEmpty())
                    return true;
                else if (item.label.toLowerCase().contains(s.toLowerCase()))
                    return false;
                else
                    return true;
            }
        });
        final LayoutParams sirlp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        addView(searchItemRecycler, sirlp);
        addView(searchBox, sboxlp);
        addView(searchButton, sblp);


        searchBox.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                searchBox.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                sirlp.setMargins(0, Tool.dp2px(12, getContext()) + searchBox.getHeight(), 0, 0);
            }
        });
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            setPadding(0, insets.getSystemWindowInsetTop(), 0, 0);
            searchItemRecycler.setPadding(searchItemRecycler.getPaddingLeft(), searchItemRecycler.getPaddingTop(), searchItemRecycler.getPaddingRight(), searchItemRecycler.getPaddingBottom() + insets.getSystemWindowInsetBottom());
            return insets;
        }
        return super.onApplyWindowInsets(insets);
    }

    public interface CallBack {
        void onInternetSearch(String string);

        void onExpand();

        void onCollapse();
    }

}
