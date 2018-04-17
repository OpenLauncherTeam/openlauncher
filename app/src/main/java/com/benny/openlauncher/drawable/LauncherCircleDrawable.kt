package com.benny.openlauncher.drawable

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import com.benny.openlauncher.util.Tool

class LauncherCircleDrawable(context: Context, icon: Drawable, color: Int) : Drawable() {

    private val iconSize: Int
    private val iconSizeReal: Int
    private val iconPadding: Int
    private var icon: Bitmap? = null
    private var iconToFade: Bitmap? = null
    private val paint: Paint
    private val paint2: Paint

    private val scaleStep = 0.1f
    private var currentScale = 1f
    private var hidingOldIcon: Boolean = false

    init {
        this.icon = Tool.drawableToBitmap(icon)

        iconPadding = Tool.dp2px(6, context)

        iconSizeReal = icon.intrinsicHeight
        iconSize = icon.intrinsicHeight + iconPadding * 2

        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = color
        paint.alpha = 100
        paint.style = Paint.Style.FILL

        paint2 = Paint(Paint.ANTI_ALIAS_FLAG)
        paint2.isFilterBitmap = true
    }

    fun setIcon(icon: Drawable) {
        iconToFade = this.icon
        hidingOldIcon = true

        this.icon = Tool.drawableToBitmap(icon)
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        canvas.drawCircle((iconSize / 2).toFloat(), (iconSize / 2).toFloat(), (iconSize / 2).toFloat(), paint)

        if (iconToFade != null) {
            canvas.save()
            if (hidingOldIcon) {
                currentScale -= scaleStep
            } else {
                currentScale += scaleStep
            }
            currentScale = Tool.clampFloat(currentScale, 0.4f, 1f)
            canvas.scale(currentScale, currentScale, (iconSize / 2).toFloat(), (iconSize / 2).toFloat())
            canvas.drawBitmap(if (hidingOldIcon) iconToFade else icon, (iconSize / 2 - iconSizeReal / 2).toFloat(), (iconSize / 2 - iconSizeReal / 2).toFloat(), paint2)
            canvas.restore()

            if (currentScale == 0.4f) {
                hidingOldIcon = false
            }

            if (!hidingOldIcon && scaleStep == 1f) {
                iconToFade = null
            }

            invalidateSelf()
        } else {
            canvas.drawBitmap(icon!!, (iconSize / 2 - iconSizeReal / 2).toFloat(), (iconSize / 2 - iconSizeReal / 2).toFloat(), paint2)
        }
    }

    override fun getIntrinsicWidth(): Int {
        return iconSize
    }

    override fun getIntrinsicHeight(): Int {
        return iconSize
    }

    override fun setAlpha(i: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }
}
