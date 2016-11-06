package com.benny.openlauncher.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.Tool;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
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

public class GridAppDrawer extends CardView{

    public RecyclerView rv;
    MyAdapter fa;

    List<AppManager.App> apps;
    private GridLayoutManager layoutManager;

    public GridAppDrawer(Context context) {
        super(context);

        init();
    }

    public GridAppDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public GridAppDrawer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
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

    private void setPortraitValue(){
        layoutManager.setSpanCount(LauncherSettings.getInstance(getContext()).generalSettings.drawerGridx);
        fa.notifyAdapterDataSetChanged();
    }

    private void setLandscapeValue() {
        layoutManager.setSpanCount(LauncherSettings.getInstance(getContext()).generalSettings.drawerGridxL);
        fa.notifyAdapterDataSetChanged();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        itemWidth = (getWidth()-rv.getPaddingRight()-rv.getPaddingRight()) / layoutManager.getSpanCount();
        itemHeightPadding = Tool.convertDpToPixel(12,getContext());
        super.onLayout(changed, left, top, right, bottom);
    }

    public static int itemWidth;
    public static int itemHeightPadding;

    private void init(){
        RelativeLayout rl = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.view_griddrawerinner,this,false);
        DragScrollBar bar = (DragScrollBar) rl.findViewById(R.id.dragScrollBar);
        bar.setIndicator(new AlphabetIndicator(getContext()),true);
        rv = (RecyclerView) rl.findViewById(R.id.vDrawerRV);

        boolean mPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        fa = new MyAdapter();
        rv.setAdapter(fa);

        layoutManager = new GridLayoutManager(getContext(), LauncherSettings.getInstance(getContext()).generalSettings.drawerGridx);
        if (mPortrait) {
            setPortraitValue();
        } else {
            setLandscapeValue();
        }
        rv.setLayoutManager(layoutManager);

        if (AppManager.getInstance(getContext()).getApps().size() != 0){
            GridAppDrawer.this.apps = AppManager.getInstance(getContext()).getApps();
            ArrayList<AppItem> items = new ArrayList<>();
            for (int i = 0; i < apps.size(); i++) {
                items.add(new AppItem(apps.get(i)));
            }
            fa.set(items);
        }
        AppManager.getInstance(getContext()).addAppUpdatedListener(new AppManager.AppUpdatedListener() {
            @Override
            public void onAppUpdated(List<AppManager.App> apps) {
                GridAppDrawer.this.apps = apps;
                ArrayList<AppItem> items = new ArrayList<>();
                for (int i = 0; i < apps.size(); i++) {
                    items.add(new AppItem(apps.get(i)));
                }
                fa.set(items);
            }
        });

        addView(rl);
    }

    private class MyAdapter extends FastItemAdapter<AppItem> implements INameableAdapter{

        @Override
        public Character getCharacterForElement(int element) {
            if (apps != null)
            return apps.get(element).appName.charAt(0);
            else return '#';
        }

    }

    public static class AppItem extends AbstractItem<AppItem,AppItem.ViewHolder> {
        AppManager.App app;

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

        private static final ViewHolderFactory<? extends AppItem.ViewHolder> FACTORY = new AppItem.ItemFactory();

        static class ItemFactory implements ViewHolderFactory<AppItem.ViewHolder> {
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
            new AppItemView.Builder(holder.appItemView).setAppItem(app).withOnClickLaunchApp(app).withOnTouchGetPosition().withOnLongClickDrag(app, DragAction.Action.ACTION_APP_DRAWER, new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Home.launcher.closeAppDrawer();
                    return false;
                }
            });
            super.bindView(holder, payloads);
        }

        public static class ViewHolder extends RecyclerView.ViewHolder{
            AppItemView appItemView;

            public  ViewHolder(View itemView) {
                super(itemView);
                appItemView = (AppItemView) itemView;
                appItemView.setTargetedWidth(itemWidth);
                appItemView.setTargetedHeightPadding(itemHeightPadding);
            }
        }
    }

}
