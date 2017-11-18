package com.benny.openlauncher.core.activity

import android.app.*
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Point
import android.graphics.PointF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.benny.openlauncher.core.R
import com.benny.openlauncher.core.interfaces.AbstractApp
import com.benny.openlauncher.core.interfaces.AppDeleteListener
import com.benny.openlauncher.core.interfaces.AppUpdateListener
import com.benny.openlauncher.core.interfaces.DialogListener
import com.benny.openlauncher.core.manager.Setup
import com.benny.openlauncher.core.model.Item
import com.benny.openlauncher.core.model.PopupIconLabelItem
import com.benny.openlauncher.core.util.*
import com.benny.openlauncher.core.viewutil.WidgetHost
import com.benny.openlauncher.core.widget.*
import com.mikepenz.fastadapter.listeners.OnClickListener
import kotlinx.android.synthetic.main.view_drawer_indicator.*
import kotlinx.android.synthetic.main.view_home.*

abstract class CoreHome : Activity(), Desktop.OnDesktopEditListener, DesktopOptionView.DesktopOptionViewListener {

    companion object {
        val REQUEST_PICK_APPWIDGET = 0x6475
        val REQUEST_CREATE_APPWIDGET = 0x3648
        val REQUEST_PERMISSION_STORAGE = 0x2678

        // static members, easier to access from any activity and class
        var launcher: CoreHome? = null
        lateinit var db: Setup.DataManager
        var appWidgetHost: WidgetHost? = null
        lateinit var appWidgetManager: AppWidgetManager

        // used for the drag shadow builder
        var itemTouchX = 0f
        var itemTouchY = 0f
        var consumeNextResume: Boolean = false

        private val timeChangesIntentFilter: IntentFilter = IntentFilter()
        private val appUpdateIntentFilter: IntentFilter = IntentFilter()
        private val shortcutIntentFilter: IntentFilter = IntentFilter()

        init {
            timeChangesIntentFilter.addAction(Intent.ACTION_TIME_TICK)
            timeChangesIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
            timeChangesIntentFilter.addAction(Intent.ACTION_TIME_CHANGED)

            appUpdateIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
            appUpdateIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
            appUpdateIntentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED)
            appUpdateIntentFilter.addDataScheme("package")

            shortcutIntentFilter.addAction("com.android.launcher.action.INSTALL_SHORTCUT")
        }
    }

    private val shortcutReceiver = ShortcutReceiver()
    private val appUpdateReceiver = AppUpdateReceiver()
    private var timeChangedReceiver: BroadcastReceiver? = null

    // region for the APP_DRAWER_ANIMATION
    private var cx: Int = 0
    private var cy: Int = 0
    private var rad: Int = 0

    fun getDesktop(): Desktop = desktop
    fun getDock(): Dock = dock
    fun getAppDrawerController(): AppDrawerController = appDrawerController
    fun getGroupPopup(): GroupPopupView = groupPopup
    fun getSearchBar(): SearchBar = searchBar
    fun getBackground(): View = background
    fun getDesktopIndicator(): PagerIndicator = desktopIndicator
    fun getDragNDropView(): DragNDropLayout = dragNDropView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Setup.wasInitialised())
            initStaticHelper()

        if (Setup.appSettings().isSearchBarTimeEnabled) {
            timeChangedReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val action = intent.action
                    if (action == Intent.ACTION_TIME_TICK) {
                        updateSearchClock()
                    }
                }
            }
        }
        launcher = this
        db = Setup.dataManager()

        setContentView(layoutInflater.inflate(R.layout.activity_home, null))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        init()
    }

    protected abstract fun initStaticHelper()

    private fun init() {
        appWidgetHost = WidgetHost(applicationContext, R.id.app_widget_host)
        appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetHost!!.startListening()

        initViews()

        initDragNDrop()

        registerBroadcastReceiver()

        // add all of the data for the desktop and dock
        initAppManager()

        initSettings()

        System.runFinalization()
        System.gc()
    }

    fun onUninstallItem(item: Item) {
        consumeNextResume = true
        Setup.eventHandler().showDeletePackageDialog(this, item)
    }

    fun onRemoveItem(item: Item) {
        when (item.locationInLauncher) {
            Item.LOCATION_DESKTOP -> {
                desktop.removeItem(desktop.currentPage.coordinateToChildView(Point(item.x, item.y))!!,true)
            }
            Item.LOCATION_DOCK -> {
                dock.removeItem(dock.coordinateToChildView(Point(item.x, item.y))!!,true)
            }
        }

        db.deleteItem(item, true)
    }

    fun onInfoItem(item: Item) {
        if (item.type === Item.Type.APP) {
            try {
                startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + item.intent!!.component!!.packageName)))
            } catch (e: Exception) {
                Tool.toast(this, R.string.toast_app_uninstalled)
            }
        }
    }

    fun onEditItem(item: Item) {
        Setup.eventHandler().showEditDialog(this, item, DialogListener.OnEditDialogListener { name ->
            item.label = name
            db.saveItem(item)

            when (item.locationInLauncher) {
                Item.LOCATION_DESKTOP -> {
                    desktop.removeItem(desktop.currentPage.coordinateToChildView(Point(item.x, item.y))!!,false)
                    desktop.addItemToCell(item, item.x, item.y)
                }
                Item.LOCATION_DOCK -> {
                    dock.removeItem(dock.coordinateToChildView(Point(item.x, item.y))!!,false)
                    dock.addItemToCell(item, item.x, item.y)
                }
            }
        })
    }

    protected open fun initDragNDrop() {
        //dragHandle's drag event
        val dragHandler = Handler()
        dragNDropView.registerDropTarget(object : DragNDropLayout.DropTargetListener(leftDragHandle) {

            val leftRunnable = object : Runnable {
                override fun run() {
                    if (getDesktop().currentItem > 0)
                        getDesktop().currentItem = getDesktop().currentItem - 1
                    else if (getDesktop().currentItem == 0)
                        getDesktop().addPageLeft(true)
                    dragHandler.postDelayed(this, 1000)
                }
            }

            override fun onStart(action: DragAction.Action, location: PointF, isInside: Boolean): Boolean =
                    when (action) {
                        DragAction.Action.APP,
                        DragAction.Action.WIDGET,
                        DragAction.Action.SEARCH_RESULT,
                        DragAction.Action.APP_DRAWER,
                        DragAction.Action.GROUP,
                        DragAction.Action.SHORTCUT,
                        DragAction.Action.ACTION -> {
                            true
                        }
                    }

            override fun onStartDrag(action: DragAction.Action, location: PointF) {
                if (leftDragHandle.alpha == 0f)
                    leftDragHandle.animate().alpha(0.5f)
            }

            override fun onEnter(action: DragAction.Action, location: PointF) {
                dragHandler.post(leftRunnable)
                leftDragHandle.animate().alpha(0.9f)
            }

            override fun onExit(action: DragAction.Action, location: PointF) {
                dragHandler.removeCallbacksAndMessages(null)
                leftDragHandle.animate().alpha(0.5f)
            }

            override fun onEnd() {
                dragHandler.removeCallbacksAndMessages(null)
                leftDragHandle.animate().alpha(0f)
            }
        })
        dragNDropView.registerDropTarget(object : DragNDropLayout.DropTargetListener(rightDragHandle) {

            val rightRunnable = object : Runnable {
                override fun run() {
                    if (getDesktop().currentItem < getDesktop().pageCount - 1)
                        getDesktop().currentItem = getDesktop().currentItem + 1
                    else if (getDesktop().currentItem == getDesktop().pageCount - 1)
                        getDesktop().addPageRight(true)
                    dragHandler.postDelayed(this, 1000)
                }
            }

            override fun onStart(action: DragAction.Action, location: PointF, isInside: Boolean): Boolean =
                    when (action) {
                        DragAction.Action.APP,
                        DragAction.Action.WIDGET,
                        DragAction.Action.SEARCH_RESULT,
                        DragAction.Action.APP_DRAWER,
                        DragAction.Action.GROUP,
                        DragAction.Action.SHORTCUT,
                        DragAction.Action.ACTION -> {
                            true
                        }
                    }

            override fun onStartDrag(action: DragAction.Action, location: PointF) {
                if (rightDragHandle.alpha == 0f)
                    rightDragHandle.animate().alpha(0.5f)
            }

            override fun onEnter(action: DragAction.Action, location: PointF) {
                dragHandler.post(rightRunnable)
                rightDragHandle.animate().alpha(0.9f)
            }

            override fun onExit(action: DragAction.Action, location: PointF) {
                dragHandler.removeCallbacksAndMessages(null)
                rightDragHandle.animate().alpha(0.5f)
            }

            override fun onEnd() {
                dragHandler.removeCallbacksAndMessages(null)
                rightDragHandle.animate().alpha(0f)
            }
        })

        val uninstallItemIdentifier = 83L
        val infoItemIdentifier = 84L
        val editItemIdentifier = 85L
        val removeItemIdentifier = 86L

        val uninstallItem = PopupIconLabelItem(R.string.uninstall, R.drawable.ic_delete_dark_24dp).withIdentifier(uninstallItemIdentifier)
        val infoItem = PopupIconLabelItem(R.string.info, R.drawable.ic_info_outline_dark_24dp).withIdentifier(infoItemIdentifier)
        val editItem = PopupIconLabelItem(R.string.edit, R.drawable.ic_edit_black_24dp).withIdentifier(editItemIdentifier)
        val removeItem = PopupIconLabelItem(R.string.remove, R.drawable.ic_close_dark_24dp).withIdentifier(removeItemIdentifier)

        fun showItemPopup() {
            val itemList = arrayListOf<PopupIconLabelItem>()
            when (dragNDropView.dragItem?.type) {
                Item.Type.APP, Item.Type.SHORTCUT, Item.Type.GROUP -> {
                    if (dragNDropView.dragAction == DragAction.Action.APP_DRAWER) {
                        itemList.add(uninstallItem)
                        itemList.add(infoItem)
                    } else {
                        itemList.add(editItem)
                        itemList.add(removeItem)
                        itemList.add(infoItem)
                    }
                }
                Item.Type.ACTION -> {
                    itemList.add(editItem)
                    itemList.add(removeItem)
                }
                Item.Type.WIDGET -> {
                    itemList.add(removeItem)
                }
            }

            var x = dragNDropView.dragLocation.x - CoreHome.itemTouchX + 10.toPx()
            var y = dragNDropView.dragLocation.y - CoreHome.itemTouchY - (46 * itemList.size).toPx()

            if ((x + 200.toPx()) > dragNDropView.width) {
                dragNDropView.setPopupMenuShowDirection(false)
                x = dragNDropView.dragLocation.x - CoreHome.itemTouchX + desktop.currentPage.cellWidth - 200.toPx().toFloat() - 10.toPx()
            } else {
                dragNDropView.setPopupMenuShowDirection(true)
            }

            if (y < 0)
                y = dragNDropView.dragLocation.y - CoreHome.itemTouchY + desktop.currentPage.cellHeight + 4.toPx()
            else
                y -= 4.toPx()

            dragNDropView.showPopupMenuForItem(x, y, itemList, OnClickListener { v, adapter, item, position ->
                when (item.identifier) {
                    uninstallItemIdentifier -> onUninstallItem(dragNDropView.dragItem!!)
                    editItemIdentifier -> onEditItem(dragNDropView.dragItem!!)
                    removeItemIdentifier -> onRemoveItem(dragNDropView.dragItem!!)
                    infoItemIdentifier -> onInfoItem(dragNDropView.dragItem!!)
                }
                dragNDropView.hidePopupMenu()
                true
            })
        }

        //desktop's drag event
        dragNDropView.registerDropTarget(object : DragNDropLayout.DropTargetListener(desktop) {
            override fun onStart(action: DragAction.Action, location: PointF, isInside: Boolean): Boolean {
                if (action != DragAction.Action.SEARCH_RESULT)
                    showItemPopup()
                return true
            }

            override fun onExit(action: DragAction.Action, location: PointF) {
                for (page in desktop.pages)
                    page.clearCachedOutlineBitmap()
                dragNDropView.cancelFolderPreview()
            }

            override fun onDrop(action: DragAction.Action, location: PointF, item: Item) {
                // this statement makes sure that adding an app multiple times from the app drawer works
                // the app will get a new id every time
                if (action == DragAction.Action.APP_DRAWER) {
                    if (appDrawerController.isOpen) return
                    item.reset()
                }

                val x = location.x.toInt()
                val y = location.y.toInt()
                if (desktop.addItemToPoint(item, x, y)) {
                    desktop.consumeRevert()
                    dock.consumeRevert()
                    // add the item to the database
                    CoreHome.db.saveItem(item, desktop.currentItem, Definitions.ItemPosition.Desktop)
                } else {
                    val pos = Point()
                    desktop.currentPage.touchPosToCoordinate(pos, x, y, item.spanX, item.spanY, false)
                    val itemView = desktop.currentPage.coordinateToChildView(pos)
                    if (itemView != null && Desktop.handleOnDropOver(this@CoreHome, item, itemView.tag as Item, itemView, desktop.currentPage, desktop.currentItem, Definitions.ItemPosition.Desktop, desktop)) {
                        desktop.consumeRevert()
                        dock.consumeRevert()
                    } else {
                        Tool.toast(this@CoreHome, R.string.toast_not_enough_space)
                        desktop.revertLastItem()
                        dock.revertLastItem()
                    }
                }
            }

            override fun onStartDrag(action: DragAction.Action, location: PointF) {
                closeAppDrawer()
            }

            override fun onEnd() {
                for (page in desktop.pages)
                    page.clearCachedOutlineBitmap()
            }

            override fun onMove(action: DragAction.Action, location: PointF) {
                if (action != DragAction.Action.SEARCH_RESULT && action != DragAction.Action.WIDGET)
                    desktop.updateIconProjection(location.x.toInt(), location.y.toInt())
            }
        })

        //dock's drag event
        dragNDropView.registerDropTarget(object : DragNDropLayout.DropTargetListener(dock) {
            override fun onStart(action: DragAction.Action, location: PointF, isInside: Boolean): Boolean {
                val ok = (action != DragAction.Action.WIDGET)

                if (ok && isInside) {
                    //showItemPopup()
                }

                return ok
            }

            override fun onDrop(action: DragAction.Action, location: PointF, item: Item) {
                if (action == DragAction.Action.APP_DRAWER) {
                    if (appDrawerController.isOpen) return
                    item.reset()
                }

                val x = location.x.toInt()
                val y = location.y.toInt()
                if (dock.addItemToPoint(item, x, y)) {
                    desktop.consumeRevert()
                    dock.consumeRevert()

                    // add the item to the database
                    CoreHome.db.saveItem(item, 0, Definitions.ItemPosition.Dock)
                } else {
                    val pos = Point()
                    dock.touchPosToCoordinate(pos, x, y, item.spanX, item.spanY, false)
                    val itemView = dock.coordinateToChildView(pos)
                    if (itemView != null) {
                        if (Desktop.handleOnDropOver(this@CoreHome, item, itemView.tag as Item, itemView, dock, 0, Definitions.ItemPosition.Dock, dock)) {
                            desktop.consumeRevert()
                            dock.consumeRevert()
                        } else {
                            Tool.toast(this@CoreHome, R.string.toast_not_enough_space)
                            desktop.revertLastItem()
                            dock.revertLastItem()
                        }
                    } else {
                        Tool.toast(this@CoreHome, R.string.toast_not_enough_space)
                        desktop.revertLastItem()
                        dock.revertLastItem()
                    }
                }
            }

            override fun onExit(action: DragAction.Action, location: PointF) {
                dock.clearCachedOutlineBitmap()
                dragNDropView.cancelFolderPreview()
            }

            override fun onEnd() {
                if (dragNDropView.dragAction == DragAction.Action.WIDGET)
                    desktop.revertLastItem()
                dock.clearCachedOutlineBitmap()
            }

            override fun onMove(action: DragAction.Action, location: PointF) {
                if (action != DragAction.Action.SEARCH_RESULT)
                    dock.updateIconProjection(location.x.toInt(), location.y.toInt())
            }
        })
    }

    // called to initialize the views
    protected open fun initViews() {
        initSearchBar()
        initDock()

        appDrawerController.init()

        appDrawerController.setHome(this)
        dragOptionPanel.setHome(this)

        desktop.init()
        desktop.desktopEditListener = this

        desktopEditOptionPanel.setDesktopOptionViewListener(this)
        desktopEditOptionPanel.updateLockIcon(Setup.appSettings().isDesktopLock)
        desktop.addOnPageChangeListener(object : SmoothViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                desktopEditOptionPanel.updateHomeIcon(Setup.appSettings().desktopPageCurrent == position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        desktop!!.setPageIndicator(desktopIndicator)

        dragOptionPanel.setAutoHideView(searchBar)

        appDrawerController!!.setCallBack(object : AppDrawerController.CallBack {
            override fun onStart() {
                Tool.visibleViews(appDrawerIndicator)
                Tool.invisibleViews(desktop)
                hideDesktopIndicator()
                updateDock(false)
                updateSearchBar(false)
            }

            override fun onEnd() {}
        }, object : AppDrawerController.CallBack {
            override fun onStart() {
                Tool.invisibleViews(appDrawerIndicator)
                Tool.visibleViews(desktop)
                showDesktopIndicator()
                if (Setup.appSettings().drawerStyle == AppDrawerController.DrawerMode.HORIZONTAL_PAGED)
                    updateDock(true, 200)
                else
                    updateDock(true)
                updateSearchBar(!dragOptionPanel.isDraggedFromDrawer)
                dragOptionPanel.isDraggedFromDrawer = false
            }

            override fun onEnd() {
                if (!Setup.appSettings().isDrawerRememberPosition) {
                    appDrawerController!!.scrollToStart()
                }
                appDrawerController!!.drawer.visibility = View.INVISIBLE
            }
        })
    }

    private fun getActivityAnimationOpts(view: View?): Bundle? {
        if (view == null) return null
        var opts: ActivityOptions? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var left = 0
            var top = 0
            var width = view.measuredWidth
            var height = view.measuredHeight
            if (view is AppItemView) {
                width = view.iconSize.toInt()
                left = view.drawIconLeft.toInt()
                top = view.drawIconTop.toInt()
            }
            opts = ActivityOptions.makeClipRevealAnimation(view, left, top, width, height)
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            opts = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.measuredWidth, view.measuredHeight)
        }
        return if (opts != null) opts.toBundle() else null
    }

    @JvmOverloads
    open fun onStartApp(context: Context, intent: Intent, view: View? = null) = try {
        context.startActivity(intent, getActivityAnimationOpts(view))

        CoreHome.consumeNextResume = true
    } catch (e: Exception) {
        Tool.toast(context, R.string.toast_app_uninstalled)
    }

    @JvmOverloads
    open fun onStartApp(context: Context, app: AbstractApp, view: View? = null) = try {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.setClassName(app.packageName, app.className)

        context.startActivity(intent, getActivityAnimationOpts(view))

        CoreHome.consumeNextResume = true
    } catch (e: Exception) {
        Tool.toast(context, R.string.toast_app_uninstalled)
    }

    protected open fun initAppManager() {
        Setup.appLoader().addUpdateListener(AppUpdateListener {
            if (desktop == null)
                return@AppUpdateListener false

            if (Setup.appSettings().desktopStyle != Desktop.DesktopMode.SHOW_ALL_APPS) {
                if (Setup.appSettings().isAppFirstLaunch) {
                    Setup.appSettings().isAppFirstLaunch = false

                    // create a new app drawer button
                    val appDrawerBtnItem = Item.newActionItem(Definitions.ACTION_LAUNCHER)

                    // center the button
                    appDrawerBtnItem.x = Definitions.DOCK_DEFAULT_CENTER_ITEM_INDEX_X
                    db.saveItem(appDrawerBtnItem, 0, Definitions.ItemPosition.Dock)
                }
            }
            if (Setup.appSettings().desktopStyle == Desktop.DesktopMode.NORMAL) {
                desktop.initDesktopNormal(this@CoreHome)
            } else if (Setup.appSettings().desktopStyle == Desktop.DesktopMode.SHOW_ALL_APPS) {
                desktop.initDesktopShowAll(this@CoreHome, this@CoreHome)
            }
            dock.initDockItem(this@CoreHome)

            // remove this listener
            true
        })
        Setup.appLoader().addDeleteListener(AppDeleteListener {
            if (Setup.appSettings().desktopStyle == Desktop.DesktopMode.NORMAL) {
                desktop.initDesktopNormal(this@CoreHome)
            } else if (Setup.appSettings().desktopStyle == Desktop.DesktopMode.SHOW_ALL_APPS) {
                desktop.initDesktopShowAll(this@CoreHome, this@CoreHome)
            }
            dock.initDockItem(this@CoreHome)
            setToHomePage()
            false
        })
    }

    override fun onDesktopEdit() {
        Tool.visibleViews(100, 20, desktopEditOptionPanel)

        hideDesktopIndicator()
        updateDock(false)
        updateSearchBar(false)
    }

    override fun onFinishDesktopEdit() {
        Tool.invisibleViews(100, 20, desktopEditOptionPanel)

        showDesktopIndicator()
        updateDock(true)
        updateSearchBar(true)
    }

    override fun onRemovePage() {
        desktop!!.removeCurrentPage()
    }

    override fun onSetPageAsHome() {
        Setup.appSettings().desktopPageCurrent = desktop!!.currentItem
    }

    override fun onLaunchSettings() {
        consumeNextResume = true
        Setup.eventHandler().showLauncherSettings(this)
    }

    override fun onPickDesktopAction() {
        Setup.eventHandler().showPickAction(this, DialogListener.OnAddAppDrawerItemListener {
            val pos = desktop!!.currentPage.findFreeSpace()
            if (pos != null)
                desktop!!.addItemToCell(Item.newActionItem(Definitions.ACTION_LAUNCHER), pos.x, pos.y)
            else
                Tool.toast(this@CoreHome, R.string.toast_not_enough_space)
        })
    }

    override fun onPickWidget() {
        pickWidget()
    }

    protected open fun initSettings() {
        updateHomeLayout()

        if (Setup.appSettings().isDesktopFullscreen) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        }

        desktop!!.setBackgroundColor(Setup.appSettings().desktopBackgroundColor)
        dock!!.setBackgroundColor(Setup.appSettings().dockColor)

        appDrawerController!!.setBackgroundColor(Setup.appSettings().drawerBackgroundColor)
        appDrawerController!!.background.alpha = 0
        appDrawerController!!.reloadDrawerCardTheme()

        when (Setup.appSettings().drawerStyle) {
            AppDrawerController.DrawerMode.HORIZONTAL_PAGED -> if (!Setup.appSettings().isDrawerShowIndicator) {
                appDrawerController!!.getChildAt(1).visibility = View.GONE
            }
            AppDrawerController.DrawerMode.VERTICAL -> {
            }
        }// handled in the AppDrawerVertical class
    }

    private fun initDock() {
        val iconSize = Setup.appSettings().dockIconSize
        dock!!.init()
        if (Setup.appSettings().isDockShowLabel) {
            dock!!.layoutParams.height = Tool.dp2px(16 + iconSize + 14 + 10, this) + Dock.bottomInset
        } else {
            dock!!.layoutParams.height = Tool.dp2px(16 + iconSize + 10, this) + Dock.bottomInset
        }
    }

    fun dimBackground() {
        Tool.visibleViews(background)
    }

    fun unDimBackground() {
        Tool.invisibleViews(background)
    }

    fun clearRoomForPopUp() {
        Tool.invisibleViews(desktop)
        hideDesktopIndicator()
        updateDock(false)
    }

    fun unClearRoomForPopUp() {
        Tool.visibleViews(desktop)
        showDesktopIndicator()
        updateDock(true)
    }

    private fun initSearchBar() {
        searchBar.setCallback(object : SearchBar.CallBack {
            override fun onInternetSearch(string: String) {
                val intent = Intent()

                if (Tool.isIntentActionAvailable(applicationContext, Intent.ACTION_WEB_SEARCH) && !Setup.appSettings().searchBarForceBrowser) {
                    intent.action = Intent.ACTION_WEB_SEARCH
                    intent.putExtra(SearchManager.QUERY, string)
                } else {
                    val baseUri = Setup.appSettings().searchBarBaseURI
                    val searchUri = if (baseUri.contains("{query}")) baseUri.replace("{query}", string) else baseUri + string

                    intent.action = Intent.ACTION_VIEW
                    intent.data = Uri.parse(searchUri)
                }

                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            override fun onExpand() {
                clearRoomForPopUp()
                dimBackground()

                searchBar.searchInput.isFocusable = true
                searchBar.searchInput.isFocusableInTouchMode = true
                searchBar.searchInput.post { searchBar.searchInput.requestFocus() }

                Tool.showKeyboard(this@CoreHome, searchBar.searchInput)
            }

            override fun onCollapse() {
                desktop.postDelayed({
                    unClearRoomForPopUp()
                }, 100)
                unDimBackground()

                searchBar.searchInput.clearFocus()

                Tool.hideKeyboard(this@CoreHome, searchBar.searchInput)
            }
        })
        searchBar.searchClock.setOnClickListener { calendarDropDownView.animateShow() }

        // this view is just a text view of the current date
        updateSearchClock()
    }

    @JvmOverloads
    fun updateDock(show: Boolean, delay: Long = 0) = if (Setup.appSettings().dockEnable && show) {
        Tool.visibleViews(100, delay, dock)
        (desktop!!.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = Tool.dp2px(4, this)
        (desktopIndicator!!.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = Tool.dp2px(4, this)
    } else {
        if (Setup.appSettings().dockEnable) {
            Tool.invisibleViews(100, dock)
        } else {
            Tool.goneViews(100, dock)
            (desktopIndicator!!.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = Desktop.bottomInset + Tool.dp2px(4, this)
            (desktop!!.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = Tool.dp2px(4, this)
        }
    }

    fun updateSearchBar(show: Boolean) = if (Setup.appSettings().searchBarEnable && show) {
        Tool.visibleViews(100, searchBar)
    } else {
        if (Setup.appSettings().searchBarEnable) {
            Tool.invisibleViews(100, searchBar)
        } else {
            Tool.goneViews(searchBar)
        }
    }

    fun updateDesktopIndicatorVisibility() = if (Setup.appSettings().isDesktopShowIndicator) {
        Tool.visibleViews(100, desktopIndicator)
    } else {
        Tool.goneViews(100, desktopIndicator)
    }

    fun hideDesktopIndicator() {
        if (Setup.appSettings().isDesktopShowIndicator)
            Tool.invisibleViews(100, desktopIndicator)
    }

    fun showDesktopIndicator() {
        if (Setup.appSettings().isDesktopShowIndicator)
            Tool.visibleViews(100, desktopIndicator)
    }

    private fun updateSearchClock() {
        if (searchBar!!.searchClock.text != null) {
            searchBar!!.updateClock()
        }
    }

    fun updateHomeLayout() {
        updateSearchBar(true)
        updateDock(true)

        updateDesktopIndicatorVisibility()

        if (!Setup.appSettings().searchBarEnable) {
            (leftDragHandle!!.layoutParams as ViewGroup.MarginLayoutParams).topMargin = Desktop.topInset
            (rightDragHandle!!.layoutParams as ViewGroup.MarginLayoutParams).topMargin = Desktop.topInset
            desktop!!.setPadding(0, Desktop.topInset, 0, 0)
        }

        if (!Setup.appSettings().dockEnable) {
            desktop!!.setPadding(0, 0, 0, Desktop.bottomInset)
        }
    }

    private fun registerBroadcastReceiver() {
        registerReceiver(appUpdateReceiver, appUpdateIntentFilter)
        if (timeChangedReceiver != null) {
            registerReceiver(timeChangedReceiver, timeChangesIntentFilter)
        }
        registerReceiver(shortcutReceiver, shortcutIntentFilter)
    }

    private fun pickWidget() {
        consumeNextResume = true
        val appWidgetId = appWidgetHost!!.allocateAppWidgetId()
        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET)
    }

    private fun configureWidget(data: Intent?) {
        val extras = data!!.extras
        val appWidgetId = extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
        if (appWidgetInfo.configure != null) {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
            intent.component = appWidgetInfo.configure
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET)
        } else {
            createWidget(data)
        }
    }

    private fun createWidget(data: Intent?) {
        val extras = data!!.extras
        val appWidgetId = extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
        val item = Item.newWidgetItem(appWidgetId)
        item.spanX = (appWidgetInfo.minWidth - 1) / desktop!!.pages[CoreHome.launcher!!.desktop!!.currentItem].cellWidth + 1
        item.spanY = (appWidgetInfo.minHeight - 1) / desktop!!.pages[CoreHome.launcher!!.desktop!!.currentItem].cellHeight + 1
        val point = desktop!!.currentPage.findFreeSpace(item.spanX, item.spanY)
        if (point != null) {
            item.x = point.x
            item.y = point.y

            // add item to database
            db.saveItem(item, desktop!!.currentItem, Definitions.ItemPosition.Desktop)
            desktop!!.addItemToPage(item, desktop!!.currentItem)
        } else {
            Tool.toast(this@CoreHome, R.string.toast_not_enough_space)
        }
    }

    override fun onDestroy() {
        appWidgetHost?.stopListening()
        appWidgetHost = null
        unregisterReceiver(appUpdateReceiver)
        if (timeChangedReceiver != null) {
            unregisterReceiver(timeChangedReceiver)
        }
        unregisterReceiver(shortcutReceiver)
        launcher = null

        super.onDestroy()
    }

    override fun onLowMemory() {
        System.runFinalization()
        System.gc()
        super.onLowMemory()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_APPWIDGET) {
                configureWidget(data)
            } else if (requestCode == REQUEST_CREATE_APPWIDGET) {
                createWidget(data)
            }
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {
            val appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (appWidgetId != -1) {
                appWidgetHost?.deleteAppWidgetId(appWidgetId)
            }
        }
    }

    override fun onStart() {
        launcher = this
        appWidgetHost?.startListening()
        super.onStart()
    }

    override fun onBackPressed() {
        handleLauncherPause()
    }

    override fun onResume() {
        if (Setup.appSettings().appRestartRequired) {
            Setup.appSettings().appRestartRequired = false

            val restartIntent = Intent(this, CoreHome::class.java)
            val restartIntentP = PendingIntent.getActivity(this, 123556, restartIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            val mgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, restartIntentP)
            System.exit(0)
            return
        }

        launcher = this
        appWidgetHost?.startListening()

        handleLauncherPause()
        super.onResume()
    }

    private fun handleLauncherPause() {
        if (consumeNextResume) {
            consumeNextResume = false
            return
        }

        onHandleLauncherPause()
    }

    protected open fun onHandleLauncherPause() {
        groupPopup.dismissPopup()
        calendarDropDownView.animateHide()
        dragNDropView.hidePopupMenu()
        if (searchBar.collapse()) return

        if (desktop != null) {
            if (!desktop.inEditMode) {
                if (appDrawerController.drawer.visibility == View.VISIBLE) {
                    closeAppDrawer()
                } else {
                    setToHomePage()
                }
            } else {
                desktop.pages[desktop.currentItem].performClick()
            }
        }
    }

    private fun setToHomePage() {
        desktop.currentItem = Setup.appSettings().desktopPageCurrent
    }

    @JvmOverloads
    fun openAppDrawer(view: View? = desktop, x: Int = -1, y: Int = -1) {
        if (!(x > 0 && y > 0)) {
            val pos = IntArray(2)
            view!!.getLocationInWindow(pos)
            cx = pos[0]
            cy = pos[1]

            cx += view.width / 2
            cy += view.height / 2
            if (view is AppItemView) {
                val appItemView = view as AppItemView?
                if (!appItemView!!.showLabel) {
                    cy -= Tool.dp2px(14, this) / 2
                }
                rad = (appItemView.iconSize / 2 - 4.toPx()).toInt()
            }
            cx -= (appDrawerController!!.drawer.layoutParams as ViewGroup.MarginLayoutParams).leftMargin
            cy -= (appDrawerController!!.drawer.layoutParams as ViewGroup.MarginLayoutParams).topMargin
            cy -= appDrawerController!!.paddingTop
        } else {
            cx = x
            cy = y
            rad = 0
        }
        val finalRadius = Math.max(appDrawerController!!.drawer.width, appDrawerController!!.drawer.height)
        appDrawerController!!.open(cx, cy, rad, finalRadius)
    }

    fun closeAppDrawer() {
        val finalRadius = Math.max(appDrawerController!!.drawer.width, appDrawerController!!.drawer.height)
        appDrawerController!!.close(cx, cy, rad, finalRadius)
    }


}
