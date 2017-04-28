package com.benny.openlauncher.widget;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
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
import java.util.Random;

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
            initShowAllApps(c);
        }

        pageCount = LauncherSettings.getInstance(c).desktopData.size();
        if (pageCount == 0) {
            pageCount = 1;
        }

        setOnDragListener(this);
        setCurrentItem(LauncherSettings.getInstance(c).generalSettings.desktopHomePage);
    }

    public void initShowAllApps(Context c) {
        LauncherSettings.GeneralSettings generalSettings = LauncherSettings.getInstance(c).generalSettings;
        LauncherSettings.getInstance(c).desktopData.clear();
        int pageCount = 0;
        List<AppManager.App> apps = AppManager.getInstance(c).getApps();
        int appsSize = apps.size();
        while ((appsSize = appsSize - (generalSettings.desktopGridY * generalSettings.desktopGridX)) >= (generalSettings.desktopGridY * generalSettings.desktopGridX) || (appsSize > -(generalSettings.desktopGridY * generalSettings.desktopGridX))) {
            pageCount++;
        }
        for (int i = 0; i < pageCount; i++) {
            ArrayList<Desktop.Item> items = new ArrayList<>();
            for (int x = 0; x < generalSettings.desktopGridX; x++) {
                for (int y = 0; y < generalSettings.desktopGridY; y++) {
                    int pagePos = y * generalSettings.desktopGridY + x;
                    final int pos = generalSettings.desktopGridY * generalSettings.desktopGridX * i + pagePos;
                    if (!(pos >= apps.size())) {
                        Desktop.Item appItem = Desktop.Item.newAppItem(apps.get(pos));
                        appItem.x = x;
                        appItem.y = y;
                        items.add(appItem);
                    }
                }
            }
            LauncherSettings.getInstance(c).desktopData.add(items);
        }
    }

    public void initDesktopItem(Home home) {
        setAdapter(new DesktopAdapter(this));
        if (LauncherSettings.getInstance(getContext()).generalSettings.showIndicator && pageIndicator != null)
            pageIndicator.setViewPager(this);
        this.home = home;
        int column = LauncherSettings.getInstance(getContext()).generalSettings.desktopGridX;
        int row = LauncherSettings.getInstance(getContext()).generalSettings.desktopGridY;
        for (int i = 0; i < LauncherSettings.getInstance(getContext()).desktopData.size(); i++) {
            if (pages.size() <= i) break;
            pages.get(i).removeAllViews();
            List<Item> items = LauncherSettings.getInstance(getContext()).desktopData.get(i);
            for (int j = 0; j < items.size(); j++) {
                Desktop.Item item = items.get(j);
                if (((item.x + item.spanX) <= column) && ((item.y + item.spanY) <= row)) {
                    addItemToPage(item, i);
                }
            }
        }
    }

    public void addPageRight() {
        LauncherSettings.getInstance(getContext()).desktopData.add(new ArrayList<Item>());
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
        LauncherSettings.getInstance(getContext()).desktopData.add(getCurrentItem(), new ArrayList<Item>());
        pageCount++;

        final int previousPage = getCurrentItem();
        ((DesktopAdapter) getAdapter()).addPageLeft();
        setCurrentItem(previousPage + 1,false);
        setCurrentItem(previousPage - 1);

        for (CellContainer cellContainer : pages) {
            cellContainer.setHideGrid(false);
        }
        pageIndicator.invalidate();
    }

    public void removeCurrentPage() {
        if (pageCount == 1) return;
        if (LauncherSettings.getInstance(getContext()).generalSettings.desktopMode == DesktopMode.ShowAllApps
                && LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).size() != 0)
            return;
        LauncherSettings.getInstance(getContext()).desktopData.remove(getCurrentItem());
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
                if (((DragAction) p2.getLocalState()).action == DragAction.Action.ACTION_APP_DRAWER) {
                    item.resetID();
                }

                if (addItemToPoint(item, (int) p2.getX(), (int) p2.getY())) {
                    home.desktop.consumeRevert();
                    home.desktopDock.consumeRevert();

                    // add the item to the database
                    home.db.setItem(item);
                } else {
                    Point pos = getCurrentPage().touchPosToCoordinate((int) p2.getX(), (int) p2.getY(), item.spanX, item.spanY, false);
                    View itemView = getCurrentPage().coordinateToChildView(pos);
                    if (itemView != null) {
                        if (Desktop.handleOnDropOver(home, item, (Item) itemView.getTag(), itemView, getCurrentPage(), getCurrentItem(), this)) {
                            home.desktop.consumeRevert();
                            home.desktopDock.consumeRevert();
                        } else {
                            Toast.makeText(getContext(), R.string.toast_notenoughspace, Toast.LENGTH_SHORT).show();
                            home.desktopDock.revertLastItem();
                            home.desktop.revertLastItem();
                        }
                    } else {
                        Toast.makeText(getContext(), R.string.toast_notenoughspace, Toast.LENGTH_SHORT).show();
                        home.desktopDock.revertLastItem();
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
        Desktop.Item item = (Desktop.Item) args[0];
        View v = (View) args[1];

        removeItemFromSettings(item);

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

            if (LauncherSettings.getInstance(getContext()).desktopData.size() < getCurrentItem() + 1) {
                LauncherSettings.getInstance(getContext()).desktopData.add(previousPage, new ArrayList<Item>());
            }
            LauncherSettings.getInstance(getContext()).desktopData.get(previousPage).add(previousItem);

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

    @Override
    public boolean addItemToPoint(final Item item, int x, int y) {
        CellContainer.LayoutParams positionToLayoutPrams = getCurrentPage().positionToLayoutPrams(x, y, item.spanX, item.spanY);
        if (positionToLayoutPrams != null) {
            item.x = positionToLayoutPrams.x;
            item.y = positionToLayoutPrams.y;
            if (LauncherSettings.getInstance(getContext()).desktopData.size() < getCurrentItem() + 1) {
                LauncherSettings.getInstance(getContext()).desktopData.add(getCurrentItem(), new ArrayList<Item>());
            }
            LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).add(item);

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
        if (LauncherSettings.getInstance(getContext()).desktopData.size() < getCurrentItem() + 1) {
            LauncherSettings.getInstance(getContext()).desktopData.add(getCurrentItem(), new ArrayList<Item>());
        }
        LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).add(item);

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

    @Override
    public void removeItemFromSettings(Item item) {
        LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).remove(item);
    }

    @Override
    public void addItemToSettings(Item item) {
        LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).add(item);
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

        private Desktop.Item getItemFromCoordinate(Point coordinate) {
            List<Item> pageData = LauncherSettings.getInstance(desktop.getContext()).desktopData.get(desktop.getCurrentItem());
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
            layout.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent ev) {
                    currentEvent = ev;

                    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                        startPosX = ev.getX();
                        startPosY = ev.getY();
                    }

                    if (ev.getAction() == MotionEvent.ACTION_UP) {
                        float minDist = 150f;
                        Tool.print((int) ev.getX(), (int) ev.getY());
                        if (startPosY - ev.getY() > minDist) {
                            if (LauncherSettings.getInstance(getContext()).generalSettings.swipe) {
                                Point p = Tool.convertPoint(new Point((int) ev.getX(), (int) ev.getY()), Desktop.this, Home.launcher.appDrawerController);
                                // FIXME: 1/22/2017 This seem weird, but the extra offset ( Tool.getNavBarHeight(getContext()) ) works on my phone
                                // FIXME: 1/22/2017 This part of the code is identical as the code in Desktop so will combine them later
                                Home.launcher.openAppDrawer(Desktop.this, p.x, p.y - Tool.getNavBarHeight(getContext()) / 2);
                            }
                        }
                    }
                    return false;
                }
            });
            layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!desktop.inEditMode && getResources().getIntArray(R.array.gestureValues)[LauncherSettings.getInstance(getContext()).generalSettings.singleClick] != 0) {
                        LauncherAction.RunAction(LauncherAction.actionItems[getResources().getIntArray(R.array.gestureValues)[LauncherSettings.getInstance(getContext()).generalSettings.singleClick] - 1].label, desktop.getContext());
                    }
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
                        v.setPivotY(v.getHeight() / 2 - Tool.dp2px(50, desktop.getContext()));
                        v.setPivotX(v.getWidth() / 2);

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

    public static boolean handleOnDropOver(Home home, Item dropItem, Item item, View itemView, ViewGroup parent, int page, DesktopCallBack callBack) {
        if (item == null || dropItem == null) {
            return false;
        }

        switch (item.type) {
            case APP:
            case SHORTCUT:
                if (dropItem.type == Desktop.Item.Type.APP || dropItem.type == Desktop.Item.Type.SHORTCUT) {
                    callBack.removeItemFromSettings(item);
                    parent.removeView(itemView);

                    // all of the code to create a new group item
                    Desktop.Item group = Desktop.Item.newGroupItem();
                    group.items.add(item);
                    group.items.add(dropItem);
                    group.name = (home.getString(R.string.unnamed));
                    group.x = item.x;
                    group.y = item.y;

                    // hide the apps added to the group
                    home.db.updateItem(item, 0);
                    home.db.updateItem(dropItem, 0);

                    // add the item to the database
                    home.db.setItem(group);

                    callBack.addItemToSettings(group);
                    callBack.addItemToPage(group, page);

                    home.desktop.consumeRevert();
                    home.desktopDock.consumeRevert();
                    return true;
                }
                break;
            case GROUP:
                if ((dropItem.type == Desktop.Item.Type.APP || dropItem.type == Desktop.Item.Type.SHORTCUT) && item.items.size() < GroupPopupView.GroupDef.maxItem) {
                    callBack.removeItemFromSettings(item);
                    parent.removeView(itemView);

                    item.items.add(dropItem);

                    // hide the new app in thr group
                    home.db.updateItem(dropItem, 0);

                    // add the item to the database
                    home.db.setItem(item);

                    callBack.addItemToSettings(item);
                    callBack.addItemToPage(item, page);

                    home.desktop.consumeRevert();
                    home.desktopDock.consumeRevert();
                    return true;
                }
                break;
        }
        return false;
    }

    public enum DesktopMode {
        Normal, ShowAllApps
    }

    public static class Item implements Parcelable {
        public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {

            @Override
            public Item createFromParcel(Parcel parcel) {
                return new Item(parcel);
            }

            @Override
            public Item[] newArray(int size) {
                return new Item[size];
            }
        };

        // all items need these values
        public int idValue;
        public Type type;
        public String name;
        public int x = 0;
        public int y = 0;

        // intent for shortcuts and apps
        public Intent appIntent;

        // list of items for groups
        public List<Desktop.Item> items;

        // int value for launcher action
        public int actionValue;

        // widget specific values
        public int widgetID;
        public int spanX = 1;
        public int spanY = 1;

        public Item() {
            Random random = new Random();
            idValue = random.nextInt();
        }

        public void resetID() {
            Random random = new Random();
            idValue = random.nextInt();
        }

        public Item(Parcel parcel) {
            idValue = parcel.readInt();
            type = Type.valueOf(parcel.readString());
            switch (type) {
                case APP:
                case SHORTCUT:
                    appIntent = Tool.getIntentFromString(parcel.readString());
                    break;
                case GROUP:
                    List<String> labels = new ArrayList<>();
                    parcel.readStringList(labels);
                    items = new ArrayList<>();
                    for (String s : labels) {
                        items.add(Home.launcher.db.getItem(Integer.parseInt(s)));
                    }
                    break;
                case WIDGET:
                    widgetID = parcel.readInt();
                    spanX = parcel.readInt();
                    spanY = parcel.readInt();
                    break;
            }
            name = parcel.readString();
        }

        @Override
        public boolean equals(Object object) {
            Item itemObject = (Item) object;
            return object != null
                    && itemObject.type == this.type
                    && itemObject.x == this.x
                    && itemObject.y == this.y
                    && this.idValue == itemObject.idValue;
        }

        public static Item newAppItem(AppManager.App app) {
            Desktop.Item item = new Item();
            item.type = Type.APP;
            item.appIntent = toIntent(app);
            return item;
        }

        public static Item newWidgetItem(int widgetID) {
            Desktop.Item item = new Item();
            item.type = Type.WIDGET;
            item.widgetID = widgetID;
            item.spanX = 1;
            item.spanY = 1;
            return item;
        }

        public static Item newShortcutItem(Context context, String name, Intent intent, Bitmap icon) {
            Desktop.Item item = new Item();
            item.type = Type.SHORTCUT;
            item.spanX = 1;
            item.spanY = 1;
            item.appIntent = intent;

            String iconID = Tool.saveIconAndReturnID(context, icon);
            intent.putExtra("shortCutIconID", iconID);
            intent.putExtra("shortCutName", name);
            return item;
        }

        public static Item newShortcutItem(Intent intent) {
            Desktop.Item item = new Item();
            item.type = Type.SHORTCUT;
            item.spanX = 1;
            item.spanY = 1;
            item.appIntent = intent;
            return item;
        }

        public static Item newActionItem(int action) {
            Desktop.Item item = new Item();
            item.type = Type.ACTION;
            item.spanX = 1;
            item.spanY = 1;
            item.actionValue = action;
            return item;
        }

        public static Item newGroupItem() {
            Desktop.Item item = new Item();
            item.type = Type.GROUP;
            item.spanX = 1;
            item.spanY = 1;
            item.items = new ArrayList<>();
            return item;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(idValue);
            out.writeString(type.toString());
            switch (type) {
                case APP:
                case SHORTCUT:
                    out.writeString(Tool.getIntentAsString(this.appIntent));
                    break;
                case GROUP:
                    List<String> labels = new ArrayList<>();
                    for (Item i : items) {
                        labels.add(Integer.toString(i.idValue));
                    }
                    out.writeStringList(labels);
                    break;
                case WIDGET:
                    out.writeInt(widgetID);
                    out.writeInt(spanX);
                    out.writeInt(spanY);
                    break;
            }
            out.writeString(name);
        }

        private static Intent toIntent(AppManager.App app) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName(app.packageName, app.className);
            return intent;
        }

        public enum Type {
            APP,
            SHORTCUT,
            GROUP,
            ACTION,
            WIDGET
        }
    }

    public interface OnDesktopEditListener {
        void onDesktopEdit();

        void onFinishDesktopEdit();
    }
}
