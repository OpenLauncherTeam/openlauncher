package com.benny.openlauncher.widget;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.LauncherAction;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DesktopCallBack;
import com.benny.openlauncher.viewutil.ItemViewFactory;
import com.benny.openlauncher.model.SmoothPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import in.championswimmer.sfg.lib.SimpleFingerGestures;

public class Desktop extends SmoothViewPager implements OnDragListener, DesktopCallBack {
    public List<CellContainer> pages = new ArrayList<>();
    public OnDesktopEditListener desktopEditListener;
    public View previousItemView;
    public Item previousItem;
    public boolean inEditMode;
    public int previousPage = -1;
    public int pageCount;

    private float startPosX;
    private float startPosY;
    private Home home;
    private PagerIndicator pageIndicator;

    public static int topInsert;

    public Desktop(Context c, AttributeSet attr) {
        super(c, attr);
    }

    public Desktop(Context c) {
        super(c);
    }

    public void setDesktopEditListener(OnDesktopEditListener desktopEditListener) {
        this.desktopEditListener = desktopEditListener;
    }

    public void setPageIndicator(PagerIndicator pageIndicator) {
        this.pageIndicator = pageIndicator;
    }

    public void init(Context c) {
        if (isInEditMode()) {
            return;
        }

        if (LauncherSettings.getInstance(c).generalSettings.desktopMode == DesktopMode.ShowAllApps) {
            initDesktopShowAll(c);
        }

        pageCount = Home.launcher.db.getDesktop().size();
        if (pageCount == 0) {
            pageCount = 1;
        }

        setOnDragListener(this);
        setCurrentItem(LauncherSettings.getInstance(c).generalSettings.desktopHomePage);
    }

    public void initDesktopShowAll(Context c) {
        LauncherSettings.GeneralSettings generalSettings = LauncherSettings.getInstance(c).generalSettings;
        List<AppManager.App> apps = AppManager.getInstance(c).getApps();
        int appsSize = apps.size();


        // reset page count
        pageCount = 0;
        while ((appsSize = appsSize - (generalSettings.desktopGridY * generalSettings.desktopGridX)) >= (generalSettings.desktopGridY * generalSettings.desktopGridX) || (appsSize > -(generalSettings.desktopGridY * generalSettings.desktopGridX))) {
            pageCount++;
        }

        // fill the desktop adapter
        for (int i = 0; i < pageCount; i++) {
            if (pages.size() <= pageCount) break;
            for (int x = 0; x < generalSettings.desktopGridX; x++) {
                for (int y = 0; y < generalSettings.desktopGridY; y++) {
                    int pagePos = y * generalSettings.desktopGridY + x;
                    int pos = generalSettings.desktopGridY * generalSettings.desktopGridX * i + pagePos;
                    if (!(pos >= apps.size())) {
                        Item appItem = Item.newAppItem(apps.get(pos));
                        appItem.x = x;
                        appItem.y = y;
                        addItemToPage(appItem, i);
                    }
                }
            }
        }
    }

    public void initDesktopNormal(Home home) {
        setAdapter(new DesktopAdapter(this));
        if (LauncherSettings.getInstance(getContext()).generalSettings.showIndicator && pageIndicator != null) {
            pageIndicator.setViewPager(this);
        }
        this.home = home;

        int column = LauncherSettings.getInstance(getContext()).generalSettings.desktopGridX;
        int row = LauncherSettings.getInstance(getContext()).generalSettings.desktopGridY;
        List<List<Item>> desktopItems = Home.launcher.db.getDesktop();
        for (int pageCount = 0; pageCount < desktopItems.size(); pageCount++) {
            if (pages.size() <= pageCount) break;
            pages.get(pageCount).removeAllViews();
            List<Item> items = desktopItems.get(pageCount);
            for (int j = 0; j < items.size(); j++) {
                Item item = items.get(j);
                if (((item.x + item.spanX) <= column) && ((item.y + item.spanY) <= row)) {
                    addItemToPage(item, pageCount);
                }
            }
        }
    }

