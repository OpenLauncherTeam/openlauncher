package com.benny.openlauncher.widget;

import android.content.Context;
import android.graphics.Point;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;

import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.DragAction.Action;
import com.benny.openlauncher.util.DragNDropHandler;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DesktopCallBack;
import com.benny.openlauncher.viewutil.ItemViewFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

import kotlin.jvm.JvmOverloads;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: Dock.kt */
public final class Dock extends CellContainer implements DesktopCallBack<View> {
    public static final Companion Companion = new Companion();
    public static int bottomInset;
    private HashMap _$_findViewCache;
    private final Point coordinate = new Point();
    private Home home;
    private final Point previousDragPoint = new Point();
    @Nullable
    private Item previousItem;
    @Nullable
    private View previousItemView;
    private float startPosX;
    private float startPosY;

    public static final class Companion {
        private Companion() {
        }

        public final int getBottomInset() {
            return Dock.bottomInset;
        }

        public final void setBottomInset(int v) {
            Dock.bottomInset = v;
        }
    }

    public void _$_clearFindViewByIdCache() {
        if (this._$_findViewCache != null) {
            this._$_findViewCache.clear();
        }
    }

    public View _$_findCachedViewById(int i) {
        if (this._$_findViewCache == null) {
            this._$_findViewCache = new HashMap();
        }
        View view = (View) this._$_findViewCache.get(Integer.valueOf(i));
        if (view != null) {
            return view;
        }
        view = findViewById(i);
        this._$_findViewCache.put(Integer.valueOf(i), view);
        return view;
    }

    @JvmOverloads
    public Dock(@NotNull Context c, @Nullable AttributeSet attr) {
        super(c, attr);
    }

    public void init() {
        if (!isInEditMode()) {
            super.init();
        }
    }

    public final void initDockItem(@NotNull Home home) {
        Intrinsics.checkParameterIsNotNull(home, "home");
        int columns = Setup.appSettings().getDockSize();
        setGridSize(columns, 1);
        List<Item> dockItems = Home.Companion.getDb().getDock();
        this.home = home;
        removeAllViews();
        for (Item item : dockItems) {
            if (item.x < columns && item.y == 0) {
                addItemToPage(item, 0);
            }
        }
    }

    public boolean dispatchTouchEvent(@NotNull MotionEvent ev) {
        Intrinsics.checkParameterIsNotNull(ev, "ev");
        detectSwipe(ev);
        super.dispatchTouchEvent(ev);
        return true;
    }

    private final void detectSwipe(MotionEvent ev) {
        if (Home.Companion.getLauncher() != null) {
            switch (ev.getAction()) {
                case 0:
                    Tool.print((Object) "ACTION_DOWN");
                    this.startPosX = ev.getX();
                    this.startPosY = ev.getY();
                    break;
                case 1:
                    Tool.print((Object) "ACTION_UP");
                    Tool.print(Integer.valueOf((int) ev.getX()), Integer.valueOf((int) ev.getY()));
                    if (this.startPosY - ev.getY() > 150.0f && Setup.appSettings().getGestureDockSwipeUp()) {
                        Point p = new Point((int) ev.getX(), (int) ev.getY());
                        View view = this;
                        Home launcher = Home.Companion.getLauncher();
                        if (launcher == null) {
                            Intrinsics.throwNpe();
                        }
                        p = Tool.convertPoint(p, view, launcher.getAppDrawerController());
                        if (Setup.appSettings().isGestureFeedback()) {
                            Tool.vibrate(this);
                        }
                        Home launcher2 = Home.Companion.getLauncher();
                        if (launcher2 == null) {
                            Intrinsics.throwNpe();
                        }
                        launcher2.openAppDrawer(this, p.x, p.y);
                        break;
                    }
                default:
                    break;
            }
        }
    }

