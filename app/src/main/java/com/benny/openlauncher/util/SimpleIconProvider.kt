package com.benny.openlauncher.util

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.ImageView
import android.widget.TextView

import com.benny.openlauncher.interfaces.IconDrawer
import com.benny.openlauncher.interfaces.IconProvider
import com.benny.openlauncher.manager.Setup
import com.benny.openlauncher.viewutil.GroupIconDrawable

open class SimpleIconProvider : BaseIconProvider {

    protected var drawable: Drawable? = null
        get() {
            if (field != null) {
                return field
            } else if (drawableResource > 0) {
                return Setup.appContext().resources.getDrawable(drawableResource)
            }
            return null
        }

    private var drawableResource: Int = 0

    constructor(drawable: Drawable?) {
        this.drawable = drawable
        this.drawableResource = -1
    }

    constructor(drawableResource: Int) {
        this.drawable = null
        this.drawableResource = drawableResource
    }

    override fun loadIcon(type: IconProvider.IconTargetType, forceSize: Int, target: Any, vararg args: Any) {
        when (type) {
            IconProvider.IconTargetType.ImageView -> {
                val iv = target as ImageView
                var d = drawable
                d = scaleDrawable(d, forceSize)
                iv.setImageDrawable(d)
            }
            IconProvider.IconTargetType.TextView -> {
                val tv = target as TextView
                val gravity = args[0] as Int
                var d = drawable
                d = scaleDrawable(d, forceSize)
                if (gravity == Gravity.LEFT || gravity == Gravity.START) {
                    tv.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null)
                } else if (gravity == Gravity.RIGHT || gravity == Gravity.END) {
                    tv.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null)
                } else if (gravity == Gravity.TOP) {
                    tv.setCompoundDrawablesWithIntrinsicBounds(null, d, null, null)
                } else if (gravity == Gravity.BOTTOM) {
                    tv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, d)
                }
            }
            IconProvider.IconTargetType.IconDrawer -> {
                val iconDrawer = target as IconDrawer
                val index = args[0] as Int
                // we simply load the drawable in a synchronised way
                iconDrawer.onIconAvailable(drawable, index)
            }
        }
    }

    override fun cancelLoad(type: IconProvider.IconTargetType, target: Any) {
        // nothing to cancel... we load everything in an synchronous way
    }

    override fun getDrawableSynchronously(forceSize: Int): Drawable? {
        var d = drawable
        d = scaleDrawable(d, forceSize)
        return d
    }

    override fun isGroupIconDrawable(): Boolean = drawable != null && drawable is GroupIconDrawable

    private fun scaleDrawable(drawable: Drawable?, forceSize: Int): Drawable? {
        var _drawable = drawable
        var _forceSize = forceSize
        if (drawable != null && forceSize != Definitions.NO_SCALE) {
            _forceSize = Tool.toPx(_forceSize)
            _drawable = BitmapDrawable(Setup.appContext().resources, Bitmap.createScaledBitmap(Tool.drawableToBitmap(drawable)!!, _forceSize, _forceSize, true))
        }
        return _drawable
    }

}