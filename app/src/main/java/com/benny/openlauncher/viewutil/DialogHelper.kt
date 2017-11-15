package com.benny.openlauncher.viewutil

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.support.v7.widget.LinearLayoutManager
import android.view.DragEvent
import android.view.Gravity
import android.widget.Toast

import com.afollestad.materialdialogs.MaterialDialog
import com.benny.openlauncher.R
import com.benny.openlauncher.core.model.IconLabelItem
import com.benny.openlauncher.core.model.Item
import com.benny.openlauncher.core.util.DragNDropHandler
import com.benny.openlauncher.core.util.Tool
import com.benny.openlauncher.util.AppManager
import com.benny.openlauncher.util.LauncherAction
import com.benny.openlauncher.util.copy
import com.benny.openlauncher.util.getStartAppIntent
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter

import net.qiujuer.genius.blur.StackBlur

import java.io.File
import java.util.ArrayList

object DialogHelper {
    fun editItemDialog(title: String, defaultText: String, c: Context, listener: OnItemEditListener) {
        val builder = MaterialDialog.Builder(c)
        builder.title(title)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .input(null, defaultText) { dialog, input -> listener.itemLabel(input.toString()) }.show()
    }

    fun alertDialog(context: Context, title: String, msg: String) {
        val builder = MaterialDialog.Builder(context)
        builder.title(title)
                .content(msg)
                .negativeText(R.string.cancel)
                .positiveText(R.string.ok)
                .show()
    }

    fun alertDialog(context: Context, title: String, msg: String, onPositive: MaterialDialog.SingleButtonCallback) {
        val builder = MaterialDialog.Builder(context)
        builder.title(title)
                .onPositive(onPositive)
                .content(msg)
                .negativeText(R.string.cancel)
                .positiveText(R.string.ok)
                .show()
    }

    fun alertDialog(context: Context, title: String, msg: String, positive: String, onPositive: MaterialDialog.SingleButtonCallback) {
        val builder = MaterialDialog.Builder(context)
        builder.title(title)
                .onPositive(onPositive)
                .content(msg)
                .negativeText(R.string.cancel)
                .positiveText(positive)
                .show()
    }

    fun addActionItemDialog(context: Context, callback: MaterialDialog.ListCallback) {
        val builder = MaterialDialog.Builder(context)
        builder.title(R.string.desktop_action)
                .items(R.array.entries__desktop_actions)
                .itemsCallback(callback)
                .show()
    }

    fun selectAppDialog(context: Context, onAppSelectedListener: OnAppSelectedListener?) {
        val builder = MaterialDialog.Builder(context)
        val fastItemAdapter = FastItemAdapter<IconLabelItem>()
        builder.title(R.string.select_app)
                .adapter(fastItemAdapter, LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false))
                .negativeText(R.string.cancel)

