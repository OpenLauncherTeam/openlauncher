package com.benny.openlauncher.viewutil;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.benny.openlauncher.R;
import com.benny.openlauncher.interfaces.FastItem;
import com.benny.openlauncher.interfaces.IconProvider;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.SimpleIconProvider;
import com.benny.openlauncher.util.Tool;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

public class IconLabelItem extends AbstractItem<IconLabelItem, IconLabelItem.ViewHolder> implements FastItem.LabelItem<IconLabelItem, IconLabelItem.ViewHolder>, FastItem.DesktopOptionsItem<IconLabelItem, IconLabelItem.ViewHolder> {

    // Data
    public SimpleIconProvider _iconProvider = null;
    public View.OnLongClickListener _onLongClickListener;
    protected String _label = null;
    // Others
    protected View.OnClickListener _listener;
    protected View.OnTouchListener _onOnTouchListener;

    private int _forceSize = -1;
    private int _iconGravity;
    private int _textColor = Color.DKGRAY;
    private int _gravity = android.view.Gravity.CENTER_VERTICAL;
    private float _drawablePadding;
    private Typeface _typeface;
    private boolean _matchParent = true;
    private int _width = -1;
    private boolean _bold = false;
    private int _textGravity = Gravity.CENTER_VERTICAL;
    private int _maxTextLines = 1;
    private boolean _dontSetOnLongClickListener;
    private boolean _hideLabel = false;

    public IconLabelItem(Item item) {
        _iconProvider = item != null ? item.getIconProvider() : null;
        _label = item != null ? item.getLabel() : null;
    }

    public IconLabelItem(Context context, int icon, int label) {
        _iconProvider = Setup.imageLoader().createIconProvider(icon);
        _label = context.getString(label);
    }

    public IconLabelItem(Context context, int label) {
        this(context, 0, label);
    }

    public IconLabelItem(Context context, int icon, String label, int forceSize) {
        this(null);
        _label = label;
        _iconProvider = Setup.imageLoader().createIconProvider(icon);
        _forceSize = forceSize;
    }

    public IconLabelItem(Context context, int icon, int label, int forceSize) {
        this(null);
        _label = context.getString(label);
        _iconProvider = Setup.imageLoader().createIconProvider(icon);
        _forceSize = forceSize;
    }

    public IconLabelItem(Context context, SimpleIconProvider iconProvider, String label, int forceSize) {
        this(null);
        _label = label;
        _iconProvider = iconProvider;
        _forceSize = forceSize;
    }

    public IconLabelItem(Context context, Drawable icon, String label, int forceSize) {
        this(null);
        _label = label;
        _iconProvider = Setup.imageLoader().createIconProvider(icon);
        _forceSize = forceSize;
    }

    public IconLabelItem withIconGravity(int iconGravity) {
        _iconGravity = iconGravity;
        return this;
    }

    public IconLabelItem withDrawablePadding(Context context, int drawablePadding) {
        _drawablePadding = Tool.dp2px(drawablePadding, context);
        return this;
    }


    public IconLabelItem withTextColor(int textColor) {
        _textColor = textColor;
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

    public IconLabelItem withGravity(int gravity) {
        _gravity = gravity;
        return this;
    }

    public IconLabelItem withTextGravity(int textGravity) {
        _textGravity = textGravity;
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
        _listener = listener;
        return this;
    }

    public IconLabelItem withMaxTextLines(int maxTextLines) {
        _maxTextLines = maxTextLines;
        return this;
    }


    public IconLabelItem withOnLongClickListener(@Nullable View.OnLongClickListener onLongClickListener) {
        _onLongClickListener = onLongClickListener;
        return this;
    }


    @Override
    public void setIcon(int resId) {
        _iconProvider = Setup.imageLoader().createIconProvider(resId);
    }

    @Override
    public String getLabel() {
        return _label;
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
        holder.textView.setMaxLines(1);
        if (getLabel() != null)
            holder.textView.setText(_maxTextLines != 0 ? getLabel() : "");
        holder.textView.setGravity(_gravity);
        holder.textView.setGravity(_textGravity);
        holder.textView.setCompoundDrawablePadding((int) _drawablePadding);

        if (_hideLabel) {
            holder.textView.setText(null);
            _iconProvider.loadIcon(IconProvider.IconTargetType.TextView, _forceSize, holder.textView, Gravity.TOP);
        } else {
            _iconProvider.loadIcon(IconProvider.IconTargetType.TextView, _forceSize, holder.textView, _iconGravity);
        }

        holder.textView.setTypeface(_typeface);
        if (_bold)
            holder.textView.setTypeface(Typeface.DEFAULT_BOLD);

        //Setup.logger().log(this, Log.INFO, null, "IconLabelItem - forceSize: %d", forceSize);

        holder.textView.setTextColor(_textColor);
        if (_listener != null)
            holder.itemView.setOnClickListener(_listener);
        if (_onLongClickListener != null && !_dontSetOnLongClickListener)
            holder.itemView.setOnLongClickListener(_onLongClickListener);
        if (_onOnTouchListener != null)
            holder.itemView.setOnTouchListener(_onOnTouchListener);
        super.bindView(holder, payloads);
    }

    @Override
    public void unbindView(@NonNull ViewHolder holder) {
        super.unbindView(holder);
        if (_iconProvider != null) {
            _iconProvider.cancelLoad(IconProvider.IconTargetType.TextView, holder.textView);
        }
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
}
