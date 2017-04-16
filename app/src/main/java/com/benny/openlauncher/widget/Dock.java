package com.benny.openlauncher.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.Tool;
import com.benny.openlauncher.viewutil.DesktopCallBack;
import com.benny.openlauncher.viewutil.ItemViewFactory;

import static com.benny.openlauncher.widget.Desktop.Item;

public class Dock extends CellContainer implements View.OnDragListener, DesktopCallBack {

    public View previousItemView;
    public Item previousItem;

    private float startPosX, startPosY;

    private Home home;

    public Dock(Context c) {
        super(c);
    }

    public Dock(Context c, AttributeSet attr) {
        super(c, attr);
    }

    @Override
    public void init() {
        if (isInEditMode()) return;

        if (LauncherSettings.getInstance(getContext()).generalSettings != null)
            setGridSize(LauncherSettings.getInstance(getContext()).generalSettings.dockGridX, 1);
        setOnDragListener(this);

        super.init();
    }

    public void initDockItem(Home home) {
        this.home = home;
        removeAllViews();
        int column = LauncherSettings.getInstance(getContext()).generalSettings.dockGridX;
        for (Item item : LauncherSettings.getInstance(getContext()).dockData) {
            if (item.x < column)
                addItemToPagePosition(item, 0);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            startPosX = ev.getX();
            startPosY = ev.getY();

            Tool.print("ActionD");
        }
        detectSwipe(ev);
        super.dispatchTouchEvent(ev);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }

    private void detectSwipe(MotionEvent ev) {
        if (Home.launcher == null) return;
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            Tool.print("ActionUP");
            float minDist = 150f;
            Tool.print((int) ev.getX(), (int) ev.getY());
            if (startPosY - ev.getY() > minDist) {
                if (LauncherSettings.getInstance(getContext()).generalSettings.swipe) {
                    Point p = Tool.convertPoint(new Point((int) ev.getX(), (int) ev.getY()), this, Home.launcher.appDrawerController);
                    // FIXME: 1/22/2017 This seem weird, but the extra offset ( Tool.getNavBarHeight(getContext()) ) works on my phone
                    // FIXME: 1/22/2017 This part of the code is identical as the code in Desktop so will combine them later
                    Home.launcher.openAppDrawer(this, p.x, p.y - Tool.getNavBarHeight(getContext()) / 2);
                }
            }
        }
    }

    @Override
    public boolean onDrag(View p1, DragEvent p2) {
        switch (p2.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                switch (((DragAction) p2.getLocalState()).action) {
                    case ACTION_APP:
                    case ACTION_GROUP:
                    case ACTION_APP_DRAWER:
                    case ACTION_SHORTCUT:
                    case ACTION_LAUNCHER:
                        return true;
                }
                return false;
            case DragEvent.ACTION_DRAG_ENTERED:
                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                return true;

            case DragEvent.ACTION_DROP:
                Intent intent = p2.getClipData().getItemAt(0).getIntent();
                intent.setExtrasClassLoader(Item.class.getClassLoader());
                Item item = intent.getParcelableExtra("mDragData");
                if (item.type == Item.Type.APP || item.type == Item.Type.GROUP || item.type == Item.Type.SHORTCUT || item.type == Item.Type.ACTION) {
                    if (addItemToPosition(item, (int) p2.getX(), (int) p2.getY())) {
                        home.desktop.consumeRevert();
                        home.desktopDock.consumeRevert();
                    } else {
                        Point pos = touchPosToCoordinate((int) p2.getX(), (int) p2.getY(), item.spanX, item.spanY, false);
                        View itemView = coordinateToChildView(pos);

                        if (itemView != null)
                            if (Desktop.handleOnDropOver(home, item, (Item) itemView.getTag(), itemView, this, 0, this)) {
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
                return true;
        }
        return false;
    }

    /**
     * @param args array storing the item(pos 0) and view ref(pos 1).
     */
    @Override
    public void setLastItem(Object... args) {
        View v = (View) args[1];
        Item item = (Item) args[0];

        //Remove the item from settings
        removeItemFromSettings(item);
        //end

        previousItemView = v;
        previousItem = item;
        removeView(v);
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

            LauncherSettings.getInstance(getContext()).dockData.add(previousItem);

            previousItem = null;
            previousItemView = null;
        }
    }

    @Override
    public void addItemToPagePosition(final Item item, int page) {
        if (item.isInvalidate) return;

        int flag = LauncherSettings.getInstance(getContext()).generalSettings.dockShowLabel ? ItemViewFactory.NO_FLAGS : ItemViewFactory.NO_LABEL;
        View itemView = ItemViewFactory.getItemView(getContext(), this, item, flag);

        if (itemView == null) {
            item.invalidate();
            //LauncherSettings.getInstance(getContext()).dockData.remove(item);
        } else
            addViewToGrid(itemView, item.x, item.y, item.spanX, item.spanY);
    }

    @Override
    public boolean addItemToPosition(final Item item, int x, int y) {
        CellContainer.LayoutParams positionToLayoutPrams = positionToLayoutPrams(x, y, item.spanX, item.spanY);
        if (positionToLayoutPrams != null) {
            //Add the item to settings
            item.x = positionToLayoutPrams.x;
            item.y = positionToLayoutPrams.y;
            LauncherSettings.getInstance(getContext()).dockData.add(item);
            //end

            int flag = LauncherSettings.getInstance(getContext()).generalSettings.dockShowLabel ? ItemViewFactory.NO_FLAGS : ItemViewFactory.NO_LABEL;
            View itemView = ItemViewFactory.getItemView(getContext(), this, item, flag);

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
    public void removeItemFromSettings(Item item) {
        LauncherSettings.getInstance(getContext()).dockData.remove(item);
    }

    @Override
    public void addItemToSettings(Item item) {
        LauncherSettings.getInstance(getContext()).dockData.add(item);
    }
}
