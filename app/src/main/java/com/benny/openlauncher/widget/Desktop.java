package com.benny.openlauncher.widget;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.model.Item.Type;
import com.benny.openlauncher.util.Definitions.ItemPosition;
import com.benny.openlauncher.util.Definitions.ItemState;
import com.benny.openlauncher.util.DragAction.Action;
import com.benny.openlauncher.util.DragHandler;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DesktopCallback;
import com.benny.openlauncher.viewutil.DesktopGestureListener;
import com.benny.openlauncher.viewutil.ItemViewFactory;
import com.benny.openlauncher.widget.CellContainer.DragState;

import java.util.ArrayList;
import java.util.List;

import in.championswimmer.sfg.lib.SimpleFingerGestures;
import in.championswimmer.sfg.lib.SimpleFingerGestures.OnFingerGestureListener;

public final class Desktop extends ViewPager implements DesktopCallback<View> {
    private OnDesktopEditListener _desktopEditListener;
    private boolean _inEditMode;
    private int _pageCount;
    private PagerIndicator _pageIndicator;

    private final List<CellContainer> _pages = new ArrayList<>();
    private final Point _previousDragPoint = new Point();

    private Point _coordinate = new Point(-1, -1);
    private DesktopAdapter _adapter;
    private Item _previousItem;
    private View _previousItemView;
    private int _previousPage;

    public static boolean handleOnDropOver(HomeActivity homeActivity, Item dropItem, Item item, View itemView, CellContainer parent, int page, ItemPosition itemPosition, DesktopCallback callback) {
        if (item != null) {
            if (dropItem != null) {
                Type type = item._type;
                if (type != null) {
                    switch (type) {
                        case APP:
                        case SHORTCUT:
                            if (Type.APP.equals(dropItem._type) || Type.SHORTCUT.equals(dropItem._type)) {
                                parent.removeView(itemView);
                                Item group = Item.newGroupItem();
                                group.getGroupItems().add(item);
                                group.getGroupItems().add(dropItem);
                                group._x = item._x;
                                group._y = item._y;
                                HomeActivity._db.saveItem(dropItem, page, itemPosition);
                                HomeActivity._db.saveItem(item, ItemState.Hidden);
                                HomeActivity._db.saveItem(dropItem, ItemState.Hidden);
                                HomeActivity._db.saveItem(group, page, itemPosition);
                                callback.addItemToPage(group, page);
                                HomeActivity launcher = HomeActivity.Companion.getLauncher();
                                if (launcher != null) {
                                    launcher.getDesktop().consumeRevert();
                                    launcher.getDock().consumeRevert();
                                }
                                return true;
                            }
                        case GROUP:
                            if ((Item.Type.APP.equals(dropItem._type) || Type.SHORTCUT.equals(dropItem._type)) && item.getGroupItems().size() < GroupPopupView.GroupDef._maxItem) {
                                parent.removeView(itemView);
                                item.getGroupItems().add(dropItem);
                                HomeActivity._db.saveItem(dropItem, page, itemPosition);
                                HomeActivity._db.saveItem(dropItem, ItemState.Hidden);
                                HomeActivity._db.saveItem(item, page, itemPosition);
                                callback.addItemToPage(item, page);
                                HomeActivity launcher = HomeActivity.Companion.getLauncher();
                                if (launcher != null) {
                                    launcher.getDesktop().consumeRevert();
                                    launcher.getDock().consumeRevert();
                                }
                                return true;
                            }
                        default:
                            break;
                    }
                }
                return false;
            }
        }
        return false;
    }

    public final class DesktopAdapter extends PagerAdapter {
        private MotionEvent _currentEvent;
        private final Desktop _desktop;

        public DesktopAdapter(Desktop desktop) {
            _desktop = desktop;
            _desktop.getPages().clear();
            int count = getCount();
            for (int i = 0; i < count; i++) {
                _desktop.getPages().add(getItemLayout());
            }
        }

        private OnFingerGestureListener getGestureListener() {
            return new DesktopGestureListener(_desktop, Setup.desktopGestureCallback());
        }

