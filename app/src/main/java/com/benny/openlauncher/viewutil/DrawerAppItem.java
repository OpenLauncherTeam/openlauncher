package com.benny.openlauncher.viewutil;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.benny.openlauncher.R;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.App;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.widget.AppDrawerGrid;
import com.benny.openlauncher.widget.AppItemView;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

public class DrawerAppItem extends AbstractItem<DrawerAppItem, DrawerAppItem.ViewHolder> {
    private App _app;

    public DrawerAppItem(App app) {
        _app = app;
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
    public void bindView(DrawerAppItem.ViewHolder holder, List payloads) {
        holder.builder
                .setAppItem(_app)
                .withOnLongClick(Item.newAppItem(_app), DragAction.Action.DRAWER, new AppItemView.LongPressCallBack() {
                    @Override
                    public boolean readyForDrag(View view) {
                        return true;
                    }

                    @Override
                    public void afterDrag(View view) {
                        // do nothing
                    }
                });
        super.bindView(holder, payloads);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        AppItemView appItemView;
        AppItemView.Builder builder;

        public ViewHolder(View itemView) {
            super(itemView);
            appItemView = (AppItemView) itemView;
            appItemView.setTargetedWidth(AppDrawerGrid._itemWidth);
            appItemView.setTargetedHeightPadding(AppDrawerGrid._itemHeightPadding);

            builder = new AppItemView.Builder(appItemView)
                    .setIconSize(Setup.appSettings().getIconSize())
                    .setLabelVisibility(Setup.appSettings().isDrawerShowLabel())
                    .setTextColor(Setup.appSettings().getDrawerLabelColor());
        }
    }
}
