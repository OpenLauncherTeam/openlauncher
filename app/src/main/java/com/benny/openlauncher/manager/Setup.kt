package com.benny.openlauncher.manager

import android.content.Context
import android.graphics.drawable.Drawable
import com.benny.openlauncher.interfaces.DialogListener
import com.benny.openlauncher.model.Item
import com.benny.openlauncher.util.AppManager
import com.benny.openlauncher.util.AppSettings
import com.benny.openlauncher.util.BaseIconProvider
import com.benny.openlauncher.util.Definitions
import com.benny.openlauncher.viewutil.DesktopGestureListener
import com.benny.openlauncher.viewutil.ItemGestureListener

abstract class Setup {

    // ----------------
    // Settings
    // ----------------

    abstract fun getAppContext(): Context

    abstract fun getAppSettings(): AppSettings

    abstract fun getDesktopGestureCallback(): DesktopGestureListener.DesktopGestureCallback

    abstract fun getItemGestureCallback(): ItemGestureListener.ItemGestureCallback

    abstract fun getImageLoader(): ImageLoader

    abstract fun getDataManager(): DataManager

    abstract fun getAppLoader(): AppManager

    abstract fun getEventHandler(): EventHandler

    abstract fun getLogger(): Logger

    // ----------------
    // Interfaces
    // ----------------

    interface ImageLoader {
        fun createIconProvider(icon: Int): BaseIconProvider

        fun createIconProvider(drawable: Drawable?): BaseIconProvider
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
        private var setup: Setup? = null

        @JvmStatic
        fun wasInitialised(): Boolean = setup != null

        @JvmStatic
        fun init(setup: Setup) {
            Setup.setup = setup
        }

        @JvmStatic
        fun get(): Setup {
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
        fun appSettings(): AppSettings = get().getAppSettings()

        @JvmStatic
        fun desktopGestureCallback(): DesktopGestureListener.DesktopGestureCallback = get().getDesktopGestureCallback()

        @JvmStatic
        fun itemGestureCallback(): ItemGestureListener.ItemGestureCallback = get().getItemGestureCallback()

        @JvmStatic
        fun imageLoader(): Setup.ImageLoader = get().getImageLoader() as Setup.ImageLoader

        @JvmStatic
        fun dataManager(): DataManager = get().getDataManager()

        @JvmStatic
        fun appLoader(): AppManager = get().getAppLoader()

        @JvmStatic
        fun eventHandler(): EventHandler = get().getEventHandler()

        @JvmStatic
        fun logger(): Logger = get().getLogger()
    }
}