    public final void updateIconProjection(int x, int y) {
        Home launcher;
        DragNDropLayout dragNDropView;
        DragState state = peekItemAndSwap(x, y, this.coordinate);
        if (!coordinate.equals(previousDragPoint)) {
            launcher = Home.Companion.getLauncher();
            if (launcher != null) {
                dragNDropView = launcher.getDragNDropView();
                if (dragNDropView != null) {
                    dragNDropView.cancelFolderPreview();
                }
            }
        }
        previousDragPoint.set(this.coordinate.x, this.coordinate.y);
        switch (state) {
            case CurrentNotOccupied:
                projectImageOutlineAt(this.coordinate, DragNDropHandler.cachedDragBitmap);
                break;
            case OutOffRange:
            case ItemViewNotFound:
                break;
            case CurrentOccupied:
                Object action;
                Home launcher2;
                DragNDropLayout dragNDropView2;
                clearCachedOutlineBitmap();
                launcher = Home.Companion.getLauncher();
                if (launcher != null) {
                    dragNDropView = launcher.getDragNDropView();
                    if (dragNDropView != null) {
                        action = dragNDropView.getDragAction();
                        if (!Action.WIDGET.equals(action) || !Action.ACTION.equals(action) && (coordinateToChildView(coordinate) instanceof AppItemView)) {
                            launcher2 = Home.Companion.getLauncher();
                            if (launcher2 != null) {
                                dragNDropView2 = launcher2.getDragNDropView();
                                if (dragNDropView2 != null) {
                                    dragNDropView2.showFolderPreviewAt(this, ((float) getCellWidth()) * (((float) this.coordinate.x) + 0.5f), (((float) getCellHeight()) * (((float) this.coordinate.y) + 0.5f)) - ((float) (Setup.appSettings().isDockShowLabel() ? Tool.toPx(7) : 0)));
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
                action = null;
                launcher2 = Home.Companion.getLauncher();
                if (launcher2 != null) {
                    dragNDropView2 = launcher2.getDragNDropView();
                    if (dragNDropView2 != null) {
                        if (Setup.appSettings().isDockShowLabel()) {
                        }
                        dragNDropView2.showFolderPreviewAt(this, ((float) getCellWidth()) * (((float) this.coordinate.x) + 0.5f), (((float) getCellHeight()) * (((float) this.coordinate.y) + 0.5f)) - ((float) (Setup.appSettings().isDockShowLabel() ? Tool.toPx(7) : 0)));
                    }
                }
                break;
            default:
                break;
        }
    }

    public void setLastItem(@NotNull Object... args) {
        Item item = (Item) args[0];
        View v = (View) args[1];
        this.previousItemView = v;
        this.previousItem = item;
        removeView(v);
    }

    @NotNull
    public WindowInsets onApplyWindowInsets(@NotNull WindowInsets insets) {
        Intrinsics.checkParameterIsNotNull(insets, "insets");
        if (VERSION.SDK_INT >= 20) {
            Companion.setBottomInset(insets.getSystemWindowInsetBottom());
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), Companion.getBottomInset());
        }
        return insets;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!isInEditMode()) {
            int height;
            int height2 = View.getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
            int iconSize = Setup.appSettings().getDockIconSize();
            if (Setup.appSettings().isDockShowLabel()) {
                height = Tool.dp2px(((16 + iconSize) + 14) + 10, getContext()) + Companion.getBottomInset();
            } else {
                height = Tool.dp2px((16 + iconSize) + 10, getContext()) + Companion.getBottomInset();
            }
            getLayoutParams().height = height;
            setMeasuredDimension(View.getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), height);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void consumeRevert() {
        this.previousItem = (Item) null;
        this.previousItemView = (View) null;
    }

    public void revertLastItem() {
        if (this.previousItemView != null) {
            View view = this.previousItemView;
            if (view == null) {
                Intrinsics.throwNpe();
            }
            addViewToGrid(view);
            this.previousItem = (Item) null;
            this.previousItemView = (View) null;
        }
    }

    public boolean addItemToPage(@NotNull Item item, int page) {
        Intrinsics.checkParameterIsNotNull(item, "item");
        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.appSettings().isDockShowLabel(), (DesktopCallBack) this, Setup.appSettings().getDockIconSize());
        if (itemView == null) {
            Home.Companion.getDb().deleteItem(item, true);
            return false;
        }
        item.locationInLauncher = 1;
        addViewToGrid(itemView, item.x, item.y, item.spanX, item.spanY);
        return true;
    }

    public boolean addItemToPoint(@NotNull Item item, int x, int y) {
        Intrinsics.checkParameterIsNotNull(item, "item");
        LayoutParams positionToLayoutPrams = coordinateToLayoutParams(x, y, item.spanX, item.spanY);
        if (positionToLayoutPrams == null) {
            return false;
        }
        item.locationInLauncher = 1;
        item.x = positionToLayoutPrams.getX();
        item.y = positionToLayoutPrams.getY();
        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.appSettings().isDockShowLabel(), (DesktopCallBack) this, Setup.appSettings().getDockIconSize());
        if (itemView != null) {
            itemView.setLayoutParams(positionToLayoutPrams);
            addView(itemView);
        }
        return true;
    }

    public boolean addItemToCell(@NotNull Item item, int x, int y) {
        Intrinsics.checkParameterIsNotNull(item, "item");
        item.locationInLauncher = 1;
        item.x = x;
        item.y = y;
        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.appSettings().isDockShowLabel(), (DesktopCallBack) this, Setup.appSettings().getDockIconSize());
        if (itemView == null) {
            return false;
        }
        addViewToGrid(itemView, item.x, item.y, item.spanX, item.spanY);
        return true;
    }

    public void removeItem(final View view, boolean animate) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        if (animate) {
            view.animate().setDuration(100).scaleX(0.0f).scaleY(0.0f).withEndAction(new Runnable() {
                @Override
                public void run() {
                    if (view.getParent().equals(Dock.this)) {
                        removeView(view);
                    }
                }
            });
        } else if (Intrinsics.areEqual(view.getParent(), (Object) this)) {
            removeView(view);
        }
    }
}