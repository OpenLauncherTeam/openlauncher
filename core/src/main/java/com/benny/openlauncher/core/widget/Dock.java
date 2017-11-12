package com.benny.openlauncher.core.widget;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Toast;

import com.benny.openlauncher.core.R;
import com.benny.openlauncher.core.activity.CoreHome;
import com.benny.openlauncher.core.manager.Setup;
import com.benny.openlauncher.core.model.Item;
import com.benny.openlauncher.core.util.Definitions;
import com.benny.openlauncher.core.util.DragAction;
import com.benny.openlauncher.core.util.DragNDropHandler;
import com.benny.openlauncher.core.util.Tool;
import com.benny.openlauncher.core.viewutil.DesktopCallBack;
import com.benny.openlauncher.core.viewutil.ItemViewFactory;

import java.util.List;

public class Dock extends CellContainer implements View.OnDragListener, DesktopCallBack {
    public static int bottomInset;
    public View previousItemView;
    public Item previousItem;
    private float startPosX, startPosY;
    private CoreHome home;
    private Point coordinate = new Point();

    public Dock(Context c) {
        this(c, null);
    }

    public Dock(Context c, AttributeSet attr) {
        super(c, attr);
    }

    @Override
    public void init() {
        if (isInEditMode()) {
            return;
        }
        setOnDragListener(this);
        super.init();
    }

