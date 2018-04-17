package com.benny.openlauncher.core.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.benny.openlauncher.core.activity.CoreHome
import com.benny.openlauncher.core.interfaces.AbstractApp
import com.benny.openlauncher.core.interfaces.IconProvider
import com.benny.openlauncher.core.manager.Setup
import com.benny.openlauncher.core.model.Item
import com.benny.openlauncher.core.util.Tool.dp2Px
import com.benny.openlauncher.core.viewutil.ItemGestureListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URISyntaxException
import java.util.*

fun Int.toPx(): Int = dp2Px(this)

object Tool {

    @JvmStatic
    fun hideKeyboard(context: Context, view: View) {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    @JvmStatic
    fun showKeyboard(context: Context, view: View) {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInputFromWindow(view.windowToken, InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    @JvmStatic
    fun visibleViews(vararg views: View?) {
        for (view in views) {
            visibleViews(200, 0, *views)
        }
    }

    @JvmStatic
    fun visibleViews(duration: Long, vararg views: View?) {
        visibleViews(duration, 0, *views)
    }

    @JvmStatic
    fun visibleViews(duration: Long, delay: Long, vararg views: View?) {
        for (view in views) {
            if (view == null) continue
            view.visibility = View.VISIBLE
            view.animate().alpha(1f).setStartDelay(delay).setDuration(duration).interpolator = AccelerateDecelerateInterpolator()
        }
    }

    @JvmStatic
    fun invisibleViews(vararg views: View?) {
        for (view in views) {
            if (view == null) continue
            view.animate().alpha(0f).setStartDelay(0).setDuration(200).setInterpolator(AccelerateDecelerateInterpolator()).withEndAction { view.visibility = View.INVISIBLE }
        }
    }

    fun invisibleViews(duration: Long, vararg views: View?) {
        for (view in views) {
            if (view == null) continue
            view.animate().alpha(0f).setStartDelay(0).setDuration(duration).setInterpolator(AccelerateDecelerateInterpolator()).withEndAction { view.visibility = View.INVISIBLE }
        }
    }

    fun invisibleViews(duration: Long, delay: Long, vararg views: View?) {
        for (view in views) {
            if (view == null) continue
            view.animate().alpha(0f).setStartDelay(delay).setDuration(duration).setInterpolator(AccelerateDecelerateInterpolator()).withEndAction { view.visibility = View.INVISIBLE }
        }
    }

    @JvmStatic
    fun goneViews(vararg views: View?) {
        for (view in views) {
            if (view == null) continue
            view.animate().alpha(0f).setStartDelay(0).setDuration(200).setInterpolator(AccelerateDecelerateInterpolator()).withEndAction { view.visibility = View.GONE }
        }
    }

    @JvmStatic
    fun goneViews(duration: Long, vararg views: View?) {
        for (view in views) {
            if (view == null) continue
            view.animate().alpha(0f).setStartDelay(0).setDuration(duration).setInterpolator(AccelerateDecelerateInterpolator()).withEndAction { view.visibility = View.GONE }
        }
    }

    @JvmStatic
    fun createScaleInScaleOutAnim(view: View, endAction: Runnable, runActionAtPercent: Float = 1.0f) {
        val animTime = (Setup.appSettings().overallAnimationSpeedModifier * 200).toLong()
        view.animate().scaleX(0.85f).scaleY(0.85f).setDuration(animTime).interpolator = AccelerateDecelerateInterpolator()
        Handler().postDelayed({
            view.animate().scaleX(1f).scaleY(1f).setDuration(animTime).interpolator = AccelerateDecelerateInterpolator()
            Handler().postDelayed({ endAction.run() }, animTime)
        }, (animTime * runActionAtPercent).toLong())
    }

    @JvmStatic
    fun toast(context: Context, str: String) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
    }

    @JvmStatic
    fun toast(context: Context, str: Int) {
        Toast.makeText(context, context.resources.getString(str), Toast.LENGTH_SHORT).show()
    }

    @JvmStatic
    fun dp2px(dp: Float, context: Context): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)

    @JvmStatic
    fun dp2px(dp: Int, context: Context): Int = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics))

    @JvmStatic
    fun dp2Px(dp: Int): Int = (dp * Resources.getSystem().displayMetrics.density).toInt()

    @JvmStatic
    fun pxToDp(px: Int): Int = (px / Resources.getSystem().displayMetrics.density).toInt()

    @JvmStatic
    fun sp2px(context: Context, spValue: Float): Int = (spValue * context.resources.displayMetrics.scaledDensity + 0.5f).toInt()

    @JvmStatic
    fun clampInt(target: Int, min: Int, max: Int): Int = Math.max(min, Math.min(max, target))

    @JvmStatic
    fun clampFloat(target: Float, min: Float, max: Float): Float = Math.max(min, Math.min(max, target))

    @JvmStatic
    fun startApp(context: Context, app: AbstractApp) {
        CoreHome.launcher?.onStartApp(context, app)
    }

    @JvmStatic
    fun startApp(context: Context, app: AbstractApp, view: View?) {
        CoreHome.launcher?.onStartApp(context, app, view)
    }

    @JvmStatic
    fun startApp(context: Context, intent: Intent) {
        CoreHome.launcher?.onStartApp(context, intent)
    }

    @JvmStatic
    fun startApp(context: Context, intent: Intent, view: View?) {
        CoreHome.launcher?.onStartApp(context, intent, view)
    }

    @JvmStatic
    fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }

        if (drawable is BitmapDrawable) {
            val bitmapDrawable = drawable as BitmapDrawable?
            if (bitmapDrawable!!.bitmap != null) {
                return bitmapDrawable.bitmap
            }
        }

        val bitmap: Bitmap
        if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            // single color bitmap will be created of 1x1 pixel
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        } else {
            bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)

        return bitmap
    }

    @JvmStatic
    fun loadBitmapFromView(v: View): Bitmap {
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        v.layout(0, 0, v.layoutParams.width, v.layoutParams.height)
        v.draw(c)
        return b
    }

    @JvmStatic
    fun convertPoint(fromPoint: Point, fromView: View, toView: View): Point {
        val fromCoord = IntArray(2)
        val toCoord = IntArray(2)
        fromView.getLocationOnScreen(fromCoord)
        toView.getLocationOnScreen(toCoord)

        return Point(fromCoord[0] - toCoord[0] + fromPoint.x,
                fromCoord[1] - toCoord[1] + fromPoint.y)
    }

    @JvmStatic
    fun vibrate(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    @JvmStatic
    fun print(o: Any?) {
        if (o != null)
            Log.e("Hey", o.toString())
    }

    @JvmStatic
    fun print(vararg o: Any) {
        val sb = StringBuilder()
        for (i in o.indices) {
            sb.append(o[i].toString()).append("  ")
        }
        Log.e("Hey", sb.toString())
    }

    @JvmStatic
    fun isIntentActionAvailable(context: Context, action: String): Boolean {
        val packageManager = context.packageManager
        val intent = Intent(action)
        val resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo.size > 0
    }

    @JvmStatic
    fun getIntentAsString(intent: Intent?): String {
        return if (intent == null) {
            ""
        } else {
            intent.toUri(0)
        }
    }

    @JvmStatic
    fun getIntentFromString(string: String?): Intent {
        return if (string == null || string.isEmpty()) {
            Intent()
        } else {
            try {
                Intent.parseUri(string, 0)
            } catch (e: URISyntaxException) {
                Intent()
            }

        }
    }

    @JvmStatic
    fun getIcon(context: Context, item: Item?): IconProvider? = item?.iconProvider

    @JvmStatic
    fun getIcon(context: Context, filename: String?): Drawable? {
        if (filename == null) {
            return null
        }
        var icon: Drawable? = null
        val bitmap = BitmapFactory.decodeFile(context.filesDir.toString() + "/icons/" + filename + ".png")
        if (bitmap != null) {
            icon = BitmapDrawable(context.resources, bitmap)
        }
        return icon
    }

    @JvmStatic
    fun saveIcon(context: Context, icon: Bitmap?, filename: String) {
        val directory = File(context.filesDir.toString() + "/icons")
        if (!directory.exists()) {
            directory.mkdir()
        }

        val file = File(context.filesDir.toString() + "/icons/" + filename + ".png")
        removeIcon(context, filename)
        try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (icon != null)
            try {
                val out = FileOutputStream(file)
                icon.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

    }

    @JvmStatic
    fun removeIcon(context: Context, filename: String) {
        val file = File(context.filesDir.toString() + "/icons/" + filename + ".png")
        if (file.exists()) {
            try {
                file.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    @JvmStatic
    fun getItemOnTouchListener(item: Item?, itemGestureCallback: ItemGestureListener.ItemGestureCallback?): View.OnTouchListener {
        val itemGestureListener = if (Definitions.ENABLE_ITEM_TOUCH_LISTENER && itemGestureCallback != null) ItemGestureListener(Setup.appContext(), item, itemGestureCallback) else null
        return View.OnTouchListener { _, motionEvent ->
            if (CoreHome.launcher != null && !CoreHome.launcher!!.getDragNDropView().dragging) {
                CoreHome.itemTouchX = motionEvent.x
                CoreHome.itemTouchY = motionEvent.y
            }
            itemGestureListener?.onTouchEvent(motionEvent) ?: false
        }
    }

    @JvmStatic
    fun <A : AbstractApp> getRemovedApps(oldApps: List<A>, newApps: List<A>): List<A> {
        val removed = ArrayList<A>()
        // if this is the first call to this function and we did not know any app yet, we return an empty list
        if (oldApps.size == 0) {
            return removed
        }
        // we can't rely on sizes because apps may have been installed and uninstalled!
        //if (oldApps.size() > newApps.size()) {
        for (i in oldApps.indices) {
            if (!newApps.contains(oldApps[i])) {
                removed.add(oldApps[i])
                break
            }
        }
        //        }
        return removed
    }
}
