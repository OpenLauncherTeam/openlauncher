package com.benny.openlauncher.viewutil;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.benny.openlauncher.R;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

public final class PopupIconLabelItem extends AbstractItem<PopupIconLabelItem, PopupIconLabelItem.ViewHolder> {
    private final int _iconRes;
    private final int _labelRes;

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconView;
        TextView labelView;

        ViewHolder(View itemView) {
            super(itemView);

            labelView = itemView.findViewById(R.id.item_popup_label);
            iconView = itemView.findViewById(R.id.item_popup_icon);
        }
    }

    public PopupIconLabelItem(int labelRes, int iconRes) {
        _labelRes = labelRes;
        _iconRes = iconRes;
    }

    public int getType() {
        return R.id.id_adapter_popup_icon_label_item;
    }

    public int getLayoutRes() {
        return R.layout.item_popup_icon_label;
    }

    public void bindView(@NonNull ViewHolder holder, @NonNull List<Object> payloads) {
        super.bindView(holder, payloads);

        TextView labelView = holder.labelView;
        if (labelView != null) {
            labelView.setText(_labelRes);
        }

        ImageView iconView = holder.iconView;
        iconView.setImageResource(_iconRes);
    }

    public void unbindView(@NonNull ViewHolder holder) {
        super.unbindView(holder);

        TextView labelView = holder.labelView;
        labelView.setText(null);

        ImageView iconView = holder.iconView;
        iconView.setImageDrawable(null);
    }

    @Override
    public ViewHolder getViewHolder(@NonNull View view) {
        return new ViewHolder(view);
    }
}