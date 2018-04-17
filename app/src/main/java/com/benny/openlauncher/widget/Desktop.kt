package com.benny.openlauncher.widget

import `in`.championswimmer.sfg.lib.SimpleFingerGestures
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.animation.AccelerateDecelerateInterpolator
import com.benny.openlauncher.activity.Home
import com.benny.openlauncher.manager.Setup
import com.benny.openlauncher.model.Item
import com.benny.openlauncher.util.*
import com.benny.openlauncher.viewutil.DesktopCallBack
import com.benny.openlauncher.viewutil.DesktopGestureListener
import com.benny.openlauncher.viewutil.ItemViewFactory
import com.benny.openlauncher.viewutil.SmoothPagerAdapter
import java.util.*

class Desktop @JvmOverloads constructor(c: Context, attr: AttributeSet? = null) : SmoothViewPager(c, attr), DesktopCallBack<View> {

    val pages: MutableList<CellContainer> = ArrayList<CellContainer>()
    var desktopEditListener: OnDesktopEditListener? = null
    var previousItemView: View? = null
    var previousItem: Item? = null
    var inEditMode: Boolean = false
    var previousPage = -1
    var pageCount: Int = 0

    private var home: Home? = null
    private var pageIndicator: PagerIndicator? = null
    private val coordinate = Point(-1, -1)

    val isCurrentPageEmpty: Boolean
        get() = currentPage.childCount == 0

    val currentPage: CellContainer
        get() = pages[currentItem]

    fun setPageIndicator(pageIndicator: PagerIndicator) {
        this.pageIndicator = pageIndicator
    }

    fun init() {
        if (isInEditMode) {
            return
        }

        pageCount = Home.db.desktop.size
        if (pageCount == 0) {
            pageCount = 1
        }

        currentItem = Setup.appSettings().desktopPageCurrent
    }

    fun initDesktopNormal(home: Home) {
        adapter = DesktopAdapter(this)
        if (Setup.appSettings().isDesktopShowIndicator && pageIndicator != null) {
            pageIndicator!!.setViewPager(this)
        }
        this.home = home

        val columns = Setup.appSettings().desktopColumnCount
        val rows = Setup.appSettings().desktopRowCount
        val desktopItems = Home.db.desktop
        for (pageCount in desktopItems.indices) {
            if (pages.size <= pageCount) break
            pages[pageCount].removeAllViews()
            val items = desktopItems[pageCount]
            for (j in items.indices) {
                val item = items[j]
                if (item.x + item.spanX <= columns && item.y + item.spanY <= rows) {
                    addItemToPage(item, pageCount)
                }
            }
        }
    }

    fun initDesktopShowAll(c: Context, home: Home) {
        val apps = ArrayList<Item>()
        val allApps = Setup.appLoader().getAllApps(c, false)
        for (app in allApps)
            apps.add(Item.newAppItem(app))

        var appsSize = apps.size

        // reset page count
        pageCount = 0
        val columns = Setup.appSettings().desktopColumnCount
        val rows = Setup.appSettings().desktopRowCount
        appsSize -= columns * rows
        while (appsSize >= columns * rows || appsSize > -(columns * rows)) {
            pageCount++
        }

        adapter = DesktopAdapter(this)
        if (Setup.appSettings().isDesktopShowIndicator && pageIndicator != null) {
            pageIndicator!!.setViewPager(this)
        }
        this.home = home

        // fill the desktop adapter
        for (i in 0 until pageCount) {
            for (x in 0 until columns) {
                for (y in 0 until rows) {
                    val pagePos = y * rows + x
                    val pos = columns * rows * i + pagePos
                    if (pos < apps.size) {
                        val appItem = apps[pos]
                        appItem.x = x
                        appItem.y = y
                        addItemToPage(appItem, i)
                    }
                }
            }
        }
    }

    fun addPageRight(showGrid: Boolean) {
        pageCount++

        val previousPage = currentItem
        (adapter as DesktopAdapter).addPageRight()
        currentItem = previousPage + 1

        if (!Setup.appSettings().isDesktopHideGrid)
            for (cellContainer in pages) {
                cellContainer.setHideGrid(!showGrid)
            }
        pageIndicator!!.invalidate()
    }

