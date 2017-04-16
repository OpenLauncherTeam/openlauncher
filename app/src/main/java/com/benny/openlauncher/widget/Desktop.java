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
import com.benny.openlauncher.util.DatabaseHelper;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.LauncherAction;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DesktopCallBack;
import com.benny.openlauncher.viewutil.ItemViewFactory;
import com.bennyv5.smoothviewpager.SmoothPagerAdapter;
import com.bennyv5.smoothviewpager.SmoothViewPager;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import in.championswimmer.sfg.lib.SimpleFingerGestures;

public class Desktop extends SmoothViewPager implements OnDragListener, DesktopCallBack {
    public int pageCount;

    public List<CellContainer> pages = new ArrayList<>();

    public void setDesktopEditListener(OnDesktopEditListener desktopEditListener) {
        this.desktopEditListener = desktopEditListener;
    }

    public OnDesktopEditListener desktopEditListener;

    public boolean inEditMode;

    public View previousItemView;
    public Item previousItem;
    public int previousPage = -1;

    private float startPosX, startPosY;

    private Home home;

    private PagerIndicator pageIndicator;

    public void setPageIndicator(PagerIndicator pageIndicator) {
        this.pageIndicator = pageIndicator;
    }

    public Desktop(Context c, AttributeSet attr) {
        super(c, attr);
    }

    public Desktop(Context c) {
        super(c);
    }

    public void init(Context c) {
        if (isInEditMode()) return;

        pageCount = LauncherSettings.getInstance(c).desktopData.size();
        if (pageCount == 0)
            pageCount = 1;
        setAdapter(new DesktopAdapter(this));
        setOnDragListener(this);

        setCurrentItem(LauncherSettings.getInstance(c).generalSettings.desktopHomePage);
    }

    public void initDesktopItem(Home home) {
        this.home = home;
        int column = LauncherSettings.getInstance(getContext()).generalSettings.desktopGridX;
        int row = LauncherSettings.getInstance(getContext()).generalSettings.desktopGridY;
        for (int i = 0; i < LauncherSettings.getInstance(getContext()).desktopData.size(); i++) {
            if (pages.size() <= i) break;
            pages.get(i).removeAllViews();
            List<Item> items = LauncherSettings.getInstance(getContext()).desktopData.get(i);
            for (int j = 0; j < items.size(); j++) {
                Desktop.Item item = items.get(j);
                if (item.x < column && item.y < row)
                    addItemToPagePosition(item, i);
            }
        }
    }

    public void addPageRight() {
        addPageRight(false);
    }

    public void addPageRight(boolean hideGrid) {
        LauncherSettings.getInstance(getContext()).desktopData.add(new ArrayList<Item>());
        pageCount++;

        final int previousPage = getCurrentItem();
        ((DesktopAdapter) getAdapter()).addPageRight();
        setCurrentItem(previousPage + 1);

        for (CellContainer cellContainer : pages)
            cellContainer.setHideGrid(false);

        pageIndicator.invalidate();
    }

