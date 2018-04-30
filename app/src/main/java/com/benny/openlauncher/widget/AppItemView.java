package com.benny.openlauncher.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.interfaces.IconDrawer;
import com.benny.openlauncher.interfaces.IconProvider;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.App;
import com.benny.openlauncher.util.Definitions;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.DragHandler;
import com.benny.openlauncher.util.SimpleIconProvider;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DesktopCallBack;
import com.benny.openlauncher.viewutil.GroupIconDrawable;
import com.benny.openlauncher.viewutil.ItemGestureListener;

public class AppItemView extends View implements Drawable.Callback, IconDrawer {

    private static final int MIN_ICON_TEXT_MARGIN = 8;
    private static final char ELLIPSIS = '…';

    private Drawable _icon = null;
    private SimpleIconProvider _iconProvider;
    private String _label;
    private Paint _textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Rect _textContainer = new Rect(), testTextContainer = new Rect();
    private Typeface _typeface;
    private float _iconSize;
    private boolean _showLabel = true;
    private boolean _vibrateWhenLongPress;
    private float _labelHeight;
    private int _targetedWidth;
    private int _fontSizeSp;
    private int _targetedHeightPadding;
    private float _heightPadding;
    private boolean _fastAdapterItem;

    public AppItemView(Context context) {
        this(context, null);
    }

    public AppItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (_typeface == null) {
            _typeface = Typeface.createFromAsset(getContext().getAssets(), "RobotoCondensed-Regular.ttf");
        }

        setWillNotDraw(false);
        setDrawingCacheEnabled(true);
        setWillNotCacheDrawing(false);

        _labelHeight = Tool.dp2px(14, getContext());

