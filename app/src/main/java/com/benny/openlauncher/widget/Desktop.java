package com.benny.openlauncher.widget;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Point;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.benny.openlauncher.viewutil.SmoothPagerAdapter;
import com.benny.openlauncher.widget.CellContainer.DragState;

import java.util.ArrayList;
import java.util.List;

import in.championswimmer.sfg.lib.SimpleFingerGestures;
import in.championswimmer.sfg.lib.SimpleFingerGestures.OnFingerGestureListener;

public final class Desktop extends SmoothViewPager implements DesktopCallback<View> {
    private OnDesktopEditListener _desktopEditListener;
    private boolean _inEditMode;
    private int _pageCount;
    private PagerIndicator _pageIndicator;

    private final List<CellContainer> _pages = new ArrayList<>();
    private final Point _previousDragPoint = new Point();

    private Point _coordinate = new Point(-1, -1);
    private Item _previousItem;
    private View _previousItemView;
    private int _previousPage;

    public static Item getItemFromCoordinate(Point point, int page) {
        List pageData = HomeActivity._db.getDesktop().get(page);
        int size = pageData.size();
        for (int i = 0; i < size; i++) {
            Item item = (Item) pageData.get(i);
            if (item._x == point.x && item._y == point.y && item._spanX == 1 && item._spanY == 1) {
                return (Item) pageData.get(i);
            }
        }
        return null;
    }

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

    public final class DesktopAdapter extends SmoothPagerAdapter {
        private MotionEvent _currentEvent;
        private final Desktop _desktop;
        private float _scaleFactor = 1.0f;
        private float _translateFactor;

        public DesktopAdapter(Desktop desktop) {
            _desktop = desktop;
            _desktop.getPages().clear();
            int count = getCount();
            for (int i = 0; i < count; i++) {
                _desktop.getPages().add(getItemLayout());
            }
        }

        private final OnFingerGestureListener getGestureListener() {
            return new DesktopGestureListener(_desktop, Setup.desktopGestureCallback());
        }

