package com.benny.openlauncher.core.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.benny.openlauncher.core.R;
import com.benny.openlauncher.core.interfaces.AbstractApp;
import com.benny.openlauncher.core.interfaces.AppUpdateListener;
import com.benny.openlauncher.core.interfaces.FastItem;
import com.benny.openlauncher.core.manager.Setup;
import com.benny.openlauncher.core.model.DrawerAppItem;
import com.benny.openlauncher.core.util.Tool;
import com.mikepenz.fastadapter.IItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.turingtechnologies.materialscrollbar.AlphabetIndicator;
import com.turingtechnologies.materialscrollbar.DragScrollBar;
import com.turingtechnologies.materialscrollbar.INameableAdapter;

import java.util.ArrayList;
import java.util.List;

public class AppDrawerVertical extends CardView {

    public static int itemWidth;
    public static int itemHeightPadding;

    public RecyclerView recyclerView;
    public GridAppDrawerAdapter gridDrawerAdapter;
    public DragScrollBar scrollBar;

    private static List<AbstractApp> apps;
    private GridLayoutManager layoutManager;
    private RelativeLayout rl;

    public AppDrawerVertical(Context context) {
        super(context);
        preInit();
    }

    public AppDrawerVertical(Context context, AttributeSet attrs) {
        super(context, attrs);
        preInit();
    }

    public AppDrawerVertical(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        preInit();
    }

    public void preInit() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);

                rl = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.view_app_drawer_vertical_inner, AppDrawerVertical.this, false);
                recyclerView = rl.findViewById(R.id.vDrawerRV);
                layoutManager = new GridLayoutManager(getContext(), Setup.Companion.appSettings().getDrawerColumnCount());

                itemWidth = (getWidth() - recyclerView.getPaddingRight() - recyclerView.getPaddingRight()) / layoutManager.getSpanCount();
                init();

                if (!Setup.Companion.appSettings().isDrawerShowIndicator())
                    scrollBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (apps == null || layoutManager == null) {
            super.onConfigurationChanged(newConfig);
            return;
        }

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setLandscapeValue();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setPortraitValue();
        }
        super.onConfigurationChanged(newConfig);
    }

    private void setPortraitValue() {
        layoutManager.setSpanCount(Setup.Companion.appSettings().getDrawerColumnCount());
        gridDrawerAdapter.notifyAdapterDataSetChanged();
    }

    private void setLandscapeValue() {
        layoutManager.setSpanCount(Setup.Companion.appSettings().getDrawerRowCount());
        gridDrawerAdapter.notifyAdapterDataSetChanged();
    }

    private void init() {
        itemHeightPadding = Tool.INSTANCE.dp2px(15, getContext());

        scrollBar = rl.findViewById(R.id.dragScrollBar);
        scrollBar.setIndicator(new AlphabetIndicator(getContext()), true);
        scrollBar.setClipToPadding(true);
        scrollBar.setDraggableFromAnywhere(true);
        scrollBar.post(new Runnable() {
            @Override
            public void run() {
                scrollBar.setHandleColour(Setup.Companion.appSettings().getDrawerFastScrollColor());
            }
        });

        boolean mPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        gridDrawerAdapter = new GridAppDrawerAdapter();
        recyclerView.setAdapter(gridDrawerAdapter);

        if (mPortrait) {
            setPortraitValue();
        } else {
            setLandscapeValue();
        }
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setDrawingCacheEnabled(true);

        List<AbstractApp> allApps = Setup.Companion.appLoader().getAllApps(getContext(), false);
        if (allApps.size() != 0) {
            AppDrawerVertical.this.apps = allApps;
            ArrayList<FastItem.AppItem> items = new ArrayList<>();
            for (int i = 0; i < apps.size(); i++) {
                items.add(new DrawerAppItem(apps.get(i)));
            }
            gridDrawerAdapter.set(items);
        }
        Setup.Companion.appLoader().addUpdateListener(new AppUpdateListener<AbstractApp>() {
            @Override
            public boolean onAppUpdated(List<AbstractApp> apps) {
                AppDrawerVertical.this.apps = apps;
                ArrayList<FastItem.AppItem> items = new ArrayList<>();
                for (int i = 0; i < apps.size(); i++) {
                    items.add(new DrawerAppItem(apps.get(i)));
                }
                gridDrawerAdapter.set(items);

                return false;
            }
        });

        addView(rl);
    }

    public static class GridAppDrawerAdapter extends FastItemAdapter<FastItem.AppItem> implements INameableAdapter {

        GridAppDrawerAdapter() {
            getItemFilter().withFilterPredicate(new IItemAdapter.Predicate<FastItem.AppItem>() {
                @Override
                public boolean filter(FastItem.AppItem item, CharSequence constraint) {
                    return !item.getApp().getLabel().toLowerCase().contains(constraint.toString().toLowerCase());
                }
            });
        }

        @Override
        public Character getCharacterForElement(int element) {
            if (apps != null && element < apps.size() && apps.get(element) != null && apps.get(element).getLabel().length() > 0)
                return apps.get(element).getLabel().charAt(0);
            else return '#';
        }
    }
}