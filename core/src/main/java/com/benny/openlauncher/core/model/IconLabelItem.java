package com.benny.openlauncher.core.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.benny.openlauncher.core.R;
import com.benny.openlauncher.core.interfaces.FastItem;
import com.benny.openlauncher.core.interfaces.IconProvider;
import com.benny.openlauncher.core.manager.Setup;
import com.benny.openlauncher.core.util.BaseIconProvider;
import com.benny.openlauncher.core.util.Tool;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

public class IconLabelItem extends AbstractItem<IconLabelItem, IconLabelItem.ViewHolder> implements FastItem.LabelItem<IconLabelItem, IconLabelItem.ViewHolder>, FastItem.DesktopOptionsItem<IconLabelItem, IconLabelItem.ViewHolder> {

    // Data
    protected BaseIconProvider iconProvider = null;
    protected String label = null;

    // Others
    protected View.OnClickListener listener;

    private int forceSize = -1;
    private int iconGravity;
    private int textColor = Color.DKGRAY;
    private int gravity = android.view.Gravity.CENTER_VERTICAL;
    private float drawablePadding;
    private Typeface typeface;
    private boolean matchParent = true;
    private int width = -1;
    private boolean bold = false;
    private int textGravity = Gravity.CENTER_VERTICAL;
    private int maxTextLines = Integer.MAX_VALUE;

    public IconLabelItem(Item item) {
        this.iconProvider = item != null ? item.getIconProvider() : null;
        this.label = item != null ? item.getLabel() : null;
    }

    public IconLabelItem(Context context, int icon, int label) {
        this.iconProvider = Setup.imageLoader().createIconProvider(icon);
        this.label = context.getString(label);
    }

    public IconLabelItem(Context context, int label) {
        this(context, 0, label);
    }

    public IconLabelItem(Context context, int icon, String label, int forceSize) {
        this(null);
        this.label = label;
        this.iconProvider = Setup.imageLoader().createIconProvider(icon);
        this.forceSize = forceSize;
    }

    public IconLabelItem(Context context, int icon, int label, int forceSize) {
        this(null);
        this.label = context.getString(label);
        this.iconProvider = Setup.imageLoader().createIconProvider(icon);
        this.forceSize = forceSize;
    }

    public IconLabelItem(Context context, BaseIconProvider iconProvider, String label, int forceSize) {
        this(null);
        this.label = label;
        this.iconProvider = iconProvider;
        this.forceSize = forceSize;
    }

    public IconLabelItem(Context context, Drawable icon, String label, int forceSize) {
        this(null);
        this.label = label;
        this.iconProvider = Setup.imageLoader().createIconProvider(icon);
        this.forceSize = forceSize;
    }

    public IconLabelItem(Context context, Drawable icon, int label, int forceSize) {
        this(null);
        this.label = context.getString(label);
        this.iconProvider = Setup.imageLoader().createIconProvider(icon);
        this.forceSize = forceSize;
    }

    public IconLabelItem withIconGravity(int iconGravity) {
        this.iconGravity = iconGravity;
        return this;
    }

    public IconLabelItem withDrawablePadding(Context context, int drawablePadding) {
        this.drawablePadding = Tool.dp2px(drawablePadding, context);
        return this;
    }

    public IconLabelItem withTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    public IconLabelItem withBold(boolean bold) {
        this.bold = bold;
        return this;
    }

    public IconLabelItem withTypeface(Typeface typeface) {
        this.typeface = typeface;
        return this;
    }

    public IconLabelItem withGravity(int gravity) {
        this.gravity = gravity;
        return this;
    }

    public IconLabelItem withTextGravity(int textGravity) {
        this.textGravity = textGravity;
        return this;
    }

    public IconLabelItem withMatchParent(boolean matchParent) {
        this.matchParent = matchParent;
        return this;
    }

    public IconLabelItem withWidth(int width) {
        this.width = width;
        return this;
    }

    public IconLabelItem withOnClickListener(@Nullable View.OnClickListener listener) {
        this.listener = listener;
        return this;
    }

    public IconLabelItem withMaxTextLines(int maxTextLines) {
        this.maxTextLines = maxTextLines;
        return this;
    }

    @Override
    public void setIcon(int resId) {
        this.iconProvider = Setup.imageLoader().createIconProvider(resId);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_icon_label;
    }

    @Override
    public int getType() {
        return R.id.id_adapter_icon_label_item;
    }

    @Override
    public void bindView(IconLabelItem.ViewHolder holder, List payloads) {
        if (matchParent)
            holder.itemView.getLayoutParams().width = RecyclerView.LayoutParams.MATCH_PARENT;
        if (width != -1)
            holder.itemView.getLayoutParams().width = width;
        holder.textView.setMaxLines(maxTextLines);
        holder.textView.setText(maxTextLines != 0 ? getLabel() : "");
        holder.textView.setGravity(gravity);
        holder.textView.setTypeface(typeface);
        holder.textView.setCompoundDrawablePadding((int) drawablePadding);
        holder.textView.setGravity(textGravity);
        if (bold)
            holder.textView.setTypeface(Typeface.DEFAULT_BOLD);

        Setup.logger().log(this, Log.INFO, null, "IconLabelItem - forceSize: %d", forceSize);
        iconProvider.loadIconIntoTextView(holder.textView, forceSize, iconGravity);

        holder.textView.setTextColor(textColor);
        if (listener != null)
            holder.itemView.setOnClickListener(listener);
        super.bindView(holder, payloads);
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        if (iconProvider != null) {
            iconProvider.cancelLoad(holder.textView);
        }
        holder.textView.setText("");
        holder.textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }
}
