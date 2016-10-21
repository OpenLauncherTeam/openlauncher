package com.benny.openlauncher.widget;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.R;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.GoodDragShadowBuilder;
import com.benny.openlauncher.util.GroupIconDrawable;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.Tool;

import java.util.UUID;

public class Dock extends CellContainer implements View.OnDragListener {

    public View previousItemView;
    public Desktop.Item previousItem;

    public Dock (Context c){
        super(c);
        init();
    }

    public Dock (Context c,AttributeSet attr){
        super(c,attr);
        init();
    }

    @Override
    public void init() {
        if (isInEditMode())return;
        
        setGridSize(LauncherSettings.getInstance(getContext()).generalSettings.dockGridx,1);
        setOnDragListener(this);

        super.init();
    }

    public void initDockItem(){
        removeAllViews();
        for (Desktop.Item item : LauncherSettings.getInstance(getContext()).dockData) {
            addAppToPosition(item);
        }
    }

    @Override
    public boolean onDrag(View p1, DragEvent p2){
        switch(p2.getAction()){
            case DragEvent.ACTION_DRAG_STARTED:
                switch(((DragAction)p2.getLocalState()).action){
                    case ACTION_APP:
                    case ACTION_GROUP:
                    case ACTION_APP_DRAWER:
                        return true;
                }
                return false;
            case DragEvent.ACTION_DRAG_ENTERED:
                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                return true;

            case DragEvent.ACTION_DROP:
                Intent intent = p2.getClipData().getItemAt(0).getIntent();
                intent.setExtrasClassLoader(Desktop.Item.class.getClassLoader());
                Desktop.Item item = intent.getParcelableExtra("mDragData");
                if(item.type == Desktop.Item.Type.APP  || item.type == Desktop.Item.Type.GROUP) {
                    if(addAppToDock(item, (int) p2.getX(), (int) p2.getY())){
                        Home.desktop.consumeRevert();
                        Home.dock.consumeRevert();
                    }else {
                        Home.dock.revertLastDraggedItem();
                        Home.desktop.revertLastDraggedItem();
                    }
                }
                return true;
            case DragEvent.ACTION_DRAG_ENDED:
                return true;
        }
        return false;
    }

    public void consumeRevert(){
        previousItem = null;
        previousItemView = null;
    }

    public void revertLastDraggedItem(){
        if (previousItemView != null) {
           addViewToGrid(previousItemView);

            LauncherSettings.getInstance(getContext()).dockData.add(previousItem);

            previousItem = null;
            previousItemView = null;
        }
    }

    public void addAppToPosition(final Desktop.Item item){
        View itemView = null;
        if (item.type == Desktop.Item.Type.APP)
            itemView = getAppItemView(item);
        else if (item.type == Desktop.Item.Type.GROUP)
            itemView = getGroupItemView(item);
        if (itemView == null){
            LauncherSettings.getInstance(getContext()).dockData.remove(item);
        }else
            addViewToGrid(itemView,item.x,item.y,item.spanX,item.spanY);
    }

