package com.benny.openlauncher.core.widget

import `in`.championswimmer.sfg.lib.SimpleFingerGestures
import android.content.Context
import android.graphics.*
import android.support.v4.view.MotionEventCompat
import android.util.AttributeSet
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.benny.openlauncher.core.util.Tool
import com.benny.openlauncher.core.util.toPx
import java.util.*

open class CellContainer : ViewGroup {
    private val mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bgPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outlinePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val tempRect = Rect()
    var cellWidth: Int = 0
    var cellHeight: Int = 0
    var cellSpanV = 0
    var cellSpanH = 0
    var blockTouch = false
    var gestures: SimpleFingerGestures? = null
    var onItemRearrangeListener: OnItemRearrangeListener? = null
    private var occupied: Array<BooleanArray>? = null
    private lateinit var cells: Array<Array<Rect?>>
    private var hideGrid = true
    private var down: Long? = 0L
    private var animateBackground: Boolean = false
    private var preCoordinate = Point(-1, -1)
    private var startCoordinate: Point = Point()
    private var peekDownTime: Long? = -1L
    private var peekDirection: PeekDirection? = null
    private var cachedOutlineBitmap: Bitmap? = null
    private var currentOutlineCoordinate: Point = Point(-1, -1)
    private var tempOutlineCoordinate: Point = Point(-1, -1)

    val allCells: List<View>
        get() {
            val views = ArrayList<View>()
            for (i in 0 until childCount) {
                views.add(getChildAt(i))
            }
            return views
        }

    constructor(c: Context) : super(c) {
        init()
    }

    constructor(c: Context, attr: AttributeSet) : super(c, attr) {
        init()
    }

    fun setGridSize(x: Int, y: Int) {
        cellSpanV = y
        cellSpanH = x

        occupied = Array(cellSpanH) { BooleanArray(cellSpanV) }

        for (i in 0 until cellSpanH) {
            for (j in 0 until cellSpanV) {
                occupied!![i][j] = false
            }
        }
        requestLayout()
    }

    fun setHideGrid(hideGrid: Boolean) {
        this.hideGrid = hideGrid
        invalidate()
    }

    fun resetOccupiedSpace() {
        if (cellSpanH > 0 && cellSpanH > 0)
            occupied = Array(cellSpanH) { BooleanArray(cellSpanV) }
    }

    override fun removeAllViews() {
        resetOccupiedSpace()
        super.removeAllViews()
    }

    private fun getPeekDirectionFromCoordinate(from: Point, to: Point): PeekDirection? {
        if (from.y - to.y > 0)
            return PeekDirection.UP
        else if (from.y - to.y < 0)
            return PeekDirection.DOWN

        if (from.x - to.x > 0)
            return PeekDirection.LEFT
        else if (from.x - to.x < 0)
            return PeekDirection.RIGHT

        return null
    }

    private var newImageJustProjected: Boolean = false
    private var previousProjectedCoordinate: Point? = null

    fun projectImageOutlineAt(newCoordinate: Point, bitmap: Bitmap?) {
        cachedOutlineBitmap = bitmap

        if (currentOutlineCoordinate != newCoordinate)
            outlinePaint.alpha = 0

        currentOutlineCoordinate.set(newCoordinate.x, newCoordinate.y)

        invalidate()
    }

    fun hasCachedOutlineBitmap(): Boolean = cachedOutlineBitmap != null

    private fun drawCachedOutlineBitmap(canvas: Canvas, cell: Rect) {
        if (cachedOutlineBitmap != null)
            canvas.drawBitmap(cachedOutlineBitmap!!, cell.centerX() - cachedOutlineBitmap!!.width.toFloat() / 2, cell.centerY() - cachedOutlineBitmap!!.width.toFloat() / 2, outlinePaint)
    }

    fun clearCachedOutlineBitmap() {
        outlinePaint.alpha = 0
        cachedOutlineBitmap = null
        invalidate()
    }

    fun peekItemAndSwap(event: DragEvent, coordinate: Point): DragState = peekItemAndSwap(event.x.toInt(), event.y.toInt(), coordinate)

