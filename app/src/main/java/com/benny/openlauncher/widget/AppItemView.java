package com.benny.openlauncher.widget;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.View;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.GoodDragShadowBuilder;

import static com.benny.openlauncher.activity.Home.resources;

/**
 * Created by BennyKok on 10/23/2016
 */

public class AppItemView extends View implements Drawable.Callback {

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
        if (icon != null) {
            this.icon.setCallback(this);
            invalidate();
        }
    }

    @Override
    public void refreshDrawableState() {
        invalidateDrawable(icon);
        super.refreshDrawableState();
    }

    private static Typeface myType;

    @Override
    public void invalidateDrawable(Drawable drawable) {
        invalidate();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
        invalidate();
    }

    public float getIconSize() {
        return iconSize;
    }

    public void setIconSize(float iconSize) {
        this.iconSize = iconSize;
    }

    private float iconSize;

    public float getIconPadding() {
        return iconPadding;
    }

    public void setIconPadding(float iconPadding) {
        this.iconPadding = iconPadding;
    }

    private float iconPadding;

    public float getIconSizeSmall() {
        return iconSizeSmall;
    }

    public void setIconSizeSmall(float iconSizeSmall) {
        this.iconSizeSmall = iconSizeSmall;
    }

    private float iconSizeSmall;

    private Drawable icon;

    private String label;

    public boolean isShortcut;

    public Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG), bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Rect mTextBound = new Rect();

    private boolean noLabel = false;

    public boolean isNoLabel() {
        return noLabel;
    }

    private boolean vibrateWhenLongPress;

    public boolean isRoundBg() {
        return roundBg;
    }

    public void setRoundBg(boolean roundBg) {
        this.roundBg = roundBg;
    }

    public int getBgColor() {
        return bgColor;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
        bgPaint.setColor(bgColor);
    }

    private boolean roundBg;

    private int bgColor;

    private float labelHeight;

    private int targetedWidth, targetedHeightPadding;

    public float getHeightPadding() {
        return heightPadding;
    }

    private float heightPadding;

    private float horizontalPadding = 8; // In dp

    public AppItemView(Context context) {
        super(context);

        init();
    }

    public AppItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public void setTargetedWidth(int width) {
        targetedWidth = width;
    }

    public void setTargetedHeightPadding(int padding) {
        targetedHeightPadding = padding;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float mWidth = iconSize;
        float mHeight = iconSize + (noLabel ? 0 : labelHeight);
        if (targetedWidth != 0)
            mWidth = targetedWidth;
        setMeasuredDimension((int) Math.ceil(mWidth), (int) Math.ceil((int) mHeight) + Tool.dp2px(2, getContext()) + targetedHeightPadding * 2);
    }

    private void init() {
        if (myType == null)
            myType = Typeface.createFromAsset(getContext().getAssets(), "RobotoCondensed-Regular.ttf");
        setWillNotDraw(false);
        setDrawingCacheEnabled(true);
        setWillNotCacheDrawing(false);

        labelHeight = Tool.dp2px(14, getContext());
        horizontalPadding = Tool.dp2px(horizontalPadding, getContext());

        textPaint.setTextSize(sp2px(getContext(), 14));
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTypeface(myType);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (label != null && !noLabel) {
            textPaint.getTextBounds(label, 0, label.length(), mTextBound);
        }

        //The height should be the same as they have the same text size.
        float mHeight = iconSize + (noLabel ? 0 : labelHeight);
        heightPadding = (getHeight() - mHeight) / 2f;

        if (label != null && !noLabel) {
            float eachTextSize = mTextBound.width() / label.length();
            int charToTruncate = (int) Math.ceil(horizontalPadding / eachTextSize);
            float x = (getWidth() - mTextBound.width()) / 2f;

            if (x < 0)
                x = 0;

            if (mTextBound.width() + horizontalPadding > getWidth())
                canvas.drawText(label.substring(0, label.length() - 3 - charToTruncate) + "..", x + horizontalPadding, getHeight() - heightPadding, textPaint);
            else
                canvas.drawText(label, x, getHeight() - heightPadding, textPaint);
        }

        if (icon != null) {
            canvas.save();
            canvas.translate((getWidth() - iconSize + iconPadding * 2) / 2, heightPadding + iconPadding);
            if (roundBg) {
                canvas.drawCircle((iconSize - iconPadding * 2) / 2, (iconSize - iconPadding * 2) / 2, (iconSize - iconPadding * 2) / 2, bgPaint);
                canvas.translate((iconSize - iconSizeSmall - iconPadding * 2) / 2, (iconSize - iconSizeSmall - iconPadding * 2) / 2);
                icon.setBounds(0, 0, (int) iconSizeSmall, (int) iconSizeSmall);
            } else {
                icon.setBounds(0, 0, (int) iconSize - (int) (iconPadding * 2), (int) iconSize - (int) (iconPadding * 2));
            }
            icon.draw(canvas);
            canvas.restore();
        }
    }

    public static class Builder {
        AppItemView view;

        public Builder(Context context) {
            view = new AppItemView(context);
            float iconSize = Tool.dp2px(LauncherSettings.getInstance(context).generalSettings.iconSize, view.getContext());
            view.setIconSize(iconSize);
        }

        public Builder(AppItemView view) {
            this.view = view;
            float iconSize = Tool.dp2px(LauncherSettings.getInstance(view.getContext()).generalSettings.iconSize, view.getContext());
            view.setIconSize(iconSize);
        }

        public AppItemView getView() {
            return view;
        }

        public Builder setAppItem(AppManager.App app) {
            view.setLabel(app.label);
            view.setIcon(app.icon);
            return this;
        }

        public Builder setLauncherAction(Desktop.Item.Type type) {
            switch (type) {
                case ACTION:
                    int iconSize = LauncherSettings.getInstance(view.getContext()).generalSettings.iconSize;

                    TypedValue typedValue = new TypedValue();
                    view.getContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);

                    view.setIconPadding(Tool.dp2px(4, view.getContext()));
                    view.setIcon(view.getResources().getDrawable(R.drawable.ic_apps_black_24dp));
                    view.setBgColor(Color.WHITE);
                    view.setRoundBg(true);
                    view.setIconSizeSmall(Tool.dp2px(iconSize / 2 - 8, view.getContext()));
                    view.setLabel(resources.getString(R.string.allApps));

                    view.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                            if (Home.launcher != null) {
                                Home.launcher.openAppDrawer(view);
                            }
                        }
                    });
                    break;
            }
            return this;
        }

        public Builder withOnClickLaunchApp(final AppManager.App app) {
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

        public Builder withOnLongPressDrag(final AppManager.App app, final DragAction.Action action, @Nullable final LongPressCallBack eventAction) {
            withOnLongPressDrag(Desktop.Item.newAppItem(app), action, eventAction);
            return this;
        }

        public Builder withOnLongPressDrag(final Desktop.Item item, final DragAction.Action action, @Nullable final LongPressCallBack eventAction) {
            view.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (eventAction != null) {
                        if (!eventAction.readyForDrag(v)) return false;
                    }

                    if (view.vibrateWhenLongPress)
                        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    Intent i = new Intent();
                    i.putExtra("mDragData", item);
                    ClipData data = ClipData.newIntent("mDragIntent", i);
                    v.startDrag(data, new GoodDragShadowBuilder(v), new DragAction(action), 0);
                    if (eventAction != null)
                        eventAction.afterDrag(v);
                    return true;
                }
            });
            return this;
        }

        public interface LongPressCallBack {
            boolean readyForDrag(View view);

            void afterDrag(View view);
        }

        public Builder withOnTouchGetPosition() {
            view.setOnTouchListener(Tool.getItemOnTouchListener());
            return this;
        }

        public Builder setTextColor(@ColorInt int color) {
            view.textPaint.setColor(color);
            return this;
        }

        public Builder setLabelVisibility(boolean visible) {
            view.noLabel = !visible;
            return this;
        }

        public Builder vibrateWhenLongPress() {
            view.vibrateWhenLongPress = true;
            return this;
        }

        public Builder setShortcutItem(final Intent intent) {
            view.isShortcut = true;
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tool.createScaleInScaleOutAnim(view, new Runnable() {
                        @Override
                        public void run() {
                            view.getContext().startActivity(intent);
                        }
                    });
                }
            });
            view.setIcon(Tool.getIconFromID(view.getContext(), intent.getStringExtra("shortCutIconID")));
            view.setLabel(intent.getStringExtra("shortCutName"));
            return this;
        }
    }
}
