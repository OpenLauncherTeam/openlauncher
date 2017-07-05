package com.benny.openlauncher.core.model;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.benny.openlauncher.core.R;
import com.benny.openlauncher.core.interfaces.FastItem;
import com.benny.openlauncher.core.interfaces.LabelProvider;
import com.mikepenz.fastadapter.items.AbstractItem;

public abstract class BaseIconLabelItem<I extends LabelProvider, IconLabelItem extends BaseIconLabelItem, VH extends RecyclerView.ViewHolder> extends AbstractItem<IconLabelItem, VH> implements FastItem.LabelItem<IconLabelItem, VH>, FastItem.DesktopOptionsItem<IconLabelItem, VH> {

    // Data
    protected I item = null;
    protected int icon = -1;
    protected String label = null;

    // Others
    protected View.OnClickListener listener;

    public BaseIconLabelItem(I item) {
        this.item = item;
    }

    public BaseIconLabelItem(Context context, int icon, int label) {
        this.icon = icon;
        this.label = context.getString(label);
    }

    public IconLabelItem withOnClickListener(@Nullable View.OnClickListener listener) {
        this.listener = listener;
        return (IconLabelItem)this;
    }

    @Override
    public void setIcon(int resId) {
        this.icon = resId;
    }

    @Override
    public String getLabel() {
        if (item != null) {
            return item.getLabel();
        } else {
            return label;
        }
    }

    @Override
    public int getType() {
        return R.id.id_adapter_icon_label_item;
    }
}