    /**
     * Test whether a moved Item is being moved over an existing Item and if so move the
     * existing Item out of the way. <P>
    </P> *
     *
     * TODO: Need to handle the dragged item having a size greater than 1x1
     *
     *
     * TODO: Need to handle moving the target back if the final drop location is not where the Item was moved from
     *
     * @param event - the drag event that contains the current x,y position
     */
    fun peekItemAndSwap(x: Int, y: Int, coordinate: Point): DragState {
        touchPosToCoordinate(coordinate, x, y, 1, 1, false, true)

        if (coordinate.x == -1 || coordinate.y == -1) {
            return DragState.OutOffRange
        }

        if (startCoordinate == null) {
            startCoordinate = coordinate
        }
        if (preCoordinate != coordinate) {
            peekDownTime = -1L
        }
        if (peekDownTime == -1L) {
            peekDirection = getPeekDirectionFromCoordinate(startCoordinate, coordinate)
            peekDownTime = System.currentTimeMillis()
            preCoordinate = coordinate
        }

        return if (!occupied!![coordinate.x][coordinate.y])
            DragState.CurrentNotOccupied
        else
            DragState.CurrentOccupied

        //        if (!(System.currentTimeMillis() - peekDownTime > 1000L)) {
        //            preCoordinate = coordinate;
        //            return null;
        //        }

        //        View targetView = coordinateToChildView(coordinate);
        //        if (targetView == null) return DragState.ItemViewNotFound;
        //        LayoutParams targetParams = (LayoutParams) targetView.getLayoutParams();
        //        if (targetParams.xSpan > 1 || targetParams.ySpan > 1)
        //            return null;
        //        occupied[targetParams.x][targetParams.y] = false;
        //        Point targetPoint = findFreeSpace(targetParams.x, targetParams.y, peekDirection);
        //
        //        startCoordinate = null;
        //        peekDownTime = -1L;
        //        preCoordinate.set(-1, -1);
        //
        //        onItemRearrange(new Point(targetParams.x, targetParams.y), targetPoint);
        //        targetParams.x = targetPoint.x;
        //        targetParams.y = targetPoint.y;
        //        occupied[targetPoint.x][targetPoint.y] = true;
        //        requestLayout();

        //        return null;
    }

    fun onItemRearrange(from: Point, to: Point) {
        if (onItemRearrangeListener != null)
            onItemRearrangeListener!!.onItemRearrange(from, to)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (MotionEventCompat.getActionMasked(event)) {
            MotionEvent.ACTION_DOWN -> down = System.currentTimeMillis()
            MotionEvent.ACTION_UP -> if (System.currentTimeMillis() - down!! < 260L && blockTouch) {
                performClick()
            }
        }
        if (blockTouch) return true
        if (gestures == null) {
            Tool.print("gestures is null")
            return super.onTouchEvent(event)
        }

        gestures!!.onTouch(this, event)
        return super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (blockTouch) {
            true
        } else super.onInterceptTouchEvent(ev)
    }

    init {
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 2f
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.color = Color.WHITE
        mPaint.alpha = 0

        bgPaint.style = Paint.Style.FILL
        bgPaint.color = Color.WHITE
        bgPaint.alpha = 0

        outlinePaint.color = Color.WHITE
        outlinePaint.alpha = 0
    }

    open fun init() {
        setWillNotDraw(false)
    }

    fun animateBackgroundShow() {
        animateBackground = true
        invalidate()
    }

    fun animateBackgroundHide() {
        animateBackground = false
        invalidate()
    }

    /**
     * Optimised version of findFreeSpace(1,1)
     *
     * @return the first empty space or null if no free space found
     */
    fun findFreeSpace(): Point? {
        for (y in 0 until occupied!![0].size) {
            for (x in occupied!!.indices) {
                if (!occupied!![x][y]) {
                    return Point(x, y)
                }
            }
        }
        return null
    }

    /**
     * Locate the first empty space large enough for the supplied dimensions
     *
     * @param spanX - the width of the required space
     * @param spanY - the height of the required space
     * @return the first empty space or null if no free space found
     */
    fun findFreeSpace(spanX: Int, spanY: Int): Point? {
        for (y in 0 until occupied!![0].size) {
            for (x in occupied!!.indices) {
                if (!occupied!![x][y] && !checkOccupied(Point(x, y), spanX, spanY)) {
                    return Point(x, y)
                }
            }
        }
        return null
    }

