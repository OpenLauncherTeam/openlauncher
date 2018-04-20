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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.championswimmer.sfg.lib.SimpleFingerGestures;
import in.championswimmer.sfg.lib.SimpleFingerGestures.OnFingerGestureListener;
import kotlin.TypeCastException;
import kotlin.jvm.JvmOverloads;
import kotlin.jvm.internal.Intrinsics;

public final class Desktop extends SmoothViewPager implements DesktopCallBack<View> {
    public static final Companion Companion = new Companion();
    public static int bottomInset;
    public static int topInset;
    private HashMap _$_findViewCache;
    private final Point coordinate = new Point(-1, -1);

    @Nullable
    private OnDesktopEditListener desktopEditListener;
    private Home home;
    private boolean inEditMode;
    private int pageCount;
    private PagerIndicator pageIndicator;

    @NotNull
    private final List<CellContainer> pages = new ArrayList<CellContainer>();
    private final Point previousDragPoint = new Point();

    @Nullable
    private Item previousItem;
    @Nullable
    private View previousItemView;
    private int previousPage;


    public static final class Companion {
        private Companion() {
        }

        public final void setTopInset(int v) {
            Desktop.topInset = v;
        }

        public final void setBottomInset(int v) {
            Desktop.bottomInset = v;
        }

        @Nullable
        public final Item getItemFromCoordinate(@NotNull Point point, int page) {
            Intrinsics.checkParameterIsNotNull(point, "point");
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
        Intrinsics.checkParameterIsNotNull(itemView, "itemView");
        Intrinsics.checkParameterIsNotNull(parent, "parent");
        Intrinsics.checkParameterIsNotNull(itemPosition, "itemPosition");
        Intrinsics.checkParameterIsNotNull(callback, "callback");
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
                                Intrinsics.checkExpressionValueIsNotNull(group, "group");
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
                            if ((Intrinsics.areEqual(dropItem.type, Type.APP) || Intrinsics.areEqual(dropItem.type, Type.SHORTCUT)) && item.getGroupItems().size() < GroupPopupView.GroupDef.maxItem) {
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
        private MotionEvent currentEvent;
        private final Desktop desktop;
        private float scaleFactor = 1.0f;
        final /* synthetic */ Desktop this$0;
        private float translateFactor;

        public DesktopAdapter(@NotNull Desktop $outer, Desktop desktop) {
            Intrinsics.checkParameterIsNotNull(desktop, "desktop");
            this.this$0 = $outer;
            this.desktop = desktop;
            this.desktop.getPages().clear();
            int count = getCount();
            for (int i = 0; i < count; i++) {
                this.desktop.getPages().add(getItemLayout());
            }
        }

        private final OnFingerGestureListener getGestureListener() {
            return new DesktopGestureListener(this.desktop, Setup.Companion.desktopGestureCallback());
        }

        private final CellContainer getItemLayout() {
            Context context = this.desktop.getContext();
            Intrinsics.checkExpressionValueIsNotNull(context, "desktop.context");
            CellContainer layout = new CellContainer(context);
            layout.setSoundEffectsEnabled(false);
            SimpleFingerGestures mySfg = new SimpleFingerGestures();
            mySfg.setOnFingerGestureListener(getGestureListener());
            layout.setGestures(mySfg);
            layout.setOnItemRearrangeListener(new CellContainer.OnItemRearrangeListener() {
                @Override
                public void onItemRearrange(@NotNull Point from, @NotNull Point to) {
                    Item itemFromCoordinate = Desktop.Companion.getItemFromCoordinate(from, getCurrentItem());
                    if (itemFromCoordinate != null) {
                        itemFromCoordinate.x = to.x;
                        itemFromCoordinate.y = to.y;
                    }
                }
            });
            layout.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    currentEvent = event;
                    return false;
                }
            });
            layout.setGridSize(Setup.Companion.appSettings().getDesktopColumnCount(), Setup.Companion.appSettings().getDesktopRowCount());
            layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!(desktop.getInEditMode() || currentEvent == null)) {
                        Intrinsics.checkExpressionValueIsNotNull(view, "view");
                        WallpaperManager instance = WallpaperManager.getInstance(view.getContext());
                        IBinder windowToken = view.getWindowToken();
                        String str = "android.wallpaper.tap";
                        MotionEvent access$getCurrentEvent$p = currentEvent;
                        if (access$getCurrentEvent$p == null) {
                            Intrinsics.throwNpe();
                        }
                        int x = (int) access$getCurrentEvent$p.getX();
                        access$getCurrentEvent$p = currentEvent;
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
            this.desktop.getPages().add(0, getItemLayout());
            notifyDataSetChanged();
        }

