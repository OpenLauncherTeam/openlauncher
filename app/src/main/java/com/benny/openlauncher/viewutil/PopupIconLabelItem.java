package com.benny.openlauncher.viewutil;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.benny.openlauncher.R;
import com.mikepenz.fastadapter.items.AbstractItem;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

public final class PopupIconLabelItem extends AbstractItem<PopupIconLabelItem, PopupIconLabelItem.ViewHolder_PopupIconLabelItem> {
    private final int _iconRes;
    private final int _labelRes;

    public static final class ViewHolder_PopupIconLabelItem extends RecyclerView.ViewHolder {
        @NonNull
        private final CardView cardView;
        private ImageView iconView;
        private TextView labelView;

        public ViewHolder_PopupIconLabelItem(@NonNull View itemView) {
            super(itemView);
            this.cardView = (CardView) itemView;
            this.labelView = itemView.findViewById(R.id.item_popup_label);
            this.iconView = itemView.findViewById(R.id.item_popup_icon);
        }

        public final TextView getLabelView() {
            return this.labelView;
        }

        public final ImageView getIconView() {
            return this.iconView;
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

    public void bindView(@Nullable ViewHolder_PopupIconLabelItem holder, @Nullable List<Object> payloads) {
        super.bindView(holder, payloads);
        if (holder != null) {
            TextView labelView = holder.getLabelView();
            if (labelView != null) {
                labelView.setText(_labelRes);
            }
        }
        if (holder != null) {
            ImageView iconView = holder.getIconView();
            if (iconView != null) {
                iconView.setImageResource(_iconRes);
            }
        }
    }

    public void unbindView(@Nullable ViewHolder_PopupIconLabelItem holder) {
        super.unbindView(holder);
        if (holder != null) {
            TextView labelView = holder.getLabelView();
            if (labelView != null) {
                labelView.setText(null);
            }
        }
        if (holder != null) {
            ImageView iconView = holder.getIconView();
            if (iconView != null) {
                iconView.setImageDrawable(null);
            }
        }
    }

    @Override
    public ViewHolder_PopupIconLabelItem getViewHolder(View v) {
        return new ViewHolder_PopupIconLabelItem(v);
    }
}