    public void addPageLeft() {
        LauncherSettings.getInstance(getContext()).desktopData.add(getCurrentItem(), new ArrayList<Item>());
        pageCount++;

        final int previousPage = getCurrentItem();
        ((DesktopAdapter) getAdapter()).addPageLeft();
        setCurrentItem(previousPage - 1);

        for (CellContainer cellContainer : pages)
            cellContainer.setHideGrid(false);

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
                switch (((DragAction) p2.getLocalState()).action) {
                    case ACTION_APP:
                    case ACTION_GROUP:
                    case ACTION_APP_DRAWER:
                    case ACTION_WIDGET:
                    case ACTION_SHORTCUT:
                    case ACTION_LAUNCHER:
                        return true;
                }
                return true;
            case DragEvent.ACTION_DRAG_ENTERED:
                Tool.print("ACTION_DRAG_ENTERED");
                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                Tool.print("ACTION_DRAG_EXITED");
                return true;

            case DragEvent.ACTION_DRAG_LOCATION:
                getCurrentPage().peekItemAndSwap(p2);
                return true;

            case DragEvent.ACTION_DROP:
                Tool.print("ACTION_DROP");
                Intent intent = p2.getClipData().getItemAt(0).getIntent();
                intent.setExtrasClassLoader(Item.class.getClassLoader());
                Item item = intent.getParcelableExtra("mDragData");
                if (item.type == Item.Type.WIDGET) {
                    if (addItemToPosition(item, (int) p2.getX(), (int) p2.getY())) {
                        home.desktop.consumeRevert();
                        home.desktopDock.consumeRevert();
                    } else {
                        Toast.makeText(getContext(), R.string.toast_notenoughspace, Toast.LENGTH_SHORT).show();
                        home.desktopDock.revertLastItem();
                        home.desktop.revertLastItem();
                    }
                }
                if (item.type == Desktop.Item.Type.APP || item.type == Item.Type.GROUP || item.type == Item.Type.SHORTCUT || item.type == Item.Type.ACTION) {
                    if (addItemToPosition(item, (int) p2.getX(), (int) p2.getY())) {
                        home.desktop.consumeRevert();
                        home.desktopDock.consumeRevert();
                    } else {
                        Point pos = getCurrentPage().touchPosToCoordinate((int) p2.getX(), (int) p2.getY(), item.spanX, item.spanY, false);
                        View itemView = getCurrentPage().coordinateToChildView(pos);

                        if (itemView != null)
                            if (Desktop.handleOnDropOver(home, item, (Item) itemView.getTag(), itemView, getCurrentPage(), getCurrentItem(), this)) {
                                home.desktop.consumeRevert();
                                home.desktopDock.consumeRevert();
                            } else {
                                Toast.makeText(getContext(), R.string.toast_notenoughspace, Toast.LENGTH_SHORT).show();
                                home.desktopDock.revertLastItem();
                                home.desktop.revertLastItem();
                            }
                        else {
                            Toast.makeText(getContext(), R.string.toast_notenoughspace, Toast.LENGTH_SHORT).show();
                            home.desktopDock.revertLastItem();
                            home.desktop.revertLastItem();
                        }
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

    /**
     * @param args array storing the item(pos 0) and view ref(pos 1).
     */
    @Override
    public void setLastItem(Object... args) {
        View v = (View) args[1];
        Desktop.Item item = (Desktop.Item) args[0];

        //Remove the item from settings
        removeItemFromSettings(item);
        //end

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

            if (LauncherSettings.getInstance(getContext()).desktopData.size() < getCurrentItem() + 1)
                LauncherSettings.getInstance(getContext()).desktopData.add(previousPage, new ArrayList<Item>());
            LauncherSettings.getInstance(getContext()).desktopData.get(previousPage).add(previousItem);

            previousItem = null;
            previousItemView = null;
            previousPage = -1;
        }
    }

    @Override
    public void addItemToPagePosition(final Item item, int page) {
        if (item.isInvalidate) return;

        int flag = LauncherSettings.getInstance(getContext()).generalSettings.desktopShowLabel ? ItemViewFactory.NO_FLAGS : ItemViewFactory.NO_LABEL;
        View itemView = ItemViewFactory.getItemView(getContext(), this, item, flag);

        if (itemView == null)
            item.invalidate();
            //LauncherSettings.getInstance(getContext()).desktopData.get(page).remove(item);
        else
            pages.get(page).addViewToGrid(itemView, item.x, item.y, item.spanX, item.spanY);
    }

    @Override
    public boolean addItemToPosition(final Item item, int x, int y) {
        CellContainer.LayoutParams positionToLayoutPrams = getCurrentPage().positionToLayoutPrams(x, y, item.spanX, item.spanY);
        if (positionToLayoutPrams != null) {
            //Add the item to settings
            item.x = positionToLayoutPrams.x;
            item.y = positionToLayoutPrams.y;
            if (LauncherSettings.getInstance(getContext()).desktopData.size() < getCurrentItem() + 1)
                LauncherSettings.getInstance(getContext()).desktopData.add(getCurrentItem(), new ArrayList<Item>());
            LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).add(item);
            //end

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
    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        if (isInEditMode()) return;

        WallpaperManager.getInstance(getContext()).setWallpaperOffsets(getWindowToken(), (float) (position + offset) / (pageCount - 1), 0);
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

        float scaleFactor = 1f;

        private MotionEvent currentEvent;

        private Desktop desktop;

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
                if (item.x == coordinate.x && item.y == coordinate.y && item.spanX == 1 && item.spanY == 1)
                    return pageData.get(i);
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
                    if (!desktop.inEditMode)
                        if (currentEvent != null)
                            WallpaperManager.getInstance(view.getContext()).sendWallpaperCommand(view.getWindowToken(), WallpaperManager.COMMAND_TAP, (int) currentEvent.getX(), (int) currentEvent.getY(), 0, null);

                    desktop.inEditMode = false;
                    if (desktop.desktopEditListener != null)
                        desktop.desktopEditListener.onFinishDesktopEdit();
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
                    if (desktop.desktopEditListener != null)
                        desktop.desktopEditListener.onDesktopEdit();
                    return true;
                }
            });

