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
import android.view.WindowInsets;
import android.widget.FrameLayout;

import com.benny.openlauncher.R;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.IconLabelItem;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BennyKok on 4/25/2017.
 */

public class DesktopOptionView extends FrameLayout {
    private AppSettings appSettings;
    private RecyclerView actionRecyclerView;

    private FastItemAdapter<IconLabelItem> actionAdapter = new FastItemAdapter<>();
    private DesktopOptionViewListener desktopOptionViewListener;

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
        this.desktopOptionViewListener = desktopOptionViewListener;
    }

    public void updateHomeIcon(boolean home) {
        if (home) {
            actionAdapter.getAdapterItem(0).setIcon(getContext().getResources().getDrawable(R.drawable.ic_star_black_36dp));
        } else {
            actionAdapter.getAdapterItem(0).setIcon(getContext().getResources().getDrawable(R.drawable.ic_star_border_black_36dp));
        }
        actionAdapter.notifyAdapterItemChanged(0);
    }

    public void updateLockIcon(boolean lock) {
        if (lock) {
            actionAdapter.getAdapterItem(4).setIcon(getContext().getResources().getDrawable(R.drawable.ic_lock_white_36dp));
        } else {
            actionAdapter.getAdapterItem(4).setIcon(getContext().getResources().getDrawable(R.drawable.ic_lock_open_white_36dp));
        }
        actionAdapter.notifyAdapterItemChanged(4);
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
        appSettings = AppSettings.get();
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "RobotoCondensed-Regular.ttf");
        actionRecyclerView = new RecyclerView(getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        actionRecyclerView.setClipToPadding(false);
        actionRecyclerView.setPadding(Tool.dp2px(20, getContext()), 0, Tool.dp2px(20, getContext()), 0);
        actionRecyclerView.setLayoutManager(linearLayoutManager);
        actionRecyclerView.setAdapter(actionAdapter);
        actionRecyclerView.setOverScrollMode(OVER_SCROLL_ALWAYS);
        LayoutParams actionRecyclerViewLP = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        actionRecyclerViewLP.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        addView(actionRecyclerView, actionRecyclerViewLP);

        List<IconLabelItem> items = new ArrayList<>();
        items.add(new IconLabelItem(getContext(), R.drawable.ic_star_black_36dp, R.string.home, null, Gravity.TOP, Color.WHITE, Gravity.CENTER, 0, typeface, false));
        items.add(new IconLabelItem(getContext(), R.drawable.ic_clear_black_36dp, R.string.remove, null, Gravity.TOP, Color.WHITE, Gravity.CENTER, 0, typeface, false));
        items.add(new IconLabelItem(getContext(), R.drawable.ic_dashboard_black_36dp, R.string.widget, null, Gravity.TOP, Color.WHITE, Gravity.CENTER, 0, typeface, false));
        items.add(new IconLabelItem(getContext(), R.drawable.ic_launch_black_36dp, R.string.action, null, Gravity.TOP, Color.WHITE, Gravity.CENTER, 0, typeface, false));
        items.add(new IconLabelItem(getContext(), R.drawable.ic_lock_open_white_36dp, R.string.lock, null, Gravity.TOP, Color.WHITE, Gravity.CENTER, 0, typeface, false));
        items.add(new IconLabelItem(getContext(), R.drawable.ic_settings_launcher_36dp, R.string.settings, null, Gravity.TOP, Color.WHITE, Gravity.CENTER, 0, typeface, false));

        actionAdapter.set(items);
        actionAdapter.withOnClickListener(new FastAdapter.OnClickListener<IconLabelItem>() {
            @Override
            public boolean onClick(View v, IAdapter<IconLabelItem> adapter, IconLabelItem item, int position) {
                if (desktopOptionViewListener != null) {
                    switch (position) {
                        case 0:
                            updateHomeIcon(true);
                            desktopOptionViewListener.onSetPageAsHome();
                            break;
                        case 1:
                            if (!appSettings.isDesktopLocked()) {
                                desktopOptionViewListener.onRemovePage();
                            } else {
                                Tool.toast(getContext(), "Desktop is locked.");
                            }
                            break;
                        case 2:
                            if (!appSettings.isDesktopLocked()) {
                                desktopOptionViewListener.onPickWidget();
                            } else {
                                Tool.toast(getContext(), "Desktop is locked.");
                            }
                            break;
                        case 3:
                            if (!appSettings.isDesktopLocked()) {
                                desktopOptionViewListener.onPickDesktopAction();
                            } else {
                                Tool.toast(getContext(), "Desktop is locked.");
                            }
                            break;
                        case 4:
                            //LauncherSettings.getInstance(getContext()).generalSettings.desktopLock = !LauncherSettings.getInstance(getContext()).generalSettings.desktopLock;
                            updateLockIcon(appSettings.isDesktopLocked());
                            break;
                        case 5:
                            desktopOptionViewListener.onLaunchSettings();
                            break;
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public interface DesktopOptionViewListener {
        void onRemovePage();

        void onSetPageAsHome();

        void onLaunchSettings();

        void onPickDesktopAction();

        void onPickWidget();
    }
}
