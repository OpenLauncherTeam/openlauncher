package com.benny.openlauncher.core.widget;

import android.content.Context;
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

import com.benny.openlauncher.core.R;
import com.benny.openlauncher.core.interfaces.IconLabelItem;
import com.benny.openlauncher.core.manager.Setup;
import com.benny.openlauncher.core.util.Tool;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BennyKok on 4/25/2017.
 */

public class DesktopOptionView extends FrameLayout {
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
            actionAdapter.getAdapterItem(0).setIcon(getContext(), R.drawable.ic_star_white_36dp);
        } else {
            actionAdapter.getAdapterItem(0).setIcon(getContext(), R.drawable.ic_star_border_white_36dp);
        }
        actionAdapter.notifyAdapterItemChanged(0);
    }

    public void updateLockIcon(boolean lock) {
        if (lock) {
            actionAdapter.getAdapterItem(4).setIcon(getContext(), R.drawable.ic_lock_white_36dp);
        } else {
            actionAdapter.getAdapterItem(4).setIcon(getContext(), R.drawable.ic_lock_open_white_36dp);
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
        if (isInEditMode()) {
            return;
        }
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "RobotoCondensed-Regular.ttf");
        actionRecyclerView = new RecyclerView(getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        actionRecyclerView.setClipToPadding(false);
        actionRecyclerView.setPadding(Tool.dp2px(42, getContext()), 0, Tool.dp2px(42, getContext()), 0);
        actionRecyclerView.setLayoutManager(linearLayoutManager);
        actionRecyclerView.setAdapter(actionAdapter);
        actionRecyclerView.setOverScrollMode(OVER_SCROLL_ALWAYS);
        LayoutParams actionRecyclerViewLP = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        actionRecyclerViewLP.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        addView(actionRecyclerView, actionRecyclerViewLP);

        List<IconLabelItem> items = new ArrayList<>();
        items.add(Setup.get().createDesktopOptionsViewItem(getContext(), R.drawable.ic_star_white_36dp, R.string.home, null, typeface));
        items.add(Setup.get().createDesktopOptionsViewItem(getContext(), R.drawable.ic_clear_white_36dp, R.string.remove, null, typeface));
        items.add(Setup.get().createDesktopOptionsViewItem(getContext(), R.drawable.ic_dashboard_white_36dp, R.string.widget, null, typeface));
        items.add(Setup.get().createDesktopOptionsViewItem(getContext(), R.drawable.ic_launch_white_36dp, R.string.action, null, typeface));
        items.add(Setup.get().createDesktopOptionsViewItem(getContext(), R.drawable.ic_lock_open_white_36dp, R.string.lock, null, typeface));
        items.add(Setup.get().createDesktopOptionsViewItem(getContext(), R.drawable.ic_settings_launcher_white_36dp, R.string.settings, null, typeface));

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
                            if (!Setup.appSettings().isDesktopLock()) {
                                desktopOptionViewListener.onRemovePage();
                            } else {
                                Tool.toast(getContext(), "Desktop is locked.");
                            }
                            break;
                        case 2:
                            if (!Setup.appSettings().isDesktopLock()) {
                                desktopOptionViewListener.onPickWidget();
                            } else {
                                Tool.toast(getContext(), "Desktop is locked.");
                            }
                            break;
                        case 3:
                            if (!Setup.appSettings().isDesktopLock()) {
                                desktopOptionViewListener.onPickDesktopAction();
                            } else {
                                Tool.toast(getContext(), "Desktop is locked.");
                            }
                            break;
                        case 4:
                            Setup.appSettings().setDesktopLock(!Setup.appSettings().isDesktopLock());
                            //LauncherSettings.getInstance(getContext()).generalSettings.desktopLock = !LauncherSettings.getInstance(getContext()).generalSettings.desktopLock;
                            updateLockIcon(Setup.appSettings().isDesktopLock());
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
