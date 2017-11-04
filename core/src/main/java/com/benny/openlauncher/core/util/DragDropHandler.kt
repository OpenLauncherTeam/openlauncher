package com.benny.openlauncher.core.util

import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Parcelable
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.widget.TextView

import com.benny.openlauncher.core.model.Item
import com.benny.openlauncher.core.util.DragDropHandler.DRAG_DROP_EXTRA
import com.benny.openlauncher.core.viewutil.GoodDragShadowBuilder
import com.benny.openlauncher.core.widget.AppItemView

object DragDropHandler {

    private val DRAG_DROP_EXTRA = "DRAG_DROP_EXTRA"
    private val DRAG_DROP_INTENT = "DRAG_DROP_INTENT"

    var cachedDragBitmap: Bitmap? = null

    private fun loadBitmapFromView(v: AppItemView): Bitmap {
        val bitmap: Bitmap = Bitmap.createScaledBitmap(v.drawingCache, v.iconSize.toInt(), v.iconSize.toInt(), true)
        v.isDrawingCacheEnabled = true
        v.isDrawingCacheEnabled = false
        return bitmap
    }

    fun <T : Parcelable> startDrag(v: View, item: T, action: DragAction.Action, eventAction: AppItemView.LongPressCallBack?) {
        val i = Intent()
        i.putExtra(DRAG_DROP_EXTRA, item)
        val data = ClipData.newIntent(DRAG_DROP_INTENT, i)

        cachedDragBitmap = if (v is AppItemView)
            loadBitmapFromView(v)
        else
            null

        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                v.startDragAndDrop(data, GoodDragShadowBuilder(v), DragAction(action), 0)
            } else {

                v.startDrag(data, GoodDragShadowBuilder(v), DragAction(action), 0)
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

        eventAction?.afterDrag(v)
    }

    fun <T : Parcelable> getDraggedObject(dragEvent: DragEvent): T {
        val intent = dragEvent.clipData.getItemAt(0).intent
        intent.setExtrasClassLoader(Item::class.java.classLoader)
        return intent.getParcelableExtra(DRAG_DROP_EXTRA)
    }
}
