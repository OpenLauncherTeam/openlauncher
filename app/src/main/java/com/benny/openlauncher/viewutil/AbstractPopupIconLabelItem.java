package com.benny.openlauncher.viewutil;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.benny.openlauncher.R;
import com.mikepenz.fastadapter.IClickable;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.items.AbstractItem;

public abstract class AbstractPopupIconLabelItem<Item extends IItem & IClickable> extends AbstractItem<Item, AbstractPopupIconLabelItem.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconView;
        TextView labelView;

        ViewHolder(View itemView) {
            super(itemView);

            labelView = itemView.findViewById(R.id.item_popup_label);
            iconView = itemView.findViewById(R.id.item_popup_icon);
        }
    }

}