        public final void addPageRight() {
            this.desktop.getPages().add(getItemLayout());
            notifyDataSetChanged();
        }

        public final void removePage(int position, boolean deleteItems) {
            if (deleteItems) {
                for (View v : ((CellContainer) this.desktop.getPages().get(position)).getAllCells()) {
                    Object item = v.getTag();
                    if (item instanceof Item) {
                        Home.Companion.getDb().deleteItem((Item) item, true);
                    }
                }
            }
            this.desktop.getPages().remove(position);
            notifyDataSetChanged();
        }

        public int getItemPosition(@NotNull Object object) {
            Intrinsics.checkParameterIsNotNull(object, "object");
            return -2;
        }

        public int getCount() {
            return this.desktop.getPageCount();
        }

        public boolean isViewFromObject(@NotNull View p1, @NotNull Object p2) {
            Intrinsics.checkParameterIsNotNull(p1, "p1");
            Intrinsics.checkParameterIsNotNull(p2, "p2");
            return p1 == p2;
        }

        public void destroyItem(@NotNull ViewGroup container, int position, @NotNull Object object) {
            Intrinsics.checkParameterIsNotNull(container, "container");
            Intrinsics.checkParameterIsNotNull(object, "object");
            container.removeView((View) object);
        }

        @NotNull
        public Object instantiateItem(@NotNull ViewGroup container, int pos) {
            Intrinsics.checkParameterIsNotNull(container, "container");
            CellContainer layout = (CellContainer) this.desktop.getPages().get(pos);
            container.addView(layout);
            return layout;
        }

        public final void enterDesktopEditMode() {
            this.scaleFactor = 0.8f;
            this.translateFactor = (float) Tool.toPx(Setup.Companion.appSettings().getSearchBarEnable() ? 20 : 40);
            for (CellContainer v : this.desktop.getPages()) {
                v.setBlockTouch(true);
                v.animateBackgroundShow();
                ViewPropertyAnimator translationY = v.animate().scaleX(this.scaleFactor).scaleY(this.scaleFactor).translationY(this.translateFactor);
                Intrinsics.checkExpressionValueIsNotNull(translationY, "v.animate().scaleX(scale…slationY(translateFactor)");
                translationY.setInterpolator(new AccelerateDecelerateInterpolator());
            }
            this.desktop.setInEditMode(true);
            if (this.desktop.getDesktopEditListener() != null) {
                OnDesktopEditListener desktopEditListener = this.desktop.getDesktopEditListener();
                if (desktopEditListener == null) {
                    Intrinsics.throwNpe();
                }
                desktopEditListener.onDesktopEdit();
            }
        }

