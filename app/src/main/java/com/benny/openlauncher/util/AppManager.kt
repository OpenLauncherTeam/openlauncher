package com.benny.openlauncher.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.AsyncTask
import android.support.v4.app.ActivityCompat
import android.view.Gravity
import com.afollestad.materialdialogs.MaterialDialog
import com.benny.openlauncher.R
import com.benny.openlauncher.activity.Home
import com.benny.openlauncher.core.activity.CoreHome
import com.benny.openlauncher.core.interfaces.AbstractApp
import com.benny.openlauncher.core.interfaces.AppDeleteListener
import com.benny.openlauncher.core.interfaces.AppUpdateListener
import com.benny.openlauncher.core.manager.Setup
import com.benny.openlauncher.core.model.IconLabelItem
import com.benny.openlauncher.core.model.Item
import com.benny.openlauncher.core.util.BaseIconProvider
import com.benny.openlauncher.core.util.Tool
import com.benny.openlauncher.core.util.toPx
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import java.text.Collator
import java.util.*

class AppManager(val context: Context) : Setup.AppLoader<AppManager.App> {

    val packageManager: PackageManager = context.packageManager
    val apps = ArrayList<App>()
    val nonFilteredApps = ArrayList<App>()
    private val updateListeners: MutableList<AppUpdateListener<App>> = ArrayList()
    private val deleteListeners: MutableList<AppDeleteListener<App>> = ArrayList()
    var recreateAfterGettingApps: Boolean = false

    private var task: AsyncTask<*, *, *>? = null

    fun findApp(intent: Intent?): App? {
        if (intent == null || intent.component == null) return null

        val packageName = intent.component!!.packageName
        val className = intent.component!!.className
        for (app in apps) {
            if (app.className == className && app.packageName == packageName) {
                return app
            }
        }
        return null
    }

    fun getApps(): List<App> = apps

    fun getNonFilteredApps(): List<App> = nonFilteredApps

    fun clearListener() {
        updateListeners.clear()
        deleteListeners.clear()
    }

    fun init() {
        getAllApps()
    }

    private fun getAllApps() {
        if (task == null || task!!.status == AsyncTask.Status.FINISHED)
            task = AsyncGetApps().execute()
        else if (task!!.status == AsyncTask.Status.RUNNING) {
            task!!.cancel(false)
            task = AsyncGetApps().execute()
        }
    }

