package com.bennyv4.project2.widget;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bennyv4.project2.R;
import com.bennyv4.project2.util.AppManager;
import com.bennyv4.project2.util.DragAction;
import com.bennyv4.project2.util.LauncherSettings;
import com.bennyv4.project2.util.Tools;

public class Dock extends CellContainer implements View.OnDragListener {

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
                if(item.type == Desktop.Item.Type.APP)
                    addAppToDock(item, (int)p2.getX(), (int)p2.getY());
                return true;
            case DragEvent.ACTION_DRAG_ENDED:
                setHideGrid(true);
                return true;
        }
        return false;
    }

    public void addAppToPosition(final Desktop.Item item){
        ViewGroup item_layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_app, null);
        TextView tv = (TextView) item_layout.findViewById(R.id.tv);
        ImageView iv = (ImageView) item_layout.findViewById(R.id.iv);
        iv.getLayoutParams().width = (int) Tools.convertDpToPixel(AppDrawer.iconSize,getContext());

        final AppManager.App app = AppManager.getInstance(getContext()).findApp(item.packageName[0], item.className[0]);
        if (app == null){
            LauncherSettings.getInstance(getContext()).dockData.remove(item);
            return;
        }

        tv.setText(app.appName);
        tv.setTextColor(Color.WHITE);
        tv.setVisibility(View.GONE);
        iv.setImageDrawable(app.icon);
        item_layout.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent i = new Intent();
                i.putExtra("mDragData", Desktop.Item.newAppItem(app));
                ClipData data = ClipData.newIntent("mDragIntent", i);
                view.startDrag(data, new DragShadowBuilder(view), DragAction.ACTION_APP, 0);

                //Remove the item from settings
                LauncherSettings.getInstance(getContext()).dockData.remove(item);
                //end

                removeView(view);
                return true;
            }
        });
        item_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Tools.createScaleInScaleOutAnim(view, new Runnable() {
                    @Override
                    public void run() {
                        Tools.startApp(getContext(),app);
                    }
                });
            }
        });
        addViewToGrid(item_layout,item.x,item.y);
    }

    public void addAppToDock(final Desktop.Item item, int x, int y){
        CellContainer.LayoutParams positionToLayoutPrams = positionToLayoutPrams(x, y);
        if(positionToLayoutPrams != null){

            //Add the item to settings
            item.x = positionToLayoutPrams.x;
            item.y = positionToLayoutPrams.y;
            LauncherSettings.getInstance(getContext()).dockData.add(item);
            //end

            ViewGroup item_layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_app, null);
            TextView tv = (TextView) item_layout.findViewById(R.id.tv);
            ImageView iv = (ImageView) item_layout.findViewById(R.id.iv);
            iv.getLayoutParams().width = (int) Tools.convertDpToPixel(AppDrawer.iconSize,getContext());

            final AppManager.App app = AppManager.getInstance(getContext()).findApp(item.packageName[0], item.className[0]);
            if (app == null)
                return;

            tv.setText(app.appName);
            tv.setTextColor(Color.WHITE);
            tv.setVisibility(View.GONE);
            iv.setImageDrawable(app.icon);
            item_layout.setLayoutParams(positionToLayoutPrams);
            item_layout.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Intent i = new Intent();
                    i.putExtra("mDragData", Desktop.Item.newAppItem(app));
                    ClipData data = ClipData.newIntent("mDragIntent", i);
                    view.startDrag(data, new DragShadowBuilder(view), DragAction.ACTION_APP, 0);

                    //Remove the item from settings
                    LauncherSettings.getInstance(getContext()).dockData.remove(item);
                    //end

                    removeView(view);
                    return true;
                }
            });
            item_layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Tools.createScaleInScaleOutAnim(view, new Runnable() {
                        @Override
                        public void run() {
                            Tools.startApp(getContext(),app);
                        }
                    });
                }
            });
            addView(item_layout);
        }
        else{
            Toast.makeText(getContext(), "Occupied", Toast.LENGTH_SHORT).show();
        }
    }

}