        private CellContainer getItemLayout() {
            Context context = _desktop.getContext();
            CellContainer layout = new CellContainer(context);
            SimpleFingerGestures mySfg = new SimpleFingerGestures();
            mySfg.setOnFingerGestureListener(getGestureListener());
            layout.setGestures(mySfg);
            layout.setGridSize(Setup.appSettings().getDesktopColumnCount(), Setup.appSettings().getDesktopRowCount());
            layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    exitDesktopEditMode();
                }
            });
            layout.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    enterDesktopEditMode();
                    return true;
                }
            });
            return layout;
        }

        public void addPageLeft() {
            _desktop.getPages().add(0, getItemLayout());
            notifyDataSetChanged();
        }

        public void addPageRight() {
            _desktop.getPages().add(getItemLayout());
            notifyDataSetChanged();
        }

        public void removePage(int position, boolean deleteItems) {
            if (deleteItems) {
                for (View view : _desktop.getPages().get(position).getAllCells()) {
                    Object item = view.getTag();
                    if (item instanceof Item) {
                        HomeActivity._db.deleteItem((Item) item, true);
                    }
                }
            }
            _desktop.getPages().remove(position);
            notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return _desktop.getPageCount();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return super.getPageTitle(position);
        }

        @Override
        public boolean isViewFromObject(View p1, Object p2) {
            return p1 == p2;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            CellContainer layout = _desktop.getPages().get(position);
            container.addView(layout);
            return layout;
        }

        public void enterDesktopEditMode() {
            float scaleFactor = 0.8f;
            float translateFactor = (float) Tool.toPx(Setup.appSettings().getSearchBarEnable() ? 20 : 40);
            for (CellContainer v : _desktop.getPages()) {
                v.setBlockTouch(true);
                v.animateBackgroundShow();
                ViewPropertyAnimator translationY = v.animate().scaleX(scaleFactor).scaleY(scaleFactor).translationY(translateFactor);
                translationY.setInterpolator(new AccelerateDecelerateInterpolator());
            }
            _desktop.setInEditMode(true);
            if (_desktop.getDesktopEditListener() != null) {
                OnDesktopEditListener desktopEditListener = _desktop.getDesktopEditListener();
                desktopEditListener.onDesktopEdit();
            }
        }

        public void exitDesktopEditMode() {
            float scaleFactor = 1.0f;
            float translateFactor = 0.0f;
            for (CellContainer v : _desktop.getPages()) {
                v.setBlockTouch(false);
                v.animateBackgroundHide();
                ViewPropertyAnimator translationY = v.animate().scaleX(scaleFactor).scaleY(scaleFactor).translationY(translateFactor);
                translationY.setInterpolator(new AccelerateDecelerateInterpolator());
            }
            _desktop.setInEditMode(false);
            if (_desktop.getDesktopEditListener() != null) {
                OnDesktopEditListener desktopEditListener = _desktop.getDesktopEditListener();
                desktopEditListener.onFinishDesktopEdit();
            }
        }
    }

    public Desktop(Context context) {
        super(context, null);
    }

    public Desktop(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public final List<CellContainer> getPages() {
        return _pages;
    }

    public final OnDesktopEditListener getDesktopEditListener() {
        return _desktopEditListener;
    }

    public final void setDesktopEditListener(@Nullable OnDesktopEditListener v) {
        _desktopEditListener = v;
    }

    public final boolean getInEditMode() {
        return _inEditMode;
    }

    public final void setInEditMode(boolean v) {
        _inEditMode = v;
    }

    public final int getPageCount() {
        return _pageCount;
    }

    public final boolean isCurrentPageEmpty() {
        return getCurrentPage().getChildCount() == 0;
    }

    public final CellContainer getCurrentPage() {
        return _pages.get(getCurrentItem());
    }

    public final void setPageIndicator(PagerIndicator pageIndicator) {
        _pageIndicator = pageIndicator;
    }

    public final void init() {
        _pageCount = HomeActivity._db.getDesktop().size();
        if (_pageCount == 0) {
            _pageCount = 1;
        }
        setCurrentItem(Setup.appSettings().getDesktopPageCurrent());
    }

    public final void initDesktop() {
        _adapter = new DesktopAdapter(this);
        setAdapter(_adapter);
        if (Setup.appSettings().isDesktopShowIndicator() && _pageIndicator != null) {
            _pageIndicator.setViewPager(this);
        }
        int columns = Setup.appSettings().getDesktopColumnCount();
        int rows = Setup.appSettings().getDesktopRowCount();
        List<List<Item>> desktopItems = HomeActivity._db.getDesktop();
        for (int pageCount = 0; pageCount < desktopItems.size(); pageCount++) {
            List<Item> page = desktopItems.get(pageCount);
            _pages.get(pageCount).removeAllViews();
            for (int itemCount = 0; itemCount < page.size(); itemCount++) {
                Item item = page.get(itemCount);
                if (item._x + item._spanX <= columns && item._y + item._spanY <= rows) {
                    addItemToPage(item, pageCount);
                }
            }
        }
    }

    public final void addPageRight(boolean showGrid) {
        _pageCount++;
        int previousPage = getCurrentItem();
        _adapter.addPageRight();
        setCurrentItem(previousPage + 1);
        if (Setup.appSettings().isDesktopShowGrid()) {
            for (CellContainer cellContainer : _pages) {
                cellContainer.setHideGrid(!showGrid);
            }
        }
        _pageIndicator.invalidate();
    }

    public final void addPageLeft(boolean showGrid) {
        _pageCount++;
        int previousPage = getCurrentItem();
        _adapter.addPageLeft();
        setCurrentItem(previousPage + 1, false);
        setCurrentItem(previousPage - 1);
        if (Setup.appSettings().isDesktopShowGrid()) {
            for (CellContainer cellContainer : _pages) {
                cellContainer.setHideGrid(!showGrid);
            }
        }
        _pageIndicator.invalidate();
    }

    public final void removeCurrentPage() {
        _pageCount--;
        int previousPage = getCurrentItem();
        _adapter.removePage(getCurrentItem(), true);
        for (CellContainer v : _pages) {
            v.setAlpha(0.0f);
            v.animate().alpha(1.0f);
            v.setScaleX(0.85f);
            v.setScaleY(0.85f);
            v.animateBackgroundShow();
        }
        if (_pageCount == 0) {
            addPageRight(false);
            _adapter.exitDesktopEditMode();
        } else {
            setCurrentItem(previousPage);
            _pageIndicator.invalidate();
        }
    }

    public final void updateIconProjection(int x, int y) {
        HomeActivity launcher;
        ItemOptionView dragNDropView;
        DragState state = getCurrentPage().peekItemAndSwap(x, y, _coordinate);
        if (_previousDragPoint != null && !_previousDragPoint.equals(_coordinate)) {
            launcher = HomeActivity.Companion.getLauncher();
            if (launcher != null) {
                dragNDropView = launcher.getItemOptionView();
                dragNDropView.cancelFolderPreview();
            }
        }
        _previousDragPoint.set(_coordinate.x, _coordinate.y);
        switch (state) {
            case CurrentNotOccupied:
                getCurrentPage().projectImageOutlineAt(_coordinate, DragHandler._cachedDragBitmap);
                break;
            case OutOffRange:
            case ItemViewNotFound:
                break;
            case CurrentOccupied:
                Object action;
                launcher = HomeActivity.Companion.getLauncher();
                dragNDropView = launcher.getItemOptionView();
                for (CellContainer page : _pages) {
                    page.clearCachedOutlineBitmap();
                }
                action = dragNDropView.getDragAction();
                if (!Action.WIDGET.equals(action) || !Action.ACTION.equals(action) && (getCurrentPage().coordinateToChildView(_coordinate) instanceof AppItemView)) {
                    launcher.getItemOptionView().showFolderPreviewAt(this, getCurrentPage().getCellWidth() * (_coordinate.x + 0.5f), getCurrentPage().getCellHeight() * (_coordinate.y + 0.5f));
                }
                break;
            default:
                break;
        }
    }

    public void setLastItem(@NonNull Object... args) {
        Item item = (Item) args[0];
        View v = (View) args[1];
        _previousPage = getCurrentItem();
        _previousItemView = v;
        _previousItem = item;
        getCurrentPage().removeView(v);
    }

    public void revertLastItem() {
        if (_previousItemView != null) {
            if (_adapter.getCount() >= _previousPage && _previousPage > -1) {
                CellContainer cellContainer = _pages.get(_previousPage);
                cellContainer.addViewToGrid(_previousItemView);
                _previousItem = null;
                _previousItemView = null;
                _previousPage = -1;
            }
        }
    }

    public void consumeRevert() {
        _previousItem = null;
        _previousItemView = null;
        _previousPage = -1;
    }

    public boolean addItemToPage(@NonNull Item item, int page) {
        View itemView = ItemViewFactory.getItemView(getContext(), this, item, Setup.appSettings().getDesktopIconSize(), Setup.appSettings().isDesktopShowLabel());
        if (itemView == null) {
            HomeActivity._db.deleteItem(item, true);
            return false;
        }
        item._location = Item.LOCATION_DESKTOP;
        _pages.get(page).addViewToGrid(itemView, item._x, item._y, item._spanX, item._spanY);
        return true;
    }

    public boolean addItemToPoint(@NonNull Item item, int x, int y) {
        CellContainer.LayoutParams positionToLayoutPrams = getCurrentPage().coordinateToLayoutParams(x, y, item._spanX, item._spanY);
        if (positionToLayoutPrams == null) {
            return false;
        }
        item._location = Item.LOCATION_DESKTOP;
        item._x = positionToLayoutPrams.getX();
        item._y = positionToLayoutPrams.getY();
        View itemView = ItemViewFactory.getItemView(getContext(), this, item, Setup.appSettings().getDesktopIconSize(), Setup.appSettings().isDesktopShowLabel());
        if (itemView != null) {
            itemView.setLayoutParams(positionToLayoutPrams);
            getCurrentPage().addView(itemView);
        }
        return true;
    }

    public boolean addItemToCell(@NonNull Item item, int x, int y) {
        item._location = Item.LOCATION_DESKTOP;
        item._x = x;
        item._y = y;
        View itemView = ItemViewFactory.getItemView(getContext(), this, item, Setup.appSettings().getDesktopIconSize(), Setup.appSettings().isDesktopShowLabel());
        if (itemView == null) {
            return false;
        }
        getCurrentPage().addViewToGrid(itemView, item._x, item._y, item._spanX, item._spanY);
        return true;
    }

    public void removeItem(final View view, boolean animate) {
        Tool.print("Start Removing a view from Desktop");
        if (animate) {
            view.animate().setDuration(100).scaleX(0.0f).scaleY(0.0f).withEndAction(new Runnable() {
                @Override
                public void run() {
                    Tool.print("Ok Removing a view from Desktop");
                    if (getCurrentPage().equals(view.getParent())) {
                        getCurrentPage().removeView(view);
                    }
                }
            });
        } else if (getCurrentPage().equals(view.getParent())) {
            getCurrentPage().removeView(view);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(@Nullable MotionEvent ev) {
        if (ev != null && ev.getActionMasked() == MotionEvent.ACTION_UP) {
            HomeActivity launcher = HomeActivity.Companion.getLauncher();
            if (launcher != null) {
                PagerIndicator desktopIndicator = launcher.getDesktopIndicator();
                desktopIndicator.showNow();
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(@Nullable MotionEvent ev) {
        if (ev != null && ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            HomeActivity launcher = HomeActivity.Companion.getLauncher();
            if (launcher != null) {
                launcher.getDesktopIndicator().hideDelay();
            }
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        float xOffset = (position + offset) / (_pageCount - 1);
        WallpaperManager.getInstance(getContext()).setWallpaperOffsets(getWindowToken(), xOffset, 0.0f);
        super.onPageScrolled(position, offset, offsetPixels);
    }

    public interface OnDesktopEditListener {
        void onDesktopEdit();

        void onFinishDesktopEdit();
    }
}