    public void addPageRight() {
        pageCount++;

        final int previousPage = getCurrentItem();
        ((DesktopAdapter) getAdapter()).addPageRight();
        setCurrentItem(previousPage + 1);

        for (CellContainer cellContainer : pages) {
            cellContainer.setHideGrid(false);
        }
        pageIndicator.invalidate();
    }

    public void addPageLeft() {
        pageCount++;

        final int previousPage = getCurrentItem();
        ((DesktopAdapter) getAdapter()).addPageLeft();
        setCurrentItem(previousPage + 1, false);
        setCurrentItem(previousPage - 1);

        for (CellContainer cellContainer : pages) {
            cellContainer.setHideGrid(false);
        }
        pageIndicator.invalidate();
    }

    public void removeCurrentPage() {
        if (pageCount == 1) return;
        if (LauncherSettings.getInstance(getContext()).generalSettings.desktopMode == DesktopMode.ShowAllApps)
            return;
        pageCount--;

        int previousPage = getCurrentItem();
        ((DesktopAdapter) getAdapter()).removePage(getCurrentItem());

        for (CellContainer v : pages) {
            v.setAlpha(0);
            v.animate().alpha(1);
            v.setScaleX(0.85f);
            v.setScaleY(0.85f);
            v.animateBackgroundShow();
        }

        setCurrentItem(previousPage);
        pageIndicator.invalidate();
    }

    @Override
    public boolean onDrag(View p1, DragEvent p2) {
        switch (p2.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                Tool.print("ACTION_DRAG_STARTED");
                return true;
            case DragEvent.ACTION_DRAG_ENTERED:
                Tool.print("ACTION_DRAG_ENTERED");
                return true;
            case DragEvent.ACTION_DRAG_EXITED:
                Tool.print("ACTION_DRAG_EXITED");
                return true;
            case DragEvent.ACTION_DRAG_LOCATION:
                Tool.print("ACTION_DRAG_LOCATION");
                getCurrentPage().peekItemAndSwap(p2);
                return true;
            case DragEvent.ACTION_DROP:
                Tool.print("ACTION_DROP");
                Intent intent = p2.getClipData().getItemAt(0).getIntent();
                intent.setExtrasClassLoader(Item.class.getClassLoader());
                Item item = intent.getParcelableExtra("mDragData");

                // this statement makes sure that adding an app multiple times from the app drawer works
                // the app will get a new id every time
                if (((DragAction) p2.getLocalState()).action == DragAction.Action.APP_DRAWER) {
                    item.resetID();
                }

                if (addItemToPoint(item, (int) p2.getX(), (int) p2.getY())) {
                    home.desktop.consumeRevert();
                    home.dock.consumeRevert();

                    // add the item to the database
                    home.db.setItem(item, getCurrentItem(), 1);
                } else {
                    Point pos = getCurrentPage().touchPosToCoordinate((int) p2.getX(), (int) p2.getY(), item.spanX, item.spanY, false);
                    View itemView = getCurrentPage().coordinateToChildView(pos);
                    if (itemView != null) {
                        if (Desktop.handleOnDropOver(home, item, (Item) itemView.getTag(), itemView, getCurrentPage(), getCurrentItem(), 1, this)) {
                            home.desktop.consumeRevert();
                            home.dock.consumeRevert();
                        } else {
                            Toast.makeText(getContext(), R.string.toast_notenoughspace, Toast.LENGTH_SHORT).show();
                            home.dock.revertLastItem();
                            home.desktop.revertLastItem();
                        }
                    } else {
                        Toast.makeText(getContext(), R.string.toast_notenoughspace, Toast.LENGTH_SHORT).show();
                        home.dock.revertLastItem();
                        home.desktop.revertLastItem();
                    }
                }
                return true;
            case DragEvent.ACTION_DRAG_ENDED:
                Tool.print("ACTION_DRAG_ENDED");
                return true;
        }
        return false;
    }

