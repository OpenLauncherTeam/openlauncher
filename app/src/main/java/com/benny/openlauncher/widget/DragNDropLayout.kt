package com.benny.openlauncher.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import com.benny.openlauncher.activity.CoreHome
import com.benny.openlauncher.manager.Setup
import com.benny.openlauncher.model.Item
import com.benny.openlauncher.model.PopupIconLabelItem
import com.benny.openlauncher.util.DragAction
import com.benny.openlauncher.util.DragNDropHandler
import com.benny.openlauncher.util.Tool
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator


/**
 * Created by BennyKok on 11/11/2017.
 */

//This custom class us more flexibility

class DragNDropLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    private val DRAG_THRESHOLD = 20f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val registeredDropTargetEntries = hashMapOf<DropTargetListener, DragFlag>()
    private val tempArrayOfInt2 = IntArray(2)

    //Dragging variable
    var dragging = false
        private set
    var dragLocation = PointF()
        private set
    var dragAction: DragAction.Action? = null
        private set
    private var dragLocationStart = PointF()
    private var dragLocationConverted = PointF()
    var dragExceedThreshold = false
        private set
    var dragView: View? = null
        private set
    var dragItem: Item? = null
        private set

    private var overlayIconScale = 1f

    //Drag shadow
    private val overlayView: OverlayView

    //Popup menu
    private val overlayPopup: RecyclerView
    private val overlayPopupAdapter = FastItemAdapter<PopupIconLabelItem>()
    private var overlayPopupShowing = false

    //Folder preview
    private var folderPreviewScale = 0f
    private var showFolderPreview = false
    private var previewLocation = PointF()

    private val slideInLeftAnimator = SlideInLeftAnimator(AccelerateDecelerateInterpolator())
    private val slideInRightAnimator = SlideInRightAnimator(AccelerateDecelerateInterpolator())

    init {
        paint.isFilterBitmap = true
        paint.color = Color.WHITE

        overlayView = OverlayView()

        overlayPopup = RecyclerView(context)
        overlayPopup.visibility = View.INVISIBLE
        overlayPopup.alpha = 0f
        overlayPopup.overScrollMode = View.OVER_SCROLL_NEVER
        overlayPopup.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        overlayPopup.itemAnimator = slideInLeftAnimator
        overlayPopup.adapter = overlayPopupAdapter

        addView(overlayView, FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(overlayPopup, FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))

        setWillNotDraw(false)
    }

    fun showFolderPreviewAt(fromView: View, x: Float, y: Float) {
        if (showFolderPreview) return

        showFolderPreview = true

        convertPoint(fromView, this, x, y)
        folderPreviewScale = 0f

        invalidate()
    }

    fun convertPoint(fromView: View, toView: View, x: Float, y: Float) {
        val fromCoordinate = IntArray(2)
        val toCoordinate = IntArray(2)
        fromView.getLocationOnScreen(fromCoordinate)
        toView.getLocationOnScreen(toCoordinate)

        previewLocation.set((fromCoordinate[0] - toCoordinate[0] + x),
                (fromCoordinate[1] - toCoordinate[1] + y))
    }

    fun cancelFolderPreview() {
        showFolderPreview = false
        previewLocation.set(-1f, -1f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas != null && showFolderPreview && !previewLocation.equals(-1f, -1f)) {
            folderPreviewScale += 0.08f
            folderPreviewScale = Tool.clampFloat(folderPreviewScale, 0.5f, 1f)
            canvas.drawCircle(previewLocation.x, previewLocation.y, Tool.toPx(Setup.appSettings().desktopIconSize / 2 + 10).toFloat() * folderPreviewScale, paint)
        }

        if (showFolderPreview) {
            invalidate()
        }
    }

    //This is always the top most view that able to drag the dragged icon over all the child content
    @SuppressLint("ResourceType")
    inner class OverlayView : View(context) {

        init {
            setWillNotDraw(false)
        }

        override fun onTouchEvent(event: MotionEvent?): Boolean {
            //The user has just tapped somewhere and the popup menu is showing, we need to intercept the touches and hide the popup menu
            if (event?.actionMasked == MotionEvent.ACTION_DOWN && !dragging && overlayPopupShowing) {
                hidePopupMenu()
                return true
            }
            return super.onTouchEvent(event)
        }

        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)

            if (canvas == null || DragNDropHandler.cachedDragBitmap == null || dragLocation.equals(-1f, -1f)) return

            val x = dragLocation.x - CoreHome.itemTouchX
            val y = dragLocation.y - CoreHome.itemTouchY

            if (dragging) {
                canvas.save()
                overlayIconScale = Tool.clampFloat(overlayIconScale + 0.05f, 1f, 1.1f)
                canvas.scale(overlayIconScale, overlayIconScale, x + DragNDropHandler.cachedDragBitmap!!.width / 2, y + DragNDropHandler.cachedDragBitmap!!.height / 2)

                canvas.drawBitmap(
                        DragNDropHandler.cachedDragBitmap,
                        x,
                        y,
                        paint
                )

                canvas.restore()
            }

            if (dragging)
                invalidate()
        }
    }

    @SuppressLint("ResourceType")
    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)

        overlayView.bringToFront()
        overlayPopup.bringToFront()
    }

    fun showPopupMenuForItem(x: Float, y: Float, popupItem: MutableList<PopupIconLabelItem>, listener: com.mikepenz.fastadapter.listeners.OnClickListener<PopupIconLabelItem>) {
        if (overlayPopupShowing) return
        overlayPopupShowing = true

        overlayPopup.visibility = View.VISIBLE

        overlayPopup.translationX = x
        overlayPopup.translationY = y

        overlayPopup.alpha = 1f
        //overlayPopup.animate().alpha(1f)

        overlayPopupAdapter.add(popupItem)
        overlayPopupAdapter.withOnClickListener(listener)
    }


    fun setPopupMenuShowDirection(left: Boolean) = if (left) {
        overlayPopup.itemAnimator = slideInLeftAnimator
    } else {
        overlayPopup.itemAnimator = slideInRightAnimator
    }

    fun hidePopupMenu() {
        if (!overlayPopupShowing) return
        overlayPopupShowing = false

        overlayPopup.animate().alpha(0f).withEndAction({
            overlayPopup.visibility = View.INVISIBLE
            overlayPopupAdapter.clear()
        })

        if (!dragging) {
            dragView = null
            dragItem = null
            dragAction = null
        }
    }

    fun startDragNDropOverlay(view: View, item: Item, action: DragAction.Action) {
        dragging = true
        dragExceedThreshold = false
        overlayIconScale = 0f

        dragView = view
        dragItem = item.copy()
        dragAction = action

        dragLocationStart.set(dragLocation)
        for (dropTarget in registeredDropTargetEntries) {
            convertPoint(dropTarget.key.view)
            dropTarget.value.shouldIgnore = !dropTarget.key.onStart(dragAction!!, dragLocationConverted, isViewContains(dropTarget.key.view, dragLocation.x.toInt(), dragLocation.y.toInt()))
        }

        overlayView.invalidate()
    }

    override fun onDetachedFromWindow() {
        cancelAllDragNDrop()

        super.onDetachedFromWindow()
    }

    fun cancelAllDragNDrop() {
        dragging = false

        //This will be handled when the popup menu is closed
        if (!overlayPopupShowing) {
            dragView = null
            dragItem = null
            dragAction = null
        }

        for (dropTarget in registeredDropTargetEntries) {
            dropTarget.key.onEnd()
        }
    }

    fun registerDropTarget(targetListener: DropTargetListener) {
        registeredDropTargetEntries += Pair(targetListener, DragFlag())
    }

    fun unregisterDropTarget(targetListener: DropTargetListener) {
        registeredDropTargetEntries -= targetListener
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        //The user has just dropped the same place the item was picked up, finished the drag n drop
        if (event?.actionMasked == MotionEvent.ACTION_UP && dragging) {
            handleDragFinished()
        }

        if (dragging) {
            return true
        } else {
            if (event != null)
                dragLocation.set(event.x, event.y)
        }
        return super.onInterceptTouchEvent(event)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null || !dragging) return super.onTouchEvent(event)

        dragLocation.set(event.x, event.y)

        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                handleMovement()
            }
            MotionEvent.ACTION_UP -> {
                handleDragFinished()
            }
        }

        //We are going to intercept all of the touch input when dragging
        if (dragging) return true
        return super.onTouchEvent(event)
    }

    private fun handleMovement() {
        if (!dragExceedThreshold && (Math.abs(dragLocationStart.x - dragLocation.x) > DRAG_THRESHOLD || Math.abs(dragLocationStart.y - dragLocation.y) > DRAG_THRESHOLD)) {
            dragExceedThreshold = true

            for (dropTarget in registeredDropTargetEntries) {
                if (dropTarget.value.shouldIgnore) continue

                convertPoint(dropTarget.key.view)

                dropTarget.key.onStartDrag(dragAction!!, dragLocationConverted)
            }
        }

        if (dragExceedThreshold)
            hidePopupMenu()

        for (dropTarget in registeredDropTargetEntries) {
            if (dropTarget.value.shouldIgnore) continue

            convertPoint(dropTarget.key.view)

            if (isViewContains(dropTarget.key.view, dragLocation.x.toInt(), dragLocation.y.toInt())) {
                dropTarget.key.onMove(dragAction!!, dragLocationConverted)

                if (dropTarget.value.previousOutside) {
                    dropTarget.value.previousOutside = false
                    dropTarget.key.onEnter(dragAction!!, dragLocationConverted)
                }
            } else {
                if (!dropTarget.value.previousOutside) {
                    dropTarget.value.previousOutside = true
                    dropTarget.key.onExit(dragAction!!, dragLocationConverted)
                }
            }
        }
    }

    private fun handleDragFinished() {
        dragging = false
        for (dropTarget in registeredDropTargetEntries) {
            if (dropTarget.value.shouldIgnore)
                continue

            if (isViewContains(dropTarget.key.view, dragLocation.x.toInt(), dragLocation.y.toInt())) {
                convertPoint(dropTarget.key.view)
                dropTarget.key.onDrop(dragAction!!, dragLocationConverted, dragItem!!)
            }
        }
        for (dropTarget in registeredDropTargetEntries) {
            dropTarget.key.onEnd()
        }
        cancelFolderPreview()
    }

    fun convertPoint(toView: View) {
        val fromCoordinate = IntArray(2)
        val toCoordinate = IntArray(2)
        getLocationOnScreen(fromCoordinate)
        toView.getLocationOnScreen(toCoordinate)

        dragLocationConverted.set((fromCoordinate[0] - toCoordinate[0] + dragLocation.x),
                (fromCoordinate[1] - toCoordinate[1] + dragLocation.y))
    }

    private fun isViewContains(view: View, rx: Int, ry: Int): Boolean {
        view.getLocationOnScreen(tempArrayOfInt2)
        val x = tempArrayOfInt2[0]
        val y = tempArrayOfInt2[1]
        val w = view.width
        val h = view.height

        return !(rx < x || rx > x + w || ry < y || ry > y + h)
    }

    open class DropTargetListener(val view: View) {
        open fun onStart(action: DragAction.Action, location: PointF, isInside: Boolean): Boolean = false
        open fun onStartDrag(action: DragAction.Action, location: PointF) {}
        open fun onDrop(action: DragAction.Action, location: PointF, item: Item) {}
        open fun onMove(action: DragAction.Action, location: PointF) {}
        open fun onEnter(action: DragAction.Action, location: PointF) {}
        open fun onExit(action: DragAction.Action, location: PointF) {}
        open fun onEnd() {}
    }

    class DragFlag {
        var previousOutside = true
        var shouldIgnore = false
    }
}