        _textPaint.setTextSize(Tool.sp2px(getContext(), 13));
        _textPaint.setColor(Color.DKGRAY);
        _textPaint.setTypeface(_typeface);
    }

    public static AppItemView createAppItemViewPopup(Context context, Item groupItem, App item, int iconSize, float fontSizeSp) {
        AppItemView.Builder b = new AppItemView.Builder(context, iconSize)
                .withOnTouchGetPosition(groupItem, Setup.itemGestureCallback())
                .setTextColor(Setup.appSettings().getFolderLabelColor())
                .setFontSize(context, fontSizeSp);
        if (groupItem.getType() == Item.Type.SHORTCUT) {
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
                .setFontSize(context, Setup.appSettings().getDrawerLabelFontSize())
                .getView();
    }

    @Override
    public Bitmap getDrawingCache() {
        return Tool.drawableToBitmap(_icon);
    }

    public View getView() {
        return this;
    }

    public IconProvider getIconProvider() {
        return _iconProvider;
    }

    public void setIconProvider(SimpleIconProvider iconProvider) {
        _iconProvider = iconProvider;
        iconProvider.loadIcon(IconProvider.IconTargetType.IconDrawer, (int) _iconSize, this, 0);
    }

    public Drawable getCurrentIcon() {
        return _icon;
    }

    public String getLabel() {
        return _label;
    }

    public void setLabel(String label) {
        _label = label;
    }

    public float getIconSize() {
        return _iconSize;
    }

    public void setIconSize(float iconSize) {
        _iconSize = iconSize;
    }

    public boolean getShowLabel() {
        return _showLabel;
    }

    public void setTargetedWidth(int width) {
        _targetedWidth = width;
    }

    public void setTargetedHeightPadding(int padding) {
        _targetedHeightPadding = padding;
    }

    public void load() {
        if (_iconProvider != null) {
            _iconProvider.loadIcon(IconProvider.IconTargetType.IconDrawer, (int) _iconSize, this, 0);
        }
    }

    public void reset() {
        if (_iconProvider != null) {
            _iconProvider.cancelLoad(IconProvider.IconTargetType.IconDrawer, this);
        }
        _label = "";
        _icon = null;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float mWidth = _iconSize;
        float mHeight = _iconSize + (_showLabel ? 0 : _labelHeight);
        if (_targetedWidth != 0) {
            mWidth = _targetedWidth;
        }
        setMeasuredDimension((int) Math.ceil(mWidth), (int) Math.ceil((int) mHeight) + Tool.dp2px(2, getContext()) + _targetedHeightPadding * 2);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        _heightPadding = (getHeight() - _iconSize - (_showLabel ? _labelHeight : 0)) / 2f;

        if (_label != null && _showLabel) {
            _textPaint.getTextBounds(_label, 0, _label.length(), _textContainer);
            int maxTextWidth = getWidth() - MIN_ICON_TEXT_MARGIN * 2;

            // use ellipsis if the label is too long
            if (_textContainer.width() > maxTextWidth) {
                String testLabel = _label + ELLIPSIS;
                _textPaint.getTextBounds(testLabel, 0, testLabel.length(), testTextContainer);

                //Premeditate to be faster
                float characterSize = testTextContainer.width() / testLabel.length();
                int charsToTruncate = (int) ((testTextContainer.width() - maxTextWidth) / characterSize);

                canvas.drawText(_label.substring(0, _label.length() - charsToTruncate) + ELLIPSIS,
                        MIN_ICON_TEXT_MARGIN, getHeight() - _heightPadding, _textPaint);
            } else {
                canvas.drawText(_label, (getWidth() - _textContainer.width()) / 2f, getHeight() - _heightPadding, _textPaint);
            }
        }

        // center the _icon
        if (_icon != null) {
            canvas.save();
            canvas.translate((getWidth() - _iconSize) / 2, _heightPadding);
            _icon.setBounds(0, 0, (int) _iconSize, (int) _iconSize);
            _icon.draw(canvas);
            canvas.restore();
        }
    }

    public float getDrawIconTop() {
        return _heightPadding;
    }

    public float getDrawIconLeft() {
        return (getWidth() - _iconSize) / 2;
    }

    @Override
    public void onIconAvailable(Drawable drawable, int index) {
        _icon = drawable;
        super.invalidate();
    }

    @Override
    public void onIconCleared(Drawable placeholder, int index) {
        _icon = placeholder;
        super.invalidate();
    }

    @Override
    public void onAttachedToWindow() {
        if (!_fastAdapterItem && _iconProvider != null) {
            _iconProvider.loadIcon(IconProvider.IconTargetType.IconDrawer, (int) _iconSize, this, 0);
        }
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        if (!_fastAdapterItem && _iconProvider != null) {
            _iconProvider.cancelLoad(IconProvider.IconTargetType.IconDrawer, this);
            _icon = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    public void invalidate() {
        if (!_fastAdapterItem && _iconProvider != null) {
            _iconProvider.cancelLoad(IconProvider.IconTargetType.IconDrawer, this);
            _icon = null;
        } else {
            super.invalidate();
        }
    }

    public interface LongPressCallBack {
        boolean readyForDrag(View view);

        void afterDrag(View view);
    }

    public static class Builder {
        AppItemView _view;

        public Builder(Context context, int iconSize) {
            _view = new AppItemView(context);
            _view.setIconSize(Tool.dp2px(iconSize, _view.getContext()));
        }

        public Builder(AppItemView view, int iconSize) {
            _view = view;
            view.setIconSize(Tool.dp2px(iconSize, view.getContext()));
        }

        public static OnLongClickListener getLongClickDragAppListener(final Item item, final DragAction.Action action, @Nullable final LongPressCallBack eventAction) {
            return new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (Setup.appSettings().isDesktopLock()) {
                        return false;
                    }
                    if (eventAction != null && !eventAction.readyForDrag(v)) {
                        return false;
                    }
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    DragHandler.startDrag(v, item, action, eventAction);
                    return true;
                }
            };
        }

        public AppItemView getView() {
            return _view;
        }

        public Builder setAppItem(final App app) {
            _view.setLabel(app.getLabel());
            _view.setIconProvider(app.getIconProvider());
            _view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tool.createScaleInScaleOutAnim(_view, new Runnable() {
                        @Override
                        public void run() {
                            Tool.startApp(_view.getContext(), app, _view);
                        }
                    }, 0.85f);
                }
            });
            return this;
        }

        public Builder setAppItem(final Item item, final App app) {
            _view.setLabel(item.getLabel());
            _view.setIconProvider(app.getIconProvider());
            _view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tool.createScaleInScaleOutAnim(_view, new Runnable() {
                        @Override
                        public void run() {
                            Tool.startApp(_view.getContext(), app, _view);
                        }
                    }, 0.85f);
                }
            });
            return this;
        }

        public Builder setShortcutItem(final Item item) {
            _view.setLabel(item.getLabel());
            _view.setIconProvider(item.getIconProvider());
            _view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tool.createScaleInScaleOutAnim(_view, new Runnable() {
                        @Override
                        public void run() {
                            _view.getContext().startActivity(item.getIntent());
                        }
                    }, 0.85f);
                }
            });
            return this;
        }

        public Builder setGroupItem(Context context, final DesktopCallBack callback, final Item item, int iconSize) {
            _view.setLabel(item.getLabel());
            _view.setIconProvider(Setup.imageLoader().createIconProvider(new GroupIconDrawable(context, item, iconSize)));
            _view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Home.Companion.getLauncher() != null && (Home.Companion.getLauncher()).getGroupPopup().showWindowV(item, v, callback)) {
                        ((GroupIconDrawable) ((AppItemView) v).getCurrentIcon()).popUp();
                    }
                }
            });
            return this;
        }

        public Builder setActionItem(Item item) {
            _view.setLabel(item.getLabel());
            _view.setIconProvider(Setup.imageLoader().createIconProvider(
                    ContextCompat.getDrawable(Setup.appContext(), R.drawable.ic_apps_white_48dp)));
            switch (item.getActionValue()) {
                case Definitions.ACTION_LAUNCHER:
                    _view.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                            Home.Companion.getLauncher().openAppDrawer(view);
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
            _view.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (Setup.appSettings().isDesktopLock()) {
                        return false;
                    }
                    if (eventAction != null && !eventAction.readyForDrag(v)) {
                        return false;
                    }
                    if (_view._vibrateWhenLongPress) {
                        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    }
                    DragHandler.startDrag(_view, item, action, eventAction);
                    return true;
                }
            });
            return this;
        }

        public Builder withOnTouchGetPosition(Item item, ItemGestureListener.ItemGestureCallback itemGestureCallback) {
            _view.setOnTouchListener(Tool.getItemOnTouchListener(item, itemGestureCallback));
            return this;
        }

        public Builder setTextColor(@ColorInt int color) {
            _view._textPaint.setColor(color);
            return this;
        }

        public Builder setFontSize(Context context, float fontSizeSp) {
            _view._textPaint.setTextSize(Tool.sp2px(context, fontSizeSp));
            return this;
        }

        public Builder setLabelVisibility(boolean visible) {
            _view._showLabel = visible;
            return this;
        }

        public Builder vibrateWhenLongPress() {
            _view._vibrateWhenLongPress = true;
            return this;
        }

        public Builder setFastAdapterItem() {
            _view._fastAdapterItem = true;
            return this;
        }
    }
}