    fun startPickIconPackIntent(activity: Activity) {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory("com.anddoes.launcher.THEME")

        val fastItemAdapter = FastItemAdapter<IconLabelItem>()

        val resolveInfos = packageManager.queryIntentActivities(intent, 0)
        Collections.sort(resolveInfos, ResolveInfo.DisplayNameComparator(packageManager))
        val d = MaterialDialog.Builder(activity)
                .adapter(fastItemAdapter, null)
                .title(activity.getString(R.string.dialog__icon_pack_title))
                .build()

        fastItemAdapter.add(IconLabelItem(activity, R.drawable.ic_launcher, R.string.label_default, -1)
                .withDrawablePadding(8.toPx().toFloat())
                .withIconGravity(Gravity.START)
                .withOnClickListener {
                    recreateAfterGettingApps = true
                    AppSettings.get().iconPack = ""
                    getAllApps()
                    d.dismiss()
                })

        for (i in resolveInfos.indices) {
            fastItemAdapter.add(IconLabelItem(activity, resolveInfos[i].loadIcon(packageManager), " " + resolveInfos[i].loadLabel(packageManager).toString(), -1)
                    .withIconGravity(Gravity.START)
                    .withOnClickListener {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            recreateAfterGettingApps = true
                            AppSettings.get().iconPack = resolveInfos[i].activityInfo.packageName
                            getAllApps()
                            d.dismiss()
                        } else {
                            Tool.toast(context, activity.getString(R.string.dialog__icon_pack_info_toast))
                            ActivityCompat.requestPermissions(Home.launcher!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), CoreHome.REQUEST_PERMISSION_STORAGE)
                        }
                    })
        }
        d.show()
    }

    fun onReceive(p1: Context, p2: Intent) {
        getAllApps()
    }

    // -----------------------
    // AppLoader interface
    // -----------------------

    override fun loadItems() {
        getAllApps()
    }

    override fun getAllApps(context: Context, includeHidden: Boolean): List<App> {
        return if (includeHidden) getNonFilteredApps() else getApps()
    }

    override fun findItemApp(item: Item): App? {
        return findApp(item.intent)
    }

    override fun createApp(intent: Intent): App? {
        try {
            val info = packageManager.resolveActivity(intent, 0)
            val app = App(context, info, packageManager)
            if (apps != null && !apps.contains(app))
                apps.add(app)
            return app
        } catch (e: Exception) {
            return null
        }

    }

    override fun onAppUpdated(p1: Context, p2: Intent) {
        onReceive(p1, p2)
    }

    override fun notifyUpdateListeners(apps: List<App>) {
        val iter = updateListeners.iterator()
        while (iter.hasNext()) {
            if (iter.next().onAppUpdated(apps)) {
                iter.remove()
            }
        }
    }

    override fun notifyRemoveListeners(apps: List<App>) {
        val iter = deleteListeners.iterator()
        while (iter.hasNext()) {
            if (iter.next().onAppDeleted(apps)) {
                iter.remove()
            }
        }
    }

    override fun addUpdateListener(updateListener: AppUpdateListener<AppManager.App>) {
        updateListeners.add(updateListener)
    }

    override fun removeUpdateListener(updateListener: AppUpdateListener<AppManager.App>) {
        updateListeners.remove(updateListener)
    }

    override fun addDeleteListener(deleteListener: AppDeleteListener<AppManager.App>) {
        deleteListeners.add(deleteListener)
    }

    override fun removeDeleteListener(deleteListener: AppDeleteListener<AppManager.App>) {
        deleteListeners.remove(deleteListener)
    }

    private inner class AsyncGetApps : AsyncTask<Void, Void, Void>() {

        private var tempApps: List<App>? = null

        override fun onPreExecute() {
            tempApps = ArrayList(apps)
            super.onPreExecute()
        }

        override fun onCancelled() {
            tempApps = null
            super.onCancelled()
        }

        protected override fun doInBackground(vararg p0: Void?): Void? {
            apps.clear()
            nonFilteredApps.clear()

            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val activitiesInfo = packageManager.queryIntentActivities(intent, 0)
            Collections.sort(activitiesInfo) { p1, p2 -> Collator.getInstance().compare(p1.loadLabel(packageManager).toString(), p2.loadLabel(packageManager).toString()) }

            for (info in activitiesInfo) {
                val app = App(context, info, packageManager)
                nonFilteredApps.add(app)
            }

            val hiddenList = AppSettings.get().hiddenAppsList
            if (hiddenList != null) {
                for (i in nonFilteredApps.indices) {
                    var shouldGetAway = false
                    for (hidItemRaw in hiddenList) {
                        if (nonFilteredApps[i].packageName + "/" + nonFilteredApps[i].className == hidItemRaw) {
                            shouldGetAway = true
                            break
                        }
                    }
                    if (!shouldGetAway) {
                        apps.add(nonFilteredApps[i])
                    }
                }
            } else {
                for (info in activitiesInfo)
                    apps.add(App(context, info, packageManager))
            }

            val appSettings = AppSettings.get()
            if (!appSettings.iconPack.isEmpty() && Tool.isPackageInstalled(appSettings.iconPack, packageManager)) {
                IconPackHelper.themePacs(this@AppManager, Tool.dp2px(appSettings.iconSize, context), appSettings.iconPack, apps)
            }
            return null
        }

        override fun onPostExecute(result: Void?) {

            notifyUpdateListeners(apps)

            val removed = Tool.getRemovedApps(tempApps!!, apps)
            if (removed.size > 0) {
                notifyRemoveListeners(removed)
            }

            if (recreateAfterGettingApps) {
                recreateAfterGettingApps = false
                (context as? Home)?.recreate()
            }

            super.onPostExecute(result)
        }
    }

    data class App(
            val context: Context,
            var info: ResolveInfo,
            val packageManager: PackageManager,
            override var label: String = info.loadLabel(packageManager).toString(),
            override var packageName: String = info.activityInfo.packageName,
            override var className: String = info.activityInfo.name,
            override var iconProvider: BaseIconProvider = Setup.imageLoader().createIconProvider(info.loadIcon(packageManager))
    ) : AbstractApp {

        override fun hashCode(): Int {
            var result = context.hashCode()
            result = 31 * result + info.hashCode()
            result = 31 * result + packageManager.hashCode()
            result = 31 * result + label.hashCode()
            result = 31 * result + packageName.hashCode()
            result = 31 * result + className.hashCode()
            result = 31 * result + iconProvider.hashCode()
            return result
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as App

            if (context != other.context) return false
            if (info != other.info) return false
            if (packageManager != other.packageManager) return false
            if (label != other.label) return false
            if (packageName != other.packageName) return false
            if (className != other.className) return false
            if (iconProvider != other.iconProvider) return false

            return true
        }
    }

    abstract class AppUpdatedListener : AppUpdateListener<App> {
        private val listenerID: String = UUID.randomUUID().toString()

        override fun equals(other: Any?): Boolean = other is AppUpdatedListener && other.listenerID == this.listenerID
    }

    companion object {
        private var ref: AppManager? = null

        fun getInstance(context: Context): AppManager = if (ref == null) {
            ref = AppManager(context)
            ref!!
        } else
            ref!!
    }
}
