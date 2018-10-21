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

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.DragAction.Action;
import com.benny.openlauncher.util.DragHandler;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DesktopCallback;
import com.benny.openlauncher.viewutil.ItemViewFactory;

import java.util.List;

public final class Dock extends CellContainer implements DesktopCallback<View> {
    private HomeActivity _homeActivity;
    private final Point _coordinate = new Point();
    private final Point _previousDragPoint = new Point();

    private Item _previousItem;
    private View _previousItemView;

    // open app drawer on slide up gesture
    private float _startPosX;
    private float _startPosY;

    public Dock(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public void init() {
        if (!isInEditMode()) {
            super.init();
        }
    }

    public final void initDockItem(HomeActivity homeActivity) {
        int columns = Setup.appSettings().getDockColumnCount();
        int rows = Setup.appSettings().getDockRowCount();
        setGridSize(columns, rows);
        List<Item> dockItems = HomeActivity._db.getDock();
        _homeActivity = homeActivity;
        removeAllViews();
        for (Item item : dockItems) {
            if (item._x + item._spanX <= columns && item._y + item._spanY <= rows) {
                addItemToPage(item, 0);
            }
        }

        // call onMeasure to set the height
        measure(getMeasuredWidth(), getMeasuredHeight());
    }

    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        detectSwipe(ev);
        super.dispatchTouchEvent(ev);
        return true;
    }

    private void detectSwipe(MotionEvent ev) {
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
                    p = Tool.convertPoint(p, this, _homeActivity.getAppDrawerController());
                    if (Setup.appSettings().isGestureFeedback()) {
                        Tool.vibrate(this);
                    }
                    _homeActivity.openAppDrawer(this, p.x, p.y);
                    break;
                }
            default:
                break;
        }
    }

    public final void updateIconProjection(int x, int y) {
        HomeActivity launcher;
        ItemOptionView dragNDropView;
        DragState state = peekItemAndSwap(x, y, _coordinate);
        if (!_coordinate.equals(_previousDragPoint)) {
            launcher = _homeActivity;
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
                projectImageOutlineAt(_coordinate, DragHandler._cachedDragBitmap);
                break;
            case OutOffRange:
            case ItemViewNotFound:
                break;
            case CurrentOccupied:
                clearCachedOutlineBitmap();
                dragNDropView = _homeActivity.getDragNDropView();
                Object action = dragNDropView.getDragAction();
                if (!Action.WIDGET.equals(action) || !Action.ACTION.equals(action) && (coordinateToChildView(_coordinate) instanceof AppItemView)) {
                    dragNDropView.showFolderPreviewAt(this, getCellWidth() * (_coordinate.x + 0.5f), getCellHeight() * (_coordinate.y + 0.5f) - (Setup.appSettings().isDockShowLabel() ? Tool.toPx(7) : 0));
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

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!isInEditMode()) {
            // set the height for the dock based on the number of rows and the show label preference
            int iconSize = Setup.appSettings().getDockIconSize();
            int height = Tool.dp2px((iconSize + 60) * getCellSpanV(), _homeActivity);
            if (Setup.appSettings().isDockShowLabel()) height += Tool.dp2px(20, getContext());
            getLayoutParams().height = height;
            setMeasuredDimension(View.getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), height);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void consumeRevert() {
        _previousItem = null;
        _previousItemView = null;
    }

    public void revertLastItem() {
        if (_previousItemView != null && _previousItem != null) {
            addViewToGrid(_previousItemView);
            _previousItem = null;
            _previousItemView = null;
        }
    }

    public boolean addItemToPage(@NonNull Item item, int page) {
        View itemView = ItemViewFactory.getItemView(getContext(), this, item, Setup.appSettings().getDockIconSize(), Setup.appSettings().isDockShowLabel());
        if (itemView == null) {
            HomeActivity._db.deleteItem(item, true);
            return false;
        }
        item._location = Item.LOCATION_DOCK;
        addViewToGrid(itemView, item._x, item._y, item._spanX, item._spanY);
        return true;
    }

    public boolean addItemToPoint(@NonNull Item item, int x, int y) {
        LayoutParams positionToLayoutPrams = coordinateToLayoutParams(x, y, item._spanX, item._spanY);
        if (positionToLayoutPrams == null) {
            return false;
        }
        item._location = Item.LOCATION_DOCK;
        item._x = positionToLayoutPrams.getX();
        item._y = positionToLayoutPrams.getY();
        View itemView = ItemViewFactory.getItemView(getContext(), this, item, Setup.appSettings().getDockIconSize(), Setup.appSettings().isDockShowLabel());
        if (itemView != null) {
            itemView.setLayoutParams(positionToLayoutPrams);
            addView(itemView);
        }
        return true;
    }

    public boolean addItemToCell(@NonNull Item item, int x, int y) {
        item._location = Item.LOCATION_DOCK;
        item._x = x;
        item._y = y;
        View itemView = ItemViewFactory.getItemView(getContext(), this, item, Setup.appSettings().getDockIconSize(), Setup.appSettings().isDockShowLabel());
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
        } else if (this.equals(view.getParent())) {
            removeView(view);
        }
    }

    public void setHome(HomeActivity homeActivity) {
        _homeActivity = homeActivity;
    }
}
