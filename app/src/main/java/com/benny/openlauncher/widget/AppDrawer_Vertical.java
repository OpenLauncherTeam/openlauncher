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
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.Tool;
import com.mikepenz.fastadapter.IItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;
import com.turingtechnologies.materialscrollbar.AlphabetIndicator;
import com.turingtechnologies.materialscrollbar.DragScrollBar;
import com.turingtechnologies.materialscrollbar.INameableAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BennyKok on 11/6/2016.
 */

public class AppDrawer_Vertical extends CardView {

    private int itemWidth;
    private int itemHeightPadding;

    public RecyclerView recyclerView;
    public GridAppDrawerAdapter gridDrawerAdapter;
    public DragScrollBar scrollBar;

    private List<AppManager.App> apps;
    private GridLayoutManager layoutManager;
    private RelativeLayout rl;

    public AppDrawer_Vertical(Context context) {
        super(context);
        preInit();
    }

    public AppDrawer_Vertical(Context context, AttributeSet attrs) {
        super(context, attrs);
        preInit();
    }

    public AppDrawer_Vertical(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        preInit();
    }

    public void preInit() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);

                rl = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.view_griddrawerinner, AppDrawer_Vertical.this, false);
                recyclerView = (RecyclerView) rl.findViewById(R.id.vDrawerRV);
                layoutManager = new GridLayoutManager(getContext(), LauncherSettings.getInstance(getContext()).generalSettings.drawerGridX);

                itemWidth = (getWidth() - recyclerView.getPaddingRight() - recyclerView.getPaddingRight()) / layoutManager.getSpanCount();
                init();

                if (!LauncherSettings.getInstance(getContext()).generalSettings.drawerShowIndicator)
                    scrollBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (apps == null) {
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
        layoutManager.setSpanCount(LauncherSettings.getInstance(getContext()).generalSettings.drawerGridX);
        gridDrawerAdapter.notifyAdapterDataSetChanged();
    }

    private void setLandscapeValue() {
        layoutManager.setSpanCount(LauncherSettings.getInstance(getContext()).generalSettings.drawerGridX_L);
        gridDrawerAdapter.notifyAdapterDataSetChanged();
    }

    private void init() {
        itemHeightPadding = Tool.dp2px(15, getContext());

        scrollBar = (DragScrollBar) rl.findViewById(R.id.dragScrollBar);
        scrollBar.setIndicator(new AlphabetIndicator(getContext()), true);


        boolean mPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        gridDrawerAdapter = new GridAppDrawerAdapter();
        recyclerView.setAdapter(gridDrawerAdapter);

        if (mPortrait) {
            setPortraitValue();
        } else {
            setLandscapeValue();
        }
        recyclerView.setLayoutManager(layoutManager);

        if (AppManager.getInstance(getContext()).getApps().size() != 0) {
            AppDrawer_Vertical.this.apps = AppManager.getInstance(getContext()).getApps();
            ArrayList<AppItem> items = new ArrayList<>();
            for (int i = 0; i < apps.size(); i++) {
                items.add(new AppItem(apps.get(i)));
            }
            gridDrawerAdapter.set(items);
        }
        AppManager.getInstance(getContext()).addAppUpdatedListener(new AppManager.AppUpdatedListener() {
            @Override
            public void onAppUpdated(List<AppManager.App> apps) {
                AppDrawer_Vertical.this.apps = apps;
                ArrayList<AppItem> items = new ArrayList<>();
                for (int i = 0; i < apps.size(); i++) {
                    items.add(new AppItem(apps.get(i)));
                }
                gridDrawerAdapter.set(items);
            }
        });

        addView(rl);
    }

    public class GridAppDrawerAdapter extends FastItemAdapter<AppItem> implements INameableAdapter {

        public GridAppDrawerAdapter() {
            withFilterPredicate(new IItemAdapter.Predicate<AppItem>() {
                @Override
                public boolean filter(AppItem item, CharSequence constraint) {
                    return !item.app.label.toLowerCase().contains(constraint.toString().toLowerCase());
                }
            });
        }

        @Override
        public Character getCharacterForElement(int element) {
            if (apps != null)
                return apps.get(element).label.charAt(0);
            else return '#';
        }
    }

    public class AppItem extends AbstractItem<AppItem, AppItem.ViewHolder> {
        public AppManager.App app;

        public AppItem(AppManager.App app) {
            this.app = app;
        }

        @Override
        public int getType() {
            return 0;
        }

        @Override
        public int getLayoutRes() {
            return R.layout.item_app;
        }

        private final ViewHolderFactory<? extends AppItem.ViewHolder> FACTORY = new AppItem.ItemFactory();

        class ItemFactory implements ViewHolderFactory<AppItem.ViewHolder> {
            public AppItem.ViewHolder create(View v) {
                return new AppItem.ViewHolder(v);
            }
        }

        @Override
        public ViewHolderFactory<? extends AppItem.ViewHolder> getFactory() {
            return FACTORY;
        }

        @Override
        public void bindView(AppItem.ViewHolder holder, List payloads) {
            new AppItemView.Builder(holder.appItemView)
                    .setAppItem(app)
                    .withOnClickLaunchApp(app)
                    .withOnTouchGetPosition()
                    .setTextColor(LauncherSettings.getInstance(holder.appItemView.getContext()).generalSettings.drawerLabelColor)
                    .withOnLongPressDrag(app, DragAction.Action.ACTION_APP_DRAWER, new AppItemView.Builder.LongPressCallBack() {
                        @Override
                        public boolean readyForDrag(View view) {
                            return LauncherSettings.getInstance(view.getContext()).generalSettings.desktopMode != Desktop.DesktopMode.ShowAllApps;
                        }

                        @Override
                        public void afterDrag(View view) {
                            Home.launcher.closeAppDrawer();

                        }
                    });
            super.bindView(holder, payloads);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            AppItemView appItemView;

            public ViewHolder(View itemView) {
                super(itemView);
                appItemView = (AppItemView) itemView;
                appItemView.setTargetedWidth(itemWidth);
                appItemView.setTargetedHeightPadding(itemHeightPadding);
            }
        }
    }
}