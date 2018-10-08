package com.benny.openlauncher.viewutil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.benny.openlauncher.R;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.Tool;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

public class IconLabelItem extends AbstractItem<IconLabelItem, IconLabelItem.ViewHolder> {

    public Drawable _icon;
    public String _label;

    /**
     * Extra string for filtering/search purpose.
     * Ex. root-locale app name (label) and Search Bar.
     */
    @Nullable public String _searchInfo;

    private View.OnLongClickListener _onLongClickListener;
    private View.OnClickListener _onClickListener;

    private Typeface _typeface;
    private int _iconSize = Integer.MAX_VALUE;
    private int _iconGravity;
    private int _iconPadding;
    private boolean _matchParent = true;
    private int _width = -1;
    private boolean _bold = false;
    private int _textMaxLines = 1;
    private int _textColor = Integer.MAX_VALUE;
    private int _textGravity = Gravity.CENTER_VERTICAL;

    public IconLabelItem(Context context, int label) {
        _label = context.getString(label);
    }

    public IconLabelItem(Context context, int icon, int label) {
        _label = context.getString(label);
        _icon = context.getResources().getDrawable(icon);
    }

    public IconLabelItem(Context context, Drawable icon, String label) {
        _label = label;
        _icon = icon;
    }

    public IconLabelItem withSearchInfo(String searchInfo) {
        _searchInfo = searchInfo;
        return this;
    }

    public IconLabelItem withIconGravity(int iconGravity) {
        _iconGravity = iconGravity;
        return this;
    }

    public IconLabelItem withIconPadding(Context context, int iconPadding) {
        _iconPadding = Tool.dp2px(iconPadding, context);
        return this;
    }

    public IconLabelItem withIconSize(Context context, int iconSize) {
        _iconSize = Tool.dp2px(iconSize, context);
        return this;
    }

    public IconLabelItem withBold(boolean bold) {
        _bold = bold;
        return this;
    }

    public IconLabelItem withTypeface(Typeface typeface) {
        _typeface = typeface;
        return this;
    }

    public IconLabelItem withTextGravity(int textGravity) {
        _textGravity = textGravity;
        return this;
    }

    public IconLabelItem withTextColor(int textColor) {
        _textColor = textColor;
        return this;
    }

    public IconLabelItem withTextMaxLines(int textMaxLines) {
        _textMaxLines = textMaxLines;
        return this;
    }

    public IconLabelItem withMatchParent(boolean matchParent) {
        _matchParent = matchParent;
        return this;
    }

    public IconLabelItem withWidth(int width) {
        _width = width;
        return this;
    }

    public IconLabelItem withOnClickListener(@Nullable View.OnClickListener listener) {
        _onClickListener = listener;
        return this;
    }

    public IconLabelItem withOnLongClickListener(@Nullable View.OnLongClickListener onLongClickListener) {
        _onLongClickListener = onLongClickListener;
        return this;
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v, this);
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
        if (_matchParent)
            holder.itemView.getLayoutParams().width = RecyclerView.LayoutParams.MATCH_PARENT;
        if (_width != -1)
            holder.itemView.getLayoutParams().width = _width;
        holder.textView.setMaxLines(_textMaxLines);
        if (_label != null)
            holder.textView.setText(_label);
        holder.textView.setGravity(_textGravity);
        holder.textView.setTypeface(_typeface);
        if (_textColor != Integer.MAX_VALUE)
            holder.textView.setTextColor(_textColor);
        if (_bold)
            holder.textView.setTypeface(Typeface.DEFAULT_BOLD);

        holder.textView.setCompoundDrawablePadding(_iconPadding);
        if (_iconSize != Integer.MAX_VALUE)
            _icon = new BitmapDrawable(Setup.appContext().getResources(), Bitmap.createScaledBitmap(Tool.drawableToBitmap(_icon), _iconSize, _iconSize, true));
        switch (_iconGravity) {
            case Gravity.START:
                holder.textView.setCompoundDrawablesWithIntrinsicBounds(_icon, null, null, null);
                break;
            case Gravity.END:
                holder.textView.setCompoundDrawablesWithIntrinsicBounds(null, null, _icon, null);
                break;
            case Gravity.TOP:
                holder.textView.setCompoundDrawablesWithIntrinsicBounds(null, _icon, null, null);
                break;
            case Gravity.BOTTOM:
                holder.textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, _icon);
                break;
        }
        if (_onClickListener != null)
            holder.itemView.setOnClickListener(_onClickListener);
        if (_onLongClickListener != null)
            holder.itemView.setOnLongClickListener(_onLongClickListener);
        super.bindView(holder, payloads);
    }

    @Override
    public void unbindView(@NonNull ViewHolder holder) {
        super.unbindView(holder);
        holder.textView.setText("");
        holder.textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View itemView, IconLabelItem item) {
            super(itemView);
            textView = (TextView) itemView;
            textView.setTag(item);
        }
    }

    public void setIcon(Drawable icon) {
        _icon = icon;
    }
}
