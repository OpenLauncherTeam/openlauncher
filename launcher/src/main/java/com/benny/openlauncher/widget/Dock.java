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
import com.benny.openlauncher.util.ItemViewFactory;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.Tool;

import java.util.UUID;

public class Dock extends CellContainer implements View.OnDragListener {

    public View previousItemView;
    public Desktop.Item previousItem;

    public Dock(Context c) {
        super(c);
        init();
    }

    public Dock(Context c, AttributeSet attr) {
        super(c, attr);
        init();
    }

    @Override
    public void init() {
        if (isInEditMode()) return;

        setGridSize(LauncherSettings.getInstance(getContext()).generalSettings.dockGridx, 1);
        setOnDragListener(this);

        super.init();
    }

    public void initDockItem() {
        removeAllViews();
        for (Desktop.Item item : LauncherSettings.getInstance(getContext()).dockData) {
            addAppToPosition(item);
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
                if (item.type == Desktop.Item.Type.APP || item.type == Desktop.Item.Type.GROUP || item.type == Desktop.Item.Type.SHORTCUT) {
                    if (addAppToDock(item, (int) p2.getX(), (int) p2.getY())) {
                        Home.desktop.consumeRevert();
                        Home.dock.consumeRevert();
                    } else {
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

    public void consumeRevert() {
        previousItem = null;
        previousItemView = null;
    }

    public void revertLastDraggedItem() {
        if (previousItemView != null) {
            addViewToGrid(previousItemView);

            LauncherSettings.getInstance(getContext()).dockData.add(previousItem);

            previousItem = null;
            previousItemView = null;
        }
    }

    public void addAppToPosition(final Desktop.Item item) {
        View itemView = null;
        if (item.type == Desktop.Item.Type.APP)
            itemView = ItemViewFactory.getAppItemView(this,item);
        else if (item.type == Desktop.Item.Type.GROUP)
            itemView = ItemViewFactory.getGroupItemView(this,item);
        else if (item.type == Desktop.Item.Type.SHORTCUT)
            itemView = ItemViewFactory.getShortcutView(this,item);
        if (itemView == null) {
            LauncherSettings.getInstance(getContext()).dockData.remove(item);
        } else
            addViewToGrid(itemView, item.x, item.y, item.spanX, item.spanY);
    }

    public boolean addAppToDock(final Desktop.Item item, int x, int y) {
        CellContainer.LayoutParams positionToLayoutPrams = positionToLayoutPrams(x, y, item.spanX, item.spanY);
        if (positionToLayoutPrams != null) {

            //Add the item to settings
            item.x = positionToLayoutPrams.x;
            item.y = positionToLayoutPrams.y;
            LauncherSettings.getInstance(getContext()).dockData.add(item);
            //end

            View itemView = null;
            if (item.type == Desktop.Item.Type.APP)
                itemView = ItemViewFactory.getAppItemView(this,item);
            else if (item.type == Desktop.Item.Type.GROUP)
                itemView = ItemViewFactory.getGroupItemView(this,item);
            else if (item.type == Desktop.Item.Type.SHORTCUT)
                itemView = ItemViewFactory.getShortcutView(this,item);
            if (itemView != null) {
                itemView.setLayoutParams(positionToLayoutPrams);
                addView(itemView);
            }

            return true;
        } else {
            Toast.makeText(getContext(), R.string.toast_notenoughspace, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

}
