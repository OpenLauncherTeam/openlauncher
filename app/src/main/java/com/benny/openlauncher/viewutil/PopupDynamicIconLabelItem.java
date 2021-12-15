package com.benny.openlauncher.viewutil;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.benny.openlauncher.R;

import java.util.List;

public final class PopupDynamicIconLabelItem extends AbstractPopupIconLabelItem<PopupDynamicIconLabelItem> {
    private final Drawable _icon;
    private final CharSequence _label;

    public PopupDynamicIconLabelItem(CharSequence label, Drawable icon) {
        _label = label;
        _icon = icon;
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
            labelView.setText(_label);
        }

        ImageView iconView = holder.iconView;
        iconView.setImageDrawable(_icon);
    }

    public void unbindView(@NonNull ViewHolder holder) {
        super.unbindView(holder);

        TextView labelView = holder.labelView;
        labelView.setText(null);

        ImageView iconView = holder.iconView;
        iconView.setImageDrawable(null);
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(@NonNull View view) {
        return new ViewHolder(view);
    }
}