    public CellContainer getCurrentPage() {
        return pages.get(getCurrentItem());
    }

    @Override
    public void setLastItem(Object... args) {
        // args stores the item in [0] and the view reference in [1]
        Item item = (Item) args[0];
        View v = (View) args[1];

        previousPage = getCurrentItem();
        previousItemView = v;
        previousItem = item;
        getCurrentPage().removeView(v);
    }

    @Override
    public void consumeRevert() {
        previousItem = null;
        previousItemView = null;
        previousPage = -1;
    }

    @Override
    public void revertLastItem() {
        if (previousItemView != null && getAdapter().getCount() >= previousPage && previousPage > -1) {
            getCurrentPage().addViewToGrid(previousItemView);
            previousItem = null;
            previousItemView = null;
            previousPage = -1;
        }
    }

    @Override
    public boolean addItemToPage(final Item item, int page) {
        int flag = LauncherSettings.getInstance(getContext()).generalSettings.desktopShowLabel ? ItemViewFactory.NO_FLAGS : ItemViewFactory.NO_LABEL;
        View itemView = ItemViewFactory.getItemView(getContext(), this, item, flag);

        if (itemView == null) {
            home.db.deleteItem(item);
            return false;
        } else {
            pages.get(page).addViewToGrid(itemView, item.x, item.y, item.spanX, item.spanY);
            return true;
        }
    }

