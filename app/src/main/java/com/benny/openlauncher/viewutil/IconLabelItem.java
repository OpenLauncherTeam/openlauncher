package com.benny.openlauncher.viewutil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.benny.openlauncher.R;
import com.benny.openlauncher.util.Tool;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;

import java.util.List;

/**
 * Created by BennyKok on 10/25/2016
 */

public class IconLabelItem extends AbstractItem<IconLabelItem, IconLabelItem.ViewHolder> {

    private static final ViewHolderFactory<? extends IconLabelItem.ViewHolder> FACTORY = new ItemFactory();
    public String label;
    private Drawable icon;
    private View.OnClickListener listener;
    private int iconGravity;
    private int textColor = Color.DKGRAY;
    private int gravity = android.view.Gravity.CENTER_VERTICAL;
    private float drawablePadding;
    private Typeface typeface;
    private boolean matchParent = true;
    private int forceSize = -1;

    public IconLabelItem(Context context, Drawable icon, String label, @Nullable View.OnClickListener listener, int iconGravity) {
        this.label = label;
        this.icon = icon;
        this.listener = listener;
        this.iconGravity = iconGravity;

        this.drawablePadding = Tool.dp2px(drawablePadding, context);
    }

    public IconLabelItem(Context context, Drawable icon, String label, @Nullable View.OnClickListener listener) {
        this(context, icon, label, listener, Gravity.START);
    }

    public IconLabelItem(Context context, Drawable icon, String label, @Nullable View.OnClickListener listener, int drawablePadding, int forceSize) {
        this(context, icon, label, listener, Gravity.START);
        this.drawablePadding = drawablePadding;
        if (forceSize != -1) {
            this.icon = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(Tool.drawableToBitmap(icon), forceSize, forceSize, true));
        }
    }

    public IconLabelItem(Context context, Drawable icon, String label, @Nullable View.OnClickListener listener, int textColor, int drawablePadding, int forceSize) {
        this(context, icon, label, listener, Gravity.START);
        this.textColor = textColor;
        this.drawablePadding = drawablePadding;
        this.forceSize = forceSize;
        if (forceSize != -1) {
            this.icon = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(Tool.drawableToBitmap(icon), forceSize, forceSize, true));
        }
    }

    public IconLabelItem(Context context, int icon, int label, @Nullable View.OnClickListener listener, int iconGravity, int textColor, int gravity, int drawablePadding, Typeface typeface) {
        this(context, context.getResources().getDrawable(icon), context.getResources().getString(label), listener, iconGravity);
        this.textColor = textColor;
        this.gravity = gravity;
        this.drawablePadding = Tool.dp2px(drawablePadding, context);
        this.typeface = typeface;
    }

    public IconLabelItem(Context context, int icon, int label, @Nullable View.OnClickListener listener, int iconGravity, int textColor, int gravity, int drawablePadding, Typeface typeface, boolean matchParent) {
        this(context, icon, label, listener, iconGravity, textColor, gravity, Tool.dp2px(drawablePadding, context), typeface);
        this.matchParent = matchParent;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_icon_label;
    }

    @Override
    public ViewHolderFactory<? extends IconLabelItem.ViewHolder> getFactory() {
        return FACTORY;
    }

    @Override
    public void bindView(IconLabelItem.ViewHolder holder, List payloads) {
        if (matchParent)
            holder.itemView.getLayoutParams().width = RecyclerView.LayoutParams.MATCH_PARENT;
        holder.textView.setText(label);
        holder.textView.setGravity(gravity);
        holder.textView.setTypeface(typeface);
        holder.textView.setCompoundDrawablePadding((int) drawablePadding);
        switch (iconGravity) {
            case Gravity.START:
                holder.textView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                break;
            case Gravity.TOP:
                holder.textView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
                break;
            case Gravity.END:
                holder.textView.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
                break;
            case Gravity.BOTTOM:
                holder.textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, icon);
                break;
        }
        holder.textView.setTextColor(textColor);
        if (listener != null)
            holder.itemView.setOnClickListener(listener);
        super.bindView(holder, payloads);
    }

    private static class ItemFactory implements ViewHolderFactory<IconLabelItem.ViewHolder> {
        public IconLabelItem.ViewHolder create(View v) {
            return new IconLabelItem.ViewHolder(v);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }
}