    fun addPageLeft(showGrid: Boolean) {
        pageCount++

        val previousPage = currentItem
        (adapter as DesktopAdapter).addPageLeft()
        setCurrentItem(previousPage + 1, false)
        currentItem = previousPage - 1

        if (!Setup.appSettings().isDesktopHideGrid)
            for (cellContainer in pages) {
                cellContainer.setHideGrid(!showGrid)
            }
        pageIndicator!!.invalidate()
    }

    fun removeCurrentPage() {
        if (Setup.appSettings().desktopStyle == DesktopMode.SHOW_ALL_APPS)
            return

        pageCount--

        val previousPage = currentItem
        (adapter as DesktopAdapter).removePage(currentItem, true)

        for (v in pages) {
            v.alpha = 0f
            v.animate().alpha(1f)
            v.scaleX = 0.85f
            v.scaleY = 0.85f
            v.animateBackgroundShow()
        }

        if (pageCount == 0) {
            addPageRight(false)
            (adapter as DesktopAdapter).exitDesktopEditMode()
        } else {
            currentItem = previousPage
            pageIndicator!!.invalidate()
        }
    }

    private val previousDragPoint = Point()

    fun updateIconProjection(x: Int, y: Int) {
        val state = currentPage.peekItemAndSwap(x, y, coordinate)

        if (previousDragPoint != coordinate)
            Home.launcher?.getDragNDropView()?.cancelFolderPreview()
        previousDragPoint.set(coordinate.x, coordinate.y)

        when (state) {
            CellContainer.DragState.CurrentNotOccupied -> currentPage.projectImageOutlineAt(coordinate, DragNDropHandler.cachedDragBitmap)
            CellContainer.DragState.OutOffRange -> {

            }
            CellContainer.DragState.ItemViewNotFound -> {
            }
            CellContainer.DragState.CurrentOccupied -> {
                for (page in pages)
                    page.clearCachedOutlineBitmap()

                val action = Home.launcher?.getDragNDropView()?.dragAction
                if (action != DragAction.Action.WIDGET && action != DragAction.Action.ACTION &&
                        currentPage.coordinateToChildView(coordinate) is AppItemView)
                    Home.launcher?.getDragNDropView()?.showFolderPreviewAt(
                            this,
                            currentPage.cellWidth * (coordinate.x + 0.5f),
                            currentPage.cellHeight * (coordinate.y + 0.5f) - if (Setup.appSettings().isDesktopShowLabel) Tool.toPx(7) else 0
                    )
            }
        }
    }

    override fun setLastItem(vararg args: Any) {
        // args stores the item in [0] and the view reference in [1]
        val item = args[0] as Item
        val v = args[1] as View

        previousPage = currentItem
        previousItemView = v
        previousItem = item
        currentPage.removeView(v)
    }

    override fun revertLastItem() {
        if (previousItemView != null && adapter.count >= previousPage && previousPage > -1) {
            pages[previousPage].addViewToGrid(previousItemView!!)
            previousItem = null
            previousItemView = null
            previousPage = -1
        }
    }

    override fun consumeRevert() {
        previousItem = null
        previousItemView = null
        previousPage = -1
    }

    override fun addItemToPage(item: Item, page: Int): Boolean {
        val itemView = ItemViewFactory.getItemView(context, item, Setup.appSettings().isDesktopShowLabel, this, Setup.appSettings().desktopIconSize)

        if (itemView == null) {
            Home.db.deleteItem(item, true)
            return false
        } else {
            item.locationInLauncher = Item.LOCATION_DESKTOP
            pages[page].addViewToGrid(itemView, item.x, item.y, item.spanX, item.spanY)
            return true
        }
    }

    override fun addItemToPoint(item: Item, x: Int, y: Int): Boolean {
        val positionToLayoutPrams = currentPage.coordinateToLayoutParams(x, y, item.spanX, item.spanY)
        if (positionToLayoutPrams != null) {
            item.locationInLauncher = Item.LOCATION_DESKTOP

            item.x = positionToLayoutPrams.x
            item.y = positionToLayoutPrams.y

            val itemView = ItemViewFactory.getItemView(context, item, Setup.appSettings().isDesktopShowLabel, this, Setup.appSettings().desktopIconSize)

            if (itemView != null) {
                itemView.layoutParams = positionToLayoutPrams
                currentPage.addView(itemView)
            }
            return true
        } else {
            return false
        }
    }

