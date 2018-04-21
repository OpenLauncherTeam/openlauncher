package com.benny.openlauncher.widget;

import android.content.Context;
import android.graphics.Point;
import android.os.Build.VERSION;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import java.util.List;

import kotlin.jvm.internal.Intrinsics;

public final class Dock extends CellContainer implements DesktopCallBack<View> {
    public int _bottomInset;
    private final Point _coordinate = new Point();
    private Home _home;
    private final Point _previousDragPoint = new Point();
    @Nullable
    private Item _previousItem;
    @Nullable
    private View _previousItemView;
    private float _startPosX;
    private float _startPosY;

    public Dock(@NonNull Context c, @Nullable AttributeSet attr) {
        super(c, attr);
    }

    public void init() {
        if (!isInEditMode()) {
            super.init();
        }
    }

    public final void initDockItem(@NonNull Home home) {

        int columns = Setup.appSettings().getDockSize();
        setGridSize(columns, 1);
        List<Item> dockItems = Home.Companion.getDb().getDock();
        _home = home;
        removeAllViews();
        for (Item item : dockItems) {
            if (item._x < columns && item._y == 0) {
                addItemToPage(item, 0);
            }
        }
    }

    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {

        detectSwipe(ev);
        super.dispatchTouchEvent(ev);
        return true;
    }

    private final void detectSwipe(MotionEvent ev) {
        switch (ev.getAction()) {
            case 0:
                Tool.print("ACTION_DOWN");
                _startPosX = ev.getX();
                _startPosY = ev.getY();
                break;
            case 1:
                Tool.print("ACTION_UP");
                Tool.print(Integer.valueOf((int) ev.getX()), Integer.valueOf((int) ev.getY()));
                if (_startPosY - ev.getY() > 150.0f && Setup.appSettings().getGestureDockSwipeUp()) {
                    Point p = new Point((int) ev.getX(), (int) ev.getY());
                    p = Tool.convertPoint(p, this, _home.getAppDrawerController());
                    if (Setup.appSettings().isGestureFeedback()) {
                        Tool.vibrate(this);
                    }
                    _home.openAppDrawer(this, p.x, p.y);
                    break;
                }
            default:
                break;
        }
    }

    public final void updateIconProjection(int x, int y) {
        Home launcher;
        DragNDropLayout dragNDropView;
        DragState state = peekItemAndSwap(x, y, _coordinate);
        if (!_coordinate.equals(_previousDragPoint)) {
            launcher = _home;
            if (launcher != null) {
                dragNDropView = launcher.getDragNDropView();
                if (dragNDropView != null) {
                    dragNDropView.cancelFolderPreview();
                }
            }
        }
        _previousDragPoint.set(_coordinate.x, _coordinate.y);
        switch (state) {
            case CurrentNotOccupied:
                projectImageOutlineAt(_coordinate, DragNDropHandler._cachedDragBitmap);
                break;
            case OutOffRange:
            case ItemViewNotFound:
                break;
            case CurrentOccupied:
                Object action;
                Home launcher2;
                DragNDropLayout dragNDropView2;
                clearCachedOutlineBitmap();
                launcher = _home;
                if (launcher != null) {
                    dragNDropView = launcher.getDragNDropView();
                    if (dragNDropView != null) {
                        action = dragNDropView.getDragAction();
                        if (!Action.WIDGET.equals(action) || !Action.ACTION.equals(action) && (coordinateToChildView(_coordinate) instanceof AppItemView)) {
                            launcher2 = _home;
                            if (launcher2 != null) {
                                dragNDropView2 = launcher2.getDragNDropView();
                                if (dragNDropView2 != null) {
                                    dragNDropView2.showFolderPreviewAt(this, ((float) getCellWidth()) * (((float) _coordinate.x) + 0.5f), (((float) getCellHeight()) * (((float) _coordinate.y) + 0.5f)) - ((float) (Setup.appSettings().isDockShowLabel() ? Tool.toPx(7) : 0)));
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
                action = null;
                launcher2 = _home;
                if (launcher2 != null) {
                    dragNDropView2 = launcher2.getDragNDropView();
                    if (dragNDropView2 != null) {
                        if (Setup.appSettings().isDockShowLabel()) {
                        }
                        dragNDropView2.showFolderPreviewAt(this, ((float) getCellWidth()) * (((float) _coordinate.x) + 0.5f), (((float) getCellHeight()) * (((float) _coordinate.y) + 0.5f)) - ((float) (Setup.appSettings().isDockShowLabel() ? Tool.toPx(7) : 0)));
                    }
                }
                break;
            default:
                break;
        }
    }

    public void setLastItem(@NonNull Object... args) {
        Item item = (Item) args[0];
        View v = (View) args[1];
        _previousItemView = v;
        _previousItem = item;
        removeView(v);
    }

    @NonNull
    public WindowInsets onApplyWindowInsets(@NonNull WindowInsets insets) {

        if (VERSION.SDK_INT >= 20) {
            _bottomInset = insets.getSystemWindowInsetBottom();
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), _bottomInset);
        }
        return insets;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!isInEditMode()) {
            int height;
            int height2 = View.getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
            int iconSize = Setup.appSettings().getDockIconSize();
            if (Setup.appSettings().isDockShowLabel()) {
                height = Tool.dp2px(((16 + iconSize) + 14) + 10, getContext()) + _bottomInset;
            } else {
                height = Tool.dp2px((16 + iconSize) + 10, getContext()) + _bottomInset;
            }
            getLayoutParams().height = height;
            setMeasuredDimension(View.getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), height);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void consumeRevert() {
        _previousItem = (Item) null;
        _previousItemView = (View) null;
    }

    public void revertLastItem() {
        if (_previousItemView != null) {
            View view = _previousItemView;
            if (view == null) {
                Intrinsics.throwNpe();
            }
            addViewToGrid(view);
            _previousItem = (Item) null;
            _previousItemView = (View) null;
        }
    }

    public boolean addItemToPage(@NonNull Item item, int page) {

        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.appSettings().isDockShowLabel(), (DesktopCallBack) this, Setup.appSettings().getDockIconSize());
        if (itemView == null) {
            Home.Companion.getDb().deleteItem(item, true);
            return false;
        }
        item._locationInLauncher = 1;
        addViewToGrid(itemView, item._x, item._y, item._spanX, item._spanY);
        return true;
    }

    public boolean addItemToPoint(@NonNull Item item, int x, int y) {

        LayoutParams positionToLayoutPrams = coordinateToLayoutParams(x, y, item._spanX, item._spanY);
        if (positionToLayoutPrams == null) {
            return false;
        }
        item._locationInLauncher = 1;
        item._x = positionToLayoutPrams.getX();
        item._y = positionToLayoutPrams.getY();
        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.appSettings().isDockShowLabel(), (DesktopCallBack) this, Setup.appSettings().getDockIconSize());
        if (itemView != null) {
            itemView.setLayoutParams(positionToLayoutPrams);
            addView(itemView);
        }
        return true;
    }

    public boolean addItemToCell(@NonNull Item item, int x, int y) {

        item._locationInLauncher = 1;
        item._x = x;
        item._y = y;
        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.appSettings().isDockShowLabel(), (DesktopCallBack) this, Setup.appSettings().getDockIconSize());
        if (itemView == null) {
            return false;
        }
        addViewToGrid(itemView, item._x, item._y, item._spanX, item._spanY);
        return true;
    }

    public void removeItem(final View view, boolean animate) {

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

    public int getBottomInset() {
        return _bottomInset;
    }

    public void setHome(Home home) {
        _home = home;
    }
}