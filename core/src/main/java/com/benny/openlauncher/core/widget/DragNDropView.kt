package com.benny.openlauncher.core.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.benny.openlauncher.core.util.DragNDropHandler

/**
 * Created by BennyKok on 11/11/2017.
 */

class DragNDropView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val registeredDropTarget = arrayListOf<DropTargetListener>()
    val tempRect = Rect()

    //Dragging variable
    var dragging = true
    var dragLocation = PointF()

    init {
        paint.isFilterBitmap = true
        paint.color = Color.WHITE
    }

    fun startDragNDropOverlay() {
        dragging = true
    }

    fun registerDropTarget(targetListener: DropTargetListener) {
        registeredDropTarget += targetListener
    }

    fun unregisterDropTarget(targetListener: DropTargetListener) {
        registeredDropTarget -= targetListener
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return super.onTouchEvent(event)

        dragLocation.set(event.x, event.y)
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                for (dropTarget in registeredDropTarget) {
                    dropTarget.getView().getGlobalVisibleRect(tempRect)
                    if (tempRect.contains(dragLocation.x.toInt(), dragLocation.y.toInt()))
                        dropTarget.onInside()
                    else
                        dropTarget.onOutside()
                }
            }
            MotionEvent.ACTION_UP -> {
                dragging = false
                for (dropTarget in registeredDropTarget) {
                    dropTarget.getView().getGlobalVisibleRect(tempRect)
                    if (tempRect.contains(dragLocation.x.toInt(), dragLocation.y.toInt()))
                        dropTarget.onDrop()
                }
            }
        }

        //We are going to intercept all of the touch input when dragging
        if (dragging) return true
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (dragging && DragNDropHandler.cachedDragBitmap != null)
            canvas?.drawBitmap(
                    DragNDropHandler.cachedDragBitmap,
                    dragLocation.x - DragNDropHandler.cachedDragBitmap!!.width / 2,
                    dragLocation.y - DragNDropHandler.cachedDragBitmap!!.height / 2,
                    paint
            )
    }

    interface DropTargetListener {
        fun onDrop()
        fun onInside()
        fun onOutside()
        fun getView(): View
    }
}