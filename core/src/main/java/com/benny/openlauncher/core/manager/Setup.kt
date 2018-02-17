package com.benny.openlauncher.core.manager

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import com.benny.openlauncher.core.interfaces.*
import com.benny.openlauncher.core.model.Item
import com.benny.openlauncher.core.util.BaseIconProvider
import com.benny.openlauncher.core.util.Definitions
import com.benny.openlauncher.core.viewutil.DesktopGestureListener
import com.benny.openlauncher.core.viewutil.ItemGestureListener

abstract class Setup<A : AbstractApp> {

    // ----------------
    // Settings
    // ----------------

    abstract fun getAppContext(): Context

    abstract fun getAppSettings(): SettingsManager

    abstract fun getDesktopGestureCallback(): DesktopGestureListener.DesktopGestureCallback

    abstract fun getItemGestureCallback(): ItemGestureListener.ItemGestureCallback

    abstract fun getImageLoader(): ImageLoader<A>

    abstract fun getDataManager(): DataManager

    abstract fun getAppLoader(): AppLoader<A>

    abstract fun getEventHandler(): EventHandler

    abstract fun getLogger(): Logger

    // ----------------
    // Interfaces
    // ----------------

    interface ImageLoader<A : AbstractApp> {
        fun createIconProvider(drawable: Drawable?): BaseIconProvider

        fun createIconProvider(icon: Int): BaseIconProvider
    }

    interface DataManager {

        val desktop: List<List<Item>>

        val dock: List<Item>

        fun saveItem(item: Item)

        fun saveItem(item: Item, state: Definitions.ItemState)

        fun saveItem(item: Item, page: Int, desktop: Definitions.ItemPosition)

        fun deleteItem(item: Item, deleteSubItems: Boolean)

        fun getItem(id: Int): Item
    }

    interface AppLoader<A : AbstractApp> {
        fun loadItems()

        fun getAllApps(context: Context, includeHidden: Boolean): List<A>

        fun createApp(intent: Intent): A?

        fun findItemApp(item: Item): A?

        fun onAppUpdated(p1: Context, p2: Intent)

        fun addUpdateListener(updateListener: AppUpdateListener<A>)

        fun removeUpdateListener(updateListener: AppUpdateListener<A>)

        fun addDeleteListener(deleteListener: AppDeleteListener<A>)

        fun removeDeleteListener(deleteListener: AppDeleteListener<A>)

        fun notifyUpdateListeners(apps: List<A>)

        fun notifyRemoveListeners(apps: List<A>)
    }

    interface EventHandler {
        fun showLauncherSettings(context: Context)

        fun showPickAction(context: Context, listener: DialogListener.OnAddAppDrawerItemListener)

        fun showEditDialog(context: Context, item: Item, listener: DialogListener.OnEditDialogListener)

        fun showDeletePackageDialog(context: Context, item: Item)
    }

    interface Logger {
        fun log(source: Any, priority: Int, tag: String?, msg: String, vararg args: Any)
    }

    companion object {

        @JvmStatic
        private var setup: Setup<*>? = null

        @JvmStatic
        fun wasInitialised(): Boolean = setup != null

        @JvmStatic
        fun init(setup: Setup<*>) {
            Setup.setup = setup
        }

        @JvmStatic
        fun get(): Setup<*> {
            if (setup == null) {
                throw RuntimeException("Setup has not been initialised!")
            }
            return setup!!
        }

        // ----------------
        // Methods for convenience and shorter code
        // ----------------

        @JvmStatic
        fun appContext(): Context = get().getAppContext()

        @JvmStatic
        fun appSettings(): SettingsManager = get().getAppSettings()

        @JvmStatic
        fun desktopGestureCallback(): DesktopGestureListener.DesktopGestureCallback = get().getDesktopGestureCallback()

        @JvmStatic
        fun itemGestureCallback(): ItemGestureListener.ItemGestureCallback = get().getItemGestureCallback()

        @JvmStatic
        fun imageLoader(): Setup.ImageLoader<AbstractApp> = get().getImageLoader() as Setup.ImageLoader<AbstractApp>

        @JvmStatic
        fun dataManager(): DataManager = get().getDataManager()

        @JvmStatic
        fun appLoader(): Setup.AppLoader<AbstractApp> = get().getAppLoader() as Setup.AppLoader<AbstractApp>

        @JvmStatic
        fun eventHandler(): EventHandler = get().getEventHandler()

        @JvmStatic
        fun logger(): Logger = get().getLogger()
    }
}
