package com.benny.openlauncher.viewutil;

import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.benny.openlauncher.R;

import java.util.List;

public final class PopupIconLabelItem extends AbstractPopupIconLabelItem<PopupIconLabelItem> {
    private final int _iconRes;
    private final int _labelRes;

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