package com.bennyv4.project2.widget;

import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.*;
import android.graphics.Color;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.View.*;

import com.bennyv4.project2.*;
import com.bennyv4.project2.util.*;

import java.util.*;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.*;

import com.bennyv5.smoothviewpager.SmoothPagerAdapter;
import com.bennyv5.smoothviewpager.SmoothViewPager;

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

    public void initDesktopItem(){
        for (int i = 0 ; i < LauncherSettings.getInstance(getContext()).desktopData.size() ; i++){
            for (int j = 0 ; j < LauncherSettings.getInstance(getContext()).desktopData.get(i).size() ; j++){
                addItemToPagePosition(LauncherSettings.getInstance(getContext()).desktopData.get(i).get(j),i);
            }
        }
    }

    public void addPageRight(){
        LauncherSettings.getInstance(getContext()).desktopData.add(new ArrayList<Item>());
        LauncherSettings.getInstance(getContext()).generalSettings.desktopPageCount ++;
        pageCount ++;

        int previousPage = getCurrentItem();
        //getAdapter().notifyDataSetChanged();
        setAdapter(new Adapter());
        initDesktopItem();

        setCurrentItem(previousPage+1);

        for (CellContainer cellContainer : pages)
            cellContainer.setHideGrid(false);
    }

    public void addPageLeft(){
        LauncherSettings.getInstance(getContext()).desktopData.add(getCurrentItem(),new ArrayList<Item>());
        LauncherSettings.getInstance(getContext()).generalSettings.desktopPageCount ++;
        pageCount ++;

        int previousPage = getCurrentItem();
        //getAdapter().notifyDataSetChanged();
        setAdapter(new Adapter());
        initDesktopItem();

        setCurrentItem(previousPage-1);

        for (CellContainer cellContainer : pages)
            cellContainer.setHideGrid(false);
    }

    public void removeCurrentPage(){
        if (pageCount == 1)return;
        //pages.add(getCurrentItem(),new CellContainer(getContext()));
        LauncherSettings.getInstance(getContext()).desktopData.remove(getCurrentItem());
        LauncherSettings.getInstance(getContext()).generalSettings.desktopPageCount --;
        pageCount --;

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
                switch ((DragAction) p2.getLocalState()) {
                    case ACTION_APP:
                    case ACTION_WIDGET:
                        for (CellContainer cellContainer : pages)
                            cellContainer.setHideGrid(false);
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
                if (item.type == Desktop.Item.Type.APP||item.type == Item.Type.WIDGET) {
                    if (addItemToCurrentPage(item, (int) p2.getX(), (int) p2.getY())){
                        Home.desktop.consumeRevert();
                        Home.dock.consumeRevert();
                    }
                }
                return true;
            case DragEvent.ACTION_DRAG_ENDED:
                revertLastDraggedItem();
                for (CellContainer cellContainer : pages)
                    cellContainer.setHideGrid(true);
                return true;
        }
        return false;
    }

    public void consumeRevert(){
        previousItem = null;
        previousItemView = null;
        previousPage = -1;
    }

    public void revertLastDraggedItem(){
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
        View itemView = item.type == Item.Type.WIDGET ? getWidgetView(item): getAppItemView(item);
        if (itemView == null){
            LauncherSettings.getInstance(getContext()).desktopData.get(page).remove(item);
        }else
            pages.get(page).addViewToGrid(itemView, item.x, item.y,item.spanX,item.spanY);
    }

    public boolean addItemToCurrentPage(final Item item, int x, int y) {
        CellContainer.LayoutParams positionToLayoutPrams = pages.get(getCurrentItem()).positionToLayoutPrams(x, y,item.spanX,item.spanY);
        if (positionToLayoutPrams != null) {
            //Add the item to settings
            item.x = positionToLayoutPrams.x;
            item.y = positionToLayoutPrams.y;
            if (LauncherSettings.getInstance(getContext()).desktopData.size() < getCurrentItem() + 1)
                LauncherSettings.getInstance(getContext()).desktopData.add(getCurrentItem(), new ArrayList<Item>());
            LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).add(item);
            //end

            View itemView = item.type == Item.Type.WIDGET ? getWidgetView(item): getAppItemView(item);
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
        WallpaperManager.getInstance(getContext()).setWallpaperOffsets(getWindowToken(), (float) (position + offset) / (pageCount-1), 0);
        super.onPageScrolled(position, offset, offsetPixels);
    }

    private View getAppItemView(final Item item){
        ViewGroup item_layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_app, null);
        TextView tv = (TextView) item_layout.findViewById(R.id.tv);
        ImageView iv = (ImageView) item_layout.findViewById(R.id.iv);
        iv.getLayoutParams().width = (int) Tools.convertDpToPixel(LauncherSettings.getInstance(getContext()).generalSettings.iconSize, getContext());
        iv.getLayoutParams().height = (int) Tools.convertDpToPixel(LauncherSettings.getInstance(getContext()).generalSettings.iconSize, getContext());

        final AppManager.App app = AppManager.getInstance(getContext()).findApp(item.actions[0].getComponent().getPackageName(), item.actions[0].getComponent().getClassName());
        if (app == null) {
            return null;
        }

        tv.setText(app.appName);
        tv.setTextColor(Color.WHITE);
        iv.setImageDrawable(app.icon);
        item_layout.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (Home.desktop.inEditMode)return false;
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                Intent i = new Intent();
                i.putExtra("mDragData",item);
                ClipData data = ClipData.newIntent("mDragIntent", i);
                view.startDrag(data, new DragShadowBuilder(view), DragAction.ACTION_APP, 0);

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
                if (Home.desktop.inEditMode)return;
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

    private void scaleWidget(View view,Item item){
        item.spanX = Math.min(item.spanX,4);
        item.spanX = Math.max(item.spanX,1);
        item.spanY = Math.min(item.spanY,4);
        item.spanY = Math.max(item.spanY,1);

        CellContainer.LayoutParams cellPositionToLayoutPrams = pages.get(getCurrentItem()).cellPositionToLayoutPrams(item.x,item.y,item.spanX,item.spanY,(CellContainer.LayoutParams)view.getLayoutParams());
        if (cellPositionToLayoutPrams == null)
            Toast.makeText(getContext(), R.string.toast_notenoughspace, Toast.LENGTH_SHORT).show();
        else{
            LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).remove(item);
            item.x = cellPositionToLayoutPrams.x;
            item.y = cellPositionToLayoutPrams.y;
            LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).add(item);
            view.setLayoutParams(cellPositionToLayoutPrams);

            updateWidgetOption(item);
        }

    }

    private void updateWidgetOption(Item item){
        Bundle newOps = new Bundle();
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,0);
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,item.spanX * pages.get(getCurrentItem()).cellWidth);
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,0);
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,item.spanY * pages.get(getCurrentItem()).cellHeight);
        Home.appWidgetManager.updateAppWidgetOptions(item.widgetID,newOps);
    }

    private View getWidgetView(final Item item){
        AppWidgetProviderInfo appWidgetInfo = Home.appWidgetManager.getAppWidgetInfo(item.widgetID);
        WidgetView widgetView = (WidgetView) Home.appWidgetHost.createView(getContext(),item.widgetID, appWidgetInfo);
        widgetView.setAppWidget(item.widgetID,appWidgetInfo);

        widgetView.post(new Runnable() {
            @Override
            public void run() {
               updateWidgetOption(item);
            }
        });

        final FrameLayout widgetContainer = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.view_widgetcontainer,null);
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
        widgetContainer.postDelayed(action,2000);

        widgetView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (Home.desktop.inEditMode){
                    pages.get(getCurrentItem()).performClick();
                    return false;
                }
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                Intent i = new Intent();
                i.putExtra("mDragData", item);
                ClipData data = ClipData.newIntent("mDragIntent", i);
                view.startDrag(data, new DragShadowBuilder(view), DragAction.ACTION_WIDGET, 0);

                //Remove the item from settings
                LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).remove(item);
                //end

                previousPage = getCurrentItem();
                previousItemView = (View)view.getParent();
                previousItem = item;
                pages.get(getCurrentItem()).removeView((View)view.getParent());

                return true;
            }
        });

        ve.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getScaleX() < 1)return;
                item.spanY++;
                scaleWidget(widgetContainer,item);
                widgetContainer.removeCallbacks(action);
                widgetContainer.postDelayed(action,2000);
            }
        });
        he.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getScaleX() < 1)return;
                item.spanX++;
                scaleWidget(widgetContainer,item);
                widgetContainer.removeCallbacks(action);
                widgetContainer.postDelayed(action,2000);
            }
        });
        vl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getScaleX() < 1)return;
                item.spanY--;
                scaleWidget(widgetContainer,item);
                widgetContainer.removeCallbacks(action);
                widgetContainer.postDelayed(action,2000);
            }
        });
        hl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getScaleX() < 1)return;
                item.spanX--;
                scaleWidget(widgetContainer,item);
                widgetContainer.removeCallbacks(action);
                widgetContainer.postDelayed(action,2000);
            }
        });

        return widgetContainer;
    }

    public class Adapter extends SmoothPagerAdapter {

        float sacleFactor = 1f;

        public Adapter() {
            pages = new ArrayList<>();
            for (int i = 0; i < getCount(); i++) {
                CellContainer layout = new CellContainer(getContext());
                layout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sacleFactor = 1f;
                        for (View v : pages) {
                            v.setBackground(null);
                            v.animate().scaleX(sacleFactor).scaleY(sacleFactor).setInterpolator(new AccelerateDecelerateInterpolator());
                        }
                        inEditMode = false;
                        if (listener != null)
                            listener.onFinished();
                    }
                });
                layout.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        sacleFactor = 0.7f;
                        for (View v : pages) {
                            v.setBackgroundResource(R.drawable.outlinebg);
                            v.animate().scaleX(sacleFactor).scaleY(sacleFactor).setInterpolator(new AccelerateDecelerateInterpolator());
                        }
                        inEditMode = true;
                        if (listener != null)
                            listener.onStart();
                        return true;
                    }
                });
                //int pad = (int) Tools.convertDpToPixel(5, getContext());
                //layout.setPadding(0,pad,0,pad);
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

        public int spanX = 1,spanY = 1;

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Item
                    && ((Item) obj).type == this.type
                    && Arrays.equals(((Item) obj).actions, this.actions)
                    && ((Item) obj).x == this.x
                    && ((Item) obj).y == this.y
                    ;
        }

        public Item() {
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
        }

        private static Intent toIntent(AppManager.App app) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName(app.packageName, app.className);
            return intent;
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
