package com.benny.openlauncher.widget;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Point;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.WindowInsets;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.model.Item.Type;
import com.benny.openlauncher.util.App;
import com.benny.openlauncher.util.Definitions.ItemPosition;
import com.benny.openlauncher.util.Definitions.ItemState;
import com.benny.openlauncher.util.DragAction.Action;
import com.benny.openlauncher.util.DragNDropHandler;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DesktopCallBack;
import com.benny.openlauncher.viewutil.DesktopGestureListener;
import com.benny.openlauncher.viewutil.ItemViewFactory;
import com.benny.openlauncher.viewutil.SmoothPagerAdapter;
import com.benny.openlauncher.widget.CellContainer.DragState;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import in.championswimmer.sfg.lib.SimpleFingerGestures;
import in.championswimmer.sfg.lib.SimpleFingerGestures.OnFingerGestureListener;
import kotlin.TypeCastException;
import kotlin.jvm.JvmOverloads;
import kotlin.jvm.internal.Intrinsics;

public final class Desktop extends SmoothViewPager implements DesktopCallBack<View> {
    public static final Companion _companion = new Companion();
    public static int _bottomInset;
    public static int _topInset;
    private final Point _coordinate = new Point(-1, -1);

    @Nullable
    private OnDesktopEditListener _desktopEditListener;
    private Home _home;
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

