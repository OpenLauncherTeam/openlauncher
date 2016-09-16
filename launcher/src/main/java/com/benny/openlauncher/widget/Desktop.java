package com.benny.openlauncher.widget;

import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.R;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.GoodDragShadowBuilder;
import com.benny.openlauncher.util.GroupIconDrawable;
import com.benny.openlauncher.util.LauncherAction;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.Tools;
import com.bennyv5.smoothviewpager.SmoothPagerAdapter;
import com.bennyv5.smoothviewpager.SmoothViewPager;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import in.championswimmer.sfg.lib.SimpleFingerGestures;

public class Desktop extends SmoothViewPager implements OnDragListener {
    public int pageCount;

    public List<CellContainer> pages = new ArrayList<>();

    public OnDesktopEditListener listener;

    public boolean inEditMode;

    public View previousItemView;
    public Item previousItem;
    public int previousPage = -1;

    public Desktop(Context c, AttributeSet attr) {
        super(c, attr);
        init(c);
    }

    public Desktop(Context c) {
        super(c);
        init(c);
    }

    private void init(Context c) {
        pageCount = LauncherSettings.getInstance(c).generalSettings.desktopPageCount;
        setAdapter(new Adapter());
        setOnDragListener(this);

        setCurrentItem(LauncherSettings.getInstance(c).generalSettings.desktopHomePage);
    }

    public void initDesktopItem() {
        for (int i = 0; i < LauncherSettings.getInstance(getContext()).desktopData.size(); i++) {
            if (pages.size() <= i)break;
            pages.get(i).removeAllViews();
            for (int j = 0; j < LauncherSettings.getInstance(getContext()).desktopData.get(i).size(); j++) {
                addItemToPagePosition(LauncherSettings.getInstance(getContext()).desktopData.get(i).get(j), i);
            }
        }
    }

    public void addPageRight() {
        LauncherSettings.getInstance(getContext()).desktopData.add(new ArrayList<Item>());
        LauncherSettings.getInstance(getContext()).generalSettings.desktopPageCount++;
        pageCount++;

        final int previousPage = getCurrentItem();
        //getAdapter().notifyDataSetChanged();
        setAdapter(new Adapter());
        initDesktopItem();

        setCurrentItem(previousPage);
        post(new Runnable() {
            @Override
            public void run() {
                setCurrentItem(previousPage + 1);
            }
        });

        for (CellContainer cellContainer : pages)
            cellContainer.setHideGrid(false);
    }

    public void addPageLeft() {
        LauncherSettings.getInstance(getContext()).desktopData.add(getCurrentItem(), new ArrayList<Item>());
        LauncherSettings.getInstance(getContext()).generalSettings.desktopPageCount++;
        pageCount++;

        final int previousPage = getCurrentItem();
        //getAdapter().notifyDataSetChanged();
        setAdapter(new Adapter());
        initDesktopItem();

        setCurrentItem(previousPage);
        post(new Runnable() {
            @Override
            public void run() {
                setCurrentItem(previousPage - 1);
            }
        });

        for (CellContainer cellContainer : pages)
            cellContainer.setHideGrid(false);
    }

    public void removeCurrentPage() {
        if (pageCount == 1) return;
        //pages.add(getCurrentItem(),new CellContainer(getContext()));
        LauncherSettings.getInstance(getContext()).desktopData.remove(getCurrentItem());
        LauncherSettings.getInstance(getContext()).generalSettings.desktopPageCount--;
        pageCount--;

        int previousPage = getCurrentItem();
        //getAdapter().notifyDataSetChanged();
        setAdapter(new Adapter());
        initDesktopItem();

        for (View v : pages) {
            v.setBackgroundResource(R.drawable.outlinebg);
            v.setScaleX(0.7f);
            v.setScaleY(0.7f);
        }
        setCurrentItem(previousPage);
    }

