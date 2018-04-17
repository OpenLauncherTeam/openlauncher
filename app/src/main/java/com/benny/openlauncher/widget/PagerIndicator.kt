package com.benny.openlauncher.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.benny.openlauncher.manager.Setup
import com.benny.openlauncher.util.Tool
import com.benny.openlauncher.widget.PagerIndicator.Mode.ARROW
import com.benny.openlauncher.widget.PagerIndicator.Mode.NORMAL

class PagerIndicator : View, SmoothViewPager.OnPageChangeListener {

    private var pager: SmoothViewPager? = null

    private var mode = Mode.NORMAL

    private var dotSize: Float = 0.toFloat()
    private var dotPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var scaleFactor = 1f
    private var scaleFactor2 = 1.5f

    private var previousPage = -1

    private var realPreviousPage: Int = 0

    private var arrowPath: Path? = null
    private var arrowPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var myX = 0f

    private var prePageCount: Int = 0

    private var scrollOffset = 0f
    private var scrollPagePosition = 0
    private var alphaFade = false
    private var alphaShow = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        pad = Tool.toPx(3).toFloat()

        setWillNotDraw(false)
        dotPaint.color = Color.WHITE
        dotPaint.strokeWidth = Tool.toPx(2).toFloat()
        dotPaint.isAntiAlias = true

        arrowPaint.color = Color.WHITE
        arrowPaint.isAntiAlias = true
        arrowPaint.style = Paint.Style.STROKE
        arrowPaint.strokeWidth = pad / 1.5f
        arrowPaint.strokeJoin = Paint.Join.ROUND

        arrowPath = Path()

        mode = Setup.appSettings().desktopIndicatorMode
    }

    fun setMode(mode: Int) {
        this.mode = mode
        invalidate()
    }

    fun setOutlinePaint() {
        dotPaint!!.style = Paint.Style.STROKE
        invalidate()
    }

    fun setFillPaint() {
        dotPaint.style = Paint.Style.FILL
        invalidate()
    }

    fun setColor(c: Int) {
        dotPaint.color = c
        invalidate()
    }

    fun setViewPager(pager: SmoothViewPager?) {
        if (pager == null) {
            if (this.pager != null) {
                this.pager!!.removeOnPageChangeListener(this)
                this.pager = null
                //getLayoutParams().width = 0;
                invalidate()
            }
            return
        }
        this.pager = pager
        prePageCount = pager.adapter.count
        pager.addOnPageChangeListener(this)

        Tool.print(pager.adapter.count)
        //getLayoutParams().width = Math.round(this.pager.getAdapter().getCount() * (dotSize + pad * 2));
        invalidate()
    }

    private var hasTriggedAlphaShow = false

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (prePageCount != pager!!.adapter.count) {
            //getLayoutParams().width = Math.round(pager.getAdapter().getCount() * (dotSize + pad * 2));
            prePageCount = pager!!.adapter.count
        }
        scrollOffset = positionOffset
        scrollPagePosition = position

        invalidate()
    }

    override fun onPageSelected(position: Int) {}

    fun showNow() {
        removeCallbacks(delayShow)
        alphaShow = true
        alphaFade = false
        invalidate()
    }

    private val delayShow = Runnable {
        alphaFade = true
        alphaShow = false
        invalidate()
    }

    fun hideDelay() {
        postDelayed(delayShow, 500)
    }

    var mCurrentPagerState: Int = -1

    override fun onPageScrollStateChanged(state: Int) {
        mCurrentPagerState = state
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        dotSize = height - pad * 1.25f
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas) {
        dotSize = height - pad * 1.25f

        when (mode) {
            NORMAL -> if (pager != null) {

                dotPaint.alpha = 255

                val circlesWidth = pager!!.adapter.count * (dotSize + pad * 2)
                canvas.translate(width / 2 - circlesWidth / 2, 0f)

                if (realPreviousPage != pager!!.currentItem) {
                    scaleFactor = 1f
                    realPreviousPage = pager!!.currentItem
                }

                for (i in 0 until pager!!.adapter.count) {
                    val targetFactor = 1.5f
                    val targetFactor2 = 1f
                    val increaseFactor = 0.05f
                    if (i == previousPage && i != pager!!.currentItem) {
                        scaleFactor2 = Tool.clampFloat(scaleFactor2 - increaseFactor, targetFactor2, targetFactor)
                        Tool.print(scaleFactor2)
                        canvas.drawCircle(dotSize / 2 + pad + (dotSize + pad * 2) * i, (height / 2).toFloat(), scaleFactor2 * dotSize / 2, dotPaint!!)
                        if (scaleFactor2 != targetFactor2)
                            invalidate()
                        else {
                            scaleFactor2 = 1.5f
                            previousPage = -1
                        }
                    } else if (pager!!.currentItem == i) {
                        if (previousPage == -1)
                            previousPage = i
                        scaleFactor = Tool.clampFloat(scaleFactor + increaseFactor, targetFactor2, targetFactor)
                        canvas.drawCircle(dotSize / 2 + pad + (dotSize + pad * 2) * i, (height / 2).toFloat(), scaleFactor * dotSize / 2, dotPaint!!)
                        if (scaleFactor != targetFactor)
                            invalidate()
                    } else {
                        canvas.drawCircle(dotSize / 2 + pad + (dotSize + pad * 2) * i, (height / 2).toFloat(), dotSize / 2, dotPaint!!)
                    }
                }
            }
            ARROW -> if (pager != null) {
                arrowPath!!.reset()
                arrowPath!!.moveTo(width / 2 - dotSize * 1.5f, height.toFloat() - dotSize / 3 - pad / 2)
                arrowPath!!.lineTo((width / 2).toFloat(), pad / 2)
                arrowPath!!.lineTo(width / 2 + dotSize * 1.5f, height.toFloat() - dotSize / 3 - pad / 2)

                canvas.drawPath(arrowPath!!, arrowPaint)

                val lineWidth = width / pager!!.adapter.count
                val currentStartX = scrollPagePosition * lineWidth

                myX = currentStartX + scrollOffset * lineWidth

                if (myX % lineWidth != 0f)
                    invalidate()

                if (alphaFade) {
                    dotPaint.alpha = Tool.clampInt(dotPaint.alpha - 10, 0, 255)
                    if (dotPaint.alpha == 0)
                        alphaFade = false
                    invalidate()
                }

                if (alphaShow) {
                    dotPaint.alpha = Tool.clampInt(dotPaint.alpha + 10, 0, 255)
                    if (dotPaint.alpha == 255) {
                        alphaShow = false
                    }
                    invalidate()
                }

                canvas.drawLine(myX, height.toFloat(), myX + lineWidth.toFloat(), height.toFloat(), dotPaint)
            }
        }
    }

    object Mode {
        val NORMAL = 0
        val ARROW = 1
    }

    companion object {

        private var pad: Float = 0.toFloat()
    }
}
