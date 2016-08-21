package com.bennyv4.project2.widget;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bennyv4.project2.Home;
import com.bennyv4.project2.R;
import com.bennyv4.project2.util.AppManager;
import com.bennyv4.project2.util.DragAction;
import com.bennyv4.project2.util.LauncherSettings;
import com.bennyv4.project2.util.Tools;

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
        cellSpanVert = 1;
        cellSpanHori = 5;
        setOnDragListener(this);

        super.init();
    }

    public void initDockItem(){
        for (Desktop.Item item : LauncherSettings.getInstance(getContext()).dockData) {
            Home.dock.addAppToPosition(item);
        }
    }

    @Override
    public boolean onDrag(View p1, DragEvent p2){
        switch(p2.getAction()){
            case DragEvent.ACTION_DRAG_STARTED:
                switch((DragAction)p2.getLocalState()){
                    case ACTION_APP:
                        setHideGrid(false);
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
                if(item.type == Desktop.Item.Type.APP) {
                    if(addAppToDock(item, (int) p2.getX(), (int) p2.getY())){
                        Home.desktop.consumeRevert();
                        Home.dock.consumeRevert();
                    }
                }
                return true;
            case DragEvent.ACTION_DRAG_ENDED:
                revertLastDraggedItem();
                setHideGrid(true);
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
        View itemView = getAppItemView(item);
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

            View itemView = getAppItemView(item);
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
        ViewGroup item_layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_app, null);
        TextView tv = (TextView) item_layout.findViewById(R.id.tv);
        ImageView iv = (ImageView) item_layout.findViewById(R.id.iv);

        iv.getLayoutParams().width = (int) Tools.convertDpToPixel(LauncherSettings.getInstance(getContext()).generalSettings.iconSize, getContext());
        iv.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;//(int) Tools.convertDpToPixel(LauncherSettings.getInstance(getContext()).generalSettings.iconSize, getContext());

        final AppManager.App app = AppManager.getInstance(getContext()).findApp(item.actions[0].getComponent().getPackageName(), item.actions[0].getComponent().getClassName());
        if (app == null) {
            return null;
        }

        tv.setText(app.appName);
        tv.setTextColor(Color.WHITE);
        tv.setVisibility(View.GONE);
        iv.setImageDrawable(app.icon);
        item_layout.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (Home.desktop.inEditMode)return false;
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                Intent i = new Intent();
                i.putExtra("mDragData", item);
                ClipData data = ClipData.newIntent("mDragIntent", i);
                view.startDrag(data, new DragShadowBuilder(view), DragAction.ACTION_APP, 0);

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

}
