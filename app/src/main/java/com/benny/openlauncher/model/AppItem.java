package com.benny.openlauncher.model;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.benny.openlauncher.R;
import com.benny.openlauncher.core.activity.Home;
import com.benny.openlauncher.core.interfaces.IApp;
import com.benny.openlauncher.core.interfaces.IAppItem;
import com.benny.openlauncher.core.interfaces.IAppItemView;
import com.benny.openlauncher.core.util.DragAction;
import com.benny.openlauncher.core.widget.AppDrawerVertical;
import com.benny.openlauncher.core.widget.Desktop;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.widget.AppItemView;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;

import java.util.List;

/**
 * Created by Michael on 25.06.2017.
 */

public class AppItem extends AbstractItem<AppItem, AppItem.ViewHolder> implements IAppItem<AppItem, AppItem.ViewHolder>
{
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

    private final ViewHolderFactory<? extends ViewHolder> FACTORY = new AppItem.ItemFactory();

    @Override
    public IApp getApp() {
        return app;
    }

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
                .withOnTouchGetPosition()
                .withOnLongClick(app, DragAction.Action.APP_DRAWER, new IAppItemView.LongPressCallBack() {
                    @Override
                    public boolean readyForDrag(View view) {
                        return AppSettings.get().getDesktopStyle() != Desktop.DesktopMode.SHOW_ALL_APPS;
                    }

                    @Override
                    public void afterDrag(View view) {
                        Home.launcher.closeAppDrawer();
                    }
                })
                .setLabelVisibility(AppSettings.get().isDrawerShowLabel())
                .setTextColor(AppSettings.get().getDrawerLabelColor());
        super.bindView(holder, payloads);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        AppItemView appItemView;

        ViewHolder(View itemView) {
            super(itemView);
            appItemView = (AppItemView) itemView;
            appItemView.setTargetedWidth(AppDrawerVertical.itemWidth);
            appItemView.setTargetedHeightPadding(AppDrawerVertical.itemHeightPadding);
        }
    }
}