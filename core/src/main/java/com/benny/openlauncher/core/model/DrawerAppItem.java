package com.benny.openlauncher.core.model;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.benny.openlauncher.core.R;
import com.benny.openlauncher.core.activity.Home;
import com.benny.openlauncher.core.interfaces.App;
import com.benny.openlauncher.core.interfaces.FastItem;
import com.benny.openlauncher.core.manager.Setup;
import com.benny.openlauncher.core.util.DragAction;
import com.benny.openlauncher.core.widget.AppDrawerVertical;
import com.benny.openlauncher.core.widget.AppItemView;
import com.benny.openlauncher.core.widget.Desktop;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

public class DrawerAppItem extends AbstractItem<DrawerAppItem, DrawerAppItem.ViewHolder> implements FastItem.AppItem<DrawerAppItem, DrawerAppItem.ViewHolder> {
    private App app;

    public DrawerAppItem(App app) {
        this.app = app;
    }

    @Override
    public int getType() {
        return R.id.id_adapter_drawer_app_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_app;
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public App getApp() {
        return app;
    }

    @Override
    public void bindView(DrawerAppItem.ViewHolder holder, List payloads) {
        new AppItemView.Builder(holder.appItemView)
                .setAppItem(app)
                .withOnTouchGetPosition()
                .withOnLongClick(app, DragAction.Action.APP_DRAWER, new AppItemView.LongPressCallBack() {
                    @Override
                    public boolean readyForDrag(View view) {
                        return Setup.appSettings().getDesktopStyle() != Desktop.DesktopMode.SHOW_ALL_APPS;
                    }

                    @Override
                    public void afterDrag(View view) {
                        Home.launcher.closeAppDrawer();
                    }
                })
                .setLabelVisibility(Setup.appSettings().isDrawerShowLabel())
                .setTextColor(Setup.appSettings().getDrawerLabelColor());
        super.bindView(holder, payloads);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        AppItemView appItemView;

        ViewHolder(View itemView) {
            super(itemView);
            appItemView = (AppItemView) itemView;
            appItemView.setTargetedWidth(AppDrawerVertical.itemWidth);
            appItemView.setTargetedHeightPadding(AppDrawerVertical.itemHeightPadding);
        }
    }
}