    override fun addItemToCell(item: Item, x: Int, y: Int): Boolean {
        item.locationInLauncher = Item.LOCATION_DESKTOP

        item.x = x
        item.y = y

        val itemView = ItemViewFactory.getItemView(context, item, Setup.appSettings().isDesktopShowLabel, this, Setup.appSettings().desktopIconSize)

        return if (itemView != null) {
            currentPage.addViewToGrid(itemView, item.x, item.y, item.spanX, item.spanY)
            true
        } else {
            false
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {

        when (ev!!.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                Home.launcher?.getDesktopIndicator()?.showNow()
            }
        }

        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {

        when (ev!!.actionMasked) {
            MotionEvent.ACTION_UP -> {
                Home.launcher?.getDesktopIndicator()?.hideDelay()
            }
        }

        return super.onTouchEvent(ev)
    }

    override fun removeItem(view: View, animate: Boolean) {
        Tool.print("Start Removing a view from Desktop")
        if (animate)
            view.animate().setDuration(100L).scaleX(0f).scaleY(0f).withEndAction({
                Tool.print("Ok Removing a view from Desktop")
                if (view.parent == currentPage)
                    currentPage.removeView(view)
            })
        else
            if (view.parent == currentPage)
                currentPage.removeView(view)
    }

    override fun onPageScrolled(position: Int, offset: Float, offsetPixels: Int) {
        if (isInEditMode) {
            return
        }
        Home.launcher?.getDragNDropView()?.cancelFolderPreview()
        WallpaperManager.getInstance(context).setWallpaperOffsets(windowToken, (position + offset) / (pageCount - 1), 0f)
        super.onPageScrolled(position, offset, offsetPixels)
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            topInset = insets.systemWindowInsetTop
            bottomInset = insets.systemWindowInsetBottom
            Home.launcher!!.updateHomeLayout()
        }
        return insets
    }

    interface OnDesktopEditListener {
        fun onDesktopEdit()

        fun onFinishDesktopEdit()
    }

    object DesktopMode {
        val NORMAL = 0
        val SHOW_ALL_APPS = 1
    }

    inner class DesktopAdapter(private val desktop: Desktop) : SmoothPagerAdapter() {
        internal var scaleFactor = 1.0f
        internal var translateFactor = 0.0f
        private var currentEvent: MotionEvent? = null

        private val gestureListener: SimpleFingerGestures.OnFingerGestureListener
            get() = DesktopGestureListener(desktop, Setup.desktopGestureCallback())

        private val itemLayout: CellContainer
            get() {
                val layout = CellContainer(desktop.context)
                layout.isSoundEffectsEnabled = false

                val mySfg = SimpleFingerGestures()
                mySfg.setOnFingerGestureListener(gestureListener)

                layout.gestures = mySfg
                layout.onItemRearrangeListener = object : CellContainer.OnItemRearrangeListener {
                    override fun onItemRearrange(from: Point, to: Point) {
                        val itemFromCoordinate = Desktop.getItemFromCoordinate(from, currentItem) ?: return
                        itemFromCoordinate.x = to.x
                        itemFromCoordinate.y = to.y
                    }
                }
                layout.setOnTouchListener { _, event ->
                    currentEvent = event
                    false
                }
                layout.setGridSize(Setup.appSettings().desktopColumnCount, Setup.appSettings().desktopRowCount)
                layout.setOnClickListener { view ->
                    if (!desktop.inEditMode && currentEvent != null) {
                        WallpaperManager.getInstance(view.context).sendWallpaperCommand(view.windowToken, WallpaperManager.COMMAND_TAP, currentEvent!!.x.toInt(), currentEvent!!.y.toInt(), 0, null)
                    }

                    exitDesktopEditMode()
                }
                layout.setOnLongClickListener {
                    enterDesktopEditMode()
                    true
                }
                return layout
            }

        init {
            desktop.pages.clear()
            for (i in 0 until count) {
                desktop.pages.add(itemLayout)
            }
        }

        fun addPageLeft() {
            desktop.pages.add(0, itemLayout)
            notifyDataSetChanged()
        }

        fun addPageRight() {
            desktop.pages.add(itemLayout)
            notifyDataSetChanged()
        }

        fun removePage(position: Int, deleteItems: Boolean) {
            if (deleteItems) {
                val views = desktop.pages[position].allCells
                for (v in views) {
                    val item = v.tag
                    if (item is Item) {
                        Home.db.deleteItem(item, true)
                    }
                }
            }
            desktop.pages.removeAt(position)
            notifyDataSetChanged()
        }

        override fun getItemPosition(`object`: Any): Int {
            return SmoothPagerAdapter.POSITION_NONE
        }

        override fun getCount(): Int {
            return desktop.pageCount
        }

        override fun isViewFromObject(p1: View, p2: Any): Boolean {
            return p1 === p2
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun instantiateItem(container: ViewGroup, pos: Int): Any {
            val layout = desktop.pages[pos]
            container.addView(layout)
            return layout
        }

        fun enterDesktopEditMode() {
            scaleFactor = 0.8f
            translateFactor = Tool.toPx(if (Setup.appSettings().searchBarEnable) 20 else 40).toFloat()
            for (v in desktop.pages) {
                v.blockTouch = true
                v.animateBackgroundShow()
                v.animate().scaleX(scaleFactor).scaleY(scaleFactor).translationY(translateFactor).interpolator = AccelerateDecelerateInterpolator()
            }
            desktop.inEditMode = true
            if (desktop.desktopEditListener != null) {
                desktop.desktopEditListener!!.onDesktopEdit()
            }
        }

        fun exitDesktopEditMode() {
            scaleFactor = 1.0f
            translateFactor = 0f
            for (v in desktop.pages) {
                v.blockTouch = false
                v.animateBackgroundHide()
                v.animate().scaleX(scaleFactor).scaleY(scaleFactor).translationY(translateFactor).interpolator = AccelerateDecelerateInterpolator()
            }
            desktop.inEditMode = false
            if (desktop.desktopEditListener != null) {
                desktop.desktopEditListener!!.onFinishDesktopEdit()
            }
        }
    }

    companion object {
        var topInset: Int = 0
        var bottomInset: Int = 0

        fun getItemFromCoordinate(point: Point, page: Int): Item? {
            val desktopItems = Home.db.desktop
            val pageData = desktopItems[page]
            for (i in pageData.indices) {
                val item = pageData[i]
                if (item.x == point.x && item.y == point.y && item.spanX == 1 && item.spanY == 1) {
                    return pageData[i]
                }
            }
            return null
        }

        fun handleOnDropOver(home: Home?, dropItem: Item?, item: Item?, itemView: View, parent: ViewGroup, page: Int, itemPosition: Definitions.ItemPosition, callback: DesktopCallBack<*>): Boolean {
            if (item == null || dropItem == null) {
                return false
            }

            when (item.type) {
                Item.Type.APP, Item.Type.SHORTCUT -> if (dropItem.type == Item.Type.APP || dropItem.type == Item.Type.SHORTCUT) {
                    parent.removeView(itemView)

                    // create a new group item
                    val group = Item.newGroupItem()
                    group.groupItems.add(item)
                    group.groupItems.add(dropItem)
                    group.x = item.x
                    group.y = item.y

                    // add the drop item just in case it is coming from the app drawer
                    Home.db.saveItem(dropItem, page, itemPosition)

                    // hide the apps added to the group
                    Home.db.saveItem(item, Definitions.ItemState.Hidden)
                    Home.db.saveItem(dropItem, Definitions.ItemState.Hidden)

                    // add the item to the database
                    Home.db.saveItem(group, page, itemPosition)

                    callback.addItemToPage(group, page)

                    Home.launcher?.getDesktop()?.consumeRevert()
                    Home.launcher?.getDock()?.consumeRevert()
                    return true
                }
                Item.Type.GROUP -> if ((dropItem.type == Item.Type.APP || dropItem.type == Item.Type.SHORTCUT) && item.groupItems.size < GroupPopupView.GroupDef.maxItem) {
                    parent.removeView(itemView)

                    item.groupItems.add(dropItem)

                    // add the drop item just in case it is coming from the app drawer
                    Home.db.saveItem(dropItem, page, itemPosition)

                    // hide the new app in the group
                    Home.db.saveItem(dropItem, Definitions.ItemState.Hidden)

                    // add the item to the database
                    Home.db.saveItem(item, page, itemPosition)

                    callback.addItemToPage(item, page)

                    Home.launcher?.getDesktop()?.consumeRevert()
                    Home.launcher?.getDock()?.consumeRevert()
                    return true
                }
                else -> {
                }
            }
            return false
        }
    }
}
