package com.benny.openlauncher.model;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.benny.openlauncher.R;
import com.benny.openlauncher.util.App;
import com.benny.openlauncher.interfaces.FastItem;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.widget.AppDrawerVertical;
import com.benny.openlauncher.widget.AppItemView;
import com.benny.openlauncher.widget.Desktop;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

public class DrawerAppItem extends AbstractItem<DrawerAppItem, DrawerAppItem.ViewHolder> implements FastItem.AppItem<DrawerAppItem, DrawerAppItem.ViewHolder> {
    private App app;
    private AppItemView.LongPressCallBack onLongClickCallback;

    public DrawerAppItem(App app) {
        this.app = app;
        onLongClickCallback = new AppItemView.LongPressCallBack() {
            @Override
            public boolean readyForDrag(View view) {
                return Setup.Companion.appSettings().getDesktopStyle() != Desktop.DesktopMode.INSTANCE.getSHOW_ALL_APPS();
            }

            @Override
            public void afterDrag(View view) {
                //This will be handled by the Drag N Drop listener in the Home
                //Home.Companion.getLauncher().closeAppDrawer();
            }
        };
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
        holder.builder
                .setAppItem(app)
                .withOnLongClick(app, DragAction.Action.APP_DRAWER, onLongClickCallback)
                .withOnTouchGetPosition(null, null);
        holder.appItemView.load();
        super.bindView(holder, payloads);
    }

    @Override
    public void unbindView(DrawerAppItem.ViewHolder holder) {
        super.unbindView(holder);
        holder.appItemView.reset();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        AppItemView appItemView;
        AppItemView.Builder builder;

        ViewHolder(View itemView) {
            super(itemView);
            appItemView = (AppItemView) itemView;
            appItemView.setTargetedWidth(AppDrawerVertical.itemWidth);
            appItemView.setTargetedHeightPadding(AppDrawerVertical.itemHeightPadding);

            builder = new AppItemView.Builder(appItemView, Setup.Companion.appSettings().getDrawerIconSize())
                    .withOnTouchGetPosition(null, null)
                    .setLabelVisibility(Setup.Companion.appSettings().isDrawerShowLabel())
                    .setTextColor(Setup.Companion.appSettings().getDrawerLabelColor())
                    .setFontSize(appItemView.getContext(), Setup.Companion.appSettings().getDrawerLabelFontSize())
                    .setFastAdapterItem();
        }
    }
}
