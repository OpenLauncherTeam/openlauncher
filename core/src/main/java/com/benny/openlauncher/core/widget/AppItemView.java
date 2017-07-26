package com.benny.openlauncher.core.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;

import com.benny.openlauncher.core.R;
import com.benny.openlauncher.core.activity.Home;
import com.benny.openlauncher.core.interfaces.App;
import com.benny.openlauncher.core.interfaces.IconDrawer;
import com.benny.openlauncher.core.interfaces.IconProvider;
import com.benny.openlauncher.core.manager.Setup;
import com.benny.openlauncher.core.model.Item;
import com.benny.openlauncher.core.util.BaseIconProvider;
import com.benny.openlauncher.core.util.Definitions;
import com.benny.openlauncher.core.util.DragAction;
import com.benny.openlauncher.core.util.DragDropHandler;
import com.benny.openlauncher.core.util.Tool;
import com.benny.openlauncher.core.viewutil.DesktopCallBack;
import com.benny.openlauncher.core.viewutil.GroupIconDrawable;
import com.benny.openlauncher.core.viewutil.ItemGestureListener;

public class AppItemView extends View implements Drawable.Callback, IconDrawer {

    public static AppItemView createAppItemViewPopup(Context context, Item groupItem, App item, int iconSize) {
        AppItemView.Builder b = new AppItemView.Builder(context, iconSize)
                .withOnTouchGetPosition(groupItem, Setup.itemGestureCallback())
                .setTextColor(Setup.appSettings().getPopupLabelColor());
        if (groupItem.type == Item.Type.SHORTCUT) {
            b.setShortcutItem(groupItem);
        } else {
            App app = Setup.appLoader().findItemApp(groupItem);
            if (app != null) {
                b.setAppItem(groupItem, app);
            }
        }
        return b.getView();
    }

    public static View createDrawerAppItemView(Context context, final Home home, App app, int iconSize, AppItemView.LongPressCallBack longPressCallBack) {
        return new AppItemView.Builder(context, iconSize)
                .setAppItem(app)
                .withOnTouchGetPosition(null, null)
                .withOnLongClick(app, DragAction.Action.APP_DRAWER, longPressCallBack)
                .setLabelVisibility(Setup.appSettings().isDrawerShowLabel())
                .setTextColor(Setup.appSettings().getDrawerLabelColor())
                .getView();
    }

    private Drawable icon = null;
    private BaseIconProvider iconProvider;
    private String label;
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Rect textContainer = new Rect();
    private Typeface typeface;
    private float iconSize;
    private boolean showLabel = false;
    private boolean vibrateWhenLongPress;
    private float labelHeight;
    private int targetedWidth;
    private int targetedHeightPadding;
    private float heightPadding;
    private boolean fastAdapterItem;

    public View getView() {
        return this;
    }

    public IconProvider getIconProvider() {
        return iconProvider;
    }

    public Drawable getCurrentIcon() {
        return icon;
    }

    public void setIconProvider(BaseIconProvider iconProvider) {
        this.iconProvider = iconProvider;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public float getIconSize() {
        return iconSize;
    }

    public void setIconSize(float iconSize) {
        this.iconSize = iconSize;
    }

    public boolean getShowLabel() {
        return showLabel;
    }

    public void setTargetedWidth(int width) {
        targetedWidth = width;
    }

    public void setTargetedHeightPadding(int padding) {
        targetedHeightPadding = padding;
    }

    public AppItemView(Context context) {
        this(context, null);
    }

    public AppItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (typeface == null) {
            typeface = Typeface.createFromAsset(getContext().getAssets(), "RobotoCondensed-Regular.ttf");
        }

        setWillNotDraw(false);
        setDrawingCacheEnabled(true);
        setWillNotCacheDrawing(false);

        labelHeight = Tool.dp2px(14, getContext());

        textPaint.setTextSize(Tool.sp2px(getContext(), 14));
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTypeface(typeface);
    }

    public void load() {
        if (iconProvider != null) {
            iconProvider.loadIconIntoIconDrawer(this, (int)iconSize, 0);
        }
    }

    public void reset() {
        if (iconProvider != null) {
            iconProvider.cancelLoad(this);
        }
        label = "";
        icon = null;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float mWidth = iconSize;
        float mHeight = iconSize + (showLabel ? 0 : labelHeight);
        if (targetedWidth != 0) {
            mWidth = targetedWidth;
        }
        setMeasuredDimension((int) Math.ceil(mWidth), (int) Math.ceil((int) mHeight) + Tool.dp2px(2, getContext()) + targetedHeightPadding * 2);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        heightPadding = (getHeight() - iconSize - (showLabel ? 0 : labelHeight)) / 2f;
        if (label != null && !showLabel) {
            textPaint.getTextBounds(label, 0, label.length(), textContainer);
        }

        // use ellipsis if the label is too long
        if (label != null && !showLabel && textContainer.width() > 0) {
            float characterSize = textContainer.width() / label.length();
            int charToTruncate = (int) Math.ceil(((label.length() * characterSize) - getWidth()) / characterSize);

            // set start position manually if text container is too large
            float x = Math.max(8, (getWidth() - textContainer.width()) / 2f);

            if (textContainer.width() > getWidth() && label.length() - 3 - charToTruncate > 0) {
                canvas.drawText(label.substring(0, label.length() - 3 - charToTruncate) + "...", x, getHeight() - heightPadding, textPaint);
            } else {
                canvas.drawText(label, x, getHeight() - heightPadding, textPaint);
            }
        }

        // center the icon
        if (icon != null) {
            canvas.save();
            canvas.translate((getWidth() - iconSize) / 2, heightPadding);
            icon.setBounds(0, 0, (int) iconSize, (int) iconSize);
            icon.draw(canvas);
            canvas.restore();
        }
    }

