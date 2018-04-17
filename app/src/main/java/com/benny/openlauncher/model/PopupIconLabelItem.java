package com.benny.openlauncher.model;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.benny.openlauncher.R;
import com.mikepenz.fastadapter.items.AbstractItem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import kotlin.jvm.internal.Intrinsics;

public final class PopupIconLabelItem extends AbstractItem<PopupIconLabelItem, PopupIconLabelItem.ViewHolder_PopupIconLabelItem> {
    private final int iconRes;
    private final int labelRes;

    public static final class ViewHolder_PopupIconLabelItem extends RecyclerView.ViewHolder {
        @NotNull
        private final CardView cardView;
        private ImageView iconView;
        private TextView labelView;

        public ViewHolder_PopupIconLabelItem(@NotNull View itemView) {
            super(itemView);
            this.cardView = (CardView) itemView;
            this.labelView = (TextView) itemView.findViewById(R.id.item_popup_label);
            this.iconView = (ImageView) itemView.findViewById(R.id.item_popup_icon);
        }

        @NotNull
        public final CardView getCardView() {
            return this.cardView;
        }

        public final TextView getLabelView() {
            return this.labelView;
        }

        public final void setLabelView(TextView labelView) {
            this.labelView = labelView;
        }

        public final ImageView getIconView() {
            return this.iconView;
        }

        public final void setIconView(ImageView iconView) {
            this.iconView = iconView;
        }
    }

    public PopupIconLabelItem(int labelRes, int iconRes) {
        this.labelRes = labelRes;
        this.iconRes = iconRes;
    }

    public final int getIconRes() {
        return this.iconRes;
    }

    public final int getLabelRes() {
        return this.labelRes;
    }

    @NotNull
    public ViewHolder_PopupIconLabelItem getViewHolder_PopupIconLabelItem(@Nullable View v) {
        if (v == null) {
            Intrinsics.throwNpe();
        }
        return new ViewHolder_PopupIconLabelItem(v);
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
                labelView.setText(this.labelRes);
            }
        }
        if (holder != null) {
            ImageView iconView = holder.getIconView();
            if (iconView != null) {
                iconView.setImageResource(this.iconRes);
            }
        }
    }

    public void unbindView(@Nullable ViewHolder_PopupIconLabelItem holder) {
        super.unbindView(holder);
        if (holder != null) {
            TextView labelView = holder.getLabelView();
            if (labelView != null) {
                labelView.setText((CharSequence) null);
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