package com.benny.openlauncher.model;

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
import com.benny.openlauncher.core.interfaces.*;
import com.benny.openlauncher.core.model.BaseIconLabelItem;
import com.benny.openlauncher.core.util.Tool;
import com.benny.openlauncher.util.AppManager;

import java.util.List;

public class IconLabelItem extends BaseIconLabelItem<Item, IconLabelItem, IconLabelItem.ViewHolder> {

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    private Drawable drawable = null;

    private int iconGravity;
    private int textColor = Color.DKGRAY;
    private int gravity = android.view.Gravity.CENTER_VERTICAL;
    private float drawablePadding;
    private Typeface typeface;
    private boolean matchParent = true;
    private boolean bold = false;
    private int textGravity = Gravity.CENTER_VERTICAL;

    public IconLabelItem(Context context, int label) {
        super(context, 0, label);
    }

    public IconLabelItem(Context context, int icon, String label, int forceSize) {
        super(null);
        this.label = label;
        this.drawable = context.getResources().getDrawable(icon);
        scaleDrawable(context, forceSize);
    }

    public IconLabelItem(Context context, int icon, int label, int forceSize) {
        super(null);
        this.label = context.getString(label);
        this.drawable = context.getResources().getDrawable(icon);
        scaleDrawable(context, forceSize);
    }

    public IconLabelItem(Context context, Drawable icon, String label, int forceSize) {
        super(null);
        this.label = label;
        this.drawable = icon;
        scaleDrawable(context, forceSize);
    }

    public IconLabelItem(Context context, Drawable icon, int label, int forceSize) {
        super(null);
        this.label = context.getString(label);
        this.drawable = icon;
        scaleDrawable(context, forceSize);
    }

    private void scaleDrawable(Context context, int forceSize) {
        if (drawable != null && forceSize != -1) {
            forceSize = Tool.dp2px(forceSize, context);

            this.drawable = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(Tool.drawableToBitmap(drawable), forceSize, forceSize, true));
        }
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

    @Override
    public int getLayoutRes() {
        return R.layout.item_icon_label;
    }

    @Override
    public void bindView(IconLabelItem.ViewHolder holder, List payloads) {
        if (matchParent)
            holder.itemView.getLayoutParams().width = RecyclerView.LayoutParams.MATCH_PARENT;
        holder.textView.setText(getLabel());
        holder.textView.setGravity(gravity);
        holder.textView.setTypeface(typeface);
        holder.textView.setCompoundDrawablePadding((int) drawablePadding);
        holder.textView.setGravity(textGravity);
        if (bold)
            holder.textView.setTypeface(Typeface.DEFAULT_BOLD);

        Drawable dl = null, dt = null, dr = null, db = null;
        switch (iconGravity) {
            case Gravity.START:
                dl = drawable;
                break;
            case Gravity.TOP:
                dt = drawable;
                break;
            case Gravity.END:
                dr = drawable;
                break;
            case Gravity.BOTTOM:
                db = drawable;
                break;
        }
        holder.textView.setCompoundDrawablesWithIntrinsicBounds(dl, dt, dr, db);

        holder.textView.setTextColor(textColor);
        if (listener != null)
            holder.itemView.setOnClickListener(listener);
        super.bindView(holder, payloads);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }
}