    @Override
    public void onIconAvailable(Drawable drawable, int index) {
        icon = drawable;
        super.invalidate();
    }

    @Override
    public void onIconCleared(Drawable placeholder, int index) {
        icon = placeholder;
        super.invalidate();
    }

    @Override
    public void onAttachedToWindow() {
        if (!fastAdapterItem && iconProvider != null) {
            iconProvider.loadIconIntoIconDrawer(this, (int) iconSize, 0);
        }
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        if (!fastAdapterItem && iconProvider != null) {
            iconProvider.cancelLoad(this);
            icon = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    public void invalidate() {
        if (!fastAdapterItem && iconProvider != null) {
            iconProvider.cancelLoad(this);
            icon = null;
        } else {
            super.invalidate();
        }
    }

    public static class Builder {
        AppItemView view;

        public Builder(Context context, int iconSize) {
            view = new AppItemView(context);
            view.setIconSize(Tool.dp2px(iconSize, view.getContext()));
        }

        public Builder(AppItemView view, int iconSize) {
            this.view = view;
            view.setIconSize(Tool.dp2px(iconSize, view.getContext()));
        }

        public AppItemView getView() {
            return view;
        }

        public Builder setAppItem(final App app) {
            view.setLabel(app.getLabel());
            view.setIconProvider(app.getIconProvider());
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tool.createScaleInScaleOutAnim(view, new Runnable() {
                        @Override
                        public void run() {
                            Tool.startApp(view.getContext(), app);
                        }
                    });
                }
            });
            return this;
        }

        public Builder setAppItem(final Item item, final App app) {
            view.setLabel(item.getLabel());
            view.setIconProvider(app.getIconProvider());
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tool.createScaleInScaleOutAnim(view, new Runnable() {
                        @Override
                        public void run() {
                            Tool.startApp(view.getContext(), item.intent);
                        }
                    });
                }
            });
            return this;
        }

        public Builder setShortcutItem(final Item item) {
            view.setLabel(item.getLabel());
            view.setIconProvider(item.getIconProvider());
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tool.createScaleInScaleOutAnim(view, new Runnable() {
                        @Override
                        public void run() {
                            view.getContext().startActivity(item.intent);
                        }
                    });
                }
            });
            return this;
        }

        public Builder setGroupItem(Context context, final DesktopCallBack callback, final Item item, int iconSize) {
            view.setLabel(item.getLabel());
            view.setIconProvider(Setup.imageLoader().createIconProvider(new GroupIconDrawable(context, item, iconSize)));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Home.launcher != null && (Home.launcher).groupPopup.showWindowV(item, v, callback)) {
                        ((GroupIconDrawable) ((AppItemView) v).getCurrentIcon()).popUp();
                    }
                }
            });
            return this;
        }

        public Builder setActionItem(Item item) {
            view.setLabel(item.getLabel());
            view.setIconProvider(Setup.imageLoader().createIconProvider(R.drawable.ic_app_drawer_24dp));
            switch (item.actionValue) {
                case Definitions.ACTION_LAUNCHER:
                    view.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                            Home.launcher.openAppDrawer(view);
                        }
                    });
                    break;
            }
            return this;
        }

        public Builder withOnLongClick(final App app, final DragAction.Action action, @Nullable final LongPressCallBack eventAction) {
            withOnLongClick(Item.newAppItem(app), action, eventAction);
            return this;
        }

        public Builder withOnLongClick(final Item item, final DragAction.Action action, @Nullable final LongPressCallBack eventAction) {
            view.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (Setup.appSettings().isDesktopLock()) {
                        return false;
                    }
                    if (eventAction != null && !eventAction.readyForDrag(v)) {
                        return false;
                    }
                    if (view.vibrateWhenLongPress) {
                        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    }
                    DragDropHandler.startDrag(v, item, action, eventAction);
                    return true;
                }
            });
            return this;
        }

        public Builder withOnTouchGetPosition(Item item, ItemGestureListener.ItemGestureCallback itemGestureCallback) {
            view.setOnTouchListener(Tool.getItemOnTouchListener(item, itemGestureCallback));
            return this;
        }

        public Builder setTextColor(@ColorInt int color) {
            view.textPaint.setColor(color);
            return this;
        }

        public Builder setLabelVisibility(boolean visible) {
            view.showLabel = !visible;
            return this;
        }

        public Builder vibrateWhenLongPress() {
            view.vibrateWhenLongPress = true;
            return this;
        }

        public Builder setFastAdapterItem() {
            view.fastAdapterItem = true;
            return this;
        }
    }

    public interface LongPressCallBack {
        boolean readyForDrag(View view);

        void afterDrag(View view);
    }
}
