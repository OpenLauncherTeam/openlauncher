package com.bennyv4.project2.widget;

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

    public OnDestopEditListener listener;

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

        AppManager.getInstance(getContext()).addAppUpdatedListener(new AppManager.AppUpdatedListener() {
            boolean fired = false;
            @Override
            public void onAppUpdated(List<AppManager.App> apps) {
                if (fired)return;
                fired = true;
                initDesktopItem();
            }
        });
    }

    private void initDesktopItem(){
        for (int i = 0 ; i < LauncherSettings.getInstance(getContext()).desktopData.size() ; i++){
            for (int j = 0 ; j < LauncherSettings.getInstance(getContext()).desktopData.get(i).size() ; j++){
                addAppToPagePosition(LauncherSettings.getInstance(getContext()).desktopData.get(i).get(j),i);
            }
        }
    }

    public void addPageRight(){
        //pages.add(new CellContainer(getContext()));
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
        //pages.add(getCurrentItem(),new CellContainer(getContext()));
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

            pages.get(0).performLongClick();
        setCurrentItem(previousPage);
    }

    @Override
    public boolean onDrag(View p1, DragEvent p2) {
        switch (p2.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                switch ((DragAction) p2.getLocalState()) {
                    case ACTION_APP:
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
                if (item.type == Desktop.Item.Type.APP) {
                    Home.desktop.consumeRevert();
                    Home.dock.consumeRevert();
                    addAppToCurrentPage(item, (int) p2.getX(), (int) p2.getY());
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

    public void addAppToPagePosition(final Item item, int page) {
        View itemView = getAppItemView(item);
        if (itemView == null){
            LauncherSettings.getInstance(getContext()).desktopData.get(page).remove(item);
        }else
            pages.get(page).addViewToGrid(itemView, item.x, item.y);
    }

    public void addAppToCurrentPage(final Item item, int x, int y) {
        CellContainer.LayoutParams positionToLayoutPrams = pages.get(getCurrentItem()).positionToLayoutPrams(x, y);
        if (positionToLayoutPrams != null) {

            //Add the item to settings
            item.x = positionToLayoutPrams.x;
            item.y = positionToLayoutPrams.y;
            if (LauncherSettings.getInstance(getContext()).desktopData.size() < getCurrentItem() + 1)
                LauncherSettings.getInstance(getContext()).desktopData.add(getCurrentItem(), new ArrayList<Item>());
            LauncherSettings.getInstance(getContext()).desktopData.get(getCurrentItem()).add(item);
            //end

            View itemView = getAppItemView(item);
            if (itemView != null) {
                itemView.setLayoutParams(positionToLayoutPrams);
                pages.get(getCurrentItem()).addView(itemView);
            }
        } else {
            Toast.makeText(getContext(), "Occupied", Toast.LENGTH_SHORT).show();
        }
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
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                Intent i = new Intent();
                i.putExtra("mDragData", Desktop.Item.newAppItem(app));
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
                        if (listener != null)
                            listener.onStart();
                        return true;
                    }
                });
                int pad = (int) Tools.convertDpToPixel(8, getContext());
                layout.setPadding(0, pad, 0, pad);
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

        public int x, y;

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

        public Item(Parcel in) {
            type = Type.valueOf(in.readString());
            switch (type) {
                case GROUP:
                case APP:
                    actions = in.createTypedArray(Intent.CREATOR);
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
            }
        }

        public enum Type {
            APP,
            WIDGET,
            SHORTCUT,
            GROUP;
        }
    }

    public interface OnDestopEditListener{
        void onStart();
        void onFinished();
    }

}