            return layout;
        }
    }

    public static boolean handleOnDropOver(Home home, Item dropItem, Item item, View itemView, ViewGroup parent, int page, DesktopCallBack callBack) {
        if (item == null) {
            return false;
        }
        switch (item.type) {
            case APP:
            case SHORTCUT:
            case GROUP:
                if ((dropItem.type == Desktop.Item.Type.APP || dropItem.type == Desktop.Item.Type.SHORTCUT) && item.actions.length < GroupPopupView.GroupDef.maxItem) {
                    callBack.removeItemFromSettings(item);
                    parent.removeView(itemView);

                    item.addActions(dropItem.actions[0]);
                    if (item.name == null || item.name.isEmpty())
                        item.name = (home.getString(R.string.unnamed));
                    item.type = Desktop.Item.Type.GROUP;
                    callBack.addItemToSettings(item);
                    callBack.addItemToPagePosition(item, page);

                    home.desktop.consumeRevert();
                    home.desktopDock.consumeRevert();
                    return true;
                }
                break;
        }
        return false;
    }

    //Used for saving
    public static class SimpleItem {
        public Item.Type type;
        public String actions;
        public int x = 0;
        public int y = 0;
        public String name;
        public int widgetID;
        public int spanX = 1;
        public int spanY = 1;
        public boolean isInvalidate;

        public SimpleItem() {
        }

        public SimpleItem(Item in) {
            this.name = in.name;
            this.type = in.type;
            this.actions = in.getActionsAsString();
            this.x = in.x;
            this.y = in.y;
            this.spanX = in.spanX;
            this.spanY = in.spanY;
            this.widgetID = in.widgetID;
            this.isInvalidate = in.isInvalidate;
        }
    }

    public enum DesktopMode {
        Normal, ShowAllApps
    }

    public static class Item implements Parcelable {
        public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {

            @Override
            public Item createFromParcel(Parcel in) {
                return new Item(in);
            }

            @Override
            public Item[] newArray(int size) {
                return new Item[size];
            }
        };

        //Call to set the object to unusable
        public void invalidate() {
            isInvalidate = true;
            actions = null;
            type = null;
            name = null;
        }

        public Type type;
        public String name;
        public int x = 0, y = 0;

        // these can be replaced once database is implemented
        public Intent[] actions;
        public boolean isInvalidate = false;

        // widget specific values
        public int widgetID;
        public int spanX = 1, spanY = 1;

        // new values to be tracked

        // every item is assigned an ID number
        // initialize this to the time at creation
        public int idValue;

        // intent for shortcuts and apps
        // this replaces actions array
        public Intent appIntent;

        // list of items for groups
        public Set<String> items;

        // int value for launcher action
        public int actionValue;

        @Override
        public boolean equals(Object obj) {
            Item obj1 = (Item) obj;
            return obj != null
                    && obj1.type == this.type
                    && Arrays.equals(obj1.actions, this.actions)
                    && obj1.x == this.x
                    && obj1.y == this.y
                    ;
        }

        public void addActions(Intent act) {
            if (isInvalidate) return;
            Intent[] newAct = new Intent[actions.length + 1];
            for (int i = 0; i < actions.length; i++) {
                newAct[i] = actions[i];
            }
            newAct[actions.length] = act;
            actions = newAct;
        }

        public void removeActions(Intent act) {
            if (isInvalidate) return;
            Intent[] newAct = new Intent[actions.length - 1];
            boolean removed = false;
            for (int i = 0; i < actions.length; i++) {
                if (!act.equals(actions[i]))
                    newAct[removed ? i - 1 : i] = actions[i];
                else
                    removed = true;
            }
            actions = newAct;
        }

        public Item() {
            idValue = (int) new Date().getTime();
        }

        public Item(SimpleItem in) {
            this.type = in.type;
            this.name = in.name;
            this.actions = getActionsFromString(in.actions);
            this.x = in.x;
            this.y = in.y;
            this.spanX = in.spanX;
            this.spanY = in.spanY;
            this.widgetID = in.widgetID;
            this.isInvalidate = in.isInvalidate;
        }

        public static Item newAppItem(AppManager.App app) {
            Desktop.Item item = new Item();
            item.type = Type.APP;
            item.actions = new Intent[]{toIntent(app)};
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

            String iconID = Tool.saveIconAndReturnID(context, icon);
            intent.putExtra("shortCutIconID", iconID);
            intent.putExtra("shortCutName", name);

            item.actions = new Intent[]{intent};
            return item;
        }

        public static Item newAppDrawerBtn() {
            Desktop.Item item = new Item();
            item.type = Type.ACTION;
            item.spanX = 1;
            item.spanY = 1;

            return item;
        }

        public static Item newShortcutItem(Intent intent) {
            Desktop.Item item = new Item();
            item.type = Type.SHORTCUT;
            item.spanX = 1;
            item.spanY = 1;

            item.actions = new Intent[]{intent};
            return item;
        }

        public static Item fromGroupItem(Item gItem) {
            Desktop.Item item = new Item();
            item.type = Type.GROUP;
            item.spanX = 1;
            item.spanY = 1;
            item.actions = gItem.actions.clone();
            return item;
        }


        public Item(Parcel in) {
            type = Type.valueOf(in.readString());
            switch (type) {
                case SHORTCUT:
                case GROUP:
                case APP:
                    actions = in.createTypedArray(Intent.CREATOR);
                    break;
                case WIDGET:
                    widgetID = in.readInt();
                    spanX = in.readInt();
                    spanY = in.readInt();
                    break;
            }
            name = in.readString();
            isInvalidate = in.readByte() != 0;
            idValue = (int) new Date().getTime();
        }

        private static Intent toIntent(AppManager.App app) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName(app.packageName, app.className);
            return intent;
        }

        public String getActionsAsString() {
            if (actions == null || actions.length == 0) {
                return "";
            } else {
                StringBuilder str = new StringBuilder();
                for (int i = 0; i < actions.length; i++) {
                    str.append(actions[i].toUri(0));
                    if (i != actions.length - 1)
                        str.append("[MyActs]");
                }
                return str.toString();
            }
        }

        public static Intent[] getActionsFromString(String str) {
            if (str == null || str.isEmpty()) {
                return new Intent[0];
            } else {
                if (!str.contains("[MyActs]")) {
                    try {
                        return new Intent[]{Intent.parseUri(str, 0)};
                    } catch (URISyntaxException e) {
                        return new Intent[0];
                    }
                }
                String[] raw = Tool.split(str, "[MyActs]");
                Intent[] acts = new Intent[raw.length];
                for (int i = 0; i < acts.length; i++) {
                    try {
                        acts[i] = Intent.parseUri(raw[i], 0);
                    } catch (URISyntaxException e) {
                        return new Intent[0];
                    }
                }
                return acts;
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(type.toString());
            switch (type) {
                case SHORTCUT:
                case GROUP:
                case APP:
                    out.writeTypedArray(actions, 0);
                    break;
                case WIDGET:
                    out.writeInt(widgetID);
                    out.writeInt(spanX);
                    out.writeInt(spanY);
                    break;
            }
            out.writeString(name);
            out.writeByte((byte) (isInvalidate ? 1 : 0));
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
