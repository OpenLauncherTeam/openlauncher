package com.benny.openlauncher.widget;

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

import com.benny.openlauncher.R;
import com.benny.openlauncher.interfaces.AppUpdateListener;
import com.benny.openlauncher.interfaces.FastItem;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.DrawerAppItem;
import com.benny.openlauncher.model.App;
import com.benny.openlauncher.util.Tool;
import com.mikepenz.fastadapter.IItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.turingtechnologies.materialscrollbar.AlphabetIndicator;
import com.turingtechnologies.materialscrollbar.DragScrollBar;
import com.turingtechnologies.materialscrollbar.INameableAdapter;

import java.util.ArrayList;
import java.util.List;

public class AppDrawerVertical extends CardView {

    public static int _itemWidth;
    public static int _itemHeightPadding;

    public RecyclerView _recyclerView;
    public GridAppDrawerAdapter _gridDrawerAdapter;
    public DragScrollBar _scrollBar;

    private static List<App> _apps;
    private GridLayoutManager _layoutManager;
    private RelativeLayout _rl;

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

                _rl = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.view_app_drawer_vertical_inner, AppDrawerVertical.this, false);
                _recyclerView = _rl.findViewById(R.id.vDrawerRV);
                _layoutManager = new GridLayoutManager(getContext(), Setup.appSettings().getDrawerColumnCount());

                _itemWidth = (getWidth() - _recyclerView.getPaddingRight() - _recyclerView.getPaddingRight()) / _layoutManager.getSpanCount();
                init();

                if (!Setup.appSettings().isDrawerShowIndicator())
                    _scrollBar.setVisibility(View.GONE);
            }
        });
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

    private void init() {
        _itemHeightPadding = Tool.dp2px(15, getContext());

        _scrollBar = _rl.findViewById(R.id.dragScrollBar);
        _scrollBar.setIndicator(new AlphabetIndicator(getContext()), true);
        _scrollBar.setClipToPadding(true);
        _scrollBar.setDraggableFromAnywhere(true);
        _scrollBar.post(new Runnable() {
            @Override
            public void run() {
                _scrollBar.setHandleColour(Setup.appSettings().getDrawerFastScrollColor());
            }
        });

        boolean mPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        _gridDrawerAdapter = new GridAppDrawerAdapter();
        _recyclerView.setAdapter(_gridDrawerAdapter);

        if (mPortrait) {
            setPortraitValue();
        } else {
            setLandscapeValue();
        }
        _recyclerView.setLayoutManager(_layoutManager);
        _recyclerView.setDrawingCacheEnabled(true);

        List<App> allApps = Setup.appLoader().getAllApps(getContext(), false);
        if (allApps.size() != 0) {
            _apps = allApps;
            ArrayList<FastItem.AppItem> items = new ArrayList<>();
            for (int i = 0; i < _apps.size(); i++) {
                items.add(new DrawerAppItem(_apps.get(i)));
            }
            _gridDrawerAdapter.set(items);
        }
        Setup.appLoader().addUpdateListener(new AppUpdateListener() {
            @Override
            public boolean onAppUpdated(List<App> apps) {
                AppDrawerVertical._apps = apps;
                ArrayList<FastItem.AppItem> items = new ArrayList<>();
                for (int i = 0; i < apps.size(); i++) {
                    items.add(new DrawerAppItem(apps.get(i)));
                }
                _gridDrawerAdapter.set(items);

                return false;
            }
        });

        addView(_rl);
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
            if (_apps != null && element < _apps.size() && _apps.get(element) != null && _apps.get(element).getLabel().length() > 0)
                return _apps.get(element).getLabel().charAt(0);
            else return '#';
        }
    }
}