    /**
     * Add an item to the specified position on the current page
     *
     * @param item - the item to add
     * @param x    - x position in screen coordinates of the centre of the item
     * @param y    - y position in screen coordinates of the centre of the item
     * @return - true if the item was added, false if not enough space
     */
    @Override
    public boolean addItemToPoint(final Item item, int x, int y) {
        CellContainer.LayoutParams positionToLayoutPrams = getCurrentPage().positionToLayoutPrams(x, y, item.spanX, item.spanY);
        if (positionToLayoutPrams != null) {
            item.x = positionToLayoutPrams.x;
            item.y = positionToLayoutPrams.y;

            int flag = LauncherSettings.getInstance(getContext()).generalSettings.desktopShowLabel ? ItemViewFactory.NO_FLAGS : ItemViewFactory.NO_LABEL;
            View itemView = ItemViewFactory.getItemView(getContext(), this, item, flag);

            if (itemView != null) {
                itemView.setLayoutParams(positionToLayoutPrams);
                getCurrentPage().addView(itemView);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addItemToCell(final Item item, int x, int y) {
        item.x = x;
        item.y = y;

        int flag = LauncherSettings.getInstance(getContext()).generalSettings.desktopShowLabel ? ItemViewFactory.NO_FLAGS : ItemViewFactory.NO_LABEL;
        View itemView = ItemViewFactory.getItemView(getContext(), this, item, flag);

        if (itemView != null) {
            getCurrentPage().addViewToGrid(itemView, item.x, item.y, item.spanX, item.spanY);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void removeItem(AppItemView view) {
        getCurrentPage().removeViewInLayout(view);
    }

    @Override
    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        if (isInEditMode()) {
            return;
        }
        WallpaperManager.getInstance(getContext()).setWallpaperOffsets(getWindowToken(), (position + offset) / (pageCount - 1), 0);
        super.onPageScrolled(position, offset, offsetPixels);
    }

    public class DesktopAdapter extends SmoothPagerAdapter {
        private MotionEvent currentEvent;
        private Desktop desktop;
        float scaleFactor = 1f;

        public DesktopAdapter(Desktop desktop) {
            this.desktop = desktop;
            desktop.pages = new ArrayList<>();
            for (int i = 0; i < getCount(); i++) {
                desktop.pages.add(getItemLayout());
            }
        }

        public void addPageLeft() {
            desktop.pages.add(0, getItemLayout());
            notifyDataSetChanged();
        }

        public void addPageRight() {
            desktop.pages.add(getItemLayout());
            notifyDataSetChanged();
        }

        public void removePage(int position) {
            desktop.pages.remove(position);
            notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return desktop.pageCount;
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
        public Object instantiateItem(ViewGroup container, int pos) {
            ViewGroup layout = desktop.pages.get(pos);
            container.addView(layout);
            return layout;
        }

        private SimpleFingerGestures.OnFingerGestureListener getGestureListener() {
            return new SimpleFingerGestures.OnFingerGestureListener() {
                @Override
                public boolean onSwipeUp(int i, long l, double v) {
                    if (getResources().getIntArray(R.array.gestureValues)[LauncherSettings.getInstance(getContext()).generalSettings.swipeUp] != 0) {
                        LauncherAction.RunAction(LauncherAction.actionItems[getResources().getIntArray(R.array.gestureValues)[LauncherSettings.getInstance(getContext()).generalSettings.swipeUp] - 1].label, desktop.getContext());
                    }
                    return true;
                }

                @Override
                public boolean onSwipeDown(int i, long l, double v) {
                    if (getResources().getIntArray(R.array.gestureValues)[LauncherSettings.getInstance(getContext()).generalSettings.swipeDown] != 0) {
                        LauncherAction.RunAction(LauncherAction.actionItems[getResources().getIntArray(R.array.gestureValues)[LauncherSettings.getInstance(getContext()).generalSettings.swipeDown] - 1].label, desktop.getContext());
                    }
                    return true;
                }

                @Override
                public boolean onSwipeLeft(int i, long l, double v) {
                    return false;
                }

                @Override
                public boolean onSwipeRight(int i, long l, double v) {
                    return false;
                }

                @Override
                public boolean onPinch(int i, long l, double v) {
                    if (getResources().getIntArray(R.array.gestureValues)[LauncherSettings.getInstance(getContext()).generalSettings.pinch] != 0) {
                        LauncherAction.RunAction(LauncherAction.actionItems[getResources().getIntArray(R.array.gestureValues)[LauncherSettings.getInstance(getContext()).generalSettings.pinch] - 1].label, desktop.getContext());
                    }
                    return true;
                }

                @Override
                public boolean onUnpinch(int i, long l, double v) {
                    if (getResources().getIntArray(R.array.gestureValues)[LauncherSettings.getInstance(getContext()).generalSettings.unPinch] != 0) {
                        LauncherAction.RunAction(LauncherAction.actionItems[getResources().getIntArray(R.array.gestureValues)[LauncherSettings.getInstance(getContext()).generalSettings.unPinch] - 1].label, desktop.getContext());
                    }
                    return true;
                }

                @Override
                public boolean onDoubleTap(int i) {
                    if (getResources().getIntArray(R.array.gestureValues)[LauncherSettings.getInstance(getContext()).generalSettings.doubleClick] != 0) {
                        LauncherAction.RunAction(LauncherAction.actionItems[getResources().getIntArray(R.array.gestureValues)[LauncherSettings.getInstance(getContext()).generalSettings.doubleClick] - 1].label, desktop.getContext());
                    }
                    return true;
                }
            };
        }

        private Item getItemFromCoordinate(Point coordinate) {
            List<List<Item>> desktopItems = Home.launcher.db.getDesktop();
            List<Item> pageData = desktopItems.get(desktop.getCurrentItem());
            for (int i = 0; i < pageData.size(); i++) {
                Item item = pageData.get(i);
                if (item.x == coordinate.x && item.y == coordinate.y && item.spanX == 1 && item.spanY == 1) {
                    return pageData.get(i);
                }
            }
            return null;
        }

        private CellContainer getItemLayout() {
            CellContainer layout = new CellContainer(desktop.getContext());
            layout.setSoundEffectsEnabled(false);

            SimpleFingerGestures mySfg = new SimpleFingerGestures();
            mySfg.setOnFingerGestureListener(getGestureListener());

            layout.gestures = mySfg;
            layout.onItemRearrangeListener = new CellContainer.OnItemRearrangeListener() {
                @Override
                public void onItemRearrange(Point from, Point to) {
                    Item itemFromCoordinate = getItemFromCoordinate(from);
                    if (itemFromCoordinate == null) return;
                    itemFromCoordinate.x = to.x;
                    itemFromCoordinate.y = to.y;
                }
            };
            layout.setGridSize(LauncherSettings.getInstance(desktop.getContext()).generalSettings.desktopGridX, LauncherSettings.getInstance(desktop.getContext()).generalSettings.desktopGridY);
            layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    scaleFactor = 1f;
                    for (final CellContainer v : desktop.pages) {
                        v.blockTouch = false;
                        v.animateBackgroundHide();
                        v.animate().scaleX(scaleFactor).scaleY(scaleFactor).setInterpolator(new AccelerateDecelerateInterpolator());
                    }
                    if (!desktop.inEditMode && currentEvent != null) {
                        WallpaperManager.getInstance(view.getContext()).sendWallpaperCommand(view.getWindowToken(), WallpaperManager.COMMAND_TAP, (int) currentEvent.getX(), (int) currentEvent.getY(), 0, null);
                    }
                    desktop.inEditMode = false;
                    if (desktop.desktopEditListener != null) {
                        desktop.desktopEditListener.onFinishDesktopEdit();
                    }
                }
            });
            layout.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    scaleFactor = 0.85f;
                    for (CellContainer v : desktop.pages) {
                        v.blockTouch = true;
                        v.animateBackgroundShow();
                        v.animate().scaleX(scaleFactor).scaleY(scaleFactor).setInterpolator(new AccelerateDecelerateInterpolator());
                    }
                    desktop.inEditMode = true;
                    if (desktop.desktopEditListener != null) {
                        desktop.desktopEditListener.onDesktopEdit();
                    }
                    return true;
                }
            });

            return layout;
        }
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            topInsert = insets.getSystemWindowInsetTop();
            return insets;
        }
        return insets;
    }

    public static boolean handleOnDropOver(Home home, Item dropItem, Item item, View itemView, ViewGroup parent, int page, int desktop, DesktopCallBack callBack) {
        if (item == null || dropItem == null) {
            return false;
        }

        switch (item.type) {
            case APP:
            case SHORTCUT:
                if (dropItem.type == Item.Type.APP || dropItem.type == Item.Type.SHORTCUT) {
                    parent.removeView(itemView);

                    // create a new group item
                    Item group = Item.newGroupItem();
                    group.items.add(item);
                    group.items.add(dropItem);
                    group.name = (home.getString(R.string.unnamed));
                    group.x = item.x;
                    group.y = item.y;

                    // hide the apps added to the group
                    home.db.updateItem(item, 0);
                    home.db.updateItem(dropItem, 0);

                    // add the item to the database
                    home.db.setItem(group, page, desktop);

                    callBack.addItemToPage(group, page);

                    home.desktop.consumeRevert();
                    home.dock.consumeRevert();
                    return true;
                }
                break;
            case GROUP:
                if ((dropItem.type == Item.Type.APP || dropItem.type == Item.Type.SHORTCUT) && item.items.size() < GroupPopupView.GroupDef.maxItem) {
                    parent.removeView(itemView);

                    item.items.add(dropItem);

                    // hide the new app in the group
                    home.db.updateItem(dropItem, 0);

                    // add the item to the database
                    home.db.setItem(item, page, desktop);

                    callBack.addItemToPage(item, page);

                    home.desktop.consumeRevert();
                    home.dock.consumeRevert();
                    return true;
                }
                break;
        }
        return false;
    }

    public enum DesktopMode {
        Normal, ShowAllApps
    }

    public interface OnDesktopEditListener {
        void onDesktopEdit();

        void onFinishDesktopEdit();
    }
}