    public void initDockItem(CoreHome home) {
        int columns = Setup.Companion.appSettings().getDockSize();
        setGridSize(columns, 1);
        List<Item> dockItems = home.Companion.getDb().getDock();

        this.home = home;
        removeAllViews();
        for (Item item : dockItems) {
            if (item.getX() < columns && item.getY() == 0) {
                addItemToPage(item, 0);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        detectSwipe(ev);
        super.dispatchTouchEvent(ev);
        return true;
    }

    private void detectSwipe(MotionEvent ev) {
        if (CoreHome.Companion.getLauncher() == null) return;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                Tool.INSTANCE.print("ACTION_UP");
                float minDist = 150f;
                Tool.INSTANCE.print((int) ev.getX(), (int) ev.getY());
                if (startPosY - ev.getY() > minDist) {
                    if (Setup.Companion.appSettings().getGestureDockSwipeUp()) {
                        Point p = Tool.INSTANCE.convertPoint(new Point((int) ev.getX(), (int) ev.getY()), this, CoreHome.Companion.getLauncher().getAppDrawerController());
                        if (Setup.Companion.appSettings().isGestureFeedback())
                            Tool.INSTANCE.vibrate(this);
                        CoreHome.Companion.getLauncher().openAppDrawer(this, p.x, p.y);
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
                Tool.INSTANCE.print("ACTION_DOWN");
                startPosX = ev.getX();
                startPosY = ev.getY();
                break;
        }
    }

    @Override
    public boolean onDrag(View p1, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                clearCachedOutlineBitmap();
                if (((DragAction) event.getLocalState()).action == DragAction.Action.WIDGET) {
                    return false;
                }
                return true;
            case DragEvent.ACTION_DRAG_LOCATION:
                updateIconProjection(((int) event.getX()), ((int) event.getY()));
                return true;
            case DragEvent.ACTION_DRAG_ENTERED:
                return true;
            case DragEvent.ACTION_DRAG_EXITED:
                clearCachedOutlineBitmap();
                return true;
            case DragEvent.ACTION_DROP:
                clearCachedOutlineBitmap();

                Item item = DragNDropHandler.INSTANCE.getDraggedObject(event);

                // this statement makes sure that adding an app multiple times from the app drawer works
                // the app will get a new id every time
                if (((DragAction) event.getLocalState()).action == DragAction.Action.APP_DRAWER) {
                    item.reset();
                }

                if (addItemToPoint(item, (int) event.getX(), (int) event.getY())) {
                    home.getDesktop().consumeRevert();
                    home.getDock().consumeRevert();

                    // add the item to the database
                    home.Companion.getDb().saveItem(item, 0, Definitions.ItemPosition.Dock);
                } else {
                    Point pos = new Point();
                    touchPosToCoordinate(pos, (int) event.getX(), (int) event.getY(), item.getSpanX(), item.getSpanY(), false);
                    View itemView = coordinateToChildView(pos);
                    if (itemView != null) {
                        if (Desktop.Companion.handleOnDropOver(home, item, (Item) itemView.getTag(), itemView, this, 0, Definitions.ItemPosition.Dock, this)) {
                            home.getDesktop().consumeRevert();
                            home.getDock().consumeRevert();
                        } else {
                            Toast.makeText(getContext(), R.string.toast_not_enough_space, Toast.LENGTH_SHORT).show();
                            home.getDock().revertLastItem();
                            home.getDesktop().revertLastItem();
                        }
                    } else {
                        Toast.makeText(getContext(), R.string.toast_not_enough_space, Toast.LENGTH_SHORT).show();
                        home.getDock().revertLastItem();
                        home.getDesktop().revertLastItem();
                    }
                }
                return true;
            case DragEvent.ACTION_DRAG_ENDED:
                clearCachedOutlineBitmap();
                return true;
        }

        invalidate();
        return false;
    }

    private void updateIconProjection(int x, int y) {
        CellContainer.DragState state = peekItemAndSwap(x, y, coordinate);
        switch (state) {
            case CurrentNotOccupied:
                projectImageOutlineAt(coordinate, DragNDropHandler.INSTANCE.getCachedDragBitmap());
                break;
            case OutOffRange:
                break;
            case ItemViewNotFound:
                break;
            case CurrentOccupied:
                clearCachedOutlineBitmap();
                break;
        }
    }

    @Override
    public void setLastItem(Object... args) {
        // args stores the item in [0] and the view reference in [1]
        View v = (View) args[1];
        Item item = (Item) args[0];

        previousItemView = v;
        previousItem = item;
        removeView(v);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            bottomInset = insets.getSystemWindowInsetBottom();
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), bottomInset);
        }
        return insets;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        int iconSize = Setup.Companion.appSettings().getDockIconSize();
        if (Setup.Companion.appSettings().isDockShowLabel()) {
            height = Tool.INSTANCE.dp2px(16 + iconSize + 14 + 10, getContext()) + Dock.bottomInset;
        } else {
            height = Tool.INSTANCE.dp2px(16 + iconSize + 10, getContext()) + Dock.bottomInset;
        }
        getLayoutParams().height = height;

        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), height);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void consumeRevert() {
        previousItem = null;
        previousItemView = null;
    }

    @Override
    public void revertLastItem() {
        if (previousItemView != null) {
            addViewToGrid(previousItemView);
            previousItem = null;
            previousItemView = null;
        }
    }

    @Override
    public boolean addItemToPage(final Item item, int page) {
        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.Companion.appSettings().isDockShowLabel(), this, Setup.Companion.appSettings().getDockIconSize());

        if (itemView == null) {
            home.Companion.getDb().deleteItem(item, true);
            return false;
        } else {
            item.locationInLauncher = Item.LOCATION_DOCK;
            addViewToGrid(itemView, item.getX(), item.getY(), item.getSpanX(), item.getSpanY());
            return true;
        }
    }

    @Override
    public boolean addItemToPoint(final Item item, int x, int y) {
        CellContainer.LayoutParams positionToLayoutPrams = coordinateToLayoutParams(x, y, item.getSpanX(), item.getSpanY());
        if (positionToLayoutPrams != null) {
            item.locationInLauncher = Item.LOCATION_DOCK;

            item.setX(positionToLayoutPrams.getX());
            item.setY(positionToLayoutPrams.getY());

            View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.Companion.appSettings().isDockShowLabel(), this, Setup.Companion.appSettings().getDockIconSize());

            if (itemView != null) {
                itemView.setLayoutParams(positionToLayoutPrams);
                addView(itemView);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addItemToCell(final Item item, int x, int y) {
        item.locationInLauncher = Item.LOCATION_DOCK;

        item.setX(x);
        item.setY(y);

        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.Companion.appSettings().isDockShowLabel(), this, Setup.Companion.appSettings().getDockIconSize());

        if (itemView != null) {
            addViewToGrid(itemView, item.getX(), item.getY(), item.getSpanX(), item.getSpanY());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void removeItem(View view) {
        removeViewInLayout(view);
    }
}
