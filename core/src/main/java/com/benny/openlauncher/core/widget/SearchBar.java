package com.benny.openlauncher.core.widget;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
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
import com.benny.openlauncher.core.interfaces.FastItem;
import com.benny.openlauncher.core.manager.Setup;
import com.benny.openlauncher.core.model.IconLabelItem;
import com.benny.openlauncher.core.model.Item;
import com.benny.openlauncher.core.util.DragAction;
import com.benny.openlauncher.core.util.Tool;
import com.benny.openlauncher.core.viewutil.CircleDrawable;
import com.mikepenz.fastadapter.IItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SearchBar extends FrameLayout {

    private static final long ANIM_TIME = 200;
    public TextView searchClock;
    public AppCompatImageView switchButton;
    public AppCompatImageView searchButton;
    public AppCompatEditText searchInput;
    public RecyclerView searchRecycler;
    private CircleDrawable icon;
    private CardView searchCardContainer;
    private FastItemAdapter<FastItem.LabelItem> adapter = new FastItemAdapter<>();
    private CallBack callback;
    private boolean expanded;
    private boolean searchInternetEnabled = true;
    private Mode mode = Mode.DateAll;
    private int searchClockTextSize = 28;
    private float searchClockSubTextFactor = 0.5f;

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

    public SearchBar setSearchInternetEnabled(boolean enabled) {
        searchInternetEnabled = enabled;
        return this;
    }

    public SearchBar setSearchClockTextSize(int size) {
        searchClockTextSize = size;
        if (searchClock != null) {
            searchClock.setTextSize(TypedValue.COMPLEX_UNIT_DIP, searchClockTextSize);
        }
        return this;
    }

    public SearchBar setSearchClockSubTextFactor(float factor) {
        searchClockSubTextFactor = factor;
        return this;
    }

    public SearchBar setMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public void setCallback(CallBack callback) {
        this.callback = callback;
    }

    public boolean collapse() {
        if (!expanded) {
            return false;
        }
        searchButton.callOnClick();
        return !expanded;
    }

    private void init() {
        int dp1 = Tool.Companion.dp2px(1, getContext());
        int iconMarginOutside = dp1 * 16;
        int iconMarginTop = dp1 * 13;
        int searchTextHorizontalMargin = dp1 * 8;
        int searchTextMarginTop = dp1 * 4;
        int iconSize = dp1 * 24;
        int iconPadding = dp1 * 6; // CircleDrawable uses 6dp as well!!

        searchClock = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.view_search_clock, this, false);
        searchClock.setTextSize(TypedValue.COMPLEX_UNIT_DIP, searchClockTextSize);
        LayoutParams clockParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clockParams.setMargins(iconMarginOutside, 0, 0, 0);
        clockParams.gravity = Gravity.START;

        LayoutParams switchButtonParams = null;
        // && Setup.appSettings().isSearchGridListSwitchEnabled()

        switchButton = new AppCompatImageView(getContext());
        updateSwitchIcon();
        switchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Setup.appSettings().setSearchUseGrid(!Setup.appSettings().isSearchUseGrid());
                updateSwitchIcon();
                updateRecyclerViewLayoutManager();
            }
        });
        switchButton.setVisibility(View.GONE);
        switchButton.setPadding(0, iconPadding, 0, iconPadding);

        switchButtonParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        switchButtonParams.setMargins(iconMarginOutside / 2, 0, 0, 0);
        switchButtonParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;

        if (isInEditMode()) return;
        icon = new CircleDrawable(getContext(), getResources().getDrawable(R.drawable.ic_search_light_24dp), Color.BLACK);
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
                    expandInternal();
                } else {
                    collapseInternal();
                }
            }
        });
        LayoutParams buttonParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(0, iconMarginTop, iconMarginOutside, 0);
        buttonParams.gravity = Gravity.END;

        searchCardContainer = new CardView(getContext());
        searchCardContainer.setCardBackgroundColor(Color.TRANSPARENT);
        searchCardContainer.setVisibility(View.GONE);
        searchCardContainer.setRadius(0);
        searchCardContainer.setCardElevation(0);
        searchCardContainer.setContentPadding(dp1 * 4, dp1 * 4, dp1 * 4, dp1 * 4);

        searchInput = new AppCompatEditText(getContext());
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
        LayoutParams inputCardParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //(switchButton != null ? iconMarginOutside + iconSize : 0) + searchTextHorizontalMargin
        //iconMarginOutside + iconSize + searchTextHorizontalMargin
        inputCardParams.setMargins(0, searchTextMarginTop, 0, 0);

        LayoutParams inputParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputParams.setMargins(iconMarginOutside + iconSize, 0, 0, 0);


        searchCardContainer.addView(switchButton, switchButtonParams);
        searchCardContainer.addView(searchInput, inputParams);

        initRecyclerView();

        Setup.appLoader().addUpdateListener(new AppUpdateListener<App>() {
            @Override
            public boolean onAppUpdated(List<App> apps) {
                adapter.clear();
                if (Setup.appSettings().getSearchBarShouldShowHiddenApps()) {
                    apps = Setup.appLoader().getAllApps(getContext(), true);
                }
                List<FastItem.LabelItem> items = new ArrayList<>();
                if (searchInternetEnabled) {
                    items.add(new IconLabelItem(getContext(), R.string.search_online)
                            .withIconGravity(Gravity.START)
                            .withOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    callback.onInternetSearch(searchInput.getText().toString());
                                    searchInput.getText().clear();
                                }
                            })
                            .withTextColor(Color.WHITE)
                            .withDrawablePadding(getContext(), 8)
                            .withBold(true)
                            .withMatchParent(true)
                            .withTextGravity(Gravity.END));
                }
                for (int i = 0; i < apps.size(); i++) {
                    final App app = apps.get(i);
                    final int finalI = i;
                    items.add(new IconLabelItem(getContext(), app.getIconProvider(), app.getLabel(), 36)
                            .withIconGravity(Setup.appSettings().getSearchGridSize() > 1 && Setup.appSettings().getSearchLabelLines() == 0 ? Gravity.TOP : Gravity.START)
                            .withOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startApp(v.getContext(), app);
                                }
                            })
                            .withOnLongClickListener(AppItemView.Builder.getLongClickDragAppListener(Item.newAppItem(app), DragAction.Action.APP, new AppItemView.LongPressCallBack() {
                                @Override
                                public boolean readyForDrag(View view) {
                                    if (finalI == -1) return false;

                                    expanded = !expanded;
                                    collapseInternal();
                                    return true;
                                }

                                @Override
                                public void afterDrag(View view) {
                                }
                            }))
                            .withTextColor(Color.WHITE)
                            .withMatchParent(true)
                            .withDrawablePadding(getContext(), 8)
                            .withMaxTextLines(Setup.appSettings().getSearchLabelLines()));
                }
                adapter.set(items);

                return false;
            }
        });
        adapter.getItemFilter().withFilterPredicate(new IItemAdapter.Predicate<FastItem.LabelItem>() {
            @Override
            public boolean filter(FastItem.LabelItem item, CharSequence constraint) {
                if (item.getLabel().equals(getContext().getString(R.string.search_online)))
                    return true;
                String s = constraint.toString();
                if (s.isEmpty())
                    return false;
                else if (item.getLabel().toLowerCase().contains(s.toLowerCase()))
                    return true;
                else
                    return false;
            }
        });

        final LayoutParams recyclerParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        addView(searchClock, clockParams);
        addView(searchRecycler, recyclerParams);
        addView(searchCardContainer, inputCardParams);
        addView(searchButton, buttonParams);

        searchInput.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                searchInput.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int marginTop = Tool.Companion.dp2px(56, getContext()) + searchInput.getHeight();
                int marginBottom = Desktop.bottomInset;
                recyclerParams.setMargins(0, marginTop, 0, marginBottom);
                recyclerParams.height = ((View) getParent()).getHeight() - marginTop - marginBottom / 2;
                searchRecycler.setLayoutParams(recyclerParams);
                searchRecycler.setPadding(0, 0, 0, (int) (marginBottom * 1.5f));
            }
        });
    }

    private void collapseInternal() {
        if (callback != null) {
            callback.onCollapse();
        }
        icon.setIcon(getResources().getDrawable(R.drawable.ic_search_light_24dp));
        Tool.Companion.visibleViews(ANIM_TIME, searchClock);
        Tool.Companion.goneViews(ANIM_TIME, searchCardContainer, searchRecycler, switchButton);
        searchInput.getText().clear();
    }

    private void expandInternal() {
        if (callback != null) {
            callback.onExpand();
        }
        if (Setup.appSettings().isResetSearchBarOnOpen()) {
            RecyclerView.LayoutManager lm = searchRecycler.getLayoutManager();
            if (lm instanceof LinearLayoutManager) {
                ((LinearLayoutManager) searchRecycler.getLayoutManager()).scrollToPositionWithOffset(0, 0);
            } else if (lm instanceof GridLayoutManager) {
                ((GridLayoutManager) searchRecycler.getLayoutManager()).scrollToPositionWithOffset(0, 0);
            }
        }
        icon.setIcon(getResources().getDrawable(R.drawable.ic_clear_white_24dp));
        Tool.Companion.visibleViews(ANIM_TIME, searchCardContainer, searchRecycler, switchButton);
        Tool.Companion.goneViews(ANIM_TIME, searchClock);
    }

    private void updateSwitchIcon() {
        switchButton.setImageResource(Setup.appSettings().isSearchUseGrid() ? R.drawable.ic_apps_white_24dp : R.drawable.ic_view_list_white_24dp);
    }

    private void updateRecyclerViewLayoutManager() {
        int gridSize = Setup.appSettings().isSearchUseGrid() ? Setup.appSettings().getSearchGridSize() : 1;
        if (gridSize == 1) {
            searchRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        } else {
            searchRecycler.setLayoutManager(new GridLayoutManager(getContext(), gridSize, GridLayoutManager.VERTICAL, false));
        }
        searchRecycler.getLayoutManager().setAutoMeasureEnabled(false);
    }

    protected void initRecyclerView() {
        searchRecycler = new RecyclerView(getContext());
        searchRecycler.setItemAnimator(null);
        searchRecycler.setVisibility(View.GONE);
        searchRecycler.setAdapter(adapter);
        searchRecycler.setClipToPadding(false);
        searchRecycler.setHasFixedSize(true);
        updateRecyclerViewLayoutManager();
    }

    protected void startApp(Context context, App app) {
        Tool.Companion.startApp(context, app);
    }

    public void updateClock() {
        if (!Setup.appSettings().isSearchBarTimeEnabled()) {
            searchClock.setText("");
            return;
        }

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        SimpleDateFormat sdf = mode.sdf;
        if (sdf == null) {
            sdf = Setup.appSettings().getUserDateFormat();
        }
        String text = sdf.format(calendar.getTime());
        String[] lines = text.split("\n");
        Spannable span = new SpannableString(text);
        span.setSpan(new RelativeSizeSpan(searchClockSubTextFactor), lines[0].length() + 1, lines[0].length() + 1 + lines[1].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        searchClock.setText(span);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            int topInset = insets.getSystemWindowInsetTop();
            setPadding(getPaddingLeft(), topInset + Tool.Companion.dp2px(10, getContext()), getPaddingRight(), getPaddingBottom());
        }
        return insets;
    }

    public enum Mode {
        DateAll(1, new SimpleDateFormat("MMMM dd'\n'EEEE',' yyyy", Locale.getDefault())),
        DateNoYearAndTime(2, new SimpleDateFormat("MMMM dd'\n'HH':'mm", Locale.getDefault())),
        DateAllAndTime(3, new SimpleDateFormat("MMMM dd',' yyyy'\n'HH':'mm", Locale.getDefault())),
        TimeAndDateAll(4, new SimpleDateFormat("HH':'mm'\n'MMMM dd',' yyyy", Locale.getDefault())),
        Custom(0, null);

        SimpleDateFormat sdf;
        int id;

        Mode(int id, SimpleDateFormat sdf) {
            this.id = id;
            this.sdf = sdf;
        }

        public static Mode getById(int id) {
            for (int i = 0; i < values().length; i++) {
                if (values()[i].getId() == id)
                    return values()[i];
            }
            throw new RuntimeException("ID not found!");
        }

        public static Mode getByIndex(int index) {
            return values()[index];
        }

        public static int getIndex(int id) {
            for (int i = 0; i < values().length; i++) {
                if (values()[i].getId() == id) {
                    return i;
                }
            }
            throw new RuntimeException("ID not found!");
        }

        public int getId() {
            return id;
        }
    }

    public interface CallBack {
        void onInternetSearch(String string);

        void onExpand();

        void onCollapse();
    }
}