        private final CellContainer getItemLayout() {
            Context context = _desktop.getContext();
            CellContainer layout = new CellContainer(context);
            layout.setSoundEffectsEnabled(false);
            SimpleFingerGestures mySfg = new SimpleFingerGestures();
            mySfg.setOnFingerGestureListener(getGestureListener());
            layout.setGestures(mySfg);
            layout.setOnItemRearrangeListener(new CellContainer.OnItemRearrangeListener() {
                @Override
                public void onItemRearrange(@NonNull Point from, @NonNull Point to) {
                    Item itemFromCoordinate = Desktop.getItemFromCoordinate(from, getCurrentItem());
                    if (itemFromCoordinate != null) {
                        itemFromCoordinate._x = to.x;
                        itemFromCoordinate._y = to.y;
                    }
                }
            });
            layout.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    _currentEvent = event;
                    return false;
                }
            });
            layout.setGridSize(Setup.appSettings().getDesktopColumnCount(), Setup.appSettings().getDesktopRowCount());
            layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!(_desktop.getInEditMode() || _currentEvent == null)) {
                        WallpaperManager instance = WallpaperManager.getInstance(view.getContext());
                        IBinder windowToken = view.getWindowToken();
                        String str = "android.wallpaper.tap";
                        MotionEvent access$getCurrentEvent$p = _currentEvent;
                        instance.sendWallpaperCommand(windowToken, str, (int) access$getCurrentEvent$p.getX(), (int) access$getCurrentEvent$p.getY(), 0, null);
                    }
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

        public final void addPageLeft() {
            _desktop.getPages().add(0, getItemLayout());
            notifyDataSetChanged();
        }

        public final void addPageRight() {
            _desktop.getPages().add(getItemLayout());
            notifyDataSetChanged();
        }

        public final void removePage(int position, boolean deleteItems) {
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

        public int getItemPosition(@NonNull Object object) {
            return -2;
        }

        public int getCount() {
            return _desktop.getPageCount();
        }

        public boolean isViewFromObject(@NonNull View p1, @NonNull Object p2) {
            return p1 == p2;
        }

        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        public Object instantiateItem(ViewGroup container, int pos) {
            CellContainer layout = _desktop.getPages().get(pos);
            container.addView(layout);
            return layout;
        }

        public final void enterDesktopEditMode() {
            _scaleFactor = 0.8f;
            _translateFactor = (float) Tool.toPx(Setup.appSettings().getSearchBarEnable() ? 20 : 40);
            for (CellContainer v : _desktop.getPages()) {
                v.setBlockTouch(true);
                v.animateBackgroundShow();
                ViewPropertyAnimator translationY = v.animate().scaleX(_scaleFactor).scaleY(_scaleFactor).translationY(_translateFactor);
                translationY.setInterpolator(new AccelerateDecelerateInterpolator());
            }
            _desktop.setInEditMode(true);
            if (_desktop.getDesktopEditListener() != null) {
                OnDesktopEditListener desktopEditListener = _desktop.getDesktopEditListener();
                desktopEditListener.onDesktopEdit();
            }
        }

        public final void exitDesktopEditMode() {
            _scaleFactor = 1.0f;
            _translateFactor = 0.0f;
            for (CellContainer v : _desktop.getPages()) {
                v.setBlockTouch(false);
                v.animateBackgroundHide();
                ViewPropertyAnimator translationY = v.animate().scaleX(_scaleFactor).scaleY(_scaleFactor).translationY(_translateFactor);
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
        if (!isInEditMode()) {
            _pageCount = HomeActivity._db.getDesktop().size();
            if (_pageCount == 0) {
                _pageCount = 1;
            }
            setCurrentItem(Setup.appSettings().getDesktopPageCurrent());
        }
    }

    public final void initDesktop() {
        setAdapter(new DesktopAdapter(this));
        if (Setup.appSettings().isDesktopShowIndicator() && _pageIndicator != null) {
            _pageIndicator.setViewPager(this);
        }
        int columns = Setup.appSettings().getDesktopColumnCount();
        int rows = Setup.appSettings().getDesktopRowCount();
        List desktopItems = HomeActivity._db.getDesktop();
        int size = desktopItems.size();
        int pageCount = 0;
        while (pageCount < size) {
            if (_pages.size() > pageCount) {
                _pages.get(pageCount).removeAllViews();
                List items = (List) desktopItems.get(pageCount);
                int size2 = items.size();
                for (int j = 0; j < size2; j++) {
                    Item item = (Item) items.get(j);
                    if (item._x + item._spanX <= columns && item._y + item._spanY <= rows) {
                        addItemToPage(item, pageCount);
                    }
                }
                pageCount++;
            } else {
                return;
            }
        }
    }

    public final void addPageRight(boolean showGrid) {
        _pageCount++;
        int previousPage = getCurrentItem();
        SmoothPagerAdapter adapter = getAdapter();
        ((DesktopAdapter) adapter).addPageRight();
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
        SmoothPagerAdapter adapter = getAdapter();
        ((DesktopAdapter) adapter).addPageLeft();
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
        SmoothPagerAdapter adapter = getAdapter();
        ((DesktopAdapter) adapter).removePage(getCurrentItem(), true);
        for (CellContainer v : _pages) {
            v.setAlpha(0.0f);
            v.animate().alpha(1.0f);
            v.setScaleX(0.85f);
            v.setScaleY(0.85f);
            v.animateBackgroundShow();
        }
        if (_pageCount == 0) {
            addPageRight(false);
            adapter = getAdapter();
            ((DesktopAdapter) adapter).exitDesktopEditMode();
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
                dragNDropView = launcher.getDragNDropView();
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
                dragNDropView = launcher.getDragNDropView();
                for (CellContainer page : _pages) {
                    page.clearCachedOutlineBitmap();
                }
                action = dragNDropView.getDragAction();
                if (!Action.WIDGET.equals(action) || !Action.ACTION.equals(action) && (getCurrentPage().coordinateToChildView(_coordinate) instanceof AppItemView)) {
                    launcher.getDragNDropView().showFolderPreviewAt(this, getCurrentPage().getCellWidth() * (_coordinate.x + 0.5f), getCurrentPage().getCellHeight() * (_coordinate.y + 0.5f));
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
            SmoothPagerAdapter adapter = getAdapter();
            if (adapter.getCount() >= _previousPage && _previousPage > -1) {
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

    public boolean onInterceptTouchEvent(@Nullable MotionEvent ev) {
        if (ev != null && ev.getActionMasked() == 0) {
            HomeActivity launcher = HomeActivity.Companion.getLauncher();
            if (launcher != null) {
                PagerIndicator desktopIndicator = launcher.getDesktopIndicator();
                desktopIndicator.showNow();
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    public boolean onTouchEvent(@Nullable MotionEvent ev) {
        if (ev != null && ev.getActionMasked() == 1) {
            HomeActivity launcher = HomeActivity.Companion.getLauncher();
            if (launcher != null) {
                launcher.getDesktopIndicator().hideDelay();
            }
        }
        return super.onTouchEvent(ev);
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

    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        if (!isInEditMode()) {
            HomeActivity launcher = HomeActivity.Companion.getLauncher();
            if (launcher != null) {
                launcher.getDragNDropView().cancelFolderPreview();
            }
            WallpaperManager.getInstance(getContext()).setWallpaperOffsets(getWindowToken(), (position + offset) / (_pageCount - 1), 0.0f);
            super.onPageScrolled(position, offset, offsetPixels);
        }
    }

    public interface OnDesktopEditListener {
        void onDesktopEdit();

        void onFinishDesktopEdit();
    }
}
