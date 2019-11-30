package com.benny.openlauncher.viewutil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.benny.openlauncher.R;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.util.Tool;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

public class IconLabelItem extends AbstractItem<IconLabelItem, IconLabelItem.ViewHolder> {
    private int _width = Integer.MAX_VALUE;
    private int _height = Integer.MAX_VALUE;

    public Drawable _icon;
    private int _iconSize = Integer.MAX_VALUE;
    private int _iconGravity;
    private int _iconPadding;
    private int _iconColor;

    public String _label;
    private int _textGravity = Gravity.CENTER_VERTICAL;
    private int _textColor = Integer.MAX_VALUE;
    private boolean _textVisibility = true;
    private boolean _isAppLauncher = false;

    private boolean _onClickAnimate = true;
    private View.OnClickListener _onClickListener;
    private View.OnLongClickListener _onLongClickListener;

    public IconLabelItem(Context context, int icon, int label) {
        _label = context.getString(label);
        _icon = context.getResources().getDrawable(icon);
    }

    public IconLabelItem(Drawable icon, String label) {
        _label = label;
        _icon = icon;
    }

    public IconLabelItem withWidth(int width) {
        _width = width;
        return this;
    }

    public IconLabelItem withIconSize(int iconSize) {
        _iconSize = Tool.dp2px(iconSize);
        return this;
    }

    public IconLabelItem withIconColor(int iconColor) {
        _iconColor = iconColor;
        return this;
    }

    public IconLabelItem withIconGravity(int iconGravity) {
        _iconGravity = iconGravity;
        return this;
    }

    public IconLabelItem withIconPadding(int iconPadding) {
        _iconPadding = Tool.dp2px(iconPadding);
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

    public IconLabelItem withTextVisibility(boolean visibility) {
        _textVisibility = visibility;
        return this;
    }

    public IconLabelItem withOnClickAnimate(boolean background) {
        _onClickAnimate = background;
        return this;
    }

    public IconLabelItem withOnClickListener(View.OnClickListener listener) {
        _onClickListener = listener;
        return this;
    }

    public IconLabelItem withOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        _onLongClickListener = onLongClickListener;
        return this;
    }

    @Override
    public ViewHolder getViewHolder(@NonNull View view) {
        return new ViewHolder(view, this);
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
    public void bindView(@NonNull ViewHolder holder, @NonNull List<Object> payloads) {
        if (_width == Integer.MAX_VALUE) {
            holder.itemView.getLayoutParams().width = RecyclerView.LayoutParams.MATCH_PARENT;
        } else {
            holder.itemView.getLayoutParams().width = _width;
        }

        if (_height == Integer.MAX_VALUE) {
            holder.itemView.getLayoutParams().height = RecyclerView.LayoutParams.WRAP_CONTENT;
        } else {
            holder.itemView.getLayoutParams().height = _height;
        }

        // only run all this code if a label should be shown
        if (_label != null && _textVisibility) {
            holder.textView.setText(_label);
            holder.textView.setGravity(_textGravity);
            holder.textView.setMaxLines(1);
            holder.textView.setEllipsize(TextUtils.TruncateAt.END);
            // no default text color since it will be set by the theme
            if (_textColor != Integer.MAX_VALUE)
                holder.textView.setTextColor(_textColor);
        }

        // icon specific padding
        holder.textView.setCompoundDrawablePadding(_iconPadding);
        if (_iconSize != Integer.MAX_VALUE) {
            _icon = new BitmapDrawable(Setup.appContext().getResources(), Bitmap.createScaledBitmap(Tool.drawableToBitmap(_icon), _iconSize, _iconSize, true));
            _icon.setColorFilter(_iconColor, PorterDuff.Mode.SRC_ATOP);
            if (_isAppLauncher) {
                _icon.setBounds(0, 0, _iconSize, _iconSize);
            }
        }

        switch (_iconGravity) {
            case Gravity.START:
                if (_isAppLauncher) {
                    holder.textView.setCompoundDrawables(_icon, null, null, null);
                } else {
                    holder.textView.setCompoundDrawablesWithIntrinsicBounds(_icon, null, null, null);
                }
                break;
            case Gravity.END:
                if (_isAppLauncher) {
                    holder.textView.setCompoundDrawables(null, null, _icon, null);
                } else {
                    holder.textView.setCompoundDrawablesWithIntrinsicBounds(null, null, _icon, null);
                }
                break;
            case Gravity.TOP:
                if (_isAppLauncher) {
                    holder.textView.setCompoundDrawables(null, _icon, null, null);
                } else {
                    holder.textView.setCompoundDrawablesWithIntrinsicBounds(null, _icon, null, null);
                }
                break;
            case Gravity.BOTTOM:
                if (_isAppLauncher) {
                    holder.textView.setCompoundDrawables(null, null, null, _icon);
                } else {
                    holder.textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, _icon);
                }
                break;
        }

        // most items will not use a long click
        if (!_onClickAnimate)
            holder.itemView.setBackgroundResource(0);
        if (_onClickListener != null)
            holder.itemView.setOnClickListener(_onClickListener);
        if (_onLongClickListener != null)
            holder.itemView.setOnLongClickListener(_onLongClickListener);

        super.bindView(holder, payloads);
    }

    public IconLabelItem withIsAppLauncher(boolean isAppLauncher) {
        _isAppLauncher = isAppLauncher;
        return this;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View itemView, IconLabelItem item) {
            super(itemView);
            textView = (TextView) itemView;
            textView.setTag(item);
        }
    }

    // only used for search bar
    public void setIconGravity(int iconGravity) {
        _iconGravity = iconGravity;
    }

    public void setTextGravity(int textGravity) {
        _textGravity = textGravity;
    }
}
