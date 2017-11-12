package com.benny.openlauncher.core.widget

import `in`.championswimmer.sfg.lib.SimpleFingerGestures
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.view.View.OnDragListener
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import com.benny.openlauncher.core.R
import com.benny.openlauncher.core.activity.CoreHome
import com.benny.openlauncher.core.manager.Setup
import com.benny.openlauncher.core.model.Item
import com.benny.openlauncher.core.util.*
import com.benny.openlauncher.core.viewutil.DesktopCallBack
import com.benny.openlauncher.core.viewutil.DesktopGestureListener
import com.benny.openlauncher.core.viewutil.ItemViewFactory
import com.benny.openlauncher.core.viewutil.SmoothPagerAdapter
import java.util.*

class Desktop @JvmOverloads constructor(c: Context, attr: AttributeSet? = null) : SmoothViewPager(c, attr), OnDragListener, DesktopCallBack<View> {

    val pages: MutableList<CellContainer> = ArrayList()
    var desktopEditListener: OnDesktopEditListener? = null
    var previousItemView: View? = null
    var previousItem: Item? = null
    var inEditMode: Boolean = false
    var previousPage = -1
    var pageCount: Int = 0

    private var home: CoreHome? = null
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

        pageCount = CoreHome.db.desktop.size
        if (pageCount == 0) {
            pageCount = 1
        }