    /**
     * Locate the first 1x1 empty space near but not equal to the supplied starting position.
     *
     *
     * TODO: check this won't return the starting point if the starting point is surrounded by two occupied cells in each direction
     *
     * @param cx            - starting x coordinate
     * @param cy            - starting y coordinate
     * @param peekDirection - direction to look first or null
     * @return the first empty space or null if no free space found
     */
    fun findFreeSpace(cx: Int, cy: Int, peekDirection: PeekDirection?): Point? {
        if (peekDirection != null) {
            val target: Point
            when (peekDirection) {
                CellContainer.PeekDirection.DOWN -> {
                    target = Point(cx, cy - 1)
                    if (isValid(target.x, target.y) && !occupied!![target.x][target.y])
                        return target
                }
                CellContainer.PeekDirection.LEFT -> {
                    target = Point(cx + 1, cy)
                    if (isValid(target.x, target.y) && !occupied!![target.x][target.y])
                        return target
                }
                CellContainer.PeekDirection.RIGHT -> {
                    target = Point(cx - 1, cy)
                    if (isValid(target.x, target.y) && !occupied!![target.x][target.y])
                        return target
                }
                CellContainer.PeekDirection.UP -> {
                    target = Point(cx, cy + 1)
                    if (isValid(target.x, target.y) && !occupied!![target.x][target.y])
                        return target
                }
            }
        }
        val toExplore = LinkedList<Point>()
        val explored = HashSet<Point>()
        toExplore.add(Point(cx, cy))
        while (!toExplore.isEmpty()) {
            val p = toExplore.remove()
            var cp: Point
            if (isValid(p.x, p.y - 1)) {
                cp = Point(p.x, p.y - 1)
                if (!explored.contains(cp)) {
                    if (!occupied!![cp.x][cp.y])
                        return cp
                    else
                        toExplore.add(cp)
                    explored.add(p)
                }
            }

            if (isValid(p.x, p.y + 1)) {
                cp = Point(p.x, p.y + 1)
                if (!explored.contains(cp)) {
                    if (!occupied!![cp.x][cp.y])
                        return cp
                    else
                        toExplore.add(cp)
                    explored.add(p)
                }
            }

            if (isValid(p.x - 1, p.y)) {
                cp = Point(p.x - 1, p.y)
                if (!explored.contains(cp)) {
                    if (!occupied!![cp.x][cp.y])
                        return cp
                    else
                        toExplore.add(cp)
                    explored.add(p)
                }
            }

            if (isValid(p.x + 1, p.y)) {
                cp = Point(p.x + 1, p.y)
                if (!explored.contains(cp)) {
                    if (!occupied!![cp.x][cp.y])
                        return cp
                    else
                        toExplore.add(cp)
                    explored.add(p)
                }
            }
        }
        return null
    }

    private fun isValid(x: Int, y: Int): Boolean = x >= 0 && x <= occupied!!.size - 1 && y >= 0 && y <= occupied!![0].size - 1

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val s = 7f
        for (x in 0 until cellSpanH) {
            for (y in 0 until cellSpanV) {
                if (x >= cells.size || y >= cells[0].size) continue

                val cell = cells[x][y]!!

                canvas.save()
                canvas.rotate(45f, cell.left.toFloat(), cell.top.toFloat())
                canvas.drawRect(cell.left - s, cell.top - s, cell.left + s, cell.top + s, mPaint)
                canvas.restore()

                canvas.save()
                canvas.rotate(45f, cell.left.toFloat(), cell.bottom.toFloat())
                canvas.drawRect(cell.left - s, cell.bottom - s, cell.left + s, cell.bottom + s, mPaint)
                canvas.restore()

                canvas.save()
                canvas.rotate(45f, cell.right.toFloat(), cell.top.toFloat())
                canvas.drawRect(cell.right - s, cell.top - s, cell.right + s, cell.top + s, mPaint)
                canvas.restore()

                canvas.save()
                canvas.rotate(45f, cell.right.toFloat(), cell.bottom.toFloat())
                canvas.drawRect(cell.right - s, cell.bottom - s, cell.right + s, cell.bottom + s, mPaint)
                canvas.restore()
            }
        }

        //Animating alpha and drawing projected image
        if (currentOutlineCoordinate.x != -1 && currentOutlineCoordinate.y != -1) {
            if (outlinePaint.alpha != 160)
                outlinePaint.alpha = Math.min(outlinePaint.alpha + 20, 160)
            drawCachedOutlineBitmap(canvas, cells[currentOutlineCoordinate.x][currentOutlineCoordinate.y]!!)
            if (outlinePaint.alpha != 160)
                invalidate()
        }