            List pageData = (List) Home.Companion.getDb().getDesktop().get(page);
            int size = pageData.size();
            for (int i = 0; i < size; i++) {
                Item item = (Item) pageData.get(i);
                if (item.x == point.x && item.y == point.y && item.spanX == 1 && item.spanY == 1) {
                    return (Item) pageData.get(i);
                }
            }
            return null;
        }
    }


    public static boolean handleOnDropOver(Home home, Item dropItem, Item item, View itemView, CellContainer parent, int page, ItemPosition itemPosition, DesktopCallBack<?> callback) {


        if (item != null) {
            if (dropItem != null) {
                Type type = item.type;
                if (type != null) {
                    switch (type) {
                        case APP:
                        case SHORTCUT:
                            if (Intrinsics.areEqual(dropItem.type, Type.APP) || Intrinsics.areEqual(dropItem.type, Type.SHORTCUT)) {
                                parent.removeView(itemView);
                                Item group = Item.newGroupItem();
                                group.getGroupItems().add(item);
                                group.getGroupItems().add(dropItem);
                                group.x = item.x;
                                group.y = item.y;
                                Home.Companion.getDb().saveItem(dropItem, page, itemPosition);
                                Home.Companion.getDb().saveItem(item, ItemState.Hidden);
                                Home.Companion.getDb().saveItem(dropItem, ItemState.Hidden);
                                Home.Companion.getDb().saveItem(group, page, itemPosition);
                                callback.addItemToPage(group, page);
                                Home launcher = Home.Companion.getLauncher();
                                if (launcher != null) {
                                    Desktop desktop = launcher.getDesktop();
                                    if (desktop != null) {
                                        desktop.consumeRevert();
                                    }
                                }
                                launcher = Home.Companion.getLauncher();
                                if (launcher != null) {
                                    Dock dock = launcher.getDock();
                                    if (dock != null) {
                                        dock.consumeRevert();
                                    }
                                }
                                return true;
                            }
                        case GROUP:
                            if ((Intrinsics.areEqual(dropItem.type, Type.APP) || Intrinsics.areEqual(dropItem.type, Type.SHORTCUT)) && item.getGroupItems().size() < GroupPopupView.GroupDef._maxItem) {
                                parent.removeView(itemView);
                                item.getGroupItems().add(dropItem);
                                Home.Companion.getDb().saveItem(dropItem, page, itemPosition);
                                Home.Companion.getDb().saveItem(dropItem, ItemState.Hidden);
                                Home.Companion.getDb().saveItem(item, page, itemPosition);
                                callback.addItemToPage(item, page);
                                Home launcher2 = Home.Companion.getLauncher();
                                if (launcher2 != null) {
                                    Desktop desktop2 = launcher2.getDesktop();
                                    if (desktop2 != null) {
                                        desktop2.consumeRevert();
                                    }
                                }
                                launcher2 = Home.Companion.getLauncher();
                                if (launcher2 != null) {
                                    Dock dock2 = launcher2.getDock();
                                    if (dock2 != null) {
                                        dock2.consumeRevert();
                                    }
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
            this._desktop = desktop;
            this._desktop.getPages().clear();
            int count = getCount();
            for (int i = 0; i < count; i++) {
                this._desktop.getPages().add(getItemLayout());
            }
        }

        private final OnFingerGestureListener getGestureListener() {
            return new DesktopGestureListener(this._desktop, Setup.desktopGestureCallback());
        }

        private final CellContainer getItemLayout() {
            Context context = this._desktop.getContext();
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
                        itemFromCoordinate.x = to.x;
                        itemFromCoordinate.y = to.y;
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
                        if (access$getCurrentEvent$p == null) {
                            Intrinsics.throwNpe();
                        }
                        int x = (int) access$getCurrentEvent$p.getX();
                        access$getCurrentEvent$p = _currentEvent;
                        if (access$getCurrentEvent$p == null) {
                            Intrinsics.throwNpe();
                        }
                        instance.sendWallpaperCommand(windowToken, str, x, (int) access$getCurrentEvent$p.getY(), 0, null);
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
            this._desktop.getPages().add(0, getItemLayout());
            notifyDataSetChanged();
        }

        public final void addPageRight() {
            this._desktop.getPages().add(getItemLayout());
            notifyDataSetChanged();
        }

        public final void removePage(int position, boolean deleteItems) {
            if (deleteItems) {
                for (View v : ((CellContainer) this._desktop.getPages().get(position)).getAllCells()) {
                    Object item = v.getTag();
                    if (item instanceof Item) {
                        Home.Companion.getDb().deleteItem((Item) item, true);
                    }
                }
            }
            this._desktop.getPages().remove(position);
            notifyDataSetChanged();
        }

        public int getItemPosition(@NonNull Object object) {

            return -2;
        }

        public int getCount() {
            return this._desktop.getPageCount();
        }

        public boolean isViewFromObject(@NonNull View p1, @NonNull Object p2) {


            return p1 == p2;
        }

        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {


            container.removeView((View) object);
        }

        @NonNull
        public Object instantiateItem(@NonNull ViewGroup container, int pos) {

            CellContainer layout = (CellContainer) this._desktop.getPages().get(pos);
            container.addView(layout);
            return layout;
        }

        public final void enterDesktopEditMode() {
            this._scaleFactor = 0.8f;
            this._translateFactor = (float) Tool.toPx(Setup.appSettings().getSearchBarEnable() ? 20 : 40);
            for (CellContainer v : this._desktop.getPages()) {
                v.setBlockTouch(true);
                v.animateBackgroundShow();
                ViewPropertyAnimator translationY = v.animate().scaleX(this._scaleFactor).scaleY(this._scaleFactor).translationY(this._translateFactor);
                translationY.setInterpolator(new AccelerateDecelerateInterpolator());
            }
            this._desktop.setInEditMode(true);
            if (this._desktop.getDesktopEditListener() != null) {
                OnDesktopEditListener desktopEditListener = this._desktop.getDesktopEditListener();
                if (desktopEditListener == null) {
                    Intrinsics.throwNpe();
                }
                desktopEditListener.onDesktopEdit();
            }
        }

        public final void exitDesktopEditMode() {
            this._scaleFactor = 1.0f;
            this._translateFactor = 0.0f;
            for (CellContainer v : this._desktop.getPages()) {
                v.setBlockTouch(false);
                v.animateBackgroundHide();
                ViewPropertyAnimator translationY = v.animate().scaleX(this._scaleFactor).scaleY(this._scaleFactor).translationY(this._translateFactor);
                translationY.setInterpolator(new AccelerateDecelerateInterpolator());
            }
            this._desktop.setInEditMode(false);
            if (this._desktop.getDesktopEditListener() != null) {
                OnDesktopEditListener desktopEditListener = this._desktop.getDesktopEditListener();
                if (desktopEditListener == null) {
                    Intrinsics.throwNpe();
                }
                desktopEditListener.onFinishDesktopEdit();
            }
        }
    }

    @JvmOverloads
    public Desktop(@NonNull Context context) {
        super(context, null);
    }

    public Desktop(@NonNull Context c, @Nullable AttributeSet attr) {
        super(c, attr);
    }

    @NonNull
    public final List<CellContainer> getPages() {
        return this._pages;
    }

    @Nullable
    public final OnDesktopEditListener getDesktopEditListener() {
        return this._desktopEditListener;
    }

    public final void setDesktopEditListener(@Nullable OnDesktopEditListener v) {
        this._desktopEditListener = v;
    }

    public final boolean getInEditMode() {
        return this._inEditMode;
    }

    public final void setInEditMode(boolean v) {
        this._inEditMode = v;
    }

    public final int getPageCount() {
        return this._pageCount;
    }

    public final boolean isCurrentPageEmpty() {
        return getCurrentPage().getChildCount() == 0;
    }

    @NonNull
    public final CellContainer getCurrentPage() {
        return (CellContainer) this._pages.get(getCurrentItem());
    }

    public final void setPageIndicator(@NonNull PagerIndicator pageIndicator) {

        this._pageIndicator = pageIndicator;
    }

    public final void init() {
        if (!isInEditMode()) {
            this._pageCount = Home.Companion.getDb().getDesktop().size();
            if (this._pageCount == 0) {
                this._pageCount = 1;
            }
            setCurrentItem(Setup.appSettings().getDesktopPageCurrent());
        }
    }

    public final void initDesktopNormal(@NonNull Home home) {

        setAdapter(new DesktopAdapter(this));
        if (Setup.appSettings().isDesktopShowIndicator() && this._pageIndicator != null) {
            PagerIndicator pagerIndicator = this._pageIndicator;
            if (pagerIndicator == null) {
                Intrinsics.throwNpe();
            }
            pagerIndicator.setViewPager(this);
        }
        this._home = home;
        int columns = Setup.appSettings().getDesktopColumnCount();
        int rows = Setup.appSettings().getDesktopRowCount();
        List desktopItems = Home.Companion.getDb().getDesktop();
        int size = desktopItems.size();
        int pageCount = 0;
        while (pageCount < size) {
            if (this._pages.size() > pageCount) {
                ((CellContainer) this._pages.get(pageCount)).removeAllViews();
                List items = (List) desktopItems.get(pageCount);
                int size2 = items.size();
                for (int j = 0; j < size2; j++) {
                    Item item = (Item) items.get(j);
                    if (item.x + item.spanX <= columns && item.y + item.spanY <= rows) {
                        addItemToPage(item, pageCount);
                    }
                }
                pageCount++;
            } else {
                return;
            }
        }
    }

    public final void initDesktopShowAll(@NonNull Context c, @NonNull Home home) {
        Desktop desktop = this;
        Context context = c;
        Home home2 = home;


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
            PagerIndicator pagerIndicator = desktop._pageIndicator;
            if (pagerIndicator == null) {
                Intrinsics.throwNpe();
            }
            pagerIndicator.setViewPager(desktop);
        }
        desktop._home = home2;

        for (int i = 0; i < _pageCount; i++) {
            for (int x = 0; x < columns; x++) {
                for (int y = 0; y < rows; y++) {
                    int pagePos = y * rows + x;
                    int pos = columns * rows * i + pagePos;
                    if (pos < apps.size()) {
                        Item appItem = (Item) apps.get(pos);
                        appItem.x = x;
                        appItem.y = y;
                        addItemToPage(appItem, i);
                    }
                }
            }
        }
    }

    public final void addPageRight(boolean showGrid) {
        this._pageCount++;
        int previousPage = getCurrentItem();
        SmoothPagerAdapter adapter = getAdapter();
        if (adapter == null) {
            throw new TypeCastException("null cannot be cast to non-null type com.benny.openlauncher.widget.Desktop.DesktopAdapter");
        }
        ((DesktopAdapter) adapter).addPageRight();
        setCurrentItem(previousPage + 1);
        if (!Setup.appSettings().isDesktopHideGrid()) {
            for (CellContainer cellContainer : this._pages) {
                cellContainer.setHideGrid(!showGrid);
            }
        }
        PagerIndicator pagerIndicator = this._pageIndicator;
        if (pagerIndicator == null) {
            Intrinsics.throwNpe();
        }
        pagerIndicator.invalidate();
    }

    public final void addPageLeft(boolean showGrid) {
        this._pageCount++;
        int previousPage = getCurrentItem();
        SmoothPagerAdapter adapter = getAdapter();
        if (adapter == null) {
            throw new TypeCastException("null cannot be cast to non-null type com.benny.openlauncher.widget.Desktop.DesktopAdapter");
        }
        ((DesktopAdapter) adapter).addPageLeft();
        setCurrentItem(previousPage + 1, false);
        setCurrentItem(previousPage - 1);
        if (!Setup.appSettings().isDesktopHideGrid()) {
            for (CellContainer cellContainer : this._pages) {
                cellContainer.setHideGrid(!showGrid);
            }
        }
        PagerIndicator pagerIndicator = this._pageIndicator;
        if (pagerIndicator == null) {
            Intrinsics.throwNpe();
        }
        pagerIndicator.invalidate();
    }

    public final void removeCurrentPage() {
        if (Setup.appSettings().getDesktopStyle() != DesktopMode.INSTANCE.getSHOW_ALL_APPS()) {
            this._pageCount--;
            int previousPage = getCurrentItem();
            SmoothPagerAdapter adapter = getAdapter();
            if (adapter == null) {
                throw new TypeCastException("null cannot be cast to non-null type com.benny.openlauncher.widget.Desktop.DesktopAdapter");
            }
            ((DesktopAdapter) adapter).removePage(getCurrentItem(), true);
            for (CellContainer v : this._pages) {
                v.setAlpha(0.0f);
                v.animate().alpha(1.0f);
                v.setScaleX(0.85f);
                v.setScaleY(0.85f);
                v.animateBackgroundShow();
            }
            if (this._pageCount == 0) {
                addPageRight(false);
                adapter = getAdapter();
                if (adapter == null) {
                    throw new TypeCastException("null cannot be cast to non-null type com.benny.openlauncher.widget.Desktop.DesktopAdapter");
                }
                ((DesktopAdapter) adapter).exitDesktopEditMode();
            } else {
                setCurrentItem(previousPage);
                PagerIndicator pagerIndicator = this._pageIndicator;
                if (pagerIndicator == null) {
                    Intrinsics.throwNpe();
                }
                pagerIndicator.invalidate();
            }
        }
    }

    public final void updateIconProjection(int x, int y) {
        Home launcher;
        DragNDropLayout dragNDropView;
        DragState state = getCurrentPage().peekItemAndSwap(x, y, this._coordinate);
        if (_previousDragPoint != null && !_previousDragPoint.equals(_coordinate)) {
            launcher = Home.Companion.getLauncher();
            if (launcher != null) {
                dragNDropView = launcher.getDragNDropView();
                if (dragNDropView != null) {
                    dragNDropView.cancelFolderPreview();
                }
            }
        }
        this._previousDragPoint.set(this._coordinate.x, this._coordinate.y);
        switch (state) {
            case CurrentNotOccupied:
                getCurrentPage().projectImageOutlineAt(this._coordinate, DragNDropHandler.cachedDragBitmap);
                break;
            case OutOffRange:
            case ItemViewNotFound:
                break;
            case CurrentOccupied:
                Object action;
                Home launcher2;
                DragNDropLayout dragNDropView2;
                for (CellContainer page : this._pages) {
                    page.clearCachedOutlineBitmap();
                }
                launcher = Home.Companion.getLauncher();
                if (launcher != null) {
                    dragNDropView = launcher.getDragNDropView();
                    if (dragNDropView != null) {
                        action = dragNDropView.getDragAction();
                        if (!Action.WIDGET.equals(action) || !Action.ACTION.equals(action) && (getCurrentPage().coordinateToChildView(_coordinate) instanceof AppItemView)) {
                            launcher2 = Home.Companion.getLauncher();
                            if (launcher2 != null) {
                                dragNDropView2 = launcher2.getDragNDropView();
                                if (dragNDropView2 != null) {
                                    dragNDropView2.showFolderPreviewAt(this, ((float) getCurrentPage().getCellWidth()) * (((float) this._coordinate.x) + 0.5f), (((float) getCurrentPage().getCellHeight()) * (((float) this._coordinate.y) + 0.5f)) - ((float) (Setup.appSettings().isDesktopShowLabel() ? Tool.toPx(7) : 0)));
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
                        if (Setup.appSettings().isDesktopShowLabel()) {
                        }
                        dragNDropView2.showFolderPreviewAt(this, ((float) getCurrentPage().getCellWidth()) * (((float) this._coordinate.x) + 0.5f), (((float) getCurrentPage().getCellHeight()) * (((float) this._coordinate.y) + 0.5f)) - ((float) (Setup.appSettings().isDesktopShowLabel() ? Tool.toPx(7) : 0)));
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
        this._previousPage = getCurrentItem();
        this._previousItemView = v;
        this._previousItem = item;
        getCurrentPage().removeView(v);
    }

    public void revertLastItem() {
        if (this._previousItemView != null) {
            SmoothPagerAdapter adapter = getAdapter();
            if (adapter.getCount() >= this._previousPage && this._previousPage > -1) {
                CellContainer cellContainer = (CellContainer) this._pages.get(this._previousPage);
                View view = this._previousItemView;
                if (view == null) {
                    Intrinsics.throwNpe();
                }
                cellContainer.addViewToGrid(view);
                this._previousItem = (Item) null;
                this._previousItemView = (View) null;
                this._previousPage = -1;
            }
        }
    }

    public void consumeRevert() {
        this._previousItem = (Item) null;
        this._previousItemView = (View) null;
        this._previousPage = -1;
    }

    public boolean addItemToPage(@NonNull Item item, int page) {

        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.appSettings().isDesktopShowLabel(), (DesktopCallBack) this, Setup.appSettings().getDesktopIconSize());
        if (itemView == null) {
            Home.Companion.getDb().deleteItem(item, true);
            return false;
        }
        item.locationInLauncher = 0;
        ((CellContainer) this._pages.get(page)).addViewToGrid(itemView, item.x, item.y, item.spanX, item.spanY);
        return true;
    }

    public boolean addItemToPoint(@NonNull Item item, int x, int y) {
        CellContainer.LayoutParams positionToLayoutPrams = getCurrentPage().coordinateToLayoutParams(x, y, item.spanX, item.spanY);
        if (positionToLayoutPrams == null) {
            return false;
        }
        item.locationInLauncher = 0;
        item.x = positionToLayoutPrams.getX();
        item.y = positionToLayoutPrams.getY();
        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.appSettings().isDesktopShowLabel(), (DesktopCallBack) this, Setup.appSettings().getDesktopIconSize());
        if (itemView != null) {
            itemView.setLayoutParams(positionToLayoutPrams);
            getCurrentPage().addView(itemView);
        }
        return true;
    }

    public boolean addItemToCell(@NonNull Item item, int x, int y) {

        item.locationInLauncher = 0;
        item.x = x;
        item.y = y;
        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.appSettings().isDesktopShowLabel(), (DesktopCallBack) this, Setup.appSettings().getDesktopIconSize());
        if (itemView == null) {
            return false;
        }
        getCurrentPage().addViewToGrid(itemView, item.x, item.y, item.spanX, item.spanY);
        return true;
    }

    public boolean onInterceptTouchEvent(@Nullable MotionEvent ev) {
        if (ev == null) {
            Intrinsics.throwNpe();
        }
        if (ev.getActionMasked() == 0) {
            Home launcher = Home.Companion.getLauncher();
            if (launcher != null) {
                PagerIndicator desktopIndicator = launcher.getDesktopIndicator();
                if (desktopIndicator != null) {
                    desktopIndicator.showNow();
                }
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    public boolean onTouchEvent(@Nullable MotionEvent ev) {
        if (ev == null) {
            Intrinsics.throwNpe();
        }
        if (ev.getActionMasked() == 1) {
            Home launcher = Home.Companion.getLauncher();
            if (launcher != null) {
                PagerIndicator desktopIndicator = launcher.getDesktopIndicator();
                if (desktopIndicator != null) {
                    desktopIndicator.hideDelay();
                }
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
        } else if (Intrinsics.areEqual(view.getParent(), getCurrentPage())) {
            getCurrentPage().removeView(view);
        }
    }

    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        if (!isInEditMode()) {
            Home launcher = Home.Companion.getLauncher();
            if (launcher != null) {
                DragNDropLayout dragNDropView = launcher.getDragNDropView();
                if (dragNDropView != null) {
                    dragNDropView.cancelFolderPreview();
                }
            }
            WallpaperManager.getInstance(getContext()).setWallpaperOffsets(getWindowToken(), (((float) position) + offset) / ((float) (this._pageCount - 1)), 0.0f);
            super.onPageScrolled(position, offset, offsetPixels);
        }
    }

    @NonNull
    public WindowInsets onApplyWindowInsets(@NonNull WindowInsets insets) {

        if (VERSION.SDK_INT >= 20) {
            _companion.setTopInset(insets.getSystemWindowInsetTop());
            _companion.setBottomInset(insets.getSystemWindowInsetBottom());
            Home launcher = Home.Companion.getLauncher();
            if (launcher == null) {
                Intrinsics.throwNpe();
            }
            launcher.updateHomeLayout();
        }
        return insets;
    }
}