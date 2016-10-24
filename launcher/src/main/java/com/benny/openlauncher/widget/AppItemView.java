package com.benny.openlauncher.widget;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.GoodDragShadowBuilder;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.Tool;

/**
 * Created by BennyKok on 10/23/2016.
 */

public class AppItemView extends View implements Drawable.Callback{

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon, boolean isStatic) {
        this.icon = icon;
        this.icon.setCallback(this);
        invalidate();
    }

    @Override
    public void refreshDrawableState() {
        invalidateDrawable(icon);
        super.refreshDrawableState();
    }

    @Override
    public void invalidateDrawable(Drawable drawable) {
        invalidate();
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

    private float iconSize;

    private Drawable icon;
    private String label;

    public boolean isShortcut;

    public  Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Rect mTextBound = new Rect();

    private boolean noLabel,vibrateWhenLongPress;

    private float labelHeight;

    public AppItemView(Context context) {
        super(context);

        init();
    }

    public AppItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init(){
        setWillNotDraw(false);

        labelHeight = Tool.convertDpToPixel(14,getContext());

        textPaint.setTextSize(sp2px(getContext(),14));
        textPaint.setColor(Color.DKGRAY);
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

        if (label != null && !noLabel){
            textPaint.getTextBounds(label,0,label.length(),mTextBound);
        }

        //The height should be the same as they have the same text size.
        float mHeight = iconSize + (noLabel? 0 : labelHeight);
        float heightPadding = (getHeight() - mHeight)/2f;

        if (label != null && !noLabel) {
            float x = (getWidth()-mTextBound.width())/2f;
            if (x < 0)
                x = 0;
            canvas.drawText(label,x, getHeight() - heightPadding, textPaint);
        }

        if (icon != null){
            canvas.save();
            canvas.translate((getWidth()-iconSize)/2,heightPadding);
            icon.setBounds(0,0,(int)iconSize,(int)iconSize);
            icon.draw(canvas);
            canvas.restore();
        }
    }

    public static class Builder{
        AppItemView view;

        public Builder(Context context){
            view = new AppItemView(context);
            float iconSize = Tool.convertDpToPixel(LauncherSettings.getInstance(view.getContext()).generalSettings.iconSize, view.getContext());
            view.setIconSize(iconSize);
        }

        public AppItemView getView(){
            return view;
        }

        public Builder setAppItem(AppManager.App app){
            view.setIcon(app.icon,true);
            view.setLabel(app.appName);
            return this;
        }

        public Builder withOnClickLaunchApp(final AppManager.App app){
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

        public Builder withOnLongClickDrag(final AppManager.App app,final DragAction.Action action,@Nullable final OnLongClickListener eventAction){
            withOnLongClickDrag(Desktop.Item.newAppItem(app),action,eventAction);
            return this;
        }

        public Builder withOnLongClickDrag(final Desktop.Item item, final DragAction.Action action, @Nullable final OnLongClickListener eventAction){
            view.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (view.vibrateWhenLongPress)
                        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    Intent i = new Intent();
                    i.putExtra("mDragData", item);
                    ClipData data = ClipData.newIntent("mDragIntent", i);
                    v.startDrag(data, new GoodDragShadowBuilder(v), new DragAction(action), 0);
                    if (eventAction != null)
                        eventAction.onLongClick(v);
                    return true;
                }
            });
            return this;
        }

        public Builder withOnTouchGetPosition(){
            view.setOnTouchListener(Tool.getItemOnTouchListener());
            return this;
        }

        public Builder setTextColor(@ColorInt int color){
            view.textPaint.setColor(color);
            return this;
        }

        public Builder setNoLabel(){
            view.noLabel = true;
            return this;
        }

        public Builder vibrateWhenLongPress(){
            view.vibrateWhenLongPress = true;
            return this;
        }

        public Builder setShortcutItem(final Intent intent){
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
            view.setIcon(Tool.getIconFromID(view.getContext(),intent.getStringExtra("shortCutIconID")),true);
            view.setLabel(intent.getStringExtra("shortCutName"));
            return this;
        }
    }
}
