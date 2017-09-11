package com.benny.openlauncher.core.util;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;

import com.benny.openlauncher.core.model.Item;
import com.benny.openlauncher.core.viewutil.GoodDragShadowBuilder;
import com.benny.openlauncher.core.widget.AppItemView;

public class DragDropHandler {

    private static final String DRAG_DROP_EXTRA = "DRAG_DROP_EXTRA";
    private static final String DRAG_DROP_INTENT = "DRAG_DROP_INTENT";

    public static Bitmap cachedDragBitmap;

    public static <T extends Parcelable> void startDrag(View v, T item, DragAction.Action action, @Nullable final AppItemView.LongPressCallBack eventAction) {
        Intent i = new Intent();
        i.putExtra(DRAG_DROP_EXTRA, item);
        ClipData data = ClipData.newIntent(DRAG_DROP_INTENT, i);

        cachedDragBitmap = v.getDrawingCache();

        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                v.startDragAndDrop(data, new GoodDragShadowBuilder(v), new DragAction(action), 0);
            } else {
                //noinspection deprecation
                v.startDrag(data, new GoodDragShadowBuilder(v), new DragAction(action), 0);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        if (eventAction != null) {
            eventAction.afterDrag(v);
        }
    }

    public static <T extends Parcelable> T getDraggedObject(DragEvent dragEvent) {
        Intent intent = dragEvent.getClipData().getItemAt(0).getIntent();
        intent.setExtrasClassLoader(Item.class.getClassLoader());
        T item = intent.getParcelableExtra(DRAG_DROP_EXTRA);
        return item;
    }
}