    @Override
    public boolean onDrag(View p1, DragEvent p2) {
        switch (p2.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                switch (((DragAction)p2.getLocalState()).action) {
                    case ACTION_APP:
                    case ACTION_GROUP:
                    case ACTION_APP_DRAWER:
                    case ACTION_WIDGET:
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
                if (item.type == Item.Type.WIDGET){
                    int px = intent.getIntExtra("mX",0);
                    int py = intent.getIntExtra("mY",0);
                    int pW = intent.getIntExtra("mW",0);
                    int pH = intent.getIntExtra("mH",0);
                    if (addItemToCurrentPage(item, (int) p2.getX(), (int) p2.getY() )) {
                        Home.desktop.consumeRevert();
                        Home.dock.consumeRevert();
                    }
                }
                if (item.type == Desktop.Item.Type.APP  || item.type == Item.Type.GROUP) {
                    if (addItemToCurrentPage(item, (int) p2.getX(), (int) p2.getY())) {
                        Home.desktop.consumeRevert();
                        Home.dock.consumeRevert();
                    }
                }
                return true;
            case DragEvent.ACTION_DRAG_ENDED:
                revertLastDraggedItem();
                return true;
        }
        return false;
    }

    public void consumeRevert() {
        previousItem = null;
        previousItemView = null;
        previousPage = -1;
    }

    public void revertLastDraggedItem() {
        if (previousItemView != null && getAdapter().getCount() >= previousPage && previousPage > -1) {
            pages.get(getCurrentItem()).addViewToGrid(previousItemView);

            if (LauncherSettings.getInstance(getContext()).desktopData.size() < getCurrentItem() + 1)
                LauncherSettings.getInstance(getContext()).desktopData.add(previousPage, new ArrayList<Item>());
            LauncherSettings.getInstance(getContext()).desktopData.get(previousPage).add(previousItem);

            previousItem = null;
            previousItemView = null;
            previousPage = -1;
        }
    }

    public void addItemToPagePosition(final Item item, int page) {
        View itemView = null;
        if (item.type == Desktop.Item.Type.APP)
            itemView = getAppItemView(item);
        else if (item.type == Desktop.Item.Type.GROUP)
            itemView = getGroupItemView(item);
        else if (item.type == Item.Type.WIDGET)
            itemView = getWidgetView(item);
        if (itemView == null) {
            LauncherSettings.getInstance(getContext()).desktopData.get(page).remove(item);
        } else
            pages.get(page).addViewToGrid(itemView, item.x, item.y, item.spanX, item.spanY);
    }

    public boolean addItemToCurrentPage(final Item item, int x, int y) {
        CellContainer.LayoutParams positionToLayoutPrams = pages.get(getCurrentItem()).positionToLayoutPrams(x, y, item.spanX, item.spanY);
        if (positionToLayoutPrams != null) {
            //Add the item to settings
            item.x = positionToLayoutPrams.x;
            item.y = positionToLayoutPrams.y;
            if (LauncherSettings.getInstance(getContext()).desktopData.size() < getCurrentItem() + 1)
                LauncherSettings.getInstance(getContext()).desktopData.add(getCurrentItem(), new ArrayList<Item>());
            LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).add(item);
            //end

            View itemView = null;
            if (item.type == Desktop.Item.Type.APP)
                itemView = getAppItemView(item);
            else if (item.type == Desktop.Item.Type.GROUP)
                itemView = getGroupItemView(item);
            else if (item.type == Item.Type.WIDGET)
                itemView = getWidgetView(item);
            if (itemView != null) {
                itemView.setLayoutParams(positionToLayoutPrams);
                pages.get(getCurrentItem()).addView(itemView);
            }
            return true;
        } else {
            Toast.makeText(getContext(), R.string.toast_notenoughspace, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        WallpaperManager.getInstance(getContext()).setWallpaperOffsets(getWindowToken(), (float) (position + offset) / (pageCount - 1), 0);
        super.onPageScrolled(position, offset, offsetPixels);
    }

    private View getAppItemView(final Item item) {
        final ViewGroup item_layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_app, null);
        TextView tv = (TextView) item_layout.findViewById(R.id.tv);
        ImageView iv = (ImageView) item_layout.findViewById(R.id.iv);
        iv.getLayoutParams().width = Tools.convertDpToPixel(LauncherSettings.getInstance(getContext()).generalSettings.iconSize, getContext());
        iv.getLayoutParams().height = Tools.convertDpToPixel(LauncherSettings.getInstance(getContext()).generalSettings.iconSize, getContext());

        final AppManager.App app = AppManager.getInstance(getContext()).findApp(item.actions[0].getComponent().getPackageName(), item.actions[0].getComponent().getClassName());
        if (app == null) {
            return null;
        }

        tv.setText(app.appName);
        tv.setTextColor(Color.WHITE);
        iv.setImageDrawable(app.icon);
        item_layout.setOnDragListener(new OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        if (((DragAction)dragEvent.getLocalState()).viewID == view.getId()) {
                            Tools.print(true);
                            return false;
                        }
                        switch (((DragAction)dragEvent.getLocalState()).action) {
                            case ACTION_APP:
                            case ACTION_APP_DRAWER:
                                return true;
                        }
                        return false;
                    case DragEvent.ACTION_DROP:
                        Intent intent = dragEvent.getClipData().getItemAt(0).getIntent();
                        intent.setExtrasClassLoader(Item.class.getClassLoader());
                        Item dropitem = intent.getParcelableExtra("mDragData");
                        if (dropitem.type == Desktop.Item.Type.APP || dropitem.actions.length < GroupPopupView.GroupDef.maxItem) {
                            LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).remove(item);
                            pages.get(getCurrentItem()).removeView(view);

                            item.addActions(dropitem.actions[0]);
                            item.name = "Unnamed";
                            item.type = Item.Type.GROUP;
                            LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).add(item);
                            addItemToPagePosition(item,getCurrentItem());

                            Home.desktop.consumeRevert();
                            Home.dock.consumeRevert();
                        }
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        return true;
                }
                return false;
            }
        });
        item_layout.setId(UUID.randomUUID().hashCode());
        item_layout.setOnTouchListener(Tools.getItemOnTouchListener());
        item_layout.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                Intent i = new Intent();
                i.putExtra("mDragData", item);
                ClipData data = ClipData.newIntent("mDragIntent", i);
                view.startDrag(data, new GoodDragShadowBuilder(view),new DragAction(DragAction.Action.ACTION_APP,item_layout.getId()), 0);

                //Remove the item from settings
                LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).remove(item);
                //end

                previousPage = getCurrentItem();
                previousItemView = view;
                previousItem = item;
                pages.get(getCurrentItem()).removeView(view);
                return true;
            }
        });
        item_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Tools.createScaleInScaleOutAnim(view, new Runnable() {
                    @Override
                    public void run() {
                        Tools.startApp(getContext(), app);
                    }
                });
            }
        });

        return item_layout;
    }

    private View getGroupItemView(final Item item) {
        final ViewGroup item_layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_app, null);
        TextView tv = (TextView) item_layout.findViewById(R.id.tv);
        final ImageView iv = (ImageView) item_layout.findViewById(R.id.iv);

        final int iconSize = Tools.convertDpToPixel(LauncherSettings.getInstance(getContext()).generalSettings.iconSize, getContext());
        iv.getLayoutParams().width = iconSize;
        iv.getLayoutParams().height = iconSize;

        AppManager.App[] apps = new AppManager.App[item.actions.length];

        for (int i = 0; i < item.actions.length; i++) {
            apps[i] = AppManager.getInstance(getContext()).findApp(item.actions[i].getComponent().getPackageName(), item.actions[i].getComponent().getClassName());
            if (apps[i] == null)
                return null;
        }

        final Bitmap[] icons = new Bitmap[4];
        for (int i = 0; i < 4; i++) {
            if (i < apps.length)
                icons[i] = Tools.drawableToBitmap(apps[i].icon);
            else
                icons[i] = Tools.drawableToBitmap(new ColorDrawable(Color.TRANSPARENT));
        }
        iv.setImageDrawable(new GroupIconDrawable(icons,iconSize));

        tv.setText(item.name);
        tv.setTextColor(Color.WHITE);
        item_layout.setOnDragListener(new OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        switch (((DragAction)dragEvent.getLocalState()).action) {
                            case ACTION_APP:
                            case ACTION_APP_DRAWER:
                                return true;
                        }
                        return false;
                    case DragEvent.ACTION_DROP:
                        Intent intent = dragEvent.getClipData().getItemAt(0).getIntent();
                        intent.setExtrasClassLoader(Item.class.getClassLoader());
                        Item dropitem = intent.getParcelableExtra("mDragData");
                        if (dropitem.type == Desktop.Item.Type.APP && item.actions.length < GroupPopupView.GroupDef.maxItem) {
                            LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).remove(item);
                            pages.get(getCurrentItem()).removeView(view);

                            item.addActions(dropitem.actions[0]);
                            item.type = Item.Type.GROUP;
                            LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).add(item);
                            addItemToPagePosition(item,getCurrentItem());

                            Home.desktop.consumeRevert();
                            Home.dock.consumeRevert();
                        }
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        return true;
                }
                return false;
            }
        });
        item_layout.setOnTouchListener(Tools.getItemOnTouchListener());
        item_layout.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                Intent i = new Intent();
                i.putExtra("mDragData", item);
                ClipData data = ClipData.newIntent("mDragIntent", i);
                view.startDrag(data, new GoodDragShadowBuilder(view),new DragAction(DragAction.Action.ACTION_GROUP,0), 0);

                //Remove the item from settings
                LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).remove(item);
                //end

                previousPage = getCurrentItem();
                previousItemView = view;
                previousItem = item;
                pages.get(getCurrentItem()).removeView(view);
                return true;
            }
        });
        item_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //iv.animate().setDuration(150).scaleX(0.5f).scaleY(0.5f).setInterpolator(new AccelerateDecelerateInterpolator());

                if (Home.groupPopup.showWindowV(item,view,false)){
                    ((GroupIconDrawable)(iv).getDrawable()).popUp();
                }
            }
        });

        return item_layout;
    }

    private void scaleWidget(View view, Item item) {
        item.spanX = Math.min(item.spanX, 4);
        item.spanX = Math.max(item.spanX, 1);
        item.spanY = Math.min(item.spanY, 4);
        item.spanY = Math.max(item.spanY, 1);

        CellContainer.LayoutParams cellPositionToLayoutPrams = pages.get(getCurrentItem()).cellPositionToLayoutPrams(item.x, item.y, item.spanX, item.spanY, (CellContainer.LayoutParams) view.getLayoutParams());
        if (cellPositionToLayoutPrams == null)
            Toast.makeText(getContext(), R.string.toast_notenoughspace, Toast.LENGTH_SHORT).show();
        else {
            LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).remove(item);
            item.x = cellPositionToLayoutPrams.x;
            item.y = cellPositionToLayoutPrams.y;
            LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).add(item);
            view.setLayoutParams(cellPositionToLayoutPrams);

            updateWidgetOption(item);
        }

    }

    private void updateWidgetOption(Item item) {
        Bundle newOps = new Bundle();
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, item.spanX * pages.get(getCurrentItem()).cellWidth);
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, item.spanX * pages.get(getCurrentItem()).cellWidth);
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, item.spanX * pages.get(getCurrentItem()).cellWidth);
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, item.spanY * pages.get(getCurrentItem()).cellHeight);
        Home.appWidgetManager.updateAppWidgetOptions(item.widgetID, newOps);
    }

    private View getWidgetView(final Item item) {
        AppWidgetProviderInfo appWidgetInfo = Home.appWidgetManager.getAppWidgetInfo(item.widgetID);
        final WidgetView widgetView = (WidgetView) Home.appWidgetHost.createView(getContext(), item.widgetID, appWidgetInfo);
        widgetView.setAppWidget(item.widgetID, appWidgetInfo);

        widgetView.post(new Runnable() {
            @Override
            public void run() {
                updateWidgetOption(item);
            }
        });

        final FrameLayout widgetContainer = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.view_widgetcontainer, null);
        widgetContainer.addView(widgetView);

        final View ve = widgetContainer.findViewById(R.id.vertexpand);
        ve.bringToFront();
        final View he = widgetContainer.findViewById(R.id.horiexpand);
        he.bringToFront();
        final View vl = widgetContainer.findViewById(R.id.vertless);
        vl.bringToFront();
        final View hl = widgetContainer.findViewById(R.id.horiless);
        hl.bringToFront();

        ve.animate().scaleY(1).scaleX(1);
        he.animate().scaleY(1).scaleX(1);
        vl.animate().scaleY(1).scaleX(1);
        hl.animate().scaleY(1).scaleX(1);

        final Runnable action = new Runnable() {
            @Override
            public void run() {
                ve.animate().scaleY(0).scaleX(0);
                he.animate().scaleY(0).scaleX(0);
                vl.animate().scaleY(0).scaleX(0);
                hl.animate().scaleY(0).scaleX(0);
            }
        };
        widgetContainer.postDelayed(action, 2000);

        //widgetView.setOnTouchListener(Tools.getItemOnTouchListener());
        widgetView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                Intent i = new Intent();
                i.putExtra("mDragData", item);
                i.putExtra("mX",Home.touchY);
                i.putExtra("mY",Home.touchY);
                i.putExtra("mW",view.getWidth()/2);
                i.putExtra("mH",view.getHeight()/2);
                ClipData data = ClipData.newIntent("mDragIntent", i);
                view.startDrag(data, new GoodDragShadowBuilder(view), new DragAction(DragAction.Action.ACTION_WIDGET,0), 0);

                //Remove the item from settings
                LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).remove(item);
                //end

                previousPage = getCurrentItem();
                previousItemView = (View) view.getParent();
                previousItem = item;
                pages.get(getCurrentItem()).removeView((View) view.getParent());
                return true;
            }
        });

        ve.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getScaleX() < 1) return;
                item.spanY++;
                scaleWidget(widgetContainer, item);
                widgetContainer.removeCallbacks(action);
                widgetContainer.postDelayed(action, 2000);
            }
        });
        he.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getScaleX() < 1) return;
                item.spanX++;
                scaleWidget(widgetContainer, item);
                widgetContainer.removeCallbacks(action);
                widgetContainer.postDelayed(action, 2000);
            }
        });
        vl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getScaleX() < 1) return;
                item.spanY--;
                scaleWidget(widgetContainer, item);
                widgetContainer.removeCallbacks(action);
                widgetContainer.postDelayed(action, 2000);
            }
        });
        hl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getScaleX() < 1) return;
                item.spanX--;
                scaleWidget(widgetContainer, item);
                widgetContainer.removeCallbacks(action);
                widgetContainer.postDelayed(action, 2000);
            }
        });

        return widgetContainer;
    }

    public class Adapter extends SmoothPagerAdapter {

        float sacleFactor = 1f;

        private MotionEvent currentEvent;

        public Adapter() {
            pages = new ArrayList<>();
            for (int i = 0; i < getCount(); i++) {
                CellContainer layout = new CellContainer(getContext());
                layout.setSoundEffectsEnabled(false);
                SimpleFingerGestures mySfg = new SimpleFingerGestures();
                mySfg.setOnFingerGestureListener(new SimpleFingerGestures.OnFingerGestureListener() {
                    @Override
                    public boolean onSwipeUp(int i, long l, double v) {
                        return false;
                    }

                    @Override
                    public boolean onSwipeDown(int i, long l, double v) {
                        return false;
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
                        return false;
                    }

                    @Override
                    public boolean onUnpinch(int i, long l, double v) {
                        return false;
                    }

                    @Override
                    public boolean onDoubleTap(int i) {
                        LauncherAction.RunAction(LauncherAction.Action.LockScreen,getContext(),null);
                        return true;
                    }
                });
                layout.gestures = mySfg;
                layout.setGridSize(LauncherSettings.getInstance(getContext()).generalSettings.desktopGridx, LauncherSettings.getInstance(getContext()).generalSettings.desktopGridy);
                layout.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        currentEvent = motionEvent;
                        return false;
                    }
                });
                layout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sacleFactor = 1f;
                        for (final CellContainer v : pages) {
                            v.blockTouch = false;
                            v.animate().scaleX(sacleFactor).scaleY(sacleFactor).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    v.setBackground(null);
                                }
                            });
                        }
                        if (!inEditMode)
                            if (currentEvent != null)
                                WallpaperManager.getInstance(view.getContext()).sendWallpaperCommand(view.getWindowToken(),WallpaperManager.COMMAND_TAP,(int)currentEvent.getX(),(int)currentEvent.getY(),0,null);

                        inEditMode = false;
                        if (listener != null)
                            listener.onFinished();
                    }
                });
                layout.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        sacleFactor = 0.7f;
                        for (CellContainer v : pages) {
                            v.blockTouch = true;
                            v.setBackgroundResource(R.drawable.outlinebg);
                            v.animate().scaleX(sacleFactor).scaleY(sacleFactor).setInterpolator(new AccelerateDecelerateInterpolator());
                        }
                        inEditMode = true;
                        if (listener != null)
                            listener.onStart();
                        return true;
                    }
                });
                pages.add(layout);
            }
        }

        @Override
        public int getCount() {
            return pageCount;
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
            ViewGroup layout = pages.get(pos);

            container.addView(layout);

            return layout;
        }
    }

    //Used for saving
    public static class SimpleItem{
        public Item.Type type;
        public String actions;
        public int x = 0;
        public int y = 0;
        public String name;
        public int widgetID;
        public int spanX = 1;
        public int spanY = 1;

        public SimpleItem(){}
        public SimpleItem(Item in){
            this.name = in.name;
            this.type = in.type;
            this.actions = in.getActionsAsString();
            this.x = in.x;
            this.y = in.y;
            this.spanX = in.spanX;
            this.spanY = in.spanY;
            this.widgetID = in.widgetID;
        }
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

        public Type type;

        public Intent[] actions;

        public int x = 0, y = 0;

        public int widgetID;

        public String name;

        public int spanX = 1, spanY = 1;

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Item
                    && ((Item) obj).type == this.type
                    && Arrays.equals(((Item) obj).actions, this.actions)
                    && ((Item) obj).x == this.x
                    && ((Item) obj).y == this.y
                    ;
        }

        public void addActions(Intent act){
            Intent[] newAct = new Intent[actions.length+1];
            for (int i = 0; i < actions.length; i++) {
                newAct[i] = actions[i];
            }
            newAct[actions.length] = act;
            actions = newAct;
        }

        public void removeActions(Intent act){
            Intent[] newAct = new Intent[actions.length-1];
            boolean removed = false;
            for (int i = 0; i < actions.length; i++) {
                if(!act.equals(actions[i]))
                    newAct[removed ?i-1:i] = actions[i];
                else
                    removed = true;
            }
            actions = newAct;
        }

        public Item() {}

        public Item(SimpleItem in) {
            this.type = in.type;
            this.name = in.name;
            this.actions = getActionsFromString(in.actions);
            this.x = in.x;
            this.y = in.y;
            this.spanX = in.spanX;
            this.spanY = in.spanY;
            this.widgetID = in.widgetID;
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

        public Item(Parcel in) {
            type = Type.valueOf(in.readString());
            switch (type) {
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
        }

        private static Intent toIntent(AppManager.App app) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName(app.packageName, app.className);
            return intent;
        }

        public String getActionsAsString() {
            if (actions == null||actions.length == 0) {
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
                if (!str.contains("[MyActs]")){
                    try {
                        return new Intent[]{Intent.parseUri(str, 0)};
                    } catch (URISyntaxException e) {
                        return new Intent[0];
                    }
                }
                String[] raw = Tools.split(str,"[MyActs]");
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
        }

        public enum Type {
            APP,
            WIDGET,
            SHORTCUT,
            GROUP
        }
    }

    public interface OnDesktopEditListener {
        void onStart();

        void onFinished();
    }

}