        public final void exitDesktopEditMode() {
            this.scaleFactor = 1.0f;
            this.translateFactor = 0.0f;
            for (CellContainer v : this.desktop.getPages()) {
                v.setBlockTouch(false);
                v.animateBackgroundHide();
                ViewPropertyAnimator translationY = v.animate().scaleX(this.scaleFactor).scaleY(this.scaleFactor).translationY(this.translateFactor);
                Intrinsics.checkExpressionValueIsNotNull(translationY, "v.animate().scaleX(scale…slationY(translateFactor)");
                translationY.setInterpolator(new AccelerateDecelerateInterpolator());
            }
            this.desktop.setInEditMode(false);
            if (this.desktop.getDesktopEditListener() != null) {
                OnDesktopEditListener desktopEditListener = this.desktop.getDesktopEditListener();
                if (desktopEditListener == null) {
                    Intrinsics.throwNpe();
                }
                desktopEditListener.onFinishDesktopEdit();
            }
        }
    }

    @JvmOverloads
    public Desktop(@NotNull Context context) {
        super(context, null);
    }

    public Desktop(@NotNull Context c, @Nullable AttributeSet attr) {
        super(c, attr);
    }

    @NotNull
    public final List<CellContainer> getPages() {
        return this.pages;
    }

    @Nullable
    public final OnDesktopEditListener getDesktopEditListener() {
        return this.desktopEditListener;
    }

    public final void setDesktopEditListener(@Nullable OnDesktopEditListener v) {
        this.desktopEditListener = v;
    }

    public final boolean getInEditMode() {
        return this.inEditMode;
    }

    public final void setInEditMode(boolean v) {
        this.inEditMode = v;
    }

    public final int getPageCount() {
        return this.pageCount;
    }

    public final boolean isCurrentPageEmpty() {
        return getCurrentPage().getChildCount() == 0;
    }

    @NotNull
    public final CellContainer getCurrentPage() {
        return (CellContainer) this.pages.get(getCurrentItem());
    }

    public final void setPageIndicator(@NotNull PagerIndicator pageIndicator) {
        Intrinsics.checkParameterIsNotNull(pageIndicator, "pageIndicator");
        this.pageIndicator = pageIndicator;
    }

    public final void init() {
        if (!isInEditMode()) {
            this.pageCount = Home.Companion.getDb().getDesktop().size();
            if (this.pageCount == 0) {
                this.pageCount = 1;
            }
            setCurrentItem(Setup.Companion.appSettings().getDesktopPageCurrent());
        }
    }

    public final void initDesktopNormal(@NotNull Home home) {
        Intrinsics.checkParameterIsNotNull(home, "home");
        setAdapter(new DesktopAdapter(this, this));
        if (Setup.Companion.appSettings().isDesktopShowIndicator() && this.pageIndicator != null) {
            PagerIndicator pagerIndicator = this.pageIndicator;
            if (pagerIndicator == null) {
                Intrinsics.throwNpe();
            }
            pagerIndicator.setViewPager(this);
        }
        this.home = home;
        int columns = Setup.Companion.appSettings().getDesktopColumnCount();
        int rows = Setup.Companion.appSettings().getDesktopRowCount();
        List desktopItems = Home.Companion.getDb().getDesktop();
        int size = desktopItems.size();
        int pageCount = 0;
        while (pageCount < size) {
            if (this.pages.size() > pageCount) {
                ((CellContainer) this.pages.get(pageCount)).removeAllViews();
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

    public final void initDesktopShowAll(@NotNull Context c, @NotNull Home home) {
        Desktop desktop = this;
        Context context = c;
        Home home2 = home;
        Intrinsics.checkParameterIsNotNull(context, "c");
        Intrinsics.checkParameterIsNotNull(home2, "home");
        ArrayList apps = new ArrayList();
        for (App app : Setup.Companion.appLoader().getAllApps(context, false)) {
            apps.add(Item.newAppItem(app));
        }
        int appsSize = apps.size();
        desktop.pageCount = 0;
        int columns = Setup.Companion.appSettings().getDesktopColumnCount();
        int rows = Setup.Companion.appSettings().getDesktopRowCount();
        appsSize -= columns * rows;
        while (true) {
            if (appsSize < columns * rows) {
                if (appsSize <= (-(columns * rows))) {
                    break;
                }
            }
            desktop.pageCount++;
        }
        setAdapter(new DesktopAdapter(desktop, desktop));
        if (Setup.Companion.appSettings().isDesktopShowIndicator() && desktop.pageIndicator != null) {
            PagerIndicator pagerIndicator = desktop.pageIndicator;
            if (pagerIndicator == null) {
                Intrinsics.throwNpe();
            }
            pagerIndicator.setViewPager(desktop);
        }
        desktop.home = home2;

        for (int i = 0; i < pageCount; i++) {
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
        this.pageCount++;
        int previousPage = getCurrentItem();
        SmoothPagerAdapter adapter = getAdapter();
        if (adapter == null) {
            throw new TypeCastException("null cannot be cast to non-null type com.benny.openlauncher.widget.Desktop.DesktopAdapter");
        }
        ((DesktopAdapter) adapter).addPageRight();
        setCurrentItem(previousPage + 1);
        if (!Setup.Companion.appSettings().isDesktopHideGrid()) {
            for (CellContainer cellContainer : this.pages) {
                cellContainer.setHideGrid(!showGrid);
            }
        }
        PagerIndicator pagerIndicator = this.pageIndicator;
        if (pagerIndicator == null) {
            Intrinsics.throwNpe();
        }
        pagerIndicator.invalidate();
    }

    public final void addPageLeft(boolean showGrid) {
        this.pageCount++;
        int previousPage = getCurrentItem();
        SmoothPagerAdapter adapter = getAdapter();
        if (adapter == null) {
            throw new TypeCastException("null cannot be cast to non-null type com.benny.openlauncher.widget.Desktop.DesktopAdapter");
        }
        ((DesktopAdapter) adapter).addPageLeft();
        setCurrentItem(previousPage + 1, false);
        setCurrentItem(previousPage - 1);
        if (!Setup.Companion.appSettings().isDesktopHideGrid()) {
            for (CellContainer cellContainer : this.pages) {
                cellContainer.setHideGrid(!showGrid);
            }
        }
        PagerIndicator pagerIndicator = this.pageIndicator;
        if (pagerIndicator == null) {
            Intrinsics.throwNpe();
        }
        pagerIndicator.invalidate();
    }

    public final void removeCurrentPage() {
        if (Setup.Companion.appSettings().getDesktopStyle() != DesktopMode.INSTANCE.getSHOW_ALL_APPS()) {
            this.pageCount--;
            int previousPage = getCurrentItem();
            SmoothPagerAdapter adapter = getAdapter();
            if (adapter == null) {
                throw new TypeCastException("null cannot be cast to non-null type com.benny.openlauncher.widget.Desktop.DesktopAdapter");
            }
            ((DesktopAdapter) adapter).removePage(getCurrentItem(), true);
            for (CellContainer v : this.pages) {
                v.setAlpha(0.0f);
                v.animate().alpha(1.0f);
                v.setScaleX(0.85f);
                v.setScaleY(0.85f);
                v.animateBackgroundShow();
            }
            if (this.pageCount == 0) {
                addPageRight(false);
                adapter = getAdapter();
                if (adapter == null) {
                    throw new TypeCastException("null cannot be cast to non-null type com.benny.openlauncher.widget.Desktop.DesktopAdapter");
                }
                ((DesktopAdapter) adapter).exitDesktopEditMode();
            } else {
                setCurrentItem(previousPage);
                PagerIndicator pagerIndicator = this.pageIndicator;
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
        DragState state = getCurrentPage().peekItemAndSwap(x, y, this.coordinate);
        if (previousDragPoint != null && !previousDragPoint.equals(coordinate)) {
            launcher = Home.Companion.getLauncher();
            if (launcher != null) {
                dragNDropView = launcher.getDragNDropView();
                if (dragNDropView != null) {
                    dragNDropView.cancelFolderPreview();
                }
            }
        }
        this.previousDragPoint.set(this.coordinate.x, this.coordinate.y);
        switch (state) {
            case CurrentNotOccupied:
                getCurrentPage().projectImageOutlineAt(this.coordinate, DragNDropHandler.cachedDragBitmap);
                break;
            case OutOffRange:
            case ItemViewNotFound:
                break;
            case CurrentOccupied:
                Object action;
                Home launcher2;
                DragNDropLayout dragNDropView2;
                for (CellContainer page : this.pages) {
                    page.clearCachedOutlineBitmap();
                }
                launcher = Home.Companion.getLauncher();
                if (launcher != null) {
                    dragNDropView = launcher.getDragNDropView();
                    if (dragNDropView != null) {
                        action = dragNDropView.getDragAction();
                        if (!Action.WIDGET.equals(action) || !Action.ACTION.equals(action) && (getCurrentPage().coordinateToChildView(coordinate) instanceof AppItemView)) {
                            launcher2 = Home.Companion.getLauncher();
                            if (launcher2 != null) {
                                dragNDropView2 = launcher2.getDragNDropView();
                                if (dragNDropView2 != null) {
                                    dragNDropView2.showFolderPreviewAt(this, ((float) getCurrentPage().getCellWidth()) * (((float) this.coordinate.x) + 0.5f), (((float) getCurrentPage().getCellHeight()) * (((float) this.coordinate.y) + 0.5f)) - ((float) (Setup.Companion.appSettings().isDesktopShowLabel() ? Tool.toPx(7) : 0)));
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
                        if (Setup.Companion.appSettings().isDesktopShowLabel()) {
                        }
                        dragNDropView2.showFolderPreviewAt(this, ((float) getCurrentPage().getCellWidth()) * (((float) this.coordinate.x) + 0.5f), (((float) getCurrentPage().getCellHeight()) * (((float) this.coordinate.y) + 0.5f)) - ((float) (Setup.Companion.appSettings().isDesktopShowLabel() ? Tool.toPx(7) : 0)));
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
        this.previousPage = getCurrentItem();
        this.previousItemView = v;
        this.previousItem = item;
        getCurrentPage().removeView(v);
    }

    public void revertLastItem() {
        if (this.previousItemView != null) {
            SmoothPagerAdapter adapter = getAdapter();
            Intrinsics.checkExpressionValueIsNotNull(adapter, "adapter");
            if (adapter.getCount() >= this.previousPage && this.previousPage > -1) {
                CellContainer cellContainer = (CellContainer) this.pages.get(this.previousPage);
                View view = this.previousItemView;
                if (view == null) {
                    Intrinsics.throwNpe();
                }
                cellContainer.addViewToGrid(view);
                this.previousItem = (Item) null;
                this.previousItemView = (View) null;
                this.previousPage = -1;
            }
        }
    }

    public void consumeRevert() {
        this.previousItem = (Item) null;
        this.previousItemView = (View) null;
        this.previousPage = -1;
    }

    public boolean addItemToPage(@NotNull Item item, int page) {
        Intrinsics.checkParameterIsNotNull(item, "item");
        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.Companion.appSettings().isDesktopShowLabel(), (DesktopCallBack) this, Setup.Companion.appSettings().getDesktopIconSize());
        if (itemView == null) {
            Home.Companion.getDb().deleteItem(item, true);
            return false;
        }
        item.locationInLauncher = 0;
        ((CellContainer) this.pages.get(page)).addViewToGrid(itemView, item.x, item.y, item.spanX, item.spanY);
        return true;
    }

    public boolean addItemToPoint(@NotNull Item item, int x, int y) {
        CellContainer.LayoutParams positionToLayoutPrams = getCurrentPage().coordinateToLayoutParams(x, y, item.spanX, item.spanY);
        if (positionToLayoutPrams == null) {
            return false;
        }
        item.locationInLauncher = 0;
        item.x = positionToLayoutPrams.getX();
        item.y = positionToLayoutPrams.getY();
        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.Companion.appSettings().isDesktopShowLabel(), (DesktopCallBack) this, Setup.Companion.appSettings().getDesktopIconSize());
        if (itemView != null) {
            itemView.setLayoutParams(positionToLayoutPrams);
            getCurrentPage().addView(itemView);
        }
        return true;
    }

    public boolean addItemToCell(@NotNull Item item, int x, int y) {
        Intrinsics.checkParameterIsNotNull(item, "item");
        item.locationInLauncher = 0;
        item.x = x;
        item.y = y;
        View itemView = ItemViewFactory.getItemView(getContext(), item, Setup.Companion.appSettings().isDesktopShowLabel(), (DesktopCallBack) this, Setup.Companion.appSettings().getDesktopIconSize());
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
        Intrinsics.checkParameterIsNotNull(view, "view");
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
            WallpaperManager.getInstance(getContext()).setWallpaperOffsets(getWindowToken(), (((float) position) + offset) / ((float) (this.pageCount - 1)), 0.0f);
            super.onPageScrolled(position, offset, offsetPixels);
        }
    }

    @NotNull
    public WindowInsets onApplyWindowInsets(@NotNull WindowInsets insets) {
        Intrinsics.checkParameterIsNotNull(insets, "insets");
        if (VERSION.SDK_INT >= 20) {
            Companion.setTopInset(insets.getSystemWindowInsetTop());
            Companion.setBottomInset(insets.getSystemWindowInsetBottom());
            Home launcher = Home.Companion.getLauncher();
            if (launcher == null) {
                Intrinsics.throwNpe();
            }
            launcher.updateHomeLayout();
        }
        return insets;
    }
}