    public boolean addAppToDock(final Desktop.Item item, int x, int y){
        CellContainer.LayoutParams positionToLayoutPrams = positionToLayoutPrams(x, y,item.spanX,item.spanY);
        if(positionToLayoutPrams != null){

            //Add the item to settings
            item.x = positionToLayoutPrams.x;
            item.y = positionToLayoutPrams.y;
            LauncherSettings.getInstance(getContext()).dockData.add(item);
            //end

            View itemView = null;
            if (item.type == Desktop.Item.Type.APP)
                itemView = getAppItemView(item);
            else if (item.type == Desktop.Item.Type.GROUP)
                itemView = getGroupItemView(item);

            if (itemView != null){
                itemView.setLayoutParams(positionToLayoutPrams);
                addView(itemView);
            }

            return true;
        } else{
            Toast.makeText(getContext(), R.string.toast_notenoughspace, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private View getAppItemView(final Desktop.Item item){
        final ViewGroup item_layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_app, null);
        TextView tv = (TextView) item_layout.findViewById(R.id.tv);
        ImageView iv = (ImageView) item_layout.findViewById(R.id.iv);

        iv.getLayoutParams().width = Tool.convertDpToPixel(LauncherSettings.getInstance(getContext()).generalSettings.iconSize, getContext());
        iv.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;//(int) Tool.convertDpToPixel(LauncherSettings.getInstance(getContext()).generalSettings.iconSize, getContext());

        final AppManager.App app = AppManager.getInstance(getContext()).findApp(item.actions[0].getComponent().getPackageName(), item.actions[0].getComponent().getClassName());
        if (app == null) {
            return null;
        }

        tv.setText(app.appName);
        tv.setTextColor(Color.WHITE);
        tv.setVisibility(View.GONE);
        iv.setImageDrawable(app.icon);
        item_layout.setOnDragListener(new OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        if (((DragAction)dragEvent.getLocalState()).viewID == view.getId())
                            return false;
                        switch (((DragAction)dragEvent.getLocalState()).action) {
                            case ACTION_APP:
                            case ACTION_APP_DRAWER:
                                return true;
                        }
                        return false;
                    case DragEvent.ACTION_DROP:
                        Intent intent = dragEvent.getClipData().getItemAt(0).getIntent();
                        intent.setExtrasClassLoader(Desktop.Item.class.getClassLoader());
                        Desktop.Item dropitem = intent.getParcelableExtra("mDragData");
                        if (dropitem.type == Desktop.Item.Type.APP || dropitem.actions.length < GroupPopupView.GroupDef.maxItem) {
                            LauncherSettings.getInstance(getContext()).dockData.remove(item);
                            removeView(view);

                            item.addActions(dropitem.actions[0]);
                            item.name = "Unnamed";
                            item.type = Desktop.Item.Type.GROUP;
                            LauncherSettings.getInstance(getContext()).dockData.add(item);
                            addAppToPosition(item);

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
        item_layout.setOnTouchListener(Tool.getItemOnTouchListener());
        item_layout.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                Intent i = new Intent();
                i.putExtra("mDragData", item);
                ClipData data = ClipData.newIntent("mDragIntent", i);
                view.startDrag(data, new GoodDragShadowBuilder(view),new DragAction(DragAction.Action.ACTION_APP,item_layout.getId()), 0);

                //Remove the item from settings
                LauncherSettings.getInstance(getContext()).dockData.remove(item);
                //end

                previousItemView = view;
                previousItem = item;
                removeView(view);
                return true;
            }
        });
        item_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Tool.createScaleInScaleOutAnim(view, new Runnable() {
                    @Override
                    public void run() {
                        Tool.startApp(getContext(), app);
                    }
                });
            }
        });

        return item_layout;
    }

    private View getGroupItemView(final Desktop.Item item) {
        final ViewGroup item_layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_app, null);
        item_layout.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        TextView tv = (TextView) item_layout.findViewById(R.id.tv);
        final ImageView iv = (ImageView) item_layout.findViewById(R.id.iv);

        final int iconSize = Tool.convertDpToPixel(LauncherSettings.getInstance(getContext()).generalSettings.iconSize, getContext());
        iv.getLayoutParams().width = iconSize;
        iv.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

        AppManager.App[] apps = new AppManager.App[item.actions.length];

        for (int i = 0; i < item.actions.length; i++) {
            apps[i] = AppManager.getInstance(getContext()).findApp(item.actions[i].getComponent().getPackageName(), item.actions[i].getComponent().getClassName());
            if (apps[i] == null)
                return null;
        }

        final Bitmap[] icons = new Bitmap[4];
        for (int i = 0; i < 4; i++) {
            if (i < apps.length)
                icons[i] = Tool.drawableToBitmap(apps[i].icon);
            else
                icons[i] = Tool.drawableToBitmap(new ColorDrawable(Color.TRANSPARENT));
        }

        iv.setImageDrawable(new GroupIconDrawable(icons,iconSize,item_layout));

        tv.setText("");
        tv.setTextColor(Color.WHITE);
        tv.setVisibility(View.GONE);
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
                        intent.setExtrasClassLoader(Desktop.Item.class.getClassLoader());
                        Desktop.Item dropitem = intent.getParcelableExtra("mDragData");
                        if (dropitem.type == Desktop.Item.Type.APP && item.actions.length < GroupPopupView.GroupDef.maxItem) {
                            LauncherSettings.getInstance(getContext()).dockData.remove(item);
                            removeView(view);

                            item.addActions(dropitem.actions[0]);
                            item.type = Desktop.Item.Type.GROUP;
                            LauncherSettings.getInstance(getContext()).dockData.add(item);
                            addAppToPosition(item);

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

        item_layout.setOnTouchListener(Tool.getItemOnTouchListener());
        item_layout.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                Intent i = new Intent();
                i.putExtra("mDragData", item);
                ClipData data = ClipData.newIntent("mDragIntent", i);
                view.startDrag(data, new GoodDragShadowBuilder(view),new DragAction(DragAction.Action.ACTION_GROUP,0), 0);

                //Remove the item from settings
                LauncherSettings.getInstance(getContext()).dockData.remove(item);
                //end

                previousItemView = view;
                previousItem = item;
                removeView(view);
                return true;
            }
        });
        item_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //item_layout.animate().setDuration(150).scaleX(0.5f).scaleY(0.5f).setInterpolator(new AccelerateDecelerateInterpolator());

                if(Home.groupPopup.showWindowV(item,view,true)){
                    ((GroupIconDrawable)(iv).getDrawable()).popUp();
                }
            }
        });

        return item_layout;
    }

}
