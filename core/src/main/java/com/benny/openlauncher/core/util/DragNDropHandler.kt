package com.benny.openlauncher.core.util

import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Parcelable
import android.view.DragEvent
import android.view.View
import com.benny.openlauncher.core.activity.CoreHome
import com.benny.openlauncher.core.interfaces.AbstractApp
import com.benny.openlauncher.core.manager.Setup
import com.benny.openlauncher.core.model.IconLabelItem

import com.benny.openlauncher.core.model.Item
import com.benny.openlauncher.core.viewutil.GoodDragShadowBuilder
import com.benny.openlauncher.core.widget.AppItemView

object DragNDropHandler {

    private val DRAG_DROP_EXTRA = "DRAG_DROP_EXTRA"
    private val DRAG_DROP_INTENT = "DRAG_DROP_INTENT"

    var cachedDragBitmap: Bitmap? = null

    private fun loadBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        var tempLabel : String? = null
        if (view is AppItemView){
            tempLabel = view.label
            view.label = " "
        }
        view.layout(0, 0, view.width, view.height)
        view.draw(canvas)
        if (view is AppItemView){
            view.label = tempLabel
        }
        view.parent.requestLayout()
        return bitmap
    }

    @JvmStatic
    fun startDrag(view: View, item: Item, action: DragAction.Action, eventAction: AppItemView.LongPressCallBack?) {
        cachedDragBitmap = loadBitmapFromView(view)

        CoreHome.launcher?.getDragNDropView()?.startDragNDropOverlay(view, item, action)

        eventAction?.afterDrag(view)
    }

    @JvmStatic
    fun <T : Parcelable> startDrag(view: View, item: T, action: DragAction.Action, eventAction: AppItemView.LongPressCallBack?) {
        val i = Intent()
        i.putExtra(DRAG_DROP_EXTRA, item)
        val data = ClipData.newIntent(DRAG_DROP_INTENT, i)

        cachedDragBitmap = loadBitmapFromView(view)

        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view.startDragAndDrop(data, GoodDragShadowBuilder(view), DragAction(action), 0)
            } else {
                view.startDrag(data, GoodDragShadowBuilder(view), DragAction(action), 0)
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

        eventAction?.afterDrag(view)
    }

    fun <T : Parcelable> getDraggedObject(dragEvent: DragEvent): T {
        val intent = dragEvent.clipData.getItemAt(0).intent
        intent.setExtrasClassLoader(Item::class.java.classLoader)
        return intent.getParcelableExtra(DRAG_DROP_EXTRA)
    }
}
