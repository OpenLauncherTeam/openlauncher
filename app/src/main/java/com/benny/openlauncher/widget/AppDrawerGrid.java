package com.benny.openlauncher.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.benny.openlauncher.R;
import com.benny.openlauncher.interfaces.AppUpdateListener;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.viewutil.DrawerAppItem;
import com.benny.openlauncher.model.App;
import com.benny.openlauncher.util.Tool;
import com.mikepenz.fastadapter.IItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.turingtechnologies.materialscrollbar.AlphabetIndicator;
import com.turingtechnologies.materialscrollbar.DragScrollBar;
import com.turingtechnologies.materialscrollbar.INameableAdapter;

import java.util.ArrayList;
import java.util.List;

public class AppDrawerGrid extends FrameLayout {

    public static int _itemWidth;
    public static int _itemHeightPadding;

    public RecyclerView _recyclerView;
    public GridAppDrawerAdapter _gridDrawerAdapter;
    public DragScrollBar _scrollBar;

    private static List<App> _apps;
    private GridLayoutManager _layoutManager;

    public AppDrawerGrid(Context context) {
        super(context);
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View view = layoutInflater.inflate(R.layout.view_app_drawer_grid, AppDrawerGrid.this, false);
        addView(view);

        _recyclerView = findViewById(R.id.recycler_view);
        _scrollBar = findViewById(R.id.scroll_bar);
        _layoutManager = new GridLayoutManager(getContext(), Setup.appSettings().getDrawerColumnCount());

        init();
    }

    private void init() {
        if (!Setup.appSettings().isDrawerShowIndicator()) _scrollBar.setVisibility(View.GONE);
        _scrollBar.setIndicator(new AlphabetIndicator(getContext()), true);
        _scrollBar.setClipToPadding(true);
        _scrollBar.setDraggableFromAnywhere(true);
        _scrollBar.setHandleColour(Setup.appSettings().getDrawerFastScrollColor());

        _gridDrawerAdapter = new GridAppDrawerAdapter();

        if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setPortraitValue();
        } else {
            setLandscapeValue();
        }
        _recyclerView.setAdapter(_gridDrawerAdapter);
        _recyclerView.setLayoutManager(_layoutManager);
        _recyclerView.setDrawingCacheEnabled(true);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                _itemWidth = getWidth() / _layoutManager.getSpanCount();
                _itemHeightPadding = Tool.dp2px(20);
                updateAdapter(Setup.appLoader().getAllApps(getContext(), false));
                Setup.appLoader().addUpdateListener(new AppUpdateListener() {
                    @Override
                    public boolean onAppUpdated(List<App> apps) {
                        updateAdapter(apps);
                        return false;
                    }
                });
            }
        });
    }

    public void updateAdapter(List<App> apps) {
        _apps = apps;
        ArrayList<DrawerAppItem> items = new ArrayList<>();
        for (int i = 0; i < apps.size(); i++) {
            items.add(new DrawerAppItem(apps.get(i)));
        }
        _gridDrawerAdapter.set(items);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (_apps == null || _layoutManager == null) {
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
        _layoutManager.setSpanCount(Setup.appSettings().getDrawerColumnCount());
        _gridDrawerAdapter.notifyAdapterDataSetChanged();
    }

    private void setLandscapeValue() {
        _layoutManager.setSpanCount(Setup.appSettings().getDrawerRowCount());
        _gridDrawerAdapter.notifyAdapterDataSetChanged();
    }

    public static class GridAppDrawerAdapter extends FastItemAdapter<DrawerAppItem> implements INameableAdapter {
        public GridAppDrawerAdapter() {
            getItemFilter().withFilterPredicate(new IItemAdapter.Predicate<DrawerAppItem>() {
                @Override
                public boolean filter(DrawerAppItem item, CharSequence constraint) {
                    return !item.getApp().getLabel().toLowerCase().contains(constraint.toString().toLowerCase());
                }
            });
        }

        @Override
        public Character getCharacterForElement(int element) {
            if (_apps != null && element < _apps.size() && _apps.get(element) != null && _apps.get(element).getLabel().length() > 0)
                return _apps.get(element).getLabel().charAt(0);
            else return '#';
        }
    }
}
