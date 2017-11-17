package com.benny.openlauncher.core.drawable

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import com.benny.openlauncher.core.manager.Setup
import com.benny.openlauncher.core.util.Tool
import com.benny.openlauncher.core.util.toPx

/**
 * Created by BennyKok on 11/17/2017.
 */
class LauncherActionDrawable(val icon: Bitmap) : Drawable() {

    constructor(drawable: Int) : this(
            Tool.drawableToBitmap(Setup.appContext().resources.getDrawable(drawable))!!
    )

    private val iconSize = Setup.appSettings().desktopIconSize.toPx()
    private val iconRadius = iconSize / 2f - 4.toPx()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val filterDkGray = LightingColorFilter(Color.DKGRAY, 1)

    init {
        paint.color = Color.WHITE
        paint.isFilterBitmap = true
    }

    override fun draw(canvas: Canvas?) {
        if (canvas == null) return

        paint.colorFilter = null
        canvas.drawCircle(iconSize / 2f, iconSize / 2f, iconRadius, paint)
        paint.colorFilter = filterDkGray
        canvas.drawBitmap(icon, (iconSize - icon.width) / 2f, (iconSize - icon.width) / 2f, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity(): Int = 1

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

}