        //Animating alpha
        if (hideGrid && mPaint.alpha != 0) {
            mPaint.alpha = Math.max(mPaint.alpha - 20, 0)
            invalidate()
        } else if (!hideGrid && mPaint.alpha != 255) {
            mPaint.alpha = Math.min(mPaint.alpha + 20, 255)
            invalidate()
        }

        //Animating alpha
        if (!animateBackground && bgPaint.alpha != 0) {
            bgPaint.alpha = Math.max(bgPaint.alpha - 10, 0)
            invalidate()
        } else if (animateBackground && bgPaint.alpha != 100) {
            bgPaint.alpha = Math.min(bgPaint.alpha + 10, 100)
            invalidate()
        }
    }

    override fun addView(child: View) {
        val lp = child.layoutParams as CellContainer.LayoutParams
        setOccupied(true, lp)
        super.addView(child)
    }

    override fun removeView(view: View) {
        val lp = view.layoutParams as CellContainer.LayoutParams
        setOccupied(false, lp)
        super.removeView(view)
    }

    fun addViewToGrid(view: View, x: Int, y: Int, xSpan: Int, ySpan: Int) {
        val lp = LayoutParams(WRAP_CONTENT, WRAP_CONTENT, x, y, xSpan, ySpan)
        view.layoutParams = lp
        addView(view)
    }

    fun addViewToGrid(view: View) {
        addView(view)
    }

    fun setOccupied(b: Boolean, lp: LayoutParams) {
        for (x in lp.x until lp.x + lp.xSpan) {
            for (y in lp.y until lp.y + lp.ySpan) {
                occupied!![x][y] = b
            }
        }
    }

    /**
     * Check if there is sufficient unoccupied space to accept an Item with the given span with it's
     * top left corner at the given starting point
     *
     * @param start - The top left corner of the proposed Item location
     * @param spanX - The Item width in cells
     * @param spanY - The Item height in cells
     * @return - true if occupied, false if not
     */
    fun checkOccupied(start: Point, spanX: Int, spanY: Int): Boolean {
        if (start.x + spanX > occupied!!.size || start.y + spanY > occupied!![0].size) {
            return true
        }
        for (y in start.y until start.y + spanY) {
            for (x in start.x until start.x + spanX) {
                if (occupied!![x][y]) {
                    return true
                }
            }
        }
        return false
    }

    fun coordinateToChildView(pos: Point?): View? {
        if (pos == null) return null
        for (i in 0 until childCount) {
            val lp = getChildAt(i).layoutParams as LayoutParams
            if (pos.x >= lp.x && pos.y >= lp.y && pos.x < lp.x + lp.xSpan && pos.y < lp.y + lp.ySpan) {
                return getChildAt(i)
            }
        }
        return null
    }

    fun coordinateToLayoutParams(mX: Int, mY: Int, xSpan: Int, ySpan: Int): LayoutParams? {
        val pos = Point()
        touchPosToCoordinate(pos, mX, mY, xSpan, ySpan, true)
        return if (!pos.equals(-1, -1)) {
            LayoutParams(WRAP_CONTENT, WRAP_CONTENT, pos.x, pos.y, xSpan, ySpan)
        } else null
    }

    // convert a touch event to a coordinate in the cell container
    @JvmOverloads
    fun touchPosToCoordinate(coordinate: Point, mX: Int, mY: Int, xSpan: Int, ySpan: Int, checkAvailability: Boolean, checkBoundary: Boolean = false) {
        var mX = mX
        var mY = mY
        mX -= (xSpan - 1) * cellWidth / 2
        mY -= (ySpan - 1) * cellHeight / 2

        var x = 0
        while (x < cellSpanH) {
            var y = 0
            while (y < cellSpanV) {
                val cell = cells[x][y]!!
                if (mY >= cell.top && mY <= cell.bottom && mX >= cell.left && mX <= cell.right) {
                    if (checkAvailability) {
                        if (occupied!![x][y]) {
                            coordinate.set(-1, -1)
                            return
                        }

                        var dx = x + xSpan - 1
                        var dy = y + ySpan - 1

                        if (dx >= cellSpanH - 1) {
                            dx = cellSpanH - 1
                            x = dx + 1 - xSpan
                        }
                        if (dy >= cellSpanV - 1) {
                            dy = cellSpanV - 1
                            y = dy + 1 - ySpan
                        }

                        for (x2 in x until x + xSpan) {
                            for (y2 in y until y + ySpan) {
                                if (occupied!![x2][y2]) {
                                    coordinate.set(-1, -1)
                                    return
                                }
                            }
                        }
                    }
                    if (checkBoundary) {
                        val offsetCell = Rect(cell)
                        val dp2 = 6.toPx()
                        offsetCell.inset(dp2, dp2)
                        if (mY >= offsetCell.top && mY <= offsetCell.bottom && mX >= offsetCell.left && mX <= offsetCell.right) {
                            coordinate.set(-1, -1)
                            return
                        }
                    }
                    coordinate.set(x, y)
                    return
                }
                y++
            }
            x++
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = r - l - paddingLeft - paddingRight
        val height = b - t - paddingTop - paddingBottom

        if (cellSpanH == 0)
            cellSpanH = 1
        if (cellSpanV == 0)
            cellSpanV = 1

        cellWidth = width / cellSpanH
        cellHeight = height / cellSpanV

        initCellInfo(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom)

        val count = childCount

        for (i in 0 until count) {
            val child = getChildAt(i)

            if (child.visibility == View.GONE)
                continue

            val lp = child.layoutParams as CellContainer.LayoutParams

            val childWidth = lp.xSpan * cellWidth
            val childHeight = lp.ySpan * cellHeight
            child.measure(View.MeasureSpec.makeMeasureSpec(childWidth, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(childHeight, View.MeasureSpec.EXACTLY))

            val upRect = cells[lp.x][lp.y]!!
            var downRect = tempRect
            if (lp.x + lp.xSpan - 1 < cellSpanH && lp.y + lp.ySpan - 1 < cellSpanV)
                downRect = cells[lp.x + lp.xSpan - 1][lp.y + lp.ySpan - 1]!!

            if (lp.xSpan == 1 && lp.ySpan == 1)
                child.layout(upRect.left, upRect.top, upRect.right, upRect.bottom)
            else if (lp.xSpan > 1 && lp.ySpan > 1)
                child.layout(upRect.left, upRect.top, downRect.right, downRect.bottom)
            else if (lp.xSpan > 1)
                child.layout(upRect.left, upRect.top, downRect.right, upRect.bottom)
            else if (lp.ySpan > 1)
                child.layout(upRect.left, upRect.top, upRect.right, downRect.bottom)
        }
    }

    private fun initCellInfo(l: Int, t: Int, r: Int, b: Int) {
        cells = Array(cellSpanH) { arrayOfNulls<Rect>(cellSpanV) }

        var curLeft = l
        var curTop = t
        var curRight = l + cellWidth
        var curBottom = t + cellHeight

        for (i in 0 until cellSpanH) {
            if (i != 0) {
                curLeft += cellWidth
                curRight += cellWidth
            }
            for (j in 0 until cellSpanV) {
                if (j != 0) {
                    curTop += cellHeight
                    curBottom += cellHeight
                }

                val rect = Rect(curLeft, curTop, curRight, curBottom)
                cells!![i][j] = rect
            }
            curTop = t
            curBottom = t + cellHeight
        }
    }

    enum class DragState {
        CurrentNotOccupied, OutOffRange, ItemViewNotFound, CurrentOccupied
    }

    enum class PeekDirection {
        UP, LEFT, RIGHT, DOWN
    }

    interface OnItemRearrangeListener {
        fun onItemRearrange(from: Point, to: Point)
    }

    class LayoutParams : ViewGroup.LayoutParams {
        var x: Int = 0
        var y: Int = 0
        var xSpan = 1
        var ySpan = 1

        constructor(w: Int, h: Int, x: Int, y: Int) : super(w, h) {
            this.x = x
            this.y = y
        }

        constructor(w: Int, h: Int, x: Int, y: Int, xSpan: Int, ySpan: Int) : super(w, h) {
            this.x = x
            this.y = y

            this.xSpan = xSpan
            this.ySpan = ySpan
        }

        constructor(w: Int, h: Int) : super(w, h) {}
    }
}
