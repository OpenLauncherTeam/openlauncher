package com.benny.openlauncher.widget

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import com.benny.openlauncher.activity.CoreHome
import com.benny.openlauncher.manager.Setup
import com.benny.openlauncher.model.Item
import com.benny.openlauncher.util.DragAction
import com.benny.openlauncher.util.DragNDropHandler
import com.benny.openlauncher.util.Tool
import com.benny.openlauncher.viewutil.DesktopCallBack
import com.benny.openlauncher.viewutil.ItemViewFactory

class Dock @JvmOverloads constructor(c: Context, attr: AttributeSet? = null) : CellContainer(c, attr), DesktopCallBack<View> {
    var previousItemView: View? = null
    var previousItem: Item? = null
    private var startPosX: Float = 0.toFloat()
    private var startPosY: Float = 0.toFloat()
    private var home: CoreHome? = null
    private val coordinate = Point()

    override fun init() {
        if (isInEditMode) {
            return
        }
        super.init()
    }

    fun initDockItem(home: CoreHome) {
        val columns = Setup.appSettings().dockSize
        setGridSize(columns, 1)
        val dockItems = CoreHome.db.dock

        this.home = home
        removeAllViews()
        for (item in dockItems) {
            if (item.x < columns && item.y == 0) {
                addItemToPage(item, 0)
            }
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean = super.onTouchEvent(ev)

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        detectSwipe(ev)
        super.dispatchTouchEvent(ev)
        return true
    }

    private fun detectSwipe(ev: MotionEvent) {
        if (CoreHome.launcher == null) return
        when (ev.action) {
            MotionEvent.ACTION_UP -> {
                Tool.print("ACTION_UP")
                val minDist = 150f
                Tool.print(ev.x.toInt(), ev.y.toInt())
                if (startPosY - ev.y > minDist) {
                    if (Setup.appSettings().gestureDockSwipeUp) {
                        val p = Tool.convertPoint(Point(ev.x.toInt(), ev.y.toInt()), this, CoreHome.launcher!!.getAppDrawerController())
                        if (Setup.appSettings().isGestureFeedback)
                            Tool.vibrate(this)
                        CoreHome.launcher!!.openAppDrawer(this, p.x, p.y)
                    }
                }
            }
            MotionEvent.ACTION_DOWN -> {
                Tool.print("ACTION_DOWN")
                startPosX = ev.x
                startPosY = ev.y
            }
        }
    }

    private val previousDragPoint = Point()

    fun updateIconProjection(x: Int, y: Int) {
        val state = peekItemAndSwap(x, y, coordinate)

        if (previousDragPoint != coordinate)
            CoreHome.launcher?.getDragNDropView()?.cancelFolderPreview()
        previousDragPoint.set(coordinate.x, coordinate.y)

        when (state) {
            CellContainer.DragState.CurrentNotOccupied -> projectImageOutlineAt(coordinate, DragNDropHandler.cachedDragBitmap)
            CellContainer.DragState.OutOffRange -> {

            }
            CellContainer.DragState.ItemViewNotFound -> {
            }
            CellContainer.DragState.CurrentOccupied -> {
                clearCachedOutlineBitmap()

                val action = CoreHome.launcher?.getDragNDropView()?.dragAction
                if (action != DragAction.Action.WIDGET && action != DragAction.Action.ACTION &&
                        coordinateToChildView(coordinate) is AppItemView)
                    CoreHome.launcher?.getDragNDropView()?.showFolderPreviewAt(
                            this,
                            cellWidth * (coordinate.x + 0.5f),
                            cellHeight * (coordinate.y + 0.5f) - if (Setup.appSettings().isDockShowLabel) Tool.toPx(7) else 0
                    )
            }
        }
    }

    override fun setLastItem(vararg args: Any) {
        // args stores the item in [0] and the view reference in [1]
        val v = args[1] as View
        val item = args[0] as Item

        previousItemView = v
        previousItem = item
        removeView(v)
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            bottomInset = insets.systemWindowInsetBottom
            setPadding(paddingLeft, paddingTop, paddingRight, bottomInset)
        }
        return insets
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (isInEditMode) return

        var height = View.getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)

        val iconSize = Setup.appSettings().dockIconSize
        if (Setup.appSettings().isDockShowLabel) {
            height = Tool.dp2px(16 + iconSize + 14 + 10, context) + Dock.bottomInset
        } else {
            height = Tool.dp2px(16 + iconSize + 10, context) + Dock.bottomInset
        }
        layoutParams.height = height

        setMeasuredDimension(View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec), height)

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun consumeRevert() {
        previousItem = null
        previousItemView = null
    }

    override fun revertLastItem() {
        if (previousItemView != null) {
            addViewToGrid(previousItemView!!)
            previousItem = null
            previousItemView = null
        }
    }

    override fun addItemToPage(item: Item, page: Int): Boolean {
        val itemView = ItemViewFactory.getItemView(context, item, Setup.appSettings().isDockShowLabel, this, Setup.appSettings().dockIconSize)

        if (itemView == null) {
            CoreHome.db.deleteItem(item, true)
            return false
        } else {
            item.locationInLauncher = Item.LOCATION_DOCK
            addViewToGrid(itemView, item.x, item.y, item.spanX, item.spanY)
            return true
        }
    }

    override fun addItemToPoint(item: Item, x: Int, y: Int): Boolean {
        val positionToLayoutPrams = coordinateToLayoutParams(x, y, item.spanX, item.spanY)
        if (positionToLayoutPrams != null) {
            item.locationInLauncher = Item.LOCATION_DOCK

            item.x = positionToLayoutPrams.x
            item.y = positionToLayoutPrams.y

            val itemView = ItemViewFactory.getItemView(context, item, Setup.appSettings().isDockShowLabel, this, Setup.appSettings().dockIconSize)

            if (itemView != null) {
                itemView.layoutParams = positionToLayoutPrams
                addView(itemView)
            }
            return true
        } else {
            return false
        }
    }

    override fun addItemToCell(item: Item, x: Int, y: Int): Boolean {
        item.locationInLauncher = Item.LOCATION_DOCK

        item.x = x
        item.y = y

        val itemView = ItemViewFactory.getItemView(context, item, Setup.appSettings().isDockShowLabel, this, Setup.appSettings().dockIconSize)

        if (itemView != null) {
            addViewToGrid(itemView, item.x, item.y, item.spanX, item.spanY)
            return true
        } else {
            return false
        }
    }

    override fun removeItem(view: View, animate: Boolean) {
        if (animate)
            view.animate().setDuration(100L).scaleX(0f).scaleY(0f).withEndAction({
                if (view.parent == this)
                    removeView(view)
            })
        else
            if (view.parent == this)
                removeView(view)
    }

    companion object {
        var bottomInset: Int = 0
    }
}
