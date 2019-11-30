package com.benny.openlauncher.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import com.benny.openlauncher.R;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.IconLabelItem;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class DesktopOptionView extends FrameLayout {

    private RecyclerView[] _actionRecyclerViews = new RecyclerView[2];
    private FastItemAdapter<IconLabelItem>[] _actionAdapters = new FastItemAdapter[2];
    private DesktopOptionViewListener _desktopOptionViewListener;

    public DesktopOptionView(@NonNull Context context) {
        super(context);
        init();
    }

    public DesktopOptionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DesktopOptionView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setDesktopOptionViewListener(DesktopOptionViewListener desktopOptionViewListener) {
        _desktopOptionViewListener = desktopOptionViewListener;
    }

    public void updateHomeIcon(final boolean home) {
        post(new Runnable() {
            @Override
            public void run() {
                if (home) {
                    _actionAdapters[0].getAdapterItem(1)._icon = getContext().getResources().getDrawable(R.drawable.ic_star);
                } else {
                    _actionAdapters[0].getAdapterItem(1)._icon = getContext().getResources().getDrawable(R.drawable.ic_star_border);
                }
                _actionAdapters[0].notifyAdapterItemChanged(1);
            }
        });
    }

    public void updateLockIcon(final boolean lock) {
        if (_actionAdapters.length == 0) return;
        if (_actionAdapters[0].getAdapterItemCount() == 0) return;
        post(new Runnable() {
            @Override
            public void run() {
                if (lock) {
                    _actionAdapters[0].getAdapterItem(2)._icon = getContext().getResources().getDrawable(R.drawable.ic_lock);
                } else {
                    _actionAdapters[0].getAdapterItem(2)._icon = getContext().getResources().getDrawable(R.drawable.ic_lock_open);
                }
                _actionAdapters[0].notifyAdapterItemChanged(2);
            }
        });
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            setPadding(0, insets.getSystemWindowInsetTop(), 0, insets.getSystemWindowInsetBottom());
            return insets;
        }
        return insets;
    }

    private void init() {
        if (isInEditMode()) {
            return;
        }

        final int paddingHorizontal = Tool.dp2px(42);
        final Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "RobotoCondensed-Regular.ttf");

        _actionAdapters[0] = new FastItemAdapter<>();
        _actionAdapters[1] = new FastItemAdapter<>();

        _actionRecyclerViews[0] = createRecyclerView(_actionAdapters[0], Gravity.TOP | Gravity.CENTER_HORIZONTAL, paddingHorizontal);
        _actionRecyclerViews[1] = createRecyclerView(_actionAdapters[1], Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, paddingHorizontal);

        final com.mikepenz.fastadapter.listeners.OnClickListener<IconLabelItem> clickListener = new com.mikepenz.fastadapter.listeners.OnClickListener<IconLabelItem>() {
            @Override
            public boolean onClick(View v, IAdapter<IconLabelItem> adapter, IconLabelItem item, int position) {
                if (_desktopOptionViewListener != null) {
                    final int id = (int) item.getIdentifier();
                    if (id == R.string.home) {
                        updateHomeIcon(true);
                        _desktopOptionViewListener.onSetHomePage();
                    } else if (id == R.string.remove) {
                        if (!Setup.appSettings().getDesktopLock()) {
                            _desktopOptionViewListener.onRemovePage();
                        } else {
                            Tool.toast(getContext(), "Desktop is locked.");
                        }
                    } else if (id == R.string.widget) {
                        if (!Setup.appSettings().getDesktopLock()) {
                            _desktopOptionViewListener.onPickWidget();
                        } else {
                            Tool.toast(getContext(), "Desktop is locked.");
                        }
                    } else if (id == R.string.action) {
                        if (!Setup.appSettings().getDesktopLock()) {
                            _desktopOptionViewListener.onPickAction();
                        } else {
                            Tool.toast(getContext(), "Desktop is locked.");
                        }
                    } else if (id == R.string.lock) {
                        Setup.appSettings().setDesktopLock(!Setup.appSettings().getDesktopLock());
                        updateLockIcon(Setup.appSettings().getDesktopLock());
                    } else if (id == R.string.pref_title__settings) {
                        _desktopOptionViewListener.onLaunchSettings();
                    } else {
                        return false;
                    }
                    return true;
                }
                return false;
            }
        };

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int itemWidth = (getWidth() - 2 * paddingHorizontal) / 3;
                initItems(typeface, clickListener, itemWidth);
            }
        });
    }

    private void initItems(final Typeface typeface, final com.mikepenz.fastadapter.listeners.OnClickListener<IconLabelItem> clickListener, int itemWidth) {
        List<IconLabelItem> itemsTop = new ArrayList<>();
        itemsTop.add(createItem(R.drawable.ic_delete, R.string.remove, typeface, itemWidth));
        itemsTop.add(createItem(R.drawable.ic_star, R.string.home, typeface, itemWidth));
        itemsTop.add(createItem(R.drawable.ic_lock, R.string.lock, typeface, itemWidth));
        _actionAdapters[0].set(itemsTop);
        _actionAdapters[0].withOnClickListener(clickListener);

        List<IconLabelItem> itemsBottom = new ArrayList<>();
        itemsBottom.add(createItem(R.drawable.ic_dashboard, R.string.widget, typeface, itemWidth));
        itemsBottom.add(createItem(R.drawable.ic_launch, R.string.action, typeface, itemWidth));
        itemsBottom.add(createItem(R.drawable.ic_settings, R.string.pref_title__settings, typeface, itemWidth));
        _actionAdapters[1].set(itemsBottom);
        _actionAdapters[1].withOnClickListener(clickListener);

        ((MarginLayoutParams) ((View) _actionRecyclerViews[0].getParent()).getLayoutParams()).topMargin = Tool.dp2px(Setup.appSettings().getSearchBarEnable() ? 36 : 4);
    }

    private RecyclerView createRecyclerView(FastAdapter adapter, int gravity, int paddingHorizontal) {
        RecyclerView actionRecyclerView = new RecyclerView(getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        actionRecyclerView.setClipToPadding(false);
        actionRecyclerView.setPadding(paddingHorizontal, 0, paddingHorizontal, 0);
        actionRecyclerView.setLayoutManager(linearLayoutManager);
        actionRecyclerView.setAdapter(adapter);
        actionRecyclerView.setOverScrollMode(OVER_SCROLL_ALWAYS);
        LayoutParams actionRecyclerViewLP = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        actionRecyclerViewLP.gravity = gravity;

        addView(actionRecyclerView, actionRecyclerViewLP);
        return actionRecyclerView;
    }

    private IconLabelItem createItem(int icon, int label, Typeface typeface, int width) {
        return new IconLabelItem(getContext(), icon, label)
                .withIdentifier(label)
                .withOnClickListener(null)
                .withTextColor(Color.WHITE)
                .withIconSize(36)
                .withIconColor(Color.WHITE)
                .withIconPadding(4)
                .withIconGravity(Gravity.TOP)
                .withWidth(width)
                .withTextGravity(Gravity.CENTER);
    }

    public interface DesktopOptionViewListener {
        void onRemovePage();

        void onSetHomePage();

        void onPickWidget();

        void onPickAction();

        void onLaunchSettings();
    }
}
