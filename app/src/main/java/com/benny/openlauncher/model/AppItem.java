package com.benny.openlauncher.model;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.benny.openlauncher.R;
import com.benny.openlauncher.core.activity.Home;
import com.benny.openlauncher.core.interfaces.App;
import com.benny.openlauncher.core.interfaces.AppItemView;
import com.benny.openlauncher.core.util.DragAction;
import com.benny.openlauncher.core.widget.AppDrawerVertical;
import com.benny.openlauncher.core.widget.Desktop;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.AppSettings;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

public class AppItem extends AbstractItem<AppItem, AppItem.ViewHolder> implements com.benny.openlauncher.core.interfaces.AppItem<AppItem, AppItem.ViewHolder> {
    private AppManager.App app;

    public AppItem(AppManager.App app) {
        this.app = app;
    }

    @Override
    public int getType() {
        return R.id.id_adapter_app_item;
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
    public void bindView(AppItem.ViewHolder holder, List payloads) {
        new com.benny.openlauncher.widget.AppItemView.Builder(holder.appItemView)
                .setAppItem(app)
                .withOnTouchGetPosition()
                .withOnLongClick(app, DragAction.Action.APP_DRAWER, new AppItemView.LongPressCallBack() {
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        com.benny.openlauncher.widget.AppItemView appItemView;

        ViewHolder(View itemView) {
            super(itemView);
            appItemView = (com.benny.openlauncher.widget.AppItemView) itemView;
            appItemView.setTargetedWidth(AppDrawerVertical.itemWidth);
            appItemView.setTargetedHeightPadding(AppDrawerVertical.itemHeightPadding);
        }
    }
}