        setOnDragListener(this)
        currentItem = Setup.appSettings().desktopPageCurrent
    }

    fun initDesktopNormal(home: CoreHome) {
        adapter = DesktopAdapter(this)
        if (Setup.appSettings().isDesktopShowIndicator && pageIndicator != null) {
            pageIndicator!!.setViewPager(this)
        }
        this.home = home

        val columns = Setup.appSettings().desktopColumnCount
        val rows = Setup.appSettings().desktopRowCount
        val desktopItems = CoreHome.db.desktop
        for (pageCount in desktopItems.indices) {
            if (pages.size <= pageCount) break
            pages[pageCount].removeAllViews()
            val items = desktopItems[pageCount]
            for (j in items.indices) {
                val item = items[j]
                if (item.getX() + item.getSpanX() <= columns && item.getY() + item.getSpanY() <= rows) {
                    addItemToPage(item, pageCount)
                }
            }
        }
    }

    fun initDesktopShowAll(c: Context, home: CoreHome) {
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
                        appItem.setX(x)
                        appItem.setY(y)
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

    override fun onDrag(p1: View, event: DragEvent): Boolean {
        val currentPage = currentPage
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                coordinate.set(-1, -1)
                for (page in pages)
                    page.clearCachedOutlineBitmap()
                return true
            }
            DragEvent.ACTION_DRAG_ENTERED -> return true
            DragEvent.ACTION_DRAG_EXITED -> {
                for (page in pages)
                    page.clearCachedOutlineBitmap()
                return true
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                updateIconProjection(event.x.toInt(), event.y.toInt())
                return true
            }
            DragEvent.ACTION_DROP -> {
                for (page in pages)
                    page.clearCachedOutlineBitmap()
                val item = DragNDropHandler.getDraggedObject<Item>(event)
                // this statement makes sure that adding an app multiple times from the app drawer works
                // the app will get a new id every time
                if ((event.localState as DragAction).action == DragAction.Action.APP_DRAWER) {
                    item.reset()
                }

                if (addItemToPoint(item, event.x.toInt(), event.y.toInt())) {
                    home!!.getDesktop().consumeRevert()
                    home!!.getDock().consumeRevert()
                    // add the item to the database
                    CoreHome.db.saveItem(item, currentItem, Definitions.ItemPosition.Desktop)
                } else {
                    val pos = Point()
                    currentPage.touchPosToCoordinate(pos, event.x.toInt(), event.y.toInt(), item.getSpanX(), item.getSpanY(), false)
                    val itemView = currentPage.coordinateToChildView(pos)
                    if (itemView != null && Desktop.handleOnDropOver(home, item, itemView.tag as Item, itemView, currentPage, currentItem, Definitions.ItemPosition.Desktop, this)) {
                        home!!.getDesktop().consumeRevert()
                        home!!.getDock().consumeRevert()
                    } else {
                        Toast.makeText(context, R.string.toast_not_enough_space, Toast.LENGTH_SHORT).show()
                        home!!.getDock().revertLastItem()
                        home!!.getDesktop().revertLastItem()
                    }
                }
                return true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                for (page in pages)
                    page.clearCachedOutlineBitmap()
                return true
            }
        }

        currentPage.invalidate()
        return false
    }

    private fun updateIconProjection(x: Int, y: Int) {
        val currentPage = currentPage
        val state = currentPage.peekItemAndSwap(x, y, coordinate)
        when (state) {
            CellContainer.DragState.CurrentNotOccupied -> currentPage.projectImageOutlineAt(coordinate, DragNDropHandler.cachedDragBitmap)
            CellContainer.DragState.OutOffRange -> {
            }
            CellContainer.DragState.ItemViewNotFound -> {
            }
            CellContainer.DragState.CurrentOccupied -> for (page in pages)
                page.clearCachedOutlineBitmap()
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
            currentPage.addViewToGrid(previousItemView!!)
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
            CoreHome.db.deleteItem(item, true)
            return false
        } else {
            item.locationInLauncher = Item.LOCATION_DESKTOP
            pages[page].addViewToGrid(itemView, item.getX(), item.getY(), item.getSpanX(), item.getSpanY())
            return true
        }
    }

    override fun addItemToPoint(item: Item, x: Int, y: Int): Boolean {
        val positionToLayoutPrams = currentPage.coordinateToLayoutParams(x, y, item.getSpanX(), item.getSpanY())
        if (positionToLayoutPrams != null) {
            item.locationInLauncher = Item.LOCATION_DESKTOP

            item.setX(positionToLayoutPrams.x)
            item.setY(positionToLayoutPrams.y)

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

        item.setX(x)
        item.setY(y)

        val itemView = ItemViewFactory.getItemView(context, item, Setup.appSettings().isDesktopShowLabel, this, Setup.appSettings().desktopIconSize)

        if (itemView != null) {
            currentPage.addViewToGrid(itemView, item.getX(), item.getY(), item.getSpanX(), item.getSpanY())
            return true
        } else {
            return false
        }
    }

    override fun removeItem(view: View?) {
        currentPage.removeViewInLayout(view)
    }

    override fun onPageScrolled(position: Int, offset: Float, offsetPixels: Int) {
        if (isInEditMode) {
            return
        }
        WallpaperManager.getInstance(context).setWallpaperOffsets(windowToken, (position + offset) / (pageCount - 1), 0f)
        super.onPageScrolled(position, offset, offsetPixels)
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            topInset = insets.systemWindowInsetTop
            bottomInset = insets.systemWindowInsetBottom
            CoreHome.launcher!!.updateHomeLayout()
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
                        itemFromCoordinate.setX(to.x)
                        itemFromCoordinate.setY(to.y)
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
                        CoreHome.db.deleteItem(item, true)
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
            translateFactor = (if (Setup.appSettings().searchBarEnable) 20 else 40).toPx().toFloat()
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
            val desktopItems = CoreHome.db.desktop
            val pageData = desktopItems[page]
            for (i in pageData.indices) {
                val item = pageData[i]
                if (item.getX() == point.x && item.getY() == point.y && item.getSpanX() == 1 && item.getSpanY() == 1) {
                    return pageData[i]
                }
            }
            return null
        }

        fun handleOnDropOver(home: CoreHome?, dropItem: Item?, item: Item?, itemView: View, parent: ViewGroup, page: Int, itemPosition: Definitions.ItemPosition, callback: DesktopCallBack<*>): Boolean {
            if (item == null || dropItem == null) {
                return false
            }

            when (item.getType()) {
                Item.Type.APP, Item.Type.SHORTCUT -> if (dropItem.getType() == Item.Type.APP || dropItem.getType() == Item.Type.SHORTCUT) {
                    parent.removeView(itemView)

                    // create a new group item
                    val group = Item.newGroupItem()
                    group.groupItems.add(item)
                    group.groupItems.add(dropItem)
                    group.setX(item.getX())
                    group.setY(item.getY())

                    // add the drop item just in case it is coming from the app drawer
                    CoreHome.db.saveItem(dropItem, page, itemPosition)

                    // hide the apps added to the group
                    CoreHome.db.saveItem(item, Definitions.ItemState.Hidden)
                    CoreHome.db.saveItem(dropItem, Definitions.ItemState.Hidden)

                    // add the item to the database
                    CoreHome.db.saveItem(group, page, itemPosition)

                    callback.addItemToPage(group, page)

                    CoreHome.launcher?.getDesktop()?.consumeRevert()
                    CoreHome.launcher?.getDock()?.consumeRevert()
                    return true
                }
                Item.Type.GROUP -> if ((dropItem.getType() == Item.Type.APP || dropItem.getType() == Item.Type.SHORTCUT) && item.groupItems.size < GroupPopupView.GroupDef.maxItem) {
                    parent.removeView(itemView)

                    item.groupItems.add(dropItem)

                    // add the drop item just in case it is coming from the app drawer
                    CoreHome.db.saveItem(dropItem, page, itemPosition)

                    // hide the new app in the group
                    CoreHome.db.saveItem(dropItem, Definitions.ItemState.Hidden)

                    // add the item to the database
                    CoreHome.db.saveItem(item, page, itemPosition)

                    callback.addItemToPage(item, page)

                    CoreHome.launcher?.getDesktop()?.consumeRevert()
                    CoreHome.launcher?.getDock()?.consumeRevert()
                    return true
                }
                else -> {
                }
            }
            return false
        }
    }
}
