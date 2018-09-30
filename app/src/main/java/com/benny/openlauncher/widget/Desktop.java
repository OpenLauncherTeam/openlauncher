package com.benny.openlauncher.widget;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Point;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.WindowInsets;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.model.Item.Type;
import com.benny.openlauncher.model.App;
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
    public static final Companion _companion = new Companion();
    public static int _bottomInset;
    public static int _topInset;
    private final Point _coordinate = new Point(-1, -1);

    @Nullable
    private OnDesktopEditListener _desktopEditListener;
    private HomeActivity _homeActivity;
    private boolean _inEditMode;
    private int _pageCount;
    private PagerIndicator _pageIndicator;

    @NonNull
    private final List<CellContainer> _pages = new ArrayList<CellContainer>();
    private final Point _previousDragPoint = new Point();

    @Nullable
    private Item _previousItem;
    @Nullable
    private View _previousItemView;
    private int _previousPage;


    public static final class Companion {
        private Companion() {
        }

        public final void setTopInset(int v) {
            Desktop._topInset = v;
        }

        public final void setBottomInset(int v) {
            Desktop._bottomInset = v;
        }

        @Nullable
        public final Item getItemFromCoordinate(@NonNull Point point, int page) {

            List pageData = (List) HomeActivity.Companion.getDb().getDesktop().get(page);
            int size = pageData.size();
            for (int i = 0; i < size; i++) {
                Item item = (Item) pageData.get(i);
                if (item._x == point.x && item._y == point.y && item._spanX == 1 && item._spanY == 1) {
                    return (Item) pageData.get(i);
                }
            }
            return null;
        }
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
                                HomeActivity.Companion.getDb().saveItem(dropItem, page, itemPosition);
                                HomeActivity.Companion.getDb().saveItem(item, ItemState.Hidden);
                                HomeActivity.Companion.getDb().saveItem(dropItem, ItemState.Hidden);
                                HomeActivity.Companion.getDb().saveItem(group, page, itemPosition);
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
                                HomeActivity.Companion.getDb().saveItem(dropItem, page, itemPosition);
                                HomeActivity.Companion.getDb().saveItem(dropItem, ItemState.Hidden);
                                HomeActivity.Companion.getDb().saveItem(item, page, itemPosition);
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

    public static final class DesktopMode {
        public static final DesktopMode INSTANCE = new DesktopMode();
        public static final int NORMAL = 0;
        public static final int SHOW_ALL_APPS = 1;

        private DesktopMode() {
        }

        public final int getNORMAL() {
            return NORMAL;
        }

        public final int getSHOW_ALL_APPS() {
            return SHOW_ALL_APPS;
        }
    }

    public interface OnDesktopEditListener {
        void onDesktopEdit();

        void onFinishDesktopEdit();
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
                    Item itemFromCoordinate = Desktop._companion.getItemFromCoordinate(from, getCurrentItem());
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
                for (View v : ((CellContainer) _desktop.getPages().get(position)).getAllCells()) {
                    Object item = v.getTag();
                    if (item instanceof Item) {
                        HomeActivity.Companion.getDb().deleteItem((Item) item, true);
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

        @NonNull
        public Object instantiateItem(@NonNull ViewGroup container, int pos) {

            CellContainer layout = (CellContainer) _desktop.getPages().get(pos);
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

    public Desktop(@NonNull Context context) {
        super(context, null);
    }

    public Desktop(@NonNull Context c, @Nullable AttributeSet attr) {
        super(c, attr);
    }

    @NonNull
    public final List<CellContainer> getPages() {
        return _pages;
    }

    @Nullable
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

    @NonNull
    public final CellContainer getCurrentPage() {
        return (CellContainer) _pages.get(getCurrentItem());
    }

    public final void setPageIndicator(@NonNull PagerIndicator pageIndicator) {

        _pageIndicator = pageIndicator;
    }

    public final void init() {
        if (!isInEditMode()) {
            _pageCount = HomeActivity.Companion.getDb().getDesktop().size();
            if (_pageCount == 0) {
                _pageCount = 1;
            }
            setCurrentItem(Setup.appSettings().getDesktopPageCurrent());
        }
    }

    public final void initDesktopNormal(@NonNull HomeActivity homeActivity) {

        setAdapter(new DesktopAdapter(this));
        if (Setup.appSettings().isDesktopShowIndicator() && _pageIndicator != null) {
            _pageIndicator.setViewPager(this);
        }
        _homeActivity = homeActivity;
        int columns = Setup.appSettings().getDesktopColumnCount();
        int rows = Setup.appSettings().getDesktopRowCount();
        List desktopItems = HomeActivity.Companion.getDb().getDesktop();
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

    public final void initDesktopShowAll(@NonNull Context c, @NonNull HomeActivity homeActivity) {
        Desktop desktop = this;
        Context context = c;
        HomeActivity homeActivity2 = homeActivity;


        ArrayList apps = new ArrayList();
        for (App app : Setup.appLoader().getAllApps(context, false)) {
            apps.add(Item.newAppItem(app));
        }
        int appsSize = apps.size();
        desktop._pageCount = 0;
        int columns = Setup.appSettings().getDesktopColumnCount();
        int rows = Setup.appSettings().getDesktopRowCount();
        appsSize -= columns * rows;
        while (true) {
            if (appsSize < columns * rows) {
                if (appsSize <= (-(columns * rows))) {
                    break;
                }
            }
            desktop._pageCount++;
        }
        setAdapter(new DesktopAdapter(desktop));
        if (Setup.appSettings().isDesktopShowIndicator() && desktop._pageIndicator != null) {
            desktop._pageIndicator.setViewPager(desktop);
        }
        desktop._homeActivity = homeActivity2;

        for (int i = 0; i < _pageCount; i++) {
            for (int x = 0; x < columns; x++) {
                for (int y = 0; y < rows; y++) {
                    int pagePos = y * rows + x;
                    int pos = columns * rows * i + pagePos;
                    if (pos < apps.size()) {
                        Item appItem = (Item) apps.get(pos);
                        appItem._x = x;
                        appItem._y = y;
                        addItemToPage(appItem, i);
                    }
                }
            }
        }
    }

    public final void addPageRight(boolean showGrid) {
        _pageCount++;
        int previousPage = getCurrentItem();
        SmoothPagerAdapter adapter = getAdapter();
        ((DesktopAdapter) adapter).addPageRight();
        setCurrentItem(previousPage + 1);
        if (!Setup.appSettings().isDesktopHideGrid()) {
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
        if (!Setup.appSettings().isDesktopHideGrid()) {
            for (CellContainer cellContainer : _pages) {
                cellContainer.setHideGrid(!showGrid);
            }
        }
        _pageIndicator.invalidate();
    }

    public final void removeCurrentPage() {
        if (Setup.appSettings().getDesktopStyle() != DesktopMode.INSTANCE.getSHOW_ALL_APPS()) {
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
    }

    public final void updateIconProjection(int x, int y) {
        HomeActivity launcher;
        DragOptionLayout dragNDropView;
        DragState state = getCurrentPage().peekItemAndSwap(x, y, _coordinate);
        if (_previousDragPoint != null && !_previousDragPoint.equals(_coordinate)) {
            launcher = HomeActivity.Companion.getLauncher();
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
                getCurrentPage().projectImageOutlineAt(_coordinate, DragHandler._cachedDragBitmap);
                break;
            case OutOffRange:
            case ItemViewNotFound:
                break;
            case CurrentOccupied:
                Object action;
                HomeActivity launcher2;
                DragOptionLayout dragNDropView2;
                for (CellContainer page : _pages) {
                    page.clearCachedOutlineBitmap();
                }
                launcher = HomeActivity.Companion.getLauncher();
                if (launcher != null) {
                    dragNDropView = launcher.getDragNDropView();
                    if (dragNDropView != null) {
                        action = dragNDropView.getDragAction();
                        if (!Action.WIDGET.equals(action) || !Action.ACTION.equals(action) && (getCurrentPage().coordinateToChildView(_coordinate) instanceof AppItemView)) {
                            launcher2 = HomeActivity.Companion.getLauncher();
                            if (launcher2 != null) {
                                dragNDropView2 = launcher2.getDragNDropView();
                                if (dragNDropView2 != null) {
                                    dragNDropView2.showFolderPreviewAt(this, ((float) getCurrentPage().getCellWidth()) * (((float) _coordinate.x) + 0.5f), (((float) getCurrentPage().getCellHeight()) * (((float) _coordinate.y) + 0.5f)) - ((float) (Setup.appSettings().isDesktopShowLabel() ? Tool.toPx(7) : 0)));
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
                action = null;
                launcher2 = HomeActivity.Companion.getLauncher();
                if (launcher2 != null) {
                    dragNDropView2 = launcher2.getDragNDropView();
                    if (dragNDropView2 != null) {
                        if (Setup.appSettings().isDesktopShowLabel()) {
                        }
                        dragNDropView2.showFolderPreviewAt(this, ((float) getCurrentPage().getCellWidth()) * (((float) _coordinate.x) + 0.5f), (((float) getCurrentPage().getCellHeight()) * (((float) _coordinate.y) + 0.5f)) - ((float) (Setup.appSettings().isDesktopShowLabel() ? Tool.toPx(7) : 0)));
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
        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.appSettings().isDesktopShowLabel(), (DesktopCallback) this, Setup.appSettings().getDesktopIconSize());
        if (itemView == null) {
            HomeActivity.Companion.getDb().deleteItem(item, true);
            return false;
        }
        item._locationInLauncher = 0;
        ((CellContainer) _pages.get(page)).addViewToGrid(itemView, item._x, item._y, item._spanX, item._spanY);
        return true;
    }

    public boolean addItemToPoint(@NonNull Item item, int x, int y) {
        CellContainer.LayoutParams positionToLayoutPrams = getCurrentPage().coordinateToLayoutParams(x, y, item._spanX, item._spanY);
        if (positionToLayoutPrams == null) {
            return false;
        }
        item._locationInLauncher = 0;
        item._x = positionToLayoutPrams.getX();
        item._y = positionToLayoutPrams.getY();
        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.appSettings().isDesktopShowLabel(), (DesktopCallback) this, Setup.appSettings().getDesktopIconSize());
        if (itemView != null) {
            itemView.setLayoutParams(positionToLayoutPrams);
            getCurrentPage().addView(itemView);
        }
        return true;
    }

    public boolean addItemToCell(@NonNull Item item, int x, int y) {
        item._locationInLauncher = 0;
        item._x = x;
        item._y = y;
        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.appSettings().isDesktopShowLabel(), (DesktopCallback) this, Setup.appSettings().getDesktopIconSize());
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
                    if (getParent() != null && getParent().equals(getCurrentPage())) {
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
                DragOptionLayout dragNDropView = launcher.getDragNDropView();
                if (dragNDropView != null) {
                    dragNDropView.cancelFolderPreview();
                }
            }
            WallpaperManager.getInstance(getContext()).setWallpaperOffsets(getWindowToken(), (((float) position) + offset) / ((float) (_pageCount - 1)), 0.0f);
            super.onPageScrolled(position, offset, offsetPixels);
        }
    }

    @NonNull
    public WindowInsets onApplyWindowInsets(@NonNull WindowInsets insets) {
        if (VERSION.SDK_INT >= 20) {
            _companion.setTopInset(insets.getSystemWindowInsetTop());
            _companion.setBottomInset(insets.getSystemWindowInsetBottom());
            HomeActivity launcher = HomeActivity.Companion.getLauncher();
            if (launcher != null) {
                launcher.updateHomeLayout();
            }
        }
        return insets;
    }
}