        val dialog = builder.build()
        val items = ArrayList<IconLabelItem>()
        val apps = AppManager.getInstance(context).apps
        val size = Tool.dp2px(18, context)
        val sizePad = Tool.dp2px(8, context)
        for (i in apps.indices) {
            items.add(IconLabelItem(context, apps[i].iconProvider, apps[i].label, size)
                    .withIconGravity(Gravity.START)
                    .withDrawablePadding(context, sizePad))
        }
        fastItemAdapter.set(items)
        fastItemAdapter.withOnClickListener { v, adapter, item, position ->
            onAppSelectedListener?.onAppSelected(apps[position])
            dialog.dismiss()
            true
        }
        dialog.show()
    }

    fun selectActionDialog(context: Context, title: String, selected: LauncherAction.ActionItem, onActionSelectedListener: OnActionSelectedListener?) {
        MaterialDialog.Builder(context)
                .title(title)
                .negativeText(R.string.cancel)
                .items(R.array.entries__gestures)
                .itemsCallbackSingleChoice(LauncherAction.getActionItemIndex(selected) + 1) { dialog, itemView, which, text ->
                    var item: LauncherAction.ActionItem? = null
                    if (which > 0) {
                        item = LauncherAction.getActionItem(which - 1)
                        if (item != null && item.action == LauncherAction.Action.LaunchApp) {
                            val finalItem = item
                            selectAppDialog(context, object : OnAppSelectedListener {
                                override fun onAppSelected(app: AppManager.App) {
                                    finalItem.extraData = Tool.getStartAppIntent(app)
                                    onActionSelectedListener!!.onActionSelected(finalItem)
                                }
                            })
                        } else onActionSelectedListener?.onActionSelected(item)
                    }
                    true
                }.show()
    }

    fun setWallpaperDialog(context: Context) {
        MaterialDialog.Builder(context)
                .title(R.string.wallpaper)
                .iconRes(R.drawable.ic_photo_black_24dp)
                .items(R.array.entries__wallpaper_options)
                .itemsCallback { dialog, itemView, position, text ->
                    when (position) {
                        0 -> {
                            val intent = Intent(Intent.ACTION_SET_WALLPAPER)
                            context.startActivity(Intent.createChooser(intent, context.getString(R.string.wallpaper_pick)))
                        }
                        1 -> try {
                            WallpaperManager.getInstance(context).setBitmap(StackBlur.blur(Tool.drawableToBitmap(context.wallpaper), 10, false))
                        } catch (e: Exception) {
                            Tool.toast(context, context.getString(R.string.wallpaper_unable_to_blur))
                        }

                    }
                }.show()
    }

    fun deletePackageDialog(context: Context, item: Item) {
        if (item.type == Item.Type.APP) {
            try {
                val packageURI = Uri.parse("package:" + item.intent?.component!!.packageName)
                val uninstallIntent = Intent(Intent.ACTION_DELETE, packageURI)
                context.startActivity(uninstallIntent)
            } catch (e: Exception) {

            }
        }
    }

    fun backupDialog(context: Context) {
        MaterialDialog.Builder(context)
                .title(R.string.pref_title__backup)
                .positiveText(R.string.cancel)
                .items(R.array.entries__backup_options)
                .itemsCallback { dialog, itemView, item, text ->
                    val m = context.packageManager
                    var s = context.packageName

                    if (context.resources.getStringArray(R.array.entries__backup_options)[item] == context.resources.getString(R.string.dialog__backup_app_settings__backup)) {
                        val directory = File(Environment.getExternalStorageDirectory().toString() + "/OpenLauncher/")
                        if (!directory.exists()) {
                            directory.mkdir()
                        }
                        try {
                            val p = m.getPackageInfo(s, 0)
                            s = p.applicationInfo.dataDir
                            Tool.copy(context, s + "/databases/home.db", directory.toString() + "/home.db")
                            Tool.copy(context, s + "/shared_prefs/app.xml", directory.toString() + "/app.xml")
                            Toast.makeText(context, R.string.dialog__backup_app_settings__success, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, R.string.dialog__backup_app_settings__error, Toast.LENGTH_SHORT).show()
                        }

                    }
                    if (context.resources.getStringArray(R.array.entries__backup_options)[item] == context.resources.getString(R.string.dialog__backup_app_settings__restore)) {
                        val directory = File(Environment.getExternalStorageDirectory().toString() + "/OpenLauncher/")
                        try {
                            val p = m.getPackageInfo(s, 0)
                            s = p.applicationInfo.dataDir
                            Tool.copy(context, directory.toString() + "/home.db", s + "/databases/home.db")
                            Tool.copy(context, directory.toString() + "/app.xml", s + "/shared_prefs/app.xml")
                            Toast.makeText(context, R.string.dialog__backup_app_settings__success, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, R.string.dialog__backup_app_settings__error, Toast.LENGTH_SHORT).show()
                        }

                        System.exit(1)
                    }
                }.show()
    }

    interface OnAppSelectedListener {
        fun onAppSelected(app: AppManager.App)
    }

    interface OnActionSelectedListener {
        fun onActionSelected(item: LauncherAction.ActionItem?)
    }

    interface OnItemEditListener {
        fun itemLabel(label: String)
    }
}
