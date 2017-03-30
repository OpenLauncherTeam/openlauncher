package com.benny.openlauncher.viewutil;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.benny.openlauncher.R;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;

import java.util.List;

/**
 * Created by BennyKok on 10/25/2016
 */

public class IconLabelItem extends AbstractItem<IconLabelItem, IconLabelItem.ViewHolder> {
    Drawable icon;
    String label;
    View.OnClickListener listener;

    public IconLabelItem(Drawable icon, String label, View.OnClickListener listener) {
        this.label = label;
        this.icon = icon;
        this.listener = listener;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_iconlabel;
    }

    private static final ViewHolderFactory<? extends IconLabelItem.ViewHolder> FACTORY = new ItemFactory();

    static class ItemFactory implements ViewHolderFactory<IconLabelItem.ViewHolder> {
        public IconLabelItem.ViewHolder create(View v) {
            return new IconLabelItem.ViewHolder(v);
        }
    }

    @Override
    public ViewHolderFactory<? extends IconLabelItem.ViewHolder> getFactory() {
        return FACTORY;
    }

    @Override
    public void bindView(IconLabelItem.ViewHolder holder, List payloads) {
        holder.ib.setText(label);
        holder.ib.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        holder.itemView.setOnClickListener(listener);
        super.bindView(holder, payloads);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ib;

        public ViewHolder(View itemView) {
            super(itemView);
            ib = (TextView) itemView;
        }
    }
}
